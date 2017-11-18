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

public class StartActivity extends AppCompatActivity {
    public static final String KEY_BEFORE_USER_DATA = "before_userdata";
    Button login, signup;
    UserData before_data;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //restoreState();
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

        login = (Button) findViewById(R.id.btn_login);
        signup = (Button) findViewById(R.id.btn_signup);
    }

    public void btn_login_Clicked(View v) {
        //로그인페이지로 이동
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void btn_signup_Clicked(View v) {
        //회원가입창으로 이동
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

    protected void restoreState() {
        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        if ((userinfo != null) && (userinfo.getString("STATUS", "").equals("Login_OK"))) {
            //이전에 저장했던 유저정보에 login_ok라는 정보가 있다면, 바로 MapActivity에 저장 하면될듯
            before_data = new UserData(userinfo.getString("ID", ""),
                    userinfo.getString("PW", ""),
                    userinfo.getString("NAME", ""),
                    userinfo.getString("INFO", ""),
                    userinfo.getString("URL", ""),
                    userinfo.getString("INDEX", ""));
        }
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
