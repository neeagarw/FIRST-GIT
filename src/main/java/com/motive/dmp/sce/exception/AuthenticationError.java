package com.motive.dmp.sce.exception;

public class AuthenticationError extends CommunicationError {

	private static final long serialVersionUID = 4473768364278545501L;

	public AuthenticationError() {
		super();
	}

	public AuthenticationError(String s) {
		super(s);
	}

	public AuthenticationError(String s, Throwable throwable) {
		super(s, throwable);
	}

	public AuthenticationError(Throwable throwable) {
		super(throwable);
	}

}
