package com.example.mp3player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class MpNitView {
	private Context target;
	private Context appContext;
	public MpNitView(Context packageContext, Context app){
		target = packageContext;
		appContext = app;
		createView();
	}
	private void createView(){
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(target);
    	
    	// Creates an explicit intent for an Activity in your app
    	Intent openActivityIntent = new Intent(target, MainActivity.class);
    	// The stack builder object will contain an artificial back stack for the
    	// started Activity.
    	// This ensures that navigating backward from the Activity leads out of
    	// your application to the Home screen.
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(target);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	// Find the parent in AndroidManifest.xml
    	// 	  AndroidManifest.xml
    	//    <activity
		//        android:name=".AActivity"
		//        .
    	//    	  .
		//    </activity>
		//    <activity
		//        android:name=".BActivity"
		//        android:label="@string/app_name"
		//        android:parentActivityName=".AActivity" >
    	//    </activity>
    	stackBuilder.addParentStack(MainActivity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(openActivityIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	// cancel test
    	
    	PendingIntent pausePendingInent = 
    			PendingIntent.getService(
    					appContext, 
    					0,
    					getIntent(MplayerService.MP_PAUSE),
    					PendingIntent.FLAG_UPDATE_CURRENT);
    	PendingIntent playPendingInent = 
    			PendingIntent.getService(
    					appContext, 
    					1,
    					getIntent(MplayerService.MP_PLAY),
    					PendingIntent.FLAG_UPDATE_CURRENT);
    	PendingIntent nextPendingInent = 
    			PendingIntent.getService(
    					appContext, 
    					2,
    					getIntent(MplayerService.MP_NEXT),
    					PendingIntent.FLAG_UPDATE_CURRENT);
    	PendingIntent prePendingInent = 
    			PendingIntent.getService(
    					appContext, 
    					3,
    					getIntent(MplayerService.MP_PRE),
    					PendingIntent.FLAG_UPDATE_CURRENT);
    	
		RemoteViews remoteView = new RemoteViews(target.getPackageName(),R.layout.notification);
		remoteView.setImageViewResource(R.id.image, R.drawable.ic_launcher);  
		remoteView.setTextViewText(R.id.text , "Hello world !");  
		remoteView.setOnClickPendingIntent(R.id.noti_pause, pausePendingInent);
		remoteView.setOnClickPendingIntent(R.id.noti_play, playPendingInent);
		remoteView.setOnClickPendingIntent(R.id.noti_next, nextPendingInent);
		remoteView.setOnClickPendingIntent(R.id.noti_pre, prePendingInent);
		
		
		mBuilder.setContentText("Music")
	    .setSmallIcon(R.drawable.ic_launcher)
	    .setWhen(System.currentTimeMillis())
	    .setContent(remoteView)
	    .setContentIntent(resultPendingIntent);
		final NotificationManager mNotifyManager =
    	        (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyManager.notify(0, mBuilder.build());
	}
	private Intent getIntent(int what){
		Intent tarInt = new Intent(target, MplayerService.class);
		tarInt.putExtra("MSG", what);
		return tarInt;
	}

}
