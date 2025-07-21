package com.tiger.service;

import java.util.List;
import java.util.Set;

import com.tiger.dto.GitStreakDTO;
import com.tiger.dto.GithubProfile;
import com.tiger.dto.GithubStreakDTO;
import com.tiger.dto.UserDTO;
import com.tiger.exception.GithubException;
import com.tiger.exception.UserException;

import reactor.core.publisher.Mono;

public interface GithubService {
	 Mono<GithubProfile>  getUserProfile(String userName) throws GithubException ;
	 Mono<GithubProfile>  getTotalUserProfile(String userName) throws GithubException ;
	 Set<String> getTotalRepositories(String userName) throws UserException ;
	 List<GitStreakDTO> getTodayCommits(String gmail) throws UserException ;
	 void updateTodaysCommits(String gmail) throws UserException ;
	 List<GithubStreakDTO> getStreaks(String gmail) throws UserException ;
 }
