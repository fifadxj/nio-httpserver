package server.handler.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import server.AIO;
import server.HttpRequest;
import server.HttpRequestBuilder;
import server.HttpResponse;
import server.NioHttpServer;
import server.Status;
import server.handler.aio.DirectoryHandler;
import server.handler.aio.StaticFileHandler;

import com.google.common.base.Strings;

public class ReadSocketHandler implements NIOEventHandler {
	private SelectionKey socketChannelSelectionKey;
	private HttpRequestBuilder builder;
	private ByteBuffer input = ByteBuffer.allocate(64);

	public ReadSocketHandler(SelectionKey sk) throws IOException {
		this.socketChannelSelectionKey = sk;
		this.builder  = new HttpRequestBuilder(sk);
	}

	
	private void process(HttpRequest req, HttpResponse resp) throws FileNotFoundException {
	    if (isStaticResource(req)) {
	        File file = new File(NioHttpServer.docRoot() + req.getPath());
	        if (! file.exists()) {
	            resp.setStatus(Status._404);
	            resp.setProtocol(req.getProtocol());
	            resp.send();
	            return;
	        } else if (file.isDirectory()) {
	            AIO.readDirectory(file, new DirectoryHandler(req, resp));
	        } else {
	            AIO.readFile(file, new StaticFileHandler(req, resp));
	        }
	    }
	    else {
	        //TODO java/php/python...
	    }
	}

	private boolean isStaticResource(HttpRequest req) {
        // TODO Auto-generated method stub
        return true;
    }


    @Override
    public void handle() throws IOException {
		input.clear();
		SocketChannel socket = (SocketChannel)socketChannelSelectionKey.channel();
		socket.read(input);
		
		input.flip();
		byte[] segmentBytes = new byte[input.limit()];
		input.get(segmentBytes);
		
		builder.appendSegment(segmentBytes);
		//System.out.println("-----");
		//System.out.println(segment);
		//System.out.println("-----");
		
		
		if (builder.isInputComplete()) {
		    HttpRequest req = builder.build();

			HttpResponse resp = new HttpResponse();
			resp.setSelectionKey(socketChannelSelectionKey);
			process(req, resp);
		}
    }
}