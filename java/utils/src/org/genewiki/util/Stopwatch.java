package org.genewiki.util;

import java.util.Calendar;

/**
 * It's exactly what it sounds like.
 * Make one, call start(), do something, call stop(), get the time
 * by asking for it: time(). Or call time() before stop() to get
 * elapsed time. You can reuse it if you stop() it first.
 * @author eclarke
 *
 */
public class Stopwatch {
	private Calendar cStart;
	private Calendar cStop;
	
	private boolean started;
	private boolean stopped;
	
	/**
	 * Instantiates a new stopwatch.
	 */
	public Stopwatch() {
		// Nothing needs to happen yet.
	}
	
	/**
	 * Starts the stopwatch. If already started, this has no
	 * effect. 
	 */
	public void start(){
		cStart = Calendar.getInstance();
		started = true;
		stopped = false;
	}
	
	/**
	 * Stops the stopwatch. If already stopped, this has no
	 * effect.
	 * @throws RuntimeException if stopwatch has not been started.
	 */
	public void stop() {
		if (started) {
			cStop = Calendar.getInstance();
			stopped = true;
			started = false;
		} else {
			throw new RuntimeException("Stopwatch has not been started.");
		}
	}
	
	/**
	 * Get the time elapsed from current, if running, or from start to stop, if 
	 * stopped. 
	 * @return milliseconds elapsed
	 * @throws RuntimeException if stopwatch has not been started.
	 */
	public long time() {
		if (stopped) {
			return cStop.getTimeInMillis()-cStart.getTimeInMillis();
		} else if (started) {
			return Calendar.getInstance().getTimeInMillis() - cStart.getTimeInMillis();
		} else {
			throw new RuntimeException("Stopwatch has not been started.");
		}
	}
	
}
