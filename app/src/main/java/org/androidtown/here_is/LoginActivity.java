package org.androidtown.here_is;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private static final int MY_PERMISSION_CAMERA = 1111;

    //로그인을 하기위한 ID, PW 입력 창
    EditText ID, PW;
    //로그인 버튼
    Button Login;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // 위치 권한 쓸때
    private boolean mPermissionDenied = false;

    //이전 로그인 기록이 남아있을 경우 바로 MapActivity로 넘겨주기 위한 정보
    UserData before_data;
    private ImageDownload imageDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        enableMyLocation();
        checkPermission();


        SharedPreferences userinfo = getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        //전에 로그인 후 로그아웃을 안했을 경우 바로 MapActivity 실행
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

    //로그인 버튼 클릭 시
    public void btn_login_check_Clicked(View v) throws ExecutionException, InterruptedException, JSONException {
        //칸이 비워져 있을 경우 에러 메시지 출력
        if (ID.getText().length() == 0 && PW.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "ID, PW를 입력해주세요", Toast.LENGTH_LONG).show();
        } else if (ID.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_LONG).show();
        } else if (PW.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "PW를 입력해주세요", Toast.LENGTH_LONG).show();
        } else {
            //모든 칸이 채워져 있을경우
            String id = ID.getText().toString();
            String pw = PW.getText().toString();
            String type = "login";

            String result = "";
            //ServerConn을 통해서 서버 DB에 접근해서 결과값을 가져옴
            result = new ServerConn(this).execute(type, id, pw).get();
            Log.d("result_",result);
            //JSON 형식의 결과값을 변환
            JSONObject jsondata =  new JSONObject(result);
            JSONArray jsondata2 = jsondata.getJSONArray("userdata");
            JSONObject jsondata3 = jsondata2.getJSONObject(0);

            //서버에서 Login_ok라는 결과를 줬을 경우
            if(jsondata3.getString("status").equals("login_ok")) {
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();

                //UserInfo에 서버에서 받아온 회원 정보를 저장
                UserData userInfo = new UserData(jsondata3.getString("id"),
                        PW.getText().toString(),
                        jsondata3.getString("name"),
                        jsondata3.getString("info"),
                        jsondata3.getString("url"),
                        jsondata3.getString("index"));

                //로그인 성공 시 user 정보 저장

                saveUserinfo(userInfo);
                String imgUrl = "http://littlecold2.iptime.org/here_is/profile_Image";
                imageDownload = new ImageDownload(LoginActivity.this,id);
                imageDownload.execute(imgUrl,"my");


                //로그인 성공이므로 MapActivity 실행
                //Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra(KEY_BEFORE_USER_DATA, userInfo);
                startActivity(intent);
                finish();
            }
            //서버에 회원 정보가 없을 경우
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


    //어플 재 실행 시 바로 MapActivity로 넘어가기 위해서 회원 정보와 로그인 정보를 저장
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

        if(requestCode == MY_PERMISSION_CAMERA) {
            for (int i = 0; i < grantResults.length; i++) {
                // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                if (grantResults[i] < 0) {
                    Toast.makeText(LoginActivity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // 허용했다면 이 부분에서..

        }
        else if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
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

    // 외부저장소 퍼미션

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }




    // 외부저장소 퍼미션 끝


}
