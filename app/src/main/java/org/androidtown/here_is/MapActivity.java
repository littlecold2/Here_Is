package org.androidtown.here_is;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.androidtown.here_is.ClientService.Mybinder;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        PlacesListener {
    private List<Marker> previous_marker = null;
    private GoogleMap map; // 구글맵 사용 할 때 필요
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
    private TextView tv; // 아래 텍스트 출력 부분 컨트롤
    private ArrayList<Marker> L_Marker_userlist;
    private FloatingActionButton fab,menu_fab;
    private Gson gson;

    //################ Profile View ################
    private LayoutInflater inflater;
    private View profileView;
    private ImageView profileImage;
    private TextView nicknameView;
    private TextView introView;
    private String targetID;
    private String targetIntro;
    private Button Btn_chatting;
    private Button Btn_Streaming;
//################ Profile View ################

//########service ########
    private ClientService CS;
    private boolean isService = false;
    private ServiceConnection conn = new ServiceConnection() {
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
//########service ########

//######### 주변 정보 ##########

    private LatLng lastknownlocation;

//######### 주변 정보 ##########

    private List<Message> User_loc_List;

    private SharedPreferences userinfo;
    private String myID;
    private String myName;
    private String myIntro;
    private int myImage_index;
    private String myUrl;



//브로드 캐스트
    private BroadcastReceiver mMessageReceiver = null;
    private BroadcastReceiver mMessageReceiver_loc = null;
    private BroadcastReceiver mMessageReceiver_chat_req = null;
    private static final String EXTRA_GET_MESSAGE ="current_chat_message";
    private static final String EXTRA_ALL_MESSAGE ="all_chat_message";
    private static final String EXTRA_LOC_MESSAGE ="all_loc_message";
    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_STRING_TO_MAP"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver_loc, new IntentFilter("EVENT_LOC"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver_chat_req, new IntentFilter("EVENT_CHAT_REQ_MAP"));
    }
    // 브로드 캐스트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  // 구글맵 프레그먼트 적용
 //########service ########
       CS = new ClientService();
        Intent intent = new Intent(getApplicationContext(), ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Toast.makeText(getApplicationContext(), "Service 시작 ", Toast.LENGTH_SHORT).show();
//########service ########
        tv = (TextView) findViewById(R.id.DDtext);
        gson = new Gson();
        L_Marker_userlist =new ArrayList<>();

        User_loc_List = new ArrayList<>();
        previous_marker = new ArrayList<>();

        userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        myID = userinfo.getString("ID","");
        myName= userinfo.getString("NAME","");
        myIntro= userinfo.getString("INFO","");
        myImage_index= Integer.parseInt(userinfo.getString("INDEX",""));
        myUrl= userinfo.getString("URL","");




        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 편지 아이콘
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

            }
        });
        menu_fab = (FloatingActionButton) findViewById(R.id.menu_fab);
        menu_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(navigationView);

            }
        });



        //#############브로드캐스트############

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getExtras().getString(EXTRA_GET_MESSAGE);
                String j_loc_msg = intent.getExtras().getString(EXTRA_LOC_MESSAGE);

                if(!TextUtils.isEmpty(message)) {
                    Log.d("chat", "msg: " + message);
                    Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    fab.setVisibility(View.VISIBLE); // 메시지 오면 편지아이콘 나오게
                }

                else if(!TextUtils.isEmpty(j_loc_msg)) {

                    User_loc_List = gson.fromJson(j_loc_msg, new TypeToken<ArrayList<Message>>() {
                    }.getType());

                    //map.clear();
                    tv.setText("");
                    for (Marker mk : L_Marker_userlist) {
                        mk.remove();
                    }
                    L_Marker_userlist.clear();

                    for (Message mg : User_loc_List) {
                        if (mg.getChat_room() == -1) {
                            pickMark(new LatLng(mg.getLat(), mg.getLng()), mg.getName(), mg.getIntro(),mg.getImage(), mg);
                            tv.append("name: " + mg.getName() + "위치: " + mg.getLat() + ", " + mg.getLng());
                        }

                    }
                }

            }
        };
        mMessageReceiver_loc = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Double Lat = intent.getExtras().getDouble("Lat");
                Double Lng = intent.getExtras().getDouble("Lng");

                lastknownlocation = new LatLng(Lat,Lng);

                if(!CS.get_key_server_ok())
                {
                    L_Marker_userlist.clear();
                    map.clear();
                   // showPlaceInformation(lastknownlocation);
                    pickMark(lastknownlocation,myName,"서버 연결 안됨");
                }



            }
        };

        mMessageReceiver_chat_req = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String  id = intent.getExtras().getString("ID");
                String  name = intent.getExtras().getString("NAME");
                int image = intent.getExtras().getInt("IMAGE");

                showdialog(id,name,image);


            }
        };
//############### 브로드캐스트 ####################


    }// oncreate

    private void showdialog(final String id, String name, int image) {
// 대화상자를만들기위한빌더객체생성

        String resName = "@drawable/profile" + image;
        int resID = getResources().getIdentifier(resName, "drawable", this.getPackageName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("채팅 요청");
        builder.setMessage(name + "님이 채팅을 요청 하셨습니다.\n 채팅을 수락 하시겠습니까?");



        builder.setIcon(resID);
        builder.setCancelable(false);

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() { // 예버튼
            public void onClick(DialogInterface dialog, int whichButton) {

                if(CS.getChat_room()==-1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CS.sendMessage(Jsonize(myID, id, "room_set"));
                        }
                    }).start();
                }
                //dialog.cancel();
                dialog.dismiss();
                return;
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 아니오버튼
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), "채팅을 취소 하였습니다.", Toast.LENGTH_SHORT).show();
                //dialog.cancel();
                dialog.dismiss();
                return;
            }
        });


        AlertDialog dialog = builder.create();// 대화상자객체생성후보여주기


        if(!isFinishing())
            dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        fab.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onDestroy() {
        Log.d("mapactivity","destroy");
        if(isService &&CS.get_key_server_ok()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CS.sendMessage(Jsonize(CS.getChat_room(), "chat_logout"));
//                            my_thread.interrupt();
//                            unbindService(conn);
                    //CS.setChat_text_clear();
                }
            }).start();
        }
        //userLocating.interrupt();
        if(isService)
            unbindService(conn);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        map = gMap;
//        map.setOnMyLocationButtonClickListener(this);// gps 버튼 활성화
        enableMyLocation(); // 내 위치 활성화



        pickMark( new LatLng( 37.628, 126.825),"안녕하세요.","GPS를 켜주세요~");
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng( 37.628, 126.825)).zoom(20).tilt(30).build()));
        map.moveCamera(CameraUpdateFactory.newLatLng( new LatLng( 37.628, 126.825)));
        map.animateCamera(CameraUpdateFactory.zoomTo(20));

       // map.setPadding(300,300,300,300); // left, top, right, bottom //버튼이나 그런거 위치 한정?
        map.getUiSettings().setZoomControlsEnabled(true); // 줌 버튼 가능하게


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override //마커 클릭시
            public boolean onMarkerClick(Marker marker) {
                //################ Profile View ################
                if(((Message)marker.getTag())==null)
                    Toast.makeText(getApplicationContext(), "자신의 마커 입니다.", Toast.LENGTH_SHORT).show();
                else if(((Message)marker.getTag()).getId().equals(myID))
                {
                    Toast.makeText(getApplicationContext(), "자신의 마커 입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    inflater = getLayoutInflater();
                    profileView = inflater.inflate(R.layout.profile, null);
                    profileImage = (ImageView) profileView.findViewById(R.id.profileImage_dialog);
                    nicknameView = (TextView) profileView.findViewById(R.id.nicknameView);
                    introView = (TextView) profileView.findViewById((R.id.introView));
                    targetID = ((Message) marker.getTag()).getId();
                    Btn_chatting = (Button) profileView.findViewById(R.id.chatBtn);
                    Btn_Streaming = (Button) profileView.findViewById(R.id.StreamingBtn);
                    // 채팅버튼 누를 때

                    String resName = "@drawable/profile" + ((Message) marker.getTag()).getImage();
                    int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());

                    AlertDialog.Builder buider = new AlertDialog.Builder(MapActivity.this); //AlertDialog.Builder 객체 생성

                    buider.setTitle("Member Information"); //Dialog 제목
                    buider.setIcon(android.R.drawable.ic_menu_add); //제목옆의 아이콘 이미지(원하는 이미지 설정)

                    profileImage.setImageResource(resID);
                    nicknameView.setText(targetID);
                    introView.setText(((Message) marker.getTag()).getName());
                    buider.setView(profileView);

                    final AlertDialog dialog = buider.create();
                    dialog.show();
                    Btn_chatting.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
//                    // +상대방 이미지 그런거?
//                    startActivity(intent);
                            if(CS.getChat_room()==-1) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CS.sendMessage(Jsonize(myID, targetID,myName,myImage_index, "room_req"));
                                    }
                                }).start();
                                Toast.makeText(getApplicationContext(), "채팅 요청을 보냈습니다.", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                            else
                            {
                                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        }
                    });
                    //################ Profile View ################

//
                }
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
           showdialog(myID,"min",0);
        }
        });
        Btn_MyLoction.setOnClickListener(new Button.OnClickListener()
        { @Override
        public void onClick(View view)
        { //위치검색 (PlacePicker)
           // MarkerPoints.clear(); // 마커 저장 해논 리스트 클리어
            //테스트용
//            Intent sintent = new Intent(getApplicationContext(), ChattingActivity.class);
//            sintent.putExtra("targetID",Build.ID);
//            startActivity(sintent);

            if(CS.get_key_location_ok()) {
                map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude())));
                showPlaceInformation(lastknownlocation);
            }
            else
                Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();
        }
        });


    } // onMapReady



    private void pickMark(final LatLng LL,String name, String intro,int image_index,Message data) // 위도 경도, 이름 주소 받아서 마커 찍는 함수
    {
        MarkerOptions markerOptions = new MarkerOptions(); // 옵션 설정 해놓을 변수
        markerOptions.position(LL); // 위치 적용
        markerOptions.title(name); // 이름
//        markerOptions.snippet(address.substring(0,20)); // 주소 넣음
        markerOptions.snippet(intro); // 인사 넣음

        markerOptions.draggable(true); // 드래그 가능하도록
        markerOptions.flat(true);

        String resName = "@drawable/profile" + image_index;
        int resID = getResources().getIdentifier(resName, "drawable", this.getPackageName());

        markerOptions.icon(BitmapDescriptorFactory.fromResource(resID));


        Marker mk;
        mk = map.addMarker(markerOptions);
        mk.setTag(data);
        //mk.showInfoWindow();
//        map.addMarker(markerOptions).showInfoWindow(); // 맵에 추가
        L_Marker_userlist.add(mk); // 위치정보 마커 리스트에 추가
    } // pickMark

    private void pickMark(final LatLng LL,String name, String address) // 위도 경도, 이름 주소 받아서 마커 찍는 함수
    {
        MarkerOptions markerOptions = new MarkerOptions(); // 옵션 설정 해놓을 변수
        markerOptions.position(LL); // 위치 적용
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


        Marker mk;
        mk = map.addMarker(markerOptions);
        mk.showInfoWindow();
//        map.addMarker(markerOptions).showInfoWindow(); // 맵에 추가
        L_Marker_userlist.add(mk); // 위치정보 마커 리스트에 추가
    } // pickMark





    //setroom
    public String Jsonize(String chat_id1, String chat_id2,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,chat_type)); //Data -> Gson -> json
        return json;

    }
    public String Jsonize(String chat_id1, String chat_id2,String name,int image,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,name,image,chat_type)); //Data -> Gson -> json
        return json;

    }

    // chat_logout
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

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("안내");

            builder.setMessage("종료하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() { // 예버튼
                public void onClick(DialogInterface dialog, int whichButton) {
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 아니오버튼
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();// 대화상자객체생성후보여주기
            dialog.show();
            TextView msgView = (TextView) dialog.findViewById(android.R.id.message);

            msgView.setTextSize(13);




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
            Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            Toast.makeText(getApplicationContext(),"채팅",Toast.LENGTH_LONG).show();
        } else if (id == R.id.etc) {
            Toast.makeText(getApplicationContext(),"기타",Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onPlacesFailure(PlacesException e) {

    }


    public void onPlacesStart() {

    }


    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(place.getVicinity());
                    Marker item = map.addMarker(markerOptions);
                    item.setTag("place");
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
            }
        });
    }


    public void onPlacesFinished() {

    }

    public void showPlaceInformation(LatLng location)
    {
       // map.clear();//지도 지우기

        if (previous_marker != null)
            previous_marker.clear();//마커지우기

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("AIzaSyDdwDyx5xMSgY_b7IPNnCrB9qWLMQ-EDgM")
                .latlng(location.latitude, location.longitude)// location 파라미터의 위치로부터
                .radius(500)  // 1000M 이내에 있는
                .type(PlaceType.RESTAURANT)  // 음식점 추적
                .build()
                .execute();
    }



}


//    public class UserLocating extends Thread
//    {
//        public void run() {
//            // 현재 UI 스레드가 아니기 때문에 메시지 큐에 Runnable을 등록 함
//            while (!userLocating.isInterrupted()) {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        List<Message> message_List;
////                        if(CS.get_key_pop_ok())
////                        {
////                            Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
////                            startActivity(intent);
////                            CS.set_key_pop(false);
////                        }
//                        if(!CS.get_key_gps_ok())
//                        {
//                            Toast.makeText(getApplicationContext(),"GPS를 켜주세요.",Toast.LENGTH_SHORT).show();
//                            tv.setText("");
//                        }
//                        else if (!CS.get_key_getlocation_ok())
//                        {
//                            Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();
//                        }
//                        else if (isService&& CS.get_key_getMessage_ok()) {
//                            message_List = CS.getLocation_List();
//                            L_Marker_userlist.clear();
//                            map.clear();
//                            tv.setText("");
//                            for (Message mg : message_List) {
//                                if(mg.getChat_room() == -1)
//                                {
//                                    pickMark(new LatLng(mg.getLat(), mg.getLng()), mg.getName(), "인삿말", mg);
//                                    tv.append("name: "+mg.getName()+ "위치: "+mg.getLat()+", "+mg.getLng());
//                                }
//
//                            }
//
//                        }
//                        else if(isService && CS.get_key_getlocation_ok() && !CS.get_key_getMessage_ok())
//                        {
//                            Toast.makeText(getApplicationContext(),"서버에서 값 못받음",Toast.LENGTH_SHORT).show();
//                            tv.setText("");
//                         //   L_Marker_userlist.clear();
//                         //   map.clear();
//                           // pickMark(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude()), Build.USER, "인삿말");
//                            tv.append("서버에서 값 못받음\n name: " + "나" + " lat: " + CS.getMyLocation().getLatitude() + " lng: " + CS.getMyLocation().getLongitude() +"\n");
//                        }
//                    }
//
//                });
//            }///
//        }
//    }



