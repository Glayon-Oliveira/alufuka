package com.lmlasmo.alufuka;

public class App {

	public static void main(String[] args) throws Exception {
		ContextConfiguration.configure();
		
		Context.receptor().start();
	}

}
