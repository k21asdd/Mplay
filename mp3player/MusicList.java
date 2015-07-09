package com.example.mp3player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

public class MusicList{
	private List<String> MusicPath;
	private int[] MuArr;
	private int current = -1;
	private int songs = 0;
	private ArrayList<String> MuArrList;
	private final String[] ignoreFolder = {
			"Ringtones","Alarms","Notifications",
			"Pictures","Movies","DCIM","Android",
			".data","My Documents","Albums","yahoo",
			"data"};
	public MusicList(){
		MuArrList = new ArrayList<String>();
		MusicPath = new ArrayList<String>();
		ArrayList<Thread> thCheck = new ArrayList<Thread>();
		if(!isNotExternalStorageReadable())
			MusicPath.add(Environment.getExternalStorageDirectory().getPath());
		MusicPath.add(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MUSIC).getPath());
		for(String path:MusicPath){
			Log.d("ML", path);
			for(File song: new File(path).listFiles()){				
				if(song.isDirectory() && notInIgnore(song.getName())){
					thCheck.add(new MusicSearchThread(song, MuArrList));
					thCheck.get(thCheck.size()-1).start();
					Log.d("Thread_bian", "#"+thCheck.size()+" New Thread for "+song.getName());
				}
			}
		}
		Log.d("Thread_bian","Wait for thread");
		boolean max;
		do{
			max = false;
			for(Thread t:thCheck) if(t.isAlive())max=true;
		}while(max);
		Log.d("Thread_bian","Done for Waiting thread");

		songs = MuArrList.size();
		MuArr = new int[songs];
		for(int i = 0 ; i < songs; i++){
			MuArr[i] = i;
//			Log.d("Songs", MuArrList.get(i));
		}
	}
	public void shuffle(){
		
	}
	public boolean isEmpty(){
		return MuArrList.isEmpty();
	}
	public String CurrSong(){
		return current != -1 ? MuArrList.get(MuArr[current]) : null;
	}
	public String NextSong(boolean isLooping){
		if(isLooping && current + 1 == songs)
			current = -1;
		if(current + 1 >= songs ){
			return null;
		}else{
			current++;
			return MuArrList.get(MuArr[current]);
		}
	}
	public String PreSong(){
		if( current <= 0)
			current = songs;
		current--;
		return MuArrList.get(MuArr[current]);
	}
	public void positionSong(String song){
		current = MuArrList.indexOf(song);
	}
	private boolean isNotExternalStorageReadable(){
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return false;
	    }
	    return true;
	}
	private boolean notInIgnore(String folder){
		for(String s: ignoreFolder){
			if(s.compareTo(folder)==0)return false;
		}
		return true;
	}

}
