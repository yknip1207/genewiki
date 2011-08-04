package org.genewiki;

import java.util.HashMap;

import com.google.common.base.Preconditions;

public enum StatusMonitor {

	instance;	// The singleton instance of the StatusMonitor
	
	private HashMap<String, Status> threadStatus;
	
	StatusMonitor() {
		threadStatus = new HashMap<String, Status>();
	}
	
	public void register(String threadName) {
		threadStatus.put(threadName, Status.NORMAL);
	}
	
	public void notifyFailed(String threadName) {
		setStatus(threadName, Status.FAILED);
	}
	
	public void notifyExited(String threadName) {
		setStatus(threadName, Status.EXITED);
	}
	
	public Status query(String threadName) {
		try {
			return Preconditions.checkNotNull(threadStatus.get(threadName),
					"Thread name not found.");
		} catch (NullPointerException e) {
			return Status.NOTFOUND;
		}
	}
	
	public void setStatus(String threadName, Status status) {
		try {
			Preconditions.checkNotNull(threadStatus.get(threadName), 
					"Invalid thread name. Did you register this thread?");
			threadStatus.put(threadName, status);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	
}

/**
 * Status representing each possible state
 * @author eclarke
 *
 */
enum Status {
	FAILED,
	EXITED,
	NORMAL,
	NOTFOUND
}