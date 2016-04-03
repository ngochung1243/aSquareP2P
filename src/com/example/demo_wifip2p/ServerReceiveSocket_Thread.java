package com.example.demo_wifip2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.demo_wifip2p.ServerSendSocket_Thread.ServerSocketListener;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ServerReceiveSocket_Thread implements Runnable{

	public static final int PORT = 9000;
	
	boolean flag = false;
	Thread t;
	
	ServerSocket mServerSocket;
	
	ServerSocketListener mListener;
	
	public ServerReceiveSocket_Thread(ServerSocketListener svListener) throws IOException{
		mServerSocket = new ServerSocket(PORT);
		mListener = svListener;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket receiveSocket;
		while (true){
			try {
				receiveSocket = mServerSocket.accept();
				mListener.onReceive_ReceiveSocket(receiveSocket);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}
	
	public void start(){
		t = new Thread(this);
		t.start();
	}
	
	public void stop(){
		t.interrupt();
	}
}
