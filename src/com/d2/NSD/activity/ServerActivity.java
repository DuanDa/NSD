package com.d2.NSD.activity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.*;
import com.d2.NSD.R;
import com.d2.NSD.view.CameraPreview;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerActivity extends AbstractActivity implements CameraPreview.IBufferLoaded {
	private static final String TAG  = ServerActivity.class.getSimpleName();
	private static final int CONNECTED = 1;
	private static final int DISCONNECTED = 0;

	private NsdManager.RegistrationListener registrationListener;
	private NsdManager nsdManager;
	private Camera mCamera;

	private CameraPreview mCameraPreview;
	private TextView tv_ip;
	private TextView tv_connection_status;

	private FrameLayout fl_container;
	private String serviceName;
	private byte[] mBuffer = new byte[1024 * 8];
	private Handler mHandler;
	private boolean appClose;

	// Define a server socket and client socket
	//private ServerSocket serverSocket;
	//private Socket socket;

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

		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		tv_ip.setText(ip);

		// Create preview and set camera preview
		mCameraPreview = new CameraPreview(this, mCamera, this);
		fl_container.addView(mCameraPreview);
	}

	@Override
	protected void onPause() {
		super.onPause();

		release();
	}

	private void init() {
		Log.i(TAG, "---> init");

		startListening(getResources().getInteger(R.integer.port));
		mCamera = getCameraInstance();
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					tv_connection_status.setText(String.format("%s is connected", msg.getData().getString("IP_ADDRESS")));
					tv_connection_status.setTextColor(Color.GREEN);
				}
				else {
					tv_connection_status.setText("Connection failed");
					tv_connection_status.setTextColor(Color.RED);
				}
			}
		};

		tv_connection_status = (TextView) findViewById(R.id.tv_connection_status);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		fl_container = (FrameLayout) findViewById(R.id.fl_container);
	}

	private Camera getCameraInstance() {
		Camera camera = Camera.open(1);
		camera.setDisplayOrientation(90);
		return camera;
	}

	private void release() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCameraPreview.getHolder().removeCallback(mCameraPreview);
			mCamera.stopPreview();
			mCamera.release();
		}

		appClose = true;

		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}

	private void startListening(final int port) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendOut(port);
			}
		}).start();
	}

	@Override
	public void bufferLoaded(byte[] buffer) {
		//new SendThread(buffer).start();
	}

	class SendThread extends Thread {
		private byte[] buffer;

		public SendThread(byte[] buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {

		}
	}

	private void sendOut(int port) {
		while (true) {
			if (appClose)
				break;

			ServerSocket serverSocket = null;
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(port);
				socket = serverSocket.accept();

				OutputStream outputStream = socket.getOutputStream();
				InputStream inputStream = socket.getInputStream();
				Log.i(TAG, "---> server connected");

				// Connected!
				String hostAddress = socket.getInetAddress().getHostAddress();
				Bundle bundle = new Bundle();
				bundle.putString("IP_ADDRESS", hostAddress);
				Message message = new Message();
				message.what = CONNECTED;
				message.setData(bundle);
				if (mHandler == null) {
					Looper.prepare();
					mHandler = new Handler();
				}
				mHandler.sendMessage(message);

				// Read from client, exit if client says close
				//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				//String line = bufferedReader.readLine();
				//if (line.equalsIgnoreCase("close"))
				//	break;

				//byte[] buffer = mCameraPreview.getBuffer();
				//ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
				//PrintStream printStream = new PrintStream(outputStream);
				//byte[] sendBuffer = new byte[1024];
				//int length = 0;
				//while ((length = byteArrayInputStream.read(sendBuffer, 0, sendBuffer.length)) > 0) {
				//	printStream.write(sendBuffer, 0, length);
				//	printStream.flush();
				//}
				//
				//socket.shutdownOutput();
				Log.i(TAG, "---> server send out");

			} catch (IOException e) {
				e.printStackTrace();
				if (mHandler != null)
					mHandler.sendEmptyMessage(DISCONNECTED);
			} finally {
				try {
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
