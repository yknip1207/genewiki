package org.gnf.pbb.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public enum PbbExceptionHandler implements ExceptionHandler {
	INSTANCE;
	Logger logger = Logger.getLogger(PbbExceptionHandler.class.getName());
	HashMap<Severity, Exception> exceptions;
	Severity severity;
	
	PbbExceptionHandler() {
		exceptions = new HashMap<Severity, Exception>(0);
		severity = Severity.NONE;
	}

	@Override
	public void pass(Exception e, Severity s) {
		if (s == Severity.FATAL) {
			System.out.println("Fatal error encountered.");
			System.out.println(printExceptionStackTraces(getExceptionsOfSeverity(Severity.FATAL)));
		}
		setSeverity(s);
		exceptions.put(s, e);
	}
	@Override
	public void fatal(Exception e){
		pass(e, Severity.FATAL);
	}
	@Override
	public void recoverable(Exception e) {
		pass(e, Severity.RECOVERABLE);
	}
	@Override
	public void minor(Exception e) {
		pass(e, Severity.MINOR);
	}

	@Override
	public Severity checkState() {
		return this.severity;
	}
	
	@Override
	public boolean canUpdate() {
		if (this.severity == Severity.NONE)
			return true;
		return false;
	}
	@Override
	public boolean canExecute() {
		if (this.severity.compareTo(Severity.RECOVERABLE) < 0)
			return true;
		return false;
	}
	@Override
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
	@Override
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
