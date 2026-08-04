package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
public class JavaElement {

	@NonNull private String name;
	@NonNull private String definition;
	
	private List<String> annotations = List.of();
	private String javadoc;
	
	@ToString.Exclude
	private String content;
	
	public JavaElement(String name, String definition, @NonNull List<String> annotations) {
		this(name, definition);
		this.annotations = annotations;
	}
	
	public JavaElement(String name, String definition, @NonNull List<String> annotations, String javadoc) {
		this(name, definition, annotations);
		this.javadoc = javadoc;
	}
	
}
