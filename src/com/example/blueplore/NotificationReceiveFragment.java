package com.example.blueplore;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class NotificationReceiveFragment extends Fragment {
    private static final String TAG = "NotificationReceiveFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    private TextView mDevInfoTextView;
    private TextView mPktTextView;
    private Button mShowRcvPktButton;//show last received packet
    private Button mMtuButton;
    
    private LeDevice mLeDevice;
    private String textStr;
    
    
    
    public NotificationReceiveFragment(LeDevice leDevice) {
        super();
        this.mLeDevice = leDevice;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        
        
        mView = inflater.inflate(R.layout.notification_receive, container, false);
        mDevInfoTextView = (TextView)mView.findViewById(R.id.notification_receive_device_info_textView);
        mPktTextView = (TextView)mView.findViewById(R.id.notification_receive_pkt_textView);
        mShowRcvPktButton = (Button)mView.findViewById(R.id.notification_receive_show_rcv_pkt_button);
        mMtuButton = (Button)mView.findViewById(R.id.notification_receive_mtu_button);
        
        mDevInfoTextView.setText("Received Notification From The Remote Gatt Server Device Name:  " + mLeDevice.name + "\n" + "Remote Device Address:" + "\n" + mLeDevice.address);
    
        
        mShowRcvPktButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textStr = "Att Mtu:" + mLeDevice.mtu + "\n" + "Rx notification count:" + mLeDevice.rxNotificationCnt + "\n" + "The last payload length:";
                
                if (null == mLeDevice.rxPayload) {
                    textStr += 0;
                } else {
                    textStr += mLeDevice.rxPayload.length;
                }
                
                textStr += "\n" + "\n" + Macro.bytesToHexString(mLeDevice.rxPayload);
                mPktTextView.setText(textStr);
            }
        });
    
        mMtuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(mMainActivity);
                View mtuView = layoutInflater.inflate(R.layout.dialog_number, null);
                final EditText mtuEditText = (EditText)mtuView.findViewById(R.id.dialog_num_edittext);
                mtuEditText.setHint(String.valueOf(Macro.PacketMaxLength));
                
                Dialog dialog = new AlertDialog.Builder(mMainActivity)
                .setTitle("mtu size")
                .setIcon(R.drawable.ic_launcher)
                .setView(mtuView)
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int mtuSize = Integer.parseInt(mtuEditText.getText().toString());
                        
                        //Toast.makeText(mMainActivity, "the remote device:" + mBleDevice.getName() + " " + mBleDevice.getAddress() + " mtu=" + mtuSize, Toast.LENGTH_LONG).show();


                        
                        mLeDevice.gattClientExchangeMtu(mtuSize);
                    }
                }).setNegativeButton("cancel", null).create();
                
                dialog.show(); 
            }
        });
        
        
        return mView;
    }
    
    
    
    
    
    
    
    
    
}
