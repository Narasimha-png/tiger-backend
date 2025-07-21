package com.tiger.utility;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorInfo {
	private String message ;
	private Integer statusCode ;
	private LocalDateTime timestamp ;
}
