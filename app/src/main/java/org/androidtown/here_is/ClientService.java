package org.androidtown.here_is;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
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

    private List<Userdata> message_List;

    private IBinder mBinder = new Mybinder();
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

    class Mybinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, listener);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        message_List = new ArrayList<>();
        myThread= new Thread(this);
        myThread.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onDestroy() {

        Log.d("SCV","destroy");
        try {
            if(s!=null)
                s.close();
            locationManager.removeUpdates(listener);
            myThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    // 소켓통신 부분 시작





    protected void finalize() throws Throwable
    {
        if(s!=null)
            s.close(); // 끝날때 소켓 닫음
    }




    public void run() // 쓰레드 시작부분
    {

        Log.d("CSV", a_targetIp+ " " +String.format(Locale.KOREA,"%d",a_targetPort));


        while ( !myThread.isInterrupted()) //
        {
            Log.d("CSV", "j_outmsg: "+j_outmsg);
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
                connectServer(a_targetIp, a_targetPort);
            }
            if(key_location_ok&&s!=null&&!s.isClosed()) {
                    // 서버에 연결 하는 함수 받아온 ip.port 넘겨줌
                    Log.d("CSV", "msging");
                    sendMessage(); // 서버에 현재 위치정보 담아서 보냄

            }
            else
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }


    }
    public void connectServer(String targetIp,int targetPort) // 서버에 연결하는부분
    {
        try{
            // 소켓 생성
            if((s = new Socket(targetIp,targetPort))==null) // 소켓연결 성공 실패시
            {
                Log.d("D_socket", "[Client]Server 연결 fail!!");
                return;
            }
            else {
                Log.d("D_socket", "[Client]Server 연결 성공!!");

            }
            // 입출력 스트림 생성
            inMsg = new BufferedReader(new InputStreamReader(s.getInputStream())); // 수신 메시지 담을 버퍼
            outMsg = new PrintWriter(s.getOutputStream(),true); //송신 메시지 롸이터
            Log.d("D_socket", Build.USER);



        }catch(Exception e) {
            Log.d("D_socket", "Error : " + e);
        }
    }// connectServer()

    public void sendMessage() // 서버에 메시지 보내는 함수
    {

        Userdata m = new Userdata(); // 메시지 형식 프로토콜 클래스 (현재 이름, 위도, 경도)
        List<Userdata> L_m = new ArrayList<>(); // 서버에서 주는 지금 접속해있는 클라이언트 위치정보 받을 메시지 리스트

        Gson gson = new Gson(); // JSon 직렬화 해서 편하게 쓰는 Gson

        if(s.isClosed() ) // 소켓 연경 안되잇으면
        {
            return;
        }
        try {

            outMsg.println(j_outmsg); // JSON화한 메시지를 서버로 보냄 (내정보, 내위치, 경도)
            j_inmsg = inMsg.readLine(); // 내가 메시지 보낸 이후 서버에서 보낸 메시지 수신

            message_List= gson.fromJson(j_inmsg, new TypeToken<ArrayList<Userdata>>() {}.getType()); // 서버에서 받은 메시지(모든 클라이언트의 이름,위치 메시지 리스트)를 JSON->Gosn-> ArrayList<Userdata>로 해서 저장
            key_getMessage_ok=true;
            Log.d("CSV","j_inmsg: "+j_inmsg);
            Log.d("CSV","message_list: " +message_List.get(0).getLat());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // return inmsg;
    }

    public List<Userdata> getMessage_List()
    {
        return message_List;
    }
    public boolean get_key_getMessage_ok()
    {
        return key_getMessage_ok;
    }
    public boolean get_key_getlocation_ok() {return  key_location_ok;}
    public boolean get_key_gps_ok()
    {
        return key_gps_ok;
    }
    public Socket getSocket(){return s;}
    public Location getMyLocation(){return lastKnownLocation;}


    public String Jsonize(String id, String name, Double lat,  Double lng) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Userdata(id,name,lat,lng)); //Data -> Gson -> json
        return json;

    }

    public class MyLocationListener implements LocationListener
    {

        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(Location location) {
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            // Get the last location.
            lastKnownLocation = location; // 업데이트 된 주소 저장
            Log.d("lastKnownLocation : ",lastKnownLocation.toString());
            lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, // 네트워크+gps 이용 업데이트
                    1000, //1초마다
                    10, // 최소 거리 10미터
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

                j_outmsg = Jsonize(Build.ID, Build.USER,lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                key_location_ok=true;

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

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






