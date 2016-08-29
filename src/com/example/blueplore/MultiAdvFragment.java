package com.example.blueplore;



import com.example.blueplore.MainActivity.PlaceholderFragment;

import android.app.AlertDialog;
import android.bluetooth.le.AdvertiseSettings;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;





//section2
public class MultiAdvFragment extends PlaceholderFragment {
    private static final String TAG = "MultiAdvFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    private BleMultiAdv mBleMultiAdv;
    private int currentAdvIndex;
    private int whichSingleChoiceItem = 0;
    
    private ScrollView mScrollView;
    public TextView mAdvTypeTextView;
    private EditText mManufacturerIdEditText;
    private EditText mManufacturerDataEditText;
    private EditText mServiceUuidEditText;
    private EditText mServiceDataUuidEditText;
    private EditText mServiceDataEditText;
    private Spinner mIncludeNameSpinner;
    private Spinner mIsConnectableSpinner;
    private EditText mRespManufacturerIdEditText;
    private EditText mRespManufacturerDataEditText;
    private EditText mRespServiceUuidEditText;
    private EditText mRespServiceDataUuidEditText;
    private EditText mRespServiceDataEditText;
    private Spinner mRespIncludeNameSpinner;
    private Spinner mTxPowerSpinner;
    private Spinner mAdvPowerTypeSpinner;
    private EditText mTimeOutValueEditText;
    public TextView mAdvResultTextView;
    
    String[] mIncludeNameSpinnerValue = { "true", "false" };
    String[] mIsConnectableSpinnerValue = { "false", "true" };
    String[] mRespIncludeNameSpinnerValue = { "true", "false" };
    String[] mTxPowerSpinnerValue = { "Choose", "Ultra Low", "Low", "Medium", "High" };
    String[] mAdvPowerTypeSpinnerValue = { "low power", "balanced", "low latency" };

    private TextView mServiceUuidTextView;
    private TextView mServiceDataUuidTextView;
    private TextView mRespServiceUuidTextView;
    private TextView mRespServiceDataUuidTextView;
    private int mServiceUuidCnt;
    private int mRespServiceUuidCnt;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        
        mView = inflater.inflate(R.layout.section2, container, false);
        mScrollView = (ScrollView)mView.findViewById(R.id.scrollView);
        mAdvTypeTextView = (TextView)mView.findViewById(R.id.adv_type);
        mManufacturerIdEditText = (EditText)mView.findViewById(R.id.manufacturerId_editText);
        mManufacturerDataEditText = (EditText)mView.findViewById(R.id.manufacturerData_editText);
        mServiceUuidEditText = (EditText)mView.findViewById(R.id.service_uuid_editText);
        mServiceDataUuidEditText = (EditText)mView.findViewById(R.id.service_data_uuid_editText);
        mServiceDataEditText = (EditText)mView.findViewById(R.id.service_data_editText);
        mIncludeNameSpinner = (Spinner)mView.findViewById(R.id.include_name_spinner);
        mIsConnectableSpinner = (Spinner)mView.findViewById(R.id.is_connectable_spinner);
        mRespManufacturerIdEditText = (EditText)mView.findViewById(R.id.resp_manufacturerId_editText);
        mRespManufacturerDataEditText = (EditText)mView.findViewById(R.id.resp_manufacturerData_editText);
        mRespServiceUuidEditText = (EditText)mView.findViewById(R.id.resp_service_uuid_editText);
        mRespServiceDataUuidEditText = (EditText)mView.findViewById(R.id.resp_service_data_uuid_editText);
        mRespServiceDataEditText = (EditText)mView.findViewById(R.id.resp_service_data_editText);
        mRespIncludeNameSpinner = (Spinner)mView.findViewById(R.id.resp_include_name_spinner);
        mTxPowerSpinner = (Spinner)mView.findViewById(R.id.resp_tx_power_spinner);
        mAdvPowerTypeSpinner = (Spinner)mView.findViewById(R.id.adv_power_type_spinner);
        mTimeOutValueEditText = (EditText)mView.findViewById(R.id.timeout_editText);
        mAdvResultTextView = (TextView)mView.findViewById(R.id.adv_result_textView);

        ArrayAdapter<String> includeNameSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mIncludeNameSpinnerValue);
        includeNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIncludeNameSpinner.setAdapter(includeNameSpinnerAdapter);
        
        ArrayAdapter<String> isConnectableSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mIsConnectableSpinnerValue);
        isConnectableSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIsConnectableSpinner.setAdapter(isConnectableSpinnerAdapter);
        
        ArrayAdapter<String> respIncludeNameSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mRespIncludeNameSpinnerValue);
        respIncludeNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRespIncludeNameSpinner.setAdapter(respIncludeNameSpinnerAdapter);
        
        ArrayAdapter<String> txPowerSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mTxPowerSpinnerValue);
        txPowerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTxPowerSpinner.setAdapter(txPowerSpinnerAdapter);
        
        ArrayAdapter<String> advPowerTypeAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mAdvPowerTypeSpinnerValue);
        advPowerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdvPowerTypeSpinner.setAdapter(advPowerTypeAdapter);

        selectAdvIndex(0);//get from first adv config
        
        mServiceUuidTextView = (TextView)mView.findViewById(R.id.service_uuid_textView);
        mServiceDataUuidTextView = (TextView)mView.findViewById(R.id.service_data_uuid_textView);
        mRespServiceUuidTextView = (TextView)mView.findViewById(R.id.resp_service_uuid_textView);
        mRespServiceDataUuidTextView = (TextView)mView.findViewById(R.id.resp_service_data_uuid_textView);
        
        mServiceUuidTextView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                switch (mServiceUuidCnt % 3) {
                case 0:
                    mServiceUuidEditText.setText(Macro.Default16bitUUID16);
                    break;
                case 1:
                    mServiceUuidEditText.setText(Macro.Default16bitUUID32);
                    break;
                case 2:
                    mServiceUuidEditText.setText(Macro.Default16bitUUID128);
                    break;
                }
                mServiceUuidCnt++;
            }
        });
        
        mServiceDataUuidTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mServiceDataUuidEditText.setText(Macro.Default16bitUUID16);
                mServiceDataEditText.setText("11");
            }
            
        });
        
        mRespServiceUuidTextView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                switch (mRespServiceUuidCnt % 3) {
                case 0:
                    mRespServiceUuidEditText.setText(Macro.Default16bitUUID16);
                    break;
                case 1:
                    mRespServiceUuidEditText.setText(Macro.Default16bitUUID32);
                    break;
                case 2:
                    mRespServiceUuidEditText.setText(Macro.Default16bitUUID128);
                    break;
                }
                mRespServiceUuidCnt++;
            }
        });
        
        mRespServiceDataUuidTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRespServiceDataUuidEditText.setText(Macro.Default16bitUUID16);
                mRespServiceDataEditText.setText("22");
            }
            
        });
        
        return mView;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    private void selectAdvIndex (int index) {
        currentAdvIndex = index;
        mBleMultiAdv = mMainActivity.leService.bleMultiAdvController.selectAdv(currentAdvIndex);
        loadConfig();
    }
    
    public void selectAdvDialog () {
        whichSingleChoiceItem = 0;
        
        AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
        dialog.setTitle("Please Select Adv");
        dialog.setSingleChoiceItems(new String[] {"Adv 1", "Adv 2", "Adv 3", "Adv 4"}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                whichSingleChoiceItem = which;
            }
        });
        
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                //Log.e(TAG, "whichSingleChoiceItem=" + whichSingleChoiceItem);
                selectAdvIndex(whichSingleChoiceItem);
                mScrollView.scrollTo(0, 0);
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }
    
    public void saveConfig () {
        mBleMultiAdv.advDataLength = 0;
        mBleMultiAdv.respAdvDataLength = 0;
        

        if (!mManufacturerIdEditText.getText().toString().equals("")) {
            mBleMultiAdv.manufacturerId = Integer.parseInt(mManufacturerIdEditText.getText().toString(), 16);
            mBleMultiAdv.advDataLength += 4;// 2 + 2
        } else {
            mBleMultiAdv.manufacturerId = -1;
        }

        if (!mManufacturerDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mManufacturerDataEditText.getText().toString(), 2);
            mBleMultiAdv.manufacturerSpecificData = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleMultiAdv.manufacturerSpecificData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
            }
            mBleMultiAdv.advDataLength += mBleMultiAdv.manufacturerSpecificData.length;
        } else {
            mBleMultiAdv.manufacturerSpecificData = null;
        }

        if (!mServiceUuidEditText.getText().toString().equals("")) {
            mBleMultiAdv.serviceUuid = ParcelUuid.fromString(mServiceUuidEditText.getText().toString());
            if (Macro.is16BitUuid(mBleMultiAdv.serviceUuid)) {
                mBleMultiAdv.advDataLength += 4;         // 2 + 2  // 2 = len + type
            }else if (Macro.is32BitUuid(mBleMultiAdv.serviceUuid)) {
                mBleMultiAdv.advDataLength += 6;         // 2 + 4
            }else{
                mBleMultiAdv.advDataLength += 18;        // 2 + 16
            }
        } else {
            mBleMultiAdv.serviceUuid = null;
        }

        if (!mServiceDataUuidEditText.getText().toString().equals("")) {
            mBleMultiAdv.serviceDataUuid = ParcelUuid.fromString(mServiceDataUuidEditText.getText().toString());
            if (Macro.is16BitUuid(mBleMultiAdv.serviceDataUuid)) {
                mBleMultiAdv.advDataLength += 4;         // 2 + 2  // 2 = len + type
            }else{
                mBleMultiAdv.serviceDataUuid = null;
                Toast.makeText(getActivity(), "service data UUID only support 16bit" , Toast.LENGTH_LONG).show();
            }
        } else {
            mBleMultiAdv.serviceDataUuid = null;
        }

        if (!mServiceDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mServiceDataEditText.getText().toString(), 2);
            mBleMultiAdv.serviceData = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleMultiAdv.serviceData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
            }
            mBleMultiAdv.advDataLength += mBleMultiAdv.serviceData.length;
        } else {
            mBleMultiAdv.serviceData = null;
        }

        if (mIncludeNameSpinner.getSelectedItem().toString().equals("true")) {
            mBleMultiAdv.includeName = true;
            mBleMultiAdv.advDataLength += 2 + mMainActivity.leService.le.mBluetoothAdapter.getName().length();
        } else {
            mBleMultiAdv.includeName = false;
        }
        
        if (mIsConnectableSpinner.getSelectedItem().toString().equals("true")) {
            mBleMultiAdv.isConnectable = true;
        } else {
            mBleMultiAdv.isConnectable = false;
        }
        
        if (!mRespManufacturerIdEditText.getText().toString().equals(""))
        {
            mBleMultiAdv.respManufacturerId = Integer.parseInt(mRespManufacturerIdEditText.getText().toString(), 16);
            mBleMultiAdv.respAdvDataLength += 4;// 2 + 2
        } else {
            mBleMultiAdv.respManufacturerId = -1;
        }
        
        if (!mRespManufacturerDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mRespManufacturerDataEditText.getText().toString(), 2);
            mBleMultiAdv.respManufacturerSpecificData = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleMultiAdv.respManufacturerSpecificData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
            }
            mBleMultiAdv.respAdvDataLength += mBleMultiAdv.respManufacturerSpecificData.length;
        } else {
            mBleMultiAdv.respManufacturerSpecificData = null;
        }
        
        if (!mRespServiceUuidEditText.getText().toString().equals("")) 
        {
            mBleMultiAdv.respServiceUuid = ParcelUuid.fromString(mRespServiceUuidEditText.getText().toString());
            if (Macro.is16BitUuid(mBleMultiAdv.respServiceUuid)) {
                mBleMultiAdv.respAdvDataLength += 4;         // 2 + 2  // 2 = len + type
            }else if (Macro.is32BitUuid(mBleMultiAdv.respServiceUuid)) {
                mBleMultiAdv.respAdvDataLength += 6;         // 2 + 4
            }else{
                mBleMultiAdv.respAdvDataLength += 18;        // 2 + 16
            }
        } else {
            mBleMultiAdv.respServiceUuid = null;
        }
        
        if (!mRespServiceDataUuidEditText.getText().toString().equals("")) 
        {
            mBleMultiAdv.respServiceDataUuid = ParcelUuid.fromString(mRespServiceDataUuidEditText.getText().toString());
            if (Macro.is16BitUuid(mBleMultiAdv.respServiceDataUuid)) {
                mBleMultiAdv.respAdvDataLength += 4;         // 2 + 2  // 2 = len + type
            }else{
                mBleMultiAdv.respServiceDataUuid = null;
                Toast.makeText(getActivity(), "resp service data UUID only support 16bit" , Toast.LENGTH_LONG).show();
            }
        } else {
            mBleMultiAdv.respServiceDataUuid = null;
        }
        
        if (!mRespServiceDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mRespServiceDataEditText.getText().toString(), 2);
            mBleMultiAdv.respServiceData = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleMultiAdv.respServiceData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
            }
            mBleMultiAdv.respAdvDataLength += mBleMultiAdv.respServiceData.length;
        } else {
            mBleMultiAdv.respServiceData = null;
        }
        
        if (mRespIncludeNameSpinner.getSelectedItem().toString().equals("true")) {
            mBleMultiAdv.respIncludeName = true;
            mBleMultiAdv.respAdvDataLength += 2 + mMainActivity.leService.le.mBluetoothAdapter.getName().length();
        } else {
            mBleMultiAdv.respIncludeName = false;
        }
        
        //String[] mTxPowerSpinnerValue = { "Choose", "Ultra Low", "Low", "Medium", "High" };
        if (mTxPowerSpinner.getSelectedItem().toString().equals(mTxPowerSpinnerValue[0])) {
            mBleMultiAdv.includeTxpowerLevel = false;
        } else {
            if (mTxPowerSpinner.getSelectedItem().toString().equals(mTxPowerSpinnerValue[1])) {
                mBleMultiAdv.txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW;
            } else if (mTxPowerSpinner.getSelectedItem().toString().equals(mTxPowerSpinnerValue[2])) {
                mBleMultiAdv.txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;
            } else if (mTxPowerSpinner.getSelectedItem().toString().equals(mTxPowerSpinnerValue[3])) {
                mBleMultiAdv.txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
            } else if (mTxPowerSpinner.getSelectedItem().toString().equals(mTxPowerSpinnerValue[4])) {
                mBleMultiAdv.txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
            } else {
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "mTxPowerSpinner.getSelectedItem().toString()=" + mTxPowerSpinner.getSelectedItem().toString()); 
            }
            
            mBleMultiAdv.includeTxpowerLevel = true;
            mBleMultiAdv.advDataLength += 3;
            mBleMultiAdv.respAdvDataLength += 3;
        }
        
        //String[] mAdvPowerTypeSpinnerValue = { "low power", "balanced", "low latency" };
        if (mAdvPowerTypeSpinner.getSelectedItem().toString().equals(mAdvPowerTypeSpinnerValue[0])) {
            mBleMultiAdv.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
        } else if (mAdvPowerTypeSpinner.getSelectedItem().toString().equals(mAdvPowerTypeSpinnerValue[1])) {
            mBleMultiAdv.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
        } else if (mAdvPowerTypeSpinner.getSelectedItem().toString().equals(mAdvPowerTypeSpinnerValue[2])) {
            mBleMultiAdv.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
        }
        
        if (!mTimeOutValueEditText.getText().toString().equals("")) {
            mBleMultiAdv.timeoutMillis = Integer.parseInt(mTimeOutValueEditText.getText().toString());
        } else {
            mBleMultiAdv.timeoutMillis = 1;
        }
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
        Log.e(TAG, "mBleMultiAdv.advDataLength=" + mBleMultiAdv.advDataLength);/*bt dbg*/
        Log.e(TAG, "mBleMultiAdv.respAdvDataLength=" + mBleMultiAdv.respAdvDataLength);/*bt dbg*/
        if (mBleMultiAdv.advDataLength > 31) {      
            Toast.makeText(getActivity(), "Adv bytes greater than 31 octets. Kindly reduce. ADV byte = " + mBleMultiAdv.advDataLength, Toast.LENGTH_LONG).show();
            mBleMultiAdv.clearConfig();
        }
        
        if (mBleMultiAdv.respAdvDataLength > 31) {      
            Toast.makeText(getActivity(), "Scan Resp Adv bytes greater than 31 octets. Kindly reduce. ADV byte = " + mBleMultiAdv.respAdvDataLength, Toast.LENGTH_LONG).show();
            mBleMultiAdv.clearConfig();
        }
    }
    
    public void loadConfig () {
        String tilte = "Adv "+ (currentAdvIndex + 1);
        if (mBleMultiAdv.isSendingAdv) {
            tilte +="   Sending";
        }
        mAdvTypeTextView.setText(tilte);

        if (mBleMultiAdv.manufacturerId != -1) {
            mManufacturerIdEditText.setText(Integer.toHexString(mBleMultiAdv.manufacturerId));
        } else {
            mManufacturerIdEditText.setText("");
        }
        
        if (mBleMultiAdv.manufacturerSpecificData != null) {
            mManufacturerDataEditText.setText(Macro.bytesToHexString(mBleMultiAdv.manufacturerSpecificData));
        } else {
            mManufacturerDataEditText.setText("");
        }
        
        if (mBleMultiAdv.serviceUuid != null) {
            mServiceUuidEditText.setText(mBleMultiAdv.serviceUuid.toString());
        } else {
            mServiceUuidEditText.setText("");
            mServiceUuidEditText.setHint(Macro.Default16bitUUID32);
        }
        
        if (mBleMultiAdv.serviceDataUuid != null) {
            mServiceDataUuidEditText.setText(mBleMultiAdv.serviceDataUuid.toString());
        } else {
            mServiceDataUuidEditText.setText("");
            mServiceDataUuidEditText.setHint(Macro.Default16bitUUID16);
        }
        
        if (mBleMultiAdv.serviceData != null) {
            mServiceDataEditText.setText(Macro.bytesToHexString(mBleMultiAdv.serviceData));
        } else {
            mServiceDataEditText.setText("");
        }
        
        if (mBleMultiAdv.includeName == true) {
            mIncludeNameSpinner.setSelection(0);
        } else {
            mIncludeNameSpinner.setSelection(1);
        }
        
        if (mBleMultiAdv.isConnectable == true) {
            mIsConnectableSpinner.setSelection(1);
        } else {
            mIsConnectableSpinner.setSelection(0);
        }
        
        if (mBleMultiAdv.respManufacturerId != -1) {
            mRespManufacturerIdEditText.setText(Integer.toHexString(mBleMultiAdv.respManufacturerId));
        } else {
            mRespManufacturerIdEditText.setText("");
        }
        
        if (mBleMultiAdv.respManufacturerSpecificData != null) {
            mRespManufacturerDataEditText.setText(Macro.bytesToHexString(mBleMultiAdv.respManufacturerSpecificData));
        } else {
            mRespManufacturerDataEditText.setText("");
        }
        
        if (mBleMultiAdv.respServiceUuid != null) {
            mRespServiceUuidEditText.setText(mBleMultiAdv.respServiceUuid.toString());
        } else {
            mRespServiceUuidEditText.setText("");
            mRespServiceUuidEditText.setHint(Macro.Default16bitUUID32);
        }
        
        if (mBleMultiAdv.respServiceDataUuid != null) {
            mRespServiceDataUuidEditText.setText(mBleMultiAdv.respServiceDataUuid.toString());
        } else {
            mRespServiceDataUuidEditText.setText("");
            mRespServiceDataUuidEditText.setHint(Macro.Default16bitUUID16);
        }
        
        if (mBleMultiAdv.respServiceData != null) {
            mRespServiceDataEditText.setText(Macro.bytesToHexString(mBleMultiAdv.respServiceData));
        } else {
            mRespServiceDataEditText.setText("");
        }
        
        if (mBleMultiAdv.respIncludeName == true) {
            mRespIncludeNameSpinner.setSelection(0);
        } else {
            mRespIncludeNameSpinner.setSelection(1);
        }
        
        if (mBleMultiAdv.includeTxpowerLevel == true) {
            switch (mBleMultiAdv.txPowerLevel) {
            case AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW:
                mTxPowerSpinner.setSelection(1);
                break;
            case AdvertiseSettings.ADVERTISE_TX_POWER_LOW:
                mTxPowerSpinner.setSelection(2);
                break;
            case AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM:
                mTxPowerSpinner.setSelection(3);
                break;
            case AdvertiseSettings.ADVERTISE_TX_POWER_HIGH:
                mTxPowerSpinner.setSelection(4);
                break;
            }
        } else {
            mTxPowerSpinner.setSelection(0);
        }
        
        switch (mBleMultiAdv.advertiseMode) {
        case AdvertiseSettings.ADVERTISE_MODE_LOW_POWER:
            mAdvPowerTypeSpinner.setSelection(0);
            break;
        case AdvertiseSettings.ADVERTISE_MODE_BALANCED:
            mAdvPowerTypeSpinner.setSelection(1);
            break;
        case AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY:
            mAdvPowerTypeSpinner.setSelection(2);
            break;
        }
        
        mTimeOutValueEditText.setText(String.valueOf(mBleMultiAdv.timeoutMillis));
    }
    
    public void resetConfig () {
        mBleMultiAdv.defaultConfig();
        loadConfig();
    }
    
    public void clearConfig () {
        mBleMultiAdv.clearConfig();
        loadConfig();
    }
    
    public void startSendAdv () {
        mBleMultiAdv.startAdv();
    }
    
    public void stopSendAdv () {
        mBleMultiAdv.stopAdv();
        mAdvTypeTextView.setText("Adv "+ (currentAdvIndex + 1));
        mMainActivity.section2Fragment.mAdvResultTextView.setText("success/fail");
    }
    
    
}
