package com.example.mp3player;

import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarChangeListener implements OnSeekBarChangeListener{
	
	private MediaPlayer mp;
	private Handler mhandler;
	int progress = 0;
	boolean User;
	
	public SeekBarChangeListener(MediaPlayer m, Handler h) {
		// TODO Auto-generated constructor stub
		mp = m;
		mhandler = h;
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if(mp == null)return;
		if( User ){
			mp.seekTo((int)TimeUnit.SECONDS.toMillis(progress));
			mhandler.obtainMessage(MainActivity.RUN).sendToTarget();
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progressValue,
			boolean fromUser) {
		// TODO Auto-generated method stub
		User = fromUser;
		progress = (int) TimeUnit.MILLISECONDS.toSeconds(progressValue);
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putInt("SK_CHANGE", progress);
		msg.setData(data);
		msg.what = MainActivity.SK_CHANGE;
		mhandler.sendMessage(msg);
		if( User )
			mhandler.obtainMessage(MainActivity.PAUSE).sendToTarget();
			
	}

}
