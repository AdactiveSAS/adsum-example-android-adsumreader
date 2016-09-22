package com.adactive.AdsumReader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.zxing.integration.android.IntentIntegrator;


public class DescriptionFragment extends MainActivity.PlaceholderFragment {

    public static DescriptionFragment newInstance() {
        return new DescriptionFragment();
    }

    public DescriptionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView mScrollView = (ScrollView) inflater.inflate(R.layout.fragment_description, container, false);

        Button scanQr = (Button) mScrollView.findViewById(R.id.scanQRButton);
        final Fragment desc = this;
        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startScanning();
            }
        });

        return mScrollView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((MainActivity)getActivity()).handleQRCode(requestCode,resultCode,data);
    }
    private void startScanning()
    {
        IntentIntegrator integrator =  IntentIntegrator.forSupportFragment(this);
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.initiateScan();
    }
}
