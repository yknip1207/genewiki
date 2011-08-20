package org.genewiki.concurrent;

import java.util.List;

/**
 * Implement this interface in conjunction with a Distributor
 * to automatically generate Runnable threads.
 * @author eclarke
 *
 */
public interface RunnableFactory {
	
	/**
	 * Wrap the initialization code of your runnable in this
	 * factory method, to accept a list of tasks and hold
	 * an id (for optional debug output).
	 * @param tasks some list of task inputs
	 * @param id use this to identify individual threads
	 * @return a new copy of your Runnable
	 */
	public Runnable newRunnable(List<?> tasks, int id);


}
