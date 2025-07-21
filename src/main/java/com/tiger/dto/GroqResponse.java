package com.tiger.dto;

import java.util.List;

import lombok.Data;
@Data
public class GroqResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    @Data
    public static class Choice {
        private int index;
        private Message message;

    }
    @Data
    public static class Message {
        private String role;
        private String content; 
    }
}
