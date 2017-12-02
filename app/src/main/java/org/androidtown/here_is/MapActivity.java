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
import android.graphics.Color;
import android.hardware.usb.UsbRequest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
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
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.androidtown.here_is.ClientService.Mybinder;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends Font
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        PlacesListener {
    private List<Marker> previous_marker = null;
    private GoogleMap map; // 구글맵 사용 할 때 필요
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
//    private TextView tv; // 아래 텍스트 출력 부분 컨트롤
    private ArrayList<Marker> L_Marker_userlist;
    private FloatingActionButton fab,menu_fab;
    private Gson gson;
    private boolean dialog_key =false;

    //################ Profile View ################
    private LayoutInflater inflater;
    private View profileView;
    private ImageView profileImage;
    private TextView nicknameView;
    private TextView introView;
    private String targetID;
    private String targetIntro;
    private ImageButton Btn_chatting;
    private ImageButton Btn_Streaming;
//################ Profile View ################

    private ImageView naviProfile;
    private TextView naviName;
    private TextView naviIntro;
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
    private int placechecker = -1;
    private LatLng lastknownlocation;
    private static final int PLACE_PICKER_REQUEST =1; // 위치검색 쓸 때
    private List<Polyline> L_Poly;

    private String Dis;
    private String Dur;
    private String Bus;

    private Marker bus_marker;

//######### 주변 정보 ##########

    private List<Message> User_loc_List;

    private SharedPreferences userinfo;
    private String myID;
    private String myName;
    private String myIntro;
    private int myImage_index;
    private String myUrl;
    private DrawerLayout drawer;
    private NavigationView navigationView;


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
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
//        tv = (TextView) findViewById(R.id.DDtext);
        gson = new Gson();
        L_Marker_userlist =new ArrayList<>();

        User_loc_List = new ArrayList<>();
        previous_marker = new ArrayList<>();
        L_Poly = new ArrayList<>();

        userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        myID = userinfo.getString("ID","");
        myName= userinfo.getString("NAME","");
        myIntro= userinfo.getString("INFO","");
        myImage_index= Integer.parseInt(userinfo.getString("INDEX",""));
        myUrl= userinfo.getString("URL","");




        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);


        View navi_header  = navigationView.getHeaderView(0);

       // View navi  = navigationView.inflateHeaderView(R.layout.nav_header_map);

        naviProfile = (ImageView) navi_header.findViewById(R.id.naviImage) ;
        naviName = (TextView) navi_header.findViewById(R.id.naviName);
        naviIntro= (TextView) navi_header.findViewById(R.id.naviIntro);

        String resName = "@drawable/profile" + myImage_index;
        Log.d("img",resName);
        int resID_ = getResources().getIdentifier(resName, "drawable", this.getPackageName());
        naviProfile.setImageResource(resID_);
        naviName.setText(myName);
        naviIntro.setText(myIntro);

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
//        menu_fab = (FloatingActionButton) findViewById(R.id.menu_fab);
//        menu_fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                drawer.openDrawer(navigationView);
//
//            }
//        });

        intent = new Intent(getApplicationContext(), WebviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        //#############브로드캐스트############

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getExtras().getString(EXTRA_GET_MESSAGE);
                String j_loc_msg = intent.getExtras().getString(EXTRA_LOC_MESSAGE);

                if(!TextUtils.isEmpty(message)) {
                    Log.d("chat", "msg: " + message);
                    TSnackbar tsnackbar= TSnackbar.make(getWindow().getDecorView().getRootView(), message, TSnackbar.LENGTH_SHORT);
                    tsnackbar.setActionTextColor(Color.WHITE);
                    //tsnackbar.setMaxWidth(10);
                    View snackbarView = tsnackbar.getView();
                    //snackbarView.setBackgroundColor(Color.parseColor("#CC00CC"));
                    TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(15);
                    tsnackbar.show();
                    fab.setVisibility(View.VISIBLE); // 메시지 오면 편지아이콘 나오게
                }

                else if(!TextUtils.isEmpty(j_loc_msg)) {

                    User_loc_List = gson.fromJson(j_loc_msg, new TypeToken<ArrayList<Message>>() {
                    }.getType());

                    //map.clear();
//                    tv.setText("");
                    for (Marker mk : L_Marker_userlist) {
                        mk.remove();
                    }
                    L_Marker_userlist.clear();

                    for (Message mg : User_loc_List) {
                        if (mg.getChat_room() == -1) {
                            pickMark(new LatLng(mg.getLat(), mg.getLng()), mg.getName(), mg.getIntro(),mg.getImage(), mg);
//                            tv.append("name: " + mg.getName() + "위치: " + mg.getLat() + ", " + mg.getLng()+"\n");
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
                    if(CS.get_key_location_ok())
                        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude())));
                }



            }
        };

        mMessageReceiver_chat_req = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(dialog_key==false) {
                    Log.d("dialog", "broad_dialog_up");
                    String id = intent.getExtras().getString("ID");
                    String name = intent.getExtras().getString("NAME");
                    int image = intent.getExtras().getInt("IMAGE");
                    showdialog(id, name, image);

                }


            }
        };
//############### 브로드캐스트 ####################
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {

            @Override
            public void onTabSelected(@IdRes int tabId) {

                if(tabId == R.id.tab1){
                    drawer.openDrawer(navigationView);
                }

                else if(tabId == R.id.tab2){

                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                else if(tabId == R.id.tab3){

                    if(CS.get_key_location_ok()) {
                        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude())));
                        //showPlaceInformation(lastknownlocation);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();


                }
                else if(tabId == R.id.tab4){
                    if (CS.get_key_location_ok()) {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        intentBuilder.setLatLngBounds(new LatLngBounds(new LatLng(lastknownlocation.latitude - 0.01, lastknownlocation.longitude - 0.01), new LatLng(lastknownlocation.latitude + 0.01, lastknownlocation.longitude + 0.01)));
                        Intent intent = null;
                        try {
                            intent = intentBuilder.build(MapActivity.this);
                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if(tabId == R.id.tab1){
                    drawer.openDrawer(navigationView);
                }

                else if(tabId == R.id.tab2){

                    Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                else if(tabId == R.id.tab3){

                    if(CS.get_key_location_ok()) {
                        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(CS.getMyLocation().getLatitude(), CS.getMyLocation().getLongitude())));
                        //showPlaceInformation(lastknownlocation);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();


                }
                else if(tabId == R.id.tab4){
                    if (CS.get_key_location_ok()) {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        intentBuilder.setLatLngBounds(new LatLngBounds(new LatLng(lastknownlocation.latitude - 0.01, lastknownlocation.longitude - 0.01), new LatLng(lastknownlocation.latitude + 0.01, lastknownlocation.longitude + 0.01)));
                        Intent intent = null;
                        try {
                            intent = intentBuilder.build(MapActivity.this);
                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "위치 확인중...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        drawer.closeDrawer(navigationView);





    }// oncreate

    private void showdialog(final String id, final String name, final int image) {
// 대화상자를만들기위한빌더객체생성
        Log.d("dialog","crate");
        dialog_key=true;
        String resName = "@drawable/profile" + image;
        int resID = getResources().getIdentifier(resName, "drawable", this.getPackageName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("채팅 요청");
        builder.setMessage(name + "님이 채팅을 요청 하셨습니다.\n\n 채팅을 수락 하시겠습니까?");




        builder.setIcon(resID);
        builder.setCancelable(false);

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() { // 예버튼
            public void onClick(DialogInterface dialog, int whichButton) {


                if (CS.getChat_room() == -1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CS.sendMessage(Jsonize(myID, id, myName, name, "room_set"));
                            CS.setChat_name(name);
                            CS.setChat_image_index(image);
                        }
                    }).start();
                }
                dialog_key=false;

                dialog.dismiss();


            }
        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 아니오버튼
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), "채팅을 취소 하였습니다.", Toast.LENGTH_SHORT).show();
                //dialog.cancel();
                dialog_key=false;
                dialog.dismiss();
                return;
            }
        });


        AlertDialog dialog = builder.create();// 대화상자객체생성후보여주기

        dialog.show();
        TextView msgView = (TextView) dialog.findViewById(android.R.id.message);
        msgView.setTextSize(14);


       // if(!isFinishing())

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



        map.setPadding(0,00,0,150); // left, top, right, bottom //버튼이나 그런거 위치 한정?
        map.getUiSettings().setZoomControlsEnabled(true); // 줌 버튼 가능하게


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override //마커 클릭시
            public boolean onMarkerClick(final Marker marker) {
                //################ Profile View ################
                if((marker.getTag())==null)
                    Toast.makeText(getApplicationContext(), "자신의 마커 입니다.", Toast.LENGTH_SHORT).show();
                else if(marker.getTag().getClass().equals(Message.class) &&((Message)marker.getTag()).getId().equals(myID))
                {
                    Toast.makeText(getApplicationContext(), "자신의 마커 입니다.", Toast.LENGTH_SHORT).show();
                }
                else if(marker.getTag().getClass().equals(Message.class)){
                    inflater = getLayoutInflater();
                    profileView = inflater.inflate(R.layout.profile, null);
                    profileImage = (ImageView) profileView.findViewById(R.id.profileImage_dialog);
                    nicknameView = (TextView) profileView.findViewById(R.id.nicknameView);
                    introView = (TextView) profileView.findViewById((R.id.introView));
                    targetID = ((Message) marker.getTag()).getId();
                    Btn_chatting = (ImageButton) profileView.findViewById(R.id.chatBtn);
                    Btn_Streaming = (ImageButton) profileView.findViewById(R.id.chatStreaming);
                    // 채팅버튼 누를 때

                    final int image= ((Message) marker.getTag()).getImage();
                    final String name = ((Message) marker.getTag()).getName();
                    String intro = ((Message) marker.getTag()).getIntro();
                    final Message msg = ((Message) marker.getTag());
                    String resName = "@drawable/profile" + image;
                    int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());

                    AlertDialog.Builder buider = new AlertDialog.Builder(MapActivity.this); //AlertDialog.Builder 객체 생성

                    //buider.setTitle("사용자 정보"); //Dialog 제목
                  //  buider.setIcon(android.R.drawable.ic_menu_add); //제목옆의 아이콘 이미지(원하는 이미지 설정)

                    profileImage.setImageResource(resID);
                    nicknameView.setText(name);
                    introView.setText(intro);
                    buider.setView(profileView);

                    final AlertDialog dialog = buider.create();
                    dialog.show();
                    Btn_chatting.setOnClickListener(new ImageButton.OnClickListener() {
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
                                CS.setChat_image_index(image);
                                CS.setChat_name( name);
                                Toast.makeText(getApplicationContext(), "채팅 요청을 보냈습니다.", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                            else
                            {
                                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        }
                    });
                    //################ Profile View ################
                    Btn_Streaming.setOnClickListener(new ImageButton.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if( msg.getUrl().isEmpty()|| msg.getUrl().equals("NO URL")) {
                                Toast.makeText(getApplicationContext(), "스트리밍 주소가 없습니다.", Toast.LENGTH_LONG).show();
                            }
                            else
                            {

                                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(((Message) marker.getTag()).getUrl()))
                                        .setPackage("com.google.android.youtube"));
                            }
                    }
                    });
//

//
                }

                else if(marker.getTag().getClass().equals(PlaceData.class)) {
                    inflater = getLayoutInflater();
                    View loc_profileView = inflater.inflate(R.layout.location_profile, null);
                    ImageView loc_profileImage = (ImageView) loc_profileView.findViewById(R.id.loc_image);
                    TextView nameView = (TextView) loc_profileView.findViewById(R.id.loc_name);
                    TextView addressView = (TextView) loc_profileView.findViewById((R.id.loc_address));
                    ImageButton Btn_find_loc = (ImageButton) loc_profileView.findViewById(R.id.find_loc);

                    final LatLng loc = ((PlaceData)marker.getTag()).getLocation();
                    String name = ((PlaceData) marker.getTag()).getName();
                    String add = ((PlaceData) marker.getTag()).getAddress();
                    String type = ((PlaceData) marker.getTag()).getType();
                    bus_marker = marker;

                    String resName = "@drawable/"+type+"_128";
                    int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());

                    AlertDialog.Builder buider = new AlertDialog.Builder(MapActivity.this); //AlertDialog.Builder 객체 생성

                    //buider.setTitle("위치 정보"); //Dialog 제목
                  // buider.setIcon(android.R.drawable.ic_menu); //제목옆의 아이콘 이미지(원하는 이미지 설정)

                    loc_profileImage.setImageResource(resID);
                    nameView.setText(name);
                    addressView.setText(add);
                    buider.setView(loc_profileView);

                    final AlertDialog dialog = buider.create();
                    dialog.show();
                    Btn_find_loc.setOnClickListener(new ImageButton.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            for (Polyline pl : L_Poly) {
                                pl.remove();
                            }
                            L_Poly.clear();
                                String url = getUrl(lastknownlocation,loc ); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
                                fetchUrl fUrl = new fetchUrl(); // fetch할 클래스 생성
                                fUrl.execute(url); // url fetch
                                dialog.cancel();
                            // 길찾기ㄱ
                        }
                    });
                    //################ Profile View ################

                }

                // 토스트나 알럿 메세지...
                return false;
            }
        }); // 마커 클릭시




        //map.moveCamera(CameraUpdateFactory.newLatLng( new LatLng( 37.628, 126.825)));
         map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng( 37.628, 126.825)).zoom(16).build()));
//         map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng( 37.628, 126.825)).zoom(16).build()));
//         map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng( 37.628, 126.825)).zoom(16).tilt(30).build()));

        //map.animateCamera(CameraUpdateFactory.zoomTo(16));





    } // onMapReady
    /////////////////////// Google place PlacePicker
    protected void onActivityResult(int requestCode, int resultCode, Intent data) // PlacePicker 끝날 때 정보 받아오기
    {
        if(requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK)
        {
            final Place place = PlacePicker.getPlace(this, data); // 정보 받아오기
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();


            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }

            //pickMark(place.getLatLng(),name.toString(),address.toString()); // 받아온 정보에서 위치, 이름 , 주소 받아와서 마크 찍기

            MarkerOptions markerOptions = new MarkerOptions();


            markerOptions.position(place.getLatLng());
            markerOptions.title(place.getName().toString());
            markerOptions.snippet(place.getAddress().toString());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.place_48));

            Log.d("add",place.getAddress().toString());
            Marker item = map.addMarker(markerOptions);
            item.setTag(new PlaceData(place.getName().toString(),place.getAddress().toString(),place.getLatLng(),"place"));
            previous_marker.add(item);
            map.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            map.animateCamera(CameraUpdateFactory.zoomTo(17));
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    } // 구글 플레이스 정보 가져오기


    private void pickMark(final LatLng LL,String name, String intro,int image_index,Message data) // 위도 경도, 이름 주소 받아서 마커 찍는 함수
    {
        MarkerOptions markerOptions = new MarkerOptions(); // 옵션 설정 해놓을 변수
        markerOptions.position(LL); // 위치 적용
        markerOptions.title(name); // 이름
//        markerOptions.snippet(address.substring(0,20)); // 주소 넣음
        markerOptions.snippet(intro); // 인사 넣음

        markerOptions.draggable(true); // 드래그 가능하도록
        //markerOptions.flat(true);

        String resName = "@drawable/marker_profile" + image_index;
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
       // markerOptions.flat(true);

        String resName = "@drawable/marker_profile" + myImage_index;
        int resID = getResources().getIdentifier(resName, "drawable", this.getPackageName());

        markerOptions.icon(BitmapDescriptorFactory.fromResource(resID));
        // map.addMarker(markerOptions).setFlat(true);


        Marker mk;
        mk = map.addMarker(markerOptions);
        mk.showInfoWindow();
//        map.addMarker(markerOptions).showInfoWindow(); // 맵에 추가
        L_Marker_userlist.add(mk); // 위치정보 마커 리스트에 추가
    } // pickMark





    //setroom
    public String Jsonize(String chat_id1, String chat_id2,String chat_name1,String chat_name2,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,chat_name1,chat_name2,chat_type)); //Data -> Gson -> json
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
//                    finish();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
//                    ActivityCompat.finishAffinity();

                    Process.killProcess(Process.myPid());
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

            msgView.setTextSize(14);




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
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

          //  LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //inflater.inflate(R.layout.profile, container, true);
        } else if (id == R.id.stream) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://youtube.com"))
            .setPackage("com.google.android.youtube"));

        } else if (id == R.id.chat) {
            Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            //Toast.makeText(getApplicationContext(),"채팅",Toast.LENGTH_LONG).show();
        } else if (id == R.id.logout) {
            Toast.makeText(getApplicationContext(),"로그아웃 성공",Toast.LENGTH_LONG).show();
            SharedPreferences pref= getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            finish();
        }else if (id == R.id.rest) {
        placechecker = 0;
            for (Polyline pl : L_Poly) {
                pl.remove();
            }
            L_Poly.clear();
        if(CS.get_key_server_ok()) {
            showPlaceInformation(lastknownlocation,placechecker);
        }
    }else if(id == R.id.bank) {
        placechecker = 1;
            for (Polyline pl : L_Poly) {
                pl.remove();
            }
            L_Poly.clear();
        if(CS.get_key_server_ok()) {
            showPlaceInformation(lastknownlocation,placechecker);
        }
    }else if(id == R.id.bus){
        placechecker = 2;
            for (Polyline pl : L_Poly) {
                pl.remove();
            }
            L_Poly.clear();
        if(CS.get_key_server_ok()) {
            showPlaceInformation(lastknownlocation,placechecker);
        }
    }else if(id == R.id.off){
        placechecker = -1;
            for (Marker mk : previous_marker) {
                mk.remove();
            }
            for (Polyline pl : L_Poly) {
                pl.remove();
            }
            L_Poly.clear();
           previous_marker.clear();
        //map.clear();
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
                    switch(placechecker)
                    {
                        case 0://식당
                        {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_rest));
                            break;
                        }
                        case 1: // 까페
                        {

                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cafe));
                            break;
                        }
                        case 2: // 버스정류장
                        {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bus));
                            break;
                        }
                    }

                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(place.getVicinity());

                    //Log.d("place","icon: "+place.getIcon()+" type:"+place.getTypes().toString());

                    Marker item = map.addMarker(markerOptions);
                    switch(placechecker)
                    {
                        case 0://식당
                        {
                            item.setTag(new PlaceData(place.getName(),place.getVicinity(),new LatLng(place.getLatitude(),place.getLongitude()),"rest"));
                            break;
                        }
                        case 1: // 까페
                        {

                            item.setTag(new PlaceData(place.getName(),place.getVicinity(),new LatLng(place.getLatitude(),place.getLongitude()),"cafe"));
                            break;
                        }
                        case 2: // 버스정류장
                        {
                            item.setTag(new PlaceData(place.getName(),place.getVicinity(),new LatLng(place.getLatitude(),place.getLongitude()),"bus"));
                            break;
                        }
                    }
                    previous_marker.add(item);
                    Log.d("add",place.getVicinity().toString());
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

    public void showPlaceInformation(LatLng location,int placetype) {
        // map.clear();//지도 지우기

        if (previous_marker != null) {
            for (Marker mk : previous_marker) {
                mk.remove();
            }
            previous_marker.clear();//마커지우기
        }
        switch (placetype) {
            case 0: { // 음식점
                new NRPlaces.Builder()
                        .listener(MapActivity.this)
                        .key("AIzaSyDdwDyx5xMSgY_b7IPNnCrB9qWLMQ-EDgM")
                        .latlng(location.latitude, location.longitude)
                        .radius(500)
                        .language("ko", "KR")
                        .type(PlaceType.RESTAURANT)
                        .build()
                        .execute();
                break;

            }
            case 1: { // 은행
                new NRPlaces.Builder()
                        .listener(MapActivity.this)
                        .key("AIzaSyDdwDyx5xMSgY_b7IPNnCrB9qWLMQ-EDgM")
                        .latlng(location.latitude, location.longitude)
                        .radius(500)
                        .language("ko", "KR")
                        .type(PlaceType.CAFE)
                        .build()
                        .execute();
                break;
            }
            case 2: { // 버스정류장
                new NRPlaces.Builder()
                        .listener(MapActivity.this)
                        .key("AIzaSyDdwDyx5xMSgY_b7IPNnCrB9qWLMQ-EDgM")
                        .latlng(location.latitude, location.longitude)
                        .radius(500)
                        .language("ko", "KR")
                        .type(PlaceType.BUS_STATION)
                        .build()
                        .execute();
                break;
            }
        }
    }
    private String getUrl(LatLng origin, LatLng dest) // 위치 두개 받아서 길찾기 URL 형식으로 바꿈  // 키 필요   Google Direction APi 이용
    {
        String url = "";
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        if()
        Log.d("l_d",Long.toString(System.currentTimeMillis()));
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        SimpleDateFormat a = new SimpleDateFormat("hh a, zzzz");
//        Log.d("l_d",a.format(date));

        //derection
        url = "https://maps.googleapis.com/maps/api/directions/json?" +  str_origin +"&"+str_dest +"&mode=transit"+"&alternatives=true"+  "&key=AIzaSyC6tzB9C33kG_99yhC0L0jSKhK3KJHycSk";
        return url;
    }

    private class fetchUrl extends AsyncTask<String, Void, String> // AsyncTsk는 일종의 쓰레드 doInBackground 에서 PostExecute로 return값 넘겨줄수 있고, Post Execute는 ui컨트롤 부분 가능 Google Direction APi 이용
    {
        protected String doInBackground(String... url)
        {
            String data="";
            try {
                data = downloadUrl(url[0]); // URL 보내서 정보 받기
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
//            Intent MyIntent = new Intent(getApplicationContext(),Urltextview.class);
//
//            MyIntent.putExtra("url",result+ "\n\n\n************\n\n\n");
//            startActivity(MyIntent);


            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }


    }// fetchUrl

    private String downloadUrl(String strUrl) throws IOException // 만든 URL 보내서 관련 정보 받아오기
    {
        String data = "";
        InputStream iStream = null;
        HttpsURLConnection urlConnection = null;
        Log.d("Url",strUrl);
        try{
            URL url = new URL(strUrl);

            // url 만들기
            urlConnection = (HttpsURLConnection) url.openConnection();

            // 연결
            urlConnection.connect();

            // 데이터 읽기
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null) // 다 읽을 때 까지 버퍼에 계속 넣기
            {
                sb.append(line);
            }

            data = sb.toString(); // 버퍼에 쌓인 내용 저장
            Log.d("downloadUrl", data.toString());
            br.close();

        }
        catch (Exception e)
        {
            Log.d("Urlfail", "urldownloadfail");
        }
        finally
        {
            Log.d("Urlend", "end");
            iStream.close();;
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> // 맵에 길찾기 한 루트를 Polyline을 이용해 그려주고 소요시간, 거리 가져오는 함수 DataParser클래스를 이용해 JSON파싱한 내용을 이용한다. Google Direction APi 이용
    {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//루트 관련 정보 저장
            JSONObject jObject_route;
            List<List<HashMap<String,String >>> routes = null;
//            List<List<String>> DD;

            try {
                jObject_route = new JSONObject(jsonData[0]);
                //jObject_DD = new JSONObject((jsonData[1]));

                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject_route);

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }// doinback

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);


                Log.d("d_parsing", "path size: " + Integer.toString(path.size()));
                // Fetching all the points in i-th route

                for (int j = 0; j < path.size(); j++) { // 패스 수 많금 포문
                    HashMap<String, String> point = path.get(j);

                    if(point.containsKey("Distance")||point.containsKey("Duration")) { // 거리나 소요시간 키를 가지고 있으면
                        Dis = point.get("Distance"); // 그 거리 정보 가져온다.
                        Dur = point.get("Duration"); // 그 소요시간 정보 가져온다.
//                        tv.append(Dis + " , " + Dur + "\n"); // 텍스트 뷰에 그 정보들 뿌려준다.
                    }
                    else{
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
//                        Log.d("d_parsing", "lat: " + Double.toString(lat) + "  lng:" + Double.toString(lng));
                        points.add(position);
                    }
                    if(point.containsKey("bus"))
                    {
                        Bus = point.get("bus");
                        bus_marker.setTitle("버스 노선: " +Bus);
                        bus_marker.setSnippet("거리: " +Dis+" , "+"소요 시간: "+Dur);
                        bus_marker.showInfoWindow();
                    }


                } // for
                // Adding all the points in the route to LineOptions
//                    lineOptions.addAll(points);
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.rgb(33, 142, 233));//8EC7fF

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                L_Poly.add(map.addPolyline(lineOptions));

            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }

    }// ParserTask


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



