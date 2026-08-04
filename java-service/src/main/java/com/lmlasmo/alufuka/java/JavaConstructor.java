package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JavaConstructor extends JavaElement {
	
	public JavaConstructor(String name, String definition) {
		super(name, definition);
	}
	
	public JavaConstructor(String name, String definition, List<String> annotations) {
		super(name, definition, annotations);
	}
	
	public JavaConstructor(String name, String definition, List<String> annotations, String javadoc) {
		super(name, definition, annotations, javadoc);
	}

}
