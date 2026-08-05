package com.lmlasmo.alufuka.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.ContextConfiguration;
import com.lmlasmo.alufuka.executor.Result.Status;
import com.lmlasmo.alufuka.java.JavaSource;

@TestInstance(Lifecycle.PER_CLASS)
class ExecutorTest {
	
	private Executor executor = new Executor();
    private Path testFile;

    @BeforeEach
    void setup() throws IOException {
        testFile = Path.of("target", "executor-test", "Test.java");

        Files.createDirectories(testFile.getParent());
        
        Files.copy(
        	getClass().getResourceAsStream("/Test.java"),
        	testFile,
        	StandardCopyOption.REPLACE_EXISTING);
        
        ContextConfiguration.configure();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(testFile.getParent());
    }
    
    @Test
    void shouldExecuteCommandFromInputStream() throws Exception {
        ReaderCommand command = readerCommand();
        
        byte[] bytes = Context.objectMapper().writeValueAsBytes(command);
        
        try(ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
        	Result result = executor.execute(in);
        	assertNotNull(result);
        	assertEquals(Status.SUCCESS, result.getStatus());
        	assertInstanceOf(JavaSource.class, result.asSuccess().getBody());
        }
    }

    @Test
    void shouldExecuteCommandFromBytes() throws Exception {
        ReaderCommand command = readerCommand();
        
        byte[] bytes = Context.objectMapper().writeValueAsBytes(command);
        
        Result result = executor.execute(bytes);
        
    	assertNotNull(result);
    	assertEquals(Status.SUCCESS, result.getStatus());
    	assertInstanceOf(JavaSource.class, result.asSuccess().getBody());
    }

    @Test
    void shouldHandleExceptionWhileReadingCommand() throws Exception {
    	ReaderCommand command = readerCommand("/"+UUID.randomUUID());
    	
    	Result result = executor.execute(command);
    	
    	assertNotNull(result);
    	assertEquals(Status.FAILURE, result.getStatus());
    }

    @Test
    void shouldExecuteJavadocWriterCommand() throws Exception {
        JavadocWriterCommand command = javadocWriterCommand();

        Result result = executor.execute(command);
        
        assertNotNull(result);
        assertEquals(Status.SUCCESS, result.getStatus());
        assertTrue(Files.exists(testFile));
        assertInstanceOf(JavaSource.class, result.asSuccess().getBody());
    }
    
    ReaderCommand readerCommand() {
    	return readerCommand(testFile.toString());
    }
    
    ReaderCommand readerCommand(String filePath) {
    	ReaderCommand command = new ReaderCommand();
    	command.setFilePath(filePath);
    	
    	return command;
    }
    
    JavadocWriterCommand javadocWriterCommand() {
    	return javadocWriterCommand(testFile.toString());
    }
    
    JavadocWriterCommand javadocWriterCommand(String filePath) {
    	return new JavadocWriterCommand(filePath, "Test.Test(T)", "Test javadoc");
    }
    
}
