package com.d2.NSD.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.d2.NSD.R;
import com.d2.NSD.view.CameraPreview;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerActivity extends AbstractActivity  {
	private final String TAG  = ServerActivity.class.getSimpleName();

	private NsdManager.RegistrationListener registrationListener;
	private NsdManager nsdManager;
	private Camera mCamera;

	private TextView tv_service_name;
	private TextView tv_ip;
	private FrameLayout fl_container;

	private String serviceName;
	private byte[] mBuffer = new byte[1024 * 8];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "---> onCreate");
		setContentView(R.layout.server);

		// Update in ActionBar
		setTitle("Server");

		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "---> onResume");

		tv_service_name.setText(getString(R.string.service_name));
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		tv_ip.setText(ip);

		// Create preview and set camera preview
		CameraPreview cameraPreview = new CameraPreview(this, mCamera);
		fl_container.addView(cameraPreview);
	}

	@Override
	protected void onStop() {
		super.onStop();

		release();
	}

	private void init() {
		Log.i(TAG, "---> init");

		initRegistrationListener();
		registerService(NsdManager.PROTOCOL_DNS_SD);
		mCamera = getCameraInstance();

		tv_service_name = (TextView) findViewById(R.id.tv_service_name);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		fl_container = (FrameLayout) findViewById(R.id.fl_container);
	}

	private void initRegistrationListener() {
		registrationListener = new NsdManager.RegistrationListener() {
			@Override
			public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
				Log.i(TAG, "---> onRegistrationFailed");
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
				Log.i(TAG, "---> onUnregistrationFailed");
			}

			@Override
			public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
				serviceName = nsdServiceInfo.getServiceName();
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
				Log.i(TAG, "---> onServiceUnregistered");
			}
		};
	}

	private void registerService(int port) {
		NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
		nsdServiceInfo.setServiceName(getString(R.string.service_name));
		nsdServiceInfo.setServiceType("_http._tcp.");
		nsdServiceInfo.setPort(port);

		nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
		nsdManager.registerService(nsdServiceInfo, port, registrationListener);
	}

	private Camera getCameraInstance() {
		Camera camera = null;
		camera = Camera.open();
		camera.setDisplayOrientation(90);
		return camera;
	}

	private void release() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
	}
}
