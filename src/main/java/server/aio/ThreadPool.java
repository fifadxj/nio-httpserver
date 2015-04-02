package server.aio;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.NioHttpServer;

public class ThreadPool {
    private static Logger logger = LoggerFactory.getLogger(ThreadPool.class);
    private static final int POOL_SIZE = 100;

    private List<Task> tasks = new ArrayList<Task>();

    public void start() {
        for (int i = 0; i < POOL_SIZE; i++) {
        	PoolThread thread = new PoolThread(i);
            thread.start();
        }
    }
    
    public void assignTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
        	tasks.notify();
        }
    }
    
    private Task fetchTask() {
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                try {
                	tasks.wait();
                } catch (InterruptedException e) {
                    logger.error("thread interrupted when fetchTask", e);
                }
            }
            Task task = tasks.remove(0);

            return task;
        }
    }
    
    private class PoolThread extends Thread {
        private int id;

        public PoolThread(int id) {
            this.id = id;
        }

        public void run() {
            while (true) {
            	Task task = ThreadPool.this.fetchTask();
                try {
                	task.execute(id);
    			} catch (Throwable e) {
    			    logger.error("thread interrupted when fetchTask", e);
    			}
            }
        }
    }
}