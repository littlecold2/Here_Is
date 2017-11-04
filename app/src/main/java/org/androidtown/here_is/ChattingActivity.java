package org.androidtown.here_is;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

public class ChattingActivity extends AppCompatActivity implements Runnable {


    private String targetID;

    //########service ########
    private ClientService CS ;
    private boolean isService = false;
    ServiceConnection conn = new ServiceConnection() {
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

    ImageView profieImg;
    TextView idTextView;
    TextView messageView;
    EditText sendEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Intent getintent = getIntent();
        targetID = getintent.getExtras().getString("targetID");
        Intent intent = new Intent(ChattingActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);



        Thread my_thread = new Thread(this);
        my_thread.start();


    }

    @Override
    protected void onPause() {
        unbindService(conn);
        super.onPause();

    }

    @Override
    protected void onPostResume() {
        Intent intent = new Intent(ChattingActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        super.onPostResume();
    }

    @Override
    public void run() {

//        Log.d("chat", String.valueOf(isService));
////        CS = new ClientService();
//        Intent intent = new Intent(ChattingActivity.this, ClientService.class);
//        bindService(intent, conn, Context.BIND_AUTO_CREATE);
//
//        Log.d("chat", String.valueOf(isService));
        while(true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
         //   if (CS.get_key_getMessage_ok())
              //  super.CS.sendMessage(Jsonize(Build.ID, targetID, "room_set"));
        }
       // Log.d("chat", String.valueOf(CS.getMyLocation().getLatitude()));
        //CS.sendMessage(Jsonize(Build.ID,targetID,"room_set"));
    }


    //setroom
    public String Jsonize(String chat_id1, String chat_id2,String chat_type ) // 데이터 받아서 JSON화 하는 함수 Data -> Gson -> json
    {

        String json = new Gson().toJson(new Message(chat_id1,chat_id2,chat_type)); //Data -> Gson -> json
        return json;

    }
}
