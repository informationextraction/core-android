/*
 * Sample application to illustrate class encryption with DexGuard.
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
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the message. We're accessing a class that DexGuard will
        // encrypt (see dexguard-project.txt).
        TextView view = new TextView(this);
        view.setText(new SecretClass().getMessage());
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has encrypted a secret class inside the application", Toast.LENGTH_LONG).show();
    }
}
