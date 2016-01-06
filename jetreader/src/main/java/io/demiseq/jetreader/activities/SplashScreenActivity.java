package io.demiseq.jetreader.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import io.demiseq.jetreader.R;
import io.demiseq.jetreader.exception.ExceptionHandler;

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}