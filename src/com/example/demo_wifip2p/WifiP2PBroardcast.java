package com.example.demo_wifip2p;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.example.demo_wifip2p.MainActivity.State;
import com.example.demo_wifip2p.P2PHandleNetwork.P2PHandleNetworkListener;
import com.example.demo_wifip2p.ReceiveSocketAsync.SocketReceiverDataListener;
import com.example.demo_wifip2p.ServerSendSocket_Thread.ServerSocketListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class WifiP2PBroardcast extends BroadcastReceiver implements PeerListListener, P2PHandleNetworkListener{
	
	WifiP2pManager mManager;
	Channel mChannel;
	Context mContext;
	public WifiP2PBroadcastListener mListener = null;
	P2PHandleNetwork mP2PHandle;
	
	public WifiP2PBroardcast(WifiP2pManager manager, Channel channel, MainActivity activity){
		mManager = manager;
		mChannel = channel;
		mContext = activity;
		
		mP2PHandle = new P2PHandleNetwork(mContext);
		mP2PHandle.mListener = this;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        	
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        	if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
        		
        	}
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        	Toast.makeText(mContext.getApplicationContext(), "Peer change", Toast.LENGTH_SHORT).show();
        	mManager.requestPeers(mChannel, this);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        	NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	Toast.makeText(mContext.getApplicationContext(), "Connection change", Toast.LENGTH_SHORT).show();
        	if (networkInfo.isConnected()){
        		
            	if (MainActivity.mState == State.StateDefault){
            		MainActivity.mState = State.StatePassive;
            	}
            	
            	
        		mManager.requestConnectionInfo(mChannel, (ConnectionInfoListener)mP2PHandle); 		
        	}
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        	Toast.makeText(mContext.getApplicationContext(), "Device change", Toast.LENGTH_SHORT).show();
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
        	//Toast.makeText(mActitity, "Discovery start", Toast.LENGTH_SHORT).show();
        	
        	int discoverState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
        	
        	if (discoverState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
        		Toast.makeText(mContext.getApplicationContext(), "Discovery start", Toast.LENGTH_SHORT).show();
        	}else if (discoverState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){		
        		Toast.makeText(mContext.getApplicationContext(), "Discovery stop", Toast.LENGTH_SHORT).show();
        	}
        }
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub
		if (mListener != null){
			mListener.onPeers(peers);
		}
	}
	
	public void send(ByteArrayInputStream is){
		mP2PHandle.send(is);
	}
	
	public void disconnect(){
		mP2PHandle.disconnect();
		mManager.removeGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				mManager.cancelConnect(mChannel, new ActionListener() {
					
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
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public interface WifiP2PBroadcastListener{
		public void onPeers(WifiP2pDeviceList peers);
		public void onConnection();
	}

	@Override
	public void onConnectComplete() {
		// TODO Auto-generated method stub
		if (mListener != null){
			mListener.onConnection();
		} 
	}
}


