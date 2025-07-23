package com.happinesea.webcrawler;

public class NotFoundContentsException extends Exception {

	public NotFoundContentsException() {
		super();
	}

	public NotFoundContentsException(String message) {
		super(message);
	}

	public NotFoundContentsException(Throwable cause) {
		super(cause);
	}

	public NotFoundContentsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundContentsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
