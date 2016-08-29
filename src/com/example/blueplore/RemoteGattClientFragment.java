package com.example.blueplore;



import java.util.List;

import com.example.blueplore.MainActivity.PlaceholderFragment;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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



//section4
public class RemoteGattClientFragment extends PlaceholderFragment {
    private static final String TAG = "RemoteGattClientFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    public RemoteGattClientAdapter mAdapter;
    private ListView mListView;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        mView = inflater.inflate(R.layout.section4, container, false);
        
        mAdapter = new RemoteGattClientAdapter(mMainActivity, android.R.layout.simple_list_item_2, mMainActivity.leService.le.remoteGattClientList);
        mListView = (ListView)mView.findViewById(R.id.section4_connected_remote_gatt_client_device_listview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new ListViewOnItemClickListener());
        
        return mView;
    }
    
    class ListViewOnItemClickListener implements ListView.OnItemClickListener {

        LeDevice leDevice;
        int whichSingleChoiceItem = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            
            whichSingleChoiceItem = 0;
            
            leDevice = mAdapter.getItem(position);
            if (null == leDevice) {
                Toast.makeText(mMainActivity, "can not get device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "can not get device:" + leDevice.name + " " + leDevice.address);  
                return;
            }
            
            AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
            dialog.setTitle(leDevice.name + "\n" + leDevice.address);
            dialog.setSingleChoiceItems(new String[] {"Send Notification", "Disconnect"}, 0, new DialogInterface.OnClickListener() {
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
                        transaction.replace(R.id.container, new NotificationSendFragment(leDevice));
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 1:
                        leDevice.gattServerDisconnect();
                        break;
                    default:
                        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                        Log.e(TAG, "whichSingleChoiceItem=" + whichSingleChoiceItem);
                    }
                }
            });
            dialog.setNegativeButton("Cancel", null);
            dialog.show();
        }
    }
    
    
    class RemoteGattClientAdapter extends ArrayAdapter<LeDevice> {
        private List<LeDevice> mList;
        private Context mContext;
        
        public RemoteGattClientAdapter(Context context, int resource,
                List<LeDevice> objects) {
            super(context, resource, objects);
            mContext = context;
            mList = objects;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public LeDevice getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = null;
            LeDevice leDevice = getItem(position);
            
            if (null == convertView) {
                textView = new TextView(mContext);
            } else {
                textView = (TextView) convertView; 
            }
            
            textView.setText(leDevice.name + "\n" + leDevice.address);
            textView.setTextSize(25);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            return textView;
        }

        @Override
        public int getItemViewType(int position) {
            // TODO Auto-generated method stub
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            // TODO Auto-generated method stub
            return super.getViewTypeCount();
        }
        
    }
    
}
