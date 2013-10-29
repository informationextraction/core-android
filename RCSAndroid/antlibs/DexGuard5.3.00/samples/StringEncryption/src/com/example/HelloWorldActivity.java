/*
 * Sample application to illustrate string encryption using DexGuard.
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
    private static final String MESSAGE = "Hello world!";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the message.
        // DexGuard is encrypting it for us (see dexguard-project.txt).
        TextView view = new TextView(this);
        view.setText(MESSAGE);
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has encrypted the message string inside the application", Toast.LENGTH_LONG).show();
    }
}
