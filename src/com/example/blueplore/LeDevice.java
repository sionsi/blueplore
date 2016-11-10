package com.example.blueplore;


import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class LeDevice {

    private static final String TAG = "BleDevice";
    MainActivity mMainActivity;

    public String address;
    public String name;
    public byte[] rxPayload;//the last packet payload received from the remote device
    public int rxCnt;//local device received from the remote device
    public int txCnt;//local device sent to the remote device

    //gatt client field
    public int deviceState;//just scanned,paired,connected
    public BluetoothGattCallback gattCallback;
    public BluetoothGatt bluetoothGatt;
    public int rxNotificationCnt;
    public int mtu;

    //gatt server field
    public int txNotificationCnt;
    public int txNotificationSuccessfullyCnt;
    
    
    
    @Override
    public boolean equals(Object o) {
        
        if(this == o) {
            return true;
        } 
        
        if (o != null && o instanceof LeDevice) {
            LeDevice leDevice = (LeDevice) o;
            return (this.address.equals(leDevice.address));
        } else {
            return false;
        }
    }

    public LeDevice(MainActivity mainActivity, String address)
    {
        mMainActivity = mainActivity;
        
        this.address = address;
        this.name = "NameUnknown";
        this.deviceState = Macro.BleDeviceStateInit;
    }
    
    public void pair(String pin) {
        boolean res;
        
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        int state = device.getBondState();
        if(state != BluetoothDevice.BOND_NONE)
        {
            Toast.makeText(mMainActivity, "the remote device is not bond none:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device is not bond none:" + name + " " + address + ",BondState=" + state);
            return;
        }

        res = device.createBond();
        if (res == false) {
            Toast.makeText(mMainActivity, "the remote device bonding begin failed:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device bonding begin failed:" + name + " " + address + ",BondState=" + state);
            return;
        }
        
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        Log.e(TAG, "Pair:" + name + " " + address + ",BondState=" + state);
        //dbg deviceState = Macro.BleDeviceStatePairing;
        return;
    }
    
    private boolean removeBond(BluetoothDevice btDevice) throws Exception {  
        Method removeBondMethod = btDevice.getClass().getMethod("removeBond");  
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }
    
    public void unpair() {
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }

        int state = device.getBondState();
        if(state != BluetoothDevice.BOND_BONDED)
        {
            Toast.makeText(mMainActivity, "the remote device is not bonded:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device is not bonded:" + name + " " + address + ",BondState=" + state);
            return;
        }
        
        try {
            boolean res = removeBond(device);
            if (res == false) {
                Toast.makeText(mMainActivity, "the remote device remove bond error:" + name + " " + address, Toast.LENGTH_LONG).show();
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "the remote device remove bond error:" + name + " " + address + ",BondState=" + state);
                return;
            }
        } catch (Exception e) {
            Toast.makeText(mMainActivity, "the remote device is not bonded:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return;
    }
    
    public void gattClientConnect() {
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        int connectionState = mMainActivity.leService.le.mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if(connectionState != BluetoothProfile.STATE_DISCONNECTED)
        {
            Toast.makeText(mMainActivity, "the remote device is not disconnected:" + device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device is not disconnected:" + device.getName() + " " + device.getAddress() + " connectionState = " + connectionState);
            return;
        }

        gattCallback = new LeGattClientCallback(mMainActivity);
        bluetoothGatt = device.connectGatt(mMainActivity, false, gattCallback);
        //bluetoothGatt.connect();

        deviceState = Macro.BleDeviceStateConnecting;
        return;
    }
    
    public void gattClientDisconnect() {
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        /*
        int connectionState = mMainActivity.ble.mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if(connectionState != BluetoothProfile.STATE_CONNECTED)
        {
            Toast.makeText(mMainActivity, "the remote device is not connected:" + device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device is not connected:" + device.getName() + " " + device.getAddress() + " connectionState = " + connectionState);
            return;
        }
        */
        
        bluetoothGatt.disconnect();
        //bluetoothGatt.close();
        
        return;
    }
    
    public void gattServerDisconnect() {
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        int connectionState = mMainActivity.leService.le.mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if(connectionState != BluetoothProfile.STATE_CONNECTED)
        {
            Toast.makeText(mMainActivity, "the remote device is not connected:" + device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the remote device is not connected:" + device.getName() + " " + device.getAddress() + " connectionState = " + connectionState);
            return;
        }
        
        mMainActivity.leService.le.mBluetoothGattServer.cancelConnection(device);

    }
    
    //A notification is sent to the remote gatt client device
    public void gattServerSentNotification(byte[] payload) {
        boolean res;
        
        res = mMainActivity.leService.le.mBluetoothGattCharacteristic.setValue(payload);
        if (false == res) {
            Toast.makeText(mMainActivity, "the requested value could not be stored locally,remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "the requested value " + Macro.bytesToHexString(payload) + " could not be stored locally,remote device:" + name + " " + address);  
            return;
        }
        
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (null == device) {
            Toast.makeText(mMainActivity, "The remote device not found: " + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The remote device not found: " + name + " " + address);  
            return;
        }
        
        int connectionState = mMainActivity.leService.le.mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if (BluetoothProfile.STATE_CONNECTED != connectionState) {
            Toast.makeText(mMainActivity, "The remote device disconnected: " + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The remote device disconnected: " + name + " " + address + "  connectionState=" + connectionState);  
            return;
        }
        
        res = mMainActivity.leService.le.mBluetoothGattServer.notifyCharacteristicChanged(device, mMainActivity.leService.le.mBluetoothGattCharacteristic, false);
        if (false == res) {
            Toast.makeText(mMainActivity, "The notification has been triggered unsuccessfully,remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The notification has been triggered unsuccessfully,remote device:" + name + " " + address);
            return;
        }
        
        txCnt++;
        txNotificationCnt++;
        //Toast.makeText(mMainActivity, "Notification:" + Macro.bytesToHexString(payload) + " send to remote device:" + Name + " " + Address, Toast.LENGTH_LONG).show();
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        Log.e(TAG, "Notification:" + Macro.bytesToHexString(payload) + ".");
        Log.e(TAG, "Notification length=" + payload.length + " send to remote device:" + name + " " + address);
    }
    
    public void gattClientExchangeMtu(int size) {
        final BluetoothDevice device = mMainActivity.leService.le.mBluetoothAdapter.getRemoteDevice(address);
        if (null == device) {
            Toast.makeText(mMainActivity, "The remote device not found: " + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The remote device not found: " + name + " " + address);  
            return;
        }

        int connectionState = mMainActivity.leService.le.mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        if (BluetoothProfile.STATE_CONNECTED != connectionState) {
            Toast.makeText(mMainActivity, "The remote device disconnected: " + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The remote device disconnected: " + name + " " + address + "  connectionState=" + connectionState);  
            return;
        }

        boolean res = bluetoothGatt.requestMtu(size);
        if (false == res) {
            Toast.makeText(mMainActivity, "The new MTU value has been requested unsuccessfully: " + name + " " + address, Toast.LENGTH_LONG).show();
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "The new MTU value has been requested unsuccessfully: " + name + " " + address + " mtu=" + size);  
            return;
        }
    }
    

    //used by gatt client
    public void gattClientNotificationTestEnable(boolean enable) {
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(Macro.ServiceUuidNotificationTest));
        if (null == service) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Notification Test Service can not be found within the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "Notification Test Service can not be found within the remote device:" + name + " " + address);  

            return;
        }
        
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Macro.CharacteristicUuidNotificationTest));
        if (null == characteristic) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Notification Test Characteristic can not be found within the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "Notification Test Characteristic can not be found within the remote device:" + name + " " + address);  

            return;
        }
        
        boolean res = bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (false == res) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "set Characteristic Notification failed within the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "setCharacteristicNotification() failed within the remote device:" + name + " " + address);  

            return;
        }
    }
}


class LeBondReceiver extends BroadcastReceiver {

    private static final String TAG = "LeBondReceiver";
    private MainActivity mMainActivity;
    private BluetoothDevice device;
    
    public LeBondReceiver(MainActivity mMainActivity) {
        super();
        this.mMainActivity = mMainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int type = 0, bondState = 0;
        LeDevice remoteGattClientLeDevice = null, remoteGattServerLeDevice = null, remoteLeDevice = null;
        boolean res;

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE , BluetoothDevice.ERROR);
            
            Log.v(TAG, "ACTION_BOND_STATE_CHANGED::" + "bondState=" + bondState + ",Name=" + device.getName() + ",address=" + device.getAddress()); 

            switch (bondState) {
            case BluetoothDevice.BOND_NONE:

                remoteLeDevice = mMainActivity.leService.le.searchDeviceListByAddr(device.getAddress());
                if (null == remoteLeDevice) {
                    Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    Log.e(TAG, "can not find the remote device:" + device.getName() + " " + device.getAddress());
                    return;
                }
                
                if (Macro.BleDeviceStatePaired != remoteLeDevice.deviceState) {
                    return;//only remove paired remote deivce;
                }
                
                //remove remoteGattClientLeDevice from the device list
                mMainActivity.leService.le.deleteDeviceByAddr(device.getAddress());
                mMainActivity.leService.le.freshStatistics();
                if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
                    mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
                }
                break;
                
            case BluetoothDevice.BOND_BONDING:
                break;
                
            case BluetoothDevice.BOND_BONDED:
                remoteGattServerLeDevice = mMainActivity.leService.le.searchDeviceListByAddr(device.getAddress());
                if (null == remoteGattServerLeDevice) {
                    //add remoteGattClientLeDevice to the device list
                    remoteGattClientLeDevice = new LeDevice(mMainActivity, device.getAddress());
                    remoteGattClientLeDevice.name = device.getName() == null ? "nameUnknown" : device.getName();
                    remoteGattClientLeDevice.deviceState = Macro.BleDeviceStatePaired;
                    mMainActivity.leService.le.deviceList.add(remoteGattClientLeDevice);
                    mMainActivity.leService.le.freshStatistics();
                    if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
                        mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
                    }
                } else {
                    remoteGattServerLeDevice.deviceState = Macro.BleDeviceStatePaired;
                    mMainActivity.leService.le.freshStatistics();
                    if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
                        mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }

        } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

            Toast.makeText(mMainActivity, "ACTION_PAIRING_REQUEST," + "Name=" + device.getName() + ",address=" + device.getAddress(), Toast.LENGTH_LONG).show();
            Log.v(TAG, "ACTION_PAIRING_REQUEST::" + "type=" + type + ",Name=" + device.getName() + ",address=" + device.getAddress()); 

            
            switch (type) {
            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                int passkey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                if (passkey == BluetoothDevice.ERROR) {
                    Log.e(TAG, "Invalid Confirmation Passkey received, not showing any dialog");
                    return;
                }
                
                String mPairingKey = String.format(Locale.US, "%06d", passkey);
                Log.e(TAG, "mPairingKey=" + mPairingKey);

            case 3/*BluetoothDevice.PAIRING_VARIANT_CONSENT*/:
                res = device.setPairingConfirmation(true);
                if (false == res) {
                    Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    Log.e(TAG, "setPairingConfirmation()  failed," + device.getName() + " " + device.getAddress());
                }
                break;
            default:
                Log.e(TAG, "unknown BluetoothDevice.EXTRA_PAIRING_VARIANT=" + type);
                break;
            }
        }
    }
}

class LeGattClientCallback extends BluetoothGattCallback {
    
    private static final String TAG = "LeGattClientCallback";
    private MainActivity mMainActivity;
    
    LeDevice leDevice;
    int mtuSize;
    String name, address;
    

    public LeGattClientCallback(MainActivity mMainActivity) {
        super();
        this.mMainActivity = mMainActivity;
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        
        name = gatt.getDevice().getName();
        address = gatt.getDevice().getAddress();
        
        leDevice = mMainActivity.leService.le.searchDeviceListByAddr(gatt.getDevice().getAddress());
        if (null == leDevice) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });

            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        leDevice.gattClientNotificationTestEnable(true);
        
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mMainActivity, "GATT client: onServicesDiscovered() done within the remote device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
            }
        });
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        Log.e(TAG, "GATT client: onServicesDiscovered() done within the remote device:" + leDevice.name + " " + leDevice.address);  
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic, int status) {
        // TODO Auto-generated method stub
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic, int status) {
        // TODO Auto-generated method stub
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor, int status) {
        // TODO Auto-generated method stub
        super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor, int status) {
        // TODO Auto-generated method stub
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        // TODO Auto-generated method stub
        super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        // TODO Auto-generated method stub
        super.onReadRemoteRssi(gatt, rssi, status);
    }
    
    

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        
        mtuSize = mtu;
        name = gatt.getDevice().getName();
        address = gatt.getDevice().getAddress();
        
        leDevice = mMainActivity.leService.le.searchDeviceListByAddr(gatt.getDevice().getAddress());
        if (null == leDevice) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });

            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        if (BluetoothGatt.GATT_SUCCESS == status) {
            leDevice.mtu = mtu;
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Mtu " + mtuSize + " exchanged OK with the remote device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "Mtu " + mtuSize + " exchanged failed with the remote device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
                }
            });
            
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + leDevice.name + " " + leDevice.address + " status=" + status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic) {
        
        //Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
        name = gatt.getDevice().getName();
        address = gatt.getDevice().getAddress();
        
        
        //receive notification
        leDevice = mMainActivity.leService.le.searchDeviceListByAddr(gatt.getDevice().getAddress());
        if (null == leDevice) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }

        leDevice.rxPayload = characteristic.getValue();
        leDevice.rxCnt++;
        leDevice.rxNotificationCnt++;
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mMainActivity, "received notification from remote server device:" + leDevice.name + " " + leDevice.address, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
            int newState) {
        
        name = gatt.getDevice().getName();
        address = gatt.getDevice().getAddress();
        
        Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
        Log.e(TAG, "LeGattClientCallback::onConnectionStateChange() newState=" + newState + "," + name + "," + address);/*bt dbg*/
        
        if (BluetoothProfile.STATE_DISCONNECTED == newState) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "LeGattClientCallback::Remote device disconnected:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
        }
        
        
        leDevice = mMainActivity.leService.le.searchDeviceListByAddr(gatt.getDevice().getAddress());
        if (null == leDevice) {
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, "can not find the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                }
            });
            
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            Log.e(TAG, "can not find the remote device:" + name + " " + address);
            return;
        }
        
        
        switch (newState) {
        case BluetoothProfile.STATE_CONNECTED:
            leDevice.deviceState = Macro.BleDeviceStateConnected;
            //mPhyDevicetList
            mMainActivity.leService.blePhyTestController.addPhyTestDevice(gatt.getDevice());
            boolean res = gatt.discoverServices();
            if (false == res) {
                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mMainActivity, "service discovery not started with the remote device:" + name + " " + address, Toast.LENGTH_LONG).show();
                    }
                });
                
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "service discovery not started with the remote device:" + name + " " + address);
                return;
            }
            break;
        case BluetoothProfile.STATE_DISCONNECTED:
            mMainActivity.leService.le.deleteDeviceByAddr(address);
            mMainActivity.leService.blePhyTestController.removePhyTestDevice(gatt.getDevice());
            break;
        }
        
        mMainActivity.leService.le.freshStatistics();
        
        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mMainActivity.section1Fragment != null) && (mMainActivity.section1Fragment.mAdapter != null)) {
                    mMainActivity.section1Fragment.mAdapter.notifyDataSetChanged();
                }
                
                if ((mMainActivity.section4Fragment != null) && (mMainActivity.section4Fragment.mAdapter != null)) {
                    mMainActivity.section4Fragment.mAdapter.notifyDataSetChanged();
                }
                
                if ((mMainActivity.mPhyFragment != null) && (mMainActivity.mPhyFragment.mAdapter != null)) {
                    mMainActivity.mPhyFragment.mAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}

