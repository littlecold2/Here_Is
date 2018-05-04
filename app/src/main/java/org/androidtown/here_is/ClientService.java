package org.androidtown.here_is;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientService extends Service implements Runnable {

    private Socket s;   // 소켓통신할 소켓
    private BufferedReader inMsg; // 받은 메시지 읽을 버퍼
    private PrintWriter outMsg; // 메세지 보낼 라이터

    private String a_targetIp = "13.124.63.18"; // 서버 ip
    private int a_targetPort = 9000; // 서버 port

    private List<Message> message_List; // 서버에서 받은 메시지 파싱해 넣어 둘 함수
    private String chat_text="채팅방 비어있음.\n"; // 여기에 채팅방 내용을 저장하고 있다.
    private String current_chat_text; // 새로 도착한 채팅 메시지만 저장 // 맵엑티비티에 팝업 용


    private final IBinder mBinder = new Mybinder();
    private LocationManager locationManager; // 위치 갱신 관련
    private MyLocationListener listener; //위치 갱신 관련
    private Thread myThread;

    private Location lastKnownLocation = null ; // 최근 받아온 위치

    private String j_inmsg=""; // 서버에서 받은 메시지 저장
    private String j_outmsg=""; // 서버에 보낼 메시지

    private boolean key_server_ok = false; // 서버 연결됬을 시 true
    private boolean key_location_ok = false; // 위치정보 받아 왔을 시 true
    private int chat_room=-1; // 채팅방 번호, 채팅 없을시 -1

    private String chat_name = "비어있음"; // 채팅 상대방 이름 저장
    private String chat_id="";
    private int chat_image_index = 1; // 채팅 상대방 이미지 인덱스 저장 //이런걸 싱글톤으로 할껄

    // 브로드캐스트
    private static final String EXTRA_GET_MESSAGE ="current_chat_message"; // 새로온 채팅 내용
    private static final String EXTRA_ALL_MESSAGE ="all_chat_message"; //모든 채팅 내용
    private static final String EXTRA_LOC_MESSAGE ="all_loc_message"; // 위치정보

    // 저장된 유저 정보 관련
    private SharedPreferences userinfo;
    private String myID;
    private String myName;
    private String myIntro;
    private int myImage_index;
    private String myUrl;

    class Mybinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CSV","Service onCreate");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener); // 서비스 생기면 네트워크 위치 한번 불러옴
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener); // 서비스 생기면 GPS 위치 한번 불러옴

        message_List = new ArrayList<>(); // 서버에서 받은 메시지 파싱해 넣어 둘 함수

        //저장된 정보들 변수에 저장
        userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        myID = userinfo.getString("ID","");
        myName= userinfo.getString("NAME","");
        myIntro= userinfo.getString("INFO","");
        myImage_index= Integer.parseInt(userinfo.getString("INDEX",""));
        myUrl= userinfo.getString("URL","");


        myThread= new Thread(this);
        myThread.start();


    }

    // 스타트 서비스시 실행됨
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.d("CSV","onStartCommand");
        SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE); // 채팅 전체 내용 전송
        SendBroadcast_chat_set(chat_id,chat_name,chat_image_index); // 상대방이미지, 이름 줌
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        Log.d("CSV","Service onBind");
        return mBinder;
    }


    @Override
    public void onDestroy() {

        Log.d("SCV","destroy");

            if(key_server_ok)
            {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //위치 갱신 중단
        locationManager.removeUpdates(listener);
            locationManager.removeUpdates(listener);
            myThread.interrupt(); // 쓰레드 중단

        super.onDestroy();
    }


    @SuppressLint("MissingPermission")
    public void run() // 쓰레드 시작부분
    {
        Log.d("CSV", a_targetIp+ " " +String.format(Locale.KOREA,"%d",a_targetPort));

        
        while ( !myThread.isInterrupted()) //
        {
            try {
                if (key_location_ok && !key_server_ok &&(s == null || s.isClosed())) { // 위치갱신 됬지만 서버는 연결 안됬을 경우
                    connectServer(a_targetIp, a_targetPort); // 서버에 연결
                }
                if (key_server_ok&& s != null) { // 서버 연결됬을 경우
                    Log.d("CSV", "msging");
                    MessageController(); // 메시지 컨트롤하는 함수
                }
                else {
                    Log.d("CSV", "locating....");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener); // 서비스 생기면 네트워크 위치 한번 불러옴
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener); // 서비스 생기면 GPS 위치 한번 불러옴
                    Thread.sleep(3000);
                }

            }catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 인터럽트가 트라이 캐치문 들어올시 while 문 꺼질수 있게 다시 인터럽트넣음
                    e.printStackTrace();
                }
        }


    }
    public void connectServer(String targetIp,int targetPort) // 서버에 연결하는부분
    {
        try{
            // 소켓 생성
            s = new Socket(targetIp,targetPort);
                Log.d("D_socket", "[Client]Server 연결 성공!!");


            // 입출력 스트림 생성
            inMsg = new BufferedReader(new InputStreamReader(s.getInputStream())); // 수신 메시지 담을 버퍼
            outMsg = new PrintWriter(s.getOutputStream(),true); //송신 메시지 롸이터
            outMsg.println(j_outmsg); // 현재 위치 메시지 한번 보냄

            key_server_ok=true;

        }catch(Exception e) {
            Log.d("D_socket", "Error : " + e);
        }
    }// connectServer()

    public void MessageController() // 서버와의 메시지 관리
    {
        Gson gson = new Gson(); // JSon 직렬화 해서 편하게 쓰는 Gson

        if(s.isClosed() ) // 소켓 연경 안되잇으면
        {
            return;
        }
        try {
            Log.d("CSV", "j_outmsg: "+j_outmsg);
            j_inmsg = inMsg.readLine(); // 서버에서 보낸 메시지 수신
            message_List= gson.fromJson(j_inmsg, new TypeToken<ArrayList<Message>>() {}.getType()); // 서버에서 받은 메시지(다른 클라이언트의 이름,위치 메시지 리스트 등)를 JSON->Gosn-> ArrayList<Message>로 해서 저장

            if(message_List.get(0).getChat_type().equals("location"))
            {
                outMsg.println(j_outmsg); // 위치정보 메시지 받았으면 내 위치정보 보냄
                SendBroadcast_map(j_inmsg,EXTRA_LOC_MESSAGE); // 맵 엑티비티에 넘겨줌
            }
            else if(message_List.get(0).getChat_type().equals("room_req")&& chat_room==-1 // 채팅 하고 있지 않고 채팅 신청 받을 경우
                    &&message_List.get(0).getChat_id()[1].equals(myID)) // 그리고 그게 내 아이디 일 경우
            {
                SendBroadcast_chat_req(message_List.get(0).getChat_id()[0],message_List.get(0).getName(),message_List.get(0).getImage()); // 채팅 신청 왔다고 맵엑티비티에 알려줘서 대화상자 띄울 수 있게함
                Log.d("req",message_List.get(0).getChat_id()[0]);
                return;
            }

            else if(message_List.get(0).getChat_type().equals("room_set")&& chat_room==-1 // 채팅방 만드는 메시지 수신하고 // 채팅 하고 있지 않을 경우
                    &&(message_List.get(0).getChat_id()[0].equals(myID)||message_List.get(0).getChat_id()[1].equals(myID))) // 채팅 set 메시지중 내아이디가 있는 경우
            {
                    chat_room=message_List.get(0).getChat_room(); // 채팅 방번호 세팅
                    chat_text = message_List.get(0).getChat_name()[0] +"님이 입장 하였습니다.\n";
                    chat_text += message_List.get(0).getChat_name()[1] +"님이 입장 하였습니다.\n";

                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent); // 채팅 엑티비티 실행
            }
            else if(message_List.get(0).getChat_type().equals("chat")&&chat_room==message_List.get(0).getChat_room()) // 채팅 메시지 경우 // 채팅방이 내 채팅방일 경우 받음
            {
                chat_text += message_List.get(0).getName()+(": ")+ message_List.get(0).getChat_text()+("\n"); // 전체 채팅 메시지에 추가
                current_chat_text = "\n "+message_List.get(0).getName()+(": ")+ message_List.get(0).getChat_text(); // 현재 채팅 메시지
                SendBroadcast_map(current_chat_text,EXTRA_GET_MESSAGE); // 맵엑티비테 에 현재 온 메시지줌
                SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE); // 채팅 엑티비티에 전체 메시지줌
            }
            else if(message_List.get(0).getChat_type().equals("chat_logout")&&chat_room==message_List.get(0).getChat_room()) // 내 채팅방 로그아웃 메시지 받았을 경우
            {
                chat_text+="상대방이 채팅방을 떠났습니다.\n";
                SendBroadcast_map("\n   상대방이 채팅방을 떠났습니다.",EXTRA_GET_MESSAGE);
                SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE);
                chat_room =-1;
            }
            Log.d("CSV","j_inmsg: "+j_inmsg);
            Log.d("chat","now chatroom: " +chat_room);

        } catch(NullPointerException e) {
            key_server_ok=false;
            try {
                s.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;}
        catch (IOException e) {
            key_server_ok=false;
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return;
        }

    }// messageController

    private void SendBroadcast_chat(String message,String key) { // 채팅 메시지 관련
        Intent it = new Intent("EVENT_CHAT");
        if (!TextUtils.isEmpty(message))
            it.putExtra(key,message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_chat_set(String id ,String name,int image_index) { // 채팅방 만들어질 때 관련
        Intent it = new Intent("EVENT_CHAT_SET");


        if (!TextUtils.isEmpty(name)){
            it.putExtra("id",id);
            it.putExtra("name",name);
            it.putExtra("image",image_index);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_map(String message,String key) { // // 현재 채팅 메시지, 주변 유저 위치정보
        Intent it = new Intent("EVENT_STRING_TO_MAP");


        if (!TextUtils.isEmpty(message))
            it.putExtra(key,message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_chat_req(String id,String name,int image) { // 맵 엑티비티에 보냄, 채팅 요청 왔을 때 관련
        Intent it = new Intent("EVENT_CHAT_REQ_MAP");
Log.d("req","reqbraod");
        if (!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(id)) {
            it.putExtra("ID", id);
            it.putExtra("NAME", name);
            it.putExtra("IMAGE", image);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_loc(Double Lat,Double Lng) { // 맵 엑티비티에 보냄, 내 현재 위치 정보
        Intent it = new Intent("EVENT_LOC");


        if (Lat!=null) {
            it.putExtra("Lat", Lat);
            it.putExtra("Lng", Lng);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }


    public boolean get_key_location_ok(){return  key_location_ok;}
    public boolean get_key_server_ok(){return  key_server_ok;}
    public Location getMyLocation(){return lastKnownLocation;}
    public int getChat_room(){return chat_room;}
    public String getChat_name(){return chat_name;}
    public int getMyImage_index(){return chat_image_index;}
    public void setChat_name(String name){chat_name = name;}
    public void setChat_image_index(int img){chat_image_index = img;}
    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public void sendMessage(String outmsg) {Log.d("chat",outmsg); outMsg.println(outmsg);}


    public String Jsonize(String id, String name, String intro,int image,String url ,Double lat,  Double lng,String chat_type) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,name,intro,image,url,lat,lng,chat_type)); //Data -> Gson -> json
        return json;

    }
    // chat
    public String Jsonize(String id, String name,int chat_room,String chat_type,String chat_text) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,name,chat_room,chat_type,chat_text)); //Data -> Gson -> json
        return json;

    }


    // 내 위치 갱신
    public class MyLocationListener implements LocationListener
    {

        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(Location location) {
            // Get the last location.
            Log.d("loclistener","loc");
            lastKnownLocation = location; // 업데이트 된 주소 저장
            Log.d("lastKnownLocation : ",lastKnownLocation.toString());
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, // 네트워크+gps 이용 업데이트
                    1500, //1초마다
                    0, // 최소 거리 10미터
                    listener
            );
            if(s==null) // 서버와 연결 안됬으면 현재 위치 텍스트뷰에
            {
                Log.d("CSV", "n" +
                        "ot connect");
            }
            else { // 서버 연결 됫으면 메세지 받은 걸 텍스트 뷰에 뿌림
//                Toast.makeText(getApplicationContext(),"메시지 받음",Toast.LENGTH_SHORT).show();
//                for(Userdata ud:message_List) {
//                    Log.d("CSV","Userlist: "+"name: " + ud.getName() + " lat: " + ud.getLat() + " lng: " + ud.getLng()+"\n");
//                }
            }
            if( lastKnownLocation.hasAltitude() && lastKnownLocation!=null) { // 위치 받아 왔을 경우
                Log.d("!!!!!!!", "n" +lastKnownLocation);
                SendBroadcast_loc(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()); // 맵 엑티비티에 정보 전달
                j_outmsg = Jsonize(myID, myName,myIntro,myImage_index,myUrl,lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),"location"); // 서버로 보낼 메시지에 담음
                key_location_ok=true;
            }

        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(),"GPS 켜짐.",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),"GPS 꺼짐.",Toast.LENGTH_LONG).show();
        }
    }



}






