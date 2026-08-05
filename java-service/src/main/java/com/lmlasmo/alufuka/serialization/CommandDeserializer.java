package com.lmlasmo.alufuka.serialization;

import java.util.List;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.executor.Command;
import com.lmlasmo.alufuka.executor.CommandExecutor;
import com.lmlasmo.alufuka.executor.CommandType;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CommandDeserializer extends ValueDeserializer<Command> {
	
	private static final List<CommandExecutor<?>> COMMAND_EXECUTORS = Context.commandExecutors();
	
	@Override
	public Command deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		JsonNode node = ctxt.readTree(p);
		node = node.asObject();
		
		if(node.has("type")) {
			String typeName = node.get("type").asString();
			
			CommandType type = CommandType.valueOf(typeName.toUpperCase());
			
			for(CommandExecutor<?> exec: COMMAND_EXECUTORS) {
				if(exec.getTargetType() == type) {
					return ctxt.readTreeAsValue(node, exec.getCommandType());
				}
			}
			
			throw new RuntimeException("The '"+typeName+"' command is unknown");
		}
		
		throw new IllegalStateException("The 'type' property is required");
	}
	
}
