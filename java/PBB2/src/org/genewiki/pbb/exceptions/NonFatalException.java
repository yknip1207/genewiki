package org.genewiki.pbb.exceptions;

public class NonFatalException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3287934526138668437L;
	private final String message;

	public NonFatalException(String string) {
		super();
		this.message = string;
	}
	
	public String getMessage() {
		return this.message;
	}

	
	
}
