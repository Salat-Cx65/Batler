package com.dataconnectiontoggler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.s_komlach.batler.MainActivity;
import com.s_komlach.batler.R;

public class DeamonService extends Service{
	public static boolean running = false;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		 running = true;
		
		int res = super.onStartCommand(intent, flags, startId);
		

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);	        
		notificationManager.cancel(987654321);
		Notification notification = new Notification();
	    notification.icon = R.drawable.ic_battery_60_grey600_24dp;
	    notification.tickerText = getString(R.string.app_name);
	    notification.when = System.currentTimeMillis();
	    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE|Notification.FLAG_AUTO_CANCEL;
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
	    notification.setLatestEventInfo(this, getResources().getString(R.string.app_name),getString(R.string.app_name), pIntent);
	    
	    
        System.out.println("startForeground");
        startForeground(987654321, notification);
        
        mReceiver = new ScreenLockReceiver();	 
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        registerReceiver(mReceiver, filter);
        
		return res;
	}
	BroadcastReceiver mReceiver;
	
    @Override
	public void onDestroy() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(987654321);

		if(mReceiver!=null)
			unregisterReceiver(mReceiver);

		running = false;

		super.onDestroy();

	}

	@Override
    public IBinder onBind(Intent intent) {
		// not supporting binding
    	return null;
    }
		
}
