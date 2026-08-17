package com.lmlasmo.alufuka.comunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmlasmo.alufuka.executor.Executor;
import com.lmlasmo.alufuka.executor.Result;

public class SocketReceptor implements Receptor {
	
	private ExecutorService pool = Executors.newScheduledThreadPool(100);
	private boolean running = false;
	private int port;
	
	public SocketReceptor(int port) throws IOException {
		this.port = port;
	}

	@Override
	public void start() throws Exception {
		running = true;
				
		Executor executor = new Executor();
		
		System.out.println("Java service started");
		
		try(ServerSocket server = new ServerSocket(port)) {
			System.out.println("Java service listening on port " + port);
			
			while(running) {
				Socket socket = server.accept();
				
				pool.execute(() -> {
					try(BufferedReader reader = reader(socket);
							Protocol protocol = Protocol.newInstance(socket.getOutputStream())) {
						
						String next;
						
						while((next = reader.readLine()) != null) {
							System.out.println("Receive: " + next);
							Result result = executor.execute(next.getBytes());
							
							protocol.writeAndFlush(result);
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
	}
	
	private BufferedReader reader(Socket socket) throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public void stop() throws Exception {
		running = false;
		pool.shutdown();
		
		System.out.println("Java service terminated");
	}

}
