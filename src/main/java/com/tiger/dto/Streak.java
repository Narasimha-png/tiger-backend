package com.tiger.dto;

import java.time.LocalDate;

import lombok.Data;
@Data
public class Streak {
	private LocalDate date ;
	private StreakStatus status ;
	
	public enum StreakStatus{
		COMPLETED, PARTIAL , NOT_STARTED
	}
	
}
