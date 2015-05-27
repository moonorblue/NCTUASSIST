package com.example.myactivity;

import com.example.myactivity.staticdata.Constant;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class FileNameActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String filename = null;
		String activityName = null;
		long askTime = 0;
		Intent receiveIntent = getIntent();
		if(receiveIntent != null) {
			String tempFile = receiveIntent.getStringExtra(Constant.FILE_KEY_LABEL);
			String tempActivity = receiveIntent.getStringExtra(Constant.ACTIVITY_NAME);
			askTime = receiveIntent.getLongExtra(Constant.ASKED_TIME,0);
			if(tempFile != null) {
				filename = tempFile;
			}
			if(tempActivity != null) {
				activityName = tempActivity;
			}
		}
		//pass file name to MainActivity
        Intent stateIntent = new Intent();
        stateIntent.setAction(Intent.ACTION_SEND);
        stateIntent.putExtra(Constant.FILE_KEY_LABEL, filename);
        stateIntent.putExtra(Constant.ACTIVITY_NAME, activityName);
        stateIntent.putExtra(Constant.ASKED_TIME, askTime);
        LocalBroadcastManager.getInstance(this)
              .sendBroadcast(stateIntent);
        this.finish();
	}
}
