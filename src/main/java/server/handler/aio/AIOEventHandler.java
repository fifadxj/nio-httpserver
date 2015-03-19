package server.handler.aio;

import java.io.IOException;

public interface AIOEventHandler<T extends Object> {
    void handle() throws IOException;
	void setEvent(T event);
}
