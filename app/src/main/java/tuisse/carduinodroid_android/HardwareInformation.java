package tuisse.carduinodroid_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoDroidData;

/**
 * Created by Bird on 28.02.2016.
 */
public class HardwareInformation{

    String TAG = "HardwareInformation";
    IpService ipService;

    NetworkInfo WifiInformation;
    NetworkInfo MobileInformation;
    ConnectivityManager connectivityManager;

    LocationListener locationListener;
    LocationManager locationManager;
    SensorManager sensorManager;
    SensorEventListener sensorEventListener;

    static double latitude;
    static double longitude;
    static double altitude;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private float delta;

    public HardwareInformation(IpService s) {

        ipService = s;
        connectivityManager = (ConnectivityManager) ipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        MobileInformation = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        locationManager = (LocationManager) ipService.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSLocationListener();
        sensorManager = (SensorManager) ipService.getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new MotionEventListener();

        if (ActivityCompat.checkSelfPermission(ipService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ipService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Error on Starting Mobility Service for GPS and Vibration");
        }else{
            if(locationManager != null)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener, Looper.getMainLooper());
            if(sensorManager != null)
                sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),1000000);
        }
    }

    private final class GPSLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();

            getDData().setGpsData(String.valueOf(longitude)+";"+String.valueOf(latitude)+";"+String.valueOf(altitude));

            Intent onLocationChanged = new Intent(Constants.EVENT.MOBILITY_GPS_CHANGED);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onLocationChanged);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private final class MotionEventListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            getDData().setVibration(mAccel);
            getDData().setBatteryPhone(getBatteryLevelPhone());

            Intent onFeatureChanged = new Intent(Constants.EVENT.FEATURES_CHANGED);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onFeatureChanged);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public float getBatteryLevelPhone(){

        Intent batteryIntent = ipService.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    public int getMobileAvailable(){

        connectivityManager = (ConnectivityManager) ipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        MobileInformation = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(MobileInformation.isAvailable()) return 1;
        else return 0;
    }

    public int getMobileActive(){

        connectivityManager = (ConnectivityManager) ipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        MobileInformation = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(MobileInformation.isConnected()) return 1;
        else return 0;
    }

    public int getWLANAvailable(){

        connectivityManager = (ConnectivityManager) ipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiInformation = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(WifiInformation.isAvailable()) return 1;
        else return 0;
    }

    public int getWLANActive(){

        connectivityManager = (ConnectivityManager) ipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiInformation = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(WifiInformation.isConnected()) return 1;
        else return 0;
    }

    public String getLocalIpAdress(){

        WifiManager wifiMgr = (WifiManager) ipService.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

        String ip = Formatter.formatIpAddress(wifiInfo.getIpAddress());

        return ip;
    }

    protected synchronized CarduinoDroidData getDData(){
        return ipService.getCarduino().dataHandler.getDData();
    }

    public void close(){

    }
}
