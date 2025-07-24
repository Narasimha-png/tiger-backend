package com.tiger.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tiger.entity.User;
import com.tiger.exception.GroqException;
import com.tiger.exception.UserException;
import com.tiger.repository.NotificationRepository;
import com.tiger.repository.UserRepository;

@Service
public class Schedulars {
	private LeetcodeServiceImpl leetcode;
	private GroqServiceImpl groq;
	private LinkedinPostServiceImpl linkedIn;
	private UserRepository userRepo;
	private GithubServiceImpl github;
	private FirebaseNotificationService firebaseService;
	private NotificationRepository notificationRepo;

	public Schedulars(LeetcodeServiceImpl leetcode, GroqServiceImpl groq, LinkedinPostServiceImpl linkedIn,
			UserRepository userRepo, GithubServiceImpl github, FirebaseNotificationService firebaseService,
			NotificationRepository notificationRepo) {
		this.leetcode = leetcode;
		this.groq = groq;
		this.linkedIn = linkedIn;
		this.userRepo = userRepo;
		this.github = github;
		this.firebaseService = firebaseService;
		this.notificationRepo = notificationRepo;
	}

	@Scheduled(cron = "0 30 23 * * *") 
	public void updateLeetcodeDaily() {
		List<User> users = (List<User>) userRepo.findAll();
		users.stream().forEach((user) -> {
			try {
				leetcode.updateTodaysSubmissions(user.getGmail());
				github.updateTodaysCommits(user.getGmail());

			} catch (UserException e) {
				e.printStackTrace();
			}
		});
	}

	@Scheduled(cron = "0 30 23 * * *") 
	public void updateDailyRoasts() {
		List<User> users = (List<User>) userRepo.findAll();
		users.stream().forEach(user -> {
			if (user.getLeetcodeProfile() != null) {
				try {
					groq.putDailyRoastings(user.getLeetcodeProfile(), user.getGmail(), 0);
				} catch (GroqException e) {

					e.printStackTrace();
				}

			}
		});

	}

	@Scheduled(cron = "0 0 21 * * *") 
	public void postInLinkedIn() {
		List<User> users = (List<User>) userRepo.findAll();
		users.stream().forEach(user -> {
			if (user.getLeetcodeProfile() != null) {
				try {
					if (user.getLinkedinPostService())
						linkedIn.sharePost(user.getGmail());
				} catch (UserException e) {

					e.printStackTrace();
				}

			}
		});

	}
	@Scheduled(cron = "0 0 6 * * *")
	public void sendNotifications() {
		String tempText = "Good Morning, Grab your laptop and learn new!!";
		try {
			String result = groq.morningNotification();
			if (result != null && !result.isBlank()) {
				tempText = result;
			}
		} catch (GroqException e) {

			e.printStackTrace();
		}
		final String morningText = tempText;
		String accessToken = firebaseService.refreshAccessToken();

		List<User> users = (List<User>) userRepo.findAll();
		List<String> fcmTokens = users.stream()
				.flatMap(user -> notificationRepo.findByUserGmail(user.getGmail()).stream())
				.map(notification -> notification.getFcmToken()).collect(Collectors.toList());
		System.out.println(fcmTokens);
		fcmTokens.stream().forEach(token -> {

			firebaseService.sendNotification(accessToken, token, "Tiger", morningText);

		});

	}
	@Scheduled(cron = "0 30 18 * * *")
	public void sendNotificationsAtEventing() {
		
		String accessToken = firebaseService.refreshAccessToken();

		List<User> users = (List<User>) userRepo.findAll();
		List<String> fcmTokens = users.stream()
				.flatMap(user -> notificationRepo.findByUserGmail(user.getGmail()).stream())
				.map(notification -> notification.getFcmToken()).collect(Collectors.toList());

		fcmTokens.stream().forEach(token -> {
			String motivation = "Good Evening, Grab your laptop and learn new!!";
			String temp = null;
			try {
				temp = groq.workUpdateNotification(notificationRepo.findByFcmToken(token).get().getUser().getGmail());
			} catch (GroqException e) {

				e.printStackTrace();
			}
			if (temp != null)
				motivation = temp;
			firebaseService.sendNotification(accessToken, token, "Tiger-your soulmate", motivation);

		});

	}

}
