package server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

public class HttpRequestImpl implements HttpRequest {
    private Map<String, String> headers;
    private Map<String, String> params;
    private Map<String, String> cookies;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    
    private String path;
    private Method method;
    private String body;
    private String startLine;
    private String host;
    private String protocol;
    private String raw;
    private SelectionKey sk;
    private String id;
    
    public HttpRequestImpl() {
        this.id = UUID.randomUUID().toString();
    }
    
    @Override
    public String getHeader(String name) {
        Preconditions.checkNotNull(name);
        return headers.get(name);
    }

    @Override
    public String getParam(String name) {
        Preconditions.checkNotNull(name);
        return params.get(name);
    }

    @Override
    public String getCookie(String name) {
        Preconditions.checkNotNull(name);
        return cookies.get(name);
    }

    @Override
    public void set(String name, Object value) {
        Preconditions.checkNotNull(name);
        attributes.put(name, value);   
    }

    @Override
    public Object get(String name) {
        Preconditions.checkNotNull(name);
        return attributes.get(name);
    }

    @Override
    public Object remove(String name) {
        Preconditions.checkNotNull(name);
        
        return attributes.remove(name);
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return "HttpRequest [raw=" + raw + "]";
    }

    public SelectionKey getSk() {
        return sk;
    }

    public void setSelectionKey(SelectionKey sk) {
        this.sk = sk;
    }

    @Override
    public SocketChannel getSocket() {
        SocketChannel socket = (SocketChannel)sk.channel();
        return socket;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return sk;
    }
    
    public String id() {
        return this.id;
    }
}
