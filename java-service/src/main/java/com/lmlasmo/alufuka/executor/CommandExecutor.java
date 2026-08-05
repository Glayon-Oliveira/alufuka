package com.lmlasmo.alufuka.executor;

public interface CommandExecutor<T extends Command> {
	
	public Class<T> getCommandType();
	
	public CommandType getTargetType();
	
	public Result execute(T command) throws Exception;
	
	public default boolean supports(Command command) {
		return getTargetType() == command.getType();
	}
	
}
