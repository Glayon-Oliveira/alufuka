package com.lmlasmo.alufuka.executor;

import java.util.List;

import com.lmlasmo.alufuka.Context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class ExceptionHandlerExecutor {
	
	public <T extends Throwable> FailureResult handle(T th) {
		return handle(null, th);
	}
	
	public <T extends Throwable> FailureResult handle(String command, T th) {
		List<ExceptionHandler<T>> supported = Context.exceptionHandlers().stream()
				.filter(h -> h.getTargetException().isInstance(th))
				.map(eh ->  (ExceptionHandler<T>) eh)
				.toList();
		
		ExceptionHandler<T> handler = (ExceptionHandler<T>) findMostSpecific(supported, th);
		
		if(handler == null) {
			return defaultHandle(command, th);
		}
		
		return handler.handle(th);
	}
	
	private <T extends Throwable> ExceptionHandler<T> findMostSpecific(List<ExceptionHandler<T>> handlers, Throwable th) {
		return handlers.stream()
				.reduce((eh1, eh2) -> {
					if(eh1.getTargetException() == eh2.getTargetException()) {
						return eh1;
					}
					
					if(eh1.getTargetException().isAssignableFrom(eh2.getTargetException())) {
						return eh2;
					}
					
					return eh1;
				})
				.orElse(null);
	}
	
	protected FailureResult defaultHandle(String command, Throwable th) {
		return new FailureResult(command, th.getMessage(), null);
	}
	
}
