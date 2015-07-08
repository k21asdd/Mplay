package com.example.mp3player;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class MusicSearchThread extends Thread{
	private File root;
	private ArrayList<String> MuArrList;
	private Stack<HashMap<File[], Integer>> sFolder;
	MusicSearchThread(final File folder, ArrayList<String> songs){
		root = folder;
		MuArrList = songs;
//		sFolder = new Stack<HashMap<File[], Integer>>();
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