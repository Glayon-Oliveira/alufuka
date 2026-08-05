package com.lmlasmo.alufuka.executor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class FailureResult implements Result {
	
	private final Status status = Status.FAILURE;

	@JsonInclude(Include.NON_NULL)
	private String command;
	
	@NonNull private String message;
	
	@JsonInclude(Include.NON_NULL)
	private Object cause;

	@Override
	public SuccessResult asSuccess() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FailureResult asFailure() {
		return this;
	}
	
}
