package com.tiger.exception;

import lombok.Data;

@Data
public class LeetcodeException extends Exception {
	private Integer statusCode ;
	public LeetcodeException(String message , Integer statusCode) {
		super(message) ;
		this.statusCode = statusCode ;
	}
}
