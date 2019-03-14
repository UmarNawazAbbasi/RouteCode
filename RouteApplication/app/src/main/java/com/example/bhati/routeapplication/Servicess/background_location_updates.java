package com.example.bhati.routeapplication.Servicess;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.bhati.routeapplication.Activities.Home;
import com.example.bhati.routeapplication.Model.GPSTracker;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class background_location_updates extends Service {
    private final IBinder mBinder = new MyBinder();

    public final static String MY_ACTION = "MY_ACTION";
    private static final String TAG = "Service_For_Location";
    private Timer timer = new Timer();
    LocationEngine locationEngine;
    MapboxMap map;
    GPSTracker gpsTracker;
    private LocationManager mLocationManager = null;
    private double lat , lng;
    public static ArrayList<LatLng> list;
    Intent intent;

    Bundle bundle ;
    private SharedPreferences preferences;
    private boolean isVideoStarted;

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    boolean value;
    private class LocationListener implements android.location.LocationListener{
        private static final String TAG = "Service_For_Location";

        Location mLastLocation;
        public LocationListener(String provider)
        {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {

            if (isVideoStarted) {
                Toast.makeText(background_location_updates.this, "l_C in service", Toast.LENGTH_SHORT).show();
                lat = location.getLatitude();
                lng = location.getLongitude();
                list.add(new LatLng(lat, lng));

                intent.setAction(MY_ACTION);
                bundle.putParcelableArrayList("LatLng", list);
                intent.putExtra("LatLngBundle", bundle);
                sendBroadcast(intent);
                //Log.e(TAG, "onLocationChanged: " + location.getLatitude() + "," + location.getLongitude());
                mLastLocation.set(location);
                //Log.d(TAG, "onLocationChanged:array in s "+list.size());
            }
            else
            {
                if (list.size() > 0) {
                    //Log.d(TAG, "onLocationChanged:after in s false "+list.size());
                    list.clear();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }


        @Override
        public void onProviderEnabled(String provider) {
           // Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Log.e(TAG, "onProviderDisabled: " + provider);

        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
           // new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        //Log.e(TAG, "OnBind: " + lat+","+lng);
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("isVideoCapturing" , MODE_PRIVATE);

        list = new ArrayList<>();
        list.clear();
        intent = new Intent();
        map = Home.map;
        bundle = new Bundle();
        locationEngine = Home.locationEngine;
        if (Build.VERSION.SDK_INT >= 26) {

                String CHANNEL_ID = "my_channel_01";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("").
                        setPriority(Notification.PRIORITY_MIN)
                        .setContentText("").build();

                startForeground(1, notification);

//            nManager.cancelAll();

        }
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @SuppressLint("MissingPermission")
//            @Override  //  Looper.getMainLooper();
                //Looper.prepare();
//                Looper.prepareMainLooper();
//            public void run() {
//              //  Looper.getMainLooper();
//                //Looper.prepare();
////                Looper.prepareMainLooper();
//
//            }
//        }, 0, 1000);//1 Minutes
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Log.e(TAG, "onStartCommand");
        isVideoStarted = preferences.getBoolean("is_video_capturing" , false);
        super.onStartCommand(intent, flags, startId);
       // value = intent.getBooleanExtra("is_video_capturing" , false);
       // Log.d(TAG, "onStartCommand: intent  "+isVideoStarted);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        return START_NOT_STICKY;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public class MyBinder extends Binder {
         background_location_updates getService() {
            return background_location_updates.this;
        }
    }
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
      try {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
      }
      catch (Exception ex)
      {

      }
        return isInBackground;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }
}
