package com.tiger.service.implementation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tiger.dto.GithubStreakDTO;
import com.tiger.dto.LeetcodeProfile;
import com.tiger.dto.LeetcodeStreakDTO;
import com.tiger.dto.LeetcodeSubmissions;
import com.tiger.dto.UserDTO;
import com.tiger.dto.LeetcodeSubmissions.Submission;
import com.tiger.entity.GithubStreak;
import com.tiger.entity.LeetcodeStreak;
import com.tiger.entity.User;
import com.tiger.exception.LeetcodeException;
import com.tiger.exception.UserException;
import com.tiger.repository.LeetcodeStreakRepository;
import com.tiger.repository.UserRepository;
import com.tiger.service.LeetcodeService;

import reactor.core.publisher.Mono;

@Service
public class LeetcodeServiceImpl implements LeetcodeService {
	private WebClient leetcode;
	private UserRepository userRepo;
	private LeetcodeStreakRepository leetcodeStreakRepo;
	private ModelMapper modelMapper;

	public LeetcodeServiceImpl(@Qualifier("leetcode") WebClient leetcode, UserRepository userRepo,
			LeetcodeStreakRepository leetcodeStreak, ModelMapper modelMapper) {
		this.leetcode = leetcode;
		this.userRepo = userRepo;
		this.leetcodeStreakRepo = leetcodeStreak;
		this.modelMapper = modelMapper;
	}

	public Boolean isUserExists(String gmail) {
		return userRepo.findByGmail(gmail).isPresent();
	}

	@Override
	public Mono<LeetcodeProfile> getUserProfile(String userName) throws LeetcodeException {
		return leetcode.get().uri("/" + userName + "/").retrieve().bodyToMono(LeetcodeProfile.class);
	}

	@Override
	public Mono<LeetcodeProfile> getTotalUserProfile(String userName) throws LeetcodeException {
		return leetcode.get().uri("/userProfile/" + userName).retrieve().bodyToMono(LeetcodeProfile.class);
	}

	@Override
	public List<Submission> getTodaySubmission(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail).get();

		ZoneId zone = ZoneId.of("Asia/Kolkata");

		LeetcodeSubmissions response = leetcode.get().uri("/" + user.getLeetcodeProfile() + "/acSubmission?limit=20")
				.retrieve().bodyToMono(LeetcodeSubmissions.class).block();

		List<Submission> submissions = response != null ? response.getSubmission() : null;

		if (submissions == null) {
			return Collections.emptyList();
		}

		return submissions.stream().filter(sub -> {
			long epochSec = Long.parseLong(sub.getTimestamp());
			LocalDate commitDate = Instant.ofEpochSecond(epochSec).atZone(zone).toLocalDate();
			return commitDate.isEqual(LocalDate.now(zone));
		}).toList();
	}

	@Override
	public void updateTodaysSubmissions(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail)
				.orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND.value()));

		int todaySubmissions = this.getTodaySubmission(gmail).size();
		LeetcodeStreak leetcodeStreak = new LeetcodeStreak();
		leetcodeStreak.setUser(user);
		leetcodeStreak.setTodaySubmissions(todaySubmissions);

		leetcodeStreakRepo.save(leetcodeStreak);
	}

	@Override
	public List<LeetcodeStreakDTO> getStreaks(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail)
				.orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND.value()));

		int todaySubmissions = this.getTodaySubmission(gmail).size();
		LeetcodeStreakDTO todaySubmissionsDTO = new LeetcodeStreakDTO();
		todaySubmissionsDTO.setTodaySubmissions(todaySubmissions);
		todaySubmissionsDTO.setUser(modelMapper.map(user, UserDTO.class));
		todaySubmissionsDTO.setRecordedAt(LocalDateTime.now());

		List<LeetcodeStreak> streaks = leetcodeStreakRepo.findByUserGmail(gmail);
		List<LeetcodeStreakDTO> finalStreak = streaks.stream().map(streak -> {
			LeetcodeStreakDTO tempStreak = modelMapper.map(streak, LeetcodeStreakDTO.class);
			tempStreak.setUser(modelMapper.map(streak.getUser(), UserDTO.class));
			return tempStreak;
		}).collect(Collectors.toList());
		finalStreak.add(todaySubmissionsDTO);
		return finalStreak;
	}

}
