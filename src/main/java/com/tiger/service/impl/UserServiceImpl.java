package com.tiger.service.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tiger.config.UrlConfig;
import com.tiger.dto.ActivityStatus;
import com.tiger.dto.LinkedInUserInfoDTO;
import com.tiger.dto.NotificationDTO;
import com.tiger.dto.RoastResponse;
import com.tiger.dto.Streak;
import com.tiger.dto.StreakDTO;
import com.tiger.dto.UserActivityDTO;
import com.tiger.dto.UserDTO;
import com.tiger.entity.Notification;
import com.tiger.entity.Roasts;
import com.tiger.entity.User;
import com.tiger.entity.UserActivity;
import com.tiger.exception.GroqException;
import com.tiger.exception.UserException;
import com.tiger.repository.NotificationRepository;
import com.tiger.repository.PostsRespository;
import com.tiger.repository.RoastsRepository;
import com.tiger.repository.UserActivityRepository;
import com.tiger.repository.UserRepository;
import com.tiger.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {
	private UserRepository userRepo;
	private ModelMapper mapper;
	private ObjectMapper objectMapper;
	private GroqServiceImpl groq;
	private RoastsRepository roastsRepo;
	private LeetcodeServiceImpl leetcodeService;
	private GithubServiceImpl githubService;
	private UrlConfig urlConfig;
	private UserActivityRepository userActivityRepo;
	private WebClient linkedinClient;
	private PostsRespository postings;
	private NotificationRepository  notificationRepo ;
	
	private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class) ;
	
	public UserServiceImpl(UserRepository user, ModelMapper mapper, GroqServiceImpl groq, ObjectMapper objectMapper,
			RoastsRepository roastsRepo, UrlConfig urlConfig, UserActivityRepository userActivityRepo,
			@Qualifier("linkedin") WebClient linkedinClient, LeetcodeServiceImpl leetcodeService,
			GithubServiceImpl githubService, PostsRespository postings ,NotificationRepository  notificationRepo ) {
		this.userRepo = user;
		this.mapper = mapper;
		this.groq = groq;
		this.objectMapper = objectMapper;
		this.roastsRepo = roastsRepo;
		this.urlConfig = urlConfig;
		this.userActivityRepo = userActivityRepo;
		this.linkedinClient = linkedinClient;
		this.githubService = githubService;
		this.leetcodeService = leetcodeService;
		this.postings = postings;
		this.notificationRepo = notificationRepo ;
	}

	@Override
	public Boolean isUserExists(String gmail) {
		return userRepo.findByGmail(gmail).isPresent();
	}

	public String storeAcessKey(String code) {

		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("code", code);
		form.add("redirect_uri", urlConfig.getLinkedinCallback());
		form.add("client_id", urlConfig.getLinkedinClientId());
		form.add("client_secret", urlConfig.getLinkedinClientSecret());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

		ResponseEntity<String> response = restTemplate.postForEntity("https://www.linkedin.com/oauth/v2/accessToken",
				request, String.class);

		String accessToken = null;
		Pattern pattern = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(response.getBody());

		if (matcher.find()) {
			accessToken = matcher.group(1);
		} else {
			LOGGER.error("Unable to find access token") ;
		}
		
	

		WebClient webClient = WebClient.builder().baseUrl("https://api.linkedin.com")
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).build();

		LinkedInUserInfoDTO userInfo = webClient.get().uri("/v2/userinfo").retrieve()
				.bodyToMono(LinkedInUserInfoDTO.class).block();

		String email = userInfo.getEmail();

		Long expiryAt = linkedinClient.post().uri("/oauth/v2/introspectToken")
				.body(BodyInserters.fromFormData("client_id", urlConfig.getLinkedinClientId())
						.with("client_secret", urlConfig.getLinkedinClientSecret()).with("token", accessToken))
				.retrieve().bodyToMono(Map.class)
				.map(responseExpiry -> ((Number) responseExpiry.get("expires_at")).longValue()).block();
		Long epochMillis = expiryAt * 1000;
		if(userRepo.findByGmail(email).isPresent()) {
			User existUser =  userRepo.findByGmail(email).get() ;
			if(existUser.getTokenExpiry().isAfter(LocalDateTime.now()))
				return existUser.getCode() ;
		}
		LocalDateTime expiryDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
		Optional<User> existingUser = userRepo.findByGmail(email);
		if (existingUser.isPresent()) {
			User user = existingUser.get();
			user.setAccessToken(accessToken);
			user.setCode(code);
			user.setTokenExpiry(expiryDate);
			user.setLinkedinUserId(userInfo.getSub());
			userRepo.save(user);

		} else {
			User user = new User();
			user.setGmail(email);
			user.setName(userInfo.getName());
			user.setAccessToken(accessToken);
			user.setCode(code);
			user.setLinkedinUserId(userInfo.getSub());
			user.setTokenExpiry(expiryDate);
			userRepo.save(user);
		}
		try {
			this.addUserActivity(ActivityStatus.LOGIN, email);
		} catch (UserException e) {

		}
		return code ;

	}

	@Override
	public Integer register(UserDTO userDto) throws UserException {
		User user = mapper.map(userDto, User.class);
		userRepo.save(user);
		Thread roastThread = new Thread(() -> this.fetchRoastings(userDto.getGmail(), userDto.getLeetcodeProfile()));
		roastThread.start();
		return user.getUserId();
	}

	@Override
	public Boolean loginUser(UserDTO user) throws UserException {
		if (!isUserExists(user.getGmail()))
			throw new UserException("User Not found.", HttpStatus.NOT_FOUND.value());
		return userRepo.findByGmail(user.getGmail()).get().getPassword().equals(user.getPassword());
	}

	public void addUserActivity(ActivityStatus status, String gmail) throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("No user found", HttpStatus.NOT_FOUND.value());
		UserActivity activity = new UserActivity();
		activity.setStatus(status);
		activity.setUser(userRepo.findByGmail(gmail).get());
		;
		userActivityRepo.save(activity);

	}

	void fetchRoastings(String gmail, String userName) {
		List<RoastResponse> roastings = null;
		String roastStr = null;
		try {
			groq.getProfileRoastings(userName, gmail, 0);

		} catch (GroqException ex) {
		} catch (Exception ex) {
		}

	}

	@Override
	public List<RoastResponse> getRoastings(String gmail) throws UserException {
		try {
			return roastsRepo.findByUserGmail(gmail).stream().flatMap(roast -> {
				try {
					List<RoastResponse> parsed = objectMapper.readValue(roast.getRoast(),
							new TypeReference<List<RoastResponse>>() {
							});
					return parsed.stream().peek(r -> r.setTimestamp(roast.getTimestamp().toString()));
				} catch (IOException e) {
					throw new RuntimeException("JSON parse error", e);
				}
			}).sorted(
					Comparator.comparing(RoastResponse::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))

					.toList();
		} catch (Exception ex) {
			throw new UserException("Error while parsing values.", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Override
	public List<Streak> getStreak(String gmail) throws UserException {
		
		return null;
	}

	@Override
	public void updateLeetCodeProfile(String gmail, String updatedUserName, Integer targetSubmissions)
			throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		User user = userRepo.findByGmail(gmail).get();
		user.setLeetcodeProfile(updatedUserName);
		user.setTargetSubmissions(targetSubmissions);
		userRepo.save(user);

	}

	@Override
	public void updateGitHubProfile(String gmail, String updatedUserName, Integer targetCommits) throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		User user = userRepo.findByGmail(gmail).get();
		user.setGithubProfile(updatedUserName);
		user.setTargetCommits(targetCommits);
		userRepo.save(user);
	}

	@Override
	public UserDTO getProfile(String gmail) throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());

		return mapper.map(userRepo.findByGmail(gmail).get(), UserDTO.class);

	}

	@Override
	public void logout(String email) throws UserException {
		if (!isUserExists(email))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		
		try {
			this.addUserActivity(ActivityStatus.LOGOUT, email);
		} catch (UserException e) {

		}
	}

	public List<UserActivityDTO> getUserActivity(String email) {
		List<UserActivity> activities = userActivityRepo.findByUserGmail(email);
		return activities.stream().map(activity -> mapper.map(activity, UserActivityDTO.class))
				.sorted(Comparator.comparing(UserActivityDTO::getTimestamp,
						Comparator.nullsLast(Comparator.reverseOrder())))

				.toList();
	}

	@Override
	public StreakDTO getUserStreak(String email) throws UserException {
		if (!isUserExists(email))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		User user = userRepo.findByGmail(email).get();

		StreakDTO streak = new StreakDTO();
		streak.setGitStreak(githubService.getStreaks(email));
		streak.setLeetcodeStreak(leetcodeService.getStreaks(email));
		streak.setTargetCommits(user.getTargetCommits());
		streak.setTargetSubmssions(user.getTargetSubmissions());
		streak.setJoinedAt(user.getJoinedAt());
		streak.setTotalPosts(postings.findByUserGmail(email).size());
		return streak;
	}

	@Transactional
	@Override
	public void toggleLinkedinPostService(String email, Boolean status) throws UserException {
		if (!isUserExists(email))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		User user = userRepo.findByGmail(email).get();

		user.setLinkedinPostService(status);
		userRepo.save(user);
	}

	@Override
	public void addNotifyDevice(String gmail, String fcmToken) throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		User user = userRepo.findByGmail(gmail).get();
		if(notificationRepo.findByFcmToken(fcmToken).isPresent())
			return ;
		Notification notification = new Notification() ;
		notification.setUser(user);
		notification.setFcmToken(fcmToken);
		notificationRepo.save(notification) ;
			
	}

	@Override
	public List<NotificationDTO> getNotifyDevices(String gmail) throws UserException {
		if (!isUserExists(gmail))
			throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());
		
		List<Notification> devices = notificationRepo.findByUserGmail(gmail) ;
		return devices.stream().map(device -> mapper.map(device, NotificationDTO.class)).toList() ;
	}

}
