package com.example.mp3player;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
	public static final int ACT_OPEN = 0;
	public static final int ACT_CLOSE = 1;
	public static final int MP_PLAY = 2;
	public static final int MP_PAUSE = 3;
	public static final int MP_NEXT = 4;
	public static final int MP_PRE = 5;
	public static final int MP_CHANGE_SONG = 6;
	public static final int SK_CHANGE = 7;
	public static final int BUF_UPDATE = 8;
	
	//Preference
	private final String preference = "BianMP3";
	private static final String LAST_SONG = "LASTSONG";
	private static final String LAST_SONG_TIME = "LASTSONG_TIME";
	private final SharedPreferences sp = this.getSharedPreferences(preference, MODE_PRIVATE);
	
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
			case MplayerService.ACT_OPEN:
				service.IsActivityAlive = true;
				break;
			case MplayerService.ACT_CLOSE:
				service.IsActivityAlive = false;
				service.closeSeekThread();
				break;
			case MplayerService.MP_PLAY:
				service.play();
				service.seekThread = new Thread(service);
				break;
			case MplayerService.MP_PAUSE:
				service.pause();
				service.closeSeekThread();
				break;
			case MplayerService.MP_NEXT:
				service.next();
				break;
			case MplayerService.MP_PRE:
				service.pre();
				break;
			case MplayerService.SK_CHANGE:
				//change by user
				service.seekTo(msg.getData().getInt("Duration"));
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
		ML = new MusicList();
		if(ML.isEmpty()){
			init = false;
		}else{
			setSong(ML.NextSong(mp.isLooping()));
			prepareSong();
		}
		
		Log.i("Service_bian", "«Ø¥ß");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}
	
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
		return super.onStartCommand(intent, flags, startId);
	}
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
	private void closeSeekThread(){
		if(seekThread != null && seekThread.isAlive()){
			seekThread.interrupt();
			seekThread = null;
		}
	}
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
		
		MusicBar.setMax(mp.getDuration());
		finalTime.setText(String.format("Length: %d:%d",
			TimeUnit.MILLISECONDS.toMinutes(mp.getDuration()),
			TimeUnit.MILLISECONDS.toSeconds(mp.getDuration())-
			TimeUnit.MINUTES.toSeconds(
					TimeUnit.MILLISECONDS.toMinutes(mp.getDuration()
					)
				)
			)
		);
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		Log.d("Bian", "Buffering : "+Integer.toString(percent));
		MusicBar.setSecondaryProgress(percent);
	}
//  ----MediaPlayer interface----

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!Thread.currentThread().interrupted()){
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putInt("Duration", mp.getCurrentPosition());
			msg.setData(data);
			msg.what = MplayerService.SK_CHANGE;
			try {
				mMessenger.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			try{
				Thread.sleep(1010 - mp.getCurrentPosition()%1000);
			}catch(InterruptedException a){
				Log.d("Bian","Seekbar thread break");
				break;
			}
		}
	}
	private void sendMsg(int what, Messenger mm){
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
