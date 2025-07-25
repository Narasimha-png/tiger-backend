package com.tiger.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiger.dto.GitStreakDTO;
import com.tiger.dto.GithubProfile;
import com.tiger.dto.MessageResponse;
import com.tiger.dto.UserDTO;
import com.tiger.exception.GithubException;
import com.tiger.exception.UserException;
import com.tiger.service.AuthService;
import com.tiger.service.impl.GithubServiceImpl;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/tiger/github")
public class GithubController {
	private GithubServiceImpl githubService ;
	private AuthService auth ;
	public GithubController(GithubServiceImpl githubService, AuthService auth) {
		this.githubService = githubService ;
		this.auth = auth ;
		
	}
	@GetMapping("profile/{userName}")
	public  Mono<GithubProfile> getGithubProfile(@PathVariable String userName) throws GithubException{
		return githubService.getUserProfile(userName) ;
	}
	@GetMapping("todaystreak")
	public ResponseEntity<List<GitStreakDTO>> getTodayStreak() throws UserException{
		githubService.updateTodaysCommits(auth.getEmail());
		return new ResponseEntity<List<GitStreakDTO>>(githubService.getTodayCommits(auth.getEmail()), HttpStatus.OK) ;
	}
	@GetMapping("testexp")
	public ResponseEntity<List<GitStreakDTO>> testException() throws UserException{
		throw new UserException("Error user", HttpStatus.ALREADY_REPORTED.value()) ;
		
	}
}
