package com.example.mp3player;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements 
MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnPreparedListener,
MediaPlayer.OnCompletionListener{

	private Button action,pre,next;
	private MediaPlayer mp;
	private TextView currenTime, finalTime, songName; 
	private SeekBar MusicBar;
	private MusicList ML;
	private Handler seekBarHandler;
	private SeekThread seekBarThread;
	private final String preference = "BianMP3";

	private boolean init = false;
	
	public static final int RUN = 0;
	public static final int PAUSE = 1;
	public static final int END = 2;
	public static final int SK_CHANGE = 3;
	
	private static final String LAST_SONG = "LASTSONG";
	private static final String LAST_SONG_TIME = "LASTSONG_TIME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.i("Bian","Create");
        
        action = (Button)findViewById(R.id.action);
        pre = (Button)findViewById(R.id.preSong);
        next = (Button)findViewById(R.id.nextSong);
        currenTime = (TextView)findViewById(R.id.currenTime);
        finalTime = (TextView)findViewById(R.id.finalTime);
        songName = (TextView)findViewById(R.id.songName);
        MusicBar = (SeekBar)findViewById(R.id.seekMusic);
       
        action.setOnClickListener(new OnClickListener (){
        	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!init)return;
				try {
					if( action.getText().toString().equals("¼È°±")){
						if(mp.isPlaying()){
							mp.pause();
						}
						closeSBT(seekBarThread);
						action.setText("¼½©ñ");
			        	
					}
					else if ( action.getText().toString().equals("¼½©ñ")){
						mp.start();
						seekBarThread = new SeekThread(mp, seekBarHandler);
						seekBarThread.start();
						action.setText("¼È°±");
					}
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });
        next.setOnClickListener(new OnClickListener (){
        
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!init)return;
				boolean playing;
				playing = mp.isPlaying();
				
				if(playing)
					mp.stop();
				setSong(ML.NextSong(true));
				prepareSong();
				if(playing)
					mp.start();
			}
        	
        });
        
        pre.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!init)return;
				boolean playing;
				playing = mp.isPlaying();	
				
				if(playing)
					mp.stop();
				setSong(ML.PreSong());
				prepareSong();
				if(playing)
					mp.start();
			}
        	
        });

        seekBarHandler = new Handler(){
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case RUN:
					MusicBar.setProgress(mp.getCurrentPosition());
					break;
				case PAUSE:
					Log.i("Handler_bian", "Seekbar pause");
					break;
				case END:
					closeSBT(seekBarThread);
					Log.i("Handler_bian", "Seekbar end");
					break;
				case SK_CHANGE:					
					int progress = msg.getData().getInt("SK_CHANGE");
					currenTime.setText(String.format("Current: %d:%d",
						TimeUnit.SECONDS.toMinutes(progress),
						progress - TimeUnit.MINUTES.toSeconds(
								TimeUnit.SECONDS.toMinutes(progress)
								)
						)
					);
					break;
				}
			}
		};
		
        //start running
    	mp = new MediaPlayer();
        mp.setOnBufferingUpdateListener(this);
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        
    	ML = new MusicList();
    	MusicBar.setOnSeekBarChangeListener(new SeekBarChangeListener(mp,seekBarHandler));
    	SharedPreferences sp = getSharedPreferences(preference, MODE_PRIVATE);
    	
        if(ML.isEmpty()){
        	currenTime.setText("Something wrong");
        	mp = null;
        }
        else{
        	init = true;
        	mp.setLooping(true);
        	String s = sp.getString(LAST_SONG, ML.NextSong(true));
        	setSong(s);
        	ML.positionSong(s);
        	prepareSong();
        	mp.seekTo(sp.getInt(LAST_SONG_TIME, 0));
        	seekBarHandler.obtainMessage(RUN).sendToTarget();
        }
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
			songName.setText(song.substring(song.lastIndexOf('/')+1, song.lastIndexOf('.')));
			MusicBar.setProgress(0);
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
//	Close seekbar thread
	private void closeSBT(Thread sbt){
		if(sbt != null && sbt.isAlive()){
			sbt.interrupt();
			sbt = null;
    	}
	}
//  ----Option menu----
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
//  ----Option menu----    
    
//  ----Activity actions----
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.i("Bian","Start");
		super.onStart();
	}
	@Override
	protected void onPause() {
		Log.i("Bian","Pause");
		// TODO Auto-generated method stub
		SharedPreferences sp = getSharedPreferences(preference, MODE_PRIVATE);
		sp.edit()
			.clear()
			.putString(LAST_SONG, ML.CurrSong())
			.putInt(LAST_SONG_TIME, mp.getCurrentPosition())
			.commit();
		super.onPause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i("Bian","Resume");
		Log.i("Bian",Thread.currentThread().getName());
		super.onResume();
	}
	@Override
	protected void onStop() {
		Log.i("Bian","Stop");
		// TODO Auto-generated method stub
		super.onStop();
	}
    @Override
    protected void onDestroy(){
    	Log.i("Bian","Destory ");
    	closeSBT(seekBarThread);
    	if( mp != null ){
    		mp.release();
    	}
    	mp = null;
    	super.onDestroy();
    }
//  ----Activity actions----    
    
//  ----Over write Back action----
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
            Intent intentHome = new Intent(Intent.ACTION_MAIN);
            intentHome.addCategory(Intent.CATEGORY_HOME);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
//  ----Over write Back action----
    
//	----Orientation----
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if(newConfig.orientation ==Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this,"landscape",Toast.LENGTH_SHORT).show();
        }else if(newConfig.orientation ==Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this,"portrait",Toast.LENGTH_SHORT).show();
        }
    }
//  ----Orientation----
    
//	----MediaPlayer interface----
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
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		setSong(ML.NextSong(mp.isLooping()));
		prepareSong();
		mp.start();
	}
//  ----MediaPlayer interface----
}
