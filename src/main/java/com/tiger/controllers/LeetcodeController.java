package com.tiger.controllers;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiger.dto.LeetcodeProfile;
import com.tiger.dto.LeetcodeSubmissions;
import com.tiger.dto.LeetcodeSubmissions.Submission;
import com.tiger.exception.GroqException;
import com.tiger.exception.LeetcodeException;
import com.tiger.exception.UserException;
import com.tiger.service.AuthService;
import com.tiger.service.impl.GroqServiceImpl;
import com.tiger.service.impl.LeetcodeServiceImpl;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/tiger/leetcode")
public class LeetcodeController {
	private LeetcodeServiceImpl leetcode ;
	private AuthService auth ;
	private GroqServiceImpl groq ;
	public LeetcodeController(LeetcodeServiceImpl leetcode, AuthService auth, GroqServiceImpl groq ) {
		this.leetcode = leetcode ;
		this.groq = groq ;
		this.auth = auth ;
	}
	@GetMapping("profile/{userName}")
	public Mono<LeetcodeProfile> getUserProfile(@PathVariable String userName) throws LeetcodeException{
		return leetcode.getUserProfile(userName) ;
	}
	@GetMapping("totalprofile/{userName}")
	public Mono<LeetcodeProfile> getTotalUserProfile(@PathVariable String userName) throws LeetcodeException{
		return leetcode.getTotalUserProfile(userName) ;
	}
	@GetMapping("todaystreak")
	public ResponseEntity<List<Submission>> getTodayStreak() throws UserException{
		leetcode.updateTodaysSubmissions(auth.getEmail());
		return new ResponseEntity<List<Submission>>(leetcode.getTodaySubmission(auth.getEmail()), HttpStatus.OK ) ;
	}
	@GetMapping("morning")
	public ResponseEntity<String> morningNotificationContent() throws GroqException{
		return new ResponseEntity<String>(groq.morningNotification(), HttpStatus.OK) ;
	}
	@GetMapping("motivation")
	public ResponseEntity<String> motivationalNotificationContent() throws GroqException{
		return new ResponseEntity<String>(groq.workUpdateNotification(auth.getEmail()), HttpStatus.OK) ;
	}
	
	
}
