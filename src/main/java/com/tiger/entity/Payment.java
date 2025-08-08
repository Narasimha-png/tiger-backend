package com.tiger.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)  
	private Integer id ;
	private Long amount ;
	@CreationTimestamp
	private LocalDateTime timestamp ;
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user ;
}
