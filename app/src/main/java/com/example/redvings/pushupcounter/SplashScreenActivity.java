package com.example.redvings.pushupcounter;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class SplashScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent in=new Intent(getApplicationContext(), HomeActivity.class);
				startActivity(in);
				finish();
			}
		},1300);
	}

}
