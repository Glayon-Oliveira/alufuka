package com.lmlasmo.alufuka.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandRegistry {

	private static final List<CommandExecutor<?>> EXECUTORS = new ArrayList<>();
	
	public static List<CommandExecutor<?>> getExecutors() {
		return Collections.unmodifiableList(EXECUTORS);
	}
	
	public static <T extends Command> void register(CommandExecutor<T> executor) {
		EXECUTORS.add(executor);
	}
	
}
