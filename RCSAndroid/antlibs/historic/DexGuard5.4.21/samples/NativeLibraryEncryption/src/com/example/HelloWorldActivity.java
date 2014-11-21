/*
 * Sample application to illustrate processing with DexGuard.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!".
 */
public class HelloWorldActivity extends Activity
{
    static {
        System.loadLibrary("helloworld");
    }

    private native String getMessage();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the message.
        TextView view = new TextView(this);
        view.setText(getMessage());
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has encrypted the native library of this sample", Toast.LENGTH_LONG).show();
    }
}
