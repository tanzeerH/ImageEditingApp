package com.tanzeer.imageeditingapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.metalev.multitouch.controller.ImageEntity;




import com.tanzeer.imageeditingapp.utils.CommonConstants;
import com.tanzeer.imageeditingapp.utils.ImageUtils;
import com.tanzeer.imageeditingapp.utils.MailUtils;
import com.tanzeer.imageeditingapp.views.PictureView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class EditImageActivity extends Activity {
	private final static String t = EditImageActivity.class.getName();
	private PictureView pictureView;
	Button btnMail,btnAdd;
	Bitmap picture;
	private Uri imageUri;
	private float scaleFractor;
	private int pictureSource;
	private ImageUtils imageUtils = ImageUtils.getInstance();
	int marginLeft = 0;
	protected int  displayWidth;
	protected int displayHeight;
	protected int pictureDisplayWidth;
	protected int pictureDisplayHeight;
	protected int footerBarHeight;

	private float cx;
	private float cy;
	List<Face> faces = null;
	private List<ImageEntity> entities = new ArrayList<ImageEntity>();
	private int entityIndex = 0;
	
	public static Bitmap sendPicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editpicture);
		pictureView = (PictureView) findViewById(R.id.imageView2);
		btnMail=(Button) findViewById(R.id.button1);
		btnMail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				pictureView.buildDrawingCache();
				pictureView.saveImage(getApplicationContext());
				MailUtils.sendMail(getApplicationContext(),
						pictureView.getOutputPictireUri());
				
			}
		});
		btnAdd=(Button)findViewById(R.id.buttonadd);
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String[] paths={"antonio_2x.png"};
				ImageEntity imageEntity = new ImageEntity(getApplicationContext(),
						paths, pictureDisplayWidth,
						pictureDisplayHeight);
				imageEntity.setScaleFactor(0.75);
				pictureView.addImageEntity(EditImageActivity.this, imageEntity);
				pictureView.invalidate();
				
			}
		});
		pictureView.setDrawingCacheEnabled(true);
		calcDisplayWidthAndHeight();
		Intent intent = getIntent();
		imageUri = intent.getData();
		if (imageUri != null) {
			 loadPicture(convertImageUriToFile(imageUri, this));
		}
	}
	private void loadPicture(Object url) {
		Bitmap bmp = null;
		Uri uri = null;
		try {
			if (picture != null) {
				picture.recycle();
				picture = null;
			}
			pictureView.setBackgroundDrawable(null);
			pictureView.setImageBitmap(null);
			
				uri = Uri.withAppendedPath(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + url);
				String binarypath = getPathFromUri((Uri) uri);

				bmp = imageUtils.decodeFileByWidthHeight(binarypath,
						pictureDisplayWidth, pictureDisplayHeight);

				// Rotate image according to orientation start
				// rotate image if 90, 180, 270 degree
				float rotation = imageUtils.rotationForImage(
						EditImageActivity.this, binarypath);
				if (rotation != 0f) {
					Matrix matrix = new Matrix();
					matrix.preRotate(rotation);
					Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0,
							bmp.getWidth(), bmp.getHeight(), matrix, true);
					if (resizedBitmap != bmp) {
						bmp.recycle();
						bmp = null;
						bmp = resizedBitmap;
						Log.i(t, "setBinaryData - rotate image " + rotation
								+ " degree successfully !");
					}
				} else {
					Log.i(t, "setBinaryData - NOT rotate image due to "
							+ rotation + " degree");
				}
			 
			picture = bmp;
			if (picture == null) {
				Log.e(t, "Could not load picture.");
				
				finish();
			} else {
				displayPicture();
			}

		} catch (Exception e) {
			e.printStackTrace();
			finish();
		} catch (OutOfMemoryError outOfMemoryError) {
			// TODO: handle exception
			outOfMemoryError.printStackTrace();
			finish();
		}
	}
	protected String getPathFromUri(Uri uri) {
		// find entry in content provider
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();

		// get data path
		String colString = c.getString(c.getColumnIndex("_data"));
		c.close();
		return colString;
	}
	public String convertImageUriToFile(Uri imageUri, Activity activity) {

		Cursor cursor = null;
		int imageID = 0;

		try {

			/*********** Which columns values want to get *******/
			String[] proj = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.ImageColumns.ORIENTATION };

			cursor = getContentResolver().query(

			imageUri, // Get data for specific image URI
					proj, // Which columns to return
					null, // WHERE clause; which rows to return (all rows)
					null, // WHERE clause selection arguments (none)
					null // Order-by clause (ascending by name)

					);

			// Get Query Data

			int columnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

			int size = cursor.getCount();

			/******* If size is 0, there are no images on the SD Card. *****/

			if (size == 0) {
				// imageDetails.setText("No Image");
			} else {
				if (cursor.moveToFirst()) {
					/***** Used to show image on view in LoadImagesFromSDCard class ******/
					imageID = cursor.getInt(columnIndex);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return "" + imageID;
	}
	protected void calcDisplayWidthAndHeight() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		this.displayWidth = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.max(metrics.widthPixels , metrics.heightPixels) : Math.min(
				metrics.widthPixels, metrics.heightPixels);
		this.displayHeight = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.min(metrics.widthPixels, metrics.heightPixels) : Math.max(
				metrics.widthPixels, metrics.heightPixels);
		Log.i("BaseActivity", "displayWidth: " + displayWidth + " ; displayHeight: " + displayHeight);
		this.footerBarHeight = this.displayWidth /6;
		this.pictureDisplayWidth = this.displayWidth;
		this.pictureDisplayHeight = (int) (this.displayHeight/1.3);
		
	}
	private void displayPicture() {
		Log.d(t,
				"Screen: w " + displayWidth + " h " + displayHeight
						+ " ; Image w " + picture.getWidth() + " h "
						+ picture.getHeight());
		if (picture.getWidth() > pictureDisplayWidth
				|| pictureSource == CommonConstants.GET_PICTURE_FROM_FACEBOOK) {
			scaleFractor = imageUtils.getScaleFactor(picture.getWidth(),
					picture.getHeight(), pictureDisplayWidth,
					pictureDisplayHeight);

			picture = imageUtils
					.rescaleBitmapAspectRatio(picture, scaleFractor);
		} else {
			scaleFractor = 1;
		}

		pictureView.setEnabled(true);
		pictureView.setImageBitmap(picture);
		pictureView.invalidate();
		//
		
		// Set onTouch listener
		pictureView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.d(t, "onTouch");
				boolean ret = pictureView.onTouchEvent(event);

				if (!pictureView.isTouchOnEntity(event.getX(), event.getY())) {
					pictureView.unselectObject(pictureView
							.getSelectedImgEntity());
				}
				

				PictureView pictureView = (PictureView) v;
				ImageEntity entity = (ImageEntity) pictureView
						.getSelectedImgEntity();
				
				return ret;
			}
		});

		//commenting
		/*Bitmap bitmap = ((BitmapDrawable) pictureView.getDrawable())
				.getBitmap();
		/*faces = ImageUtils.getInstance().detectFaces(bitmap);*/
		//showProgressDialog();
		/*faces = ImageUtils.getInstance().detectFaces( bitmap);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				//dismissProgressDialog();
			}

		}, 2000);
		
		if (faces != null && faces.size() > 0) {

			int facesCount = faces.size();
			// Rect rect = null;
			PointF midPoint = new PointF();
			float eyeDistance = 0.0f;
			float confidence = 0.0f;
			float scaleX = 1 / getResources().getDisplayMetrics().density;// /getResources().getDisplayMetrics().density;//picture.getScaledWidth(getResources().getDisplayMetrics());//getResources().getDisplayMetrics().density;
			float scaleY = scaleX;// ((BitmapDrawable)
									// pictureView.getDrawable()).getBitmap().getScaledHeight(getResources().getDisplayMetrics());

			for (int index = 0; index < facesCount; index++) {
				faces.get(index).getMidPoint(midPoint);
				eyeDistance = faces.get(index).eyesDistance();
				confidence = faces.get(index).confidence();
				final Rect rect = getFaceRect(faces.get(index));
				Log.i(t, "displayPicture: Confidence: " + confidence
						+ ", Eye distance: " + eyeDistance + ", Mid Point: ("
						+ midPoint.x + ", " + midPoint.y + ")" + ", Pose: "
						+ faces.get(index).pose(Face.EULER_Y));
				cx = scaleX * midPoint.x;
				cy = scaleY * midPoint.y;
				// Add Glasses
				/*
				 * Handler mHandler = new Handler(); mHandler.postDelayed(new
				 * Runnable() { public void run() {
				 */
				/*ImageEntity glasses = getGlasses2(rect, index);
				if (glasses != null) {
					glasses.setCenterXY(cx, cy);
					entities.add(glasses);
				}

				cx = scaleX * midPoint.x;
				cy = scaleY * (midPoint.y + eyeDistance);

				ImageEntity mustache;
				if (getRandomIndex() == 1) {
					mustache = getMustache2(rect, index);
					mustache.setCenterXY(cx, cy);
					entities.add(mustache);
				} else {
					mustache = getBread2(rect, index);
					// cy = scaleY * (midPoint.y +
					// mustache.getDrawable().getIntrinsicHeight()/2);
					// cy = cy + mustache.getDrawable().getIntrinsicHeight()/2;
					cy = cy + cy / 4;
					mustache.setCenterXY(cx, cy);
					entities.add(mustache);
				}

			}
			faces.clear();
			faces = null;
		}

		if (entities.size() > 0) {
			Handler mHandler = new Handler();

			mHandler.postDelayed(new Runnable() {
				public void run() {
					dropStache();
				}

			}, 1000);

		}

		/*
		 * } }, 100);
		 */

	}
	
}
