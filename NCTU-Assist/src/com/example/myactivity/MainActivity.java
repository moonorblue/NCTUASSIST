package com.example.myactivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.myactivity.staticdata.Constant;
import com.example.myactivity.staticdata.SiteUrl;
import com.parse.Parse;
import com.parse.ParseObject;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener,OnClickListener{
	int notifyId = 99;
	Bundle bundle = new Bundle();
	AQuery aQuery;
	public final static String IN_MAIN = "IN_MAIN";
	//debug
	int askTime = 0;
	int put_time = 0;
	String imei;
	
	//main layout
	Button btnStart;
	ImageView frequency_pie;
	private RadioGroup freqRadioGroup;
	Button setting_dining,setting_entertainment,setting_shopping;
    Button setting_sporting,setting_transportation,setting_working;
	
	//dialog layout
	Button button_past,button_now,button_future,button_no;
	TextView content_tv;
	Dialog qaDialog;
	AlertDialog.Builder QA_dialog;
	Dialog scDialog;
	
	//activity variable
	String[] question;	//the string array of receiving question and selection
	String[] activityNormal = {"Transportation","Shopping","Dining","Working","Sporting","Entertainment"};
	String activityName, dialogTitle;
	String activityReceived;
	String resultCode = null;
	JSONArray miningModel;
	private static final String PackageName = "com.example.myactivity";
	String lastFileName = null;
	String retrieveFile;
	ArrayList<PredictActivity> predictActivities;
	String Mode="Default";
	
	//select Timer
	private Spinner spinner;
	private String[] list = {"10 min","5 min","3 min"};
	private ArrayAdapter<String> listAdapter;
	private Context mContext;
	private int min;
	private int essentialFreqTime = 60;
	private int freq_min = essentialFreqTime;
	
	//Timer
	private Handler sensorHandler = new Handler();
	private Handler requestHandler = new Handler();
	private Handler assistHandler = new Handler();
	private Long startSensorTime;
	private Long startRequestTime;
	private Long startAssistTime;
	private int requestNum = 1;
	
	//dialog handling
	private Handler dialogHandler = new Handler();

	
	//user location variable
	private LocationManager lm;
	
	//app state variable
	private String state = Constant.LOG_STATE;
	
	//variables written to parse.com
	String activityPredictToParse;
	String activityChoseToParse;
	String userIdToParse;
	long answeredTimeToParse;
	long askedTimeToParse;
	long waitPeriodToParse;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		mContext = this.getApplicationContext();
		findSpinner();
		findViews();
		setMainListener();
		Parse.initialize(this, "x2WX86qsaH5DAf0X3Tbsbd74YbfePFlIIVo63REU", "u9o8HvjIsTdKDa9Q6U4ilEwrY29wMoE6y28lYMYT");
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		userIdToParse = imei;
		resetVolume();
		
		aQuery = new AQuery(this);
		lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		//use BroadcastReceiver to change state
		IntentFilter stateFilter = new IntentFilter(Intent.ACTION_SENDTO);
		IntentFilter fileNameFilter = new IntentFilter(Intent.ACTION_SEND);
		StateAndFileNameReceiver stateAndFileNameReceiver = new StateAndFileNameReceiver();
	    LocalBroadcastManager.getInstance(this).registerReceiver(stateAndFileNameReceiver, stateFilter);
	    LocalBroadcastManager.getInstance(this).registerReceiver(stateAndFileNameReceiver, fileNameFilter);
  
//		getPredictAct("h2014-07-30 16-58-06_355026051312078");
		//getModel(SiteUrl.GENERAL_USER);
		/*decideActivityName("default");
		//ask QA after logging sensor data and decide activity
		findDialogView();
		setDialogListener();
		qaDialog.show();
		sendNotification();*/
		
		/*//================set timmer to stop sending request==================//
		requestHandler.postDelayed(requestTimer, 1000);
		Log.d("Timer", "request timer start");
		startRequestTime = System.currentTimeMillis();*/
		
		/*Location location = getMyLocation();
		if(location != null) {
			showMapSearch(location, "restaurant");
		}else {
			Toast.makeText(this.getApplicationContext(),"there is no location", Toast.LENGTH_LONG).show();
		}*/
	}
	
	@Override 
    protected void onDestroy(){
    	super.onDestroy();
    	Intent intent = new Intent(MainActivity.this, LoggerService.class);
    	Intent intent2 = new Intent(MainActivity.this, SWLoggerService.class);
		stopService(intent);
		stopService(intent2);
		Intent intentUpload = new Intent(MainActivity.this, UploadService.class);
		stopService(intentUpload);
		sensorHandler.removeCallbacks(senserTimer);
		requestHandler.removeCallbacks(requestTimer);
		assistHandler.removeCallbacks(assistTimer);
		dialogHandler.removeCallbacks(dialogTimer);
		Log.i(IN_MAIN, "Main onDestroy");
	}
	@Override 
    protected void onPause(){
		super.onPause();
		//prevent QA dialog can not be dismiss
		if(qaDialog != null) {
			qaDialog.dismiss();
		}
		if(scDialog != null) {
			scDialog.dismiss();
		}
	}
	
	public void findSpinner() {
        spinner = (Spinner)findViewById(R.id.spinner_time);

        listAdapter = new ArrayAdapter<String>(this, R.layout.spinner_textview, list);
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(listAdapter);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
           	    switch(position) {
           	 	case 0:
           	 		min = 10;
           	 		break;
           	 	case 1:
           	 		min = 5;
           	 		break;
           	 	case 2:
           	 		min = 3;
           	 		break;
           	    }
           	    if(min == 1) {
           	    	Toast.makeText(mContext, "You select "+min+" minute!", Toast.LENGTH_SHORT).show();
           	    }else {
           	    	Toast.makeText(mContext, "You select "+min+" minutes!", Toast.LENGTH_SHORT).show();
           	    }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
	
	public void decideActivityName(String activityFlagName) {
		activityName = "default";
        for(int i=0;i<6;i++){
        	if(activityFlagName.equals(activityNormal[i])){
        		activityName = activityFlagName;
        	}
        }
        if(activityName.equals("default")) {
        	//get Activity By Time
        	Calendar calendar = new GregorianCalendar();  
    		int hour =calendar.get(Calendar.HOUR_OF_DAY);
    		if(hour>=8 && hour<12)
    			activityName = "Transportation";
    		else if(hour>=12 && hour<17)
    			activityName = "Working";
    		else if(hour>=17 && hour<20)
    			activityName = "Dining";
    		else if(hour>=20 && hour<24)
    			activityName = "Shopping";
    		else if(hour>=24 || hour<8)
    			activityName = "Entertainment";
        }
	}
	public void getQuestion(String activityResource) {
		//get string array in xml by variable name
       	int resourceId = getResources().getIdentifier(activityResource, "array", PackageName);
       	question = getResources().getStringArray(resourceId);
	}
	public void findDialogView(String activityResource) {
		getQuestion(activityResource);
       	
       	qaDialog = new Dialog(MainActivity.this);
       	qaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       	qaDialog.setContentView(R.layout.dialog_main);
       	//qaDialog.setTitle(activityName);
       	
       	content_tv = (TextView)qaDialog.findViewById(R.id.content_tv);
        button_past = (Button)qaDialog.findViewById(R.id.button_past);
        button_now = (Button)qaDialog.findViewById(R.id.button_now);
        button_future = (Button)qaDialog.findViewById(R.id.button_future);
        button_no = (Button)qaDialog.findViewById(R.id.button_no);
                
        //set text
        content_tv.setText(question[0]);
        button_past.setText(question[1]);
        button_now.setText(question[2]);
        button_future.setText(question[3]);
        button_no.setText(question[4]);
                
        //set graph should be lower-case word
        String lowerActivityName = activityResource.toLowerCase(Locale.ENGLISH);
               
        int btn1ID = getResources().getIdentifier(lowerActivityName + "_btn1", "drawable", PackageName);
        button_past.setCompoundDrawablesWithIntrinsicBounds(0,btn1ID,0,0);
                
        int btn2ID = getResources().getIdentifier(lowerActivityName + "_btn2", "drawable", PackageName);
        button_now.setCompoundDrawablesWithIntrinsicBounds(0,btn2ID,0,0);
                
        int btn3ID = getResources().getIdentifier(lowerActivityName + "_btn3", "drawable", PackageName);
        button_future.setCompoundDrawablesWithIntrinsicBounds(0,btn3ID,0,0);
        
        int btn4ID = getResources().getIdentifier(lowerActivityName + "_btn4", "drawable", PackageName);
        button_no.setCompoundDrawablesWithIntrinsicBounds(0,btn4ID,0,0);
	}
	public void setDialogListener() {
		button_past.setOnClickListener(this);
		button_now.setOnClickListener(this);
		button_future.setOnClickListener(this);
		button_no.setOnClickListener(this);
	}
	public void setMainListener() {
		btnStart.setOnClickListener(this);
		freqRadioGroup.setOnCheckedChangeListener(this);
		setting_dining.setOnClickListener(this);
		setting_entertainment.setOnClickListener(this);
		setting_shopping.setOnClickListener(this);
		setting_sporting.setOnClickListener(this);
		setting_transportation.setOnClickListener(this);
		setting_working.setOnClickListener(this);
	}
	
	public void findViews() {
		frequency_pie = (ImageView)findViewById(R.id.iv_pie);
		btnStart = (Button)findViewById(R.id.start);
		freqRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_ask_freq);
		setting_dining = (Button)findViewById(R.id.setting_dining);        
        setting_entertainment = (Button)findViewById(R.id.setting_entertainment);
        setting_shopping = (Button)findViewById(R.id.setting_shopping);
        setting_sporting = (Button)findViewById(R.id.setting_sporting);
        setting_transportation = (Button)findViewById(R.id.setting_transportation);
        setting_working = (Button)findViewById(R.id.setting_working);
	}
	private Runnable requestTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startRequestTime;
			Log.i(IN_MAIN, "request timer running");
			if(spentTime >= requestNum*60*1000) {//request every minute
				//Toast.makeText(mContext, "request num:" +requestNum, Toast.LENGTH_LONG).show();
				requestNum++;
			}
		}
	};
	private Runnable dialogTimer = new Runnable() {
		@Override
		public void run() {
			//check dialog is not null and the main Activity is living
			if(!isFinishing()) {
				/*setDialogListener();
				qaDialog.setCanceledOnTouchOutside(false);
				qaDialog.setCancelable(false);
				qaDialog.show();*/
				showDialog(activityReceived);
			}else {
				dialogHandler.post(this);
			}
		}
	};
	private Runnable assistTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startAssistTime;
			Calendar calendar = new GregorianCalendar();  
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if( ((spentTime >= 1000*60*freq_min) && (hour>=9 && hour<=21)) || askTime == 0) {
			//if( ((spentTime >= 1000*60*freq_min) || askTime == 0)) {
				askTime++;
				startLogSensor();
				startAssistTime = System.currentTimeMillis();
			}
			assistHandler.postDelayed(this, 1000);
		}
	};
	private Runnable senserTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startSensorTime;
			//Log.i("Timer", "sensor timer running");
			 
			if (spentTime >= min*60*1000 && state.equals(Constant.LOG_STATE)){//log state to upload state
				/*//send debugging msg to parse
				ParseObject stateObject = new ParseObject("Debug");
				put_time++;
				stateObject.put("put_time",String.valueOf(put_time));
				stateObject.put("state",state);
				stateObject.put("IMEI",imei);
				stateObject.put("ask_time", String.valueOf(askTime));
				stateObject.saveInBackground();*/
				
				state = Constant.IN_PROGRESS_STATE;
				
				/*//send debugging msg to parse
				stateObject = new ParseObject("Debug");
				put_time++;
				stateObject.put("put_time",String.valueOf(put_time));
				stateObject.put("state",state);
				stateObject.put("IMEI",imei);
				stateObject.put("ask_time", String.valueOf(askTime));
				stateObject.saveInBackground();*/
				
				//stop logging sensor data service
				Intent intent = new Intent(MainActivity.this, LoggerService.class);
				Intent intent2 = new Intent(MainActivity.this, SWLoggerService.class);
	            Bundle bundle = new Bundle();
				bundle.putBoolean("stop",true);
				bundle.putBoolean("filesave",true);
				intent.putExtras(bundle);
				intent2.putExtras(bundle);
				startService(intent);
				startService(intent2);
				
				lastFileName = getFileName();
				Log.i(IN_MAIN,"log");
				sensorHandler.postDelayed(this, 1000);
			}else if (spentTime >= min*60*1000 && state.equals(Constant.UPLOAD_STATE)){//upload state to predict state
				state = Constant.IN_PROGRESS_STATE;
				
				/*//send debugging msg to parse
				ParseObject stateObject = new ParseObject("Debug");
				put_time++;
				stateObject.put("put_time",String.valueOf(put_time));
				stateObject.put("state",state);
				stateObject.put("IMEI",imei);
				stateObject.put("ask_time", String.valueOf(askTime));
				stateObject.saveInBackground();*/
				
				Intent intent = new Intent(MainActivity.this, UploadService.class);
				startService(intent);
				Log.i(IN_MAIN,"upload");
				sensorHandler.postDelayed(this, 1000);
			}else if (spentTime >= min*60*1000 && state.equals(Constant.PREDICT_STATE)){//predict state to display state
				state = Constant.IN_PROGRESS_STATE;
				
				/*//send debugging msg to parse
				ParseObject stateObject = new ParseObject("Debug");
				put_time++;
				stateObject.put("put_time",String.valueOf(put_time));
				stateObject.put("state",state);
				stateObject.put("IMEI",imei);
				stateObject.put("ask_time", String.valueOf(askTime));
				stateObject.saveInBackground();*/
				
				//get prediction from server
				getPredictAct(lastFileName);
				//getPredictAct("h2014-07-30 16-58-06_355026051312078");
				Log.i(IN_MAIN,"predict");
				sensorHandler.postDelayed(this, 1000);
			}else if (spentTime >= min*60*1000 && state.equals(Constant.DISPLAY_STATE)){//display state to log state(reset)
				if(!predictActivities.isEmpty()) {
					String nextActivity = predictActivities.get(0).getActivity();
					if(nextActivity == null) {
						//error result and get activity by time
						decideActivityName("default");
					}else if(nextActivity.equals("error")) {
						//error result and get activity by time
						decideActivityName("default");
					}else {
						//the first prediction activity is the most possible activity
						decideActivityName(nextActivity);
					}
					//ask QA after logging sensor data and decide activity
					sendNotification();
					Log.i(IN_MAIN,"display");
				}
				state = Constant.LOG_STATE;
			}else {
				sensorHandler.postDelayed(this, 1000);
			}
		}
	};
	public void sendNotification() {
		Notification notify = null;
		//notifications not to overlap each other
		notifyId = (notifyId+1) % 100;
		Intent notificationIntent = new Intent(mContext, FileNameActivity.class);
		//avoid that clicking on a Notification calls onCreate()
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		long askTime = System.currentTimeMillis();
		notificationIntent.putExtra(Constant.FILE_KEY_LABEL, lastFileName);
		notificationIntent.putExtra(Constant.ACTIVITY_NAME, activityName);
		notificationIntent.putExtra(Constant.ASKED_TIME, askTime);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notifyId,
	            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		getQuestion(activityName);
		if (Build.VERSION.SDK_INT >= 11) {
			notify = newNotification(mContext,pendingIntent,activityName,question[0]);
		}else {
			notify = oldNotification(mContext, pendingIntent,activityName,question[0]);
		}
		notify.defaults |= Notification.DEFAULT_SOUND;
		notify.defaults |= Notification.DEFAULT_VIBRATE;
		WakeLock screenOn = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
		screenOn.acquire();
		screenOn.release();
		NotificationManager notificationManager =
				(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notifyId, notify);
	}
	
	@SuppressWarnings("deprecation")
	private Notification oldNotification(
			Context context, PendingIntent pi,
			String title, String msg) {
		String lowerActivityName = title.toLowerCase(Locale.ENGLISH);
		int smallIcon = getResources().getIdentifier(lowerActivityName, "drawable", PackageName);
		Notification notify = new Notification(
				smallIcon, title,
			System.currentTimeMillis());
		notify.setLatestEventInfo(context, title, msg, pi);
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		return notify;
	}
	
	@SuppressLint("NewApi")
	private Notification newNotification(
			Context context,PendingIntent pi,
			String title, String msg) {
		String lowerActivityName = title.toLowerCase(Locale.ENGLISH);
		int smallIcon = getResources().getIdentifier(lowerActivityName, "drawable", PackageName);
		Notification.Builder builder =
				new Notification.Builder(context);
		builder.setSmallIcon(smallIcon);
		builder.setContentTitle(title);
		builder.setContentText(msg);
		builder.setContentIntent(pi);
		builder.setTicker(msg);
		builder.setWhen(System.currentTimeMillis());
		Notification notify = null;
		if (Build.VERSION.SDK_INT >= 16) {
			notify = builder.build();
		}else {
			notify = builder.getNotification();
		}
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		return notify;		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void writeUserDataToParse() {
		ParseObject userObject = new ParseObject(Constant.tableUserActivities);
		userObject.put("ActivityPredict",activityPredictToParse);
		userObject.put("ActivityChose",activityChoseToParse);
		userObject.put("UserID",userIdToParse);
		userObject.put("AnsweredTime", String.valueOf(answeredTimeToParse));
		userObject.put("AskedTime", String.valueOf(askedTimeToParse));
		userObject.put("WaitPeriod", String.valueOf(waitPeriodToParse));
		userObject.saveInBackground();
	}
	public void setLabelToServer(String fileName, String label) {
		activityChoseToParse = label;
		aQuery.ajax(SiteUrl.SET_LABEL_URL+"filename="+fileName+"&"+"label="+label, String.class, new AjaxCallback<String>(){
            @Override
            public void callback(String url, String queryResult, AjaxStatus status) {
            	answeredTimeToParse = System.currentTimeMillis();
            	waitPeriodToParse = answeredTimeToParse - askedTimeToParse;
            	writeUserDataToParse();
            	if(queryResult != null){
            		//successful ajax call and server returns "done" or "error"
            		resultCode = queryResult;
            	}else{	
        			//ajax error, show error code
            		resultCode = Constant.REQUEST_ERROR;
        			Toast.makeText(aQuery.getContext(), "Error: it is nternet problem!", Toast.LENGTH_LONG).show();
            		Log.i(IN_MAIN, "Error:" + status.getCode());
                }
            }
		});
	}
	public void setLabel(String fileName, String label) {
		if(label == null) {
			//can not decide the label and start an "Activity" to ask user to choose label
			Intent intent = new Intent();
			intent.setClass(MainActivity.this,ChooseLabelActivity.class);
			startActivityForResult(intent, Constant.ACTIVITY_SET_LABEL);
		}else {
			//if label is not null
			//set label to server directly
			setLabelToServer(fileName,label);
		}
	}
	@Override
	protected void onActivityResult(int requestCode,int resultCode, Intent intent) {
		if (intent == null)
			return;
		
		super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode){
        case 1:	
            Mode = intent.getStringExtra("Mode"); 
        	setModeStart(Mode);
        	break;
        }
		/*String label = "noAnswer";
		if (intent == null)
			return;
		
		super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
		case Constant.ACTIVITY_SET_LABEL:
			label = intent.getStringExtra(Constant.KEY_LABEL);
			break;
		}
		String filename = lastFileName;
		if(retrieveFile != null) {
			filename = retrieveFile;
		}
		setLabelToServer(filename,label);
		moveTaskToBack(true);*/
	}
	public void getModel(final String userName) {
    	aQuery.ajax(SiteUrl.GET_MODEL_URL+SiteUrl.QUOTE+userName+SiteUrl.QUOTE, JSONArray.class, new AjaxCallback<JSONArray>(){
            @Override
            public void callback(String url, JSONArray model, AjaxStatus status) {
            	if(model != null){	
            		//successful ajax call
            		miningModel = model;
            	}else{	
        			//ajax error, show error code
        			Toast.makeText(aQuery.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
                }
            }
		});
    }
	public void getPredictAct(final String fileName) {
		//clear the prediction activities
		predictActivities = new ArrayList<PredictActivity>();
		//request prediction activities from server
    	aQuery.ajax(SiteUrl.GET_PREDICT_ACTIVITY+"filename="+fileName, JSONObject.class, new AjaxCallback<JSONObject>(){
            @Override
            public void callback(String url, JSONObject prediction, AjaxStatus status) {
            	if(prediction != null){
            		//successful ajax call
                	Iterator<?> keys = prediction.keys();
                	for(int i=0;i<6 && keys.hasNext();i++) {
                		String activity = keys.next().toString();
                		String prob = prediction.optString(activity);
                		predictActivities.add(new PredictActivity(activity, prob));
                		int size = predictActivities.size();
                		//predictActivitys[i] = new PredictActivity(activity, prob);
                	}
            	}else{
        			//ajax error, show error code
        			//Toast.makeText(aQuery.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
            		Log.i(IN_MAIN, "Error:" + status.getCode());
                }
            	//change to DISPLAY_STATE
            	state = Constant.DISPLAY_STATE;
            	
            	/*//send debugging msg to parse
            	ParseObject stateObject = new ParseObject("Debug");
				put_time++;
				stateObject.put("put_time",String.valueOf(put_time));
				stateObject.put("state",state);
				stateObject.put("IMEI",imei);
				stateObject.put("ask_time", String.valueOf(askTime));
				stateObject.saveInBackground();*/
            }
		});
    }
	public Location getMyLocation() {
		Location location = lm.getLastKnownLocation(
				LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = lm.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
		}
		return location;
	}
	
	public void showMapSearch(Location center, String query) {
		String latitude = Double.toString(center.getLatitude());
		String longtitude = Double.toString(center.getLongitude());
		Uri uri = Uri.parse("geo:"+latitude+","+longtitude+"?q="+query);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
	}
	public void startAssist() {
		//assistHandler.postDelayed(assistTimer, 1000*60*freq_min);
		assistHandler.postDelayed(assistTimer, 1000);
		startAssistTime = System.currentTimeMillis();
	}
	public void startLogSensor() {
		//start logging data
		Intent intent = new Intent(MainActivity.this, LoggerService.class);
		Intent intent2 = new Intent(MainActivity.this, SWLoggerService.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("stop",false);
	    bundle.putString("lifelabel","Working");
	    intent.putExtras(bundle);
	    intent2.putExtras(bundle);
		startService(intent);
		startService(intent2);
		
		//================set timmer to stop logger==================//
		sensorHandler.postDelayed(senserTimer, 1000);
	    Log.d(IN_MAIN, "sensor timer start");
	    startSensorTime = System.currentTimeMillis();
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		String answer = null;
		Intent intent;
		switch(id) {
		case R.id.setting_dining:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Dining");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.setting_entertainment:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Entertainment");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.setting_shopping:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Shopping");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.setting_sporting:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Sporting");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.setting_transportation:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Transportation");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.setting_working:
			//呼叫SetVolume
			intent = new Intent();
    		intent.setClass(MainActivity.this, SetVolume.class);
    		intent.putExtra("toSetActivity", "Working");	//欲set的activity
    		intent.putExtra("Mode", Mode);
    		startActivityForResult(intent, 1);
			break;
		case R.id.start:
			//decideActivityName("default");
			startAssist();
			moveTaskToBack(true);
			break;
		case R.id.button_past:
			//qaDialog.dismiss();
			qaDialog.cancel();
			//Toast.makeText(this, "I get the answer: "+answer, Toast.LENGTH_LONG).show();
			break;
		case R.id.button_now:
			//qaDialog.dismiss();
			qaDialog.cancel();
			answer = activityName;
			moveTaskToBack(true);
			break;
		case R.id.button_future:
			//qaDialog.dismiss();
			qaDialog.cancel();
			//Toast.makeText(this, "I get the answer: "+answer, Toast.LENGTH_LONG).show();
			//help user to find restaurant or bus station
			Location location = getMyLocation();
			if(location != null) {
				//notice! compare string with upper-case words
				if(activityName.equals("Dining")) {
					showMapSearch(location, "restaurant");
				}else if(activityName.equals("Transportation")) {
					showMapSearch(location, "火車站");
				}
			}else {
				Toast.makeText(this.getApplicationContext(),"there is no location", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.button_no:
			//qaDialog.dismiss();
			qaDialog.cancel();
			//Toast.makeText(this, "I get the answer: "+answer, Toast.LENGTH_LONG).show();
			break;
		}
		/*if(id != R.id.start) {
			//press button in QA dialog to set label
			String filename = lastFileName;
			if(retrieveFile != null) {
				filename = retrieveFile;
			}
			setLabel(filename, answer);
		}*/
	}
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		case R.id.radio_seldom:
			//ask QA at three-hour intervals
			frequency_pie.setImageDrawable(getResources().getDrawable(R.drawable.seldom_pie));
			freq_min = essentialFreqTime * 3;
			break;
		case R.id.radio_often:
			//ask QA at two-hour intervals
			frequency_pie.setImageDrawable(getResources().getDrawable(R.drawable.often_pie));
			freq_min = essentialFreqTime * 2;
			break;
		case R.id.radio_usually:
			//ask QA every hour
			frequency_pie.setImageDrawable(getResources().getDrawable(R.drawable.usually_pie));
			//test!
			freq_min = essentialFreqTime;
			//Toast.makeText(this, "freq_min: "+freq_min, Toast.LENGTH_LONG).show();
			Log.i(IN_MAIN, "freq_min: "+freq_min);
			break;
		}	
	}
	public String getFileName() {
		 String LocalDirectory = Environment.getExternalStorageDirectory().getPath()+"/HTCactivitylogger";
		 String fileName = null;
		 
         //Hardware
         File dir = new File(LocalDirectory+"/Hardware/");
         String[] child = dir.list();
         
         if(child == null){
       	  Log.i(IN_MAIN, "directory not have failed" );
         }else{
             for(int i = 0; i < child.length; i++){
            	 //get the last file name
                 fileName = child[i];
             }
         }
         //remove .txt from file name
         fileName = fileName.replace(".txt", "");
         return fileName;
	}
	
	public void resetVolume() {
		//=======================================================================       
        //reset SharedPreferences的初值趨
        //======================================================================= 
        
        AudioManager initial_audioManager;
        initial_audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        int initial_ring_max = initial_audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        
        SharedPreferences initial_settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);
        SharedPreferences.Editor PE = initial_settings.edit();
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Dining"+Constant.SPF_SettingVolume_Postfix, initial_ring_max/3);
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Entertainment"+Constant.SPF_SettingVolume_Postfix, initial_ring_max/3);
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Shopping"+Constant.SPF_SettingVolume_Postfix, initial_ring_max);
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Sporting"+Constant.SPF_SettingVolume_Postfix, initial_ring_max);
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Transportation"+Constant.SPF_SettingVolume_Postfix, initial_ring_max);
		PE.putInt(Constant.SPF_SettingVolume_Prefix+"Working"+Constant.SPF_SettingVolume_Postfix, 0);
		PE.commit();
	}
	
    public void setModeStart(String toStartActivity){	
    	
    	final ImageView dining_start,entertainment_start,shopping_start;
    	final ImageView sporting_start,transportation_start,working_start;
    	
    	dining_start = (ImageView)findViewById(R.id.dining_start);
    	entertainment_start = (ImageView)findViewById(R.id.entertainment_start);
    	shopping_start = (ImageView)findViewById(R.id.shopping_start);
    	sporting_start = (ImageView)findViewById(R.id.sporting_start);
    	transportation_start = (ImageView)findViewById(R.id.transportation_start);
    	working_start = (ImageView)findViewById(R.id.working_start);
    	
    	dining_start.setVisibility(View.INVISIBLE);
    	entertainment_start.setVisibility(View.INVISIBLE);
    	shopping_start.setVisibility(View.INVISIBLE);
    	sporting_start.setVisibility(View.INVISIBLE);
    	transportation_start.setVisibility(View.INVISIBLE);
    	working_start.setVisibility(View.INVISIBLE);
    	
    	//toStartActivity 為欲start之activity
    	switch(toStartActivity) {
    	case "Dining":
    		dining_start.setVisibility(View.VISIBLE);
    		break;
    	case "Entertainment":
    		entertainment_start.setVisibility(View.VISIBLE);
    		break;
    	case "Shopping":
    		shopping_start.setVisibility(View.VISIBLE);
    		break;
    	case "Sporting":
    		sporting_start.setVisibility(View.VISIBLE);
    		break;
    	case "Transportation":
    		transportation_start.setVisibility(View.VISIBLE);
    		break;
    	case "Working":
    		working_start.setVisibility(View.VISIBLE);
    		break;
    	}
    }
	
	public class StateAndFileNameReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction() == Intent.ACTION_SENDTO) {
        		//receive state from services
        		String toState = intent.getStringExtra(Constant.STATE);
        		if(toState != null) {
        			state = toState;
	        		/*//send debugging msg to parse
	        		ParseObject stateObject = new ParseObject("Debug");
	        		put_time++;
	        		stateObject.put("put_time",String.valueOf(put_time));
					stateObject.put("state",state);
					stateObject.put("IMEI",imei);
					stateObject.put("ask_time", String.valueOf(askTime));
					stateObject.saveInBackground(); */
        		}
        	}else if(intent.getAction() == Intent.ACTION_SEND) {
        		//receive file name and activity name from FileNameActivity
        		String fileName = intent.getStringExtra(Constant.FILE_KEY_LABEL);
        		activityReceived = intent.getStringExtra(Constant.ACTIVITY_NAME);
        		askedTimeToParse = intent.getLongExtra(Constant.ASKED_TIME, 0);
				if(fileName != null) {
					retrieveFile = fileName;
				}
				if(activityReceived == null) {
					activityReceived = activityName;
				}
				activityPredictToParse = activityReceived;
				//findDialogView(activityReceived);
				//show dialog in handler
				dialogHandler.post(dialogTimer);
        	}
        }
    }
	
	/*
	 * This showDialog(String ActivityPredict) function is written by pk74323jacky(undergraduate intern students)
	 */
	public void showDialog(final String ActivityPredict) {
    	
    	//(ActivityPredict可以在上面就給，這是為了我SHOW給大家看方變而已)
    	
    	//=======================================================================       
        //QA dialog 的 layout 設定區
        //=======================================================================
        QA_dialog = new AlertDialog.Builder(MainActivity.this);
        
        if(ActivityPredict.equals("Working")){
        	QA_dialog.setTitle("Are you in Working mode?");
        }
        else if(ActivityPredict.equals("Dining")){
        	QA_dialog.setTitle("Are you in Dining mode?");
        }
        else if(ActivityPredict.equals("Entertainment")){
        	QA_dialog.setTitle("Are you in Entertainment mode?");
        }
        else if(ActivityPredict.equals("Sporting")){
        	QA_dialog.setTitle("Are you in Sporting mode?");
        }
        else if(ActivityPredict.equals("Shopping")){
        	QA_dialog.setTitle("Are you in Shopping mode?");
        }
        else if(ActivityPredict.equals("Transportation")){
        	QA_dialog.setTitle("Are you in Transportation mode?");
        }
        
        //取得自定義的View
        LayoutInflater inflater = getLayoutInflater();
        View QA_layout = inflater.inflate(R.layout.qa_dialog_main , null);
        QA_dialog.setView(QA_layout);
        
        final AudioManager audioManager; //音頻
        final SeekBar QA_seekBar;
        final TextView QA_seekBarValue;

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        QA_seekBar = (SeekBar)QA_layout.findViewById(R.id.qa_dialog_seekBar);    
        QA_seekBarValue = (TextView)QA_layout.findViewById(R.id.qa_dialog_seekBarValue);
        
        //使用SharedPreferences讀取user偏好設定
		SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);
			final int ring_value = settings.getInt(Constant.SPF_SettingVolume_Prefix + ActivityPredict + Constant.SPF_SettingVolume_Postfix,-1);	//第二個值為預設值
			final int ring_max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
			
			QA_seekBar.setMax(ring_max); //設定目前SeekBar之最大值
	       
			//設定目前SeekBar初始值
			QA_seekBar.setProgress(ring_value);	
			QA_seekBarValue.setText("Set volume(" + String.valueOf(ring_value) + "/" + String.valueOf(ring_max) + ")");
			
			QA_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
	        	@Override  
	        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	        		//當seekbar改變時
	        		QA_seekBarValue.setText("Set volume(" + String.valueOf(progress) + "/" + String.valueOf(ring_max) + ")");
		 			
	 		   	}	  
	 		  
	        	@Override  
	        	public void onStartTrackingTouch(SeekBar seekBar) {  
	        		//當按下seekbar時
	        	}  
	 		  
	        	@Override  
	        	public void onStopTrackingTouch(SeekBar seekBar) {
	        		//當離開seekbar時
	 			   
	        		//使用SharedPreferences儲存user偏好設定
	        		SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);	//0:private,此app才能用  ,1:public,所有app皆可用
	        		SharedPreferences.Editor PE = settings.edit();
	        		PE.putInt(Constant.SPF_SettingVolume_Prefix + ActivityPredict + Constant.SPF_SettingVolume_Postfix, seekBar.getProgress());
	        		PE.commit();
	 			  
	 		   	}  
	 		});  
		
		//=======================================================================       
        //QA dialog 的 button 設定區
        //======================================================================= 
        
		DialogInterface.OnClickListener Dialoglistener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_NEGATIVE){//取消按键的點擊事件，左
					//Toast.makeText(MainActivity.this, "Yes, I'm in" ,Toast.LENGTH_SHORT).show();
					
					Mode = ActivityPredict;
					
					//使用SharedPreferences讀取user偏好設定
        			final AudioManager audioManager1; //音頻
        			audioManager1 = (AudioManager)getSystemService(AUDIO_SERVICE);
					SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);
		 			final int ring_value = settings.getInt(Constant.SPF_SettingVolume_Prefix + ActivityPredict + Constant.SPF_SettingVolume_Postfix,-1);	//第二個值為預設值
		 			final int ring_max = audioManager1.getStreamMaxVolume(AudioManager.STREAM_RING);
		 			
		 			Long spentTime = System.currentTimeMillis() - askedTimeToParse;
		 			//change mode if spentTime is smaller than 30 minutes
		 			if(spentTime <= 30*60*1000) {
			 			//改變音量
			 			audioManager1.setStreamVolume(AudioManager.STREAM_RING, ring_value, 0);
			 			
			 			//show "start" marker on button by mode
			 			setModeStart(Mode);
		 			}
		 			
		 			//set activity label to server and move app to background
					String fileName = lastFileName;
					if(retrieveFile != null) {
						fileName = retrieveFile;
					}
		 			setLabelToServer(fileName,Mode);
		 			moveTaskToBack(true);
				}
				else if(which == DialogInterface.BUTTON_POSITIVE){//確定按键的點擊事件，右
					//Toast.makeText(MainActivity.this, "No, I'm not" ,Toast.LENGTH_SHORT).show();			
					
					Mode = ActivityPredict;
					
					scDialog = new Dialog(MainActivity.this);
					scDialog.setContentView(R.layout.sc_dialog_main);
					scDialog.setTitle("choose your activity");
					
					final String scMode = "without_" + ActivityPredict.toLowerCase();  //改此mode即可, 用 without_ + 已經選過的activity (需小寫)
					
					final ImageView scImage;
					final Button scButton1,scButton2,scButton3,scButton4,scButton5,scButton6;
					final Button scButton7,scButton8,scOutButton;
				
					//set image
					scImage = (ImageView)scDialog.findViewById(R.id.scImage);
			        int imageID = getResources().getIdentifier(scMode + "_image0", "drawable", getPackageName());
					scImage.setImageResource(imageID);
					
					//button control
					scButton1 = (Button)scDialog.findViewById(R.id.scButton1);
					scButton2 = (Button)scDialog.findViewById(R.id.scButton2);
					scButton3 = (Button)scDialog.findViewById(R.id.scButton3);
					scButton4 = (Button)scDialog.findViewById(R.id.scButton4);
					scButton5 = (Button)scDialog.findViewById(R.id.scButton5);
					scButton6 = (Button)scDialog.findViewById(R.id.scButton6);
					scButton7 = (Button)scDialog.findViewById(R.id.scButton7);
					scButton8 = (Button)scDialog.findViewById(R.id.scButton8);			        
					scOutButton = (Button)scDialog.findViewById(R.id.scOutButton);	
		
			        scButton1.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image1", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);

				        	if(scMode.equals("without_working"))	Mode = "Entertainment";
				        	if(scMode.equals("without_dining"))		Mode = "Entertainment";
				        	if(scMode.equals("without_entertainment"))	Mode ="Transportation";
				        	if(scMode.equals("without_spotring"))	Mode = "Entertainment";
				        	if(scMode.equals("without_shopping"))	Mode = "Entertainment";
				        	if(scMode.equals("without_transportation"))	Mode = "Entertainment";
			        	}
			        });
			        scButton2.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image2", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);
				        	
				        	if(scMode.equals("without_working"))	Mode = "Sporting";
				        	if(scMode.equals("without_dining"))		Mode = "Sporting";
				        	if(scMode.equals("without_entertainment"))	Mode = "Sporting";
				        	if(scMode.equals("without_spotring"))	Mode = "Transportation";
				        	if(scMode.equals("without_shopping"))	Mode = "Sporting";
				        	if(scMode.equals("without_transportation"))	Mode = "Sporting";
				        	
			        	}
			        });
			        scButton3.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image3", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);
				        	
				        	if(scMode.equals("without_working"))	Mode = "Shopping";
				        	if(scMode.equals("without_dining"))		Mode = "Shopping";
				        	if(scMode.equals("without_entertainment"))	Mode = "Shopping";
				        	if(scMode.equals("without_spotring"))	Mode = "Shopping";
				        	if(scMode.equals("without_shopping"))	Mode = "Transportation";
				        	if(scMode.equals("without_transportation"))	Mode = "Shopping";
				        	
			        	}
			        });
			        scButton4.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image4", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);

				        	if(scMode.equals("without_working"))	Mode = "Transportation";
				        	if(scMode.equals("without_dining"))		Mode = "Working";
				        	if(scMode.equals("without_entertainment"))	Mode = "Working";
				        	if(scMode.equals("without_spotring"))	Mode = "Working";
				        	if(scMode.equals("without_shopping"))	Mode = "Working";
				        	if(scMode.equals("without_transportation"))	Mode = "Working";
				        	
			        	}
			        });
			        scButton5.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image5", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);

				        	if(scMode.equals("without_working"))	Mode = "Dining";
				        	if(scMode.equals("without_dining"))		Mode = "Transportation";
				        	if(scMode.equals("without_entertainment"))	Mode = "Dining";
				        	if(scMode.equals("without_spotring"))	Mode = "Dining";
				        	if(scMode.equals("without_shopping"))	Mode = "Dining";
				        	if(scMode.equals("without_transportation"))	Mode = "Dining";
				        	
			        	}
			        });
			        scButton6.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image3", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);
				        	
				        	if(scMode.equals("without_working"))	Mode = "Shopping";
				        	if(scMode.equals("without_dining"))		Mode = "Shopping";
				        	if(scMode.equals("without_entertainment"))	Mode = "Shopping";
				        	if(scMode.equals("without_spotring"))	Mode = "Shopping";
				        	if(scMode.equals("without_shopping"))	Mode = "Transportation";
				        	if(scMode.equals("without_transportation"))	Mode = "Shopping";
				        	
			        	}
			        });
			        scButton7.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image4", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);
				        	
				        	if(scMode.equals("without_working"))	Mode = "Transportation";
				        	if(scMode.equals("without_dining"))		Mode = "Working";
				        	if(scMode.equals("without_entertainment"))	Mode = "Working";
				        	if(scMode.equals("without_spotring"))	Mode = "Working";
				        	if(scMode.equals("without_shopping"))	Mode = "Working";
				        	if(scMode.equals("without_transportation"))	Mode = "Working";
				        	
			        	}
			        });
			        scButton8.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	int imageID = getResources().getIdentifier(scMode + "_image5", "drawable", getPackageName());
				        	scImage.setImageResource(imageID);
				        	
				        	if(scMode.equals("without_working"))	Mode = "Dining";
				        	if(scMode.equals("without_dining"))		Mode = "Transportation";
				        	if(scMode.equals("without_entertainment"))	Mode = "Dining";
				        	if(scMode.equals("without_spotring"))	Mode = "Dining";
				        	if(scMode.equals("without_shopping"))	Mode = "Dining";
				        	if(scMode.equals("without_transportation"))	Mode = "Dining";
				        	
			        	}
			        });
			        
			        //確定button
			        scOutButton.setOnClickListener(new OnClickListener()
			        {
				        @Override
				        public void onClick(View v) {
				        	if(Mode.equals(ActivityPredict)){	//沒選activity
				        		Toast.makeText(MainActivity.this, "please choose an activity" ,Toast.LENGTH_SHORT).show();
				        	}
				        	else{
				        		//====================================================
				        		//====================================================
				        		//Mode為user所選的activity
				        		//====================================================
				        		//====================================================
				        		
				        		//使用SharedPreferences讀取user偏好設定
			        			final AudioManager audioManager2; //音頻
			        			audioManager2 = (AudioManager)getSystemService(AUDIO_SERVICE);
		    					SharedPreferences settings = getSharedPreferences(Constant.SPF_SettingVolume, 0);
					 			final int ring_value = settings.getInt(Constant.SPF_SettingVolume_Prefix + Mode + Constant.SPF_SettingVolume_Postfix,-1);	//第二個值為預設值
					 			final int ring_max = audioManager2.getStreamMaxVolume(AudioManager.STREAM_RING);
					 			
					 			Long spentTime = System.currentTimeMillis() - askedTimeToParse;
					 			//change mode if spentTime is smaller than 30 minutes
					 			if(spentTime <= 30*60*1000) {
						 			//改變音量
						 			audioManager2.setStreamVolume(AudioManager.STREAM_RING, ring_value, 0);
					        		
						 			//show "start" marker on button by mode
						 			setModeStart(Mode);
					 			}
					 			
				        		scDialog.dismiss();
				        		
					 			//set activity label to server and move app to background
								String fileName = lastFileName;
								if(retrieveFile != null) {
									fileName = retrieveFile;
								}
					 			setLabelToServer(fileName,Mode);
					 			moveTaskToBack(true);
				        	}
				        	
			        	}
			        });
					scDialog.setCanceledOnTouchOutside(false);
					scDialog.setCancelable(false);
					scDialog.show(); 
				}			
			}
		};
		
		QA_dialog.setPositiveButton("No, I'm not", Dialoglistener);
		QA_dialog.setNegativeButton("Yes, I'm in", Dialoglistener);
		QA_dialog.setCancelable(false);
		qaDialog = QA_dialog.show();
	}
}
