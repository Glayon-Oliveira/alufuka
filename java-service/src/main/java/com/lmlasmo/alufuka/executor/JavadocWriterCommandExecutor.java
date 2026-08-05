package com.lmlasmo.alufuka.executor;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.lmlasmo.alufuka.java.JavadocWriter;

public class JavadocWriterCommandExecutor implements CommandExecutor<JavadocWriterCommand> {
	
	@Override
	public Class<JavadocWriterCommand> getCommandType() {
		return JavadocWriterCommand.class;
	}

	@Override
	public CommandType getTargetType() {
		return CommandType.JAVADOC_WRITER;
	}

	@Override
	public Result execute(JavadocWriterCommand command) throws Exception {
		String filePath = command.getFilePath();
		
		byte[] bytes = null;
		
		try(FileInputStream in = new FileInputStream(filePath)) {
			bytes = in.readAllBytes();
		}
		
		try(ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
				FileOutputStream out = new FileOutputStream(filePath)) {
			
			SuccessResult result = new SuccessResult(command.getType().name(), JavadocWriter.write(bain, out, command));
			result.getMetadata().put("file_path", filePath);
			
			if(command.getContent() == null || command.getContent().isBlank()) {
				result.setMessage("Javadoc is removed");
			}else {
				result.setMessage("Javadoc is inserted");
				result.getMetadata().put("content", command.getContent());
			}
			
			return result;
		}
	}

}
