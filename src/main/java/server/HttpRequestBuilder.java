package server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

public class HttpRequestBuilder {
    private List<Byte> input = new ArrayList<Byte>();
    private String inputString = "";
    private int contentLength = 0;
    private boolean startLineAndHeaderParsed = false;
    private SelectionKey socketChannelSelectionKey;
    
    private String path;
    private Method method;
    private String body;
    private String startLine;
    private String host;
    private String protocol;
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private Map<String, String> params = new LinkedHashMap<String, String>();
    private Map<String, String> cookies = new LinkedHashMap<String, String>();
    
    public HttpRequestBuilder(SelectionKey sk) {
        this.socketChannelSelectionKey = sk;
    }
    
    public boolean isInputComplete() {
        
        if (inputString.indexOf("\r\n\r\n") != -1) {//request header is end
            String startLineAndHeader = inputString.substring(0, inputString.indexOf("\r\n\r\n"));
            List<String> lines = Splitter.on("\r\n").trimResults().splitToList(startLineAndHeader);
            
            if (! startLineAndHeaderParsed) {
                parseStartLine(lines.get(0));
                parseHeaders(lines.subList(1, lines.size()));
                startLineAndHeaderParsed = true;
            }

            if (! method.hasBody()) {
                return true;
            }
            else {
                String body = inputString.substring(inputString.indexOf("\r\n\r\n") + 4);
                if (body.length() >= contentLength) {
                    this.body = body;
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        else {
            return false;
        }
    }
    
    public void appendSegment(byte[] segment) {
        Preconditions.checkNotNull(segment);
        for (byte b : segment) {
            input.add(b);
        }
        inputString += new String(segment, Charsets.UTF_8);
    }
    
    private void parseHeaders(List<String> lines) {
        for (String header : lines) {
            List<String> pair = Splitter.on(":").trimResults().limit(2).splitToList(header);
            if (pair.size() != 2) {
                throw new BadRequestException("invaid request header: " + header);
            }
            
            this.headers.put(pair.get(0), pair.get(1));
        }
        
        if (method.hasBody()) {
            if (headers.get("Content-Length") == null) {
                throw new BadRequestException("request header[Content-Length] is missing");
            }
            else {
                Integer length = Ints.tryParse(headers.get("Content-Length"));
                if (length == null || length < 0) {
                    throw new BadRequestException("request header[Content-Length] is invalid");
                }
                this.contentLength = length;
            }
        }
    }
    
    private void parseStartLine(String requestStartLine) {
        if (Strings.isNullOrEmpty(requestStartLine)) {
            throw new IllegalArgumentException();
        }
        List<String> items = Splitter.on(" ").trimResults().splitToList(requestStartLine);
        if (items.size() != 3) {
            throw new BadRequestException("request start line invalid: " + requestStartLine);
        }
        
        String method = items.get(0);
        Method m = Method.parse(method);
        if (m == null) {
            throw new BadRequestException("request start line invalid: " + requestStartLine);
        }
        this.method = m;
        this.path = items.get(1);
        this.protocol = items.get(2);
        
        
    }
    
    public HttpRequest build() {
        HttpRequestImpl req = new HttpRequestImpl();

        req.setSelectionKey(socketChannelSelectionKey);
        req.setRaw(inputString);
        req.setStartLine(startLine);
        req.setBody(body);
        req.setHeaders(Collections.unmodifiableMap(headers));
        req.setParams(Collections.unmodifiableMap(params));
        req.setCookies(Collections.unmodifiableMap(cookies));
        req.setMethod(method);
        req.setPath(path);
        req.setProtocol(protocol);
        req.setHost(host);
        
        return req;
    }
}
