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


public class MainActivity extends Activity{

	private Button action,pre,next;
	private TextView currenTime, finalTime, songName; 
	private SeekBar MusicBar;
	
	//Is this right for memory leak?
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
				Log.i("Handler_bian", "ACT : MP_CHANGE_SONG");
				Bundle data = msg.getData();
				act.newSong(data.getInt("MCS_DURATION"), data.getString("MCS_NAME"));
				break;
			case MplayerService.SK_CHANGE:
				int progress = msg.getData().getInt("Duration");
				act.MusicBar.setProgress(progress);
				act.currenTime.setText(String.format("%d:%d",
					TimeUnit.MILLISECONDS.toMinutes(progress),
					TimeUnit.MILLISECONDS.toSeconds(progress) - 
					TimeUnit.MINUTES.toSeconds(
							TimeUnit.MILLISECONDS.toMinutes(progress)
							)
					)
				);
			break;
			case MplayerService.BUF_UPDATE:
				Log.i("Handler_bian", "ACT : BUF_UPDATE "
						+Integer.toString(msg.getData().getInt("BUF_PROGRESS")));
				act.MusicBar.setSecondaryProgress(msg.getData().getInt("BUF_PROGRESS"));
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
			sendMsg(MplayerService.ACT_OPEN, cMessanger);
			Log.i("Service_bian", "SER : onServiceConnected " +cMessanger.toString());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i("Service_bian", "SER : onServiceDisconnected");
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
				sendMsg(MplayerService.MP_NEXT,cMessanger);
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
    	MusicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    		
    		int progress = 0;
    		boolean User;
    		@Override
    		public void onStopTrackingTouch(SeekBar seekBar) {
    			// TODO Auto-generated method stub
    			if( User ){
    				Bundle data = new Bundle();
    				data.putInt("Duration", progress);
    				sendMsg(MplayerService.SK_CHANGE, data, cMessanger);
    				sendMsg(MplayerService.SK_CHANGE, data, mMessanger);
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
    			progress = progressValue;
    		}
		});	
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
		Intent startIntent = new Intent(MainActivity.this, MplayerService.class);
		startService(startIntent);
		bindService(startIntent, MPSconn, Context.BIND_AUTO_CREATE);
		
	}
	@Override
	protected void onPause() {
		Log.i("Bian","Pause");
		sendMsg(MplayerService.ACT_CLOSE, cMessanger);
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
		unbindService(MPSconn);
	}
    @Override
    protected void onDestroy(){
    	Log.i("Bian","Destory ");
    	Intent stopIntent = new Intent(MainActivity.this, MplayerService.class);  
        stopService(stopIntent);
    	super.onDestroy();
    }
//  ----Activity actions----    
    

	private void newSong(int duration, String name){
		songName.setText(name);
		MusicBar.setMax(duration);
		MusicBar.setProgress(0);
		currenTime.setText("0:0");
		finalTime.setText(String.format("%d:%d",
			TimeUnit.MILLISECONDS.toMinutes(duration),
			TimeUnit.MILLISECONDS.toSeconds(duration)-
			TimeUnit.MINUTES.toSeconds(
					TimeUnit.MILLISECONDS.toMinutes(duration
					)
				)
			)
		);
	}
	private void sendMsg(int what, Messenger mm){
		Log.i("Service_bian", "Activity : send Msg");
		if(mm == null){
			Log.e("Service_bian", "Activity : cMessenger is null");
			return;
		}
		Message msg = new Message();
		msg.what = what;
		msg.replyTo = mMessanger;
		try {
			mm.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void sendMsg(int what, Bundle data,  Messenger mm){
		Log.i("Service_bian", "Activity : send Msg");
		if(mm == null){
			Log.e("Service_bian", "Activity : cMessenger is null");
			return;
		}
		Message msg = new Message();
		msg.what = what;
		msg.setData(data);
		msg.replyTo = mMessanger;
		try {
			mm.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//  ----Below code are just configuration for somethings----
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
}
