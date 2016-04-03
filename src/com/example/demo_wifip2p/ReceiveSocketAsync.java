package com.example.demo_wifip2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class ReceiveSocketAsync implements Runnable{
	
	static final int PORT = 9000;
	
	Socket mReceiveSocket;
	
	SocketReceiverDataListener mReceiveListener;
	
	Context mContext;
	
	Thread t;
	
	public ReceiveSocketAsync(SocketReceiverDataListener receiveListener, Socket receiveSocket, Context context) {
		// TODO Auto-generated constructor stub
		mReceiveSocket = receiveSocket;
		mReceiveListener = receiveListener;
		mContext = context;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			InputStream receiveInputStream = mReceiveSocket.getInputStream();
			while (true){
				
				final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + mContext.getPackageName() + "/wifip2pImageShare-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();
                
                FileTransferService.copyFile(receiveInputStream, new FileOutputStream(f));
                ((SocketReceiverDataListener)mContext).onReceiveData(f.getAbsolutePath());
                break;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start(){
		t = new Thread(this);
		t.start();
	}
	
	public void stop(){
		t.interrupt();
	}
	
	public interface SocketReceiverDataListener{
		public void onReceiveData(String imagePath);
	}
}
