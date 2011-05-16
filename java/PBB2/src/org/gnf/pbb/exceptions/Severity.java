package org.gnf.pbb.exceptions;

public enum Severity {
	NONE, 			// A non-severity state
	MINOR,			// Non-fatal, can continue task, but worth noting
	RECOVERABLE,	// Skip current task (if going through independent tasks)
	FATAL,			// Cannot continue; some larger configuration is broken
}
