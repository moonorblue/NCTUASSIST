package com.example.myactivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SWLoggerService extends Service {

	private Handler handler = new Handler();
	private String lifelable;
	private boolean filesave;
	private boolean firstwrite;
	private static final String TAG = "software";
	File file;
	FileOutputStream fout;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	@Override
	public IBinder onBind(Intent i) {
		return null;
	}	
    	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		Bundle bundle = intent.getExtras();
		if(bundle.getBoolean("stop")) {
			filesave = bundle.getBoolean("filesave");
			onDestroy();
		}
		else {
			lifelable = bundle.getString("lifelable");
			openfile();
			handler.post(CreateJson);
		}
	}
	
	@Override
	public void onDestroy() {
		handler.removeCallbacks(CreateJson);		
		
		try {  
			fout.write(']'); 
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!filesave)file.delete();
	    super.onDestroy();
	}
	
	private void openfile() {
		String path = Environment.getExternalStorageDirectory().getPath();
		File dir = new File(path + "/HTCactivitylogger/Software");
		if (!dir.exists()) {
	    dir.mkdir();
		}

		try {
			TelephonyManager tM=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = tM.getDeviceId();
			Date dt = new Date();
			file = new File(path + "/HTCactivitylogger/Software/s" +sdf.format(dt)+"_"+imei+".txt");
			fout = new FileOutputStream(file);
	       
			//fout.write(new Date().toString().getBytes());
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
		}
		
		firstwrite = true;
	 }
	
	private Runnable CreateJson = new Runnable() {
        public void run() {
			
        	ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> appList = activityManager.getRunningAppProcesses();
			//Date dt = new Date();
			long dt = System.currentTimeMillis();
			
			JSONObject JsonObject = new JSONObject();
			JSONArray AppArray = new JSONArray();
			String JsonString;
			
			try {
				JsonObject.put("lifelable",lifelable);
				JsonObject.put("Version",2.1);
				JsonObject.put("time", dt);
				for (RunningAppProcessInfo i : appList)
				{
					JSONObject AppObject = new JSONObject();
					AppObject.put("name", i.processName);
					AppArray.put(AppObject);
				}
				JsonObject.put("App", AppArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (firstwrite) {
				JsonString = "[";
				firstwrite = false;
			}
			else {
				JsonString = ",";
			}
			JsonString += JsonObject.toString();
			
			try {
				fout.write(JsonString.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			handler.postDelayed(CreateJson, 1000);
        }
    };
	
}
