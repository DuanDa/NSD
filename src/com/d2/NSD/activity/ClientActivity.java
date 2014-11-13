package com.d2.NSD.activity;

import android.content.Context;
import android.hardware.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.d2.NSD.R;
import com.d2.NSD.view.CameraPreview;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AbstractActivity {

	private final String TAG = ClientActivity.class.getSimpleName();

	private NsdManager nsdManager;
	private NsdManager.DiscoveryListener discoveryListener;
	private NsdManager.ResolveListener resolveListener;

	private List<String> devices = new ArrayList<String>();

	private ListView lv_devices;
	private ArrayAdapter<String> adapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		// Update in ActionBar
		setTitle("Client");

		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		devices.clear();
		nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}

	private void init() {
		// init ListView adapter
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);

		// init UI resources
		lv_devices = (ListView) findViewById(R.id.lv_devices);
		lv_devices.setAdapter(adapter);

		// init NsdManager
		nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

		// init ResolverListener
		resolveListener = new NsdManager.ResolveListener() {
			@Override
			public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
				Log.i(TAG, "---> onResolveFailed: " + nsdServiceInfo);
			}

			@Override
			public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
				Log.i(TAG, "---> onServiceResolved: " + nsdServiceInfo);

				int port = nsdServiceInfo.getPort();
				InetAddress inetAddress = nsdServiceInfo.getHost();
				Log.i(TAG, "---> port: " + port + " / ip address: " + inetAddress);

				sendTestToServer(inetAddress, port);
			}
		};

		// init DiscoveryListener
		discoveryListener = new NsdManager.DiscoveryListener() {
			@Override
			public void onStartDiscoveryFailed(String s, int i) {
				Log.i(TAG, "---> onStartDiscoveryFailed: " + s);
			}

			@Override
			public void onStopDiscoveryFailed(String s, int i) {
				Log.i(TAG, "---> onStopDiscoveryFailed: " + s);
			}

			@Override
			public void onDiscoveryStarted(String s) {
				Log.i(TAG, "---> onDiscoveryStarted: " + s);
			}

			@Override
			public void onDiscoveryStopped(String s) {
				Log.i(TAG, "---> onDiscoveryStopped: " + s);
			}

			@Override
			public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
				Log.i(TAG, "---> onServiceFound: " + nsdServiceInfo.getServiceName());
				devices.add(nsdServiceInfo.getServiceName());
				adapter.notifyDataSetChanged();

				if (nsdServiceInfo.getServiceName().contains(getString(R.string.service_name))) {
					nsdManager.resolveService(nsdServiceInfo, resolveListener);
				}
			}

			@Override
			public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
				Log.i(TAG, "---> onServiceLost: " + nsdServiceInfo.getServiceName());
			}
		};
	}

	private void sendTestToServer(InetAddress inetAddress, int port) {
		String text = "This text is from client";

		// 创建一个套接字
		Socket socket = null;
		try {
			// 创建一个套接字并将其指定到Server IP地址
			socket = new Socket(inetAddress.getHostAddress(), port);
			socket.setSoTimeout(30000);

			// 由Socket对象得到输出流，并构造PrinterWriter对象
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

			// 将字符串输出到Server
			BufferedReader bufferedReader = new BufferedReader(new StringReader(text));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}

			bufferedReader.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//private NsdManager.RegistrationListener registrationListener;
	//private NsdManager nsdManager;
	//private Camera mCamera;
	//
	//private TextView tv_service_name;
	//private TextView tv_ip;
	//private FrameLayout fl_container;
	//
	//private String serviceName;
	//private byte[] mBuffer = new byte[1024 * 8];
	//
	//@Override
	//protected void onCreate(Bundle savedInstanceState) {
	//	super.onCreate(savedInstanceState);
	//	Log.i(TAG, "---> onCreate");
	//	setContentView(R.layout.client);
	//
	//	// Update in ActionBar
	//	setTitle(getString(R.string.client));
	//
	//	init();
	//}
	//
	//@Override
	//protected void onResume() {
	//	super.onResume();
	//	Log.i(TAG, "---> onResume");
	//
	//	tv_service_name.setText(getString(R.string.service_name));
	//	WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
	//	String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
	//	tv_ip.setText(ip);
	//
	//	// Create preview and set camera preview
	//	CameraPreview cameraPreview = new CameraPreview(this, mCamera);
	//	fl_container.addView(cameraPreview);
	//
	//
	//}
	//
	//@Override
	//protected void onStop() {
	//	super.onStop();
	//
	//	//release();
	//}
	//
	//private void init() {
	//	Log.i(TAG, "---> init");
	//
	//	initRegistrationListener();
	//	registerService(NsdManager.PROTOCOL_DNS_SD);
	//	mCamera = getCameraInstance();
	//
	//	tv_service_name = (TextView) findViewById(R.id.tv_service_name);
	//	tv_ip = (TextView) findViewById(R.id.tv_ip);
	//	fl_container = (FrameLayout) findViewById(R.id.fl_container);
	//}
	//
	//private void initRegistrationListener() {
	//	registrationListener = new NsdManager.RegistrationListener() {
	//		@Override
	//		public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
	//			Log.i(TAG, "---> onRegistrationFailed");
	//		}
	//
	//		@Override
	//		public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
	//			Log.i(TAG, "---> onUnregistrationFailed");
	//		}
	//
	//		@Override
	//		public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
	//			serviceName = nsdServiceInfo.getServiceName();
	//		}
	//
	//		@Override
	//		public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
	//			Log.i(TAG, "---> onServiceUnregistered");
	//		}
	//	};
	//}
	//
	//private void registerService(int port) {
	//	NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
	//	nsdServiceInfo.setServiceName(getString(R.string.service_name));
	//	nsdServiceInfo.setServiceType("_http._tcp.");
	//	nsdServiceInfo.setPort(port);
	//
	//	nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
	//	nsdManager.registerService(nsdServiceInfo, port, registrationListener);
	//}
	//
	//private Camera getCameraInstance() {
	//	Camera camera = null;
	//	camera = Camera.open();
	//	camera.setDisplayOrientation(90);
	//	return camera;
	//}
	//
	//private void release() {
	//	if (mCamera != null) {
	//		mCamera.stopPreview();
	//		mCamera.release();
	//	}
	//}
}
