package org.androidtown.here_is;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class shared_test extends AppCompatActivity {
    public static final String KEY_BEFORE_USER_DATA = "before_userdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_test);

        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(), userinfo.getString("ID", "") + ", "+ userinfo.getString("PW", ""), Toast.LENGTH_LONG).show();
    }


    //정보수정
    //EditProfileActivity 실행
    public void btn_edit_info_Clicked(View v) {
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        startActivity(intent);
    }


    //로그아웃 기능
    //sharedpreference에 저장된 데이터 전부 삭제
    public void button2_Clicked(View v) {
        SharedPreferences pref= getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        finish();
    }
}
