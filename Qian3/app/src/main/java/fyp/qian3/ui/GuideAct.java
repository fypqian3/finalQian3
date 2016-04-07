package fyp.qian3.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import fyp.qian3.R;

public class GuideAct extends Activity {
    Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // If it's not the first time to run, skip to home activity
        if (!sharedPrefs.getBoolean("pref_firstTime", true)) {
            startActivity(new Intent(GuideAct.this, HomeAct.class));
            finish();
        }

        btnSkip = (Button) findViewById(R.id.btnGuideSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("pref_firstTime", false);
                editor.commit();
                startActivity(new Intent(GuideAct.this, LoginAct.class));

                finish();
            }
        });

    }
}
