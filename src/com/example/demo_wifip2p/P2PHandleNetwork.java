package com.example.demo_wifip2p;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.example.demo_wifip2p.ReceiveSocketAsync.SocketReceiverDataListener;
import com.example.demo_wifip2p.ServerSendSocket_Thread.ServerSocketListener;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.Toast;

public class P2PHandleNetwork implements ConnectionInfoListener, ServerSocketListener, PeerListListener {

	private static final int SOCKET_TIMEOUT = 5000;
	
	Context mContext;
	
	ServerReceiveSocket_Thread serverReceiveThread;
	ServerSendSocket_Thread serverSendThread;
	Socket mSendSocket;
	Socket mReceiveSocket;
	
	public P2PHandleNetwork(Context context){
		mContext = context;
	}
	
	public void send(ByteArrayInputStream is){
		try {
			OutputStream os = mSendSocket.getOutputStream();
			FileTransferService.copyFile(is, os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		if (info.groupOwnerAddress == null){
			return;
		}
		final String hostIP = info.groupOwnerAddress.getHostAddress();
		
		if (info.groupFormed){
			if (info.isGroupOwner){
				try {
					if (serverSendThread == null && serverReceiveThread == null){
						serverReceiveThread = new ServerReceiveSocket_Thread((ServerSocketListener)mContext);
						serverReceiveThread.start();
						
						serverSendThread = new ServerSendSocket_Thread((ServerSocketListener)mContext);
						serverSendThread.start();
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(mContext.getApplicationContext(), "Error of create", Toast.LENGTH_SHORT).show();
				}
			}else {
				if (mSendSocket == null && mReceiveSocket == null){
					mSendSocket = new Socket();
					mReceiveSocket = new Socket();
						
					Runnable runnable = new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								mReceiveSocket.bind(null);
								mReceiveSocket.connect(new InetSocketAddress(hostIP, ServerSendSocket_Thread.PORT), SOCKET_TIMEOUT);
								
								mSendSocket.bind(null);
								mSendSocket.connect(new InetSocketAddress(hostIP, ServerReceiveSocket_Thread.PORT), SOCKET_TIMEOUT);
								ReceiveSocketAsync receiveThread = new ReceiveSocketAsync((SocketReceiverDataListener)mContext, mReceiveSocket, mContext);
								receiveThread.start();
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								
							}

						}
					};
					Thread connect = new Thread(runnable);
					connect.start();
				}
			}
		}
	}
	
	@Override
	public void onReceive_SendSocket(Socket sendSocket) {
		// TODO Auto-generated method stub
		mSendSocket = sendSocket;
	}

	@Override
	public void onReceive_ReceiveSocket(Socket receiveSocket) {
		// TODO Auto-generated method stub
		mReceiveSocket = receiveSocket;
		ReceiveSocketAsync receiveThread = new ReceiveSocketAsync((SocketReceiverDataListener)mContext, receiveSocket, mContext);
		receiveThread.start();
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub
		
	}
}
