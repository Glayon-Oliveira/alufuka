package com.lmlasmo.alufuka.executor;

import java.io.FileInputStream;
import java.io.IOException;

import com.lmlasmo.alufuka.java.JavaSourceParser;

public class ReaderCommandExecutor implements CommandExecutor<ReaderCommand> {
	
	@Override
	public Class<ReaderCommand> getCommandType() {
		return ReaderCommand.class;
	}
	
	@Override
	public CommandType getTargetType() {
		return CommandType.READER;
	}

	@Override
	public Result execute(ReaderCommand command) throws IOException {
		String filePath = command.getFilePath();
		
		try(FileInputStream in = new FileInputStream(filePath)) {
			SuccessResult result = new SuccessResult(command.getType().name(), JavaSourceParser.parse(in));
			result.getMetadata().put("file_path", filePath);
			
			return result;
		}
	}

}
