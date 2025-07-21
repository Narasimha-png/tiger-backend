package com.tiger.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class LeetcodeStreakDTO {
	private Integer leetcodeId;
	private Integer todaySubmissions;
	private LocalDateTime recordedAt;
	private UserDTO user;

	
}
