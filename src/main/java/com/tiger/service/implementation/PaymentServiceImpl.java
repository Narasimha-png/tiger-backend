package com.tiger.service.implementation;

import org.springframework.stereotype.Service;

import com.tiger.entity.Payment;
import com.tiger.entity.User;
import com.tiger.exception.PaymentException;
import com.tiger.repository.PaymentRepository;
import com.tiger.repository.UserRepository;
import com.tiger.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {
	private PaymentRepository paymentRepository ;
	private UserRepository userRepo ;
	public PaymentServiceImpl(PaymentRepository paymentRepository, UserRepository userRepo) {
		this.paymentRepository = paymentRepository ;
		this.userRepo = userRepo ;
	}
	@Override
	public Integer newPaymentUpdate(String email) throws PaymentException {
		User user = userRepo.findByGmail(email).orElseThrow(()-> new PaymentException("User Not Found")) ;
		user.setSubscription(true);
		userRepo.save(user) ;
		Payment payment = new Payment() ;
		payment.setAmount(50L) ;
		payment.setUser(user);
		paymentRepository.save(payment) ;
		
		return payment.getId() ;
	}
	@Override
	public Boolean checkSubscriptionStatus(String email) throws PaymentException {
		return paymentRepository.findByUserGmail(email) != null ;
	}

}
