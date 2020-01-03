package com.gmail.upcovino.resteventsregistry.commons.exceptions;

public class UnauthorizedUserException extends Exception {
	private static final long serialVersionUID = -2161808073357292179L;

	public UnauthorizedUserException(String msg) {
		super(msg);
	}
}
