package org.androidtown.here_is;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends Font {
    public static final String KEY_BEFORE_USER_DATA = "before_userdata";
    EditText ID, PW;
    Button Login;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
    private boolean mPermissionDenied = false;

    UserData before_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        enableMyLocation();

        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        if ((userinfo != null) && (userinfo.getString("STATUS", "").equals("Login_OK"))) {
            //이전에 저장했던 유저정보에 login_ok라는 정보가 있다면, 바로 MapActivity에 저장 하면될듯
            before_data = new UserData(userinfo.getString("ID", ""),
                    userinfo.getString("PW", ""),
                    userinfo.getString("NAME", ""),
                    userinfo.getString("INFO", ""),
                    userinfo.getString("URL", ""),
                    userinfo.getString("INDEX", ""));

            //Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            //intent.putExtra(KEY_BEFORE_USER_DATA, before_data);
            startActivity(intent);
            //finish();
        }

        ID = (EditText) findViewById(R.id.editText_ID);
        PW = (EditText) findViewById(R.id.editText_PW);
        Login = (Button) findViewById(R.id.btn_login_check);

    }

    public void btn_login_check_Clicked(View v) throws ExecutionException, InterruptedException, JSONException {
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
            JSONObject jsondata =  new JSONObject(result);
            JSONArray jsondata2 = jsondata.getJSONArray("userdata");
            JSONObject jsondata3 = jsondata2.getJSONObject(0);

            //Toast.makeText(getApplicationContext(), jsondata3.getString("status").toString(), Toast.LENGTH_LONG).show();




            if(jsondata3.getString("status").equals("login_ok")) {
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();

                UserData userInfo = new UserData(jsondata3.getString("id"),
                        PW.getText().toString(),
                        jsondata3.getString("name"),
                        jsondata3.getString("info"),
                        jsondata3.getString("url"),
                        jsondata3.getString("index"));

                //로그인 성공 시 user 정보 저장
                saveUserinfo(userInfo);

                //로그인 성공이므로 MapActivity 실행
                //Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra(KEY_BEFORE_USER_DATA, userInfo);
                startActivity(intent);
                finish();
            }
            else if(jsondata3.getString("status").equals("login_fail")) {
                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.\nID, PW를 확인해주세요.", Toast.LENGTH_LONG).show();
            }

        }
    }
    public void btn_signup_Clicked(View v) {
        //회원가입창으로 이동
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }


    protected void saveUserinfo(UserData userData) {
        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = userinfo.edit();
        editor.putString("STATUS", "Login_OK");
        editor.putString("ID", userData.getID());
        editor.putString("PW", userData.getPW());
        editor.putString("NAME", userData.getNAME());
        editor.putString("INFO", userData.getINFO());
        editor.putString("URL", userData.getURL());
        editor.putString("INDEX", userData.getINDEX());
        editor.commit();
    }


    // 위치 퍼미션
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            org.androidtown.here_is.PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
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

}
