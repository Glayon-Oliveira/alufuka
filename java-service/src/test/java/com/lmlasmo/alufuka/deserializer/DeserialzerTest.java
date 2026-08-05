package com.lmlasmo.alufuka.deserializer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.ContextConfiguration;
import com.lmlasmo.alufuka.executor.Command;
import com.lmlasmo.alufuka.executor.ReaderCommand;

@TestInstance(Lifecycle.PER_CLASS)
class DeserialzerTest {
	
	@BeforeAll
	void setup() {
		ContextConfiguration.configure();
	}

	@ParameterizedTest
	@CsvSource({
		"file_path", "fp"
	})
	void shouldDeserializerReaderCommand(String alias) throws Exception {
	    String json = """
	            {
	                "type": "READER",
	                "%s": "/Test.java"
	            }
	            """.formatted(alias);
	    
	    assertInstanceOf(
	            ReaderCommand.class,
	            Context.objectMapper().readValue(json, Command.class)
	    );
	}
	
	@ParameterizedTest
	@CsvSource({
		"file_path", "fp"
	})
	void shouldDeserializerJavadocWriterCommand(String alias) throws Exception {
	    String json = """
	            {
	                "type": "READER",
	                "%s": "/Test.java",
	                "path": "Test.Test()",
	                "content": "Test javadoc"
	            }
	            """.formatted(alias);
	    
	    assertInstanceOf(
	            ReaderCommand.class,
	            Context.objectMapper().readValue(json, Command.class)
	    );
	}
	
}
