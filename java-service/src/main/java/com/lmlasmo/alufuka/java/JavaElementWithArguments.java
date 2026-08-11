package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class JavaElementWithArguments extends JavaElement {

	private List<String> argumentTypes;
	
	public JavaElementWithArguments(String name, String definition, List<String> argumentTypes) {
		super(name, definition);
		this.argumentTypes = argumentTypes;
	}
	
	public JavaElementWithArguments(String name, String definition, List<String> argumentTypes, List<String> annotations) {
		super(name, definition, annotations);
		this.argumentTypes = argumentTypes;
	}
	
	public JavaElementWithArguments(String name, String definition, List<String> argumentTypes, List<String> annotations, String javadoc) {
		super(name, definition, annotations, javadoc);
		this.argumentTypes = argumentTypes;
	}

}
