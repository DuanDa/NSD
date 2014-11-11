package com.d2.NSD.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private final String TAG = CameraPreview.class.getSimpleName();

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.callback so we get notified when the underlying surface is created and destroyed
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			//mCamera.addCallbackBuffer(mBuffer);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		if (mSurfaceHolder.getSurface() == null)
			return;

		mCamera.stopPreview();

		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

	}

	@Override
	public void onPreviewFrame(byte[] bytes, Camera camera) {
		Log.i(TAG, "---> onPreviewFrame: " + bytes.toString());
	}
}
