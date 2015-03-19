package server.handler.nio;

import java.io.IOException;

public interface NIOEventHandler {
	void handle() throws IOException;
}
