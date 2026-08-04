package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JavaField extends JavaElement {
	
	public JavaField(String name, String definition) {
		super(name, definition);
	}
	
	public JavaField(String name, String definition, @NonNull List<String> annotations) {
		super(name, definition, annotations);
	}
	
	public JavaField(String name, String definition, @NonNull List<String> annotations, String javadoc) {
		super(name, definition, annotations, javadoc);
	}
	
}
