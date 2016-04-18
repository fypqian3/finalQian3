package com.counter.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

import com.counter.app.R;


public class LoadingAct extends Activity {
    // Button btnSkip;
    ImageView ivLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ivLoading = (ImageView) findViewById(R.id.loading);
        ivLoading.setBackgroundResource(R.drawable.animation_loading);

        // Get the background, which has been compiled to an AnimationDrawable object.
        AnimationDrawable frameAnimation = (AnimationDrawable) ivLoading.getBackground();

        // Start the animation (looped playback by default).
        frameAnimation.start();

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startActivity(new Intent(LoadingAct.this, HomeAct.class));
            }
        }.start();

    }
}
