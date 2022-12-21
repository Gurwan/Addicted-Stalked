package com.okariastudio.addictedstalked;

import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Intent usageAccess;
    private TextView time_usage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("Addicted Stalked",MODE_PRIVATE);

        if(!authorized()){
            usageAccess = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            usageAccess.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(usageAccess);

            if(authorized()){
                startForegroundService(new Intent(MainActivity.this,BackgroundService.class));
            } else {
                Toast.makeText(getApplicationContext(),"Addicted Stalker won't work without this authorization !",Toast.LENGTH_LONG).show();
            }
        } else {
            if(startForegroundService(new Intent(MainActivity.this,BackgroundService.class)) != null){
                System.out.println("already running");
            } else {
                System.out.println("starting service..");
            }
        }

        time_usage = findViewById(R.id.timeToChange);
        TimerTask updateView = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int allTime = sharedPreferences.getInt("ALL_COUNTER",-1);
                    String s = (allTime/(1000*60*60)) + " heures " + ((allTime/(1000*60))%60) + " minutes " + (allTime/1000)%60 + " secondes ";
                    time_usage.setText(s);
                });
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(updateView,0,1000);
    }

    public boolean authorized(){
        try {
            ApplicationInfo applicationinfo = getPackageManager().getApplicationInfo(getPackageName(),0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int m = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationinfo.uid,applicationinfo.packageName);
            return (m == 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if(authorized()){
            startForegroundService(new Intent(MainActivity.this,BackgroundService.class));
        }
        super.onDestroy();
    }
}
