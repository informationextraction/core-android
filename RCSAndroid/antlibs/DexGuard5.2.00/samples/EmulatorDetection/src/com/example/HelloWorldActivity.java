/*
 * Sample application to illustrate checking whether an application runs in an
 * emulator.
 *
 * Copyright (c) 2012-2013 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!". It displays a different
 * message if the application is running in an emulator.
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get an Android system property that distinguishes an emulator from
        // an actual device. You can pick one, or perhaps combine a few.
        // DexGuard will encrypt the strings for us (see dexguard-project.txt).
        boolean emulator;
        try
        {
            emulator = getSystemProperty("ro.kernel.qemu").length() > 0;
            //emulator = getSystemProperty("ro.kernel.qemu.gles").length() > 0;
            //emulator = getSystemProperty("ro.kernel.android.qemud").length() > 0;
            //emulator = getSystemProperty("ro.qemu.init.completed").length() > 0;
            //emulator = getSystemProperty("ro.hardware").equals("goldfish");
            //emulator = getSystemProperty("ro.build.type").equals("eng");
            //emulator = getSystemProperty("ro.build.description").contains("eng");
            //emulator = getSystemProperty("ro.product.cpu.abi").length() == 0;
            //emulator = getSystemProperty("ro.product.model").equals("sdk");
            //emulator = getSystemProperty("ro.product.name").equals("sdk");
        }
        catch (Exception e)
        {
            emulator = false;
        }

        // Display a message.
        TextView view = new TextView(this);
        view.setText(!emulator ?
                         "Hello world!" :
                         "Hello emulated world!");
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, 
                       !emulator ?
                           "The application is running on an actual device" :
                           "The application is running in an emulator",
                       Toast.LENGTH_LONG).show();
    }


    /**
     * Returns the Android system property with the given name.
     */
    private String getSystemProperty(String propertyName) throws Exception
    {
        // We're using reflection, because this Android API is private.
        // DexGuard will encrypt the strings for us (see dexguard-project.txt).
        Class clazz = Class.forName("android.os.SystemProperties");

        return (String)clazz
            .getMethod("get", new Class[]  { String.class })
            .invoke(clazz,    new Object[] { propertyName });
    }
}
