package com.tiger.dto;

import java.util.List;

import lombok.Data;

@Data
public class RoastResponse {
    private String name;
    private List<String> message;
    private String timestamp; 

}
