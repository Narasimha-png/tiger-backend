package com.tiger.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import com.tiger.config.UrlConfig;
import com.tiger.dto.MessageResponse;
import com.tiger.dto.NotificationDTO;
import com.tiger.dto.RoastResponse;
import com.tiger.dto.StreakDTO;
import com.tiger.dto.TempDto;
import com.tiger.dto.UserActivityDTO;
import com.tiger.dto.UserDTO;
import com.tiger.exception.GroqException;
import com.tiger.exception.UserException;
import com.tiger.service.AuthService;
import com.tiger.service.impl.GroqServiceImpl;
import com.tiger.service.impl.LinkedinPostServiceImpl;
import com.tiger.service.impl.Schedulars;
import com.tiger.service.impl.UserServiceImpl;

import jakarta.persistence.PostRemove;


@RestController
@RequestMapping("api/tiger/user")
public class UserController {
	private UserServiceImpl userService;
	private AuthService auth;
	private final UrlConfig urlConfig ;
	private GroqServiceImpl groqService ;
	private LinkedinPostServiceImpl linkedinPost ;
	private Schedulars schedulars ;

	public UserController(UserServiceImpl user, AuthService auth, GroqServiceImpl groqService,  UrlConfig urlConfig,LinkedinPostServiceImpl linkedinPost, Schedulars schedulars  ) {
		this.userService = user;
		this.auth = auth;
		this.groqService = groqService ;
		this.urlConfig = urlConfig ;
		this.linkedinPost = linkedinPost ;
		this.schedulars = schedulars ;
	}

	@PostMapping("register")
	public ResponseEntity<MessageResponse> register(@RequestBody UserDTO user) throws UserException {
		Integer userId = userService.register(user);
		MessageResponse res = new MessageResponse("User Successfully added: " + userId);
		return new ResponseEntity<MessageResponse>(res, HttpStatus.CREATED);
	}

	@PostMapping("login")
	public ResponseEntity<MessageResponse> login(@RequestBody UserDTO user) throws UserException {
		userService.loginUser(user);
		return new ResponseEntity<MessageResponse>(new MessageResponse("LoggedIn successfully "), HttpStatus.ACCEPTED);
	}

	@GetMapping("roasts")
	public ResponseEntity<List<RoastResponse>> roastings() throws UserException {
		return new ResponseEntity<List<RoastResponse>>(userService.getRoastings(auth.getEmail()), HttpStatus.OK);
	}
	
	@GetMapping("profile")
	public ResponseEntity<UserDTO> profile() throws UserException {
		return new ResponseEntity<UserDTO>(userService.getProfile(auth.getEmail()), HttpStatus.OK) ;
	}
	//@PostMapping("roasts/{profile}")
	public ResponseEntity<MessageResponse> roastingsPost( String profile) throws GroqException {
		groqService.getProfileRoastings(profile, auth.getEmail() , 0);
		return new ResponseEntity<MessageResponse>(new MessageResponse("DOne"), HttpStatus.OK) ;
	}

	@GetMapping("linkedin/login")
	public RedirectView redirectToLinkedIn() {
		String authUrl = UriComponentsBuilder.newInstance().scheme("https").host("www.linkedin.com")
				.path("/oauth/v2/authorization").queryParam("response_type", "code")
				.queryParam("client_id", urlConfig.getLinkedinClientId())
				.queryParam("redirect_uri", urlConfig.getLinkedinCallback())
				.queryParam("scope", "openid profile email w_member_social").queryParam("state", "random_state_123")
				.toUriString();

		return new RedirectView(authUrl);
	}

	@GetMapping("/linkedin/callback")
	public RedirectView handleCallback(@RequestParam("code") String code) throws Exception {
		code = userService.storeAcessKey(code) ;
		String redirectUrl = urlConfig.getRedirectUrl() + code;
		return new RedirectView(redirectUrl);
	}

	@PatchMapping("leetcode")
	public ResponseEntity<MessageResponse> updateLeetCodeProfile(@RequestBody UserDTO user) throws UserException{
		String email = auth.getEmail() ;
		userService.updateLeetCodeProfile(auth.getEmail(), user.getLeetcodeProfile(), user.getTargetSubmissions());

		try {
			groqService.getProfileRoastings(user.getLeetcodeProfile(),email, 0 ) ;
		} catch (GroqException e) {
			
			e.printStackTrace();
		}

		return new ResponseEntity<MessageResponse>(new MessageResponse("Updated LeetCode Profile" , HttpStatus.OK.value()),HttpStatus.OK) ;
	}
	@PatchMapping("github")
	public ResponseEntity<MessageResponse> updateGitHubProfile(@RequestBody UserDTO user) throws UserException{
		userService.updateGitHubProfile(auth.getEmail(), user.getGithubProfile(), user.getTargetCommits());
		return new ResponseEntity<MessageResponse>(new MessageResponse("Updated GitHub Profile", HttpStatus.OK.value()),HttpStatus.OK) ;
	}
	
	@PostMapping("logout")
	public ResponseEntity<MessageResponse> logout() throws UserException{
		userService.logout(auth.getEmail());
		return new ResponseEntity<MessageResponse>(new MessageResponse("Logged out successfully"), HttpStatus.OK) ;
	}
	
	@GetMapping("activity")
	public ResponseEntity<List<UserActivityDTO>> getMapping(){
		
		return new ResponseEntity<List<UserActivityDTO>>(userService.getUserActivity(auth.getEmail()) , HttpStatus.OK ) ;
	}
	@GetMapping("streak")
	public ResponseEntity<StreakDTO> getStreak() throws UserException{
		return new ResponseEntity<StreakDTO>(userService.getUserStreak(auth.getEmail()) , HttpStatus.OK) ;
	}
	@PostMapping("image")
	public ResponseEntity<String> postImage() throws UserException {
	    return new ResponseEntity<>(linkedinPost.postImage(auth.getEmail()), HttpStatus.OK);
	}
	@PostMapping("post")
	public ResponseEntity<MessageResponse> newLinkedInPost() throws UserException {
	    return new ResponseEntity<MessageResponse>(new MessageResponse(linkedinPost.sharePost(auth.getEmail())) , HttpStatus.OK);
	}
	@PostMapping("content")
	public ResponseEntity<MessageResponse> groq() throws GroqException  {
	    return new ResponseEntity<MessageResponse>(new MessageResponse(groqService.genereateDailyLinkedinPost(auth.getEmail())) , HttpStatus.OK);
	}
	@PostMapping("linkedinservice")
	public ResponseEntity<MessageResponse> toggleLinkedinService(@RequestBody UserDTO user ) throws  UserException  {
		userService.toggleLinkedinPostService(auth.getEmail(), user.getLinkedinPostService()) ;
	    return new ResponseEntity<MessageResponse>(new MessageResponse("Updated Successfully" ), HttpStatus.OK);
	}
	@GetMapping("totalposts")
	public ResponseEntity<TempDto> getTotalPosts( ) throws  UserException  {
		return new ResponseEntity<TempDto>( linkedinPost.totalPostsShared(auth.getEmail()) , HttpStatus.OK);
	}
	
	@PostMapping("addnotificationdevice")
	public ResponseEntity<MessageResponse> addNotificationDevice(@RequestBody NotificationDTO notification) throws UserException{
		userService.addNotifyDevice(auth.getEmail() , notification.getFcmToken());
		return new ResponseEntity<MessageResponse>(new MessageResponse("Registered Succesfully"), HttpStatus.OK) ;
	}
	@PostMapping("notify")
	public ResponseEntity<MessageResponse> notifyUser(){
		schedulars.sendNotifications();
		return new ResponseEntity<MessageResponse>(new MessageResponse("Sent successfully"), HttpStatus.OK) ;
	}
	
}
