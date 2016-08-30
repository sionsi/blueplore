package com.example.blueplore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.blueplore.MainActivity.PlaceholderFragment;

public class PhyTestFragment extends PlaceholderFragment {

    private static final String TAG = "PhyTestFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    
    public TextView mCurrentPhyTextView;
    public TextView mCurrentChannelTextView;
    public TextView mGattConnectionStatus;
    
    private Spinner mSetPhyTypeSpinner;
    private ListView mConnectedDeviceListView;
    
    private ArrayList<BlePhyTestConfig> deviceList;
    
    private List<Map <String,Object> >  mInfos;
    
    String[] mPhyTypeSpinnerValue = { "Choose", "1M", "2M", "coded(s=2)", "coded(s=8)" };
    
    private String[] info_Address={"40:45:32:00:01:02","40:45:32:00:01:02","40:45:32:00:01:02"};
    private String[] info_Name={"aa","bb","cc"};
    private String[] info_PhyStatus={"tx, rx","tx, rx","tx, rx"};
    
    private String[] item_name = {"address", "name", "phy"};
    private int[] item_resource = {R.id.phy_list_device_addr, R.id.phy_list_device_name, R.id.phy_list_current_phy};
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mMainActivity = (MainActivity)getActivity();
		mView = inflater.inflate(R.layout.section5phy, container, false);
		
		mGattConnectionStatus = (TextView)mView.findViewById(R.id.section5_connection_status);
		mCurrentPhyTextView = (TextView)mView.findViewById(R.id.section5_current_phy);
		mSetPhyTypeSpinner = (Spinner)mView.findViewById(R.id.section5_set_phy_spinner);
		
		mConnectedDeviceListView = (ListView)mView.findViewById(R.id.section5_device_list_view);
		deviceList = new ArrayList<BlePhyTestConfig>();
		
		mInfos= new ArrayList <Map <String,Object> >();
		
		for(int i = 0; i < info_Address.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(item_name[0], info_Address[i]);
			map.put(item_name[1], info_Name[i]);
			map.put(item_name[2], info_PhyStatus[i]);
			mInfos.add(map);
		}


		SimpleAdapter adapter = new SimpleAdapter(getActivity(), mInfos, R.layout.phy_list,
				                                  item_name, item_resource);

		mConnectedDeviceListView.setAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
    public void setPhy(BluetoothDevice dev, int tx, int rx) {
    	Log.d(TAG, "set " + dev.getAddress().toString() +" phy: tx=" + tx + "; rx=" + rx);
    }
    
    public void getPhy(BluetoothDevice dev) {
    	Log.d(TAG, "get phy:");
    }

}
