package com.lmlasmo.alufuka.java;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class JavaSource {

	private String packageName;
	@NonNull private List<String> imports = List.of();
	@NonNull private JavaType javaType;
	
	private String content;
	
}
