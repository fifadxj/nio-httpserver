package server.handler.aio;

import java.io.IOException;

public interface AIOEventHandler {
    void handle() throws IOException;
	void setEvent(Object event);
}
