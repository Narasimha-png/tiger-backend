package com.tiger.service;
import java.util.List;

import com.tiger.dto.GitStreakDTO;
import com.tiger.dto.LeetcodeProfile;
import com.tiger.dto.LeetcodeStreakDTO;
import com.tiger.dto.LeetcodeSubmissions;
import com.tiger.dto.LeetcodeSubmissions.Submission;
import com.tiger.exception.LeetcodeException;
import com.tiger.exception.UserException;

import reactor.core.publisher.Mono;

public interface LeetcodeService {
	 Mono<LeetcodeProfile>  getUserProfile(String userName) throws LeetcodeException ;
	 Mono<LeetcodeProfile>  getTotalUserProfile(String userName) throws LeetcodeException ;
	 List<Submission> getTodaySubmission(String gmail) throws UserException ;
	 void updateTodaysSubmissions(String gmail) throws UserException ;
	 List<LeetcodeStreakDTO> getStreaks(String gmail) throws UserException ;
}
