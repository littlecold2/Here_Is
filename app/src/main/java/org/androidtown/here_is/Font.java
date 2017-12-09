package org.androidtown.here_is;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tsengvn.typekit.Typekit;
import com.tsengvn.typekit.TypekitContextWrapper;

/**
 * Created by Pig on 2017-11-26.
 */

// 폰트 전체 적용 할 수있게 여기서 처리하고 상속받아 처리하게
public class Font extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "aL.ttf"))
                .addBold(Typekit.createFromAsset(this, "aB.ttf"));
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

}

