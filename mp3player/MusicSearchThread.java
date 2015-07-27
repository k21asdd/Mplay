package com.example.mp3player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.media.MediaMetadataRetriever;
import android.util.Log;


public class MusicSearchThread extends Thread{
	private ArrayList<String> MuArrList;
	private ArrayList<File> DirArrList;
	MusicSearchThread(final File folder, ArrayList<String> songs){
		MuArrList = songs;
		DirArrList = new ArrayList<File>();
		DirArrList.add(folder);
	}
	@Override
	public void run(){
		String dir = DirArrList.get(0).getName();
		long start = System.currentTimeMillis();
		while(DirArrList.size() > 0){
			File f = DirArrList.remove(DirArrList.size()-1);
			for(File subF: f.listFiles()){
				if(subF.isFile() && acceptMp3(subF)){
					synchronized(MuArrList){
						MuArrList.add(subF.getPath());
					}
				}else if(subF.isDirectory()){
					DirArrList.add(subF);
				}
			}
		}
		Log.d("Time", "Thread " + dir + " : " +
				(System.currentTimeMillis() - start));
	}
	private boolean acceptMp3(File pathname) {
		return pathname.toString().matches("[\\w0-9\\s\\S]+.mp3$");
	}
}
