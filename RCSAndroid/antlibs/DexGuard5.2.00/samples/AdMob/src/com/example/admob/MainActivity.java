/*
 * Sample application to illustrate processing AdMob with DexGuard.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example.admob;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

import com.google.ads.AdView;
import com.google.ads.AdSize;
import com.google.ads.AdRequest;

/**
 * Sample activity that displays an AdView.
 */
public class MainActivity extends Activity
{
    // You can get your own publisher ID by signing up at the AdMob site.
    // This is the ID from com.google.example.ads.fundamentals.
    private static final String ADMOB_PUBLISHER_ID = "a14d91b10f12454";

    // You can get the ID of your own test device by running the application
    // and examining the log (adb logcat 'Ads:I' '*:S').
    private static final String TEST_DEVICE = "6207CAE4743690C8FB58DEF0411F928F";


    private AdView adView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Create and display just an AdView.
        adView = new AdView(this, AdSize.BANNER, ADMOB_PUBLISHER_ID);
        setContentView(adView);

        // Send a request to load an advertisement.
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adRequest.addTestDevice(TEST_DEVICE);
        adView.loadAd(adRequest);

        // Briefly display a comment.
        Toast.makeText(this, TEST_DEVICE.hashCode() == 0xc2a5c57f ?
            "You should still specify the ID of your test device in the code" :
            "DexGuard has processed the AdMob library in this sample",
            Toast.LENGTH_LONG).show();
    }


    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }

        super.onDestroy();
    }
}
