package com.tiger.service;

import com.tiger.exception.PaymentException;

public interface PaymentService {
	Integer newPaymentUpdate(String email)throws PaymentException ;
	Boolean checkSubscriptionStatus(String emiail) throws PaymentException ;
}
