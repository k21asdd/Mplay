package com.example.mplay;

import java.io.File;
import java.io.FileFilter;
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
	
	public MusicList(){
		MuArrList = new ArrayList<String>();
		MusicPath = new ArrayList<String>();
		if(!isNotExternalStorageReadable())
			MusicPath.add(Environment.getExternalStorageDirectory().getPath());
		MusicPath.add(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_MUSIC).getPath());
		for(String BasePath: MusicPath)
			Log.d("ML", BasePath);
		for(String path:MusicPath){
			for(File song: new File(path).listFiles()){
				Log.d("ML", song.getName());
			}
		}
		/*for(File songName: new File(MusicPath.get(0)).listFiles
				(new FileFilter(){
					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						if( pathname.isFile())
							return pathname.toString().matches("[\\w0-9\\s\\S]+.mp3$");
						else
							return false;
					}
				})
			){
			if(songName.isFile()==false);
			else MuArrList.add(songName.getPath());
		}*/
		songs = MuArrList.size();
		MuArr = new int[songs];
		for(int i = 0 ; i < songs; i++)
			MuArr[i] = i;
	}
	public void shuffle(){
		
	}
	public boolean isEmpty(){
		return MuArrList.isEmpty();
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
	private boolean isNotExternalStorageReadable(){
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return false;
	    }
	    return true;
	}
	

}
