package com.dataconnectiontoggler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.s_komlach.batler.Constants;
import com.s_komlach.batler.R;

import org.json.JSONObject;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@SuppressLint("NewApi")
public class Utils implements Constants{

/* Communication interfaces:
 * - WiFi (+)
 * - Bluetooth (+)
 * - MobileData (2G/3G/LTE)
 * - GPS ()
 * - USB
 * - NFC
 * - IR
 * 
 * Screen Rotation (+)
 * Flashlight
 * Brightness (+)
 * Sound Profile
 * Synchronization (+)
 * Airplane Mode
 * Charhing
 * */

    public static void pushToWear(Context ctx, int id,  String title, String text){
        // Nuke all previous notifications and generate unique ids
        NotificationManagerCompat.from(ctx).cancel(id);

        /*
        ArrayList<NotificationCompat.Action> actions = new ArrayList<NotificationCompat.Action>();
        Intent loginIntent = new Intent(ctx.getApplicationContext(), LogindoubleService.class);
        loginIntent.setAction("com.lwi.LOGIN");
        loginIntent.putExtra(LogindoubleService.VALUE_KEY, login.getUsername());

        Intent passwordIntent = new Intent(ctx.getApplicationContext(), LogindoubleService.class);
        loginIntent.setAction("com.lwi.PASSWORD");
        passwordIntent.putExtra(LogindoubleService.VALUE_KEY, login.getPassword());



        NotificationCompat.Action copyLogin = new NotificationCompat.Action.Builder(
                R.drawable.ic_content_copy_white_48dp, ctx.getString(R.string.copy_login),PendingIntent.getService(ctx, id, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT) )
                .extend(new NotificationCompat.Action.WearableExtender()
                        .setAvailableOffline(true))
                .build();



        NotificationCompat.Action copyPass = new NotificationCompat.Action.Builder(
                R.drawable.ic_content_copy_white_48dp, ctx.getString(R.string.copy_password),PendingIntent.getService(ctx, id, passwordIntent, PendingIntent.FLAG_UPDATE_CURRENT) )
                .extend(new NotificationCompat.Action.WearableExtender()
                        .setAvailableOffline(true))
                .build();


        actions.add(copyLogin);
        actions.add(copyPass);
*/
        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
                // .setHintHideIcon(true)
                //.setBackground(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.manatee));


        // Create a NotificationCompat.Builder to build a standard notification
        // then extend it with the WearableExtender

        NotificationCompat.Builder notif = new NotificationCompat.Builder(ctx)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_battery_60_grey600_24dp);


        /*
        for(NotificationCompat.Action action : actions){
            notif.addAction(action);
            wearableExtender.addAction(action);
        }
    */

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(ctx);
        // Issue the notification with notification manager.
        notificationManager.notify(id, notif.extend(wearableExtender).build());


    }



    public static class LocationTools {

        Location lastKnownLocation = null;
        LocationManager locationManager = null;
        private final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                lastKnownLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                lastKnownLocation = null;


            }

            @Override
            public void onProviderEnabled(String provider) {
                lastKnownLocation = null;
            }

            @Override
            public void onProviderDisabled(String provider) {
                lastKnownLocation = null;
            }
        };

        private static final int TIMEOUT_MINUTES = 1000 * 60 * 5;

        /**
         * Determines whether one Location reading is better than the current Location fix
         *
         * @param location            The new Location that you want to evaluate
         * @param currentBestLocation The current Location fix, to which you want to compare the new one
         */
        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TIMEOUT_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TIMEOUT_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /**
         * Checks whether two providers are the same
         */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }

        private Location getLocation(final Context context) {
            // Acquire a reference to the system Location Manager
            if (locationManager == null) {
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(mLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIMEOUT_MINUTES, 50, mLocationListener); // each 5 mins and 50 m
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIMEOUT_MINUTES, 50, mLocationListener); // each 5 mins and 50 m
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000 * 60, 1, mLocationListener);
            }


            if (lastKnownLocation == null) {
                Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (net != null && gps != null) {
                    if (isBetterLocation(net, gps))
                        lastKnownLocation = net;
                    else
                        lastKnownLocation = gps;

                } else if (gps != null)
                    lastKnownLocation = gps;
                else if (net != null)
                    lastKnownLocation = net;

            }
            return lastKnownLocation;


        }

        public void enforceActualLocation() {
            lastKnownLocation = null;
        }

        public void pluggedIn(final Context context, final Intent intent) {

            if (intent == null)
                return;



            Location loc = getLocation(context);


            if (loc != null)
                System.out.println("Speed : " + loc.hasSpeed() + " (" + loc.getProvider() + ")");
            else System.out.println("Cann't get Location");

            if (loc != null && !loc.hasSpeed()) {


                String locationAsJson = null;
                try {
                    JSONObject object = new JSONObject();
                    object.put("longitude", loc.getLongitude());
                    object.put("latitude", loc.getLatitude());
                    object.put("provider", loc.getProvider());
                    object.put("counter", 0);
                    locationAsJson = object.toString();
                    System.out.println(locationAsJson);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                final SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(context);
                String allLocations = pref.getString(ALL_KNOWNLOCATIONS, null);
                if (!TextUtils.isEmpty(allLocations)) {

                    String[] jsons = TextUtils.split(allLocations, "\n\n");
                    boolean found = false;
                    for (int i = 0; i < jsons.length; i++) {
                        String json = jsons[i];
                        if (TextUtils.isEmpty(json))
                            continue;

                        try {
                            JSONObject obj = new JSONObject(json);
                            double lat = obj.getDouble("latitude");
                            double lon = obj.getDouble("longitude");
                            String provider = obj.getString("provider");
                            Location tmpLoc = new Location(provider);
                            tmpLoc.setLatitude(lat);
                            tmpLoc.setLongitude(lon);


                            if (tmpLoc.distanceTo(loc) < 50) {
                                found = true;
                                long counter = obj.getLong("counter");
                                counter++;
                                //Really???
                                if(counter >= Long.MAX_VALUE)
                                    counter = Long.MAX_VALUE;

                                Toast.makeText(context, "Charge location found, count value " + counter, Toast.LENGTH_LONG).show();

                                JSONObject object = new JSONObject();
                                object.put("longitude", loc.getLongitude());
                                object.put("latitude", loc.getLatitude());
                                object.put("provider", loc.getProvider());
                                object.put("counter", counter);
                                jsons[i] = object.toString();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

                    if (found) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < jsons.length; i++) {
                            sb.append(jsons[i]);
                            if (i < jsons.length - 1)
                                sb.append("\n\n");
                        }
                        allLocations = sb.toString();

                    } else {
                        allLocations = allLocations + "\n\n" + locationAsJson;
                        Toast.makeText(context, "Charge location added", Toast.LENGTH_LONG).show();
                    }


                } else

                {
                    allLocations = locationAsJson;
                    Toast.makeText(context, "Charge location created", Toast.LENGTH_LONG).show();

                }
                pref.edit().putString(ALL_KNOWNLOCATIONS, allLocations).commit();


            }


        }


        int lastRawLevel, lastPluged;

        public void batteryStatechanged(final Context context) {


            /*
            int plugged = BatteryManager.BATTERY_PLUGGED_AC;
            double level = 1;
            Date now = Calendar.getInstance().getTime();

            for (int k = 0; k < 50; k++) {
                level = level - 0.01;
                batteryLevel.put(new Date(now.getTime() + k * 1000 * 60 *5), level);

            }
*/


            Intent intent = context.getApplicationContext()
                    .registerReceiver(null,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if(plugged == lastPluged && lastRawLevel == rawlevel)
                return;

            lastPluged = plugged;
            lastRawLevel = rawlevel;

            double scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            double level = -1;
            if (rawlevel >= 0 && scale > 0)
                level = rawlevel / scale;


            //save location were we can chgarge device
            if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {

                   setBatteryLevel(context, Calendar.getInstance().getTime(), level);

               return;
            }

            setBatteryLevel(context, Calendar.getInstance().getTime(), level);

            Location loc = getLocation(context);


            if (loc != null) {

                final SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(context);
                String allLocations = pref.getString(ALL_KNOWNLOCATIONS, null);
                if (!TextUtils.isEmpty(allLocations)) {

                    String[] jsons = TextUtils.split(allLocations, "\n\n");

                    ArrayList<Double> distance = new ArrayList<Double>();
                    HashMap<Double, Location> locationHashMap = new HashMap<Double, Location>();
                    for (int i = 0; i < jsons.length; i++) {
                        String json = jsons[i];
                        if(TextUtils.isEmpty(json))
                            continue;
                        
                        try {
                            JSONObject obj = new JSONObject(json);
                            double lat = obj.getDouble("latitude");
                            double lon = obj.getDouble("longitude");
                            String provider = obj.getString("provider");
                            if (obj.getLong("counter") > 0) {// at least more than one time charged
                                Location tmpLoc = new Location(provider);
                                tmpLoc.setLatitude(lat);
                                tmpLoc.setLongitude(lon);
                                tmpLoc.setProvider(provider);
                                distance.add((double) loc.distanceTo(tmpLoc));
                                locationHashMap.put((double)loc.distanceTo(tmpLoc), tmpLoc);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }



                    Collections.sort(distance);
                    //Collections.reverse(distance);
                    Location targetLocation = locationHashMap.get(distance.get(0));
                    int timeToPathMinutes = 0;
                    if (loc.hasSpeed()) {
                        int timeToPathSec = (int) (distance.get(0) / loc.getSpeed());
                        timeToPathMinutes = timeToPathSec / 60;


                    } else {
                        //no info, use WALK speed
                        int timeToPathSec = (int) (distance.get(0) / 2.0f);
                        timeToPathMinutes = timeToPathSec / 60;
                    }

                    System.out.println("timeToPathMinutes "+timeToPathMinutes);

                    //use batteryLevel() calculate remaining time
                    ArrayList<Date> batteryLog = new ArrayList<Date>();
                    batteryLog.addAll(getBatteryLevelKeyset(context));
                    Collections.sort(batteryLog);

                    double last = -1;
                    Date start = batteryLog.get(0);
                    Date end = null;
                    for (int i = 0; i < batteryLog.size(); i++) {
                        Date d = batteryLog.get(i);
                        double vl = getBatteryLevel(context, d);
                        if (last <= vl)
                            last = vl;
                        if (last > vl) {
                            end = d;
                            break;
                        }


                    }
                    if (end == null)
                        end = batteryLog.get(batteryLog.size()-1);

                    int dischargeTime = (int) (start.getTime() - end.getTime()) / 1000; //sec

                    System.out.println("dischargeTime "+dischargeTime);

                    double dischargeDelta = getBatteryLevel(context, end) - getBatteryLevel(context, start);

                    double dischargeSpeed = ((double) dischargeTime / dischargeDelta);

                    System.out.println("dischargeSpeed "+dischargeSpeed);

                    int leftTime = (int) (level * dischargeSpeed) / 60;
                    System.out.println("leftTime "+leftTime);

                    if ( leftTime < timeToPathMinutes && pref.getBoolean(STATE_SMART, STATE_SMART_DEFVALUE)) {
                        pushToWear(context, 98765432, "Extra save mode",  "Remaines: "+leftTime+" min, nearest charge: "+ ((int)distance.get(0).doubleValue())+" m");
                        disableAll(context, true);

                    } else {
                        // all fine
                        System.out.println("all fine");
                    }


                }


            }


        }


    }


    static LocationTools locationTools = new LocationTools();
    public static LocationTools getLocationTools(){ return locationTools; }

    public static void setBatteryLevel(Context context, Date date, double level){
      
        final SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
       
        String battery = pref.getString("battery", null);

        String batteryAsJson = null;
        try {
            JSONObject object = new JSONObject();
            object.put("date", date.getTime());
            object.put("level", level);
            object.put("currentLevel", pref.getString("currentLevel", "0"));
            batteryAsJson = object.toString();
            System.out.println(batteryAsJson);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(battery)) 
            battery = battery + "\n\n" + batteryAsJson;                
            else
            battery = batteryAsJson;
           

        
        pref.edit().putString("battery", battery).commit();

    }
    public static double getBatteryLevel(Context context, Date date){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        String battery = pref.getString("battery", null);
        if (!TextUtils.isEmpty(battery)) {

            String[] jsons = TextUtils.split(battery, "\n\n");
            for (int i = 0; i < jsons.length; i++) {
                String json = jsons[i];
                if(TextUtils.isEmpty(json))
                    continue;

                try {
                    JSONObject obj = new JSONObject(json);
                    long d = obj.getLong("date");
                    double l = (double) obj.getDouble("level");
                    if(date.getTime() == d)
                        return l;
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            
        }
        
        return 0f;
        
    }

    public static int getBatterySaveLevel(Context context, Date date){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        String battery = pref.getString("battery", null);
        if (!TextUtils.isEmpty(battery)) {

            String[] jsons = TextUtils.split(battery, "\n\n");
            for (int i = 0; i < jsons.length; i++) {
                String json = jsons[i];
                if(TextUtils.isEmpty(json))
                    continue;

                try {
                    JSONObject obj = new JSONObject(json);
                    long d = obj.getLong("date");
                    int l = Integer.parseInt(obj.getString("currentLevel"));
                    if(date.getTime() == d)
                        return l;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }

        return 0;

    }

    public static ArrayList<Date> getBatteryLevelKeyset(Context context){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        String battery = pref.getString("battery", null);
        ArrayList<Date> list = new ArrayList<Date>();
        if (!TextUtils.isEmpty(battery)) {

            String[] jsons = TextUtils.split(battery, "\n\n");
            for (int i = 0; i < jsons.length; i++) {
                String json = jsons[i];
                if(TextUtils.isEmpty(json))
                    continue;

                try {
                    JSONObject obj = new JSONObject(json);
                    long d = obj.getLong("date");
                    list.add(new Date(d));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }

        return list;

    }


    public static double batteryLevel(Context context) {
		Intent batteryIntent = context.getApplicationContext()
				.registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		double scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0)
			level = rawlevel / scale;

		return level;

	}

public static boolean isConnectedCharging(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		return plugged == BatteryManager.BATTERY_PLUGGED_AC
				|| plugged == BatteryManager.BATTERY_PLUGGED_USB
                || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;


	}




    public static void checkInactiveConnections(Context context, boolean isconnected) {}

    public static void disableAll(Context context) {
        disableAll(context, false);
    }

    public static void disableAll(Context context, boolean extemeMode) {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
        if(pref.getBoolean(STATE_CHARGING, STATE_CHARG_DEFVALUE) && isConnectedCharging(context) )
            return;


        Editor edit = pref.edit();

		if(pref.getBoolean(STATE_BRIGHTNESS, STATE_BRIGHTNESS_DEFVALUE) || extemeMode)
        {
            if (getBrightnessAuto(context) && pref.getInt("brigthness_auto_value", -1) == -1)
                try {
                    edit.putBoolean("brightness_mode_value", getBrightnessAuto(context));
                    edit.putInt("brigthness_auto_value", getBrightness(context));
                    edit.putInt("brigthness_value", -1);
                    System.out.println("auto brightness disabled");
                    setBrightnessAuto(context, false);
                    setBrightness(context, 10);

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            else
            if (!getBrightnessAuto(context) && pref.getInt("brigthness_value", -1) == -1)
                try {
                    edit.putInt("brigthness_auto_value", -1);
                    edit.putInt("brigthness_value", getBrightness(context));
                    System.out.println("brightness disabled");
                    setBrightness(context, 20);

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
        }
		if(pref.getBoolean(STATE_ORIENTATION, STATE_ORIENT_DEFVALUE)|| extemeMode)
		if (isAutoOrientationEnabled(context))
			try {
				setAutoOrientationEnabled(context, false);
				edit.putBoolean("orientation", true);
				System.out.println("orientation disabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		if(pref.getBoolean(STATE_SYNC, STATE_SYNC_DEFVALUE)|| extemeMode)
		if (ContentResolver.getMasterSyncAutomatically())
			try {
				ContentResolver.setMasterSyncAutomatically(false);
				edit.putBoolean("sync", true);
				System.out.println("Sync disabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		if(pref.getBoolean(STATE_WIFI, STATE_WIFI_DEFVALUE)|| extemeMode)
            if (isWiFi(context))
			try {
				setWifiConnection(context, false);
				edit.putBoolean("wifi", true);
				System.out.println("wifi disabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		if(pref.getBoolean(STATE_BLUETOOTH, STATE_BT_DEFVALUE)|| extemeMode)
		if (isBluetooth(context))
			try {
				setBluetooth(false);
				edit.putBoolean("bt", true);
				System.out.println("bt disabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
if(! (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)) {

    if (pref.getBoolean(STATE_GPS, STATE_GPS_DEFVALUE)|| extemeMode)
        if (isGpsEnabled(context))
            try {
                setGps(context, false);
                edit.putBoolean("gps", true);
                System.out.println("gps disabled");

            } catch (Throwable ex) {
                ex.printStackTrace();
            }

    if (pref.getBoolean(STATE_AIRPLANEMODE, STATE_APM_DEFVALUE)|| extemeMode)
        if (!isAirplaneMode(context))
            try {
                setAirplaneMode(context, true);
                edit.putBoolean("airplane", true);
                System.out.println("airplane mode enabled");
            } catch (Throwable ex) {
                ex.printStackTrace();
            }


}
else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT )
{
    if (pref.getBoolean(STATE_MOBILEDATA, STATE_MOBDATA_DEFVALUE)|| extemeMode)
        if (isMobile(context))
            try {
                setDataConnection(context, false);
                edit.putBoolean("mobile", true);
                System.out.println("mobile data disabled");
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
}
        if(!extemeMode)
		edit.commit();

	}


	public static void enableAll(Context context) {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

        if (pref.getInt("brigthness_value", -1) != -1)
            try {
                System.out.println("brightness enabled");
                setBrightness(context, pref.getInt("brigthness_value", -1));
                edit.putInt("brigthness_value", -1);
                edit.putInt("brigthness_auto_value", -1);

			} catch (Throwable ex) {
				ex.printStackTrace();
			}
        if (pref.getInt("brigthness_auto_value", -1) != -1)
            try {
                System.out.println("auto brightness enabled");
                setBrightness(context, pref.getInt("brigthness_auto_value", -1));
                setBrightnessAuto(context, pref.getBoolean("brightness_mode_value", getBrightnessAuto(context)));
                edit.putInt("brigthness_value", -1);
                edit.putInt("brigthness_auto_value", -1);

            } catch (Throwable ex) {
                ex.printStackTrace();
            }
		if (pref.getBoolean("orientation", false))
			try {
				setAutoOrientationEnabled(context, true);
				edit.putBoolean("orientation", false);
				System.out.println("orientation enabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		if (pref.getBoolean("sync", false))
			try {
				ContentResolver.setMasterSyncAutomatically(true);
				System.out.println("Sync enabled");
				edit.putBoolean("sync", false);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

		if (pref.getBoolean("wifi", false))
			try {
				setWifiConnection(context, true);
				System.out.println("wifi enabled");
				edit.putBoolean("wifi", false);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

		if (pref.getBoolean("bt", false))
			try {
				setBluetooth(true);
				edit.putBoolean("bt", false);
				System.out.println("bt enabled");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

		
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {


            if (pref.getBoolean("gps", false))
                try {
                    setGps(context, true);
                    System.out.println("gps enabled");
                    edit.putBoolean("gps", false);

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }


            if (pref.getBoolean("airplane", false))
                try {
                    setAirplaneMode(context, false);
                    System.out.println("airplane disabled");
                    edit.putBoolean("airplane", false);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

        }
        else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT )
        {
            if (pref.getBoolean("mobile", false)) try {
                setDataConnection(context,
                        true);
                System.out.println("mobile data enabled");
                edit.putBoolean("mobile", false);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

        }
		 
		 

		edit.commit();

	}



    public static boolean isMobile(Context ctx){
	try {
		if (Build.VERSION.SDK_INT >= 17)
			return Settings.Global.getInt(ctx.getContentResolver(),"mobile_data", 0) == 1;
			return Settings.System.getInt(ctx.getContentResolver(), "mobile_data", 0) == 1;
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	return false;
}


    public static boolean setDataConnection(Context context, boolean enabled)
            throws Throwable {


        final ConnectivityManager conman = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO)
                {

                        Method dataConnSwitchmethod;
                        Class telephonyManagerClass;
                        Object ITelephonyStub;
                        Class ITelephonyClass;
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                        getITelephonyMethod.setAccessible(true);
                        ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                        ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                        if (enabled) {
                            dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");

                        } else {
                            dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                        }
                        dataConnSwitchmethod.setAccessible(true);
                        dataConnSwitchmethod.invoke(ITelephonyStub);


                }
                else {
                    final Field iConnectivityManagerField = ConnectivityManager.class
                            .getDeclaredField("mService");
                    iConnectivityManagerField.setAccessible(true);
                    final Object iConnectivityManager = iConnectivityManagerField
                            .get(conman);
                    final Class iConnectivityManagerClass = Class
                            .forName(iConnectivityManager.getClass().getName());
                    final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                            .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
                }
                return true;
            }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            String command = null;
            int state = 0;

                // Get the current state of the mobile network.
                state = enabled ? 1 : 0;
                // Get the value of the "TRANSACTION_setDataEnabled" field.
                String transactionCode = getTransactionCode(context);
                // Android 5.1+ (API 22) and later.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    // Loop through the subscription list i.e. SIM list.
                    for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                        if (transactionCode != null && transactionCode.length() > 0) {
                            // Get the active subscription ID for a given SIM card.
                            int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                            // Execute the command via `su` to turn off
                            // mobile network for a subscription service.
                            command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                            executeCommandViaSu(context, "-c", command);
                        }
                    }
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    // Android 5.0 (API 21) only.
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Execute the command via `su` to turn off mobile network.
                        command = "service call phone " + transactionCode + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }

        }

        throw new Exception("Cannt setup MobileDataConnection");
    }


    private static String getTransactionCode(Context context) throws Exception {

            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));

    }
    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (Exception e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }

public static boolean isWiFi(Context ctx) {
    WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
    return wifi.isWifiEnabled();
	}

public static void setWifiConnection(Context context, boolean enabled){
	WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	wifi.setWifiEnabled(enabled);
}


public static boolean isBluetooth(Context ctx){
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    return  bluetoothAdapter.isEnabled();
}


public static boolean setBluetooth(boolean enable) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean isEnabled = bluetoothAdapter.isEnabled();
    if (enable && !isEnabled) {
        return bluetoothAdapter.enable(); 
    }
    else if(!enable && isEnabled) {
        return bluetoothAdapter.disable();
    }

    return true;
}





public static boolean isAutoOrientationEnabled(Context ctx){
	try {				
			return Settings.System.getInt(ctx.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
	} catch (Throwable e) {
		e.printStackTrace();
	}

	return false;
}


public static void setAutoOrientationEnabled(Context context, boolean enabled)
{
	Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
}



public static boolean getBrightnessAuto(Context ctx){
	try {				
			return Settings.System.getInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 1;
	} catch (Throwable e) {
		e.printStackTrace();
	}

	return false;
}


    public static void setBrightnessAuto(Context ctx, boolean enabled){
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    enabled ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

    }

public static int getBrightness(Context ctx){
	try {				
			return Settings.System.getInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
	} catch (Throwable e) {
		e.printStackTrace();
	}

	return -1;
}
public static void setBrightness(Context context, int value)
{
	Settings.System.putInt(context.getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS, value);
}



public static void setGps(Context context, boolean enabled) throws Exception{

	
	if(!canToggleGPS(context))
		{		
		Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", enabled);
		context.sendBroadcast(intent);
		return;
		}
	else {
	
		final Intent poke = new Intent();
		poke.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
		poke.setData(Uri.parse("3"));
		context.sendBroadcast(poke);
	}
}

public static boolean isGpsEnabled(Context context) {
    int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
    return (mode != Settings.Secure.LOCATION_MODE_OFF);

}


public static boolean canToggleGPS(Context context) {
    PackageManager pacman = context.getPackageManager();
    PackageInfo pacInfo = null;

    try {
        pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
    } catch (Exception e) {
        return false; //package not found
    }

    if(pacInfo != null){
        for(ActivityInfo actInfo : pacInfo.receivers){
            //test if recevier is exported. if so, we can toggle GPS.
            if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                return true;
            }
        }
    }

    return false; //default
}


public static boolean isAirplaneMode(Context context){
	try{
		if (Build.VERSION.SDK_INT >= 17)
			return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
		    return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,  0) == 1;
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return false;

}


public static void setAirplaneMode(Context context, boolean enabled){


	if (Build.VERSION.SDK_INT >= 17)
		 Settings.Global.putInt(context.getContentResolver(),  Settings.Global.AIRPLANE_MODE_ON, enabled ? 1: 0) ;
	else
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 1: 0);


    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    intent.putExtra("state", enabled);
    context.sendBroadcast(intent);

}



public static boolean isPowerMode(Context context){
    if (Build.VERSION.SDK_INT >= 20)
    return Settings.Global.getInt(context.getContentResolver(),  "low_power",  0)  == 1;
    else
        return  false;
}

    public static boolean isNfcEnabled(Context context)
    {

        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            return true;
        }
        return false;
    }



    public static boolean isLed(Context context){
        try{
            if (Build.VERSION.SDK_INT >= 17)
              return Settings.Global.getInt(context.getContentResolver(), "notification_light_pulse", 0) == 1;
            return Settings.System.getInt(context.getContentResolver(), "notification_light_pulse",  0) == 1;
        } catch (Throwable e) {
           e.printStackTrace();
        }
        return false;

    }


    public static void setLed(Context context, boolean enabled){
       if (Build.VERSION.SDK_INT >= 17)
           Settings.Global.putInt(context.getContentResolver(),  Settings.Global.AIRPLANE_MODE_ON, enabled ? 1: 0) ;
        else
            Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", enabled ? 1: 0);


    }

    public static boolean isKeyboardlight(Context context){
        try{
            if (Build.VERSION.SDK_INT >= 17)
                 return Settings.Global.getInt(context.getContentResolver(), "notification_light_pulse", 0) == 1;
            return Settings.System.getInt(context.getContentResolver(), "button_key_light",  0) != 0;
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }


    public static void setKeyboardLight(Context context, boolean enabled){
         if (Build.VERSION.SDK_INT >= 17)
            Settings.Global.putInt(context.getContentResolver(),  Settings.Global.AIRPLANE_MODE_ON, enabled ? 1: 0) ;
        else
        Settings.System.putInt(context.getContentResolver(), "button_key_light", enabled ? -1: 0);


    }












public static void setPowerMode(Context context, boolean enabled) throws Exception {

	PowerManager mPowerMan = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	Method dataMtd = PowerManager.class.getDeclaredMethod(
			"setPowerSaveMode", boolean.class);
	dataMtd.setAccessible(true);
	dataMtd.invoke(mPowerMan, enabled);

}

	public static void setPowerMode2(Context context, boolean enabled){
		if (Build.VERSION.SDK_INT >= 20)
			Settings.Global.putInt(context.getContentResolver(),  "low_power", enabled ? 1: 0) ;

	}


}
