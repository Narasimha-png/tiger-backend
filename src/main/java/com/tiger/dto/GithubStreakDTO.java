package com.tiger.dto;

import java.time.LocalDateTime;

import com.tiger.entity.User;

import lombok.Data;

@Data
public class GithubStreakDTO {
	 private Integer todayCommits ;
	    private LocalDateTime recordedAt ;
	    private UserDTO user ;
}
