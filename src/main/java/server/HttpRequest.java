package server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface HttpRequest {

    String getHeader(String name);

    Map<String, String> getHeaders();

    String getParam(String name);

    Map<String, String> getParams();

    String getCookie(String name);

    Map<String, String> getCookies();

    void set(String name, Object value);

    Object get(String name);

    Object remove(String name);

    String getPath();

    Method getMethod();

    String getBody();

    String getStartLine();

    String getHost();

    String getProtocol();
    
    SocketChannel getSocket();
    
    SelectionKey getSelectionKey();
    
    String id();

}