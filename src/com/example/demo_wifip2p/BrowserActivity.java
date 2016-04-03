package com.example.demo_wifip2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo_wifip2p.WifiP2PBroardcast.WifiP2PBrooadcastListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BrowserActivity extends Activity implements WifiP2PBrooadcastListener{
	
	DeviceListAdapter deviceListAdapter;
	ListView lvDevice;
	List<WifiP2pDevice> mPeerList = new ArrayList<WifiP2pDevice>();
	
	String DETECT = "Demo_WifiP2P";
	String STATEDETECT = "ON";
	
	WifiP2PBroardcast mBroadcast;
	IntentFilter filter = new IntentFilter();
	
	WifiP2pManager mManager;
	Channel mChannel;
	WifiP2pDnsSdServiceInfo serviceInfo;
	WifiP2pDnsSdServiceRequest serviceRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browserpeer_activity);
		
		mManager = MainActivity.mManager;
		mChannel = MainActivity.mChannel;
		mBroadcast = MainActivity.mBroadcast;
		mBroadcast.mListener = this;
		filter = MainActivity.filter;
		
		lvDevice = (ListView)findViewById(R.id.lvDevice);
		
		deviceListAdapter = new DeviceListAdapter(this, R.layout.listview_item, mPeerList);
		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				connectToPeer(position);
			}
		});
		
		startRegistration();
		searchPeer();
	}
	
	
	public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice>{

		Context mContext;
		int mResource;
		List <WifiP2pDevice> mList;
		
		public DeviceListAdapter(Context context, int resource,
				List<WifiP2pDevice> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			
			mContext = context;
			mResource = resource;
			mList = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			View v = convertView;
			
			LayoutInflater inflater = getLayoutInflater();
			v = inflater.inflate(mResource, null);
			
			TextView tvDeviceName = (TextView)v.findViewById(R.id.tvLabel);
			tvDeviceName.setText(mList.get(position).deviceName);
			
			return v; 
		}
		
	}

	private void showPeer(WifiP2pDevice device){	
		if (!mPeerList.contains(device)){
			mPeerList.add(device);
			deviceListAdapter.notifyDataSetChanged();
		}
	}
	
	private void startRegistration(){

       // Service information.  Pass it an instance name, service type
       // _protocol._transportlayer , and the map containing
       // information other devices will want once they connect to this one.
       serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(DETECT, STATEDETECT, null);

       // Add the local service, sending the service info, network channel,
       // and listener that will be used to indicate success or failure of
       // the request.
       mManager.clearLocalServices(mChannel, new ActionListener() {
		
		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
		           @Override
		           public void onSuccess() {
		        	   Handler advertise = new Handler();
		        	   advertise.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
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
					}, 1000);
		           }

		           @Override
		           public void onFailure(int arg0) {
		               // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
		           }
		       });
		}
		
		@Override
		public void onFailure(int reason) {
			// TODO Auto-generated method stub
			
		}
       });
	}
	
	private void setServiceRequest(){
		
		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
	        @Override

	        public void onDnsSdTxtRecordAvailable(
	                String fullDomain, Map record, WifiP2pDevice device) {
	            }
	        };
	        
	        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
	            @Override
	            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
	                    WifiP2pDevice resourceType) {
	                            
	                    if (instanceName.equals(DETECT) && registrationType.equals("ON.local.")){
		                	//showPeer(resourceType);         
	                    }
	                    
	            }
	        };

	    mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
	    
	    serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
	}
	
	private void searchPeer(){	
		mPeerList.clear();
		deviceListAdapter.notifyDataSetChanged();
		
		discoverService();
	}
	
	
	
	private void discoverService(){
		
		setServiceRequest();
		
		mManager.removeServiceRequest(mChannel, serviceRequest , new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				mManager.addServiceRequest(mChannel, serviceRequest, new ActionListener() {
					
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						mManager.discoverServices(mChannel, new ActionListener() {
							
							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								Handler discover = new Handler();
								discover.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										discoverService();
									}
								}, 30000);
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
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void connectToPeer(int position){
		WifiP2pDevice device = mPeerList.get(position);
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 0;
		
		mManager.connect(mChannel, config, new ActionListener() {
			
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (serviceInfo != null){
			mManager.removeLocalService(mChannel, serviceInfo, new ActionListener() {
				
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
	}

	@Override
	public void onPeers(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub
		mPeerList.clear();
		mPeerList.addAll(peers.getDeviceList());
		boolean p = mPeerList.get(0).isServiceDiscoveryCapable();
		deviceListAdapter.notifyDataSetChanged();
	}
}
