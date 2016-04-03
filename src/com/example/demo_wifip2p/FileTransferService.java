package com.example.demo_wifip2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class FileTransferService {

	public static boolean copyFile(final InputStream inputStream, final OutputStream out) {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				byte buf[] = new byte[1024];
		        int len;
		        
		        try {
		            while ((len = inputStream.read(buf)) != -1) {
		                out.write(buf, 0, len);
		            }
		            
		            inputStream.close();
		            out.close();
		            
		        } catch (IOException e) {
		            
		        }
			}
		};
        Thread t = new Thread(runnable);
        t.start();
        
        return true;
    }
}
