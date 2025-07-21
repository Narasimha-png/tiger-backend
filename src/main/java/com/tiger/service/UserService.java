package com.tiger.service;

import java.util.List;

import com.tiger.dto.NotificationDTO;
import com.tiger.dto.RoastResponse;
import com.tiger.dto.Streak;
import com.tiger.dto.StreakDTO;
import com.tiger.dto.UserActivityDTO;
import com.tiger.dto.UserDTO;
import com.tiger.exception.UserException;


public interface UserService {
	Boolean isUserExists(String gmail) ;
	Integer register(UserDTO user ) throws UserException ;
	Boolean loginUser(UserDTO user) throws UserException ;
	List<RoastResponse> getRoastings(String gmail) throws UserException ;
	List<Streak> getStreak(String gmail) throws UserException ;
	void updateLeetCodeProfile(String gmail, String updatedUserName, Integer targetSubmissions) throws UserException ;
	void updateGitHubProfile(String gmail , String updatedUserName, Integer targetCommits ) throws UserException ;
	UserDTO getProfile(String gmail) throws UserException ;
	void logout(String email) throws UserException ;
	List<UserActivityDTO> getUserActivity(String email) ;
	StreakDTO getUserStreak(String email) throws UserException ;
	void toggleLinkedinPostService(String email, Boolean status ) throws UserException ;
	void addNotifyDevice(String gmail, String fcmToken) throws UserException ;
	List<NotificationDTO> getNotifyDevices(String gmail) throws UserException ;
}
