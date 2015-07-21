package com.example.mp3player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.media.MediaMetadataRetriever;
import android.util.Log;


public class MusicSearchThread extends Thread{
	private File root;
	private ArrayList<String> MuArrList;
	MusicSearchThread(final File folder, ArrayList<String> songs){
		root = folder;
		MuArrList = songs;
	}
	@Override
	public void run(){
		if(root.isFile())return;
		recFindMusic(root);
	}
	private void recFindMusic(File f){
		if(f.listFiles() == null) return;
		for(File subF: f.listFiles()){
			if(subF.isFile() && acceptMp3(subF)){
				synchronized(MuArrList){
					MuArrList.add(subF.getPath());
					MediaMetadataRetriever mmr = new MediaMetadataRetriever();
					mmr.setDataSource(subF.getPath());
					Log.i("Songs",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)+" "+
							mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)+" "+
							mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)+" "+
							mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR));
				}
			}else if(subF.isDirectory()){
				recFindMusic(subF);
			}
		}
	}
	private boolean acceptMp3(File pathname) {
		return pathname.toString().matches("[\\w0-9\\s\\S]+.mp3$");
	}
}
