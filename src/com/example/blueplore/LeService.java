package com.example.blueplore;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;




public class LeService extends Service {
    static final String TAG = "LeService";
    
    private final IBinder binder = new LocalBinder();
    public MainActivity mMainActivity;
    public Le le;
    public BleMultiAdvController bleMultiAdvController;
    public BleBatchScanController bleBatchScanController;
    public BlePhyTestController blePhyTestController;
    
    //used for test
    void showToast() {
        Toast.makeText(mMainActivity, "LeService::showToast()", Toast.LENGTH_LONG).show();
    }
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        le = new Le();
        bleMultiAdvController = new BleMultiAdvController();
        bleBatchScanController = new BleBatchScanController();
        blePhyTestController = new BlePhyTestController();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public LeService getService() {
            return LeService.this;
        }
    }
    
}
