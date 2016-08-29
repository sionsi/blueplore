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

public class NotificationSendFragment extends Fragment {
    private static final String TAG = "NotificationSendFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    private TextView mDevInfoTextView;
    private TextView mPktTextView;
    private Button mSetPktLenButton;
    private Button mShowSndPktButton;
    private Button mSndPktButton;
    
    private LeDevice mLeDevice;

    private byte[] sendPayload1;
    private byte[] sendPayload2;
    private byte[] sendPayload3;
    private String sendPayload1str;
    private String sendPayload2str;
    private String sendPayload3str;
    private byte[] currentSendPayload;
    private String currentSendPayloadStr;
    private int sendPayloadSelector;
    
    private String textStr;
    private int sendPktLength = Macro.PacketMaxLength;
    
    public NotificationSendFragment(LeDevice leDevice) {
        super();
        this.mLeDevice = leDevice;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        
        mView = inflater.inflate(R.layout.notification_send, container, false);
        mDevInfoTextView = (TextView)mView.findViewById(R.id.notification_send_device_info_textView);
        mPktTextView = (TextView)mView.findViewById(R.id.notification_send_pkt_textView);
        mSetPktLenButton = (Button)mView.findViewById(R.id.notification_send_set_pkt_len_button);
        mShowSndPktButton = (Button)mView.findViewById(R.id.notification_send_show_snd_pkt_button);
        mSndPktButton = (Button)mView.findViewById(R.id.notification_send_send_pkt_button);
        
        mDevInfoTextView.setText("Send Notification To The Remote Gatt Client Device Name:  " + mLeDevice.name + "\n" + "Remote Device Address:" + "\n" + mLeDevice.address);
        
        sendPayloadInit(sendPktLength);
        
        mShowSndPktButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (sendPayloadSelector % 3) {
                case 0:
                    currentSendPayload = sendPayload1;
                    currentSendPayloadStr = sendPayload1str;
                    break;
                case 1:
                    currentSendPayload = sendPayload2;
                    currentSendPayloadStr = sendPayload2str;
                    break;
                case 2:
                    currentSendPayload = sendPayload3;
                    currentSendPayloadStr = sendPayload3str;
                    break;
                }
                
                sendPayloadSelector++;
                
                textStr = "Att Mtu:" + mLeDevice.mtu + "\n" + "Tx notification count:" + mLeDevice.txNotificationCnt + "\n" + "Tx notification successfully count:" + mLeDevice.txNotificationSuccessfullyCnt + "\n" + "Payload length:" + currentSendPayload.length;
                textStr += "\n" + "\n" + currentSendPayloadStr;
                mPktTextView.setText("");
                mPktTextView.setText(textStr);
                
            }
        });
        
        mSndPktButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLeDevice.gattServerSentNotification(currentSendPayload);
            }
        });
        
        mSetPktLenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(mMainActivity);
                View view = layoutInflater.inflate(R.layout.dialog_number, null);
                final EditText editText = (EditText)view.findViewById(R.id.dialog_num_edittext);
                editText.setHint(String.valueOf(Macro.PacketMaxLength));
                
                Dialog dialog = new AlertDialog.Builder(mMainActivity)
                .setTitle("send packet length")
                .setIcon(R.drawable.ic_launcher)
                .setView(view)
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int len = Integer.parseInt(editText.getText().toString());
                        sendPayloadInit(len);
                    }
                }).setNegativeButton("cancel", null).create();
                
                dialog.show(); 
            }
        });

        return mView;
    }
    
    public void sendPayloadInit (int len) {
        int i;
        
        sendPayload1 = new byte[len];
        sendPayload2 = new byte[len];
        sendPayload3 = new byte[len];
        
        for (i = 0; i < len; i++) {
            sendPayload1[i] = (byte)0x11;
            sendPayload2[i] = (byte)0x22;
            sendPayload3[i] = (byte)0x33;
        }
        
        sendPayload1str = Macro.bytesToHexString(sendPayload1);
        sendPayload2str = Macro.bytesToHexString(sendPayload2);
        sendPayload3str = Macro.bytesToHexString(sendPayload3);

        currentSendPayload = sendPayload1;
        currentSendPayloadStr = sendPayload1str;
        
        textStr = "Att Mtu:" + mLeDevice.mtu + "\n" + "Tx notification count:" + mLeDevice.txNotificationCnt + "\n" + "Tx notification successfully count:" + mLeDevice.txNotificationSuccessfullyCnt + "\n" + "Payload length:" + currentSendPayload.length;
        textStr += "\n" + "\n" + currentSendPayloadStr;
        mPktTextView.setText("");
        mPktTextView.setText(textStr);
    }

}
