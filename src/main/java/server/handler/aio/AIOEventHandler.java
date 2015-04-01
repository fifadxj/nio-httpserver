package server.handler.aio;

import java.io.IOException;

public interface AIOEventHandler<T> {
    void handle() throws IOException;
	void setEvent(T event);
}
