package com.example.mp3player;

import android.util.Log;

public class Waiter implements Runnable{
	Thread runThread;
	public Waiter(Thread t){
		runThread = t;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.d("Thread_bain", "Thread is waitting");
		synchronized (runThread) {
			try {
				runThread.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.d("Thread_bain", "Thread interrup when waitting");
			}
		}
		Log.d("Thread_bain", "Thread is notified");
	}

}
