package server.handler.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

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
	    try {
	        resp.getSocket().write(output);
	    }catch (IOException e) {
	        resp.getSocket().close();
	        throw e;
	    }
		if (outputIsComplete()) {
		    resp.getSocket().close();
		    //resp.getSelectionKey().cancel();
		}
    }
}