package com.tiger.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.dto.LeetcodeStreakDTO;
import com.tiger.entity.LeetcodeStreak;
@Repository
public interface LeetcodeStreakRepository extends CrudRepository<LeetcodeStreak, Integer>{

	List<LeetcodeStreak> findByUserGmail(String gmail);
}
