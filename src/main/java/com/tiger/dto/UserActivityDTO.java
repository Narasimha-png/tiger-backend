package com.tiger.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class UserActivityDTO {
	private Integer activityId ;
	private ActivityStatus status ;
	private LocalDateTime timestamp ;
}
