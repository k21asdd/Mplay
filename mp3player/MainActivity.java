package com.example.mp3player;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements 
MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnPreparedListener{

	private Button action,pre,next;
	private TextView currenTime, finalTime, songName; 
	private SeekBar MusicBar;
	
	//Is this right?
	
	private static final class seekBarHandler extends Handler{
		WeakReference<MainActivity> mAct;
		public seekBarHandler(MainActivity mm){
			// TODO Auto-generated constructor stub
			mAct = new WeakReference<MainActivity>(mm);
		}
		@Override
		public void handleMessage(Message msg){
			MainActivity act = mAct.get();
			if(act == null)return;
			switch(msg.what){
			case MplayerService.MP_CHANGE_SONG:
				Log.i("Handler_bian", "MP_CHANGE_SONG");
				break;
			case MplayerService.SK_CHANGE:					
				int progress = msg.getData().getInt("Duration");
				act.currenTime.setText(String.format("Current: %d:%d",
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
	private final seekBarHandler mHandler = new seekBarHandler(this);
	private Messenger mMessanger  = new Messenger(mHandler);
	
	private Messenger cMessanger;
	private final ServiceConnection MPSconn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			cMessanger = new Messenger(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			cMessanger = null;
		}
	};
	@SuppressLint("NewApi")
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
				
				try {
					if( action.getText().toString().equals("¼È°±")){
						sendMsg(MplayerService.MP_PAUSE,cMessanger);
						action.setText("¼½©ñ");
			        	
					}
					else if ( action.getText().toString().equals("¼½©ñ")){
						sendMsg(MplayerService.MP_PLAY,cMessanger);
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
				sendMsg(MplayerService.ACT_OPEN,cMessanger);
			}
        });
        
        pre.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendMsg(MplayerService.MP_PRE,cMessanger);
			}
        });

		
        //start running
    	MusicBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
    	
    	Intent startIntent = new Intent(MainActivity.this, MplayerService.class);
		startService(startIntent);
		bindService(startIntent, MPSconn, Context.BIND_AUTO_CREATE);
		
		
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
		sendMsg(MplayerService.ACT_OPEN, cMessanger);
	}
	@Override
	protected void onPause() {
		Log.i("Bian","Pause");
		
		// TODO Auto-generated method stub
		super.onPause();
		sendMsg(MplayerService.ACT_CLOSE, cMessanger);
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
    	Intent stopIntent = new Intent(MainActivity.this, MplayerService.class);  
        stopService(stopIntent);
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

//  ----MediaPlayer interface----
	private final class SeekBarChangeListener implements OnSeekBarChangeListener{
		
		int progress = 0;
		boolean User;
		
		public SeekBarChangeListener() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if( User ){
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putInt("Duration", progress);
				msg.what = MplayerService.SK_CHANGE;
				msg.setData(data);
				try {
					cMessanger.send(msg);
					mMessanger.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			msg.what = MplayerService.SK_CHANGE;
			try {
				mMessanger.send(msg);
				if( User )
					cMessanger.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
