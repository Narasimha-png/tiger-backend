package com.tiger.exception;

import lombok.Data;

@Data
public class GroqException extends Exception {
	private Integer statusCode ;
	public GroqException(String message , Integer statusCode) {
		super(message) ;
		this.statusCode = statusCode ;
	}
}
