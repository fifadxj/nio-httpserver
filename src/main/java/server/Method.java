package server;

public enum Method {
    GET(false),
    POST(true);
    
    private boolean hasBody;
    
    private Method(boolean hasBody) {
        this.hasBody = hasBody;
    }
    
    public static Method parse(String method) {
        for (Method m : values()) {
            if (m.toString().equals(method)) {
                return m;
            }
        }
        
        return null;
    }
    
    public boolean hasBody() {
        return this.hasBody;
    }
}
