package com.tiger.service;

import com.tiger.dto.TempDto;
import com.tiger.exception.UserException;

public interface LinkedinPostService {
	String postImage(String gmail) throws UserException ;
	String sharePost(String gmail) throws UserException ;
	TempDto totalPostsShared(String gmail ) throws UserException ;
}
