package com.tanzeer.imageeditingapp;

import com.tanzeer.imageeditingapp.R;
import com.tanzeer.imageeditingapp.utils.MailUtils;
import com.tanzeer.imageeditingapp.views.PictureView;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	PictureView pv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btn=(Button)findViewById(R.id.button1);
		 pv=(PictureView)findViewById(R.id.imageView1);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pv.saveImage(getApplicationContext());
				MailUtils.sendMail(getApplicationContext(),pv.getOutputPictireUri());
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
