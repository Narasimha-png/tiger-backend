package com.tiger.service;

import com.tiger.dto.LeetcodeStreakDTO;
import com.tiger.exception.GroqException;

public interface GroqService {
	void getProfileRoastings(String userName, String gmail, int attempts) throws GroqException ;
	void putDailyRoastings(String userName , String gmail, int attempts) throws GroqException ;
	String genereateDailyLinkedinPost(String gmail) throws GroqException ;
	String morningNotification() throws GroqException ;
	String workUpdateNotification(String userName) throws GroqException ;
}
