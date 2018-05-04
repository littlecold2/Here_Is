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

        //스플래시 비디오로
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        videoView.setVideoURI(video);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                startNextActivity();
            }
        });
//        videoView.start();
        startNextActivity(); // 이거 위에꺼 쓰면 뺴라
    }

    private void startNextActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}

