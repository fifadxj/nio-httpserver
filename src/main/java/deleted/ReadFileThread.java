package deleted;

import java.io.File;
import java.io.IOException;

import server.NioHttpServer;
import server.handler.aio.AIOEventHandler;

import com.google.common.io.Files;

public class ReadFileThread extends Thread {
	private AIOEventHandler handler;
	private File file;
	public ReadFileThread(File file, AIOEventHandler handler) {
		this.file = file;
		this.handler = handler;
	}
	
	@Override
    public void run() {
	    try {
	    	//System.out.println("reading start...");
	        byte[] bytes = Files.toByteArray(file);
	        //System.out.println("reading finished...");
	        handler.setEvent(bytes);
	        NioHttpServer.addHandler(handler);
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    
    }

}
