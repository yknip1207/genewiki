package org.gnf.pbb.exceptions;

public enum Severity {

	FATAL,			// Cannot continue; some larger configuration is broken
	RECOVERABLE,	// Skip current task (if going through independent tasks)
	MINOR,			// Non-fatal, can continue task, but worth noting
	NONE; 
	
}
