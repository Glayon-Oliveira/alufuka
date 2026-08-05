package com.lmlasmo.alufuka.executor;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class SuccessResult implements Result {

	private final Status status = Status.SUCCESS;
	
	private @NonNull String command;
	
	@JsonInclude(Include.NON_EMPTY)
	private String message;
	
	private @NonNull Object body;
	
	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Object> metadata = new HashMap<>();

	@Override
	public SuccessResult asSuccess() {
		return this;
	}

	@Override
	public FailureResult asFailure() {
		throw new UnsupportedOperationException();
	}
	
}
