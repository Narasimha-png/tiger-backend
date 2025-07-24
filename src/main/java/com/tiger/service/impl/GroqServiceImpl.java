package com.tiger.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiger.config.GroqConfig;
import com.tiger.config.UrlConfig;
import com.tiger.dto.GitStreakDTO;
import com.tiger.dto.GithubStreakDTO;
import com.tiger.dto.GroqRequest;
import com.tiger.dto.GroqRequest.Message;
import com.tiger.dto.GroqResponse;
import com.tiger.dto.LeetcodeProfile;
import com.tiger.dto.LeetcodeStreakDTO;
import com.tiger.dto.LeetcodeSubmissions.Submission;
import com.tiger.dto.RoastResponse;
import com.tiger.entity.Roasts;
import com.tiger.entity.User;
import com.tiger.exception.GroqException;
import com.tiger.exception.LeetcodeException;
import com.tiger.exception.UserException;
import com.tiger.repository.RoastsRepository;
import com.tiger.repository.UserRepository;
import com.tiger.service.GroqService;

import reactor.core.publisher.Mono;

@Service
public class GroqServiceImpl implements GroqService {
	private WebClient groq;
	private LeetcodeServiceImpl leetcode;
	private GithubServiceImpl github;
	private GroqConfig groqConfig;
	private ObjectMapper mapper;
	private UserRepository userRepo;
	private RoastsRepository roastRepo;
	private UrlConfig urlConfig;

	public GroqServiceImpl(@Qualifier("groq") WebClient groq, LeetcodeServiceImpl leetcode, GithubServiceImpl github,
			ObjectMapper mapper, UserRepository userRepo, RoastsRepository roastRepo, UrlConfig urlConfig,
			GroqConfig groqConfig) {
		this.groq = groq;
		this.leetcode = leetcode;
		this.github = github;
		this.mapper = mapper;
		this.userRepo = userRepo;
		this.roastRepo = roastRepo;
		this.urlConfig = urlConfig;
		this.groqConfig = groqConfig;
	}

//	 @Scheduled(cron = "10 * * * * *")
	public void storeAutoRoasts() {
		List<User> users = (List<User>) userRepo.findAll();
		users.stream().forEach(user -> {
			try {
				this.putDailyRoastings(user.getLeetcodeProfile(), user.getGmail(), 0);
				Thread.sleep(5000);
			} catch (GroqException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		});
	}

	@Override
	@Transactional

	public void getProfileRoastings(String userName, String gmail, int attempts) throws GroqException {
		if (attempts > urlConfig.getGroqRetryCount())
			throw new GroqException("Profile parsing failed max limit reached",
					HttpStatus.INTERNAL_SERVER_ERROR.value());

		if (gmail == null || gmail.isBlank()) {
			throw new GroqException("Gmail is null or blank", HttpStatus.BAD_REQUEST.value());
		}

		roastRepo.deleteAllByUserGmail(gmail);

		LeetcodeProfile profile;
		try {
			profile = leetcode.getTotalUserProfile(userName).block(); // Blocking here is OK if leetcode is not reactive
		} catch (LeetcodeException e) {
			throw new GroqException("Error fetching LeetCode profile.", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		String prompt;
		try {
			prompt = groqConfig.getProfileRoastPrompt() + mapper.writeValueAsString(profile);
		} catch (Exception e) {
			try {
				prompt = "Partial profile available: " + mapper.writeValueAsString(profile).substring(0, 1000);
			} catch (JsonProcessingException ex) {
				throw new GroqException("Profile parsing failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}

		Message message = new Message("user", prompt);
		GroqRequest request = new GroqRequest(groqConfig.getModel(), List.of(message));

		GroqResponse response = groq.post().uri("/openai/v1/chat/completions").bodyValue(request).retrieve()
				.bodyToMono(GroqResponse.class).block();

		System.out.println(response) ;
		String rawContent = response.getChoices().get(0).getMessage().getContent();

		List<RoastResponse> roastList = new ArrayList<>();
		Pattern pattern = Pattern.compile("```(?:json)?\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(rawContent);

		while (matcher.find()) {
			try {
				String json = matcher.group(1);
				RoastResponse roast = mapper.readValue(json, RoastResponse.class);
				roastList.add(roast);
				
			} catch (Exception e) {
				try {
					Thread.sleep(groqConfig.getRetryLatency());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				getProfileRoastings(userName, gmail, attempts + 1);
				e.printStackTrace();
				return;

			}
		}

		if(roastList.size() == 0 )
			getProfileRoastings(userName, gmail, attempts + 1);

		Roasts roast = new Roasts();
		roast.setUser(userRepo.findByGmail(gmail).orElseThrow(
				() -> new GroqException("User not found for gmail: " + gmail, HttpStatus.NOT_FOUND.value())));

		try {
			roast.setRoast(mapper.writeValueAsString(roastList));
			roastRepo.save(roast);
		} catch (JsonProcessingException e) {
			throw new GroqException("Error saving roast", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Override
	public void putDailyRoastings(String userName, String gmail, int attempts) throws GroqException {
		if (attempts > urlConfig.getGroqRetryCount())
			throw new GroqException(
					"Maximum threashold of " + urlConfig.getGroqRetryCount() + " reached for this request.",
					HttpStatus.INTERNAL_SERVER_ERROR.value());

		LeetcodeProfile profile;
		try {
			profile = leetcode.getTotalUserProfile(userName).block();
		} catch (LeetcodeException e) {
			throw new GroqException("Error fetching LeetCode profile.", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		String prompt;
		try {

			prompt = groqConfig.getDailyRoastPrompt() + mapper.writeValueAsString(profile);
		} catch (Exception e) {
			try {
				prompt = "Partial profile available: " + mapper.writeValueAsString(profile).substring(0, 1000);
			} catch (JsonProcessingException ex) {
				throw new GroqException("Profile parsing failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}

		Message message = new Message();
		message.setRole("user");
		message.setContent(prompt);

		GroqRequest request = new GroqRequest();
		request.setModel(groqConfig.getModel());
		request.setMessages(List.of(message));
		GroqResponse response = groq.post().uri("/openai/v1/chat/completions").bodyValue(request).retrieve()
				.bodyToMono(GroqResponse.class).block();

		String rawContent = response.getChoices().get(0).getMessage().getContent();

		List<RoastResponse> roastList = new ArrayList<>();
		Pattern pattern = Pattern.compile("```(?:json)?\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(rawContent);

		while (matcher.find()) {
			try {
				String json = matcher.group(1);
				RoastResponse roast = mapper.readValue(json, RoastResponse.class);
				roastList.add(roast);
			} catch (Exception e) {
				try {
					Thread.sleep(groqConfig.getRetryLatency());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				getProfileRoastings(userName, gmail, attempts + 1);
				e.printStackTrace();
				return;

			}
		}

		Roasts roast = new Roasts();
		roast.setUser(userRepo.findByGmail(gmail).orElseThrow(
				() -> new GroqException("User not found for gmail: " + gmail, HttpStatus.NOT_FOUND.value())));

		try {
			roast.setRoast(mapper.writeValueAsString(roastList));
			roastRepo.save(roast);
		} catch (JsonProcessingException e) {
			throw new GroqException("Error saving roast", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Override
	public String genereateDailyLinkedinPost(String gmail) throws GroqException {
	    User user = userRepo.findByGmail(gmail)
	            .orElseThrow(() -> new GroqException("User not found", HttpStatus.NOT_FOUND.value()));

	    List<GitStreakDTO> githubStreak;
	    List<Submission> leetcodeStreak;

	    try {
	        leetcodeStreak = leetcode.getTodaySubmission(gmail);
	        githubStreak = github.getTodayCommits(gmail);
	        if(leetcodeStreak.size() < user.getTargetSubmissions() || githubStreak.size() < user.getTargetCommits())
	        	return null ;
	    } catch (UserException e) {
	        throw new GroqException("Failed to retrieve streak data", HttpStatus.INTERNAL_SERVER_ERROR.value());
	    }
	    String prompt =null ;
		try {
			prompt = """
				    You are an assistant that helps developers write daily LinkedIn posts.

				    Write a professional and inspiring post based on my coding activity for today.

				    Format:
				    - A strong, engaging title
				    - A short paragraph summarizing today's LeetCode and GitHub progress
				   - Maintain a motivational, developer-friendly tone
				    - Do not include hashtags

				    LeetCode Submissions:
				    %s

				    GitHub Commits:
				    %s
				""".formatted(mapper.writeValueAsString(leetcodeStreak), mapper.writeValueAsString(githubStreak));
		} catch (JsonProcessingException e) {
		
			e.printStackTrace();
		}

		if(prompt == null )
			return null ;
	    Message message = new Message("user", prompt);

	    GroqRequest request = new GroqRequest();
	    request.setModel(groqConfig.getModel());
	    request.setMessages(List.of(message));

	    GroqResponse response = groq.post()
	            .uri("/openai/v1/chat/completions")
	            .bodyValue(request)
	            .retrieve()
	            .bodyToMono(GroqResponse.class)
	            .block();

	    return response != null && response.getChoices() != null && !response.getChoices().isEmpty()
	            ? response.getChoices().get(0).getMessage().getContent()
	            : null ;
	}

	@Override
	public String morningNotification() throws GroqException {
		System.out.println(groqConfig.getNotifyMorning()) ;
	    Message message = new Message("user", groqConfig.getNotifyMorning());

	    GroqRequest request = new GroqRequest();
	    request.setModel(groqConfig.getModel());
	    request.setMessages(List.of(message));

	    GroqResponse response = groq.post()
	            .uri("/openai/v1/chat/completions")
	            .bodyValue(request)
	            .retrieve()
	            .bodyToMono(GroqResponse.class)
	            .block();
	    return response != null && response.getChoices() != null && !response.getChoices().isEmpty()
	            ? response.getChoices().get(0).getMessage().getContent()
	            : null ; 
	}

	@Override
	public String workUpdateNotification(String gmail) throws GroqException {
		String prompt = null ;
		try {
			 prompt = groqConfig.getNotifyWork() + mapper.writeValueAsString(leetcode.getTodaySubmission(gmail)) ;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (UserException e) {
			
			e.printStackTrace();
		}
		
		if(prompt == null )
			return null ;
		
		Message message = new Message("user", prompt );

		    GroqRequest request = new GroqRequest();
		    request.setModel(groqConfig.getModel());
		    request.setMessages(List.of(message));

		    GroqResponse response = groq.post()
		            .uri("/openai/v1/chat/completions")
		            .bodyValue(request)
		            .retrieve()
		            .bodyToMono(GroqResponse.class)
		            .block();
		    return response != null && response.getChoices() != null && !response.getChoices().isEmpty()
		            ? response.getChoices().get(0).getMessage().getContent()
		            : null ; 
		    
	}


}
