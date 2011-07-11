package org.gnf.pbb.exceptions;

@SuppressWarnings("serial")
public class NoBotsException extends Exception {
	String flagLocation;
	public NoBotsException(int startIndex, int endIndex) {
		super();
		flagLocation = "Nobots flag found in text at position " + startIndex + " -- " + endIndex;
	}
	
	public NoBotsException() {
		flagLocation = "{{nobots}} found in text, location not specified.";
	}

	public String getFlagLocation() {
		return flagLocation;
	}
}
