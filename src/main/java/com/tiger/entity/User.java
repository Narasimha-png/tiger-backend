package com.tiger.entity;




import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import lombok.Data;


@Entity
@Data
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId ;
	private String name ;
	private String gmail ;
	private String password ;
	private String githubProfile ;
	private Integer targetCommits ;
	private String leetcodeProfile ;
	private Integer targetSubmissions ;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String code ;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String accessToken ;
	private LocalDateTime tokenExpiry ;
	private LocalDateTime joinedAt ;
	private Boolean linkedinPostService ;
	private String linkedinUserId ;
	@PrePersist
	public void getJoinedAtTime() {
		this.joinedAt = LocalDateTime.now() ;
		this.linkedinPostService = true ;
	}

	
}
