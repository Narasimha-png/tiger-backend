package com.tiger.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tiger.dto.TempDto;
import com.tiger.entity.Postings;
import com.tiger.entity.User;
import com.tiger.exception.GroqException;
import com.tiger.exception.UserException;
import com.tiger.repository.PostsRespository;
import com.tiger.repository.UserRepository;
import com.tiger.service.LinkedinPostService;
@Service
public class LinkedinPostServiceImpl implements LinkedinPostService {
	private UserRepository userRepo ;
	private PostsRespository postRepo ;
	private GroqServiceImpl groq; 
	public LinkedinPostServiceImpl(UserRepository userRepo, PostsRespository postRepo, GroqServiceImpl groq) {
		this.userRepo = userRepo ;
		this.postRepo = postRepo ;
		this.groq = groq ;
	}
	public Boolean isUserExists(String gmail) {
		return userRepo.findByGmail(gmail).isPresent();
	}

	@Override
	public String postImage(String email) throws UserException {
		   if (!isUserExists(email))
		        throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());

		    User user = userRepo.findByGmail(email).get();
		    String accessToken = user.getAccessToken();
		    String authorUrn = "urn:li:person:"+ user.getLinkedinUserId();

		    WebClient webClient = WebClient.builder()
		            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
		            .build();

		    String registerUploadJson = """
		        {
		          "registerUploadRequest": {
		            "recipes": ["urn:li:digitalmediaRecipe:feedshare-image"],
		            "owner": "%s",
		            "serviceRelationships": [
		              {
		                "relationshipType": "OWNER",
		                "identifier": "urn:li:userGeneratedContent"
		              }
		            ]
		          }
		        }
		    """.formatted(authorUrn);

		    String registerResponse = webClient.post()
		            .uri("https://api.linkedin.com/v2/assets?action=registerUpload")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(registerUploadJson)
		            .retrieve()
		            .bodyToMono(String.class)
		            .block();

		    JSONObject registerJson = new JSONObject(registerResponse);
		    String uploadUrl = registerJson.getJSONObject("value")
		            .getJSONObject("uploadMechanism")
		            .getJSONObject("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest")
		            .getString("uploadUrl");

		    String assetUrn = registerJson.getJSONObject("value").getString("asset");

		    byte[] data;
		    try {
		        data = Files.readAllBytes(Path.of("src/main/resources/static/images/post.png"));
		    } catch (IOException e) {
		        throw new RuntimeException("Image file not found", e);
		    }

		    WebClient.create().put()
		            .uri(uploadUrl)
		            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
		            .bodyValue(data)
		            .retrieve()
		            .toBodilessEntity()
		            .block();
		    return assetUrn;
	}

	@Override
	public String sharePost(String email) throws UserException {
	    if (!isUserExists(email))
	        throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());

	    User user = userRepo.findByGmail(email).get();
	    String MEDIA_URN = this.postImage(email);
	    String accessToken = user.getAccessToken();
	    String authorUrn = "urn:li:person:" + user.getLinkedinUserId();
	    String text = null ;
		try {
			
			text = groq.genereateDailyLinkedinPost(email);
			if(text ==null )
				return null;
		} catch (GroqException e) {
			
			e.printStackTrace();
		}
		String escapedText = text.replace("\"", "\\\"").replace("\n", "\\n");
	    if( text == null )
	    	return null ;
	    WebClient webClient = WebClient.builder()
	            .baseUrl("https://api.linkedin.com") 
	            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .build();
	    
	    String jsonBody = """
	        {
	            "author": "%s",
	            "lifecycleState": "PUBLISHED",
	            "specificContent": {
	                "com.linkedin.ugc.ShareContent": {
	                    "shareCommentary": {
	                        "text": "%s"
	                    },
	                    "shareMediaCategory": "IMAGE",
	                    "media": [
	                        {
	                            "status": "READY",
	                            "description": {
	                                "text": "Center stage!"
	                            },
	                            "media": "%s",
	                            "title": {
	                                "text": "LinkedIn Talent Connect 2021"
	                            }
	                        }
	                    ]
	                }
	            },
	            "visibility": {
	                "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
	            }
	        }
	    """.formatted(authorUrn, escapedText, MEDIA_URN);

	    String response = webClient.post()
	            .uri("/v2/ugcPosts")
	            .bodyValue(jsonBody)
	            .retrieve()
	            .bodyToMono(String.class)
	            .block();

	 
	    JSONObject obj = new JSONObject(response);
	    String postUrn = obj.getString("id"); 

	    String encodedPostUrn = postUrn.replace(":", "%3A");
	    String postUrl = "https://www.linkedin.com/feed/update/" + encodedPostUrn;
	    
	    Postings post = new Postings() ;
	    post.setUrl(postUrl) ;
	    post.setUser(user) ;
	    postRepo.save(post) ;
	    
	    return postUrl;
	}
	@Override
	public TempDto totalPostsShared(String gmail) throws UserException {
		 if (!isUserExists(gmail))
		        throw new UserException("User not found.", HttpStatus.NOT_FOUND.value());

		 Integer posts = postRepo.findByUserGmail(gmail).size() ;
		 TempDto totalPosts = new TempDto() ;
		 totalPosts.setTotalPosts(posts);
		 return totalPosts ;
	}
	
	 

}
