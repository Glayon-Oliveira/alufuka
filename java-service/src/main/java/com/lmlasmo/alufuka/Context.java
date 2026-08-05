package com.lmlasmo.alufuka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.lmlasmo.alufuka.executor.Command;
import com.lmlasmo.alufuka.executor.CommandExecutor;
import com.lmlasmo.alufuka.executor.ExceptionHandler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {
	
	private static final List<CommandExecutor<?>> COMMAND_EXECUTORS = new ArrayList<>();
    private static final List<ExceptionHandler<?>> EXCEPTION_HANDLERS = new ArrayList<>();
    
    private static SimpleModule SIMPLE_MODULE = new SimpleModule();
    private static ObjectMapper OBJECT_MAPPER = createObjectMapper();

    public static List<CommandExecutor<?>> commandExecutors() {
        return Collections.unmodifiableList(COMMAND_EXECUTORS);
    }

    public static List<ExceptionHandler<?>> exceptionHandlers() {
        return Collections.unmodifiableList(EXCEPTION_HANDLERS);
    }
    
    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
    
    public static void register(CommandExecutor<? extends Command> executor) {
    	COMMAND_EXECUTORS.removeIf(ce -> ce.getTargetType() == executor.getTargetType());
    	COMMAND_EXECUTORS.add(executor);
    }
    
    public static void register(ExceptionHandler<? extends Throwable> handler) {
    	EXCEPTION_HANDLERS.removeIf(eh -> eh.getTargetException() == handler.getTargetException());
    	
    	EXCEPTION_HANDLERS.add(handler);
    }
    
    public static void configureModule(Consumer<SimpleModule> c) {
    	c.accept(SIMPLE_MODULE);
    	
    	OBJECT_MAPPER = createObjectMapper();
    }
    
    private static ObjectMapper createObjectMapper() {
    	return JsonMapper.builder()
    			.addModule(SIMPLE_MODULE)
    			.build();
    }
    
    static void removeCommandExecutor(Class<CommandExecutor<?>> exec) {
    	COMMAND_EXECUTORS.removeIf(ce -> ce.getClass() == exec);
    }
    
    static void removeExceptionHandler(Class<ExceptionHandler<?>> handler) {
    	EXCEPTION_HANDLERS.removeIf(eh -> eh.getClass() == handler);
    }
    
    static void clearCommandExecutors() {
    	COMMAND_EXECUTORS.clear();
    }
    
    static void clearExceptionHandlers() {
    	EXCEPTION_HANDLERS.clear();
    }
    
}
