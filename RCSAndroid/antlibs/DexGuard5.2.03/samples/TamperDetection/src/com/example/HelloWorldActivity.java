/*
 * Sample application to illustrate tamper detection with DexGuard.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!". It displays a different
 * message if someone has tampered with the application archive after it has
 * been created by DexGuard.
 *
 * You can experiment with it by first building, installing, and trying the
 * original version:
 *   ant release install
 * You can then tamper with it in some trivial way:
 *   zipalign -f 4 bin/HelloWorld-release.apk HelloWorld-tampered.apk
 *   adb install -r HelloWorld-tampered.apk
 * If you try the application again, you'll see that it detects that it has
 * been modified.
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Call the tamper detection in the DexGuard utility library of this
        // sample. The return code is 0 if the application archive is original
        // (created by DexGuard), or non-0 if it has been modified afterwards
        // (by jar, zip, jarsigner, zipalign, or any other tool).
        int check = dexguard.util.TamperDetection.checkApk(this);

        // Display a message.
        TextView view = new TextView(this);
        view.setText(check == 0 ?
                         "Hello world!" :
                         "Tamper alert!");
        view.setGravity(Gravity.CENTER);

        // Change the background color if the application archive has been
        // tampered with.
        if (check != 0)
        {
            view.setBackgroundColor(Color.RED);
        }

        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, 
                       check == 0 ?
                           "The application archive is original" :
                           "The application archive has been modified",
                       Toast.LENGTH_LONG).show();
    }
}
