package server.handler.aio;

import java.io.IOException;

import server.HttpRequest;
import server.HttpResponse;
import server.MIME;
import server.Status;

import com.google.common.base.Strings;
import com.google.common.io.Files;

public class StaticFileHandler implements AIOEventHandler<byte[]> {

    private HttpRequest req;
    private HttpResponse resp;
    
	private byte[] content;
	private String mime;
	
	public StaticFileHandler(HttpRequest req, HttpResponse resp) {
		this.req = req;
		this.resp = resp;
		
		String path = req.getPath();
        String extension = Files.getFileExtension(path);

        this.mime = MIME.getContentType(extension);
	}
	
	@Override
	public void handle() throws IOException {
		//System.out.println("handle static resource handler");
		
	    resp.setStatus(Status._200);
	    resp.setProtocol(req.getProtocol());
	    if (! Strings.isNullOrEmpty(mime)) {
	        resp.addHeader("Content-Type", mime);
	    }
	    resp.addHeader("Pragma", "no-cache");
	    resp.addHeader("Cache-Control", "no-store");
	    resp.setBody(content);
	    resp.send();
	}

	@Override
	public void setEvent(byte[] content) {
		this.content = content;
		//System.out.println(content.length);
		//System.out.println(Integer.MAX_VALUE + " " + Integer.MIN_VALUE);
	}

}
