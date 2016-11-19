package com.karenpownall.android.aca.notetoself;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private boolean mSound;

    public static final int FAST = 0;
    public static final int SLOW = 1;
    public static final int NONE = 2;

    private int mAnimOption;

    int mIdBeep = -1;
    SoundPool mSp;

    Animation mAnimFlash;
    Animation mFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPrefs = getSharedPreferences("Note to Self", MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mSound = mPrefs.getBoolean("sound", true); //check boolean 1st

        //Checkboxes, sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSp = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try {
            //Create objects of the 2 required classes
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor descriptor;

            //Load our FX in memory for use
            descriptor = assetManager.openFd("beep.ogg");
            mIdBeep = mSp.load(descriptor, 0);
        } catch (IOException e) {
            //Print an error message to the console
        }
        Log.e("error", "failed to load sound files");

        CheckBox checkBoxSound = (CheckBox) findViewById(R.id.checkBoxSound);

        if (mSound){
            checkBoxSound.setChecked(true); //set as checked
        } else {
            checkBoxSound.setChecked(false); //unchecked
        }

        checkBoxSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(
                    CompoundButton buttonView, boolean isChecked)
            {
                Log.i("sound = ", "" + mSound);
                Log.i("isChecked = ", "" + isChecked);

                //if mSound is true, make it false
                //if mSound is false, make it true
                mSound = ! mSound;
                mEditor.putBoolean("sound", mSound);
                if (mSound){
                    mSp.play(mIdBeep, 1, 1, 0, 0, 1);
                }
            }
        });

        //Radio buttons, animation
        mAnimOption = mPrefs.getInt("anim option", FAST);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        //Deselect all buttons
        radioGroup.clearCheck();

        //which radio button should be selected?
        switch (mAnimOption){
            case FAST:
                radioGroup.check(R.id.radioFast);
                break;
            case SLOW:
                radioGroup.check(R.id.radioSlow);
                break;
            case NONE:
                radioGroup.check(R.id.radioNone);
                break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1){
                    switch (rb.getId()){
                        case R.id.radioFast:
                            mAnimOption = FAST;
                            break;
                        case R.id.radioSlow:
                            mAnimOption = SLOW;
                            break;
                        case R.id.radioNone:
                            mAnimOption = NONE;
                            break;
                    }
                    //End Switch Block
                    mEditor.putInt("anim option", mAnimOption);
                }
            }
        }); //end onCheckedChangeListener

    } //end onCreate

    @Override
    protected void onPause(){
        super.onPause();

        //save settings here
        mEditor.commit();
    }
}
