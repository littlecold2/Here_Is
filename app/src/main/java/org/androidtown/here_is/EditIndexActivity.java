package org.androidtown.here_is;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class EditIndexActivity extends Font {
    public static final String KEY_USER_DATA2 = "userdata2";

    //화면의 이미지 버튼 9개
    ImageButton img1, img2, img3, img4, img5, img6, img7, img8, img9;
    //회원정보 변경 버튼
    Button edit_profile;

    //UserData 형식의 data 설정
    UserData data;
    //초기 index는 공백으로 설정
    String index = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_index);

        edit_profile = (Button) findViewById(R.id.btn_edit_profile);

        img1 = (ImageButton) findViewById(R.id.img_1e);
        img2 = (ImageButton) findViewById(R.id.img_2e);
        img3 = (ImageButton) findViewById(R.id.img_3e);
        img4 = (ImageButton) findViewById(R.id.img_4e);
        img5 = (ImageButton) findViewById(R.id.img_5e);
        img6 = (ImageButton) findViewById(R.id.img_6e);
        img7 = (ImageButton) findViewById(R.id.img_7e);
        img8 = (ImageButton) findViewById(R.id.img_8e);
        img9 = (ImageButton) findViewById(R.id.img_9e);

        Intent intent = getIntent();
        processIntent(intent);
    }


    //각각의 이미지 버튼 클릭 시 그에 맞는 index로 설정
    public void img_1e_Clicked(View v) { index = "1"; }
    public void img_2e_Clicked(View v) { index = "2"; }
    public void img_3e_Clicked(View v) { index = "3"; }
    public void img_4e_Clicked(View v) { index = "4"; }
    public void img_5e_Clicked(View v) { index = "5"; }
    public void img_6e_Clicked(View v) { index = "6"; }
    public void img_7e_Clicked(View v) { index = "7"; }
    public void img_8e_Clicked(View v) { index = "8"; }
    public void img_9e_Clicked(View v) { index = "9"; }

    //EditProfileActivity에서 넘겨준 변경하려는 유저 값을 받아서 전역변수 data에 저장
    private void processIntent(Intent intent) {
        if(intent != null) {
            Bundle bundle = intent.getExtras();

            data = (UserData) bundle.getParcelable(KEY_USER_DATA2);

            //Toast.makeText(getApplicationContext(), "id : " + data.getID() + "\npw : " + data.getPW() + "\nname : " + data.getNAME()
            //        + "\ninfo : " + data.getINFO() + "\nurl : " + data.getURL(), Toast.LENGTH_LONG).show();
        }
    }

    //회원정보 변경 버튼 클릭 시
    public void btn_edit_profile_Clicked(View v) throws ExecutionException, InterruptedException, JSONException {
        //인덱스가 설정 안되었다면 이미지버튼을 클릭하라는 토스트
        if(index.equals("")) {
            Toast.makeText(getApplicationContext(), "이미지를 선택해 주세요.", Toast.LENGTH_LONG).show();
        }
        //이미지가 선택되었을 경우
        else {
            //Server와 연결할 때 edit_profile이라는 type으로 php를 선택해야해서 type설정
            String type = "edit_profile";

            //EditProfileActivity에서 받아온 데이터를 각각 변수에 저장
            String id = data.getID().toString();
            String pw = data.getPW().toString();
            String name = data.getNAME().toString();
            String info = data.getINFO().toString();
            String url = data.getURL().toString();

            String result = "";

            //ServerConn.excute을 통해서 변경하려는 유저의 정보를 보냄
            //.get을 통해서 서버의 반환값을 result에 저장
            result = new ServerConn(this).execute(type, id, pw, name, info, url, index).get();

            //로그로 return값 확인
            Log.d("server", result);

            //JSON형식으로 온 데이터를 저장 후 반환 값 확인
            JSONObject jsondata =  new JSONObject(result);
            JSONArray jsondata2 = jsondata.getJSONArray("editdata");
            JSONObject jsondata3 = jsondata2.getJSONObject(0);

            //반환값을 다시 EditProfileActivity에 전달하기 위해 저장
            Intent intent = new Intent();
            intent.putExtra("result", jsondata3.getString("status"));
            intent.putExtra("index", index);

            //전달 후 종료료
           setResult(RESULT_OK, intent);
            finish();
        }
    }
}
