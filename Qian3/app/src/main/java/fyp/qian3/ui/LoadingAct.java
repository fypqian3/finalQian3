package fyp.qian3.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import fyp.qian3.R;

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
        // future, interval
        new CountDownTimer(2000,1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startActivity(new Intent(LoadingAct.this, GuideAct.class));
            }
        }.start();


    }

}
