package com.tiger.dto;

import java.util.List;

import lombok.Data;
@Data
public class LeetcodeSubmissions {
	private List<Submission> submission; 
	@Data
	public static class Submission{
		private String title ;
		private String titleSlug ;
		private String timestamp ;
		private String lang ;
	}
}
