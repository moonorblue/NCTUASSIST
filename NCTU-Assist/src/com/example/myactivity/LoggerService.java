package com.example.myactivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.myactivity.staticdata.Constant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Button;

public class LoggerService extends Service{
	private Handler handler = new Handler();
	
	private  SensorManager sm; 
	private LocationManager lm;
	private String lifelabel;
    //private String posturelabel;
	private float Light,Pro,Pre;
	private float [] A=new float [3]; 
	private float [] M=new float [3];
	private float [] G=new float [3];
	private double [] GPS= new double [3]; 
	private static final String TAG = "sensor";
	private JSONArray aGPS,Accel,Gyro,Magne,Pressure,Proximity,aLight;
	private boolean filesave=true;
	File file;
	FileOutputStream fout;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	Bundle bundle = new Bundle();
	String filename;
	WifiManager mWifiManager=null;
	WifiInfo mWifiInfo=null;
	TelephonyManager manager;
	GsmCellLocation gsm;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@SuppressWarnings("deprecation")
	
	@Override
    public void onStart(Intent intent, int startId) {
	  Log.i(TAG, "Received start id " + startId + ": " + intent);
	  
	  /*
	  mWifiManager = (WifiManager)this.getSystemService(this.WIFI_SERVICE);  
	  mWifiInfo = mWifiManager.getConnectionInfo();   
      */
	  /* 
	  manager = (TelephonyManager) mContext2.getSystemService(Context.TELEPHONY_SERVICE);
	  gsm = ((GsmCellLocation) manager.getCellLocation());
	  */
	  if(intent == null) {
		  return;
	  }
	  bundle = intent.getExtras();
	  if(bundle.getBoolean("stop")){
		  filesave = bundle.getBoolean("filesave");
		  onDestroy();
	  }
	  
	  else{
		  lifelabel =  bundle.getString("lifelabel");
	      handler.postDelayed(showTime, 1000);
	      super. onStart(intent,startId);
		  newjsonarray();
	      sensorregister();
	      openfile();

	  }
    }
	
	 @Override  
	 public boolean onUnbind(Intent intent) {
	 	Bundle bundle = intent.getExtras();
		filesave =  bundle.getBoolean("filesave");
	 	Log.i(TAG,"unbind"+filesave);
	    return super.onUnbind(intent);  
	 }
	
	 
	 private void openfile()
	 {
		  String path = Environment.getExternalStorageDirectory().getPath();
		   File dir = new File(path + "/HTCactivitylogger");
		   if (!dir.exists()){
			   dir.mkdir();
		   }
		   File dir2 = new File(path + "/HTCactivitylogger/Hardware");
		   if (!dir2.exists()) {
			   dir2.mkdir();
		   }

		   try {
			   TelephonyManager tM=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
			   String imei = tM.getDeviceId();
			   Date dt = new Date();
			   
		       file = new File(path + "/HTCactivitylogger/Hardware/h" +sdf.format(dt)+"_"+imei+".txt");
		       fout = new FileOutputStream(file);
		       filename = "h" +sdf.format(dt)+"_"+imei+".txt";
		       
		       
		       //fout.write(new Date().toString().getBytes());
		  } catch (FileNotFoundException e) {
		        e.printStackTrace();
		  } 
		  fisttime=true; 
		  Log.d(TAG, "Write to SDCARD!");
		   
	 }
	
	private void sensorregister()
	{
		int rate=SensorManager.SENSOR_DELAY_FASTEST;
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE); 
		
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),rate);
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),rate);
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE),rate);
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_PRESSURE),rate);
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_PROXIMITY),rate);
	    sm.registerListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_LIGHT),rate);
	    
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 0, locationListener);
	}
	
	final LocationListener locationListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			if (location != null) {
				GPS[0] = location.getLongitude() ;  
                GPS[1] = location.getLatitude();
                GPS[2] = location.getSpeed();
                //Date dt = new Date();
                long dt = System.currentTimeMillis();
                
				try {
		      	 		JSONObject gps = new JSONObject();
		      	 		gps.put("X", GPS[0]);
						gps.put("Y", GPS[1]);
						gps.put("Speed", GPS[2]);
						gps.put("time",dt);
		      	 		aGPS.put(gps);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void newjsonarray()
	{
		aGPS= new JSONArray();
		Accel= new JSONArray();
		Gyro= new JSONArray();
		Magne= new JSONArray();
		Pressure= new JSONArray();
		Proximity= new JSONArray();
		aLight= new JSONArray();
	}
	
	final SensorEventListener mysensorListener = new SensorEventListener(){  
        
		@Override
        public void onSensorChanged(SensorEvent sensorEvent){
			
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){  
                Log.i(TAG,"accChanged");  
                  
                A[0] = sensorEvent.values[0];  
                A[1] = sensorEvent.values[1];  
                A[2] = sensorEvent.values[2];
                long dt = System.currentTimeMillis();
   	      	 	try {
   	      	 		JSONObject a = new JSONObject();
					a.put("X", A[0]);
					a.put("Y", A[1]);
   	      	 		a.put("Z", A[2]);
   	      	 		a.put("time",dt);
   	      	 		Accel.put(a);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
   	      	 	
            } 
            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){  
                Log.i(TAG,"magChanged");  
                  
                M[0] = sensorEvent.values[0];  
                M[1] = sensorEvent.values[1];  
                M[2] = sensorEvent.values[2];
                long dt = System.currentTimeMillis();
                try {
   	      	 		JSONObject m = new JSONObject();
					m.put("X", M[0]);
					m.put("Y", M[1]);
   	      	 		m.put("Z", M[2]);
   	      	 		m.put("time",dt);
   	      	 		Magne.put(m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){  
                Log.i(TAG,"groChanged");  
                  
                G[0] = sensorEvent.values[0];  
                G[1] = sensorEvent.values[1];  
                G[2] = sensorEvent.values[2];
                long dt = System.currentTimeMillis();
                try {
   	      	 		JSONObject g = new JSONObject();
					g.put("X", G[0]);
					g.put("Y", G[1]);
   	      	 		g.put("Z", G[2]);
   	      	 		g.put("time",dt);
   	      	 		Gyro.put(g);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            if(sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE){  
                Log.i(TAG,"preChanged");  
                  
                Pre = sensorEvent.values[0];
                long dt = System.currentTimeMillis();
                try {
   	      	 		JSONObject pre = new JSONObject();
   	      	 		pre.put("X",Pre);
   	      	 		pre.put("time",dt);
   	      	 		Pressure.put(pre);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY){  
                Log.i(TAG,"proChanged");  
                  
                Pro = sensorEvent.values[0];
                long dt = System.currentTimeMillis();
                try {
   	      	 		JSONObject pro = new JSONObject();
   	      	 		pro.put("X",Pro);
   	      	 		pro.put("time",dt);
   	      	 		Proximity.put(pro);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){  
                Log.i(TAG,"lightChanged");  
                  
                Light = sensorEvent.values[0];
                long dt = System.currentTimeMillis();
                try {
   	      	 		JSONObject light = new JSONObject();
   	      	 		light.put("X",Light);
   	      	 		light.put("time",dt);
   	      	 		aLight.put(light);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
        }  
        @Override
        public void onAccuracyChanged(Sensor sensor , int accuracy){  
            Log.i(TAG, "onAccuracyChanged");  
        }
			
    };  
   @Override
   public void onDestroy() {
	   Log.i("Destroy", "sensor Destroy");
      handler.removeCallbacks(showTime);
      
      if(sm != null) {
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_PRESSURE));
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
	      sm.unregisterListener(mysensorListener,sm.getDefaultSensor(Sensor.TYPE_LIGHT));
      }
      if(lm != null) {
    	  lm.removeUpdates(locationListener);
      }
      try {
    	if(fout == null) {
    		Log.i(TAG, "fout is null!");
    		Log.i(TAG, "file is "+file);
    	}else {
    		Log.i(TAG, "fout is not null!");
        	fout.write(']');
        	fout.flush();
    		fout.close();
    	}
    	if(!filesave) {
			file.delete();
		} 
        //change to UPLOAD_STATE 
        String state = Constant.UPLOAD_STATE;
        Intent stateIntent = new Intent();
        stateIntent.setAction(Intent.ACTION_SENDTO);
        stateIntent.putExtra(Constant.STATE, state);
        LocalBroadcastManager.getInstance(this)
              .sendBroadcast(stateIntent);
      } catch (IOException e) {
		  // TODO Auto-generated catch block
		  Log.i(TAG, "write error: " + e.toString());
	  }
      super.onDestroy();
    }

   private Runnable showTime = new Runnable() {
      public void run() {
    	  CreateJsonFile();
          handler.postDelayed(this,1000);
        }
   };
   
   private boolean fisttime=true;
   
   private void CreateJsonFile()
   {
	   String JSONString=null;
	   
	   
 	   JSONObject jsonObject = new JSONObject();
	   try {
		     //jsonObject.put("Wifi_ID",mWifiInfo.getBSSID()); 
		     //jsonObject.put("GSM_ID",gsm.getCid());
		     jsonObject.put("GPS",aGPS);
		     jsonObject.put("Accel",Accel);
		     jsonObject.put("Gyro",Gyro);
		     jsonObject.put("Magne",Magne);
		     jsonObject.put("Pressure",Pressure);
		     jsonObject.put("Proximity",Proximity);
		     jsonObject.put("Light",aLight);
		     jsonObject.put("lifelabel",lifelabel);
			 jsonObject.put("Version",2.1);
	      	 
			 if(fisttime==true){JSONString="[";fisttime=false;}else JSONString=",";
			 JSONString+=jsonObject.toString();
			 newjsonarray();
		  } catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		  }
  	  
  	  try {
			fout.write(JSONString.getBytes());
			//fout.write(new Date().toString().getBytes());
  	  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
  	  }
   }

}
