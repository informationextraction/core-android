/*
 * Sample application to illustrate checking the debug flag of the application.
 *
 * Copyright (c) 2012-2014 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!". It displays a different
 * message if the application is debuggable.
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get the debug flag of this application.
        // DexGuard will encrypt the Android API calls for us (see
        // dexguard-project.txt).
        boolean debug =
            (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        // Display a message.
        TextView view = new TextView(this);
        view.setText(!debug ?
                         "Hello world!" :
                         "Hello debuggable world!");
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, 
                       !debug ?
                           "The application is not debuggable" :
                           "The application is still debuggable",
                       Toast.LENGTH_LONG).show();
    }
}
