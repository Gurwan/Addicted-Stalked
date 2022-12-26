package com.okariastudio.addictedstalked;

import android.app.KeyguardManager;
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
import android.os.PowerManager;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String CHANNEL_ID = "okariastudio.addictedstalker.alert";
    private final int MINUTES_TEN = 600000;
    private final int MINUTES_NINE = 540000;
    private final int MINUTES_EIGHT = 480000;
    private final int MINUTES_FIVE = 300000;
    private final int MINUTES_FOUR = 240000;
    private final int MINUTES_ONE = 60000;
    private final int SECONDS_TWENTY = 20000;
    private final int SECONDS_FIFTY = 50000;

    @Override
    public void onCreate() {
        super.onCreate();

        String NOTIFICATION_CHANNEL_ID = "okariastudio.addictedstalker.persistant";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Background Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID, "Alerte Addicted", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager2 = getSystemService(NotificationManager.class);
        manager2.createNotificationChannel(channel2);

        channel.setLockscreenVisibility(-1);
        channel.setLightColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Addicted Stalker is protecting you")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("Addicted Stalked",MODE_PRIVATE);
        editor = sharedPreferences.edit();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Alerte : Addicted Stalked")
                .setContentText("Oula ça fait déjà 10 minutes qu'est que tu fais de beau ?")
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        editor.putInt("TEN_SENT",0);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                KeyguardManager manager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
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
                    if(!(manager.inKeyguardRestrictedInputMode())){
                        int totalBeforeLock = sharedPreferences.getInt("TIME_LAST_PAUSE",-1);
                        if(totalBeforeLock != -1){
                           total = total - totalBeforeLock;
                        }
                    } else {
                        editor.putInt("TIME_LAST_PAUSE",total);
                    }
                    if(total >= MINUTES_ONE && (sharedPreferences.getInt("TEN_SENT",-1) == 0)){
                        editor.putInt("TEN_SENT",1);
                        notificationManager.notify(sharedPreferences.getInt("I_NOTIFICATION_ID",2), builder.build());
                        editor.putInt("I_NOTIFICATION_ID",sharedPreferences.getInt("I_NOTIFICATION_ID",2)+1);
                    } else if (total >= SECONDS_TWENTY && total <= SECONDS_FIFTY){
                        editor.putInt("TEN_SENT",0);
                    }
                    editor.putInt("ALL_COUNTER",total);
                    editor.apply();
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
