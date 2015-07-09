package com.example.mp3player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class SeekThread extends Thread{
	private MediaPlayer mp;
	private Handler mhandler;
	
	
	public SeekThread(MediaPlayer m, Handler h) {
		// TODO Auto-generated constructor stub
		mp = m;
		mhandler = h;
	}
	
	@Override
	public void run(){
		while(!Thread.currentThread().interrupted()){
			if( mp != null && mp.isPlaying() ){
				mhandler.sendMessage(mhandler.obtainMessage(MainActivity.RUN));
				try{
					Thread.sleep(1010 - mp.getCurrentPosition()%1000);
				}catch(InterruptedException a){
					Log.d("Bian","Seekbar thread break");
					break;
				}
			}
		}
	}

}
