package com.example.redvings.pushupcounter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    public void start(View v) {
        Intent mainAct = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainAct);
    }
    public void goal(View v) {
        Intent in=new Intent(getApplicationContext(), ReverseCountActivity.class);
        startActivity(in);
    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));  //opening Mysetting activity

            //this is for language change
                /*String languageToLoad = "en"; // your language
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                this.setContentView(R.layout.activity_main);
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }
}
