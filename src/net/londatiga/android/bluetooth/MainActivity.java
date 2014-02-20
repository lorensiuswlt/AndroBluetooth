package net.londatiga.android.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.app.ProgressDialog;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Main activity.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class MainActivity extends Activity {
	private TextView mStatusTv;
	private Button mActivateBtn;
	private Button mPairedBtn;
	private Button mScanBtn;
	
	private ProgressDialog mProgressDlg;
	
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	
	private BluetoothAdapter mBluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mStatusTv 			= (TextView) findViewById(R.id.tv_status);
		mActivateBtn 		= (Button) findViewById(R.id.btn_enable);
		mPairedBtn 			= (Button) findViewById(R.id.btn_view_paired);
		mScanBtn 			= (Button) findViewById(R.id.btn_scan);
		
		mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
		
		mProgressDlg 		= new ProgressDialog(this);
		
		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        
		        mBluetoothAdapter.cancelDiscovery();
		    }
		});
		
		if (mBluetoothAdapter == null) {
			showUnsupported();
		} else {
			mPairedBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
					
					if (pairedDevices == null || pairedDevices.size() == 0) { 
						showToast("No Paired Devices Found");
					} else {
						ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
						
						list.addAll(pairedDevices);
						
						Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
						
						intent.putParcelableArrayListExtra("device.list", list);
						
						startActivity(intent);						
					}
				}
			});
			
			mScanBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					mBluetoothAdapter.startDiscovery();
				}
			});
			
			mActivateBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					if (mBluetoothAdapter.isEnabled()) {
						mBluetoothAdapter.disable();
						
						showDisabled();
					} else {
						Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						
					    startActivityForResult(intent, 1000);
					}
				}
			});
			
			if (mBluetoothAdapter.isEnabled()) {
				showEnabled();
			} else {
				showDisabled();
			}
		}
		
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onPause() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}
	
	private void showEnabled() {
		mStatusTv.setText("Bluetooth is On");
		mStatusTv.setTextColor(Color.BLUE);
		
		mActivateBtn.setText("Disable");		
		mActivateBtn.setEnabled(true);
		
		mPairedBtn.setEnabled(true);
		mScanBtn.setEnabled(true);
	}
	
	private void showDisabled() {
		mStatusTv.setText("Bluetooth is Off");
		mStatusTv.setTextColor(Color.RED);
		
		mActivateBtn.setText("Enable");
		mActivateBtn.setEnabled(true);
		
		mPairedBtn.setEnabled(false);
		mScanBtn.setEnabled(false);
	}
	
	private void showUnsupported() {
		mStatusTv.setText("Bluetooth is unsupported by this device");
		
		mActivateBtn.setText("Enable");
		mActivateBtn.setEnabled(false);
		
		mPairedBtn.setEnabled(false);
		mScanBtn.setEnabled(false);
	}
	
	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {	    	
	        String action = intent.getAction();
	        
	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	        	 
	        	if (state == BluetoothAdapter.STATE_ON) {
	        		showToast("Enabled");
	        		 
	        		showEnabled();
	        	 }
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	mDeviceList = new ArrayList<BluetoothDevice>();
				
				mProgressDlg.show();
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	mProgressDlg.dismiss();
	        	
	        	Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
	        	
	        	newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
				
				startActivity(newIntent);
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        
	        	mDeviceList.add(device);
	        	
	        	showToast("Found device " + device.getName());
	        }
	    }
	};

}