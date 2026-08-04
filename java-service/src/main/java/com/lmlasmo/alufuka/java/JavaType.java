package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JavaType extends JavaElement {

	private List<JavaElement> members;
	
	public JavaType(String name, String definition) {
		super(name, definition);
	}
	
	public JavaType(String name, String definition, List<String> annotations) {
		super(name, definition, annotations);
	}
	
	public JavaType(String name, String definition, List<String> annotations, String javadoc) {
		super(name, definition, annotations, javadoc);
	}
	
}
