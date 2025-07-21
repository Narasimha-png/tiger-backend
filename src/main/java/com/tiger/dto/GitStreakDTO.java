package com.tiger.dto;

import lombok.Data;

@Data
public class GitStreakDTO {
	private Integer target ;
    private Commit commit;
   
    @Data
    public static class Commit {
        private String message;
        private Author author;
    }

    @Data
    public static class Author {
        private String name;
        private String email;
        private String date; 
    }
}
