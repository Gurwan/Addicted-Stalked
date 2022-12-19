package com.okariastudio.addictedstalked;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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
                long start = end-1000;
                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,end);
                int total = 0;
                if(usageStatsList!=null && !(usageStatsList.isEmpty())){
                    for(UsageStats u : usageStatsList){
                        total += u.getTotalTimeInForeground();
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
