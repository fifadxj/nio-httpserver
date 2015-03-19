package server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import server.handler.aio.AIOEventHandler;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class AIO {
    public static void readFile(final File file, final AIOEventHandler<byte[]> handler) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    //System.out.println("reading start...");
                    byte[] bytes = Files.toByteArray(file);
                    //System.out.println("reading finished...");
                    handler.setEvent(bytes);
                    NioHttpServer.addHandler(handler);
                } catch (IOException e) {
                    throw new RuntimeException("read file failed: " + file.getAbsolutePath());
                }
            }
        };
        
        new Thread(runnable).start();
    }
    
    public static void readDirectory(final File file, final AIOEventHandler<List<File>> handler) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Iterable<File> children = Files.fileTreeTraverser().children(file);
                    handler.setEvent(Lists.newArrayList(children));
                    NioHttpServer.addHandler(handler);
                } catch (IOException e) {
                    throw new RuntimeException("read directory failed: " + file.getAbsolutePath());
                }
            }
        };
        
        new Thread(runnable).start();
    }
}
