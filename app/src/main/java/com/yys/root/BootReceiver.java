package com.yys.root;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Boot receiver to auto-start services if configured.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (ConfigManager.getInstance().isAutoStart()) {
                // Start float service
                Intent serviceIntent = new Intent(context, FloatService.class);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}
