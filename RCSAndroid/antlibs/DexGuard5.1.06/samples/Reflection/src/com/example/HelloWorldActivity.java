/*
 * Sample application to illustrate adding reflection using DexGuard.
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

        // Perform an API call that DexGuard will replace by reflection and
        // then encrypt (see dexguard-project.txt).
        String message = "  Hello world!  ".trim();

        // Display the message.
        TextView view = new TextView(this);
        view.setText(message);
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has performed reflection and encryption on an API call inside the application", Toast.LENGTH_LONG).show();
    }
}
