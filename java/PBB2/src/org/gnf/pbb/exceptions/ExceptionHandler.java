package org.gnf.pbb.exceptions;

public interface ExceptionHandler {
	
	/**
	 * Pass an exception and the corresponding severity of the 
	 * exception to the ExceptionHandler.
	 * @param e exception
	 * @param s corresponding severity
	 */
	public void pass(Exception e, Severity s);
	
	
	public void fatal(Exception e);
	public void recoverable(Exception e);
	public void minor(Exception e);
	
	
	/**
	 * Returns the highest level of severity set so far during
	 * operation; used to make decisions about bot termination
	 * or update ability.
	 * @return highest severity encountered
	 */
	public Severity checkState(); 

	public void reset();


	boolean canUpdate();


	boolean canExecute();


	boolean canRecover();
	
}
