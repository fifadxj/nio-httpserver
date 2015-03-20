package server.aio;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
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
        	if (tasks.size() == 0) {
        	    tasks.add(task);
        		tasks.notifyAll();
        	}
        }
    }
    
    private Task fetchTask() {
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                try {
                	tasks.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
        }
    }
}