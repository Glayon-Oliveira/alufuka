package com.lmlasmo.alufuka.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.ContextConfiguration;
import com.lmlasmo.alufuka.ContextTool;
import com.lmlasmo.alufuka.executor.Result.Status;

@TestInstance(Lifecycle.PER_CLASS)
class ExceptionHandlerExecutorTest {
	
	@AfterEach
	void tearDown() {
		ContextTool.clear();
	}
	
    @Test
    void shouldSelectMostSpecificHandler() {
        ExceptionHandler<Exception> exceptionHandler = new TestExceptionHandler<>(Exception.class, "exception");
        ExceptionHandler<IOException> ioHandler = new TestExceptionHandler<>(IOException.class, "io");
        ExceptionHandler<FileNotFoundException> fileHandler = new TestExceptionHandler<>(FileNotFoundException.class, "file");
        
        ContextConfiguration.configure();
        Context.register(exceptionHandler);
        Context.register(ioHandler);
        Context.register(fileHandler);
        
        ExceptionHandlerExecutor executor = new ExceptionHandlerExecutor();

        Result result = executor.handle(new FileNotFoundException());
        
        assertEquals(Status.FAILURE, result.getStatus());
        assertEquals("file", result.asFailure().getMessage());
    }

    @Test
    void shouldSelectNearestAvailableHandler() {
        ExceptionHandler<Exception> exceptionHandler = new TestExceptionHandler<>(Exception.class, "exception");
        ExceptionHandler<IOException> ioHandler = new TestExceptionHandler<>(IOException.class, "io");

        ContextConfiguration.configure();
        Context.register(exceptionHandler);
        Context.register(ioHandler);

        ExceptionHandlerExecutor executor = new ExceptionHandlerExecutor();

        Result result = executor.handle(new FileNotFoundException());

        assertEquals(Status.FAILURE, result.getStatus());
        assertEquals("io", result.asFailure().getMessage());
    }

    @Test
    void shouldHandleExactExceptionType() {
        ExceptionHandler<IOException> handler = new TestExceptionHandler<>(IOException.class, "io");
        
        ContextConfiguration.configure();
        Context.register(handler);

        ExceptionHandlerExecutor executor = new ExceptionHandlerExecutor();

        Result result = executor.handle(new IOException());

        assertEquals(Status.FAILURE, result.getStatus());
        assertEquals("io", result.asFailure().getMessage());
    }

    @Test
    void shouldUseDefaultHandlerWhenNoHandlerMatches() {
        ExceptionHandlerExecutor executor = new ExceptionHandlerExecutor();

        Result result = executor.handle(new IOException("def.io"));

        assertEquals(Status.FAILURE, result.getStatus());
        assertEquals("def.io", result.asFailure().getMessage());
    }

    private static class TestExceptionHandler<T extends Throwable>
            implements ExceptionHandler<T> {

        private final Class<T> targetException;
        private final String message;

        private TestExceptionHandler(
                Class<T> targetException,
                String message
        ) {
            this.targetException = targetException;
            this.message = message;
        }

        @Override
        public Class<T> getTargetException() {
            return targetException;
        }

        @Override
        public FailureResult handle(T th) {
            return new FailureResult(message);
        }
    }
}
