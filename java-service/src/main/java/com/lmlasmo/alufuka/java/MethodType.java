package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MethodType extends JavaElement {
	
	public MethodType(String name, String definition) {
		super(name, definition);
	}
	
	public MethodType(String name, String definition, List<String> annotations) {
		super(name, definition, annotations);
	}
	
	public MethodType(String name, String definition, List<String> annotations, String javadoc) {
		super(name, definition, annotations, javadoc);
	}

}
