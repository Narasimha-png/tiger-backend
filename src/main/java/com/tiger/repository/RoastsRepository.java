package com.tiger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.Roasts;
@Repository
public interface RoastsRepository extends CrudRepository<Roasts, Integer> {
	List<Roasts> findByUserGmail(String gmail) ;
	@Modifying
	@Query("DELETE FROM Roasts r WHERE r.user.gmail = :gmail")
	void deleteAllByUserGmail(String gmail) ;
}
