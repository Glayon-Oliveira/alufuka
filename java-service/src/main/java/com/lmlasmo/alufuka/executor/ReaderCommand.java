package com.lmlasmo.alufuka.executor;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReaderCommand implements Command, CommandWithFilePath {

	private final CommandType type = CommandType.READER;
	
	@NonNull
	@JsonProperty("filePath")
	@JsonAlias({"file_path", "fp"})
	private String filePath;
	
}
