package com.lmlasmo.alufuka.executor;

public interface Result {

	public Status getStatus();
	
	public SuccessResult asSuccess();
	
	public FailureResult asFailure();
	
	public static enum Status {
		SUCCESS,
		FAILURE
	}
	
}
