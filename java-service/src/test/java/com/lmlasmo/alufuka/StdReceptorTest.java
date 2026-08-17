package com.lmlasmo.alufuka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lmlasmo.alufuka.comunication.Protocol;
import com.lmlasmo.alufuka.comunication.StdReceptor;

class StdReceptorTest {
	
	private Path testFile;

    private InputStream originalIn;
    private PrintStream originalOut;

    private PipedInputStream input;
    private PipedOutputStream inputWriter;

    private ByteArrayOutputStream output;
    private PrintStream outputWriter;

    private Thread receptorThread;

    @BeforeEach
    void setUp() throws Exception {
        originalIn = System.in;
        originalOut = System.out;

        input = new PipedInputStream();
        inputWriter = new PipedOutputStream(input);

        output = new ByteArrayOutputStream();
        outputWriter = new PrintStream(output, true, StandardCharsets.UTF_8);

        System.setIn(input);
        System.setOut(outputWriter);

        ContextConfiguration.configure();
        Context.setRECEPTOR(new StdReceptor());
        
        testFile = Path.of("target", "executor-test", "Test.java");

        Files.createDirectories(testFile.getParent());
        
        Files.copy(
        	getClass().getResourceAsStream("/Test.java"),
        	testFile,
        	StandardCopyOption.REPLACE_EXISTING);
        
        ContextConfiguration.configure();
    }

    @AfterEach
    void tearDown() throws Exception {
    	Context.receptor().stop();

        if(inputWriter != null) {
            inputWriter.close();
        }

        if(input != null) {
            input.close();
        }

        if(receptorThread != null) {
            receptorThread.interrupt();
            receptorThread.join(1000);
        }

        System.setIn(originalIn);
        System.setOut(originalOut);
        
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(testFile.getParent());
    }

    @Test
    void shouldContinueReceivingAfterProcessingMessage() throws Exception {
        receptorThread = new Thread(() -> {
            try {
                Context.receptor().start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        receptorThread.start();

        String readerCommand = """
        		{
        			"type": "READER",
        			"file_path": "%s"
        		}
        		"""
        		.replace("\n", "")
        		.formatted(testFile.toString())
        		+ "\n";
        
        String writerCommand = """
        		{
        			"type": "JAVADOC_WRITER",
        			"file_path": "%s",
        			"path": "Test.Test",
        			"content": "Javadoc",
        			"types": ["T"]
        		}
        		"""
        		.replace("\n", "")
        		.formatted(testFile.toString())
        		+ "\n";

        inputWriter.write(readerCommand.getBytes(StandardCharsets.UTF_8));
        inputWriter.flush();

        waitForOutput();

        assertTrue(receptorThread.isAlive());
        assertTrue(((StdReceptor) Context.receptor()).isRunning());

        String firstOutput = output.toString(StandardCharsets.UTF_8);

        assertThat(firstOutput)
        .isNotBlank()
        .contains("\"status\":\"SUCCESS\"");
        
        output.reset();

        inputWriter.write(writerCommand.getBytes(StandardCharsets.UTF_8));
        inputWriter.flush();

        waitForOutput();

        String secondOutput = output.toString(StandardCharsets.UTF_8);
        
        assertThat(secondOutput)
        .isNotBlank()
        .contains("\"status\":\"SUCCESS\"");

        assertTrue(secondOutput.length() > firstOutput.length());
        assertTrue(receptorThread.isAlive());
        assertTrue(((StdReceptor) Context.receptor()).isRunning());
        
        output.reset();
    }

    private void waitForOutput() throws InterruptedException {
    	long timeout = System.currentTimeMillis() + 10000;
    	boolean expires = false;
    	
        while(!expires) {
            Thread.sleep(10);
            expires = System.currentTimeMillis() >= timeout;
            
            if(Protocol.hasLine(output.toByteArray())) {
            	return;
            }
        }
        
        if(expires) {
        	throw new RuntimeException(output.toString(StandardCharsets.UTF_8));
        }
    }

}