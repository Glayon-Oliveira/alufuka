package com.lmlasmo.alufuka.java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaSourceParser {

	public static JavaSource parse(InputStream in) throws IOException {
		StaticJavaParser.getParserConfiguration()
		.setLanguageLevel(LanguageLevel.JAVA_17)
		.setCharacterEncoding(StandardCharsets.UTF_8)
		.setLexicalPreservationEnabled(true);
		
		CompilationUnit compilationUnit = StaticJavaParser.parse(in);
		
		return parse(compilationUnit);
	}
	
	public static JavaSource parse(CompilationUnit compilationUnit) {
		String packageName = compilationUnit.getPackageDeclaration()
				.map(p -> p.getNameAsString())
				.orElse(null);
		
		List<String> imports = compilationUnit.getImports()
				.stream()
				.map(i -> {
					String name = i.getNameAsString();
					
					if(i.isStatic()) {
						name = "static " + name;
					}
					
					if(i.isAsterisk()) {
						name += ".*";
					}
					
					return name;
				})
				.toList();
		
		TypeDeclaration<?> declaration = compilationUnit.getType(0);
		
		return new JavaSource(packageName, imports, parseType(declaration), LexicalPreservingPrinter.print(compilationUnit));
	}
	
	private static JavaType parseType(TypeDeclaration<?> type) {
		if(type.isClassOrInterfaceDeclaration()) {
			return parseClassOrInterface(type.asClassOrInterfaceDeclaration());
		}else if(type.isEnumDeclaration()) {
			return parseEnum(type.asEnumDeclaration());
		}else if(type.isRecordDeclaration()) {
			return parseRecord(type.asRecordDeclaration());
		}else if(type.isAnnotationDeclaration()) {
			return parseAnnotation(type.asAnnotationDeclaration());
		}
		
		return null;
	}
	
	private static JavaType parseClassOrInterface(ClassOrInterfaceDeclaration classOrInterface) {
		List<JavaElement> members = classOrInterface.getMembers()
				.stream()
				.map(JavaSourceParser::parseMember)
				.filter(Objects::nonNull)
				.toList();
		
		JavaType java = new JavaType(classOrInterface.getNameAsString(), parseDefinition(classOrInterface));
		
		java.setAnnotations(parseAnnotations(classOrInterface.getAnnotations()));
		java.setMembers(members);
		java.setJavadoc(parseJavadoc(classOrInterface.getComment().orElse(null)));
		java.setContent(classOrInterface.toString());
		
		return java;
	}
	
	private static JavaType parseEnum(EnumDeclaration declaration) {
		List<JavaElement> members = declaration.getMembers()
				.stream()
				.map(JavaSourceParser::parseMember)
				.filter(Objects::nonNull)
				.toList();
		
		JavaType java = new JavaType(declaration.getNameAsString(), parseDefinition(declaration));
		
		java.setAnnotations(parseAnnotations(declaration.getAnnotations()));
		java.setMembers(members);
		java.setJavadoc(parseJavadoc(declaration.getComment().orElse(null)));
		java.setContent(declaration.toString());
		
		return java;
	}
	
	private static JavaType parseRecord(RecordDeclaration declaration) {
		List<JavaElement> members = declaration.getMembers()
				.stream()
				.map(JavaSourceParser::parseMember)
				.filter(Objects::nonNull)
				.toList();
		
		JavaType java = new JavaType(declaration.getNameAsString(), parseDefinition(declaration));
		
		java.setAnnotations(parseAnnotations(declaration.getAnnotations()));
		java.setMembers(members);
		java.setJavadoc(parseJavadoc(declaration.getComment().orElse(null)));
		java.setContent(declaration.toString());
		
		return java;
	}
	
	private static JavaType parseAnnotation(AnnotationDeclaration declaration) {
		List<JavaElement> members = declaration.getMembers()
				.stream()
				.map(JavaSourceParser::parseMember)
				.filter(Objects::nonNull)
				.toList();
		
		JavaType java = new JavaType(declaration.getNameAsString(), parseDefinition(declaration));
		java.setAnnotations(parseAnnotations(declaration.getAnnotations()));
		java.setMembers(members);
		java.setJavadoc(parseJavadoc(declaration.getComment().orElse(null)));
		java.setContent(declaration.toString());
		
		return java;
	}
	
	private static JavaElement parseMember(BodyDeclaration<?> declaration) {
		JavaElement member = null;
		
		if(declaration.isConstructorDeclaration()) {
			ConstructorDeclaration constructor = declaration.asConstructorDeclaration();
			
			member = new JavaConstructor(
					constructor.getNameAsString(),
					constructor.getDeclarationAsString(),
					parseParameterTypes(constructor.getParameters()),
					parseAnnotations(constructor.getAnnotations()),
					parseJavadoc(constructor.getComment().orElse(null))
					);
		}else if(declaration.isCompactConstructorDeclaration()) {
			CompactConstructorDeclaration constructor = declaration.asCompactConstructorDeclaration();
			
			member = new JavaConstructor(
					constructor.getNameAsString(),
					constructor.getDeclarationAsString(true, true, true),
					List.of(),
					parseAnnotations(constructor.getAnnotations()),
					parseJavadoc(constructor.getComment().orElse(null))
					);
		}else if(declaration.isFieldDeclaration()) {
			FieldDeclaration field = declaration.asFieldDeclaration();
			
			String names = field.getVariables().stream()
					.map(v -> v.getNameAsString())
					.collect(Collectors.joining(", "));
			
			member = new JavaField(
					names,
					parseDefinition(field),
					parseAnnotations(field.getAnnotations()),
					parseJavadoc(field.getComment().orElse(null))
					);
		}else if(declaration.isMethodDeclaration()) {
			MethodDeclaration method = declaration.asMethodDeclaration();
			
			member = parseMethod(method);
		}else if(declaration.isTypeDeclaration()) {
			TypeDeclaration<?> type = declaration.asTypeDeclaration();
			member = parseType(type);
		}else if(declaration.isAnnotationMemberDeclaration()) {
			AnnotationMemberDeclaration annotationMember = declaration.asAnnotationMemberDeclaration();
			
			member = new JavaElement(annotationMember.getNameAsString(), annotationMember.toString());
		}
		
		if(member != null) {
			member.setAnnotations(parseAnnotations(declaration.getAnnotations()));
			member.setContent(declaration.toString());
			member.setJavadoc(declaration.getComment().map(JavaSourceParser::parseJavadoc).orElse(null));
		}
		
		return member;
	}
	
	private static String parseTypes(List<ClassOrInterfaceType> types) {
		return types.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(", "));
	}
	
	private static String parseModifiers(List<Modifier> modifiers) {
		return join(modifiers, " ");
	}
	
	private static List<String> parseAnnotations(List<AnnotationExpr> annotations) {
		return annotations.stream()
				.map(an -> an.toString())
				.toList();
	}
	
	private static MethodType parseMethod(MethodDeclaration declaration) {
		return new MethodType(
				declaration.getNameAsString(),
				parseDefinition(declaration),
				parseParameterTypes(declaration.getParameters()),
				parseAnnotations(declaration.getAnnotations()),
				declaration.getJavadocComment()
					.map(JavaSourceParser::parseJavadoc)
					.orElse(null)
				);
	}
	
	private static String parseJavadoc(Comment comment) {
		if(comment == null) {
			return null;
		}
		
		if(comment.isJavadocComment()) {
			return normalizeJavadoc(comment.getContent());
		}else if(comment.isTraditionalJavadocComment()) {
			return normalizeJavadoc(comment.getContent());
		}
		
		return null;
	}
	
	private static String normalizeJavadoc(String javadoc) {
		return javadoc.lines()
				.filter(j -> !j.isBlank())
				.map(j -> {
					j = j.trim();
					
					if(j.startsWith("*")) {
						return j.replace("*", "").trim();
					}
					
					return j;
				})
				.collect(Collectors.joining("\n"));
	}
	
	private static String parseDefinition(Node node) {
		if(node instanceof ClassOrInterfaceDeclaration declaration) {
			return parseClassOrInterfaceDefinition(declaration);
		}else if(node instanceof EnumDeclaration declaration) {
			return parseEnumDefinition(declaration);
		}else if(node instanceof RecordDeclaration declaration) {
			return parseRecordDefinition(declaration);
		}else if(node instanceof MethodDeclaration declaration) {
			return parseMethodDefinition(declaration);
		}else if(node instanceof FieldDeclaration declaration) {
			return declaration.toString();
		}else if(node instanceof AnnotationDeclaration declaration) {
			return parseAnnotationDefinition(declaration);
		}
		
		return null;
	}
	
	private static String parseClassOrInterfaceDefinition(ClassOrInterfaceDeclaration declaration) {
		List<String> values = new ArrayList<>();
		
		String modifiers = join(declaration.getModifiers(), " ");
		String name = declaration.getNameAsString();
		String type = join(declaration.getTypeParameters(), ", ");
		String extended = join(declaration.getExtendedTypes(), ", ");
		String implemented = join(declaration.getImplementedTypes(), ", ");
		String permited = join(declaration.getPermittedTypes(), ", ");
		
		if(!modifiers.isEmpty()) {
			values.add(modifiers);
		}
		
		values.add(declaration.isInterface() ? "interface" : "class");
		
		if(!type.isEmpty()) {
			name += "<"+type+">"; 
		}
		
		values.add(name);
		
		if(!extended.isEmpty()) {
			values.add("extends " + extended);
		}
		
		if(!implemented.isEmpty()) {
			values.add("implements " + implemented);
		}
		
		if(!permited.isEmpty()) {
			values.add("permits " + permited);
		}
		
		return String.join(" ", values);
	}
	
	private static String parseEnumDefinition(EnumDeclaration declaration) {
		List<String> values = new ArrayList<>();
		
		String modifiers = parseModifiers(declaration.getModifiers());
		String name = declaration.getNameAsString();
		String implemented = parseTypes(declaration.getImplementedTypes());
		
		if(!modifiers.isEmpty()) {
			values.add(modifiers);
		}
		
		values.add("enum");
		values.add(name);
		
		if(!implemented.isEmpty()) {
			values.add("implements " + implemented);
		}
		
		return String.join(" ", values.toArray(String[]::new));
	}
	
	private static String parseRecordDefinition(RecordDeclaration declaration) {
		List<String> values = new ArrayList<>();
		
		String modifiers = parseModifiers(declaration.getModifiers());
		
		String name = declaration.getNameAsString();
		
		String implemented = parseTypes(declaration.getImplementedTypes());
		
		if(!modifiers.isEmpty()) {
			values.add(modifiers);
		}
		
		values.add("record");
		values.add(name);
		
		if(!implemented.isEmpty()) {
			values.add("implements " + implemented);
		}
		
		return String.join(" ", values);
	}
	
	private static String parseAnnotationDefinition(AnnotationDeclaration declaration) {
		String modifiers = parseModifiers(declaration.getModifiers());
		
		String name = declaration.getNameAsString();
		
		return String.join(" ", modifiers, "@interface", name);
	}
	
	private static String parseMethodDefinition(MethodDeclaration declaration) {
		List<String> values = new ArrayList<>();
		
		String parameters = join(declaration.getParameters(), ", ");
		String modifiers = parseModifiers(declaration.getModifiers());
		String typeParameters = join(declaration.getTypeParameters(), ", ");
		String type = declaration.getType().toString();
		String exceptions = join(declaration.getThrownExceptions(), ", ");
		
		if(!modifiers.isBlank()) {
			values.add(modifiers);
		}
		
		if(!typeParameters.isBlank()) {
			values.add("<"+typeParameters+">");
		}
		
		values.add(type);
		values.add(declaration.getNameAsString() + "("+parameters+")");
		
		if(!exceptions.isBlank()) {
			values.add("throws " + exceptions);
		}
		
		return String.join(" ", values);
	}
	
	static List<String> parseParameterTypes(List<Parameter> parameters) {
		return parameters.stream()
				.map(p -> parseType(p.getType(), p.isVarArgs()))
				.toList();
	}
	
	static String parseType(Type type, boolean isVarargs) {
		String strType = type.toString();
		
		if(strType.contains("<")) {
			strType = strType.substring(0, strType.lastIndexOf("<"))
					.trim();
		}
		
		if(isVarargs) {
			strType += "...";
		}
		
		return strType;
	}
	
	private static String join(Collection<?> values, String delimiter) {
		return values.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(delimiter));
	}
	
}
