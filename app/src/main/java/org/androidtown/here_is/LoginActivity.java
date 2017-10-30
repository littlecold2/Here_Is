package org.androidtown.here_is;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    EditText ID, PW;
    Button Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ID = (EditText) findViewById(R.id.editText_ID);
        PW = (EditText) findViewById(R.id.editText_PW);
        Login = (Button) findViewById(R.id.btn_login_check);
    }

    public void btn_login_check_Clicked(View v) throws ExecutionException, InterruptedException {
        if (ID.getText().length() == 0 && PW.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "ID, PW를 입력해주세요", Toast.LENGTH_LONG).show();
        } else if (ID.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_LONG).show();
        } else if (PW.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "PW를 입력해주세요", Toast.LENGTH_LONG).show();
        } else {
            String id = ID.getText().toString();
            String pw = PW.getText().toString();
            String type = "login";

            //LoginRequest loginRequest = new LoginRequest(this);
            //loginRequest.execute(type, id, pw);

            String result = "";
            result = new ServerConn(this).execute(type, id, pw).get();
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            if(result.equals("login_ok")) {
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
            else if(result.equals("login_fail")) {
                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다. ID, PW를 확인해주세요.", Toast.LENGTH_LONG).show();
            }

        }
    }
}
