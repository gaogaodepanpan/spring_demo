package com.sipc.catalina.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 *   全局捕获异常
 */
@RestControllerAdvice
public class ExamExceptionHandler {

	@ExceptionHandler(ExamException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, Object> exceptionHandler(ExamException e) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", e.getCode());
		map.put("message", e.getMessage());
		return map;
	}

}