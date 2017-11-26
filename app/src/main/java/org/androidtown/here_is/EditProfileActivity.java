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

public class EditProfileActivity extends Font {
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

            Intent intent = new Intent(getApplicationContext(), EditIndexActivity.class);
            UserData data = new UserData(id, pw, name, info, url);
            intent.putExtra(KEY_USER_DATA2, data);
            startActivityForResult(intent, REQUEST_CODE_PROFILE2);
        }
    }

    protected void Edit_saveUserinfo(UserData userData) {
        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = userinfo.edit();

        editor.clear();

        editor.putString("STATUS", "Login_OK");
        editor.putString("ID", userData.getID());
        editor.putString("PW", userData.getPW());
        editor.putString("NAME", userData.getNAME());
        editor.putString("INFO", userData.getINFO());
        editor.putString("URL", userData.getURL());
        editor.putString("INDEX", userData.getINDEX());

        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQUEST_CODE_PROFILE2) {
            //Toast.makeText(getApplicationContext(), "onActivityResult 메소드호출됨. 요청코드: " + requestCode+ ", 결과코드: " + resultCode, Toast.LENGTH_LONG).show();
            if (resultCode== RESULT_OK) {
                String result = data.getExtras().getString("result");
                String index = data.getExtras().getString("index");

                //Toast.makeText(getApplicationContext(), "응답으로전달된 result : " + result, Toast.LENGTH_LONG).show();
                if (result.equals("no_id")) {
                    Toast.makeText(getApplicationContext(), "로그아웃 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                } else if (result.equals("edit_ok")) {
                    UserData userInfo;
                    if(URL.getText().toString().equals("")) {
                        userInfo = new UserData(ID.getText().toString(),
                                PW.getText().toString(),
                                NAME.getText().toString(),
                                INFO.getText().toString(),
                                URL.getText().toString(),
                                index);
                    }
                    else {
                        userInfo = new UserData(ID.getText().toString(),
                                PW.getText().toString(),
                                NAME.getText().toString(),
                                INFO.getText().toString(),
                                "NO URL",
                                index);
                    }

                    Edit_saveUserinfo(userInfo);

                    Toast.makeText(getApplicationContext(), "정보수정 완료.", Toast.LENGTH_LONG).show();
                    finish();
                } else if (result.equals("edit_fail")) {
                    Toast.makeText(getApplicationContext(), "정보수정 실패. 다시 시도해주세요", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
