package com.example.myactivity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.example.myactivity.R;
import com.example.myactivity.staticdata.Constant;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class QADialog extends Activity {
	
	Button start_button,button1,button2,button3;
	TextView content_tv;
	String[] question;	//the string array of receiving question and selection
	String[] ActivityNormal = {"Transportation","Shopping","Dining","Working","Sporting","Entertainment"};
	String PackageName;
	String InputActivityName;
	String ActivityName;
	String DialogTitle;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
               
        Bundle bundle = getIntent().getExtras();  
        InputActivityName = bundle.getString("ActivityName");
        PackageName = bundle.getString("PackageName");
        
    	ActivityName = "default";
        for(int i=0;i<6;i++){
        	if(InputActivityName.equals(ActivityNormal[i])){
        		ActivityName = InputActivityName;
        	}
        }
        
        if(ActivityName.equals("default")) {
        	//getActivityByTime
        	Calendar calendar = new GregorianCalendar();  
    		int hour =calendar.get(Calendar.HOUR_OF_DAY);
    		if(hour>=8 && hour<12)
    			ActivityName = "Transportation";
    		else if(hour>=12 && hour<17)
    			ActivityName = "Working";
    		else if(hour>=17 && hour<20)
    			ActivityName = "Dining";
    		else if(hour>=20 && hour<24)
    			ActivityName = "Shopping";
    		else if(hour>=24 || hour<8)
    			ActivityName = "Entertainment";
        }
        			
        //get string array in xml by variable name
       	int resourceId = getResources().getIdentifier(ActivityName, "array", PackageName);
       	question = getResources().getStringArray(resourceId);
        		
     		
     	final Dialog dialog = new Dialog(QADialog.this);
        dialog.setContentView(R.layout.dialog_main);
        dialog.setTitle(ActivityName);
        		
        		
        content_tv = (TextView)dialog.findViewById(R.id.content_tv);
        button1 = (Button)dialog.findViewById(R.id.button_past);
        button2 = (Button)dialog.findViewById(R.id.button_now);
        button3 = (Button)dialog.findViewById(R.id.button_future);
                
        //set text
        content_tv.setText(question[0]);
        button1.setText(question[1]);
        button2.setText(question[2]);
        button3.setText(question[3]);
                
        //set graph
        ActivityName = ActivityName.toLowerCase(Locale.ENGLISH);
               
        int btn1ID = getResources().getIdentifier(ActivityName + "_btn1", "drawable", PackageName);
        button1.setCompoundDrawablesWithIntrinsicBounds(0,btn1ID,0,0);
                
        int btn2ID = getResources().getIdentifier(ActivityName + "_btn2", "drawable", PackageName);
        button2.setCompoundDrawablesWithIntrinsicBounds(0,btn2ID,0,0);
                
        int btn3ID = getResources().getIdentifier(ActivityName + "_btn3", "drawable", PackageName);
        button3.setCompoundDrawablesWithIntrinsicBounds(0,btn3ID,0,0);
                
                
                
        button1.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	dialog.dismiss();
	        	Intent intent = new Intent();
				intent.putExtra(Constant.ANSWER, 1);
	        	setResult(RESULT_OK,intent);
	        	finish();
        	}
        });
        button2.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            dialog.dismiss();
	            Intent intent = new Intent();
				intent.putExtra(Constant.ANSWER, 2);
	        	setResult(RESULT_OK,intent);
	            finish();
	        }
        });
        button3.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            dialog.dismiss();
	            Intent intent = new Intent();
				intent.putExtra(Constant.ANSWER, 3);
	        	setResult(RESULT_OK,intent);
	            finish();
        	}
        });
        dialog.show();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
