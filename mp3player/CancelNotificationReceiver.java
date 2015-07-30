package com.example.mp3player;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelNotificationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		final int notifyID = intent.getIntExtra("cancel_notify_id", 0);
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
		notificationManager.cancel(notifyID);
	}
}
	
