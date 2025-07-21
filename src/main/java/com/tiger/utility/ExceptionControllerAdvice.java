package com.tiger.utility;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ExceptionControllerAdvice {
	@ExceptionHandler({NoSuchElementException.class})
	public ResponseEntity<ErrorInfo> sqlError(NoSuchElementException ex){
		return new ResponseEntity<ErrorInfo>(new ErrorInfo(ex.toString() , HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now()), HttpStatus.UNAUTHORIZED ) ;
		
	}
	@ExceptionHandler({HttpClientErrorException.class})
	public ResponseEntity<ErrorInfo> unAuthorised(HttpClientErrorException ex){
		return new ResponseEntity<ErrorInfo>(new ErrorInfo(ex.toString() , HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now()), HttpStatus.UNAUTHORIZED ) ;
		
	}
	@ExceptionHandler({Exception.class})
	public ResponseEntity<ErrorInfo> globalHandler(Exception exception){
		return new ResponseEntity<ErrorInfo>(new ErrorInfo(exception.toString() , HttpStatus.NOT_FOUND.value() , LocalDateTime.now()) , HttpStatus.NOT_FOUND ) ;
	}
}
