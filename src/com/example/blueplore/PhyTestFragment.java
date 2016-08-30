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
    
    public SimpleAdapter mPhyListAdapter;
    public TextView mCurrentPhyTextView;
    public TextView mCurrentChannelTextView;
    public TextView mGattConnectionStatus;
    public phyTestAdapter mAdapter;
    
    private Spinner mSetPhyTypeSpinner;
    private ListView mConnectedDeviceListView;
    
    private String[] info_Address={"40:45:32:00:01:02","40:45:32:00:01:02","40:45:32:00:01:02"};
    private String[] info_Name={"aa","bb","cc"};
    private String[] info_PhyStatus={"tx, rx","tx, rx","tx, rx"};
    
    private String[] item_name = {"address", "name", "phy"};
    private int[] item_resource = {R.id.phy_list_device_addr, R.id.phy_list_device_name, R.id.phy_list_current_phy};
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sensen onCreateView");
		mMainActivity = (MainActivity)getActivity();
		mView = inflater.inflate(R.layout.section5phy, container, false);
		
		mGattConnectionStatus = (TextView)mView.findViewById(R.id.section5_connection_status);
		mCurrentPhyTextView = (TextView)mView.findViewById(R.id.section5_current_phy);
		
		mConnectedDeviceListView = (ListView)mView.findViewById(R.id.section5_device_list_view);

		mAdapter = new phyTestAdapter(mMainActivity, mMainActivity.leService.blePhyTestController.mPhyDevicetList);
		mConnectedDeviceListView.setAdapter(mAdapter);

		return mView;
	}
	
    public void setPhy(BluetoothDevice dev, int tx, int rx) {
    	Log.d(TAG, "set " + dev.getAddress().toString() +" phy: tx=" + tx + "; rx=" + rx);
    }
    
    public void getPhy(BluetoothDevice dev) {
    	Log.d(TAG, "get phy:");
    }
    
    
    public class phyTestAdapter extends ArrayAdapter<BlePhyTestConfig> {
        public phyTestAdapter(Context context, ArrayList<BlePhyTestConfig> users) {
           super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
           // Get the data item for this position
        	BlePhyTestConfig phy = getItem(position);    
           // Check if an existing view is being reused, otherwise inflate the view
           if (convertView == null) {
              convertView = LayoutInflater.from(getContext()).inflate(R.layout.phy_list, parent, false);
           }
           // Lookup view for data population
           TextView tvAddr = (TextView) convertView.findViewById(R.id.phy_list_device_addr);
           TextView tvName = (TextView) convertView.findViewById(R.id.phy_list_device_name);
           TextView tvPhy = (TextView) convertView.findViewById(R.id.phy_list_current_phy);
           // Populate the data into the template view using the data object
           tvAddr.setText(phy.getDevice().getAddress());
           tvName.setText(phy.getDevice().getName());
           tvPhy.setText("Tx PHY:" + phy.getTxPhy() + "    Rx PHY:" + phy.getRxPhy());
           // Return the completed view to render on screen
           return convertView;
       }
    }

}





