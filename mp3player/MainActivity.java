package com.example.mp3player;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class MainActivity extends Activity implements 
MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnPreparedListener{

	private Button action,pre,next;
	private MediaPlayer mp;
	private TextView currenTime, finalTime, songName; 
	private SeekBar MusicBar;
	private MusicList ML;
	private Handler seekBarHandler;
	private Thread seekBarThread;
	private Runnable seekBarRun;
	private boolean init = false;
	
	public static final int RUN = 0;
	public static final int PAUSE = 1;
	public static final int END = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.i("Bian","Create");
        
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnPreparedListener(this);
        
        ML = new MusicList();
        action = (Button)findViewById(R.id.action);
        pre = (Button)findViewById(R.id.preSong);
        next = (Button)findViewById(R.id.nextSong);
        currenTime = (TextView)findViewById(R.id.currenTime);
        finalTime = (TextView)findViewById(R.id.finalTime);
        songName = (TextView)findViewById(R.id.songName);
        MusicBar = (SeekBar)findViewById(R.id.seekMusic);
        
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				setSong(ML.NextSong(mp.isLooping()));
				prepareSong();
				mp.start();
			}
		});
       
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
						seekBarThread = new Thread(seekBarRun);
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
        
        MusicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress = 0;
			boolean User;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if(!init)return;
				if( User ){
					mp.seekTo((int)TimeUnit.SECONDS.toMillis(progress));
					seekBarHandler.obtainMessage(RUN).sendToTarget();
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
				currenTime.setText(String.format("Current: %d:%d",
						TimeUnit.SECONDS.toMinutes(progress),
						progress - TimeUnit.MINUTES.toSeconds(
								TimeUnit.SECONDS.toMinutes(progress)
								)
						)
					);
				if( User )
					seekBarHandler.obtainMessage(PAUSE).sendToTarget();
					
			}
		});
        
        seekBarHandler = new Handler(){
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case RUN:
					MusicBar.setProgress(mp.getCurrentPosition());
					Log.i("Bian", "Seekbar runs " + (1010 - mp.getCurrentPosition()%1000));
					break;
				case PAUSE:
					Log.i("Bian", "Seekbar pause");
					break;
				case END:
					closeSBT(seekBarThread);
					Log.i("Bian", "Seekbar end");
					break;
				}
			}
		};
		seekBarRun = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!Thread.currentThread().interrupted() && mp != null){
					if( mp.isPlaying() ){
						seekBarHandler.sendMessage(seekBarHandler.obtainMessage(RUN));
						Log.d("Bian",seekBarThread.getName());
						try{
							Thread.sleep(1010 - mp.getCurrentPosition()%1000);
						}catch(InterruptedException a){
							Log.d("Bian","Seekbar thread break");
							break;
						}
					}
				}
			}
    	};		
        //start running
        if(ML.isEmpty())
        	currenTime.setText("Something wrong");
        else{
        	init = true;
        	mp.setLooping(true);
        	setSong(ML.NextSong(true));
        	prepareSong();
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
	private void closeSBT(Thread sbt){
		if(sbt != null && sbt.isAlive()){
			sbt.interrupt();
			sbt = null;
    	}
	}
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
    	Log.i("Bian","Destory");
    	closeSBT(seekBarThread);
    	if( mp != null )
    		mp.release();
    	mp = null;
    	super.onDestroy();
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
		action.setText("¼½©ñ");
	}


	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		Log.d("Bian", "Buffering : "+Integer.toString(percent));
		MusicBar.setSecondaryProgress(percent);
	}
}
