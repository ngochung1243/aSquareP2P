package com.example.demo_wifip2p;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class ShowImageActivity extends Activity {

	ImageView imvReceiveImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showimage_activity);
		imvReceiveImage = (ImageView)findViewById(R.id.imvReceiveImage);
		Intent mIntent = getIntent();
		String imagePath = mIntent.getStringExtra("ImagePath");
		Bitmap bm = BitmapFactory.decodeFile(imagePath);
		imvReceiveImage.setImageBitmap(bm);
	}
}
