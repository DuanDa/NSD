package com.d2.NSD.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.d2.NSD.R;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ClientActivity extends AbstractActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
	private final String TAG = ClientActivity.class.getSimpleName();
	private final static int CONNECTED = 1;
	private final static int DISCONNECTED = -1;

	private List<String> devices = new ArrayList<String>();

	private ListView lv_devices;
	private TextView tv_connection_status;
	private Button btn_connect;
	private ImageView iv;
	private ArrayAdapter<String> adapter;
	private Handler mHandler;
	private boolean appClose;

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

		//new Thread(new Runnable() {
		//	@Override
		//	public void run() {
		//		sniffDevices();
		//	}
		//}).start();

		//sendTestToServer();
	}

	@Override
	protected void onStop() {
		super.onStop();

		appClose = true;
	}

	private void init() {
		// init Handler
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				if (data != null) {
					//devices.add(data.getString("IP_ADDRESS") + " (" + data.getString("HOST_NAME") + ")");
					//adapter.notifyDataSetChanged();

					if (msg.what == CONNECTED) {
						tv_connection_status.setText(String.format("%s is connected", data.getString("IP_ADDRESS")));
						tv_connection_status.setTextColor(Color.GREEN);
					}
					else if (msg.what == DISCONNECTED) {
						tv_connection_status.setText("Connection failed");
						tv_connection_status.setTextColor(Color.RED);
					}

					byte[] bytes = data.getByteArray("BYTE");
					if (bytes != null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
						iv.setImageBitmap(bitmap);
					}
				}
			}
		};

		// init ListView adapter
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);

		// init UI resources
		lv_devices = (ListView) findViewById(R.id.lv_devices);
		lv_devices.setOnItemClickListener(this);
		lv_devices.setAdapter(adapter);
		findViewById(R.id.btn_connect).setOnClickListener(this);
		iv = (ImageView) findViewById(R.id.iv);
		tv_connection_status = (TextView) findViewById(R.id.tv_connection_status);
	}

	private void sniffDevices() {
		byte[] ip_address = null;
		Enumeration<NetworkInterface> nets = null;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netIf : Collections.list(nets)) {
				System.out.println("--->");
				System.out.printf("Display name: %s\n", netIf.getDisplayName());
				System.out.printf("Name: %s\n", netIf.getName());
				Enumeration<InetAddress> inetAddresses = netIf.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					byte[] address = inetAddress.getAddress();
					System.out.println("address: " + inetAddress.getHostAddress());
					if (address.length == 4) {
						ip_address = address;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		for (int i = 1; i <= 254; i++) {
			ip_address[3] = (byte) i;
			InetAddress byAddress = null;
			try {
				byAddress = InetAddress.getByAddress(ip_address);
				if (byAddress.isReachable(1000)) {
					System.out.println("---> ip: " + byAddress.getHostAddress() + " / name: " + byAddress.getHostName());
					Bundle bundle = new Bundle();
					bundle.putString("IP_ADDRESS", byAddress.getHostAddress());
					bundle.putString("HOST_NAME", byAddress.getHostName());
					Message message = new Message();
					message.setData(bundle);
					mHandler.sendMessage(message);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendTestToServer(InetAddress inetAddress, int port) {
		Log.i(TAG, "---> sendTestToServer");
		String text = "Client says hello";
		String line = null;
		StringBuilder builder = new StringBuilder();

		// 创建一个套接字
		Socket socket = null;
		try {
			// 创建一个套接字并将其指定到Server IP地址
			socket = new Socket(inetAddress.getHostAddress(), port);
			socket.setSoTimeout(60000);

			// Get inputstream and outputstream from connected socket
			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();

			// Show connection status


			ServerSocket serverSocket = new ServerSocket(2015);
			Socket accept = serverSocket.accept();
			System.out.println("---> client received:");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
			String result = null;
			while ((result = bufferedReader.readLine()) != null) {
				System.out.println("---> client received:" + result);
			}

			// Get input stream from socket
			BufferedReader bufferedReaderFromServer = new BufferedReader(new InputStreamReader(inputStream));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			//line = null;
			//StringBuilder stringBuilder = new StringBuilder();
			//while ((line = bufferedReaderFromServer.readLine()) != null) {
			//	stringBuilder.append(line);
			//	Bundle bundle = new Bundle();
			//	bundle.putString("MESSAGE", stringBuilder.toString());
			//	Message message = new Message();
			//	message.setData(bundle);
			//	mHandler.sendMessage(message);
			//}

			int count = 1;
			while (true) {
				inputStream = socket.getInputStream();



				if (count++ % 1000 == 0) {
					Thread.sleep(500);
					break;
				}
			}

			bufferedReaderFromServer.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void connectServer(InetAddress inetAddress, int port) {
		while (true) {
			if (appClose)
				break;

			try {
				Log.i(TAG, "---> client connected");
				Socket socket = new Socket(inetAddress.getHostAddress(), port);
				socket.setSoTimeout(60000);

				OutputStream outputStream = socket.getOutputStream();
				InputStream inputStream = socket.getInputStream();

				//Log.i(TAG, "---> client connected");
				// Connected!
				Bundle bundle = new Bundle();
				bundle.putString("IP_ADDRESS", inetAddress.getHostAddress());
				Message message = new Message();
				message.what = CONNECTED;
				message.setData(bundle);
				mHandler.sendMessage(message);

				// Sending connecting status to server
				PrintWriter printWriter = new PrintWriter(outputStream, true);
				printWriter.println("connecting");

				// Receiving resources from server
				//ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				//byte[] buffer = new byte[1024];
				//int length = 0;
				//while ((length = inputStream.read(buffer, 0, buffer.length)) > 0) {
				//	byteArrayOutputStream.write(buffer, 0, length);
				//}
				//
				//byteArrayOutputStream.flush();
				//byte[] data = byteArrayOutputStream.toByteArray();
				//Log.i(TAG, "---> get data: " + data.length);
				//bundle = new Bundle();
				//bundle.putByteArray("BYTE", data);
				//message = new Message();
				//message.setData(bundle);
				//mHandler.sendMessage(message);

				Log.i(TAG, "---> client update image");
				Thread.sleep(500);
			} catch (IOException e) {
				e.printStackTrace();

				// Disconnected!
				if (mHandler != null)
					mHandler.sendEmptyMessage(DISCONNECTED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		String ip_server = "10.0.11.249";
		try {
			final InetAddress inetAddress = InetAddress.getByName(ip_server);
			new Thread(new Runnable() {
				@Override
				public void run() {
					sendTestToServer(inetAddress, 2014);
				}
			}).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.btn_connect) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String ip_server = "10.0.11.249";
					try {
						final InetAddress inetAddress = InetAddress.getByName(ip_server);
						//sendTestToServer(inetAddress, 2014);
						connectServer(inetAddress, 2014);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
}
