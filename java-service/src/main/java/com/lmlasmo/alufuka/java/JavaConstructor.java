package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JavaConstructor extends JavaElementWithArguments {
	
	public JavaConstructor(String name, String definition, List<String> argumentTypes) {
		super(name, definition, argumentTypes);
	}
	
	public JavaConstructor(String name, String definition, List<String> argumentTypes, List<String> annotations) {
		super(name, definition, argumentTypes, annotations);
	}
	
	public JavaConstructor(String name, String definition, List<String> argumentTypes, List<String> annotations, String javadoc) {
		super(name, definition, argumentTypes, annotations, javadoc);
	}

}
