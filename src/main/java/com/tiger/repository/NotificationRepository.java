package com.tiger.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {
	List<Notification> findByUserGmail(String gmail); 
	Optional<Notification> findByFcmToken(String fcmToken) ;
}
