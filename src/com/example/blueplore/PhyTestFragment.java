package com.example.blueplore;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
           Spinner snTxPhy = (Spinner)convertView.findViewById(R.id.phy_list_tx_phy);
           Spinner snRxPhy = (Spinner)convertView.findViewById(R.id.phy_list_rx_phy);
           Button bnSetPhy = (Button)convertView.findViewById(R.id.phy_list_set_phy);
           Button bnGetPhy = (Button)convertView.findViewById(R.id.phy_list_get_phy);
           
           // Populate the data into the template view using the data object
           tvAddr.setText(phy.getDevice().getAddress());
           tvName.setText(phy.getDevice().getName());
           tvPhy.setText("Tx PHY:" + phy.getTxPhy() + "    Rx PHY:" + phy.getRxPhy());
           bnSetPhy.setTag(position);
           bnGetPhy.setTag(position);
           snTxPhy.setTag(position);
           snRxPhy.setTag(position);
           
           
           snTxPhy.setOnItemSelectedListener(new OnItemSelectedListener(){
        	   int listPosition = 0;

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					//listPosition = Integer.parseInt(view.getTag().toString());
					Log.d(TAG, "sensen To Set Tx Phy=" + position);
					mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(listPosition).setToSetTxPhy(position);
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					Log.d(TAG, "noting selected");
				}
           });
           
           
           snRxPhy.setOnItemSelectedListener(new OnItemSelectedListener(){
        	   int listPosition = 0;

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					//listPosition = (Integer) view.getTag();
					Log.d(TAG, "sensen To Set Rx Phy=" + position);
					BluetoothDevice dev = mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(position).getDevice();
					//mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(listPosition).setToSetRxPhy(position);
					mMainActivity.leService.blePhyTestController.getPhy(dev);
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					Log.d(TAG, "noting selected");
				}
           });
           bnSetPhy.setOnClickListener(new View.OnClickListener() {
			
        	   @Override
			   public void onClick(View v) {
        		   // TODO Auto-generated method stub
        		   int position = (Integer) v.getTag();
        		   BluetoothDevice dev = mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(position).getDevice();
        		   int tx = mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(position).getToSetTxPhy();
        		   int rx = mMainActivity.leService.blePhyTestController.mPhyDevicetList.get(position).getToSetRxPhy();

        		   mMainActivity.leService.blePhyTestController.setPhy(dev, tx, rx);
        		   
        	   }
			});
           // Return the completed view to render on screen
           return convertView;
        }
         
    }

}





