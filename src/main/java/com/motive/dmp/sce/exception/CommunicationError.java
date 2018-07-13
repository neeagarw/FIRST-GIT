package com.motive.dmp.sce.exception;

public class CommunicationError extends RuntimeException {

	private static final long serialVersionUID = 2227924523937903355L;

	public CommunicationError() {
		super();
	}

	public CommunicationError(String s) {
		super(s);
	}

	public CommunicationError(String s, Throwable throwable) {
		super(s, throwable);
	}

	public CommunicationError(Throwable throwable) {
		super(throwable);
	}

}
