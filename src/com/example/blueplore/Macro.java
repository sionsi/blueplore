package com.example.blueplore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.Manifest;
import android.os.ParcelUuid;
import android.util.Log;

public class Macro {
    private static final String TAG = "Macro";
    
	public static final int RequestCodeBtEnable = 0;
	
	public static final int BleDeviceStateInit = 0;
	public static final int BleDeviceStateScanned = 10;
	public static final int BleDeviceStateConnecting = 11;//the state belongs to Scanned state within statistic
	public static final int BleDeviceStatePairing = 12;//the state belongs to Scanned state within statistic
	public static final int BleDeviceStateConnected = 20;//Connected state can not transfer to Paired state directly, indicates remote gatt server device
	public static final int BleDeviceStatePaired = 30;
	
	
	public static final String PIN = "aaaaaa";
	//public static final byte[] PIN = {'1', '1', '1', '1', '1', '1'};
	
	public static final String[] Permissions = {
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.ACCESS_FINE_LOCATION
    };
	
	
	public static final int LeScanConnectViewTypePairedText = 1;
	public static final int LeScanConnectViewTypePairedDevice = 2;
	public static final int LeScanConnectViewTypeConnectedText = 3;
    public static final int LeScanConnectViewTypeConnectedDevice = 4;
	public static final int LeScanConnectViewTypeScannedText = 5;
	public static final int LeScanConnectViewTypeScannedDevice = 6;
	public static final int LeScanConnectViewTypeMaxNum = 6;
	
	public static final int MultiAdvMaxInstance = 4;
	public static final int BatchScanMaxInstance = 4;

	public static final String Default16bitUUID16 = "00001801-0000-1000-8000-00805F9B34FB";
	public static final String Default16bitUUID32 = "12345678-0000-1000-8000-00805F9B34FB";
	public static final String Default16bitUUID128 = "12345678-1234-5678-1234-5678ABCDEF12";
	
	public static final int PacketMaxLength = 251;

	
	
	//GATT server service and char define
	public static final String ServiceUuidNotificationTest = "00005000-0000-1000-8000-00805F9B34FB";
	public static final String ServiceNameNotificationTest = "Notification Test Service";
	public static final String CharacteristicUuidNotificationTest = "0000a00b-0000-1000-8000-00805f9b34fb";
	public static final String CharacteristicNameNotificationTest = "Notification Test Characteristic";
	
	
	
	public static final int PhyMaxNumber = 4;
	
	
	
	
	
	
    public static String[] splitStringEvery(String paramString, int paramInt)
    {
        String[] arrayOfString = new String[(int)Math.ceil(paramString.length() / paramInt)];
        int i = 0;
        int j = -1 + arrayOfString.length;
        
        for (int k = 0; ; k++)
        {
            if (k >= j)
            {
                arrayOfString[j] = paramString.substring(i);
                return arrayOfString;
            }
            arrayOfString[k] = paramString.substring(i, i + paramInt);
            i += paramInt;
        }
    }
    
    public static boolean is16BitUuid(ParcelUuid parcelUuid) {
        Class c = null;
        Method method = null;
        Boolean res = null;

        try {
            c = Class.forName("android.bluetooth.BluetoothUuid");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }

        try {
            method = c.getMethod("is16BitUuid", ParcelUuid.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }
        
        try {
            res = (Boolean)method.invoke(null, parcelUuid);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }
         
        //Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
        //Log.e(TAG, "res.booleanValue()=" + res.booleanValue());   
        return res.booleanValue();
    }
    
    public static boolean is32BitUuid(ParcelUuid parcelUuid) {
        Class c = null;
        Method method = null;
        Boolean res = null;

        try {
            c = Class.forName("android.bluetooth.BluetoothUuid");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }

        try {
            method = c.getMethod("is32BitUuid", ParcelUuid.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }
        
        try {
            res = (Boolean)method.invoke(null, parcelUuid);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
            e.printStackTrace();
        }
          
        return res.booleanValue();
    }
    
    public static final String bytesToHexString(byte[] bArray) {
        
        if (null == bArray) {
            return null;
        }
        
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
    
	
	
}
