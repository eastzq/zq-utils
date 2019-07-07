package com.zq.utils.concurrent.interrupt;

public class Interrupt  {

	public static  class InterruptThread implements Runnable{
		@Override
		public void run() {
			while(!Thread.interrupted()) {
				System.out.println("1");

			}
		}	
	}
	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new InterruptThread());
		t.start();
		while(true) {
			try {
				Thread.sleep(1000);
				t.interrupt();
				System.out.println(t.isInterrupted());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
