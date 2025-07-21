package com.tiger.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDTO {
	private String name ;
	private String gmail ;
	private String password ;
	private String githubProfile ;
	private Integer targetCommits ;
	private String leetcodeProfile ;
	private Integer targetSubmissions ;
	private LocalDateTime joinedAt ;
	private Boolean linkedinPostService ;
}
