package org.gnf.pbb.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * ExceptionHandler monitors the exceptions that may arise during the bot's 
 * operation and ranks them in order of severity. Any process, such
 * as a parent thread, can query ExceptionHandler for any errors above a certain
 * threshold and take appropriate action (i.e. in case of a "fatal" error, shut down
 * the bot cleanly, or in a "recoverable" error, gracefully abort and move on 
 * to the next update).
 * @author eclarke
 *
 */
public enum ExceptionHandler {
	INSTANCE;
	Logger logger = Logger.getLogger(ExceptionHandler.class.getName());
	HashMap<Severity, Exception> exceptions;
	Severity severity;
	
	ExceptionHandler() {
		exceptions = new HashMap<Severity, Exception>(0);
		severity = Severity.NONE;
	}

	 
	public void pass(Exception e, Severity s) {
		try {
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (s == Severity.FATAL) {
			System.out.println("Fatal error encountered.");
			System.out.println(printExceptionStackTraces(getExceptionsOfSeverity(Severity.FATAL)));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
		}
		exceptions.put(s, e);
		setSeverity(s);
	}
	 
	
	/**
	 * Indicates an unrecoverable error and causes the whole process to quit.
	 * @param fatal exception
	 */
	public void fatal(Exception e){
		pass(e, Severity.FATAL);
	}
	 
	/**
	 * Causes the bot to abort this current update and move on to the next.
	 * @param recoverable exception 
	 */
	public void recoverable(Exception e) {
		pass(e, Severity.RECOVERABLE);
	}
	 
	public void minor(Exception e) {
		pass(e, Severity.MINOR);
	}

	 
	public Severity checkState() {
		return this.severity;
	}
	
	 
	public boolean isFine() {
		if (this.severity == Severity.NONE)
			return true;
		return false;
	}
	 
	public boolean canExecute() {
		if (this.severity.compareTo(Severity.RECOVERABLE) < 0)
			return true;
		return false;
	}
	 
	public boolean canRecover() {
		if (this.severity.compareTo(Severity.FATAL) < 0)
			return true;
		return false;
	}
	
	/**
	 * Returns a list of exceptions accumulated during runtime
	 * that corresponded to a certain severity, as set by the 
	 * method that passed the exception.
	 * @param s severity
	 * @return corresponding exceptions
	 */
	public List<Exception> getExceptionsOfSeverity(Severity s) {
		List<Exception> _exceptions = new ArrayList<Exception>(0);
		for (Severity sev : exceptions.keySet()) {
			if (sev == s) 
				_exceptions.add(exceptions.get(sev));
		}
		return _exceptions;
	}
	
	/**
	 * Don't know what to do with a list of exceptions? This method parses
	 * all of them and returns a string with their stacktraces and messages.
	 * @param exceptionList
	 * @return String with stacktraces and messages collated together
	 */
	public String printExceptionStackTraces(List<Exception> exceptionList) {
		StringBuilder sb = new StringBuilder();
		for (Exception e : exceptionList) {
			sb.append(e.getMessage() + ": \n");
			for (StackTraceElement ste : e.getStackTrace()) {
				sb.append(ste.toString()+"\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Resets any non-fatal severity state for a new bot iteration
	 */
	 
	public void reset() {
		if (this.severity != Severity.FATAL) {
			this.severity = Severity.NONE;
		}
	}
	
	/**
	 * Sets the handler's severity level if the passed Severity is
	 * higher than currently set
	 * 
	 * @param s Severity
	 */
	private void setSeverity(Severity s) {
		switch (s) {
		case FATAL: 
			this.severity = s;
			break;
		case RECOVERABLE:
			if (s.compareTo(this.severity) > 0)
				this.severity = s;
			break;
		case MINOR:
			if (s.compareTo(this.severity) > 0)
				this.severity = s;
			break;
		case NONE:
			break;
		}
			
	}

}
