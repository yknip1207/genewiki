package org.gnf.pbb.exceptions;

public class MalformedWikitextException extends Exception {
	private String message;

	public MalformedWikitextException(String message) {
		super();
		this.message = message;
	}
	
	public String getMessage() { return this.message; }
	
	
}
