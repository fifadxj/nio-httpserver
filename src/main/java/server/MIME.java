package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MIME {
    
    public static Map<String, String> map = new HashMap<String, String>();
    public static Properties props = new Properties();
    
    static {
        try {
            props.load(new InputStreamReader(MIME.class.getResourceAsStream("/mime.properties")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("load mime types failed");
        }
    }
    
    public static String getContentType(String fileExtension) {
        return props.getProperty(fileExtension);
    }
}
