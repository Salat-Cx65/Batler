package com.s_komlach.batler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.github.paolorotolo.appintroexample.SecondLayoutIntro;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        if(pref.getBoolean("first_launch", true)) {
            //if(!BuildConfig.DEBUG) {
                SharedPreferences.Editor edit = pref.edit();
                edit.putBoolean("first_launch", false);
                edit.commit();
            //}
            Intent intent = new Intent(this, SecondLayoutIntro.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }



}
