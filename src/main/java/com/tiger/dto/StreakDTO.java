package com.tiger.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class StreakDTO {
	private Integer targetCommits ;
	private Integer targetSubmssions ;
	private List<GithubStreakDTO> gitStreak ;
	private List<LeetcodeStreakDTO> leetcodeStreak ;
	private LocalDateTime joinedAt ;
	private Integer totalPosts ;
}
