package org.androidtown.here_is;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class SignupActivity extends Font {
    public static final String KEY_USER_DATA = "userdata";
    public static final int REQUEST_CODE_PROFILE = 101;

    //회원 가입을 하기위한 EditText들
    EditText ID, PW, NAME, INFO, URL;
    //프로필 사진 고르기 버튼
    Button btn_profile_select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ID = (EditText) findViewById(R.id.editText_signup_id);
        PW = (EditText) findViewById(R.id.editText_signup_pw);
        NAME = (EditText) findViewById(R.id.editText_signup_name);
        INFO = (EditText) findViewById(R.id.editText_signup_info);
        URL = (EditText) findViewById(R.id.editText_youtube_url);

        btn_profile_select = (Button) findViewById(R.id.btn_profile_select);
    }

    //유튜브 연결 버튼이 눌리면 Youtube로 연결
    public void btn_youtube_Clicked(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/live_dashboard"));
        startActivity(intent);
    }

    //프로필 사진 고르기 버튼이 눌리면
    public void btn_profile_select_Clicked(View v) throws ExecutionException, InterruptedException {
        //채워지지 않은 EditText를 체크하여 경고 문구 출력하기 위한 초기 문장
        String check = "";
        int count = 0;

        //각 EditText가 비어있을 경우 비어있는 항목의 이름을 check 문자열에 추가
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


        //모든 항목이 채워져 있을 경우
        else {
                //EditText에서 값을 가져와 변수에 저장
                String id = ID.getText().toString();
                String pw = PW.getText().toString();
                String name = NAME.getText().toString();
                String info = INFO.getText().toString();
                String url = "";
                //Url 항목이 비어있을 경우 NO URL을, 있을 경우 입력된 값을 저장
                if(URL.getText().toString().equals("")) {
                    url = "NO URL";
                }
                else {
                    url = URL.getText().toString();
                }

                //SignupProfileActivity에 입력된 값을들 같이 Intent에 넣어서 보냄
                Intent intent = new Intent(getApplicationContext(), SignupProfileActivity.class);
                UserData data = new UserData(id, pw, name, info, url);
                intent.putExtra(KEY_USER_DATA, data);
                //반환값을 받기 위해서 ForResult 사용
                startActivityForResult(intent, REQUEST_CODE_PROFILE);
        }
    }



    //SignupProgileActivity 종료 시 반환값 받아와서 사용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== REQUEST_CODE_PROFILE) {
            //Toast.makeText(getApplicationContext(), "onActivityResult 메소드호출됨. 요청코드: " + requestCode+ ", 결과코드: " + resultCode, Toast.LENGTH_LONG).show();
            if (resultCode== RESULT_OK) {
                String result = data.getExtras().getString("result");
                //Toast.makeText(getApplicationContext(), "응답으로전달된 result : " + result, Toast.LENGTH_LONG).show();


                //서버에서 반환된 값을 사용하여 그에 맞는 토스트 메시지 출력
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
