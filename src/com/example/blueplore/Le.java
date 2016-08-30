package com.example.blueplore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Toast;



public class Le {

    private static final String TAG = "Le";

    public MainActivity mMainActivity;
    
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private IntentFilter mIntentFilter;
    boolean isScanning;
    public List<LeDevice> deviceList = new ArrayList<LeDevice>();
    private LeBondReceiver mLeBondReceiver;
    
    public int pairedDeviceNum;
    public int connectedDeviceNum;//indicate the number of remote gatt server device
    public int scannedDeviceNum;

    //gatt server field:
    public BluetoothGattServer mBluetoothGattServer;
    public LeGattServerCallback mLeGattServerCallback;
    public List<LeDevice> remoteGattClientList = new ArrayList<LeDevice>();
    public BluetoothGattService mBluetoothGattService;
    public BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    
    
    
    
    public void onCreate(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        
        mBluetoothManager = (BluetoothManager) mMainActivity.getSystemService(Context.BLUETOOTH_SERVICE);;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "mBluetoothAdapter = null.");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "bluetooth disable.");
        }
        
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            Log.e(TAG, "mBluetoothLeScanner = null,BluetoothAdapter.isEnabled()=" + mBluetoothAdapter.isEnabled());
        }
        
        createGattServer();

        if (!(PackageManager.PERMISSION_GRANTED == mMainActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                || !(PackageManager.PERMISSION_GRANTED == mMainActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            mMainActivity.requestPermissions(Macro.Permissions, 5);
        }
        
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        mIntentFilter.setPriority(Integer.MAX_VALUE);
        mLeBondReceiver = new LeBondReceiver(mMainActivity);
        mMainActivity.registerReceiver(mLeBondReceiver, mIntentFilter);
        
        Log.v(TAG, "Le init OK.");
    }
    
    public void onDestroy() {
        
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            
            Log.v(TAG, "onScanResult name: " + result.getDevice().getName() + ",addr: " + result.getDevice().getAddress());
            
            BluetoothDevice device = result.getDevice();
            
            LeDevice leDevice = new LeDevice(mMainActivity, device.getAddress());
            leDevice.name = (device.getName() == null ? "nameUnknown" : device.getName());
            leDevice.deviceState = Macro.BleDeviceStateScanned;
            
            if (!deviceList.contains(leDevice)) {
                deviceList.add(leDevice);
                freshStatistics();
                
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
                            mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: " + errorCode);
        }
    };
    
    
    public boolean startScan() {
        
        if (true == isScanning) {
            return true;
        }
            
        deleteDeviceByState(Macro.BleDeviceStateScanned);
        freshStatistics();
        
        if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
            mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
        }
        
        mBluetoothLeScanner.startScan(mScanCallback);
        isScanning = true;
        
        mMainActivity.section1Fragment.mScanTextView.setVisibility(View.VISIBLE);
        return true;
    }
    
    public boolean stopScan() {
        
        if (false == isScanning) {
            return true;
        }
        
        mBluetoothLeScanner.stopScan(mScanCallback);
        isScanning = false;
        
        mMainActivity.section1Fragment.mScanTextView.setVisibility(View.GONE);
        return true;
    }
    
    private void deleteDeviceByState(int state) {
        Iterator<LeDevice> it = deviceList.iterator();
        while (it.hasNext()) {
            LeDevice bleDevice = it.next();
            if (bleDevice.deviceState == state) {
                it.remove();
            }
        }
    }
    
    public void deleteDeviceByAddr(String addr) {
        Iterator<LeDevice> it = deviceList.iterator();
        while (it.hasNext()) {
            LeDevice bleDevice = it.next();
            if (bleDevice.address.equals(addr)) {
                it.remove();
            }
        }
    }
    
    public LeDevice searchDeviceListByAddr(String addr) {
        Iterator<LeDevice> it = deviceList.iterator();
        while (it.hasNext()) {
            LeDevice leDevice = it.next();
            if (leDevice.address.equals(addr)) {
                return leDevice;
            }
        }
        
        return null;
    }
    
    void freshStatistics () {
        Comparator<LeDevice> comparator = new LeDeviceStateComparator();
        Collections.sort(deviceList, comparator);//sort the ble device according to state

        pairedDeviceNum = 0;
        connectedDeviceNum = 0;
        scannedDeviceNum = 0;

        for (LeDevice device: deviceList) {

            switch (device.deviceState) {
            case Macro.BleDeviceStatePaired:
                pairedDeviceNum++;
                break;
            case Macro.BleDeviceStateConnected:
                connectedDeviceNum++;
                break;
            case Macro.BleDeviceStateScanned:
            case Macro.BleDeviceStateConnecting:
            case Macro.BleDeviceStatePairing:
                scannedDeviceNum++;
                break;
            case Macro.BleDeviceStateInit:
                //do nothing
                break;
            default:
                Log.e(TAG, "device.getState() = "+ device.deviceState);
            }
        }
    }

  //this cell phone as a Peripheral Role
    public void createGattServer() {
        boolean res;
        
        mLeGattServerCallback = new LeGattServerCallback(mMainActivity);
        
        mBluetoothGattServer = mBluetoothManager.openGattServer(mMainActivity, mLeGattServerCallback);
        if (null == mBluetoothGattServer) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "mBluetoothGattServer = null", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "mBluetoothGattServer = null");
            return;
        }
        
        mBluetoothGattService = new BluetoothGattService(UUID.fromString(Macro.ServiceUuidNotificationTest), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        if (null == mBluetoothGattService) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "mBluetoothGattService = null", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "mBluetoothGattService = null");
            return;
        }
        
        Class<?> clazz = mBluetoothGattService.getClass();
        Method m;
        try {
            m = clazz.getDeclaredMethod("setInstanceId", int.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
            return;
        }
        m.setAccessible(true);
        try {
            int random  = (int)(1 + Math.random() * 100);
            m.invoke(mBluetoothGattService, random);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        }
        
        try {
            m = clazz.getDeclaredMethod("setHandles", int.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
            return;
        }
        m.setAccessible(true);
        try {
            m.invoke(mBluetoothGattService, 200);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, Log.getStackTraceString(e));
        }
        
        
        
        //mBluetoothGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(Macro.CharacteristicUuidNotificationTest), BluetoothGattCharacteristic.PROPERTY_INDICATE + BluetoothGattCharacteristic.PROPERTY_NOTIFY + BluetoothGattCharacteristic.PROPERTY_READ + BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE + BluetoothGattCharacteristic.PERMISSION_READ);
        mBluetoothGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(Macro.CharacteristicUuidNotificationTest), 0x3A, BluetoothGattCharacteristic.PERMISSION_WRITE + BluetoothGattCharacteristic.PERMISSION_READ);
        if (null == mBluetoothGattCharacteristic) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "mBluetoothGattCharacteristic = null", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "mBluetoothGattCharacteristic = null");
            return;
        }
        //mBluetoothGattCharacteristic.setValue("test");

        res = mBluetoothGattService.addCharacteristic(mBluetoothGattCharacteristic);
        if (false == res) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "add Characteristic failed.", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "add Characteristic failed.");
            return;
        }
        
        res = mBluetoothGattServer.addService(mBluetoothGattService);
        if (false == res) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "add Service failed.", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "add Service failed.");
            return;
        }
        
        Log.i(TAG, "Gatt Server Init Done.");
    }
    
    public void deleteRemoteGattClientByAddr(String addr) {
        Iterator<LeDevice> it = remoteGattClientList.iterator();
        while (it.hasNext()) {
            LeDevice leDevice = it.next();
            if (leDevice.address.equals(addr)) {
                it.remove();
            }
        }
    }
    
    public LeDevice searchRemoteGattClientListByAddr(String addr) {
        Iterator<LeDevice> it = remoteGattClientList.iterator();
        while (it.hasNext()) {
            LeDevice leDevice = it.next();
            if (leDevice.address.equals(addr)) {
                return leDevice;
            }
        }
        
        return null;
    }
}


class LeDeviceStateComparator implements Comparator<LeDevice> {
    
    @Override
    public int compare(LeDevice lhs, LeDevice rhs) {
        if (lhs.deviceState > rhs.deviceState) {
            return -1;
        } else if (lhs.deviceState < rhs.deviceState) {
            return 1;
        } else {
            return 0;
        }
    }
}

class LeGattServerCallback extends BluetoothGattServerCallback {
    private static final String TAG = "LeGattServerCallback";
    private MainActivity mMainActivity;
    
    LeDevice leDevice;
    String name, address;
    int mtuSize;

    public LeGattServerCallback(MainActivity mMainActivity) {
        super();
        this.mMainActivity = mMainActivity;
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        // TODO Auto-generated method stub
        super.onMtuChanged(device, mtu);
       
        name = device.getName();
        address = device.getAddress();
        mtuSize = mtu;
        
        leDevice = mMainActivity.leService.le.searchRemoteGattClientListByAddr(device.getAddress());
        if (null == leDevice) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + device.getName() + " " + device.getAddress());
            return;
        }
        leDevice.mtu = mtu;
        
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mMainActivity, "Exchange MTU from the remote device:" + name + " " + address + " mtu=" + mtuSize, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device,
            int status, int newState) {
        name = device.getName();
        address = device.getAddress();
        
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
        Log.e(TAG, "LeGattServerCallback::onConnectionStateChange() newState=" + newState + "," + name + "," + address);/*bt dbg*/

        if (BluetoothProfile.STATE_DISCONNECTED == newState) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "LeGattServerCallback::Remote device disconnected:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
        }
        
        switch (newState) {
        case BluetoothProfile.STATE_CONNECTED:
            leDevice = mMainActivity.leService.le.searchDeviceListByAddr(device.getAddress());
            if ((null != leDevice) && (leDevice.deviceState != Macro.BleDeviceStateScanned)) {
                /*
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mMainActivity, "filter device:" + name + " " + address, Toast.LENGTH_LONG).show();
                    }
                });
                */
                Log.w(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.w(TAG, "filter device:" + device.getName() + "," + device.getAddress());
                return;//filter other gatt server;
            }
            
            leDevice = mMainActivity.leService.le.searchRemoteGattClientListByAddr(device.getAddress());
            if (null != leDevice) {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mMainActivity, "already existed device:" + name + " " + address, Toast.LENGTH_LONG).show();
                    }
                });
                
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "already existed device:" + device.getName() + "," + device.getAddress());
                return;//already exists;
            }
            
            leDevice = new LeDevice(mMainActivity, device.getAddress());
            leDevice.name = device.getName() == null ? "nameUnknown" : device.getName();
            leDevice.deviceState = Macro.BleDeviceStateConnected;
            mMainActivity.leService.le.remoteGattClientList.add(leDevice);
            mMainActivity.leService.blePhyTestController.addPhyTestDevice(device);
            Log.e(TAG, "add to remoteGattClientList:" + device.getName() + "," + device.getAddress());/*bt dbg*/
            
            break;
        case BluetoothProfile.STATE_DISCONNECTED:
            mMainActivity.leService.le.deleteRemoteGattClientByAddr(device.getAddress());
            mMainActivity.leService.blePhyTestController.removePhyTestDevice(device);
            break;
        }
        
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if ((mMainActivity.section4Fragment != null) && (mMainActivity.section4Fragment.mAdapter != null)) {
                  mMainActivity.section4Fragment.mAdapter.notifyDataSetChanged();
              }
            }
        });

    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {

        if(status != BluetoothGatt.GATT_SUCCESS) {
            
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Log.e(TAG, "Called: " + this, here);
            
            
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "onServiceAdded failed!", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "onServiceAdded() failed, status=" + status);
            return;
        }
        
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Macro.CharacteristicUuidNotificationTest));
        if (null == characteristic) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Notification Test Characteristic can not be found.", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "Notification Test Characteristic can not be found.");  
            return;
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device,
            int requestId, int offset,
            BluetoothGattCharacteristic characteristic) {
        // TODO Auto-generated method stub
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device,
            int requestId, BluetoothGattCharacteristic characteristic,
            boolean preparedWrite, boolean responseNeeded, int offset,
            byte[] value) {
        // TODO Auto-generated method stub
        super.onCharacteristicWriteRequest(device, requestId, characteristic,
                preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device,
            int requestId, int offset,
            BluetoothGattDescriptor descriptor) {
        // TODO Auto-generated method stub
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device,
            int requestId, BluetoothGattDescriptor descriptor,
            boolean preparedWrite, boolean responseNeeded, int offset,
            byte[] value) {
        // TODO Auto-generated method stub
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite,
                responseNeeded, offset, value);
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId,
            boolean execute) {
        // TODO Auto-generated method stub
        super.onExecuteWrite(device, requestId, execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
        name = device.getName();
        address = device.getAddress();
        
        if(status == BluetoothGatt.GATT_SUCCESS) {
            leDevice = mMainActivity.leService.le.searchRemoteGattClientListByAddr(device.getAddress());
            if (null == leDevice) {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                    }
                });
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "can not find the remote device:" + device.getName() + " " + device.getAddress());
                return;
            }
            
            leDevice.txNotificationSuccessfullyCnt++;
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Send Notification to " + leDevice.name + " " + leDevice.address + " OK", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Send Notification to " + name + " " + address + " Fail", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "Send Notification to " + device.getName()+ " " + device.getAddress() + " Fail." + " status=" + status);
        }
    }
}


class BleMultiAdv {
    private static final String TAG = "BleMultiAdv";
    private MainActivity mMainActivity;
    
    public boolean isSendingAdv;
    public int advDataLength;
    public int respAdvDataLength;
    
    public BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseSettings.Builder mAdvertiseSettingsBuilder;
    private AdvertiseData.Builder mAdvertiseDataBuilder;
    private AdvertiseData.Builder mRespAdvertiseDataBuilder;
    private AdvertiseCallback mAdvertiseCallback;
    
    
    /* multi adv parameters */
    public int manufacturerId;
    public byte[] manufacturerSpecificData;
    public ParcelUuid serviceUuid;
    public ParcelUuid serviceDataUuid;
    public byte[] serviceData;
    public boolean includeName;
    public boolean isConnectable;
    public int respManufacturerId;
    public byte[] respManufacturerSpecificData;
    public ParcelUuid respServiceUuid;
    public ParcelUuid respServiceDataUuid;
    public byte[] respServiceData;
    public boolean respIncludeName;
    public boolean includeTxpowerLevel;
    public int txPowerLevel;
    public int advertiseMode;
    public int timeoutMillis;
    
    
    public BleMultiAdv(MainActivity mainActivity) {
        mMainActivity = mainActivity;

        mAdvertiseCallback = new AdvertiseCallback() {

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainActivity.section2Fragment.mAdvTypeTextView.setText(mMainActivity.section2Fragment.mAdvTypeTextView.getText().toString() + "   Sending");
                        mMainActivity.section2Fragment.mAdvResultTextView.setText("Adv Start Success.");
                    }
                });
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                Log.e(TAG, "errorCode=" + errorCode);/*bt dbg*/
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainActivity.section2Fragment.mAdvResultTextView.setText("Adv Start Failure.");
                    }
                });
            }

        };
        
        defaultConfig();
    }
    
    public void startAdv() {
        mAdvertiseSettingsBuilder = new AdvertiseSettings.Builder();
        mAdvertiseSettingsBuilder.setAdvertiseMode(advertiseMode).setConnectable(isConnectable).setTimeout(timeoutMillis).setTxPowerLevel(txPowerLevel);
        
        mAdvertiseDataBuilder = new AdvertiseData.Builder();
        mAdvertiseDataBuilder.setIncludeDeviceName(includeName).setIncludeTxPowerLevel(includeTxpowerLevel);
        if(manufacturerId != -1 && manufacturerSpecificData != null) {
            mAdvertiseDataBuilder.addManufacturerData(manufacturerId, manufacturerSpecificData);
        }
        if(serviceUuid != null)
        {
            mAdvertiseDataBuilder.addServiceUuid(serviceUuid);
        }
        if(serviceDataUuid != null && serviceData != null)
        {
            mAdvertiseDataBuilder.addServiceData(serviceDataUuid, serviceData);
        }
        
        mRespAdvertiseDataBuilder = new AdvertiseData.Builder();
        mRespAdvertiseDataBuilder.setIncludeDeviceName(respIncludeName).setIncludeTxPowerLevel(includeTxpowerLevel);
        if (respManufacturerId != -1 && respManufacturerSpecificData != null)
        {
            mRespAdvertiseDataBuilder.addManufacturerData(respManufacturerId, respManufacturerSpecificData);
        }
        
        if (respServiceUuid != null)
        {
            mRespAdvertiseDataBuilder.addServiceUuid(respServiceUuid);
        }
        
        if(respServiceDataUuid != null && respServiceData != null)
        {
            mRespAdvertiseDataBuilder.addServiceData(respServiceDataUuid, respServiceData);
        }
        
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettingsBuilder.build(), mAdvertiseDataBuilder.build(), mRespAdvertiseDataBuilder.build(), mAdvertiseCallback);
        isSendingAdv = true;
    }

    public void stopAdv() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        isSendingAdv = false;
    }

    public void clearConfig() {
        advDataLength = 0;
        respAdvDataLength = 0;
        
        manufacturerId = -1;
        manufacturerSpecificData = null;
        serviceUuid = null;
        serviceDataUuid = null;
        serviceData = null;
        includeName = false;
        isConnectable = false;
        respManufacturerId = -1;
        respManufacturerSpecificData = null;
        respServiceUuid = null;
        respServiceDataUuid = null;
        respServiceData = null;
        respIncludeName = false;
        includeTxpowerLevel = false;
        txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
        advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
        timeoutMillis = 1;
    }
    
    public void defaultConfig() {

        manufacturerId = 0x10;
        manufacturerSpecificData = new byte[2];
        manufacturerSpecificData[0] = 0x10;
        manufacturerSpecificData[1] = 0x20;
        serviceUuid = ParcelUuid.fromString(Macro.Default16bitUUID32);
        serviceDataUuid = ParcelUuid.fromString(Macro.Default16bitUUID16);
        serviceData = new byte[2];
        serviceData[0] = 0x30;
        serviceData[1] = 0x40;
        includeName = true;
        isConnectable = true;
        respManufacturerId = 0x20;
        respManufacturerSpecificData = new byte[2];
        respManufacturerSpecificData[0] = 0x50;
        respManufacturerSpecificData[1] = 0x60;
        respServiceUuid = ParcelUuid.fromString(Macro.Default16bitUUID32);
        respServiceDataUuid = ParcelUuid.fromString(Macro.Default16bitUUID16);
        respServiceData = new byte[2];
        respServiceData[0] = 0x70;
        respServiceData[1] = 0x01;
        respIncludeName = true;
        includeTxpowerLevel = false;
        txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
        advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_POWER;
        timeoutMillis = 100;
    }
}


class BleMultiAdvController {
    private static final String TAG = "BleMultiAdvController";
    public List<BleMultiAdv> multiAdvList = new ArrayList<BleMultiAdv>();//used within MultiAdvFragment
    public MainActivity mMainActivity;

    public void onCreate(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        
        for (int i = 0; i < Macro.MultiAdvMaxInstance; i++) {
            multiAdvList.add(i, new BleMultiAdv(mMainActivity));
        }
        
        setBluetoothLeAdvertiser();
    }
    
    public void onDestory() {
        for (BleMultiAdv bleMultiAdv : multiAdvList) {
            bleMultiAdv.stopAdv();
        }
    }
    
    public void setBluetoothLeAdvertiser() {

        //Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        
        if (null == mMainActivity.leService.le) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        }
        
        if (null == mMainActivity.leService.le.mBluetoothAdapter) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        }
        
        if (mMainActivity.leService.le.mBluetoothAdapter.isEnabled()) {
            for (int i = 0; i < Macro.MultiAdvMaxInstance; i++) {
                //multiAdvList.get(i).mBluetoothLeAdvertiser = mMainActivity.ble.mBluetoothAdapter.getBluetoothLeAdvertiser();
                multiAdvList.get(i).mBluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
                if (null == multiAdvList.get(i).mBluetoothLeAdvertiser) {
                    Log.e(TAG, "getBluetoothLeAdvertiser() failed!"); 
                }
            }
        } else {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        }
    }
    
    public BleMultiAdv selectAdv(int idx) {
        return multiAdvList.get(idx);
    }
}

class BleBatchScanConfig {
    private static final String TAG = "BleBatchScanConfig";
    private MainActivity mMainActivity;
    private int currentConfigIndex;
    public boolean isScanning;
    
    /* batch scan parameters */
    public int manufacturerId;
    public byte[] manufacturerSpecificData;
    public ParcelUuid serviceUuid;
    public ParcelUuid serviceDataUuid;
    public byte[] serviceData;
    public byte[] serviceDataMask;
    public String deviceAddress;
    public String deviceName;
    public long reportDelayMillis;
    public int scanMode;
    public int callbackType;
    
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings mScanSetting;
    private List<ScanFilter> scanFilterList;
    private ScanCallback mScanCallback;
    private int count;
    String str1, str2;
    
    public BleBatchScanConfig(MainActivity mainActivity, int idx) {
        mMainActivity = mainActivity;
        currentConfigIndex = idx;
        scanFilterList = new ArrayList<ScanFilter>();
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        if (mBluetoothLeScanner == null) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            Log.e(TAG, "mBluetoothLeScanner == null");
        }
        
        mScanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                
                if (currentConfigIndex != mMainActivity.section3Fragment.currentConfigIndex) {
                    return;
                }
                
                count = 0;
                
                Log.e(TAG, "onScanResult,callbackType=" + callbackType);/*bt dbg*/

                
                str1 = result.getDevice().toString();
                if (result.getDevice().getName() != null) {
                    str1 = str1 + " " + result.getDevice().getName();
                }
                str2 = str1 + " " + result.toString();

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainActivity.section3Fragment.mScanCountTextView.setText("It's not a batch scan.");
                        mMainActivity.section3Fragment.mScanResultTextView.setText(str1);
                    }
                });

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                if (currentConfigIndex != mMainActivity.section3Fragment.currentConfigIndex) {
                    return;
                }
                
                count = 0;
                str1 = "Results\n";
                Iterator<ScanResult> iterator = results.iterator();
                
                Log.e(TAG, "onBatchScanResults");/*bt dbg*/
                
                while (true) {
                    if (!iterator.hasNext()) {
                        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                        Log.e(TAG, "str1=" + str1);/*bt dbg*/
                        
                        mMainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMainActivity.section3Fragment.mScanCountTextView.setText("Number of Scan Results:" + count);
                                if (str1 != null) {
                                    mMainActivity.section3Fragment.mScanResultTextView.setText(str1);
                                }
                            }
                        });
                        return;
                    }
                    
                    final ScanResult scanResult = iterator.next();
                    
                    count++;
                    str2 = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(str1)).append("\n").toString())).append("\nDevice").append(count).toString() + "==>" + scanResult.getDevice().toString();
                    if (scanResult.getDevice().getName() != null) {
                        str2 = str2 + " " + scanResult.getDevice().getName();
                    }
                    
                    str1 = str2 + " " + scanResult.getScanRecord().toString();
                    Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                    Log.e(TAG, "str1=" + str1);/*bt dbg*/
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "onScanFailed,errorCode=" + errorCode + ",currentConfigIndex=" + currentConfigIndex);
                // TODO Auto-generated method stub
                super.onScanFailed(errorCode);
            }
            
        };
        
        defaultConfig();
    }

    public void startScan() {
        isScanning = true;
        ScanSettings.Builder builder = new ScanSettings.Builder().setScanMode(scanMode).setReportDelay(reportDelayMillis).setCallbackType(callbackType);

        Class<?> c = builder.getClass();
        Method method = null;
        int SCAN_RESULT_TYPE_FULL = 0;

        try {
            method = c.getDeclaredMethod("setScanResultType", int.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "NoSuchMethodException"); 
            return;
        }
        
        method.setAccessible(true);
        try {
            builder = (android.bluetooth.le.ScanSettings.Builder) method.invoke(builder, SCAN_RESULT_TYPE_FULL);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "IllegalAccessException");
            return;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "IllegalArgumentException");
            return;
        } catch (InvocationTargetException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "InvocationTargetException");
            return;
        }
        
        mScanSetting = builder.build();
        if(mScanSetting == null) {
            Log.e(TAG, "mScanSetting == null");
            return;
        }
        
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        if ((manufacturerId != -1) && (manufacturerSpecificData != null) && (!manufacturerSpecificData.equals(""))) {
            scanFilterBuilder.setManufacturerData(manufacturerId, manufacturerSpecificData);
        }
        
        if ((deviceName != null) && (!this.deviceName.equals(""))) {
            scanFilterBuilder.setDeviceName(deviceName);
        }
        
        if ((deviceAddress != null) && (!deviceAddress.equals(""))) {
            scanFilterBuilder.setDeviceAddress(deviceAddress);
        }
        
        if ((serviceData != null) && (!serviceData.equals("")) && (serviceDataUuid != null)) {
            scanFilterBuilder.setServiceData(serviceDataUuid, serviceData, serviceDataMask);
        }
        
        if ((serviceUuid != null) && (!serviceUuid.equals(""))) {
            scanFilterBuilder.setServiceUuid(this.serviceUuid);
        } 
        
        ScanFilter scanFilter = scanFilterBuilder.build();
        scanFilterList.add(scanFilter);

        mBluetoothLeScanner.startScan(scanFilterList, mScanSetting, mScanCallback);
    }
    
    public void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            isScanning = false;
        }
    }
    
    public void clearConfig() {
        manufacturerId = -1;
        manufacturerSpecificData = null;
        serviceUuid = null;
        serviceDataUuid = null;
        serviceData = null;
        serviceDataMask = null;
        deviceAddress = null;
        deviceName = null;
        reportDelayMillis = 0;
        scanMode = ScanSettings.SCAN_MODE_LOW_POWER;
        callbackType = ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
    }
    
    public void defaultConfig() {
        manufacturerId = -1;
        manufacturerSpecificData = null;
        serviceUuid = null;
        serviceDataUuid = null;
        serviceData = null;
        serviceDataMask = null;
        
        deviceAddress = null;
        deviceName = null;
        reportDelayMillis = 1000;
        scanMode = ScanSettings.SCAN_MODE_LOW_POWER;
        callbackType = ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
    }
}

class BleBatchScanController {
    private static final String TAG = "BleBatchScanController";
    public List<BleBatchScanConfig> batchScanConfigList = new ArrayList<BleBatchScanConfig>();//used within BatchScanFragment
    public MainActivity mMainActivity;

    public void onCreate(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        
        for (int i = 0; i < Macro.BatchScanMaxInstance; i++) {
            batchScanConfigList.add(i, new BleBatchScanConfig(mMainActivity, i));
        }
    }

    public BleBatchScanConfig selectConfig(int idx) {
        return batchScanConfigList.get(idx);
    }
}

class BlePhyTestConfig {
    private static final String TAG = "BlePhyTestConfig";
    private MainActivity mMainActivity;
    private int connection_status;
    private BluetoothDevice connected_device;
    private int tx_phy;
    private int rx_phy;
    private int tx_phy_to_set;
    private int rx_phy_to_set;
    
    public BlePhyTestConfig(BluetoothDevice dev) {
    	connected_device = dev;
    	tx_phy = 1;
    	rx_phy = 1;
    }
    
    public BlePhyTestConfig(BluetoothDevice dev, int tx, int rx) {
    	connected_device = dev;
    	tx_phy = tx;
    	rx_phy = rx;
    }
    
    public BluetoothDevice getDevice() {
    	return connected_device;
    }
    
    public int getTxPhy() {
    	return tx_phy;
    }
    
    public int getRxPhy() {
    	return rx_phy;
    }
    
    public void setToSetTxPhy(int tx) {
    	tx_phy_to_set = tx;
    }
    
    public void setToSetRxPhy(int rx) {
    	rx_phy_to_set = rx;
    }
    
    public int getToSetTxPhy() {
    	return tx_phy_to_set;
    }
    
    public int getToSetRxPhy() {
    	return tx_phy_to_set;
    }
}


class BlePhyTestController {
    private static final String TAG = "BlePhyTestController";
    public ArrayList<BlePhyTestConfig> mPhyDevicetList = new ArrayList<BlePhyTestConfig>();//used within BlePhyTestConfig
    public MainActivity mMainActivity;

    public void onCreate(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        BluetoothDevice temp_device;
        
        
    }

	public void addPhyTestDevice(BluetoothDevice device) {
		BlePhyTestConfig conf = new BlePhyTestConfig(device);
		
		mPhyDevicetList.add(conf);
    }
	
	public void removePhyTestDevice(BluetoothDevice device) {
		BlePhyTestConfig conf = new BlePhyTestConfig(device);
		int xx = 0;
		for(xx = 0; xx < mPhyDevicetList.size(); xx++) {
			if(mPhyDevicetList.get(xx).getDevice().equals(device)) {
				Log.d(TAG, "FOUND device to remove:" + device.getAddress().toString());
				mPhyDevicetList.remove(xx);
			}
		}
        if(xx == mPhyDevicetList.size()) {
        	Log.e(TAG, "Not Find device to remove:" + device.getAddress().toString());
        }
    }
	
	public void setPhy(BluetoothDevice dev, int tx, int rx) {
    	Log.d(TAG, "set phy" + dev.getAddress().toString() +" phy: tx=" + tx + "; rx=" + rx);
    }
    
    public void getPhy(BluetoothDevice dev) {
    	Log.d(TAG, "get phy:");
    }
}



