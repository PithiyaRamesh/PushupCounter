package com.pushupcounter;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsActivity extends Activity {
	private SharedPreferences sharedpreferences;
	private CheckBox sound_chkbx,vibrate_chkbx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		vibrate_chkbx=findViewById(R.id.chkbx_mysettings_vibrate);
		sound_chkbx=findViewById(R.id.chkbx_mysettings_sound);

		sharedpreferences =getSharedPreferences("Mypref", Context.MODE_PRIVATE);
		try {
			if (sharedpreferences.contains("counter")) {
				vibrate_chkbx.setChecked(sharedpreferences.getBoolean("counter", true));
			} else {
				vibrate_chkbx.setChecked(true);
			}
			if (sharedpreferences.contains("sound")) {
				sound_chkbx.setChecked(sharedpreferences.getBoolean("sound", true));
			} else {
				sound_chkbx.setChecked(true);
			}
		}
		catch(Exception ex)
		{
			Toast.makeText(getApplicationContext(), "ERROR: "+ex, Toast.LENGTH_LONG).show();
		}
	}
	public void Save(View v)
	{
		boolean vib = false;
		if(vibrate_chkbx.isChecked())
		{
			vib = true;
		}
		boolean sound = false;
		if(sound_chkbx.isChecked())
		{
			sound = true;
		}

		SharedPreferences.Editor editor=sharedpreferences.edit();
		editor.putBoolean("counter", vib);
		editor.putBoolean("sound", sound);
		editor.apply();
		Toast.makeText(getApplicationContext(), "Settings Saved!", Toast.LENGTH_SHORT).show();
		this.finish();
	}
}
