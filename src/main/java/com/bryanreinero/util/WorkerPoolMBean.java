package com.bryanreinero.util;

public interface WorkerPoolMBean {
	public void setNumThreads(int count);
	public int getNumThreads();
	public void start();
	public void stop();
}
