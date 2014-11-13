package com.d2.NSD.activity;

import android.content.Context;
import android.hardware.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.*;
import com.d2.NSD.R;
import com.d2.NSD.view.CameraPreview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

		//release();
	}

	private void init() {
		Log.i(TAG, "---> init");

		initRegistrationListener();
		registerService(getResources().getInteger(R.integer.port), NsdManager.PROTOCOL_DNS_SD);
		startListening(getResources().getInteger(R.integer.port));
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

	private void registerService(int port, int protocolType) {
		NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
		nsdServiceInfo.setServiceName(getString(R.string.service_name));
		nsdServiceInfo.setServiceType("_http._tcp.");
		nsdServiceInfo.setPort(port);

		nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
		nsdManager.registerService(nsdServiceInfo, protocolType, registrationListener);
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

	private void startListening(final int port) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ServerSocket serverSocket = null;
				try {
					// 创建一个ServerSocket在端口port监听客户请求
					serverSocket = new ServerSocket(port);

					// 侦听并接受到此Socket的连接，并产生一个Socket对象
					Socket socket = serverSocket.accept();

					// 获取客户端传来的信息
					// 由Socket对象得到输入流，并构造相应的BufferedReader对象
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					// 获取从字符串
					String line = null;
					StringBuilder builder = new StringBuilder();
					while ((line = bufferedReader.readLine()) != null) {
						builder.append(line);
					}
					Log.i(TAG, "---> Client says hello");

					bufferedReader.close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	//private NsdManager nsdManager;
	//private NsdManager.DiscoveryListener discoveryListener;
	//private NsdManager.ResolveListener resolveListener;
	//
	//private List<String> devices = new ArrayList<String>();
	//
	//private ListView lv_devices;
	//private ArrayAdapter<String> adapter;
	//
	//protected void onCreate(Bundle savedInstanceState) {
	//	super.onCreate(savedInstanceState);
	//	setContentView(R.layout.server);
	//
	//	// Update in ActionBar
	//	setTitle(getString(R.string.server));
	//
	//	init();
	//}
	//
	//@Override
	//protected void onResume() {
	//	super.onResume();
	//	devices.clear();
	//	nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	//}
	//
	//private void init() {
	//	// init ListView adapter
	//	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);
	//
	//	// init UI resources
	//	lv_devices = (ListView) findViewById(R.id.lv_devices);
	//	lv_devices.setAdapter(adapter);
	//
	//	// init NsdManager
	//	nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
	//
	//	// init ResolverListener
	//	resolveListener = new NsdManager.ResolveListener() {
	//		@Override
	//		public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
	//			Log.i(TAG, "---> onResolveFailed: " + nsdServiceInfo);
	//		}
	//
	//		@Override
	//		public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
	//			Log.i(TAG, "---> onServiceResolved: " + nsdServiceInfo);
	//
	//			int port = nsdServiceInfo.getPort();
	//			InetAddress host = nsdServiceInfo.getHost();
	//			Log.i(TAG, "---> port: " + port + " / ip address: " + host);
	//			startListening(port);
	//		}
	//	};
	//
	//	// init DiscoveryListener
	//	discoveryListener = new NsdManager.DiscoveryListener() {
	//		@Override
	//		public void onStartDiscoveryFailed(String s, int i) {
	//			Log.i(TAG, "---> onStartDiscoveryFailed: " + s);
	//		}
	//
	//		@Override
	//		public void onStopDiscoveryFailed(String s, int i) {
	//			Log.i(TAG, "---> onStopDiscoveryFailed: " + s);
	//		}
	//
	//		@Override
	//		public void onDiscoveryStarted(String s) {
	//			Log.i(TAG, "---> onDiscoveryStarted: " + s);
	//		}
	//
	//		@Override
	//		public void onDiscoveryStopped(String s) {
	//			Log.i(TAG, "---> onDiscoveryStopped: " + s);
	//		}
	//
	//		@Override
	//		public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
	//			Log.i(TAG, "---> onServiceFound: " + nsdServiceInfo.getServiceName());
	//			devices.add(nsdServiceInfo.getServiceName());
	//			adapter.notifyDataSetChanged();
	//
	//			if (nsdServiceInfo.getServiceName().contains(getString(R.string.service_name))) {
	//				nsdManager.resolveService(nsdServiceInfo, resolveListener);
	//			}
	//		}
	//
	//		@Override
	//		public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
	//			Log.i(TAG, "---> onServiceLost: " + nsdServiceInfo.getServiceName());
	//		}
	//	};
	//}
	//
	//private void startListening(int port) {
	//	ServerSocket serverSocket = null;
	//	try {
	//		// 创建一个ServerSocket在端口port监听客户请求
	//		serverSocket = new ServerSocket(port);
	//
	//		// 侦听并接受到此Socket的连接，并产生一个Socket对象
	//		Socket socket = serverSocket.accept();
	//
	//		// 获取客户端传来的信息
	//		// 由Socket对象得到输入流，并构造相应的BufferedReader对象
	//		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	//		// 获取从字符串
	//		String line = null;
	//		StringBuilder builder = new StringBuilder();
	//		while ((line = bufferedReader.readLine()) != null) {
	//			builder.append(line);
	//		}
	//		System.out.println(builder.toString());
	//	} catch (IOException e) {
	//		e.printStackTrace();
	//	}
	//}
}
