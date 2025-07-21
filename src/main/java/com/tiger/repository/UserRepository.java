package com.tiger.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
	Optional<User> findByGmail(String gmail) ;
	Optional<User> findByCode(String code) ;
}
