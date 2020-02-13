package com.zq.utils.concurrent.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockRunner {
    private Lock lock = new ReentrantLock();
    private Condition hello,world;
  
    
    LockRunner(){
        this.hello = lock.newCondition();
        this.world = lock.newCondition();
    }
    
    public void run() {
        this.lock.lock();
        try {
            this.hello.awaitUninterruptibly();
        }finally {
            lock.unlock();
        }
    }
    
    
    public static void main(String[] args) {
        
    }
}
