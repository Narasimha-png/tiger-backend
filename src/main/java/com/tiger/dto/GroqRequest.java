package com.tiger.dto;

import java.util.List;

import lombok.Data;

@Data
public class GroqRequest {
	private String model ;
	private List<Message> messages ;
	public GroqRequest() {
		
	}
	public GroqRequest(String model, List<Message> messages) {
		super();
		this.model = model;
		this.messages = messages;
	}

	@Data
	public static class Message{
		private String role ;
		private Object content ;
		public Message() {
		}
		public Message(String role, Object content) {
			super();
			this.role = role;
			this.content = content;
		}
	}
}
