package com.tanzeer.imageeditingapp;

import com.tanzeer.imageeditingapp.R;
import com.tanzeer.imageeditingapp.utils.CommonConstants;
import com.tanzeer.imageeditingapp.utils.MailUtils;
import com.tanzeer.imageeditingapp.views.PictureView;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button btngetPicture;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btngetPicture=(Button)findViewById(R.id.button1);
		 btngetPicture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
				galleryIntent.setType("image/*");
				startActivityForResult(galleryIntent,CommonConstants.REQ_OPEN_PICTURE);
				
				
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==CommonConstants.REQ_OPEN_PICTURE)
		{
			//Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_LONG).show();
			Uri imageUri=data.getData();
			Log.v("image uri",imageUri.toString());
			Intent intent=new Intent(getApplicationContext(),EditImageActivity.class);
			
			intent.putExtra(CommonConstants.KEY_PICTURE_SOURCE,
					CommonConstants.LOAD_PICTURE_FROM_GALLERY);
			intent.setData(data.getData());
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			//Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_LONG).show();

		}
		//Toast.makeText(getApplicationContext(),"new app",Toast.LENGTH_LONG).show();
		//Intent intent=new Intent(getApplicationContext(),EditImageActivity.class);
		//startActivity(intent);
		//super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
