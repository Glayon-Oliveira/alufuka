package com.lmlasmo.alufuka.executor;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Command {

	@JsonProperty
	public CommandType getType();
	
}
