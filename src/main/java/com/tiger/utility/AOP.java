package com.tiger.utility;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AOP {
	private static final Logger LOGGER = LogManager.getLogger(AOP.class) ;
	@AfterThrowing(pointcut =  "execution(* com.tiger.controllers.*.*(..))", throwing = "ex")
	public void ControllerTestExp(Exception ex) {
		LOGGER.error(ex.getMessage(), ex) ;
		System.err.println(LocalDateTime.now() + " " + ex.getMessage()) ;
	}
	@AfterThrowing(pointcut =  "execution(* com.tiger.service.impl.*.*(..))", throwing = "ex")
	public void serviceLayerErrors(Exception ex) {
		LOGGER.error(ex.getMessage(), ex) ;
	}

}
