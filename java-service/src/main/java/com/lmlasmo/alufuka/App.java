package com.lmlasmo.alufuka;

import com.lmlasmo.alufuka.comunication.SocketReceptor;

public class App {

	public static void main(String[] args) throws Exception {
		ContextConfiguration.configure();
		Context.setRECEPTOR(new SocketReceptor(5130));
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Context.receptor().stop();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}));
		
		Context.receptor().start();
	}

}
