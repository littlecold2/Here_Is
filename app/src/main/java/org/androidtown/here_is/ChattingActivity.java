package org.androidtown.here_is;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ChattingActivity extends AppCompatActivity {


    //########service ########
    private ClientService CS;
    private boolean isService = false;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ClientService.Mybinder mb =(ClientService.Mybinder) service;
            CS = mb.getService();
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

        CS = new ClientService();
        Intent intent = new Intent(ChattingActivity.this, ClientService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        for (int i=0;i<100;i++)
            Log.d("CA", String.valueOf(CS.getChat_room())+"    "+CS.getMessage_List());


    }
}
