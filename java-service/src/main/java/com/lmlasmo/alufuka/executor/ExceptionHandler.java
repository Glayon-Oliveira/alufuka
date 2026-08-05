package com.lmlasmo.alufuka.executor;

public interface ExceptionHandler<T extends Throwable> {

	public Class<T> getTargetException();
	
	public FailureResult handle(T th);
	
}
