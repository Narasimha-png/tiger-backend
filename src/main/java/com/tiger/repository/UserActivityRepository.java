package com.tiger.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.UserActivity;
@Repository
public interface UserActivityRepository extends CrudRepository<UserActivity, Integer> {
	List<UserActivity> findByUserGmail(String gmail) ;
}
