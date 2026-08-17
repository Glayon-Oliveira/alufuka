package com.lmlasmo.alufuka.comunication;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.lmlasmo.alufuka.Context;
import com.lmlasmo.alufuka.executor.Result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Protocol implements Closeable {

	private DataOutputStream out;
	
	public void writeAndFlush(@NonNull Result result) throws IOException {
		String json = Context.objectMapper().writeValueAsString(result);
		
		writeAndFlush(json);
	}
	
	public void writeAndFlush(@NonNull String value) throws IOException {
		int size = value.getBytes(StandardCharsets.UTF_8).length;
		
		out.writeInt(size);
		out.writeBytes(value+"\n");
		out.flush();
	}
	
	public static Protocol newInstance(OutputStream out) {
		Protocol protocol = new Protocol();
		protocol.out = new DataOutputStream(out);
		
		return protocol;
	}
	
	public static boolean hasLine(byte[] bytes) {
		int size = bytes.length;

        if(size > Integer.BYTES) {
        	int payload = ByteBuffer.wrap(bytes, 0, Integer.BYTES).getInt();
        	int expected = Integer.BYTES + payload + 1;
        	
        	if(size == expected && bytes[size-1] == '\n') {
        		return true;
        	}
        }
        
        return false;
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
	
}
