package org.androidtown.here_is;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class ChattingActivity extends Font {
    //########service ########
    private ClientService CS ; // 서비스 변수
    private boolean isService = false; // 서비스 바인드 확인 변수

    private ImageView profieImg; // 프로필이미지뷰
    private TextView idTextView; // 아이디 텍스트뷰
    private TextView messageView; // 채팅 텍스트 뷰
    private ScrollView scrollView; // 스크롤뷰
    private EditText sendEditText; // 채팅 입력창

    private SharedPreferences userinfo; // 저장된 유저정보 가져오기
    private String myID; // 내 아이디 저장
    private String myName; // 내 이름 저장

    private String chatName="비어있음"; //
    private int image_index=1; // 프로필 이미지 인덱스


    //########service ########
    private Intent svcIntent; // 스타트 서비스 할 서비
    private ServiceConnection conn = new ServiceConnection() { // 서비스 코넥션
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) { // 연결 되면
            ClientService.Mybinder mb =(ClientService.Mybinder) service;
            // 서비스와 연결되었을 때 호출되는 메서드
            CS = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            isService =true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { // 연결 끊어지면

            isService = false;
        }
    };
    //########service ########


    //########브로드 캐스트#######
    private BroadcastReceiver mMessageReceiver = null; // 채팅 메시지 받는
    private BroadcastReceiver setMessageReceiver = null; // 채팅방 만들어질때 받음, 상대방 정보 세팅
    private static final String EXTRA_GET_MESSAGE ="current_chat_message";
    private static final String EXTRA_ALL_MESSAGE ="all_chat_message";
    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_CHAT")); // 채팅 메시지
        LocalBroadcastManager.getInstance(this).registerReceiver(setMessageReceiver, new IntentFilter("EVENT_CHAT_SET")); // 채팅방 만들어질 떄
    }
// ########브로드 캐스트#######


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("chat","oncreateCHAT");
        setContentView(R.layout.activity_chatting);
        profieImg = (ImageView)findViewById(R.id.profileImg);
        idTextView = (TextView)findViewById(R.id.idTextView);
        messageView = (TextView)findViewById(R.id.messageView);
        scrollView = (ScrollView)findViewById(R.id.chat_scroll);
        sendEditText = (EditText)findViewById(R.id.sendEditText);



        // 저장된 데이터 가져옴
        userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        myID = userinfo.getString("ID","");
        myName= userinfo.getString("NAME","");


        sendEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                switch(actionId) {
                    case EditorInfo.IME_ACTION_GO:
                        sendEditText.setText("");
                        break;
                }
                return true;
            }
        });

        svcIntent =new Intent(this, ClientService.class);
        startService(svcIntent);
        final Intent intent = new Intent(ChattingActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        //이미지 설정
        String resName = "@drawable/profile" + image_index;
        int resID = getResources().getIdentifier(resName, "drawable", this.getPackageName());
        profieImg.setImageResource(resID);

        idTextView.setText(chatName);

        //브로드캐스트
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { // 채팅 메시지 받을 때
                String message = intent.getExtras().getString(EXTRA_ALL_MESSAGE);
                messageView.setText(message);
                scrollView.fullScroll(View.FOCUS_DOWN); // 받은 메시지 스크롤뷰 포커스 아래로
            }
        };
        setMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { // 상대방과의 채팅 방 생성 될 때
                chatName = intent.getExtras().getString("name");
                image_index = intent.getExtras().getInt("image");
                String resName = "@drawable/profile" + image_index;
                int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());
                profieImg.setImageResource(resID);
                idTextView.setText(chatName);
            }
        };
// 브로드캐스트

        // 채팅 전송 버튼
        ImageButton sendBtn = (ImageButton)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(isService &&CS.get_key_server_ok()&&CS.getChat_room()!=-1) { // 서비스 연결 됫고 서버 연결됫고 상대방과의 채팅방이 있을 때 만
                            CS.sendMessage(Jsonize(myID,myName, CS.getChat_room(), "chat", sendEditText.getText().toString())); // 채팅메시지에 내 ID,이름, 채팅방번호,chat타입, 채팅내용 담아 보낸다
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    sendEditText.setText(""); //보냇으면 전송 텍스트부분 초기화

                                }
                            });
                        }
                        else if(CS.getChat_room()==-1) // 방이 비었으면
                        {
                            runOnUiThread(new Runnable() {

                                public void run() {
                                    sendEditText.setText("");
                                    messageView.append("채팅방 비어있음.\n");
                                    scrollView.fullScroll(View.FOCUS_DOWN);

                                }
                            });

                        }

                    }
                }).start();
            }
        });



        // 메뉴를 위한 바터바
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar_chat);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {

            @Override
            public void onTabSelected(@IdRes int tabId) {

                if(tabId == R.id.tab1){
                }
                else if(tabId == R.id.tab2){ // 채팅방 로그아웃 버튼

                    if(isService &&CS.get_key_server_ok()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CS.sendMessage(Jsonize(CS.getChat_room(), "chat_logout"));
                                finish();
                            }
                        }).start();
                    }

                }
//

            }
        });
        // 바텀바 다시 누를 때 리스너
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if(tabId == R.id.tab1){ // 채팅내용 유지 , 맵으로 돌아가기
                    onBackPressed();
                }
                else if(tabId == R.id.tab2){
                    if(isService &&CS.get_key_server_ok()) { // 채팅방 로그아웃
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CS.sendMessage(Jsonize(CS.getChat_room(), "chat_logout"));
                                finish();
                            }
                        }).start();
                    }
                    finish();
                }

            }
        });


    } // onCreate


    @Override
    protected void onDestroy() {
        Log.d("chat","destroy");

        // 채팅방 나갈때 서비스 바인드 해제
        if(isService) {
            unbindService(conn);
            stopService(svcIntent);

        }
        super.onDestroy();
    }


    // chat
    public String Jsonize(String id, String name,int chat_room,String chat_type,String chat_text) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,name,chat_room,chat_type,chat_text)); //Data -> Gson -> json
        return json;

    }
    // chat_logout
    public String Jsonize(int chat_room ,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_room,chat_type)); //Data -> Gson -> json
        return json;

    }


}
