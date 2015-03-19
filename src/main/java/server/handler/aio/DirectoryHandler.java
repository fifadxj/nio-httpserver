package server.handler.aio;

import java.io.File;
import java.io.IOException;
import java.util.List;

import server.HttpRequest;
import server.HttpResponse;
import server.NioHttpServer;
import server.Status;

import com.google.common.base.Charsets;

public class DirectoryHandler implements AIOEventHandler<List<File>> {
    private HttpRequest req;
    private HttpResponse resp;
    private List<File> children;
    
    public DirectoryHandler(HttpRequest req, HttpResponse resp) {
        this.req = req;
        this.resp = resp;
    }
    
    @Override
    public void handle() throws IOException {
        resp.setStatus(Status._200);
        resp.setProtocol(req.getProtocol());
        resp.addHeader("Content-Type", "text/html");
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
    public void setEvent(List<File> event) {
        this.children = event;
    }

}
