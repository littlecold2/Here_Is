package org.androidtown.here_is;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

public class ChattingActivity extends AppCompatActivity  {


    private Thread my_thread;
    private String targetID;

    //########service ########
    private ClientService CS ;
    private boolean isService = false;

    private ImageView profieImg;
    private TextView idTextView;
    private TextView messageView;
    private EditText sendEditText;

    //########service ########
    private Intent svcIntent;
    private ServiceConnection conn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
            ClientService.Mybinder mb =(ClientService.Mybinder) service;
            // 서비스와 연결되었을 때 호출되는 메서드
            CS = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            isService =true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

            isService = false;
        }
    };
    //########service ########


    //########브로드 캐스트#######
    private BroadcastReceiver mMessageReceiver = null;
    private static final String EXTRA_GET_MESSAGE ="current_chat_message";
    private static final String EXTRA_ALL_MESSAGE ="all_chat_message";
    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_CHAT"));
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
        sendEditText = (EditText)findViewById(R.id.sendEditText);

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
//
//        Intent getintent = getIntent();
//       // targetID = getintent.getExtras().getString("targetID");
        Intent intent = new Intent(ChattingActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        //브로드캐스트
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getExtras().getString(EXTRA_ALL_MESSAGE);
                messageView.setText(message);
            }
        };
// 브로드캐스트





        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        Button OUTBtn = (Button)findViewById(R.id.OUTbutton);
        Button returnBtn = (Button)findViewById(R.id.returnbutton);

        returnBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });


        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(isService &&CS.get_key_getMessage_ok()&&CS.getChat_room()!=-1) {
                            CS.sendMessage(Jsonize(Build.ID, CS.getChat_room(), "chat", sendEditText.getText().toString()));
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    sendEditText.setText("");

                                }
                            });
                        }
                        else if(CS.getChat_room()==-1)
                        {
                            runOnUiThread(new Runnable() {

                                public void run() {
                                    sendEditText.setText("");
                                    messageView.append("채팅방 비어있음.\n");
                                }
                            });

                        }

                    }
                }).start();
                //......
            }
        });
        OUTBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isService &&CS.get_key_getMessage_ok()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CS.sendMessage(Jsonize(CS.getChat_room(), "logout"));
//                            my_thread.interrupt();
//                            unbindService(conn);
                            //CS.setChat_text_clear();
                        }
                    }).start();
                }
               // onBackPressed();
                finish();
            }
        });

    } // onCreate



    @Override
    protected void onStart() {
        super.onStart();

      //  my_thread = new Thread(this);
       // my_thread.start();
    }


    @Override
    protected void onDestroy() {
        Log.d("chat","destroy");

//        if(CS.get_key_getMessage_ok()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    CS.sendMessage(Jsonize(CS.getChat_room(), "logout"));
//                    CS.set_key_chat(false);
//                    //CS.setChat_text_clear();
//                }
//            }).start();
//        }

       // my_thread.interrupt();
        if(isService) {
            stopService(svcIntent);
            unbindService(conn);
        }
        super.onDestroy();
    }

//    @Override
//    public void run() {
//        Log.d("chat", String.valueOf(isService));
//
//        while(!my_thread.isInterrupted()) {
//            try{
//                Thread.sleep(1000);
//                Log.d("chat", "chat_sleep");
//            }catch (InterruptedException e) {
//                e.printStackTrace();
//                Thread.currentThread().interrupt();
//            }
//                runOnUiThread(new Runnable() {
//
//                    public void run() {
//                        if(isService) {
//                            messageView.setText(CS.getChat_text());
//                        }
//
//
//                    }
//                });
//        }
//
//    }
//



    //setroom
    public String Jsonize(String chat_id1, String chat_id2,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,chat_type)); //Data -> Gson -> json
        return json;

    }
    // chat
    public String Jsonize(String id, int chat_room,String chat_type,String chat_text) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(id,chat_room,chat_type,chat_text)); //Data -> Gson -> json
        return json;

    }
    // logout
    public String Jsonize(int chat_room ,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_room,chat_type)); //Data -> Gson -> json
        return json;

    }

}
