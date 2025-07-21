package com.tiger.exception;

import lombok.Data;

@Data
public class UserException extends Exception {
	private Integer statusCode ;
	public UserException(String message , Integer statusCode) {
		super(message) ;
		this.statusCode = statusCode ;
	}
}
