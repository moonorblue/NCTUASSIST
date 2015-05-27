package com.example.myactivity;

import com.example.myactivity.staticdata.Constant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseLabelActivity extends Activity implements View.OnClickListener{
	Button btn_transportation,btn_shopping,btn_dining,btn_working,btn_sporting,btn_entertainment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_label);
		findViews();
		setListeners();
	}

	public void findViews() {
		btn_transportation = (Button)findViewById(R.id.btn_transportation);
		btn_shopping = (Button)findViewById(R.id.btn_shopping);
		btn_dining = (Button)findViewById(R.id.btn_dining);
		btn_working = (Button)findViewById(R.id.btn_working);
		btn_sporting = (Button)findViewById(R.id.btn_sporting);
		btn_entertainment = (Button)findViewById(R.id.btn_entertainment);
	}
	
	public void setListeners() {
		btn_transportation.setOnClickListener(this);
		btn_shopping.setOnClickListener(this);
		btn_dining.setOnClickListener(this);
		btn_working.setOnClickListener(this);
		btn_sporting.setOnClickListener(this);
		btn_entertainment.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		String label = null;
		Intent intent = new Intent();
		switch(id) {
		case R.id.btn_dining:
			label = "Dining";
			break;
		case R.id.btn_entertainment:
			label = "Entertainment";
			break;
		case R.id.btn_shopping:
			label = "Shopping";
			break;
		case R.id.btn_sporting:
			label = "Sporting";
			break;
		case R.id.btn_transportation:
			label = "Transportation";
			break;
		case R.id.btn_working:
			label = "Working";
			break;
		}
		intent.putExtra(Constant.KEY_LABEL, label);
		setResult(RESULT_OK, intent);
		finish();
	}
}
