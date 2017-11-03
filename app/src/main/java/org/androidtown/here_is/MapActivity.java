package org.androidtown.here_is;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbRequest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import org.androidtown.here_is.ClientService.Mybinder;

public class MapActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap map; // 구글맵 사용 할 때 필요
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
    private TextView tv; // 아래 텍스트 출력 부분 컨트롤
    private ArrayList<MarkerOptions> L_Marker_userlist;
    private ClientService CS;
    private boolean isService = false;
    private UserLocating userLocating;



    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Mybinder mb =(Mybinder) service;
            CS = mb.getService();
            isService =true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  // 구글맵 프레그먼트 적용

        CS = new ClientService();
        Intent intent = new Intent(MapActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Toast.makeText(getApplicationContext(), "Service 시작 ", Toast.LENGTH_SHORT).show();

       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        tv = (TextView) findViewById(R.id.DDtext);
        L_Marker_userlist =new ArrayList<>();

        userLocating = new UserLocating();
        userLocating.start();
//        chatController =new ChatController();
//        chatController.start();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onDestroy() {
        Log.d("mapactivity","destroy");
        userLocating.interrupt();

        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        map = gMap;
        enableMyLocation(); // 내 위치 활성화

        map.moveCamera(CameraUpdateFactory.newLatLng( new LatLng( 37.628, 126.825)));
        map.animateCamera(CameraUpdateFactory.zoomTo(16));

       // map.setPadding(300,300,300,300); // left, top, right, bottom //버튼이나 그런거 위치 한정?
        map.getUiSettings().setZoomControlsEnabled(true); // 줌 버튼 가능하게


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override //마커 클릭시
            public boolean onMarkerClick(Marker marker) {
//                Userdata ud =marker.getTag();
         //       Toast.makeText(getApplicationContext(),((Message)marker.getTag()).getId(),Toast.LENGTH_SHORT).show(); // 마커 클릭 시 걔 이름 출력


                CS.setJ_outmsg(Jsonize(Build.ID,Build.USER,"채팅"));
//
                // 토스트나 알럿 메세지...
                return false;
            }
        });




        Button Btn_startSendloc = (Button) findViewById(R.id.StartSendLocBtn); // 대중교통 길찾기 버튼
        Button Btn_stopSendloc = (Button) findViewById(R.id.StopSendLocBtn); // clear 버튼
        Button Btn_MyLoction = (Button) findViewById(R.id.MyLocationBtn); // 위치검색 버튼

        Btn_startSendloc.setOnClickListener(new Button.OnClickListener()
        { @Override
        public void onClick(View view)
        {

            if(!isService ) {
                    Intent intent = new Intent(MapActivity.this, ClientService.class);
                    bindService(intent, conn, Context.BIND_AUTO_CREATE);
                    Toast.makeText(getApplicationContext(), "Service 시작 ", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Service 켜있음", Toast.LENGTH_SHORT).show();
            }
        }
        });

        Btn_stopSendloc.setOnClickListener(new Button.OnClickListener()
        { @Override
        public void onClick(View view)
        {
            if(isService) {
                unbindService(conn);
                Toast.makeText(getApplicationContext(), "위치 추적 끔 ,Service unBind", Toast.LENGTH_SHORT).show();
                isService=false;
            }
            else
            {
                Toast.makeText(getApplicationContext(), "위치 추적 켜 있지 않음", Toast.LENGTH_SHORT).show();
            }
        }
        });
        Btn_MyLoction.setOnClickListener(new Button.OnClickListener()
        { @Override
        public void onClick(View view)
        { //위치검색 (PlacePicker)
           // MarkerPoints.clear(); // 마커 저장 해논 리스트 클리어
            if(CS.get_key_getlocation_ok())
                map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(CS.getMyLocation().getLatitude(),CS.getMyLocation().getLongitude())));
            else
                Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();
        }
        });


    } // onMapReady



    private void pickMark(final LatLng LL,String name, String address,Message data) // 위도 경도, 이름 주소 받아서 마커 찍는 함수
    {
        MarkerOptions markerOptions = new MarkerOptions(); // 옵션 설정 해놓을 변수
        markerOptions.position(LL); // 위치 적용
//        markerOptions.title(String.format(Locale.KOREA,"%.3f",LL.latitude)+","+String.format(Locale.KOREA,"%.3f",LL.longitude));
        markerOptions.title(name); // 이름
//        markerOptions.snippet(address.substring(0,20)); // 주소 넣음
        markerOptions.snippet(address); // 주소 넣음

        markerOptions.draggable(true); // 드래그 가능하도록
        markerOptions.flat(true);
        if(L_Marker_userlist.size()==0)
        {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image1));
        }
        else if (L_Marker_userlist.size() == 1) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image2));
        }
        else if(L_Marker_userlist.size()>1)
        {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image3));
        }
       // map.addMarker(markerOptions).setFlat(true);
        map.addMarker(markerOptions).setTag(data);
//        map.addMarker(markerOptions).showInfoWindow(); // 맵에 추가
        L_Marker_userlist.add(markerOptions); // 위치정보 마커 리스트에 추가
    } // pickMark

    private void pickMark(final LatLng LL,String name, String address) // 위도 경도, 이름 주소 받아서 마커 찍는 함수
    {
        MarkerOptions markerOptions = new MarkerOptions(); // 옵션 설정 해놓을 변수
        markerOptions.position(LL); // 위치 적용
//        markerOptions.title(String.format(Locale.KOREA,"%.3f",LL.latitude)+","+String.format(Locale.KOREA,"%.3f",LL.longitude));
        markerOptions.title(name); // 이름
//        markerOptions.snippet(address.substring(0,20)); // 주소 넣음
        markerOptions.snippet(address); // 주소 넣음

        markerOptions.draggable(true); // 드래그 가능하도록

        if(L_Marker_userlist.size()==0)
        {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image1));
        }
        else if (L_Marker_userlist.size() == 1) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image2));
        }
        else if(L_Marker_userlist.size()>1)
        {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.image3));
        }
        map.addMarker(markerOptions).setFlat(true);
        map.addMarker(markerOptions).setDraggable(true);
        map.addMarker(markerOptions).showInfoWindow(); // 맵에 추가
        L_Marker_userlist.add(markerOptions); // 위치정보 마커 리스트에 추가
    } // pickMark

    public class UserLocating extends Thread
    {
        public void run() {
            // 현재 UI 스레드가 아니기 때문에 메시지 큐에 Runnable을 등록 함
            while (!userLocating.isInterrupted()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        List<Message> message_List;
                        if(!CS.get_key_gps_ok())
                        {
                            Toast.makeText(getApplicationContext(),"GPS를 켜주세요.",Toast.LENGTH_SHORT).show();
                            tv.setText("");
                        }
                        else if (isService&& CS.get_key_getMessage_ok()) {
                            message_List = CS.getMessage_List();
                            L_Marker_userlist.clear();
                            map.clear();
                            for (Message mg : message_List) {
                                pickMark(new LatLng(mg.getLat(), mg.getLng()), mg.getName(), "인삿말", mg);
                                if (mg.getChat_type().equals("room_set") && ( mg.getChat_id()[0].equals(Build.ID) || mg.getChat_id()[1].equals(Build.ID) )) {
                                    tv.append("room : " + mg.getChat_room() + "\n");
                                }
                            }

//                            for (Message mg : message_List) {
//                                tv.setText("");
                               // tv.append("ID: "+ mg.getId() + " name: " + mg.getName() + "\nlat: " + mg.getLat() + " lng: " + mg.getLng()+"\n");
//                            }
                        }
                        else if(isService && CS.get_key_getlocation_ok() && !CS.get_key_getMessage_ok())
                        {
                            Toast.makeText(getApplicationContext(),"서버 안켜짐",Toast.LENGTH_SHORT).show();
                            tv.setText("");
                            L_Marker_userlist.clear();
                            map.clear();
                          //  pickMark(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude()), Build.USER, "인삿말");
//                            tv.append("서버 켜지지 않음\n name: " + "나" + " lat: " + CS.getMyLocation().getLatitude() + " lng: " + CS.getMyLocation().getLongitude() +"\n");
                        }
                    }

                });
            }///
        }
    }



    // chat
    public String Jsonize(String id, int chat_room,String chat_text, String chat_type) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,chat_room,chat_text,chat_type)); //Data -> Gson -> json
        return json;

    }

    //setroom
    public String Jsonize(String chat_id1, String chat_id2,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,chat_type)); //Data -> Gson -> json
        return json;

    }

    // logout
    public String Jsonize(int chat_room ,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_room,chat_type)); //Data -> Gson -> json
        return json;

    }








    // 위치 퍼미션
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            org.androidtown.here_is.PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
           // map.setMyLocationEnabled(true);

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (org.androidtown.here_is.PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();


        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        org.androidtown.here_is.PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

// 위치퍼미션 끝



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.info) {
            Toast.makeText(getApplicationContext(),"정보",Toast.LENGTH_LONG).show();
            LayoutInflater inflater=getLayoutInflater();
            final View profileView= inflater.inflate(R.layout.profile, null);
            AlertDialog.Builder buider= new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성

            buider.setTitle("Member Information"); //Dialog 제목

            buider.setIcon(android.R.drawable.ic_menu_add); //제목옆의 아이콘 이미지(원하는 이미지 설정)

            buider.setView(profileView);
            AlertDialog dialog=buider.create();
            dialog.show();

          //  LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //inflater.inflate(R.layout.profile, container, true);
        } else if (id == R.id.stream) {
            Toast.makeText(getApplicationContext(),"스트리밍",Toast.LENGTH_LONG).show();
        } else if (id == R.id.chat) {
            Toast.makeText(getApplicationContext(),"채팅",Toast.LENGTH_LONG).show();
        } else if (id == R.id.etc) {
            Toast.makeText(getApplicationContext(),"기타",Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


