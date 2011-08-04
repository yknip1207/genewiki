package org.gnf.pbb.exceptions;

public class FatalException extends RuntimeException {
	
	private static final long serialVersionUID = 4872797517332586828L;
	private final String message;

	public FatalException(String string) {
		super();
		this.message = string;
	}
	
	public String getMessage() {
		return this.message;
	}

	
	
}
