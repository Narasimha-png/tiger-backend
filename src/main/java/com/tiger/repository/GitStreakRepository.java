package com.tiger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.GithubStreak;
@Repository
public interface GitStreakRepository extends JpaRepository<GithubStreak, Integer>{
	List<GithubStreak> findByUserGmail(String gmail ) ;
}
