package server.handler.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.HttpResponse;

public class WriteSocketHandler implements NIOEventHandler {
	private HttpResponse resp;
	private ByteBuffer output;
	
	public WriteSocketHandler(HttpResponse resp, byte[] content) {
		this.resp = resp;
		this.output = ByteBuffer.wrap(content);
	}

	boolean outputIsComplete() {
		return ! output.hasRemaining();
	}

	@Override
    public void handle() throws IOException {
		//System.out.println("write...");
	    try {
	        resp.getSocket().write(output);
	    }catch (IOException e) {
	        resp.getSocket().close();
	        throw e;
	    }
		if (outputIsComplete()) {
		    resp.getSocket().close();
		    //resp.getSelectionKey().cancel();
		} else {
			//System.out.println(i + "=======" + last + " - " + output.remaining() + " = " + (last - output.remaining()));
			//last = output.remaining();
		}
    }
}