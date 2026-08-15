package com.lmlasmo.alufuka.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class JavadocWriterTest {

	@Test
	void shouldWriteJavadoc() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		JavadocWriter.write(
				getClass().getResourceAsStream("/Test.java"),
				byteStream,
				new JavadocTarget("Test", "One javadoc")
				);
		
		File file = new File("/tmp/Test.java");
		
		if(!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream fileStream = new FileOutputStream(file);
		byteStream.writeTo(fileStream);
	}
	
	@Test
	void shouldWriteJavadocOnRootType() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget("Test", "One javadoc")
	    );

	    assertThat(source)
	        .contains("/**\n"
	        		+ " * One javadoc\n"
	        		+ " */\n"
	        		+ "@Deprecated\n"
	        		+ "@SuppressWarnings(\"unused\")\n"
	        		+ "public abstract class Test");
	}
	
	@Test
	void shouldWriteJavadocOnField() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget("Test.value", "Value documentation")
	    );

	    assertThat(source)
	        .contains("\t/**\n"
	                + "\t * Value documentation\n"
	                + "\t */\n"
	                + "\tprivate final T value;\n");
	}

	@Test
	void shouldWriteJavadocOnMethod() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget("Test.convert", "Convert documentation", List.of("E", "List"))
	    );
	    
	    assertThat(source)
	        .contains("\t/**\n"
	                + "\t * Convert documentation\n"
	                + "\t */\n"
	                + "\tpublic static <E extends Number> List<E> convert(E value, List<? extends E> values)");
	}
	
	@Test
	void shouldWriteJavadocOnOverrideMethod() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	        		"Test.convert",
	        		"Convert documentation",
	        		List.of("E", "List", "String...")
	        		)
	    );
	    
	    assertThat(source)
	        .contains("\t/**\n"
	                + "\t * Convert documentation\n"
	                + "\t */\n"
	                + "\tpublic static <E extends Number> List<E> convert(E value, List<? extends E> values, String... names) throws IllegalArgumentException");
	}

	@Test
	void shouldWriteJavadocOnNestedMethod() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	            "Test.NestedClass.getValue",
	            "Get value documentation"
	        )
	    );

	    assertThat(source)
	        .contains("\t\t/**\n"
	                + "\t\t * Get value documentation\n"
	                + "\t\t */\n"
	                + "\t\tpublic int getValue()");
	}
	
	@Test
	void shouldWriteJavadocOnNestedConstructor() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	            "Test.NestedClass.NestedClass",
	            "Get value documentation"
	        )
	    );

	    assertThat(source)
	        .contains("\t\t/**\n"
	                + "\t\t * Get value documentation\n"
	                + "\t\t */\n"
	                + "\t\tpublic NestedClass()");
	}
	
	@Test
	void shouldWriteJavadocOnNestedOverrideConstructor() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	            "Test.NestedClass.NestedClass",
	            "Override constructor documentation",
	            List.of("int")
	        )
	    );

	    assertThat(source)
	        .contains("\t\t/**\n"
	                + "\t\t * Override constructor documentation\n"
	                + "\t\t */\n"
	                + "\t\tpublic NestedClass(int value)");
	}

	@Test
	void shouldWriteJavadocOnNestedField() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	            "Test.NestedClass.value",
	            "Nested value documentation"
	        )
	    );

	    assertThat(source)
	        .contains("\t\t/**\n"
	                + "\t\t * Nested value documentation\n"
	                + "\t\t */\n"
	                + "\t\tprivate int value;\n");
	}
	
	@Test
	void shouldWriteJavadocOnNestedAnnotation() throws IOException {
	    String source = writeJavadoc(
	        new JavadocTarget(
	            "Test.TestAnnotation",
	            "Nested value documentation"
	        )
	    );

	    assertThat(source)
	        .contains("\t/**\n"
	                + "\t * Nested value documentation\n"
	                + "\t */\n"
	                + "\t@Documented\n"
	                + "\t@Retention(RetentionPolicy.RUNTIME)\n"
	                + "\t@Target({TYPE, FIELD, METHOD})\n"
	                + "\tpublic @interface TestAnnotation ");
	    
	    source = writeJavadoc(
		        new JavadocTarget(
		            "Test.TestAnnotation.value",
		            "Nested value documentation"
		        )
		    );

	    assertThat(source)
	        .contains("\t\t/**\n"
	                + "\t\t * Nested value documentation\n"
	                + "\t\t */\n"
	                + "\t\tString value() default \"\";");
	}
	
	private String writeJavadoc(JavadocTarget target) throws IOException {
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    JavadocWriter.write(
	        getClass().getResourceAsStream("/Test.java"),
	        output,
	        target
	    );

	    return output.toString(StandardCharsets.UTF_8);
	}
	
}
