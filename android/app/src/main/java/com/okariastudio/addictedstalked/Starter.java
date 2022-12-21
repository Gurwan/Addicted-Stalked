package com.okariastudio.addictedstalked;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class Starter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            System.out.println("Recue");
        }
        PowerManager manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "addictedstalker:wakelock");
        wakeLock.acquire(5*1000L /*10 minutes*/);
        context.startForegroundService(new Intent(context, BackgroundService.class));
    }
}
