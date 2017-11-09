package org.androidtown.here_is;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class EditProfileActivity extends AppCompatActivity {
    EditText editID, editPW, editNAME, editINFO;
    ImageButton img1, img2, img3, img4;
    Button btn_Edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editID = (EditText) findViewById(R.id.editText_edit_id);
        editPW = (EditText) findViewById(R.id.editText_edit_pw);
        editNAME = (EditText) findViewById(R.id.editText_edit_name);
        editINFO = (EditText) findViewById(R.id.editText_edit_info);

        img1 = (ImageButton) findViewById(R.id.imageView1_edit);
        img2 = (ImageButton) findViewById(R.id.imageView2_edit);
        img3 = (ImageButton) findViewById(R.id.imageView3_edit);
        img4 = (ImageButton) findViewById(R.id.imageView4_edit);

        btn_Edit = (Button) findViewById(R.id.btn_edit_check);
    }

    public void btn_edit_chcek_Clicked(View v) {

    }
}
