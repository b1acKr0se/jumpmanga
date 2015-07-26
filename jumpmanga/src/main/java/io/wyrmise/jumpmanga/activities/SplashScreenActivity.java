package io.wyrmise.jumpmanga.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import io.wyrmise.jumpmanga.R;

public class SplashScreenActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);

                startActivity(i);

                finish();

                overridePendingTransition(R.anim.activity_fade_in,
                        R.anim.splash_fade_out);

            }
        }, SPLASH_TIME_OUT);
    }

}