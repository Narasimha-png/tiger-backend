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
@Entity
@Data
public class Postings {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer postingsId ;
	private String url  ;
	private LocalDateTime timestamp ;
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user ;
	
	@PrePersist
	public void setPostingTime() {
		this.timestamp = LocalDateTime.now() ;
	}
}
