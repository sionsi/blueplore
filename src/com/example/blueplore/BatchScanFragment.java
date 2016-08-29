package com.example.blueplore;

import com.example.blueplore.MainActivity.PlaceholderFragment;

import android.app.AlertDialog;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;



public class BatchScanFragment extends PlaceholderFragment {

    private static final String TAG = "BatchScanFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    private BleBatchScanConfig mBleBatchScanConfig;
    public int currentConfigIndex;
    private int whichSingleChoiceItem = 0;
    
    private ScrollView mScrollView;
    public TextView mBatchScanTextView;
    
    private EditText mManufacturerIdEditText;
    private EditText mManufacturerDataEditText;
    
    private TextView mServiceUuidTextView;
    private EditText mServiceUuidEditText;
    private TextView mServiceDataUuidTextView;
    private EditText mServiceDataUuidEditText;
    private EditText mServiceDataEditText;
    
    private EditText mDevAddrEditText;
    private EditText mLocalNameEditText;
    private EditText mReportDelayTimestampEditText;
    
    private Spinner mScanTypeSpinner;
    private Spinner mCallbackTypeSpinner;
    
    String[] mScanTypeSpinnerValue = { "Choose", "low power", "balanced", "low latency" };
    String[] mCallbackTypeSpinnerValue = { "Choose", "all matches", "first match", "match lost" };
    
    public TextView mScanCountTextView;
    public TextView mScanResultTextView;
    
    private int mServiceUuidCnt;
    
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mMainActivity = (MainActivity)getActivity();
        
        mView = inflater.inflate(R.layout.section3, container, false);
        mScrollView = (ScrollView)mView.findViewById(R.id.section3_scrollView);
        mBatchScanTextView = (TextView)mView.findViewById(R.id.section3_batch_scan_textView);
        mManufacturerIdEditText = (EditText)mView.findViewById(R.id.section3_manufacturerId_editText);
        mManufacturerDataEditText = (EditText)mView.findViewById(R.id.section3_manufacturerData_editText);
        mServiceUuidTextView = (TextView)mView.findViewById(R.id.section3_service_uuid_textView);
        mServiceUuidEditText = (EditText)mView.findViewById(R.id.section3_service_uuid_editText);
        mServiceDataUuidTextView = (TextView)mView.findViewById(R.id.section3_service_data_uuid_textView);
        mServiceDataUuidEditText = (EditText)mView.findViewById(R.id.section3_service_data_uuid_editText);
        mServiceDataEditText = (EditText)mView.findViewById(R.id.section3_service_data_editText);
        
        mDevAddrEditText = (EditText)mView.findViewById(R.id.section3_dev_addr_editText);
        mLocalNameEditText = (EditText)mView.findViewById(R.id.section3_local_name_editText);
        mReportDelayTimestampEditText = (EditText)mView.findViewById(R.id.section3_report_delay_timestamp_editText);
        
        mScanTypeSpinner = (Spinner)mView.findViewById(R.id.section3_scan_type_spinner);
        mCallbackTypeSpinner = (Spinner)mView.findViewById(R.id.section3_cb_type_spinner);
        
        mScanCountTextView = (TextView)mView.findViewById(R.id.section3_scan_count);
        mScanResultTextView = (TextView)mView.findViewById(R.id.section3_scan_result);
        
        ArrayAdapter<String> scanTypeSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mScanTypeSpinnerValue);
        scanTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScanTypeSpinner.setAdapter(scanTypeSpinnerAdapter);
        
        ArrayAdapter<String> callbackSpinnerAdapter = new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_spinner_item, mCallbackTypeSpinnerValue);
        callbackSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCallbackTypeSpinner.setAdapter(callbackSpinnerAdapter);

        selectConfig(0);//get from first batch scan config
        
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
        
        return mView;
    }

    private void selectConfig (int index) {
        currentConfigIndex = index;
        mBleBatchScanConfig = mMainActivity.leService.bleBatchScanController.selectConfig(currentConfigIndex);
        loadConfig();
    }
    
    public void selectConfigDialog () {
        whichSingleChoiceItem = 0;
        
        AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
        dialog.setTitle("Please Select Batch Scan");
        dialog.setSingleChoiceItems(new String[] {"Batch Scan 1", "Batch Scan 2", "Batch Scan 3", "Batch Scan 4"}, 0, new DialogInterface.OnClickListener() {
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
                selectConfig(whichSingleChoiceItem);
                mScanCountTextView.setText("");
                mScanResultTextView.setText("");
                mScrollView.scrollTo(0, 0);
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }
    
    public void saveConfig () {
        if (!mManufacturerIdEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.manufacturerId = Integer.parseInt(mManufacturerIdEditText.getText().toString(), 16);
        } else {
            mBleBatchScanConfig.manufacturerId = -1;
        }
        
        if (!mManufacturerDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mManufacturerDataEditText.getText().toString(), 2);
            mBleBatchScanConfig.manufacturerSpecificData = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleBatchScanConfig.manufacturerSpecificData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
            }
        } else {
            mBleBatchScanConfig.manufacturerSpecificData = null;
        }
        
        if (!mServiceUuidEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.serviceUuid = ParcelUuid.fromString(mServiceUuidEditText.getText().toString());
        } else {
            mBleBatchScanConfig.serviceUuid = null;
        }
        
        if (!mServiceDataUuidEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.serviceDataUuid = ParcelUuid.fromString(mServiceDataUuidEditText.getText().toString());
        } else {
            mBleBatchScanConfig.serviceDataUuid = null;
        }
        
        if (!mServiceDataEditText.getText().toString().equals("")) {
            String[] arrayOfString = Macro.splitStringEvery(mServiceDataEditText.getText().toString(), 2);
            mBleBatchScanConfig.serviceData = new byte[arrayOfString.length];
            mBleBatchScanConfig.serviceDataMask = new byte[arrayOfString.length];
            for(int i = 0 ; i < arrayOfString.length; i++) {
                mBleBatchScanConfig.serviceData[i] = (byte)Integer.parseInt(arrayOfString[i], 16);
                mBleBatchScanConfig.serviceDataMask[i] = (byte)0xFF;
            }
        } else {
            mBleBatchScanConfig.serviceData = null;
        }
        
        if (!mDevAddrEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.deviceAddress = mDevAddrEditText.getText().toString();
        } else {
            mBleBatchScanConfig.deviceAddress = null;
        }
        
        if (!mLocalNameEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.deviceName = mLocalNameEditText.getText().toString();
        } else {
            mBleBatchScanConfig.deviceName = null;
        }
        
        if (!mReportDelayTimestampEditText.getText().toString().equals("")) {
            mBleBatchScanConfig.reportDelayMillis = Long.parseLong(mReportDelayTimestampEditText.getText().toString());
        } else {
            mBleBatchScanConfig.reportDelayMillis = 1;
        }
        
        if (mScanTypeSpinner.getSelectedItem().toString().equals(mScanTypeSpinnerValue[0])) {
            mBleBatchScanConfig.scanMode = ScanSettings.SCAN_MODE_LOW_POWER;
        } else if (mScanTypeSpinner.getSelectedItem().toString().equals(mScanTypeSpinnerValue[1])) {
            mBleBatchScanConfig.scanMode = ScanSettings.SCAN_MODE_LOW_POWER;
        } else if (mScanTypeSpinner.getSelectedItem().toString().equals(mScanTypeSpinnerValue[2])) {
            mBleBatchScanConfig.scanMode = ScanSettings.SCAN_MODE_BALANCED;
        } else if (mScanTypeSpinner.getSelectedItem().toString().equals(mScanTypeSpinnerValue[3])) {
            mBleBatchScanConfig.scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY;
        }
        
        if (mCallbackTypeSpinner.getSelectedItem().toString().equals(mCallbackTypeSpinnerValue[0])) {
            mBleBatchScanConfig.callbackType = ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
        } else if (mCallbackTypeSpinner.getSelectedItem().toString().equals(mCallbackTypeSpinnerValue[1])) {
            mBleBatchScanConfig.callbackType = ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
        } else if (mCallbackTypeSpinner.getSelectedItem().toString().equals(mCallbackTypeSpinnerValue[2])) {
            mBleBatchScanConfig.callbackType = ScanSettings.CALLBACK_TYPE_FIRST_MATCH;
        } else if (mCallbackTypeSpinner.getSelectedItem().toString().equals(mCallbackTypeSpinnerValue[3])) {
            mBleBatchScanConfig.callbackType = ScanSettings.CALLBACK_TYPE_MATCH_LOST;
        }
    }
    
    public void loadConfig () {
        String tilte = "Batch Scan "+ (currentConfigIndex + 1);
        if (mBleBatchScanConfig.isScanning) {
            tilte +="   Scanning...";
        }
        mBatchScanTextView.setText(tilte);
        
        if (mBleBatchScanConfig.manufacturerId != -1) {
            mManufacturerIdEditText.setText(Integer.toHexString(mBleBatchScanConfig.manufacturerId));
        } else {
            mManufacturerIdEditText.setText("");
        }
        
        if (mBleBatchScanConfig.manufacturerSpecificData != null) {
            mManufacturerDataEditText.setText(Macro.bytesToHexString(mBleBatchScanConfig.manufacturerSpecificData));
        } else {
            mManufacturerDataEditText.setText("");
        }
        
        if (mBleBatchScanConfig.serviceUuid != null) {
            mServiceUuidEditText.setText(mBleBatchScanConfig.serviceUuid.toString());
        } else {
            mServiceUuidEditText.setText("");
        }
        
        if (mBleBatchScanConfig.serviceDataUuid != null) {
            mServiceDataUuidEditText.setText(mBleBatchScanConfig.serviceDataUuid.toString());
        } else {
            mServiceDataUuidEditText.setText("");
        }
        
        if (mBleBatchScanConfig.serviceData != null) {
            mServiceDataEditText.setText(Macro.bytesToHexString(mBleBatchScanConfig.serviceData));
        } else {
            mServiceDataEditText.setText("");
        }
        
        if (mBleBatchScanConfig.deviceAddress != null) {
            mDevAddrEditText.setText(mBleBatchScanConfig.deviceAddress);
        } else {
            mDevAddrEditText.setText("");
        }
        
        if (mBleBatchScanConfig.deviceName != null) {
            mLocalNameEditText.setText(mBleBatchScanConfig.deviceName);
        } else {
            mLocalNameEditText.setText("");
        }
        
        mReportDelayTimestampEditText.setText(String.valueOf(mBleBatchScanConfig.reportDelayMillis));
        
        switch (mBleBatchScanConfig.scanMode) {
        case ScanSettings.SCAN_MODE_LOW_POWER:
            mScanTypeSpinner.setSelection(1);
            break;
        case ScanSettings.SCAN_MODE_BALANCED:
            mScanTypeSpinner.setSelection(2);
            break;
        case ScanSettings.SCAN_MODE_LOW_LATENCY:
            mScanTypeSpinner.setSelection(3);
            break;
        }
        
        switch (mBleBatchScanConfig.callbackType) {
        case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
            mCallbackTypeSpinner.setSelection(1);
            break;
        case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
            mCallbackTypeSpinner.setSelection(2);
            break;
        case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
            mCallbackTypeSpinner.setSelection(3);
            break;
        }
    }
    
    public void resetConfig () {
        mBleBatchScanConfig.defaultConfig();
        loadConfig();
    }
    
    public void clearConfig () {
        mBleBatchScanConfig.clearConfig();
        loadConfig();
    }
    
    public void startScan () {
        mBleBatchScanConfig.startScan();
        
        String tilte = "Batch Scan "+ (currentConfigIndex + 1);
        if (mBleBatchScanConfig.isScanning) {
            tilte +="   Scanning...";
        }
        mBatchScanTextView.setText(tilte);
    }
    
    public void stopScan () {
        mBleBatchScanConfig.stopScan();
        mBatchScanTextView.setText("Batch Scan "+ (currentConfigIndex + 1));
    }
}
