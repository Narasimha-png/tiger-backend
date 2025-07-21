package com.tiger.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;
@Data
@Entity 
public class Roasts {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer roastId ;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String roast;

	private LocalDateTime timestamp ;
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user ;
	@PrePersist
	public void setTimeStamp() {
		this.timestamp = LocalDateTime.now() ;
	}
}
