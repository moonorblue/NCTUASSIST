package com.example.myactivity;

import com.example.myactivity.staticdata.Constant;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class SetVolume extends Activity {
	
	String Mode;	//記路現在是哪個activity
	String toSetActivity;	//欲改變音量之activity
	
	private Handler progressHandler = new Handler();
	private Long startProgressTime;
	Dialog progress_dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_volumn);
        
        //接住SetMain的intent
        Intent intent = getIntent();
        Mode = intent.getStringExtra("Mode");
        toSetActivity = intent.getStringExtra("toSetActivity");
        
        
        
        //=======================================================================       
        //seekbar音量設定區
        //======================================================================= 
    	
    	final AudioManager audioManager; //音頻
        final SeekBar SetVolumn_seekBar;
        final TextView SetVolumn_seekBarValue;

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        SetVolumn_seekBar = (SeekBar)findViewById(R.id.setting_volumn_seekBar);    
        SetVolumn_seekBarValue = (TextView)findViewById(R.id.setting_volumn_seekBarValue);
        
       
        //使用SharedPreferences讀取user偏好設定
		SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);		
		final int ring_value = settings.getInt(Constant.SPF_SettingVolume_Prefix + toSetActivity + Constant.SPF_SettingVolume_Postfix,-1);//第二個值為預設值
        final int ring_max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
       
        
        SetVolumn_seekBar.setMax(ring_max); //設定目前SeekBar之最大值
       
        //設定目前SeekBar初始值
        SetVolumn_seekBar.setProgress(ring_value);	
        SetVolumn_seekBarValue.setText("Set volume(" + String.valueOf(ring_value) + "/" + String.valueOf(ring_max) + ")");
                
        
        
        SetVolumn_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
        	@Override  
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        		//當seekbar改變時
        		SetVolumn_seekBarValue.setText("Set volume(" + String.valueOf(progress) + "/" + String.valueOf(ring_max) + ")");
	 			
 		   	}	  
 		  
        	@Override  
        	public void onStartTrackingTouch(SeekBar seekBar) {  
        		//當按下seekbar時
        	}  
 		  
        	@Override  
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		//當離開seekbar時
 			   
        		progress_dialog = new Dialog(SetVolume.this);
        		progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        		progress_dialog.setContentView(R.layout.setting_volumn_progress_dialog);
        		progress_dialog.setCanceledOnTouchOutside(false);
        		progress_dialog.setCancelable(false);
        		
        		progress_dialog.show();
        		
        		//使用SharedPreferences儲存user偏好設定
        		SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);	//0:private,此app才能用  ,1:public,所有app皆可用
        		SharedPreferences.Editor PE = settings.edit();
        		PE.putInt(Constant.SPF_SettingVolume_Prefix + toSetActivity + Constant.SPF_SettingVolume_Postfix, seekBar.getProgress());
        		PE.commit();
        		
        		
        		//若 toSetActivity 和 Mode 相同時,即時更改音量
        		if(toSetActivity.equals(Mode)){
        			audioManager.setStreamVolume(AudioManager.STREAM_RING,
        					seekBar.getProgress(), 
							0);
        		}
        		startProgressTime = System.currentTimeMillis();
        		progressHandler.post(progressTimer);
 		   	}  
 		});  
        
        
        
        //=======================================================================       
        //button回去區
        //=======================================================================
        
        final Button ApplyNow,Back;
        
        ApplyNow = (Button)findViewById(R.id.ApplyNow);
        Back = (Button)findViewById(R.id.Back);
        
        ApplyNow.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v){
	        	
	        	final AudioManager audioManager; //音頻
	        	audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
	        	
        		SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);	//0:private,此app才能用  ,1:public,所有app皆可用
        		final int ring_value = settings.getInt(Constant.SPF_SettingVolume_Prefix + toSetActivity + Constant.SPF_SettingVolume_Postfix,-1);//第二個值為預設值
        		audioManager.setStreamVolume(AudioManager.STREAM_RING,
        				ring_value, 
						0);
	        	
	        	
        		
	        	Intent intent = new Intent();
	        	intent.putExtra("Mode", toSetActivity);	//Mode改變成toSetActivity
	        	setResult(RESULT_OK, intent);
	        	finish();	        	
	        }
        });
	        
        Back.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v){
	        	
	        	
	        	Intent intent = new Intent();
	        	intent.putExtra("Mode", Mode);	//Mode不變
	        	setResult(RESULT_OK, intent);
	        	finish();
	        }
        });
        
        
        
        
    }
    
    //按下
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		
    		Intent intent = new Intent();
        	intent.putExtra("Mode", Mode);	//Mode不變
        	setResult(RESULT_OK, intent);
        	finish();
    	}
    	return true;
    }
    private Runnable progressTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startProgressTime;
			if(spentTime >= 2000) {//it takes 2 seconds
				progress_dialog.dismiss();
			}else {
				progressHandler.post(this);
			}
		}
    };
    
    @Override 
    protected void onDestroy(){
    	super.onDestroy();
    	progressHandler.removeCallbacks(progressTimer);
    }
}
