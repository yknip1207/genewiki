package org.genewiki.pbb.exceptions;

@SuppressWarnings("serial")
public class ValidationException extends Exception {
	String message;
	String status;
	public ValidationException() {
		super();
		message = "Validation error: update is not valid.";
	}
	
	public ValidationException(String status) {
		super();
		this.status = status;
	}
	
	public String getError() {
		return message;
	}
	
	public String getMessage() {
		return status;
	}

}
