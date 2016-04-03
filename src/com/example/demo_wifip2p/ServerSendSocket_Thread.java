package com.example.demo_wifip2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ServerSendSocket_Thread implements Runnable{
	
	public static final int PORT = 9001;
	
	ServerSocket serverSendSocket;
	
	ServerSocketListener mListener;
	
	Thread t;

	public ServerSendSocket_Thread(ServerSocketListener svSocListener) throws IOException {
		// TODO Auto-generated constructor stub
		mListener = svSocListener;
		
		serverSendSocket = new ServerSocket(PORT);
	}
	
	public interface ServerSocketListener{
		public void onReceive_SendSocket(Socket sendSocket);
		public void onReceive_ReceiveSocket(Socket receiveSocket);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true){
			try {
				Socket sendSocket = serverSendSocket.accept();
				//Toast.makeText(mContext, "onConnect", Toast.LENGTH_SHORT).show();
				mListener.onReceive_SendSocket(sendSocket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//Toast.makeText(mContext, "ServerSend: Error of accept", Toast.LENGTH_SHORT).show();
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
