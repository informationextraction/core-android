/*
 * Sample application to illustrate processing with DexGuard.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!".
 *
 * The activity will log some mesages, unless DexGuard has removed the logging
 * code. You can see the logging with:
 *   adb logcat '*:S' 'HelloWorld:V'
 */
public class HelloWorldActivity extends Activity
{
    private static final String TAG = "HelloWorld";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // DexGuard can remove all logging for us.
        Log.d(TAG, "This is unnecessary logging from the HelloWorld application.");

        // Display the message.
        TextView view = new TextView(this);
        view.setText("Hello world!");
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has removed the unnecessary logging from this sample", Toast.LENGTH_LONG).show();
    }
}
