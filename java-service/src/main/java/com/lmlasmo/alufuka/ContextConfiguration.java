package com.lmlasmo.alufuka;

import com.lmlasmo.alufuka.executor.Command;
import com.lmlasmo.alufuka.executor.JavadocWriterCommandExecutor;
import com.lmlasmo.alufuka.executor.ReaderCommandExecutor;
import com.lmlasmo.alufuka.serialization.CommandDeserializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContextConfiguration {

	public static void configure() {
		Context.register(new ReaderCommandExecutor());
		Context.register(new JavadocWriterCommandExecutor());
		
		Context.configureModule(sm -> sm.addDeserializer(Command.class, new CommandDeserializer()));
	}
	
}
