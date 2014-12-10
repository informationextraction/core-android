/*
 * Sample application to illustrate checking the origin of the application with
 * DexGuard.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!". It displays a different
 * message if the application does not originate from the Google Play market.
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get the package name of the installer of this package (only
        // possible in Android 2.0 and higher). We'll check for the prefix.
        // DexGuard will encrypt the string and the Android API calls for us
        // (see dexguard-project.txt).
        String installerPackageName =
            getPackageManager().getInstallerPackageName(getPackageName());
        boolean googlePlayMarket =
            installerPackageName != null  &&
            // Google Play market:       "com.android.vending"
            // Google Play market (old): "com.google.android.feedback"
            // Amazon store:             "com.amazon.venezia"
            installerPackageName.startsWith("com.android");

        // Display a message.
        TextView view = new TextView(this);
        view.setText(googlePlayMarket ?
                         "Hello world!" :
                         "Not from the Google Play market!");
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, 
                       googlePlayMarket ?
                           "The application originates from the Google Play market" :
                           "The application does not originate from the Google Play market",
                       Toast.LENGTH_LONG).show();
    }
}
