/*
 * Sample application to illustrate asset encryption with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import java.io.*;

/**
 * Sample activity that displays "Hello world!".
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try {
            // Open the message asset. DexGuard will encrypt the file for us.
            InputStream stream = getAssets().open("message.txt");

            // Read the message from the stream.
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String message = reader.readLine();
            reader.close();

            // Display the message.
            TextView view = new TextView(this);
            view.setText(message);
            view.setGravity(Gravity.CENTER);
            setContentView(view);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has encrypted the asset file of this sample", Toast.LENGTH_LONG).show();
    }
}
