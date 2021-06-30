package com.example.redvings.pushupcounter;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private boolean sound_vibrate_controller=true;
	private boolean vibrate;
	private boolean sound;
	private ImageView counter_released;
	private int count=0;
	private SharedPreferences sharedpreferences;

	private ToggleButton sound_tgl;
	private boolean pushup_controller=true;
	private TextView counter_tv;
	private Vibrator vibrator;

	private SoundPool soundPool;
	private int sound1;
	private boolean loaded = false;
	private float volume;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SensorManager sensorManager;
		AudioManager audioManager;
		counter_tv = findViewById(R.id.counter_tv);
		sound_tgl=findViewById(R.id.sound_tgl);
		counter_released=findViewById(R.id.counter_released);   //imageView
		RelativeLayout relativeLayout = findViewById(R.id.relative_layout);


		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actVolume / maxVolume;

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			AudioAttributes audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();
			soundPool = new SoundPool.Builder()
					.setMaxStreams(2)
					.setAudioAttributes(audioAttributes)
					.build();
		}
		else{
			soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		}
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				loaded = true;
			}
		});
		sound1 = soundPool.load(this, R.raw.sound, 1);
		//END of soundPool

		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		sharedpreferences =getSharedPreferences("Mypref", Context.MODE_PRIVATE);

		Sensor proximitysensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if(proximitysensor==null){  //Checking Proximity sensor is available or not
			Toast.makeText(getApplicationContext(), "Required Hardware not found!!!", Toast.LENGTH_LONG).show();
			counter_tv.setText(R.string.hardware_not_found);
		}
		else {
			sensorManager.registerListener(my_proximity_SensorEventListener,proximitysensor,SensorManager.SENSOR_DELAY_FASTEST);
		}

        sound_tgl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor= sharedpreferences.edit();
				if(isChecked){ //Toggle button in on state
					editor.putBoolean("sound",true);
					editor.apply();
					sound=true;
					sound_tgl.setBackground(getDrawable(R.drawable.unmute));
				}
				else {
					editor.putBoolean("sound",false);
					editor.apply();
					sound=false;
					sound_tgl.setBackground(getDrawable(R.drawable.mute));
				}
			}
		});

			relativeLayout.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(pushup_controller) {
						counter_released.setVisibility(View.GONE);
						if (sound_vibrate_controller) // to prevent multiple sound in single touch because onTouched() method called repeatedly as long as screen touched
						{
							vibrate_sound();    //Generate a vibrate and sound alert
							sound_vibrate_controller = false;
						}
					}
					return false;
				}
			});

			relativeLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (pushup_controller) {
						counter_released.setVisibility(View.VISIBLE);   //Reseting Image in backgroung of counter  //making Visible the first Image
						count++;
						counter_tv.setText(String.format("%d", count));
						sound_vibrate_controller = true;
						pushup_controller = false;
						wait_for_next();
					}
				}
			});
		}

          private SensorEventListener my_proximity_SensorEventListener=new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent arg0) {
				if (pushup_controller) {
					if (arg0.values[0] < 5)  //near of sensor
					{
						counter_released.setVisibility(View.INVISIBLE);//Hiding the dark image
						count++;
						counter_tv.setText(String.format("%d", count));
						vibrate_sound();   //Generate a vibrate and sound alert
					} else {
						counter_released.setVisibility(View.VISIBLE);  //Showing the dark image
					}
				}
			}
			@Override
			public void onAccuracyChanged (Sensor arg0,int arg1){}
		};
		public void Reset_btn(View v)  { //Resetting counter to 0
			count=0;
			counter_tv.setText(String.format("%d", count));
			 
		}

		protected void onResume() {
			super.onResume();
			sound= sharedpreferences.getBoolean("sound", true);
			vibrate= sharedpreferences.getBoolean("counter",true );  //vibrate is misnamed to counter in Mypref(setting) file
			if(sound) {
				sound_tgl.setBackground(getDrawable(R.drawable.unmute));
			}
			else {
				sound_tgl.setBackground(getDrawable(R.drawable.mute));
			}
		}
	private void vibrate_sound()
	{
		if(vibrate)  //if setting found than vibrates
		{
			if(vibrator != null) {
				vibrator.vibrate(100);
			}
		}

		if(sound)  //if setting found true than short audio played
		{
			try
			{
				if (loaded) {
					soundPool.play(sound1, volume, volume, 1, 0, 1f);
				}
			}
			catch(Exception ex)
			{
				Toast.makeText(getApplicationContext(), ""+ex, Toast.LENGTH_LONG).show();
			}
		}
	 }
	 private void wait_for_next()
	 {
		 new Handler().postDelayed(new Runnable() {
			 @Override
			 public void run() {
				 pushup_controller=true;
			 }
		 },500);
	 }
    protected void onPause()
    {
        super.onPause();
        my_proximity_SensorEventListener=null;
		vibrator =null;
		soundPool=null;
		pushup_controller=false;
    }
}
