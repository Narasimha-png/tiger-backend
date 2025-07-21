package com.tiger.dto;

import lombok.Data;

@Data
public class MessageResponse {
	private Integer status ;

	private String message ;
	public MessageResponse(String message) {
		this.message = message;
	}
	public MessageResponse(String message,Integer status ) {
		this.status = status;
		this.message = message;
	}

	
	
}
