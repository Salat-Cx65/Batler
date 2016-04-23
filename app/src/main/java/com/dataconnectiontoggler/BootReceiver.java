package com.dataconnectiontoggler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.s_komlach.batler.Constants;

public class BootReceiver extends BroadcastReceiver implements Constants{
    @Override
    public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		if(pref.getBoolean(STATE_BATLER, STATE_BATLER_DEFVALUE) && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
    		if(!DeamonService.running)
    	   context.startService(new Intent(context, DeamonService.class));
    	   }
    	
    }
}
