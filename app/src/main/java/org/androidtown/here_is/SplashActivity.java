package org.androidtown.here_is;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.VideoView;

import static java.lang.Thread.sleep;

/**
 * Created by Vegetable on 2017-11-14.
 */


public class SplashActivity extends Activity {
    VideoView videoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        videoView = (VideoView) findViewById(R.id.videoView);

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        videoView.setVideoURI(video);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                startNextActivity();
            }
        });
        Log.d("splash","1");
        videoView.start();
        Log.d("splash","2");
    }

    private void startNextActivity() {
        Log.d("splash","???");
        //videoView.suspend();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}


/*

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        try {
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            Thread.sleep(2000);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

 */