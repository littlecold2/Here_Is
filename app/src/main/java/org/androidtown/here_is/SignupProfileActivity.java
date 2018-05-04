package org.androidtown.here_is;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SignupProfileActivity extends Font {
    public static final String KEY_USER_DATA = "userdata";
    public static final int REQUEST_CODE_PHOTO = 111;
    Button btn_profile_signup;
    ImageButton img1, img2, img3, img4, img5, img6, img7, img8, img9;

    UserData data;
    String index = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_profile);

        btn_profile_signup = (Button) findViewById(R.id.btn_profile_signup);

        img1 = (ImageButton) findViewById(R.id.img_1);
        img2 = (ImageButton) findViewById(R.id.img_2);
        img3 = (ImageButton) findViewById(R.id.img_3);
        img4 = (ImageButton) findViewById(R.id.img_4);
        img5 = (ImageButton) findViewById(R.id.img_5);
        img6 = (ImageButton) findViewById(R.id.img_6);
        img7 = (ImageButton) findViewById(R.id.img_7);
        img8 = (ImageButton) findViewById(R.id.img_8);
        img9 = (ImageButton) findViewById(R.id.img_9);

        Intent intent = getIntent();
        processIntent(intent);
    }

    public void img_1_Clicked(View v) {
        index = "1";
    }
    public void img_2_Clicked(View v) {
        index = "2";
    }
    public void img_3_Clicked(View v) {
        index = "3";
    }
    public void img_4_Clicked(View v) {
        index = "4";
    }
    public void img_5_Clicked(View v) {
        index = "5";
    }
    public void img_6_Clicked(View v) {
        index = "6";
    }
    public void img_7_Clicked(View v) {
        index = "7";
    }
    public void img_8_Clicked(View v) {
        index = "8";
    }
    public void img_9_Clicked(View v) {
        index = "9";
    }

    private void processIntent(Intent intent) {
        if(intent != null) {
            Bundle bundle = intent.getExtras();

            data = (UserData) bundle.getParcelable(KEY_USER_DATA);

            //Toast.makeText(getApplicationContext(), "id : " + data.getID() + "\npw : " + data.getPW() + "\nname : " + data.getNAME()
            //        + "\ninfo : " + data.getINFO() + "\nurl : " + data.getURL(), Toast.LENGTH_LONG).show();
        }
    }
    public void btn_photo_signup_Clicked(View v)
    {
        Intent intent = new Intent(getApplicationContext(), SignupPhotoActivity.class);
        intent.putExtra(KEY_USER_DATA, data);
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
//        startActivity(intent);
    }


    public void btn_profile_signup_Clicked(View v) throws ExecutionException, InterruptedException, JSONException {
        if(index.equals("")) {
            Toast.makeText(getApplicationContext(), "이미지를 선택해 주세요.", Toast.LENGTH_LONG).show();
        }
        else {
            String type = "signup";

            String id = data.getID().toString();
            String pw = data.getPW().toString();
            String name = data.getNAME().toString();
            String info = data.getINFO().toString();
            String url = data.getURL().toString();

            String result = "";
            //Toast.makeText(getApplicationContext(), "before", Toast.LENGTH_LONG).show();


            //result = new ServerConn(this).execute(type, "wqwwq96", "1234", "shj", "hello", "no", "6").get();

            //result = new ServerConn(this).execute(type, data.getID(), data.getPW(), data.getNAME(), data.getINFO(), data.getURL(), index).get();

            result = new ServerConn(this).execute(type, id, pw, name, info, url, index).get();
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

//            if(index.equals("0"))
//            {
//
//            }
            Log.d("kkkk", result);

            JSONObject jsondata =  new JSONObject(result);
            JSONArray jsondata2 = jsondata.getJSONArray("signupdata");
            JSONObject jsondata3 = jsondata2.getJSONObject(0);

            result = jsondata3.getString("status").toString();

            Intent intent = new Intent();
            intent.putExtra("result", result);
            setResult(RESULT_OK, intent);
            finish();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTO) {
            //Toast.makeText(getApplicationContext(), "onActivityResult 메소드호출됨. 요청코드: " + requestCode+ ", 결과코드: " + resultCode, Toast.LENGTH_LONG).show();
            if (resultCode == RESULT_OK) {
                String result = data.getExtras().getString("result");
                //Toast.makeText(getApplicationContext(), "응답으로전달된 result : " + result, Toast.LENGTH_LONG).show();


                if (result.equals("yes")) {
                    Toast.makeText(getApplicationContext(), "촬영된 사진으로 프로필을 지정합니다.", Toast.LENGTH_LONG).show();
                    index = "0";
                    Log.d("photo","if");
                } else if (result.equals("no")) {
                    Toast.makeText(getApplicationContext(), "사진 취소", Toast.LENGTH_LONG).show();
                    index = "";
                    Log.d("photo","else if");
                }
                else {
                    Toast.makeText(getApplicationContext(), "?????", Toast.LENGTH_LONG).show();
                    Log.d("photo","else");
                }
            }
        }
    }

}
