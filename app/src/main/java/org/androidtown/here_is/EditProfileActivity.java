package org.androidtown.here_is;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {
    public static final String KEY_USER_DATA2 = "userdata2";
    public static final int REQUEST_CODE_PROFILE2 = 102;

    EditText ID, PW, NAME, INFO, URL;
    Button btn_Edit_index;
    SharedPreferences userinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);

        ID = (EditText) findViewById(R.id.editText_profile_id);
        PW = (EditText) findViewById(R.id.editText_profile_pw);
        NAME = (EditText) findViewById(R.id.editText_profile_name);
        INFO = (EditText) findViewById(R.id.editText_profile_info);
        URL = (EditText) findViewById(R.id.editText_profile_youtube_url);

        ID.setText(userinfo.getString("ID", ""));
        PW.setText(userinfo.getString("PW", ""));
        NAME.setText(userinfo.getString("NAME", ""));
        INFO.setText(userinfo.getString("INFO", ""));
        if(!userinfo.getString("URL","").equals("NO URL")) {
            URL.setText(userinfo.getString("URL", ""));
        }
        //INDEX.setText(userinfo.getString("INDEX", ""));

        btn_Edit_index = (Button) findViewById(R.id.btn_edit_index);
    }

    public void btn_edit_index_Clicked(View v) {
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
            String url = "";
            if(URL.getText().equals("")) {
                url = "NO URL";
            }
            else {
                url = URL.getText().toString();
            }

            Intent intent = new Intent(getApplicationContext(), SignupProfileActivity.class);
            UserData data = new UserData(id, pw, name, info, url);
            intent.putExtra(KEY_USER_DATA2, data);
            startActivityForResult(intent, REQUEST_CODE_PROFILE2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQUEST_CODE_PROFILE2) {
            //Toast.makeText(getApplicationContext(), "onActivityResult 메소드호출됨. 요청코드: " + requestCode+ ", 결과코드: " + resultCode, Toast.LENGTH_LONG).show();
            if (resultCode== RESULT_OK) {
                String result = data.getExtras().getString("result");
                //Toast.makeText(getApplicationContext(), "응답으로전달된 result : " + result, Toast.LENGTH_LONG).show();
                if (result.equals("diff_id")) {
                    Toast.makeText(getApplicationContext(), "이미 존재하는 아이디 입니다.", Toast.LENGTH_LONG).show();
                } else if (result.equals("signup_ok")) {
                    Toast.makeText(getApplicationContext(), "회원가입 완료.", Toast.LENGTH_LONG).show();
                    finish();
                } else if (result.equals("signup_fail")) {
                    Toast.makeText(getApplicationContext(), "회원가입 실패. 다시한번 시도해주세요", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
