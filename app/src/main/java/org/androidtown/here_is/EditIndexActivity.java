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

public class EditIndexActivity extends AppCompatActivity {
    public static final String KEY_USER_DATA2 = "userdata2";

    ImageButton img1, img2, img3, img4, img5, img6, img7, img8, img9;
    Button edit_profile;

    UserData data;
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

    public void img_1e_Clicked(View v) { index = "1"; }
    public void img_2e_Clicked(View v) { index = "2"; }
    public void img_3e_Clicked(View v) { index = "3"; }
    public void img_4e_Clicked(View v) { index = "4"; }
    public void img_5e_Clicked(View v) { index = "5"; }
    public void img_6e_Clicked(View v) { index = "6"; }
    public void img_7e_Clicked(View v) { index = "7"; }
    public void img_8e_Clicked(View v) { index = "8"; }
    public void img_9e_Clicked(View v) { index = "9"; }

    private void processIntent(Intent intent) {
        if(intent != null) {
            Bundle bundle = intent.getExtras();

            data = (UserData) bundle.getParcelable(KEY_USER_DATA2);

            //Toast.makeText(getApplicationContext(), "id : " + data.getID() + "\npw : " + data.getPW() + "\nname : " + data.getNAME()
            //        + "\ninfo : " + data.getINFO() + "\nurl : " + data.getURL(), Toast.LENGTH_LONG).show();
        }
    }

    public void btn_edit_profile_Clicked(View v) throws ExecutionException, InterruptedException, JSONException {
        if(index.equals("")) {
            Toast.makeText(getApplicationContext(), "이미지를 선택해 주세요.", Toast.LENGTH_LONG).show();
        }
        else {
            String type = "edit_profile";

            String id = data.getID().toString();
            String pw = data.getPW().toString();
            String name = data.getNAME().toString();
            String info = data.getINFO().toString();
            String url = data.getURL().toString();

            String result = "";

            result = new ServerConn(this).execute(type, id, pw, name, info, url, index).get();

            Log.d("server", result);

            JSONObject jsondata =  new JSONObject(result);
            JSONArray jsondata2 = jsondata.getJSONArray("editdata");
            JSONObject jsondata3 = jsondata2.getJSONObject(0);

            Intent intent = new Intent();
            intent.putExtra("result", jsondata3.getString("status"));
            intent.putExtra("index", index);

            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
