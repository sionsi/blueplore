package com.example.blueplore;


import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    
    
    
    private static final String TAG = "MainActivity";
    
    public int mCurrentSelectedSection = 0;//0 indicates section1; 1 indicates section2;
    
    private static boolean  serviceConnected = false;
    private final ServiceConnection mServiceConnection = new LeServiceConnection();

    public LeService leService;
    public LeConnectFragment section1Fragment;
    public MultiAdvFragment section2Fragment;
    public BatchScanFragment section3Fragment;
    public RemoteGattClientFragment section4Fragment;
    public PhyTestFragment mPhyFragment;
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        leInit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        PlaceholderFragment placeholderFragment = PlaceholderFragment.newInstance(position + 1);
        
        if (true == serviceConnected) {
            switch (position + 1) {
            case 1://section1
                section1Fragment = (LeConnectFragment)placeholderFragment;
                break;
            case 2://section2
                section2Fragment = (MultiAdvFragment)placeholderFragment;
                break;
            case 3://section3
                section3Fragment = (BatchScanFragment)placeholderFragment;
                break;
            case 4://section4
                section4Fragment = (RemoteGattClientFragment)placeholderFragment;
                break;
            case 5://section4
            	mPhyFragment = (PhyTestFragment)placeholderFragment;
                break;
            default:
                Log.e(TAG, "position = "+ position); 
            }
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, placeholderFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
            Log.e(TAG, "mCurrentSelectedSection=" + mCurrentSelectedSection);/*bt dbg*/
            
            switch (mCurrentSelectedSection) {
            case 0:
                getMenuInflater().inflate(R.menu.section1, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.section2, menu);
                break;
            case 2:
                getMenuInflater().inflate(R.menu.section3, menu);
                break;
            case 3:
                getMenuInflater().inflate(R.menu.section4, menu);
                break;
            case 4:
                getMenuInflater().inflate(R.menu.phy_menu, menu);
                break;

            default:
                Log.e(TAG, "err: mCurrentSelectedSection=" + mCurrentSelectedSection);
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        
        if (true == serviceConnected) {
            switch (item.getItemId()) {
            
            case R.id.section1_action_ble_scan_start:
                leService.le.startScan();
                return true;
            case R.id.section1_action_ble_scan_stop:
                leService.le.stopScan();
                return true;

            case R.id.section2_action_adv_select:
                section2Fragment.selectAdvDialog();
                return true;
            case R.id.section2_action_adv_start:
                section2Fragment.startSendAdv();
                return true;
            case R.id.section2_action_adv_stop:
                section2Fragment.stopSendAdv();
                return true;
            case R.id.section2_action_adv_cfg_save:
                section2Fragment.saveConfig();
                return true;
            case R.id.section2_action_adv_cfg_reset:
                section2Fragment.resetConfig();
                return true;
            case R.id.section2_action_adv_cfg_clear:
                section2Fragment.clearConfig();
                return true;
                
            case R.id.section3_action_select:
                section3Fragment.selectConfigDialog();
                return true;
            case R.id.section3_action_start:
                section3Fragment.startScan();
                return true;
            case R.id.section3_action_stop:
                section3Fragment.stopScan();
                return true;
            case R.id.section3_action_save:
                section3Fragment.saveConfig();
                return true;
            case R.id.section3_action_reset:
                section3Fragment.resetConfig();
                return true;
            case R.id.section3_action_clear:
                section3Fragment.clearConfig();
                return true;
                
            case R.id.action_bt_adapter:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                return true;
            case R.id.action_exit:
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        } else {
            switch (item.getItemId()) {
            
            case R.id.action_bt_adapter:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                return true;
            case R.id.action_exit:
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            //PlaceholderFragment fragment = new PlaceholderFragment();
            PlaceholderFragment fragment;
            
            if (false == serviceConnected) {
                fragment = new PlaceholderFragment();
            } else {
                switch (sectionNumber) {
                case 1:
                    fragment = new LeConnectFragment();
                    Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");/*bt dbg*/
                    break;
                case 2:
                    fragment = new MultiAdvFragment();
                    Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    break;
                case 3:
                    fragment = new BatchScanFragment();
                    Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    break;
                case 4:
                    fragment = new RemoteGattClientFragment();
                    Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    break;
                case 5:
                    fragment = new PhyTestFragment();
                    Log.d(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                    break;

                default:
                    Log.e(TAG, "sectionNumber = "+ sectionNumber);
                    return null;
                }
            }

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        
        if (leService != null) {
            unbindService(mServiceConnection);
            leService = null;
        }
    }

    public class LeServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            leService = ((LeService.LocalBinder)service).getService();

            if (null == leService) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "leService = null.", Toast.LENGTH_LONG).show();
                    }
                });
                Log.e(TAG, "["+Thread.currentThread().getStackTrace()[2].getFileName()+","+Thread.currentThread().getStackTrace()[2].getLineNumber()+","+Thread.currentThread().getStackTrace()[2].getMethodName()+"]");
                Log.e(TAG, "leService = null.");
            }

            Log.v(TAG, "LeService connected successfully.");
            serviceConnected = true;
            leService.mMainActivity = MainActivity.this;
            leService.le.onCreate(MainActivity.this);
            leService.bleMultiAdvController.onCreate(MainActivity.this);
            leService.bleBatchScanController.onCreate(MainActivity.this);
            leService.blePhyTestController.onCreate(MainActivity.this);

            onNavigationDrawerItemSelected(0);//create real section1, the first section fragment;
            
            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leService.showToast();
                }
            });
            */
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            leService = null;  
        }
        
    }
    
    void leInit () {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();//turn on bt
                    
                    while (false == bluetoothAdapter.isEnabled()) {}
                }
                
                Intent bindIntent = new Intent(MainActivity.this, LeService.class);
                boolean res = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                if (false == res) {
                    Log.e(TAG, "Bind to LeService failed.");
                    finish();
                }
            }
        }).start();
    }
    
    
    
}
