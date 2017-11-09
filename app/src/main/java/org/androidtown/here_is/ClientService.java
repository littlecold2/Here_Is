package org.androidtown.here_is;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

    private List<Message> message_List;
    private List<Message> location_List;
    private String chat_text="채팅방 비어있음.";
    private String current_chat_text;

    private final IBinder mBinder = new Mybinder();
    private LocationManager locationManager;
    private MyLocationListener listener;
    private Thread myThread;

    private Location lastKnownLocation = null ;

    private String j_inmsg=""; // 받은 메시지 저장
    private String j_outmsg="";
    private boolean key_getMessage_ok = false;
    private boolean key_location_ok = false;
    private boolean key_gps_ok =false;
    private boolean key_socket_ok = false;
    private boolean key_chat_ok =false;
    private int chat_room=-1;

    private static final String EXTRA_GET_MESSAGE ="current_chat_message";
    private static final String EXTRA_ALL_MESSAGE ="all_chat_message";
    private static final String EXTRA_LOC_MESSAGE ="all_loc_message";

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        location_List = new ArrayList<>();
        message_List = new ArrayList<>();
        myThread= new Thread(this);
        myThread.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.d("CSV","onStartCommand");
        SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        Log.d("CSV","Service onBind");
        return mBinder;
    }


    @Override
    public void onDestroy() {

        Log.d("SCV","destroy");
        try {
            if(!s.isClosed()) {
                inMsg.close();
                outMsg.close();
                s.close();
            }
            locationManager.removeUpdates(listener);
            locationManager.removeUpdates(listener);
            //myThread.interrupt();
            //myThread.interrupt();
            myThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    // 소켓통신 부분 시작





    protected void finalize() throws Throwable
    {
//        if(s!=null)
//            s.close(); // 끝날때 소켓 닫음
    }




    public void run() // 쓰레드 시작부분
    {

        Log.d("CSV", a_targetIp+ " " +String.format(Locale.KOREA,"%d",a_targetPort));


        while ( !myThread.isInterrupted()) //
        {
            //Log.d("CSV", "service ~~~~~~~~~");
            if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                Log.d("CSV", "no gps connect");
                key_gps_ok =false;
            }
            else
            {
                key_gps_ok =true;
            }
            if(s==null||s.isClosed()) {
                key_getMessage_ok =false;
                connectServer(a_targetIp, a_targetPort);
            }
            if(get_key_getlocation_ok()&&s!=null&&!s.isClosed()) {
                    // 서버에 연결 하는 함수 받아온 ip.port 넘겨줌
                    Log.d("CSV", "msging");
                    if(!key_getMessage_ok)
                        sendMessage(j_outmsg);
                    MessageController(); // 서버에 현재 위치정보 담아서 보냄

            }
            else
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
        }


    }
    public void connectServer(String targetIp,int targetPort) // 서버에 연결하는부분
    {
        try{
            // 소켓 생성
            s = new Socket(targetIp,targetPort); // 소켓연결 성공 실패시
                Log.d("D_socket", "[Client]Server 연결 성공!!");


            // 입출력 스트림 생성
            inMsg = new BufferedReader(new InputStreamReader(s.getInputStream())); // 수신 메시지 담을 버퍼
            outMsg = new PrintWriter(s.getOutputStream(),true); //송신 메시지 롸이터
            Log.d("D_socket", Build.USER);



        }catch(Exception e) {
            Log.d("D_socket", "Error : " + e);
        }
    }// connectServer()

    public void MessageController() // 서버에 메시지 보내는 함수
    {
        Gson gson = new Gson(); // JSon 직렬화 해서 편하게 쓰는 Gson

        if(s.isClosed() ) // 소켓 연경 안되잇으면
        {
            return;
        }
        try {
            Log.d("CSV", "j_outmsg: "+j_outmsg);
             // JSON화한 메시지를 서버로 보냄 (내정보, 내위치, 경도)
            j_inmsg = inMsg.readLine(); // 내가 메시지 보낸 이후 서버에서 보낸 메시지 수신

            message_List= gson.fromJson(j_inmsg, new TypeToken<ArrayList<Message>>() {}.getType()); // 서버에서 받은 메시지(모든 클라이언트의 이름,위치 메시지 리스트)를 JSON->Gosn-> ArrayList<Userdata>로 해서 저장

            if(message_List.get(0).getChat_type().equals("location"))
            {
                outMsg.println(j_outmsg);
                SendBroadcast_map(j_inmsg,EXTRA_LOC_MESSAGE);
                location_List=message_List;
            }
            else if(message_List.get(0).getChat_type().equals("room_set")&& chat_room==-1
                    &&(message_List.get(0).getChat_id()[0].equals(Build.ID)||message_List.get(0).getChat_id()[1].equals(Build.ID)))
            {
                    chat_text="";
                    chat_room=message_List.get(0).getChat_room();
                    sendMessage(Jsonize(Build.ID,getChat_room(),"chat", "님이 입장 하였습니다."));
                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
            }
            else if(message_List.get(0).getChat_type().equals("chat")&&chat_room==message_List.get(0).getChat_room())
            {
                chat_text += message_List.get(0).getId()+(": ")+ message_List.get(0).getChat_text()+("\n");
                current_chat_text = "메시지 도착함, " + message_List.get(0).getId()+(": ")+ message_List.get(0).getChat_text()+("\n");
                SendBroadcast_map(current_chat_text,EXTRA_GET_MESSAGE);
                SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE);
            }
            else if(message_List.get(0).getChat_type().equals("logout")&&chat_room==message_List.get(0).getChat_room())
            {
                chat_text+="상대방이 채팅방을 떠났습니다.\n";
                SendBroadcast_map("상대방이 채팅방을 떠났습니다.\n",EXTRA_GET_MESSAGE);
                SendBroadcast_chat(chat_text,EXTRA_ALL_MESSAGE);
                chat_room =-1;
            }
            key_getMessage_ok=true;
            Log.d("CSV","j_inmsg: "+j_inmsg);
            Log.d("chat","now chatroom: " +chat_room);

        } catch (IOException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }


        // return inmsg;
    }// messageController

    private void SendBroadcast_chat(String message,String key) {
        Intent it = new Intent("EVENT_CHAT");


        if (!TextUtils.isEmpty(message))
            it.putExtra(key,message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_map(String message,String key) {
        Intent it = new Intent("EVENT_STRING_TO_MAP");


        if (!TextUtils.isEmpty(message))
            it.putExtra(key,message);

        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
    private void SendBroadcast_loc(Double Lat,Double Lng) {
        Intent it = new Intent("EVENT_LOC");


        if (Lat!=null) {
            it.putExtra("Lat", Lat);
            it.putExtra("Lng", Lng);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }


    public List<Message> getLocation_List()
    {
        return location_List;
    }
    public String getChat_text()
    {
        return chat_text;
    }
    public void set_appendChat_text(String chat_text){this.chat_text +=chat_text;}
    public boolean get_key_getMessage_ok()
    {
        return key_getMessage_ok;
    }
    public boolean get_key_getlocation_ok() {return  key_location_ok;}
    public boolean get_key_gps_ok()
    {
        return key_gps_ok;
    }
    public boolean get_key_chat_ok() { return key_chat_ok;}
    public void set_ket_chat_ok(boolean ket_chat_ok){this.key_chat_ok=key_chat_ok;}
    public String get_current_chat_text(){return current_chat_text;}
    public Socket getSocket(){return s;}
    public Location getMyLocation(){return lastKnownLocation;}
    public int getChat_room(){return chat_room;}
    public void sendMessage(String outmsg) {Log.d("chat",outmsg); outMsg.println(outmsg);}


    public String Jsonize(String id, String name, Double lat,  Double lng,String chat_type) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,name,lat,lng,chat_type)); //Data -> Gson -> json
        return json;

    }
    // chat
    public String Jsonize(String id, int chat_room,String chat_type,String chat_text) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,chat_room,chat_type,chat_text)); //Data -> Gson -> json
        return json;

    }


    public class MyLocationListener implements LocationListener
    {

        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(Location location) {
//            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            // Get the last location.
            lastKnownLocation = location; // 업데이트 된 주소 저장
            Log.d("lastKnownLocation : ",lastKnownLocation.toString());
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, // 네트워크+gps 이용 업데이트
                    1000, //1초마다
                    0, // 최소 거리 10미터
                    listener
            );
            if(s==null) // 서버와 연결 안됬으면 현재 위치 텍스트뷰에
            {
                Log.d("CSV", "n" +
                        "ot connect");
                Toast.makeText(getApplicationContext(), "서버 연결 안됨, 위치 받아옴 "+String.format(Locale.KOREA, "%.3f", lastKnownLocation.getLatitude()) + " , " + String.format(Locale.KOREA, "%.3f", lastKnownLocation.getLongitude()), Toast.LENGTH_SHORT).show();
            }
            else { // 서버 연결 됫으면 메세지 받은 걸 텍스트 뷰에 뿌림
//                Toast.makeText(getApplicationContext(),"메시지 받음",Toast.LENGTH_SHORT).show();
//                for(Userdata ud:message_List) {
//                    Log.d("CSV","Userlist: "+"name: " + ud.getName() + " lat: " + ud.getLat() + " lng: " + ud.getLng()+"\n");
//                }
            }
            if( lastKnownLocation.hasAltitude()) { // lastKnownLocation이 위치를
                SendBroadcast_loc(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                j_outmsg = Jsonize(Build.ID, Build.USER,lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),"location");
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
                key_gps_ok =true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),"GPS 꺼짐.",Toast.LENGTH_LONG).show();
            key_gps_ok =false;
        }
    }
}






