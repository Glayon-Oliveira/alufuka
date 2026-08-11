package com.lmlasmo.alufuka.java;

import java.util.List;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class JavadocTarget {
	
	private static final Pattern PATH_REGEX = Pattern.compile("^\\w+(?:\\.\\w+)*$");

	private String path;
	private List<String> argumentsTypes;
	private String content;
	
	public JavadocTarget(@NonNull String path, String content) {
		if(!PATH_REGEX.matcher(path).matches()) {
			throw new IllegalArgumentException("Invalid path format");
		}
		
		this.path = path;
		this.content = content;
	}
	
	public JavadocTarget(@NonNull String path, String content, List<String> argumentsTypes) {
		if(!PATH_REGEX.matcher(path).matches()) {
			throw new IllegalArgumentException("Invalid path format");
		}
		
		this.path = path;
		this.content = content;
		this.argumentsTypes = argumentsTypes;
	}
	
}
