package com.tiger.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.Postings;
@Repository
public interface PostsRespository extends CrudRepository<Postings, Integer> {
	List<Postings> findByUserGmail(String gmail);
}
