package com.lmlasmo.alufuka.executor;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.alufuka.java.JavadocTarget;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JavadocWriterCommand extends JavadocTarget implements Command, CommandWithFilePath {

	private final CommandType type = CommandType.JAVADOC_WRITER;
	private String filePath;
	
	@JsonCreator
	public JavadocWriterCommand(
			@JsonProperty("file_path") @JsonAlias("fp") String filePath,
	        @JsonProperty("path") String path,
	        @JsonProperty("content") String content) {
		
		super(path, content);
		this.filePath = filePath;
	}
	
}
