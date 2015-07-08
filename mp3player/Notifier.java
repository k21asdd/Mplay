package com.example.mp3player;

import android.util.Log;

public class Notifier implements Runnable{
	Thread waitThread;
	public Notifier(Thread t){
		waitThread = t;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.d("Thread_bain", "Notify thread");
		synchronized (waitThread) {
			waitThread.notify();
		}
	}

}