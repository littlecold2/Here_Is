package org.androidtown.here_is;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class SignupActivity extends AppCompatActivity {
    EditText ID, PW, NAME, INFO;
    Button btn_signup_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ID = (EditText) findViewById(R.id.editText_signup_id);
        PW = (EditText) findViewById(R.id.editText_signup_pw);
        NAME = (EditText) findViewById(R.id.editText_signup_name);
        INFO = (EditText) findViewById(R.id.editText_signup_info);

        btn_signup_check = (Button) findViewById(R.id.btn_signup_check);

    }

    public void btn_signup_check_Clicked(View v) throws ExecutionException, InterruptedException {
        String check = "";
        int count = 0;

        if(ID.getText().length() == 0) {
            check += "ID";
            count++;
        }
        if(PW.getText().length() == 0) {
            if(count == 0) {
                check += "PW";
                count++;
            }
            else {
                check += ", PW";
                count++;
            }
        }

        if(NAME.getText().length() == 0) {
            if(count == 0) {
                check += "이름";
                count++;
            }
            else {
                check += ", 이름";
                count++;
            }
        }

        if(INFO.getText().length() == 0) {
            if(count == 0){
                check += "자기소개";
                count++;
            }
            else {
                check += ", 자기소개";
                count++;
            }
        }

        if(count != 0) {
            check += " 칸을 채워주세요.";
            Toast.makeText(getApplicationContext(), check, Toast.LENGTH_LONG).show();
        }

        else {
            String id = ID.getText().toString();
            String pw = PW.getText().toString();
            String name = NAME.getText().toString();
            String info = INFO.getText().toString();
            String index = "1";
            String type = "signup";

            String result = "";
            //Toast.makeText(getApplicationContext(), "before", Toast.LENGTH_LONG).show();

            result = new ServerConn(this).execute(type, id, pw, name ,info, index).get();
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            if(result.equals("diff_id")) {
                Toast.makeText(getApplicationContext(), "이미 존재하는 아이디 입니다.", Toast.LENGTH_LONG).show();
            }

            else if(result.equals("signup_ok")) {
                Toast.makeText(getApplicationContext(), "회원가입 완료.", Toast.LENGTH_LONG).show();
            }

            else if(result.equals("signup_fail")) {
                Toast.makeText(getApplicationContext(), "회원가입 실패.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
