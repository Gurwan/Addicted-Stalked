package com.okariastudio.addictedstalked;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Starter extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Recu");
        context.startForegroundService(new Intent(context,BackgroundService.class));
    }
}
