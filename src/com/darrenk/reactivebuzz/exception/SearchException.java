package com.darrenk.reactivebuzz.exception;

/**
 * Runtime Exception for ReactiveBuzz search exceptions
 */
public class SearchException extends RuntimeException {

	private static final long serialVersionUID = 175341569883724254L;

	public SearchException(String s) {
		super(s);
	}

	public SearchException(Throwable t) {
		super(t);
	}

	public SearchException(String s, Throwable t) {
		super(s, t);
	}

}
