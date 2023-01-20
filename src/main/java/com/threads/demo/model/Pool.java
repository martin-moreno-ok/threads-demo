package com.threads.demo.model;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class Pool <R extends Resource> {

    private static final int POOL_SIZE = 10;
    private BlockingQueue<R> pool;

    public synchronized void open() {
        if(!this.isOpen()){
            this.pool = new ArrayBlockingQueue<>(POOL_SIZE);
        }
    }

    public synchronized boolean isOpen() {
        return this.pool != null;
    }

    public synchronized void close() throws InterruptedException {
        while(this.pool.size() > 0){
            this.pool.take();
        }
        this.pool = null;
    }

    public synchronized void closeNow() {
        this.pool.clear();
    }

    public synchronized boolean add(R resource) throws InterruptedException {

        boolean resourceAdded = false;

        if (!resource.isManaged()) {
            this.pool.put(resource);
            resource.setManaged(true);
            resourceAdded = true;
        }

        return resourceAdded;
    }

    public synchronized boolean remove(R resource) throws Exception {

        boolean resourceRemoved = false;

        if (resource.isManaged()) {
            this.pool.remove(resource);
            resourceRemoved = true;
            resource.setManaged(false);
        }

        return resourceRemoved;
    }

    public synchronized boolean removeNow(R resource) throws Exception {

        boolean resourceRemoved = false;
        List<R> poolList = new ArrayList<>();

        if (resource.isManaged()) {
            this.pool.drainTo(poolList);
            poolList.remove(resource);
            this.pool.addAll(poolList);
            resourceRemoved = true;
            resource.setManaged(false);
        }

        return resourceRemoved;
    }

    public R acquire() throws RuntimeException, InterruptedException {
        if (!isOpen()) {
            throw new RuntimeException("Pool is closed");
        }
        return this.pool.take();
    }

    public R acquire(long timeout, TimeUnit unit) throws InterruptedException {
        return this.pool.poll(timeout, unit);
    }

    public void release(R resource) throws InterruptedException {
        if(resource.isManaged()){
            this.pool.put(resource);
        }
    }

}
