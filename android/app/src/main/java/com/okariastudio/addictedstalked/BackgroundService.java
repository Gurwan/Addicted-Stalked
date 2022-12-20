package com.okariastudio.addictedstalked;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("Addicted Stalked",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sharedPreferences = getSharedPreferences("Addicted Stalked",MODE_PRIVATE);
                editor = sharedPreferences.edit();
                long end = System.currentTimeMillis();
                long start = end-500;
                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,end);
                int total = 0;
                if(usageStatsList!=null && !(usageStatsList.isEmpty())){
                    for(UsageStats u : usageStatsList){
                        total += u.getTotalTimeInForeground();
                    }
                    editor.putInt("ALL_COUNTER_REAL",total);
                    UserManager userManager = (UserManager)getSystemService(Context.USER_SERVICE);
                    if(userManager.isUserUnlocked()){
                        int allTime = sharedPreferences.getInt("ALL_COUNTER_REAL",-1);
                        if(total - 10000 > allTime){
                            editor.putInt("TIME_LAST_PAUSE",total);
                            System.out.println("total : " + total + " et allTime : "+ allTime);
                        }
                        int totalBeforeLock = sharedPreferences.getInt("TIME_LAST_PAUSE",-1);
                        if(totalBeforeLock != -1){
                           total = total - totalBeforeLock;
                        }
                    } else {
                        System.out.println("lock");
                    }
                    editor.putInt("ALL_COUNTER",total);
                    editor.apply();
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,500);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
