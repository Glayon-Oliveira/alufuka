package com.lmlasmo.alufuka.executor;

import java.io.InputStream;

import com.lmlasmo.alufuka.Context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Executor {
	
	private ExceptionHandlerExecutor exhExecutor = new ExceptionHandlerExecutor();
	
	public Result execute(InputStream in) {
		try {
			Command command = Context.objectMapper().readValue(in, Command.class);
			
			return execute(command);
		}catch(Exception ex) {
			return exhExecutor.handle(ex);
		}
	}
	
	public Result execute(byte[] bytes) throws Exception {
		try {
			Command command = Context.objectMapper().readValue(bytes, Command.class);
			
			return execute(command);
		}catch(Exception ex) {
			return exhExecutor.handle(ex);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Command> Result execute(T command) {
		try {
			CommandExecutor<T> executor = (CommandExecutor<T>) Context.commandExecutors().stream()
					.filter(e -> e.supports(command))
					.findFirst()
					.orElse(null);
			
			if(executor == null) {
				throw new IllegalArgumentException();
			}
			
			return executor.execute(command);
		}catch(Exception ex) {
			return exhExecutor.handle(command.getType().name(), ex);
		}
	}
	
}
