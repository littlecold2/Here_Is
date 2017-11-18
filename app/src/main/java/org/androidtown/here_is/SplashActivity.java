package org.androidtown.here_is;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by Vegetable on 2017-11-14.
 */


public class SplashActivity extends Activity {
    @Override

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        try {
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            Thread.sleep(2000);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this,StartActivity.class));
        finish();
    }

}
