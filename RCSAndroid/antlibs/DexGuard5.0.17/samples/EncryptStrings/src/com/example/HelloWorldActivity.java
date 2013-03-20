/*
 * Sample application to illustrate string encryption using DexGuard.
 *
 * Copyright (c) 2012 Saikoa / Itsana BVBA
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
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the message.
        TextView view = new TextView(this);
        view.setText("Hello world!");
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has encrypted the message string inside the application", Toast.LENGTH_LONG).show();
    }
}
