package com.tiger.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tiger.config.UrlConfig;
import com.tiger.exception.PaymentException;
import com.tiger.service.AuthService;
import com.tiger.service.implementation.PaymentServiceImpl;

@RestController
@RequestMapping("tiger/payment")
public class PaymentController {
	private PaymentServiceImpl paymentService ;
	private AuthService authService ;
	private UrlConfig urlConfig ;
	public PaymentController(PaymentServiceImpl paymentService, AuthService authService, UrlConfig urlConfig) {
		this.authService = authService ;
		this.paymentService = paymentService ;
		this.urlConfig = urlConfig ;
	}
	@GetMapping("checkout")
	public ResponseEntity<Map<String, String>> checkOutPage() throws Exception {
		Stripe.apiKey="sk_test_51RtfLPAWam4a1NyqGcrq5FKwnKygDo1oqtN2WrXW3ugahgGA8quN3Dpca7afVcWJnM6IlRmeHy2zaHuuQ3G9CWCn00DYUjFsI4" ;
		if(paymentService.checkSubscriptionStatus(authService.getEmail())) {
			throw new PaymentException("Already a premium user") ;
		}
		SessionCreateParams params = SessionCreateParams.builder().setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				.setSuccessUrl(urlConfig.getMyUrl() + "/tiger/payment/collect?email="+ authService.getEmail())
				.setCancelUrl(urlConfig.getMyDomain())
				.addLineItem(
						SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPrice("price_1RtfOMAWam4a1Nyq1LOSWa1Q")
						.build()
						)
				.build();
		Session session = Session.create(params) ;
		Map<String, String> response = new HashMap<>() ;
		response.put("url", session.getUrl()) ;
		return new ResponseEntity<Map<String,String>>(response, HttpStatus.OK) ;
	}
	@GetMapping("collect")
	public RedirectView collectMoney(@RequestParam String email) throws PaymentException {
		paymentService.newPaymentUpdate(email) ;
		return new RedirectView(urlConfig.getMyDomain() + "/signup") ;
	}

}
