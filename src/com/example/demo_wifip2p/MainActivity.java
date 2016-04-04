package com.example.demo_wifip2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import com.example.demo_wifip2p.ReceiveSocketAsync.SocketReceiverDataListener;
import com.example.demo_wifip2p.ServerSendSocket_Thread.ServerSocketListener;
import com.example.demo_wifip2p.WifiP2PBroardcast.WifiP2PBroadcastListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements SocketReceiverDataListener, WifiP2PBroadcastListener{
	
	public static enum State{
		StateDefault,
		StateActive,
		StatePassive
	}

	Context mContext;
	
	public static WifiP2pManager mManager;
	public static Channel mChannel;
	public static WifiP2PBroardcast mBroadcast;
	public static IntentFilter filter = new IntentFilter();

	ListView lvImage;
	ImageListAdapter lvImageAdapter;
	List<String> image_urls = new ArrayList<String>();
	
	public boolean firstDiscover = true;
	ProgressDialog mProgess;
	public static State mState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		mProgess = new ProgressDialog(this){
			@Override
			public void onBackPressed() {
				if (mProgess.isShowing()){
					mProgess.dismiss();
				}
			};
		};
		
		mState = State.StateDefault;
		
		lvImage = (ListView)findViewById(R.id.lvImage);
		lvImageAdapter = new ImageListAdapter(this, R.layout.listview_item, image_urls);
		lvImage.setAdapter(lvImageAdapter);
		lvImage.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent imageViewIntent = new Intent(getApplicationContext(), ShowImageActivity.class);
				imageViewIntent.putExtra("ImagePath", image_urls.get(position));
				startActivity(imageViewIntent);
			}
		});
		
		setManager();
	}
	
	private void setManager(){			
		mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
		
		mChannel = mManager.initialize(this, getMainLooper(), null);
		
		advertiseWifiP2P();
		
		mBroadcast = new WifiP2PBroardcast(mManager, mChannel, this);
		
		mBroadcast.mListener = this;
		
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		
		registerReceiver(mBroadcast, filter);
	}
	
	private void advertiseWifiP2P(){
		mManager.discoverPeers(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public class ImageListAdapter extends ArrayAdapter<String>{
		Context mContext;
		int mResource;
		List<String> image_urls;

		public ImageListAdapter(Context context, int resource,
				List<String> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			mContext = context;
			mResource = resource;
			image_urls = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = convertView;
			LayoutInflater inflater = getLayoutInflater();
			v = inflater.inflate(mResource, null);
			ImageView imageView = (ImageView)v.findViewById(R.id.imvIcon);
			TextView txtView = (TextView)v.findViewById(R.id.tvLabel);
			txtView.setText("image_" + position);
			Bitmap bm = BitmapFactory.decodeFile(image_urls.get(position));
			imageView.setImageBitmap(bm);
			return v;
		}
	}

	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	
	
	
	private void sendImageCapture(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.setType("image/*");
        startActivityForResult(intent, 100);
	}
	
	private void sendImageInGalery(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		if (requestCode == 100 || resultCode == RESULT_OK){
			final Bitmap bm = (Bitmap)data.getExtras().get("data");
			
			ByteArrayOutputStream osBm = new ByteArrayOutputStream();

			bm.compress(CompressFormat.PNG, 0, osBm);
			
			byte[] barray = osBm.toByteArray();
			
			ByteArrayInputStream isBm = new ByteArrayInputStream(osBm.toByteArray());
			
			mProgess.setTitle("Send Picture");
			mProgess.setMessage("Wait for send...");
			mProgess.show();
			
			mBroadcast.send(isBm);
			//mBroadcast.disconnect();
			
			mProgess.dismiss();
			advertiseWifiP2P();
			
		}else if (requestCode == 20 || resultCode == RESULT_OK){
			
			mBroadcast.mListener = this;
			
			if (mState == State.StateActive){
				mProgess.setTitle("Receive Picture");
				mProgess.setMessage("Wait for picture...");
				mProgess.show();
			}else if (mState == State.StatePassive){
				sendImageCapture();
			}
			
			mState = State.StateDefault;
		}
	}

	@Override
	public void onReceiveData(String imagePath) {
		Handler hd = new Handler(mContext.getMainLooper());
		image_urls.add(imagePath);
		hd.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mProgess.isShowing()){
					mProgess.dismiss();
				}
				lvImageAdapter.notifyDataSetChanged();
			}
		});
		mBroadcast.disconnect();
		advertiseWifiP2P();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflate = getMenuInflater();
		inflate.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.mItem_Browser){
			Intent intent = new Intent(this, BrowserActivity.class);
			startActivityForResult(intent, 20);
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnection() {
		// TODO Auto-generated method stub
		if (mState == State.StatePassive){
			sendImageCapture();
		}
	}
}
