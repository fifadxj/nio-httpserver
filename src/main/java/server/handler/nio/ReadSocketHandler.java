package server.handler.nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.HttpRequest;
import server.HttpRequestBuilder;
import server.HttpResponse;
import server.MIME;
import server.NioHttpServer;
import server.Status;
import server.aio.AIO;
import server.handler.aio.AIOEventHandler;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class ReadSocketHandler implements NIOEventHandler {
    private static Logger logger = LoggerFactory.getLogger(ReadSocketHandler.class);
    private static Logger accessLogger = LoggerFactory.getLogger("accessLog");
    
	private SelectionKey socketChannelSelectionKey;
	private HttpRequestBuilder builder;
	private ByteBuffer input = ByteBuffer.allocate(1024);
	private String id;

	public ReadSocketHandler(SelectionKey sk) throws IOException {
	    this.id = UUID.randomUUID().toString();
		this.socketChannelSelectionKey = sk;
		this.builder  = new HttpRequestBuilder(sk);
	}

	
	private void process(final HttpRequest req, final HttpResponse resp) throws IOException {
	    if (isStaticResource(req)) {
	        String filename = URLDecoder.decode(req.getPath(), Charsets.UTF_8.name());
	        File file = new File(NioHttpServer.docRoot() + filename);
	        if (! file.exists()) {
	            resp.setStatus(Status._404);
	            resp.setProtocol(req.getProtocol());
	            resp.setBody("<html><body>404 Not Found</body></html>".getBytes(Charsets.UTF_8));
	            resp.send();
	            return;
	        } else if (file.isDirectory()) {
	            AIO.readDirectory(file, new AIOEventHandler() {

	                private List<File> children;
	                
	                @Override
	                public void handle() throws IOException {
	                    resp.setStatus(Status._200);
	                    resp.setProtocol(req.getProtocol());
	                    resp.addHeader("Content-Type", "text/html;charset=UTF-8");
	                    resp.addHeader("Pragma", "no-cache");
	                    resp.addHeader("Cache-Control", "no-store");
	                    String body = generateHtml();
	                    resp.setBody(body.getBytes(Charsets.UTF_8));
	                    resp.send();
	                }
	                
	                private String getRelativePath(File file, File folder) {
	                    String filePath = file.getAbsolutePath();
	                    String folderPath = folder.getAbsolutePath();
	                    if (filePath.startsWith(folderPath)) {
	                        return filePath.substring(folderPath.length());
	                    } else {
	                        return null;
	                    }
	                }

	                private String generateHtml() {
	                    StringBuilder sb = new StringBuilder();
	                    sb.append("<html><body>");
	                    for (File child : children) {
	                        String relativePath = getRelativePath(child, new File(NioHttpServer.docRoot()));
	                        String text = child.getName();
	                        if (child.isDirectory()) {
	                            text = "+ " + text;
	                        } else {
	                            text = "- " + text;
	                        }
	                        String link = "<p><a href=\"" + relativePath + "\">" + text + "</a></p>\n";
	                        sb.append(link);
	                    }
	                    sb.append("</body></html>");
	                    return sb.toString();
	                }

	                @Override
	                public void setEvent(Object event) {
	                    this.children = (List<File>) event;
	                }
                });
	        } else {
	            AIO.readFile(file, new AIOEventHandler() {
                    private byte[] content;
                    
                    @Override
                    public void setEvent(Object event) {
                        this.content = (byte[]) event;
                    }
                    
                    @Override
                    public void handle() throws IOException {
                        String path = req.getPath();
                        String extension = Files.getFileExtension(path);

                        String mime = MIME.getContentType(extension);
                        
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
                });
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
		int n = socket.read(input);
		
		if (n == -1) {// if EOS is reached, then close the socket
		    socket.close();
		    return;
		}
		
		input.flip();
		byte[] segmentBytes = new byte[input.limit()];
		input.get(segmentBytes);
		
		if (logger.isDebugEnabled()) {
    		String text = new String(segmentBytes, Charsets.UTF_8);
    		logger.debug("[" + id + "] received request segment: " + text);
		}
		builder.appendSegment(segmentBytes);
		
		
		if (! builder.ignoreRestInput()/*every socket only accept one request*/ && builder.isInputComplete()) {
		    HttpRequest req = builder.build();
		    if (accessLogger.isInfoEnabled()) {
		        InetSocketAddress address = (InetSocketAddress) socket.socket().getRemoteSocketAddress();
		        accessLogger.info("[" + req.id() + "] " + address.getAddress().getHostAddress() + " " + req.getMethod() + " " + req.getPath());
		    }

			HttpResponse resp = new HttpResponse();
			resp.setSelectionKey(socketChannelSelectionKey);
			process(req, resp);
		}
    }
}