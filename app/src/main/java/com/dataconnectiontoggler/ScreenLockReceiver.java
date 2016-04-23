package com.dataconnectiontoggler;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.s_komlach.batler.Constants;

@SuppressLint("NewApi")
public class ScreenLockReceiver extends BroadcastReceiver implements Constants {
	
	
	static final String wakeup = "wakeup";
	static final String sleep = "sleep";
    static final String checkNetwork = "checkNetwork";
	static String lastAction;

	@Override
	public void onReceive(final Context context, final Intent intent) {
        final SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);

        if(!DeamonService.running || !pref.getBoolean(STATE_BATLER, STATE_BATLER_DEFVALUE))
           return;


        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW) )
        {
            Utils.getLocationTools().enforceActualLocation();
            Utils.getLocationTools().batteryStatechanged(context);
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED) )
        {
            Utils.getLocationTools().pluggedIn(context, intent);
            return;
        }

        Utils.getLocationTools().batteryStatechanged(context);


		cancelLastAlarm(context);
       int wakeupTime = (int) (pref.getInt(STATE_WAKEUP, STATE_WAKEUP_DEFVALUE) * (1.0 +(1.0- Utils.batteryLevel(context))));
        int standyTime = pref.getInt(STATE_STANDBY, STATE_STANBY_DEFVALUE);

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Utils.disableAll(context);
            setAlarm(context, wakeupTime, wakeup);
		} else
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) || intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
			KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);			
			if(!km.inKeyguardRestrictedInputMode()) {
                Utils.enableAll(context);

            }
			
		}
		
		
		if (intent.getAction().equals(sleep)) {
			Utils.disableAll(context);
			setAlarm(context, wakeupTime, wakeup);
		} else if (intent.getAction().equals(wakeup)) {
			Utils.enableAll(context);
			setAlarm(context, standyTime, sleep);

		}

	}

	
	
	
private void setAlarm(Context ctx, int wakeupMinute, String action){

	lastAction = action;
	long delay = 60*1000*wakeupMinute;
	long when = System.currentTimeMillis() + delay;
	
	AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE); 
    Intent intent = new Intent(ctx, ScreenLockReceiver.class);
    intent.setAction(action);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );
    am.setAlarmClock(new AlarmManager.AlarmClockInfo(when, pendingIntent), pendingIntent);
    //this method doesnt work on Android M
    //am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
   
}

private void cancelLastAlarm(Context context){
	if(TextUtils.isEmpty(lastAction))
		return;
	
	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
    Intent intent = new Intent(context, ScreenLockReceiver.class);
    intent.setAction(lastAction);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    am.cancel(pendingIntent);
    
    lastAction = null;
}



/*

    private void setAlarmforNetwork(Context ctx, int wakeupMinute){
        cancelLastAlarmForNetwork(ctx);

        long delay = 60*1000*wakeupMinute;
        long when = System.currentTimeMillis() + delay;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, ScreenLockReceiver.class);
        intent.setAction(checkNetwork);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );

        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);

    }

    private void cancelLastAlarmForNetwork(Context context){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScreenLockReceiver.class);
        intent.setAction(checkNetwork);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );
        am.cancel(pendingIntent);

    }
*/

}