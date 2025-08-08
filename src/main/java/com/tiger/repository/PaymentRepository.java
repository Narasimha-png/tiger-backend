package com.tiger.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiger.entity.Payment;
import com.tiger.entity.User;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Integer> {
	Payment findByUserGmail(String gmail) ;
}
