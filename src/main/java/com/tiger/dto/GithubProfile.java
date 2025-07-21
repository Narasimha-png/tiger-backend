package com.tiger.dto;

import lombok.Data;

@Data
public class GithubProfile {
	private String login ;
	private String avatar_url ;
	private String bio ;
	private String public_repos ;
	private String name ;
	private Integer followers ;
	private Integer following ;
	
}
