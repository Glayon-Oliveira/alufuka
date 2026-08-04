package com.lmlasmo.alufuka.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithMembers;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithParameters;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class JavadocWriter {
	
	private static final Pattern MATHOD_REGEX = Pattern.compile("^.*\\((.*?)\\)$");

	public static JavaSource write(InputStream in, OutputStream out, JavadocTarget target) throws IOException {
		StaticJavaParser.getParserConfiguration()
		.setLanguageLevel(LanguageLevel.JAVA_17)
		.setLexicalPreservationEnabled(true);
		
		CompilationUnit compilationUnit = StaticJavaParser.parse(in);
		
		Javadoc javadoc = (target.getContent() != null && !target.getContent().isBlank())
				? new Javadoc(JavadocDescription.parseText(target.getContent()))
				: null;
		
		setJavadoc(compilationUnit, target.getPath(), javadoc);
		
		String result = LexicalPreservingPrinter.print(compilationUnit);
		out.write(result.getBytes());
		
		return JavaSourceParser.parse(compilationUnit);
	}
	
	private static void setJavadoc(CompilationUnit unit, String path, Javadoc javadoc) {
		boolean success = setJavadoc(unit.getType(0), 0, path.split("\\."), javadoc);
		
		if(!success) {
			throw new RuntimeException(path + " not found.");
		}
	}
	
	private static boolean setJavadoc(Node node, int index, String[] pathParts, Javadoc javadoc) {
		String part = pathParts[index];
		String name = part.replaceAll("\\(.*?\\)", "").replace("[]", "");

		if(node instanceof NodeWithSimpleName<?> nwsn && nwsn.getNameAsString().equals(name)
				|| node instanceof NodeWithName<?> nwn && nwn.getNameAsString().equals(name)) {
			
			if(index < pathParts.length - 1) {
				if(node instanceof NodeWithMembers<?> nwm) {
					return setJavadoc(nwm.getMembers(), index + 1, pathParts, javadoc);
				}
			}else {
				Matcher matcher = MATHOD_REGEX.matcher(part);

				if(matcher.matches() && node instanceof NodeWithParameters<?> parametersNode) {
					String[] parameters = matcher.group(1).replaceAll("\\s", "").split(",");
					
					if(parameters.length == parametersNode.getParameters().size()) {
						boolean matched = true;
						
						for (int i = 0; i < parameters.length; i++) {
							String expected = parameters[i];
							Parameter parameter = parametersNode.getParameter(i);
							Type type = parameter.getType();
							
							String strType = type.toString()
									.replaceAll("<.*?>", "")
									.trim();
							
							if(expected.endsWith("[]")) {
								if(!type.isArrayType() && !parameter.isVarArgs()) {
									matched = false;
									break;
								}

								if(parameter.isVarArgs()) {
									expected = expected.substring(0, expected.lastIndexOf("[]"));
								}

								while(type.isArrayType() && expected.endsWith("[]")) {
									expected = expected.substring(0, expected.lastIndexOf("[]"));

									type = type.asArrayType().getComponentType();
								}

								if(type.isArrayType()) {
									matched = false;
									break;
								}
								
								strType = type.toString()
										.replaceAll("<.*?>", "")
										.trim();

								if(!strType.equals(expected)) {
									matched = false;
									break;
								}
							}else if(parameter.isVarArgs() || type.isArrayType() || !strType.equals(expected)) {
								matched = false;
								break;
							}
						}

						if(matched) {
							setJavadoc(node, javadoc);
							return true;
						}
					}
				}else if(!matcher.matches()) {
					setJavadoc(node, javadoc);
					return true;
				}
			}
		}

		return false;
	}
	
	private static boolean setJavadoc(List<? extends Node> nodes, int index, String[] pathParts, Javadoc javadoc) {
		String name = pathParts[index].replaceAll("\\(.*?\\)", "").replace("[]", "");
		
		for(Node node: nodes) {
			if(node instanceof NodeWithVariables<?> nwv) {
				boolean matched = nwv.getVariables().stream()
						.anyMatch(v -> v.getNameAsString().equals(name));
				
				if(matched) {
					setJavadoc(node, javadoc);
					return true;
				}
			}else if(node instanceof NodeWithName<?> nwn && nwn.getNameAsString().equals(name)
					|| node instanceof NodeWithSimpleName<?> nwsn && nwsn.getNameAsString().equals(name)) {
				
				if(setJavadoc(node, index, pathParts, javadoc)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static void setJavadoc(Node node, Javadoc javadoc) {
		String identation = getIndentation(node);
		
		if(javadoc == null || javadoc.toComment().getContent().isBlank()) {
			node.removeComment();
		}else if(node instanceof NodeWithJavadoc<?> nwj) {
			nwj.setJavadocComment(identation, javadoc);
		}else {
			node.setComment(javadoc.toComment(identation));
		}
	}
	
	private static String getIndentation(Node node) {
	    Range range = node.getRange()
	        .orElseThrow();

	    String source = node.findCompilationUnit()
	        .orElseThrow()
	        .getTokenRange()
	        .map(TokenRange::toString)
	        .orElseThrow();

	    int begin = range.begin.line;
	    int column = range.begin.column;

	    String line = source.lines()
	        .skip(begin - 1L)
	        .findFirst()
	        .orElseThrow();

	    return line.substring(0, column - 1);
	}
	
}
