package com.d2.NSD.view;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.*;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private final String TAG = CameraPreview.class.getSimpleName();

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;

	private byte[] mBuffer = new byte[1024 * 8];

	public CameraPreview(Context context, Camera camera) {
		super(context);
		Log.i(TAG, "---> CameraPreview()");
		mCamera = camera;

		// Install a SurfaceHolder.callback so we get notified when the underlying surface is created and destroyed
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.i(TAG, "---> surfaceCreated()");
		try {
			//mCamera.addCallbackBuffer(mBuffer);
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		Log.i(TAG, "---> surfaceChanged()");
		if (mSurfaceHolder.getSurface() == null)
			return;

		mCamera.stopPreview();

		try {
			//mCamera.addCallbackBuffer(mBuffer);
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		surfaceHolder.removeCallback(this);

		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mCamera.release();
	}

	@Override
	public void onPreviewFrame(byte[] bytes, Camera camera) {
		Log.i(TAG, "---> onPreviewFrame: " + bytes.toString());

		byte[] buffer = new byte[1024];

		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();

		String file_name = "save.png";
		File path = new File(Environment.getExternalStorageDirectory().getPath() + "/D2 Camera/");
		File file = new File(path, file_name);
		if (!path.exists()) {
			path.mkdirs();
		}

		// generate NV21 image
		Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
		YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, previewSize.width, previewSize.height, null);

		// convert NV21 image to bitmap
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bytes.length);
		yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, byteArrayOutputStream);
		Bitmap bitmap = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.toByteArray().length, null);

		// save bitmap
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mCamera.setPreviewCallback(this);
		mCamera.startPreview();
	}
}
