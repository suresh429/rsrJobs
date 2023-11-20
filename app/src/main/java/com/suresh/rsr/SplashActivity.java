package com.suresh.rsr;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private static  int SPLASH_TIME_OUT = 3000;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            // shared animation between two activites
            Intent intent  = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);

        }, SPLASH_TIME_OUT);

    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
