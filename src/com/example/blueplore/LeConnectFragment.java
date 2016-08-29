package com.example.blueplore;

import java.util.List;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blueplore.MainActivity.PlaceholderFragment;





//section1
public class LeConnectFragment extends PlaceholderFragment {

    private static final String TAG = "LeScanConnectFragment";
    
    private MainActivity mMainActivity;
    private View mView;
    private List<LeDevice> mDeviceList;
    public LeConnectFragmentAdapter mAdapter;
    
    private TextView mInfoTextView;
    public TextView mScanTextView;
    
    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        mMainActivity = (MainActivity)getActivity();
        mDeviceList = mMainActivity.leService.le.deviceList;
        
        mView = inflater.inflate(R.layout.section1, container, false);

        mInfoTextView = (TextView)mView.findViewById(R.id.section1_device_info_textView);
        mInfoTextView.setText("Local Name:" + BluetoothAdapter.getDefaultAdapter().getName() + "\n" + "Local Address:" + BluetoothAdapter.getDefaultAdapter().getAddress());
        
        mScanTextView = (TextView)mView.findViewById(R.id.section1_scan_textView);
        mScanTextView.setVisibility(View.GONE);
        
        mAdapter = new LeConnectFragmentAdapter(mMainActivity, android.R.layout.simple_list_item_1, mDeviceList);
        ListView listView = (ListView) mView.findViewById(R.id.section1_device_list_view);        
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());

        return mView;
    }
    
    class ListViewOnItemClickListener implements ListView.OnItemClickListener {

        LeDevice leDevice;
        int whichSingleChoiceItem;
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            
            whichSingleChoiceItem = 0;
            
            if (0 == position) {
                return;
            } else if ((position > 0) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1))) {
                leDevice = mDeviceList.get(position - 1);
                leDevice.unpair();
                return;//Macro.LeScanConnectViewTypePairedDevice
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1)) {
                return;
            } else if ((position > (mMainActivity.leService.le.pairedDeviceNum + 1)) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1))) {
                leDevice = mDeviceList.get(position - 2);
                if (null == leDevice) {
                    Toast.makeText(mMainActivity, "can not get device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    Log.e(TAG, "can not get device:" + leDevice.name + " " + leDevice.address);  
                    return;
                }
                
                AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
                dialog.setTitle(leDevice.name + "\n" + leDevice.address);
                dialog.setSingleChoiceItems(new String[] {"Receive Notification", "Disconnect From Gatt Server"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                        //Log.e(TAG, "which=" + which); 
                        //dialog.dismiss();
                        whichSingleChoiceItem = which;
                    }
                });
                
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (whichSingleChoiceItem) {
                        case 0:
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, new NotificationReceiveFragment(leDevice));
                            transaction.addToBackStack(null);
                            transaction.commit();
                            break;
                        case 1:
                            leDevice.gattClientDisconnect();
                            break;
                        default:
                            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                            Log.e(TAG, "whichSingleChoiceItem=" + whichSingleChoiceItem);
                        }
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();
                return;//Macro.LeScanConnectViewTypeConnectedDevice
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                return;
            } else if (position > (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                
                leDevice = mDeviceList.get(position - 3);
                
                AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
                dialog.setTitle(leDevice.name + "\n" + leDevice.address);
                dialog.setSingleChoiceItems(new String[] {"Connect to Gatt Server", "Pair"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                        //Log.e(TAG, "which=" + which); 
                        //dialog.dismiss();
                        whichSingleChoiceItem = which;
                    }
                });
                
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (whichSingleChoiceItem) {
                        case 0:
                            leDevice.gattClientConnect();
                            break;
                        case 1:
                            leDevice.pair(Macro.PIN);
                            break;
                        default:
                            Log.e(TAG, "whichSingleChoiceItem=" + whichSingleChoiceItem);
                        }
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();
                
                return;//Macro.LeScanConnectViewTypeScannedDevice
            } else {
                Log.e(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + ",position=" + position);
            }
            
        }
        
    }

    
    
    class LeConnectFragmentAdapter extends ArrayAdapter<LeDevice> {
    
        private List<LeDevice> mList;
        private Context mContext;
        
        public LeConnectFragmentAdapter(Context context, int resource,
                List<LeDevice> objects) {
            super(context, resource, objects);
            
            mContext = context;
            mList = objects;
        }
        
        @Override
        public int getCount() {
            return (mList.size() + 3);
        }
    
        @Override
        public LeDevice getItem(int position) {
            
            //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            //Log.d(TAG, "mList.size()=" + mList.size() + ",scannedDeviceNum=" + mMainActivity.ble.scannedDeviceNum + ",connectedDeviceNum=" + mMainActivity.ble.connectedDeviceNum + ",pairedDeviceNum=" + mMainActivity.ble.pairedDeviceNum);/*bt dbg*/
            //Log.e(TAG, "position=" + position); 
            
            if (0 == position) {
                return null;
            } else if ((position > 0) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1))) {
                return mList.get(position - 1);
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1)) {
                return null;
            } else if ((position > (mMainActivity.leService.le.pairedDeviceNum + 1)) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1))) {
                return mList.get(position - 2);
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                return null;
            } else if (position > (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                return mList.get(position - 3);
            } else {
                Log.e(TAG, "position=" + position);
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            
            //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            //Log.d(TAG, "mList.size()=" + mList.size() + ",scannedDeviceNum=" + mMainActivity.ble.scannedDeviceNum + ",connectedDeviceNum=" + mMainActivity.ble.connectedDeviceNum + ",pairedDeviceNum=" + mMainActivity.ble.pairedDeviceNum);/*bt dbg*/
            //Log.e(TAG, "position=" + position); 
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            
            //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            //Log.d(TAG, "mList.size()=" + mList.size() + ",scannedDeviceNum=" + mMainActivity.ble.scannedDeviceNum + ",connectedDeviceNum=" + mMainActivity.ble.connectedDeviceNum + ",pairedDeviceNum=" + mMainActivity.ble.pairedDeviceNum);/*bt dbg*/
            //Log.e(TAG, "position=" + position); 
            
            if (0 == position) {
                return Macro.LeScanConnectViewTypePairedText;
            } else if ((position > 0) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1))) {
                return Macro.LeScanConnectViewTypePairedDevice;
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1)) {
                return Macro.LeScanConnectViewTypeConnectedText;
            } else if ((position > (mMainActivity.leService.le.pairedDeviceNum + 1)) && (position < (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1))) {
                return Macro.LeScanConnectViewTypeConnectedDevice;
            } else if (position == (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                return Macro.LeScanConnectViewTypeScannedText;
            } else if (position > (mMainActivity.leService.le.pairedDeviceNum + 1 + mMainActivity.leService.le.connectedDeviceNum + 1)) {
                return Macro.LeScanConnectViewTypeScannedDevice;
            } else {
                Log.e(TAG, "position=" + position);
                return 0;
            }
        }
    
        @Override
        public int getViewTypeCount() {
            
            //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            //Log.d(TAG, "mList.size()=" + mList.size() + ",scannedDeviceNum=" + mMainActivity.ble.scannedDeviceNum + ",connectedDeviceNum=" + mMainActivity.ble.connectedDeviceNum + ",pairedDeviceNum=" + mMainActivity.ble.pairedDeviceNum);/*bt dbg*/
            
            return (Macro.LeScanConnectViewTypeMaxNum + 1);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.e(TAG, "position=" + position); 
            
            TextView textView = null;
            LeDevice leDevice = getItem(position);
            int type = getItemViewType(position);
            
            if (null == convertView) {
                textView = new TextView(mContext);
            } else {
                textView = (TextView) convertView; 
            }
            
            switch (type) {
            case Macro.LeScanConnectViewTypePairedText:
                textView.setText("Paired Device:");
                break;
            case Macro.LeScanConnectViewTypeConnectedText:
                textView.setText("Connected Device:");
                break;
            case Macro.LeScanConnectViewTypeScannedText:
                textView.setText("Scanned Device:");
                break;
            case Macro.LeScanConnectViewTypePairedDevice:
            case Macro.LeScanConnectViewTypeConnectedDevice:
            case Macro.LeScanConnectViewTypeScannedDevice:
                textView.setText(leDevice.name + "\n" + leDevice.address);
                textView.setTextSize(25);
                //textView.setHeight(200);
                textView.setGravity(Gravity.CENTER_VERTICAL);//¥π÷±æ”÷–
                //textView.setLines(2);
                break;
            default:
                Log.e(TAG, "type = "+ type);
            }
            
            //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            //Log.d(TAG, "mList.size()=" + mList.size() + ",scannedDeviceNum=" + mMainActivity.ble.scannedDeviceNum + ",connectedDeviceNum=" + mMainActivity.ble.connectedDeviceNum + ",pairedDeviceNum=" + mMainActivity.ble.pairedDeviceNum);/*bt dbg*/
            return textView;
        }
    
    }

}
