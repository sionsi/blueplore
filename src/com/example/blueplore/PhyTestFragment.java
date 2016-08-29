package com.example.blueplore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.blueplore.MainActivity.PlaceholderFragment;

public class PhyTestFragment extends PlaceholderFragment {

    private static final String TAG = "PhyTestFragment";
    private MainActivity mMainActivity;
    private View mView;
    
    
    public TextView mCurrentPhyTextView;
    public TextView mCurrentChannelTextView;
    public TextView mGattConnectionStatus;
    
    private Spinner mSetPhyTypeSpinner;
    
    String[] mPhyTypeSpinnerValue = { "Choose", "1M", "2M", "coded(s=2)", "coded(s=8)" };
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mMainActivity = (MainActivity)getActivity();
		mView = inflater.inflate(R.layout.section5, container, false);
		
		mGattConnectionStatus = (TextView)mView.findViewById(R.id.section5_connection_status);
		mCurrentPhyTextView = (TextView)mView.findViewById(R.id.section5_current_phy);
		mSetPhyTypeSpinner = (Spinner)mView.findViewById(R.id.section5_set_phy_spinner);
		
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
    public void setPhy () {

    }

}
