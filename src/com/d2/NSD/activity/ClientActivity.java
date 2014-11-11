package com.d2.NSD.activity;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.d2.NSD.R;

import java.net.InetAddress;
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
				InetAddress host = nsdServiceInfo.getHost();
				Log.i(TAG, "---> port: " + port + " / ip address: " + host);
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
}
