package com.lmlasmo.alufuka.comunication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.executor.Executor;
import com.lmlasmo.alufuka.executor.FailureResult;
import com.lmlasmo.alufuka.executor.Result;
import com.lmlasmo.alufuka.executor.SuccessResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class StdReceptor implements Receptor {
	
	private boolean running = false;

	@Override
	public void start() throws Exception {
		System.err.println("Java service started");
		
		running = true;
		
		Executor executor = new Executor();
		
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(in);
		
		String next;
		
		while(running && (next = reader.readLine()) != null) {
			try {
				Result result = executor.execute(next.getBytes());
				
				String json = Context.objectMapper().writeValueAsString(result);
				
				System.out.println(json);
				System.out.flush();
			}catch(Exception e) {
				StringWriter swriter = new StringWriter();
				PrintWriter pwriter = new PrintWriter(swriter);
				e.printStackTrace(pwriter);
				e.printStackTrace(System.err);
				
				FailureResult result = new FailureResult(e.getMessage());
				result.setCause(swriter.toString());
				
				String json = Context.objectMapper().writeValueAsString(result);
				
				System.out.println(json);
				System.out.flush();
			}
		}
	}

	@Override
	public void stop() throws Exception {
		running = false;
	}
	
	@Getter
	@AllArgsConstructor
	private class InitializationResult implements Result {
		
		private Status status;
		private String message;
		
		@Override
		public SuccessResult asSuccess() {
			if(status == Status.SUCCESS) {
				return new SuccessResult("Initialization", message);
			}
			
			throw new UnsupportedOperationException();
		}
		
		@Override
		public FailureResult asFailure() {
			if(status == Status.FAILURE) {
				return new FailureResult(message);
			}
			
			throw new UnsupportedOperationException();	
		}
		
	}

}
