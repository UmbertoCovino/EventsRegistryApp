package com.gmail.upcovino.resteventsregistry.commons;

public class InvalidUserEmailException extends Exception {
	private static final long serialVersionUID = -2161808073357292179L;

	public InvalidUserEmailException(String msg) {
		super(msg);
	}
}
