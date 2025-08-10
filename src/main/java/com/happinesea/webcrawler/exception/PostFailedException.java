package com.happinesea.webcrawler.exception;

public class PostFailedException extends Exception {

	private static final long serialVersionUID = -2640497495750173865L;

	public PostFailedException(String message, Throwable e) {
		super(message, e);
	}
}
