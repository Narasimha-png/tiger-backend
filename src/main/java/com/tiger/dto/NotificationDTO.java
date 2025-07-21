package com.tiger.dto;

import lombok.Data;

@Data
public class NotificationDTO {
	private String fcmToken ;
	private UserDTO user ;
}
