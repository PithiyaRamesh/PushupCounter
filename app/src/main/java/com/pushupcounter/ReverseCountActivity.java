package com.pushupcounter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ReverseCountActivity extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private boolean sound=false,vibrate;
    private int goal=0, users_entered_goal =0, total_pushups =0;
    private Button finish_goal_button;
    private ImageView Counter_released_goal_image;
    private TextView counter_tv_1;
    private boolean sound_vibrate_controller=true;
    private boolean pushup_controller=false;
    private AlertDialog ad;
    private AlertDialog ad2;
    private final Context con=this;
    private Vibrator vibrator;

    private ToggleButton sound_tgl_1;

    //For soundPool
    private SoundPool soundPool;
    private int sound1;
    private boolean loaded = false;
    private float volume;
    //End

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_count);

        Sensor proximity_sensor;
        SensorManager sensor_manager;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Counter_released_goal_image=findViewById(R.id.img_reverse_countReleased);
        finish_goal_button=findViewById(R.id.btn_reverse_finish);
        counter_tv_1=findViewById(R.id.tv_reverse_pushUpCount);
        sound_tgl_1 = findViewById(R.id.tgl_reverse_sound);

        get_goal_dialog(con);

        RelativeLayout relativeLayout_2=findViewById(R.id.relativeLayout2);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    //.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        else
        {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        sound1 = soundPool.load(this, R.raw.sound, 1);

        //END of soundPool

        sensor_manager=(SensorManager)getSystemService(SENSOR_SERVICE);

        proximity_sensor=sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(proximity_sensor==null)
        {
            Toast.makeText(getApplicationContext(), R.string.hardware_not_found, Toast.LENGTH_SHORT).show();
        }
        else
        {
            sensor_manager.registerListener(proximity_sensorEventListener ,proximity_sensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
        sound_tgl_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)    //Toggle button in on state
                {
                    SharedPreferences.Editor editor=sharedpreferences.edit();
                    editor.putBoolean("sound",true);
                    editor.apply();
                    sound=true;
                    sound_tgl_1.setBackground(getDrawable(R.drawable.unmute));
                    //onResume();
                }
                else
                {
                    SharedPreferences.Editor editor=sharedpreferences.edit();
                    editor.putBoolean("sound",false);
                    editor.apply();
                    sound=false;
                    sound_tgl_1.setBackground(getDrawable(R.drawable.mute));
                    //onResume();
                }
            }
        });
       relativeLayout_2.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               if (pushup_controller) {
                   Counter_released_goal_image.setVisibility(View.GONE);
                   if (sound_vibrate_controller) //to control the sound on touch to prevent multiple sound in single touch
                   {
                       vibrate_sound();    //Generate a vibrate and sound alert
                       sound_vibrate_controller = false;
                   }
               }
               return false;
           }
       });
        relativeLayout_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pushup_controller) {
                    Counter_released_goal_image.setVisibility(View.VISIBLE);   //Reseting Image in backgroung of counter  //making Visible the first Image
                    if(goal>1) {
                        goal--;
                        counter_tv_1.setText(String.format("%d", goal));
                        sound_vibrate_controller = true;
                        pushup_controller = false;
                        wait_for_next();
                    }
                    else
                    {
                        counter_tv_1.setText(""+0);
                        goal=0;
                        show_result(con);//showing result
                    }
                }
            }
        });
    }//End of onCreate()

    private final SensorEventListener proximity_sensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent arg0) {
            if (pushup_controller) {
            try {
                    if (arg0.values[0] < 5)  //near of sensor
                    {
                        Counter_released_goal_image.setVisibility(View.INVISIBLE);//Hiding the dark image
                        if (goal > 1) {
                            goal--;
                            counter_tv_1.setText(String.format("%d", goal));
                            vibrate_sound();   //Generate a vibrate and sound alert
                        } else {
                            vibrate_sound();
                            counter_tv_1.setText("" + 0);
                            goal = 0;
                            show_result(con);//Showing Results
                        }
                    } else {
                        Counter_released_goal_image.setVisibility(View.VISIBLE);  //Showing the dark image
                    }
                }
            catch(Exception ex)
                {
                    Toast.makeText(getApplicationContext(), "" + ex, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    protected void onResume()
    {
        super.onResume();
        sharedpreferences =getSharedPreferences("Mypref", Context.MODE_PRIVATE);
        sound=sharedpreferences.getBoolean("sound", true);
        vibrate=sharedpreferences.getBoolean("counter",true );  //vibrate is misnamed to counter in Mypref(setting) file
        if(sound)
        {
            sound_tgl_1.setBackground(getDrawable(R.drawable.unmute));
        }
        else
        {
            sound_tgl_1.setBackground(getDrawable(R.drawable.mute));
        }
    }
    public void finish_Goal(View v)
    {
        show_result(con);  //Showing Results
    }

    private void vibrate_sound()
    {
        if(vibrate)  //if setting found than vibrates
        {
            if(vibrator!=null) {
                vibrator.vibrate(200);
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
    private void show_result(Context con)
    {
        if(!((Activity)con).isFinishing()) //To Avoid ERROR android.view.WindowManager$BadToken Exception: Unable to add window -- token android.os.BinderProxy@47923f1 is not valid; is your acivity running?
        {
            pushup_controller=false;
            AlertDialog.Builder adb = new AlertDialog.Builder(this.con);
            ViewGroup vg = findViewById(android.R.id.content);
            View dv = LayoutInflater.from(this.con).inflate(R.layout.dialog_onresult, vg, false);
            if (goal == 0) {
                Button continue_btn;
                continue_btn = dv.findViewById(R.id.continue_button);
                continue_btn.setText("More Pushups");
                continue_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.cancel();
                        ad2.show();
                    }
                });
            }
            adb.setView(dv);
            TextView pushup_left_tv = dv.findViewById(R.id.pushups_left_tv);
            TextView push_done_tv = dv.findViewById(R.id.pushups_done_tv);
            TextView total_pushups_tv=dv.findViewById(R.id.total_pushups_tv);
            int done = users_entered_goal - goal;
            total_pushups += users_entered_goal - goal;
            String total_pushups_str=getString(R.string.total_pushups);
            total_pushups_tv.setText(total_pushups_str+total_pushups);
            String pushups_done=getString(R.string.dialog_result_pushups_done);
            push_done_tv.setText(pushups_done+done);
            String pushups_left=getString(R.string.dialog_result_pushups_left);
            pushup_left_tv.setText(pushups_left+goal);
            ad = adb.create();
            ad.setCancelable(false);
            ad.show();
        }
    }
    public void Continue(View v)   //Result Dialog button
    {
        pushup_controller=true;
        ad.cancel();

    }
    public void Home(View v)   //Result Dialog button
    {
        ad.dismiss();
        finish();
    }
    private void get_goal_dialog(Context con2)
    {
        AlertDialog.Builder adb2 = new AlertDialog.Builder(con2);
        ViewGroup vg = findViewById(android.R.id.content);
        View dv = LayoutInflater.from(con2).inflate(R.layout.dialog_setgoal, vg, false);
        final EditText get_goal_edt =dv.findViewById(R.id.edt_dialog_setGoal);
        Button set_goal_button =dv.findViewById(R.id.btn_dialog_setGoal);
        adb2.setView(dv);
        ad2 = adb2.create();
        ad2.setCancelable(false);
        ad2.show();

        set_goal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    goal=Integer.parseInt(get_goal_edt.getText().toString());
                   if(goal>0) {
                       pushup_controller=true;
                        users_entered_goal = goal;
                        counter_tv_1.setText(String.valueOf(goal));   //or (""+gaol);
                        ad2.cancel();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Please Enter Number Greater Than 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"Please Enter Number Greater Than 0",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }
    protected void onPause()
    {
        super.onPause();
        pushup_controller=false;
        soundPool=null;
        if(ad!=null)
        {
            ad.dismiss();
        }
    }
    protected void onDestroy()
    {
        super.onDestroy();
        if(ad!=null)
        {
            ad.dismiss();
        }
    }

}
