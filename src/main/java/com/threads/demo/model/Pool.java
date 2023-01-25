package com.threads.demo.model;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class Pool <R> {
    private static final int POOL_SIZE = 10;
    private BlockingQueue<R> pool;
    private Set<R> managedResources;

    public synchronized void open() {
        if(!this.isOpen()){
            this.pool = new ArrayBlockingQueue<>(POOL_SIZE);
            this.managedResources = new HashSet<>();
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

        if (!managedResources.contains(resource)) {
            this.pool.put(resource);
            managedResources.add(resource);
            resourceAdded = true;
        }

        return resourceAdded;
    }

    public synchronized boolean remove(R resource) throws Exception {

        boolean resourceRemoved = false;

        if (managedResources.contains(resource)) {
            this.pool.remove(resource);
            resourceRemoved = true;
            managedResources.remove(resource);
        }

        return resourceRemoved;
    }

    public synchronized boolean removeNow(R resource) throws Exception {

        boolean resourceRemoved = false;
        List<R> poolList = new ArrayList<>();

        if (managedResources.contains(resource)) {
            this.pool.drainTo(poolList);
            poolList.remove(resource);
            this.pool.addAll(poolList);
            resourceRemoved = true;
            managedResources.remove(resource);
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
        if(managedResources.contains(resource)){
            this.pool.put(resource);
        }
    }

    public int size() {
        return this.pool.size();
    }

}
