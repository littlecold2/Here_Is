package org.androidtown.here_is;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditProfileActivity extends AppCompatActivity {
    EditText ID, PW, NAME, INFO, YOUTUBE;
    Button btn_Edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ID = (EditText) findViewById(R.id.editText_profile_id);
        PW = (EditText) findViewById(R.id.editText_profile_pw);
        NAME = (EditText) findViewById(R.id.editText_profile_name);
        INFO = (EditText) findViewById(R.id.editText_profile_info);
        YOUTUBE = (EditText) findViewById(R.id.editText_youtube_url);

        btn_Edit = (Button) findViewById(R.id.btn_profile_edit);
    }

    public void btn_profile_edit_Clicked(View v) {

    }
}
