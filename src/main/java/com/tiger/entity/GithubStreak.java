package com.tiger.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;
@Data
@Entity
public class GithubStreak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gitId;
    private Integer todayCommits ;
    private LocalDateTime recordedAt ;
    
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user ;
    
    @PrePersist
    public void setRecordedTime() {
    	this.recordedAt = LocalDateTime.now() ;
    }
    
}

