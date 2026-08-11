package com.lmlasmo.alufuka.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class JavaSourceParserTest {
	
	JavaSource source;
	JavaType javaType;
	
	@BeforeAll
	void setup() throws IOException {
		source = JavaSourceParser.parse(getClass().getResourceAsStream("/Test.java"));
		javaType = source.getJavaType();
	}
	
	@Test
	void shorldParsePackageAndNameAndDefinitionSource() {
		JavaType javaType = source.getJavaType();
		
		assertEquals("com.dev.top", source.getPackageName());
		
		assertEquals("Test", javaType.getName());
		assertEquals(
			"public abstract class Test<T extends Number & Comparable<T>> extends BaseTest implements Serializable, Comparable<Test<T>>",
			javaType.getDefinition()
			);
	}
	
	@Test
	void shouldParseImports() {
		assertThatCollection(source.getImports())
		.hasSize(7)
		.contains(
			"static java.util.Calendar.JANUARY",
			"java.io.Serializable",
			"java.util.*",
			"java.util.Comparator",
			"java.util.List",
			"java.util.Map",
			"java.util.Objects"
		);
	}
	
	@Test
	void shouldParseClassJavadoc() {
		assertEquals(
			"...",
			javaType.getJavadoc()
		);
	}
	
	@Test
	void shouldParseClassAnnotations() {
		assertThatCollection(javaType.getAnnotations())
		.hasSize(2)
		.contains("@Deprecated", "@SuppressWarnings(\"unused\")");
	}
	
	@Test 
	void shouldParseFields() throws IOException {
	    assertThatCollection(javaType.getMembers())
	    .filteredOn(JavaField.class::isInstance)
	    .hasSize(6);

	    assertMembersDefinition(
	    	"serialVersionUID",
	    	"private static final long serialVersionUID = 1L;"
	    	);
	    
	    assertMembersDefinition(
	    	"value",
	    	"private final T value;"
	    	);
	    
	    assertMembersDefinition(
	    	"name",
	    	"protected String name;"
	    	);
	    
	    assertMembersDefinition(
	    	"counter",
	    	"public static int counter;"
	    	);
	    
	    assertMembersDefinition(
	    	"active",
	    	"volatile boolean active;"
	    	);

	    assertMembersDefinition(
	    	"cache",
	    	"transient Object cache;"
	    	);
	}

	@Test
	void shouldParseConstructors() throws IOException {
		assertMembersDefinition(
	    	"Test",
	    	"public Test(T value, String name)",
            "protected Test(T value) throws IllegalArgumentException"
	    	);
	}
	
	@Test
	void shouldParseArgumentTypesOfConstructors() throws IOException {
		assertArgumensTypes("Test", "T", "String");
		assertArgumensTypes("Test", "T");
	}

	@Test
	void shouldParseMethods() throws IOException {
	    assertThat(javaType.getMembers())
	        .filteredOn(member -> member instanceof MethodType)
	        .extracting(JavaElement::getName)
	        .hasSize(13)
	        .contains(
	            "calculate",
	            "validate",
	            "convert",
	            "compare",
	            "compareTo",
	            "getValue",
	            "getName",
	            "equals",
	            "hashCode",
	            "toString"
	        );
	}

	@Test
	void shouldParseMethodDefinitions() throws IOException {
		assertMembersDefinition(
	    	"calculate",
	    	"public abstract T calculate(T value)"
	    	);
		
		assertMembersDefinition(
	    	"validate",
	    	"protected final void validate(T value)"
	    	);
		
		assertMembersDefinition(
	    	"convert",
	    	"public static <E extends Number> List<E> convert(E value, List<? extends E> values)",
	    	"public static <E extends Number> List<E> convert(E value, List<? extends E> values, String... names) throws IllegalArgumentException"
	    	);
		
		assertMembersDefinition(
	    	"compare",
	    	"public static int compare(String[] vs1, String[] vs2)",
	    	"public static int compare(String vs1, String vs2)"
	    	);
	}
	
	@Test
	void shouldParseArgumentTypesOfMethod() throws IOException {
		assertArgumensTypes("calculate", "T");
		assertArgumensTypes("validate", "T");
		
		assertArgumensTypes("convert", "E", "List");
		assertArgumensTypes("convert", "E", "List", "String...");
		
		assertArgumensTypes("compare", "String[]", "String[]");
		assertArgumensTypes("compare", "String", "String");
	}

	@Test
	void shouldParseMethodAnnotations() throws IOException {
	    assertThat(findMember(javaType, "calculate").getAnnotations())
	        .isEmpty();

	    assertThat(findMember(javaType, "compareTo").getAnnotations())
	        .containsExactly("@Override");

	    assertThat(findMember(javaType, "equals").getAnnotations())
	        .containsExactly("@Override");

	    assertThat(findMember(javaType, "hashCode").getAnnotations())
	        .containsExactly("@Override");

	    assertThat(findMember(javaType, "toString").getAnnotations())
	        .containsExactly("@Override");
	}
	
	@Test
	void shouldParseMethodJavadoc() throws IOException {
	    assertEquals(
    		"...\n\n@param ...\n@return ...",
    		findMember(javaType, "calculate").getJavadoc()
    		);
	}

	@Test
	void shouldParseNestedEnum() throws IOException {
	    JavaType status = findType(javaType, "Status");

	    assertEquals("Status", status.getName());
	    assertEquals("public enum Status", status.getDefinition());

	    assertThat(status.getMembers())
	        .isEmpty();
	}

	@Test
	void shouldParseNestedRecord() throws IOException {
	    JavaType result = findType(javaType, "Result");

	    assertEquals("Result", result.getName());
	    assertEquals("public record Result", result.getDefinition());
	    
	    assertMembersDefinition(
	    	result,
	    	null,
	    	"public Result()",
	    	"public static <E> Result<E> success(E value)",
	    	"public static <E> Result<E> success(E value, boolean success)"
	    	);
	}

	@Test
	void shouldParseNestedInterface() throws IOException {
	    JavaType factory = findType(javaType, "Factory");

	    assertEquals("Factory", factory.getName());
	    assertEquals(
	        "public interface Factory<E>",
	        factory.getDefinition()
	    );
	    
	    assertMembersDefinition(
	    	factory,
	    	null,
	    	"E create()",
	    	"default E createDefault()"
	    	);
	    
	    assertMemberNames(
	    	factory,
	    	2,
	    	"create",
            "createDefault");
	}

	@Test
	void shouldParseNestedClass() throws IOException {
	    JavaType nested = findType(javaType, "NestedClass");

	    assertEquals(
	        "protected static class NestedClass",
	        nested.getDefinition()
	    );
	    
	    assertMemberNames(
	    	nested,
	    	4,
	    	"value",
	    	"NestedClass",
	    	"getValue");
	    
	    assertMembersDefinition(
	    	nested,
	    	null,
	    	"private int value;",
        	"public NestedClass()",
	    	"public NestedClass(int value)",
        	"public int getValue()"
	    	);
	}

	@Test
	void shouldParseEmptyNestedClass() throws IOException {
	    JavaType nested = findType(javaType, "PrivateNestedClass");

	    assertEquals(
	        "private static final class PrivateNestedClass",
	        nested.getDefinition()
	    );

	    assertThat(nested.getMembers())
	        .isEmpty();
	}
	
	private void assertMemberNames(JavaType javaType, int size, String... containing) {
		assertThatCollection(javaType.getMembers())
		.hasSize(size)
	    .map(JavaElement::getName)
	    .containsOnly(containing);
	}
	
	private void assertMembersDefinition(String name, String... containing) {
		assertMembersDefinition(javaType, name, containing);
	}
	
	private void assertMembersDefinition(JavaType javaType, String name, String... containing) {
		assertThatCollection(name != null ? findMembers(javaType, name) : javaType.getMembers())
	    .map(JavaElement::getDefinition)
	    .containsExactly(containing);
	}
	
	private void assertArgumensTypes(String name, String... types) {
		assertArgumensTypes(javaType, name, types);
	}
	
	private void assertArgumensTypes(JavaType javaType, String name, String... types) {
		assertThatCollection(findMembers(javaType, name))
		.filteredOn(JavaElementWithArguments.class::isInstance)
		.map(JavaElementWithArguments.class::cast)
		.satisfiesOnlyOnce(a -> {
			assertThatCollection(a.getArgumentTypes())
            .hasSize(types.length)
            .containsExactly(types);
		});
	}
	
	private JavaElement findMember(JavaType type, String name) {
		return type.getMembers()
				.stream()
				.filter(member -> member.getName().equals(name))
				.findFirst()
				.orElseThrow();
	}

	private List<JavaElement> findMembers(JavaType type, String name) {
	    return type.getMembers().stream()
	        .filter(member -> member.getName().equals(name))
	        .toList();
	}

	private JavaType findType(JavaType type, String name) {
	    return (JavaType) findMember(type, name);
	}
	
}
