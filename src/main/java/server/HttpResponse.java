package server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import server.handler.nio.WriteSocketHandler;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

public class HttpResponse {
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private Map<String, String> cookies = new LinkedHashMap<String, String>();
    private Status status;
    private byte[] body;
    private String protocol;
    private SelectionKey sk;
    
    public void send() {
        String startLine = buildStartLine(protocol, status);
        addHeader("Content-Length", String.valueOf(body.length));
        String headerLines = buildHeaders(headers);
        
        String startLineAndHeaders = startLine + "\r\n" + headerLines + "\r\n\r\n";
        
        byte[] raw = Bytes.concat(startLineAndHeaders.getBytes(Charsets.UTF_8), body);
        
        WriteSocketHandler writeSocketHandler = new WriteSocketHandler(this, raw);
        //SelectionKey sk = socket.register(NioHttpServer.getSelector(), SelectionKey.OP_WRITE, writeSocketHandler);
        sk.interestOps(SelectionKey.OP_WRITE);
        sk.attach(writeSocketHandler);
    }
    
    public void redirect(String url) {
        String startLine = buildStartLine(protocol, Status._301);
        headers.put("Location", url);
        String headerLines = buildHeaders(headers);
        
        String startLineAndHeaders = startLine + "\r\n" + headerLines + "\r\n\r\n";
        
        byte[] raw = startLineAndHeaders.getBytes(Charsets.UTF_8);
        
        WriteSocketHandler writeSocketHandler = new WriteSocketHandler(this, raw);
        //SelectionKey sk = socket.register(NioHttpServer.getSelector(), SelectionKey.OP_WRITE, writeSocketHandler);
        sk.interestOps(SelectionKey.OP_WRITE);
        sk.attach(writeSocketHandler);
    }
    
    private String buildStartLine(String protocol, Status status) {
        return Joiner.on(" ").join(protocol, status, status.getMessage());
    }
    
    private String buildHeaders(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue()).append("\r\n");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public void addHeader(String name, String value) {
        Preconditions.checkNotNull(name);
        headers.put(name, value);
    }
    public void addCookie(String name, String value) {
        Preconditions.checkNotNull(name);
        cookies.put(name, value);
    }
    public Map<String, String> getCookies() {
        return cookies;
    }
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public Status getStatus() {
        return status;
    }
    public byte[] getBody() {
        return body;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setBody(byte[] body) {
        this.body = body;
    }
    public SocketChannel getSocket() {
        SocketChannel socket = (SocketChannel)sk.channel();
        return socket;
    }
    public SelectionKey getSelectionKey() {
        return sk;
    }
    public void setSelectionKey(SelectionKey sk) {
        this.sk = sk;
    }
}
