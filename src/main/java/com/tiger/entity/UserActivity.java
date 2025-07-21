package com.tiger.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.tiger.dto.ActivityStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Data
@Entity
public class UserActivity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer activityId ;
	@Enumerated(EnumType.STRING)
	private ActivityStatus status ;
	@CreationTimestamp
	private LocalDateTime timestamp ;
	
	@ManyToOne
	private User user ;
	

}
