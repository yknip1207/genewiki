package org.genewiki.pbb.exceptions;

@SuppressWarnings("serial")
public class MalformedWikitextException extends Exception {
	private String message;

	public MalformedWikitextException(String message) {
		super();
		this.message = message;
	}
	
	public String getMessage() { return this.message; }
	
	
}
