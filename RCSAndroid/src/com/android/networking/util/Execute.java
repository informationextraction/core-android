package com.android.networking.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.android.networking.auto.Cfg;

import android.util.Log;

public class Execute {
    private static final String TAG = "Execute";

    public ArrayList<String> execute(String cmd){
        String line = null;
        ArrayList<String> fullResponse = new ArrayList<String>();
        Process localProcess = null;

        try {
        	localProcess = Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            if (Cfg.EXP) {
            	Check.log(e);
            }
        }

        if (localProcess == null) {
        	return null;
        }
        
        try {
            //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(localProcess.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            
            while ((line = in.readLine()) != null) {
                fullResponse.add(line);
            }
            
            in.close();
            localProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fullResponse;
    }
}