package com.d2.NSD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.d2.NSD.activity.ClientActivity;
import com.d2.NSD.activity.ServerActivity;

public class MyActivity extends Activity implements View.OnClickListener{

	private String TAG = MyActivity.class.getSimpleName();

	private Button btn_server;
	private Button btn_client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		init();
	}

	private void init() {
		btn_server = (Button) findViewById(R.id.btn_server);
		btn_client = (Button) findViewById(R.id.btn_client);

		btn_server.setOnClickListener(this);
		btn_client.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		int id = view.getId();
		if (id == R.id.btn_server) {
			intent.setClass(this, ServerActivity.class);
		}
		else if (id == R.id.btn_client) {
			intent.setClass(this, ClientActivity.class);
		}

		startActivity(intent);
	}
}
