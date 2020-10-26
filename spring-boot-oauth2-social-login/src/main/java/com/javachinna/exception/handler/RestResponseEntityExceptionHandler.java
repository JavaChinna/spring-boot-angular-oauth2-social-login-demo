package com.javachinna.exception.handler;

import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.javachinna.dto.ApiResponse;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	public RestResponseEntityExceptionHandler() {
		super();
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		logger.error("400 Status Code", ex);
		final BindingResult result = ex.getBindingResult();

		String error = result.getAllErrors().stream().map(e -> {
			if (e instanceof FieldError) {
				return ((FieldError) e).getField() + " : " + e.getDefaultMessage();
			} else {
				return e.getObjectName() + " : " + e.getDefaultMessage();
			}
		}).collect(Collectors.joining(", "));
		return handleExceptionInternal(ex, new ApiResponse(false, error), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
}
