package com.example.mp3player;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MplayerService extends Service implements 
MediaPlayer.OnCompletionListener,
MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnPreparedListener,
Runnable{
	private MediaPlayer mp;
	private MusicList ML;
	private boolean init = false;
	private Thread seekThread;
	//communication
	private Messenger mMessenger;
	private Messenger cMessenger;
	private boolean service;
	public boolean IsActivityAlive;
	public static final int MP_PLAY = 0;
	public static final int MP_PAUSE = 1;
	public static final int MP_NEXT = 2;
	public static final int MP_PRE = 3;
	public static final int MP_CHANGE_SONG = 4;
	public static final int SK_CHANGE = 5;
	public static final int BUF_UPDATE = 6;
	public static final int ACT_OPEN = 7;
	public static final int ACT_CLOSE = 8;
	public static final int MP_EXIT = 9;
	
	//Preference
	private final String preference = "BianMP3";
	private static final String LAST_SONG = "LASTSONG";
	private static final String LAST_SONG_TIME = "LASTSONG_TIME";

	
	//Preference
	
	//seek bar handler, use static to avoid memory leak ? 
	private static final class seekBarHandler extends Handler{
		WeakReference<MplayerService> mService;
		public seekBarHandler(MplayerService mm){
			// TODO Auto-generated constructor stub
			mService = new WeakReference<MplayerService>(mm);
		}
		@Override
		public void handleMessage(Message msg){
			// TODO Auto-generated method stub
			
			MplayerService service = mService.get();
			if(service == null)return;
			if( !service.init )return;
			
			switch(msg.what){
			case ACT_OPEN:
				Log.i("Handler_bian", "SER : Get ACT_OPEN");
				service.cMessenger =  msg.replyTo;
				service.changeSong();
				//set text of start time
				Bundle data = new Bundle();
				data.putInt("Duration", service.mp.getCurrentPosition());
				service.sendMsg(SK_CHANGE, data, service.cMessenger);
				break;
			case ACT_CLOSE:
				Log.i("Handler_bian", "SER : Get ACT_CLOSE");
				service.cMessenger = null;
				break;
			case MP_PLAY:
				Log.i("Handler_bian", "SER : Get MP_PLAY");
				if(service.cMessenger != null && service.cMessenger != msg.replyTo)
					service.sendMsg(MP_PLAY, service.cMessenger);
				service.play();
				service.seekThread = new Thread(service);
				service.seekThread.start();
				break;
			case MP_PAUSE:
				Log.i("Handler_bian", "SER : Get MP_PAUSE");
				if(service.cMessenger != null && service.cMessenger != msg.replyTo)
					service.sendMsg(MP_PAUSE, service.cMessenger);
				service.pause();
				//User press pause button
				service.closeSeekThread();
				break;
			case MP_NEXT:
				Log.i("Handler_bian", "SER : Get MP_NEXT");
				service.next();
				break;
			case MP_PRE:
				Log.i("Handler_bian", "SER : Get MP_PRE");
				service.pre();
				break;
			case SK_CHANGE:
				Log.i("Handler_bian", "SER : Get SK_CHANGE");
				//change by user
				service.seekTo(msg.getData().getInt("Duration"));
				break;
			case MP_EXIT:
				Log.i("Handler_bian", "SER : Get MP_EXIT");
				//change by user
				break;
			default: 
				super.handleMessage(msg);
			}
		}
	};
	private final seekBarHandler mHandler = new seekBarHandler(this);
	//seek bar handler
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		
		mMessenger = new Messenger(mHandler);
		mp = new MediaPlayer();
		mp.setLooping(true);
		mp.setOnCompletionListener(this);
		mp.setOnPreparedListener(this);
		long start = System.currentTimeMillis();
		ML = new MusicList();
		Log.d("Time", "Ser new MusicList : "+
				(System.currentTimeMillis() - start));
		if(ML.isEmpty()){
			Log.d("Songs","ML is empty");
			init = false;
		}else{
			init = true;
			start = System.currentTimeMillis();
			SharedPreferences sp = getSharedPreferences(preference, MODE_PRIVATE);
			String lSong = sp.getString(LAST_SONG, null);
			if( lSong != null){
				setSong(lSong);
				ML.positionSong(lSong);
				prepareSong();
				mp.seekTo(sp.getInt(LAST_SONG_TIME, mp.getCurrentPosition()));
			}else{
				setSong(ML.NextSong(mp.isLooping()));
				prepareSong();
			}
			Log.d("Time", "Ser SharedPreferences : "+
					(System.currentTimeMillis() - start));
		}
		
		Log.i("Service_bian", "«Ø¥ß");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		if(seekThread == null)
			seekThread = new Thread(this);
		if(mp.isPlaying())
			seekThread.start();
		Log.i("Service_bian", "onBind");
		return mMessenger.getBinder();
	}
	@Override
	public boolean onUnbind(Intent intent) {
		closeSeekThread();
		Log.i("Service_bian", "onUnbind");
		return super.onUnbind(intent);
	};
	
	@Override
	public void onDestroy() {
		 // TODO Auto-generated method stub
		super.onDestroy();
		if( mp != null ){
    		SharedPreferences sp = getSharedPreferences(preference, MODE_PRIVATE);
    		sp.edit()
    			.clear()
    			.putString(LAST_SONG, ML.CurrSong())
    			.putInt(LAST_SONG_TIME, mp.getCurrentPosition())
    			.commit();
    		mp.release();
    	}
    	mp = null;
    	ML = null;
    	cMessenger = null;
		Log.i("Service_bian", "¾P·´");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	 // TODO Auto-generated method stub
		Log.i("Service_bian", "°õ¦æ");
		int what = intent.getIntExtra("MSG",-1);
		if(what != -1){
			sendMsg(what, mMessenger);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	//Player functions
	private void play(){
			mp.start();
	}
	private void pause(){
		if(mp.isPlaying()){
			mp.pause();
		}
	}
	private void stop(){
		mp.stop();
	}
	private void seekTo(int position){
		mp.seekTo(position);
	}
	private void next(){
		boolean playing;
		playing = mp.isPlaying();
		if(playing)
			stop();
		setSong(ML.NextSong(true));
		prepareSong();
		if(playing)
			play();
	}
	private void pre(){
		boolean playing;
		playing = mp.isPlaying();
		if(playing)
			stop();
		setSong(ML.PreSong());
		prepareSong();
		if(playing)
			play();
	}
	
    private void prepareSong(){
    	try {
    		Log.d("initSong", "Prepare");
			mp.prepare();
			//changeSong();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	private void setSong(String song){
		try {
			Log.d("initSong", "Set song");
			mp.reset();
			mp.setDataSource(song);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Player functions
	
//  ----MediaPlayer interface----
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		setSong(ML.NextSong(mp.isLooping()));
		prepareSong();
		mp.start();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		// http://stackoverflow.com/questions/22827190/android-mediaplayer-onpreparedlistener
		Log.i("initSong","Send message when music prepared !");
		changeSong();
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		Bundle data = new Bundle();
		data.putInt("BUF_PROGRESS", percent);
		sendMsg(BUF_UPDATE, data, cMessenger);
		
	}
	private void changeSong(){
		Bundle data = new Bundle();
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(ML.CurrSong());
		data.putInt("MCS_DURATION", mp.getDuration());
		data.putString("MCS_NAME", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
		Log.i("Handler_bian","SER : Send MP_CHANGE_SONG");
		sendMsg(MP_CHANGE_SONG, data, cMessenger);
	}
//  ----MediaPlayer interface----

//	----Seekbar thread----
	private void closeSeekThread(){
		IsActivityAlive = false;
		if(seekThread != null && seekThread.isAlive()){
			seekThread.interrupt();
		}
		seekThread = null;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		IsActivityAlive = true;
		while(IsActivityAlive){
			if(mp.getCurrentPosition() < 0) continue;
			Bundle data = new Bundle();
			data.putInt("Duration", mp.getCurrentPosition());
			sendMsg(SK_CHANGE, data, cMessenger);
			try {
				Thread.sleep(1010 - mp.getCurrentPosition()%1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.i("Thread_bian", "SeekBar thread interrupt");
				break;
			}
		}
	}
//	----Seekbar thread----
	
	private void sendMsg(int what, Messenger mm){
		if(mm == null){
			Log.d("Service_bian", "Service : cMessenger is null with " + String.valueOf(what));
			return;
		}
		Message msg = new Message();
		msg.what = what;
		try {
			mm.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void sendMsg(int what, Bundle data,  Messenger mm){
		if(mm == null){
			Log.d("Service_bian", "Service : cMessenger is null with " + String.valueOf(what));
			return;
		}
		Message msg = new Message();
		msg.what = what;
		msg.setData(data);
		try {
			mm.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
