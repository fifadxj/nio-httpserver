package server.handler.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptSocketConnectionHandler implements NIOEventHandler {
	private SelectionKey serverSocketSelectionKey;

	public AcceptSocketConnectionHandler(SelectionKey sk) {
		this.serverSocketSelectionKey = sk;
	}

	public void handle() throws IOException {
	    ServerSocketChannel serverSocket = (ServerSocketChannel) serverSocketSelectionKey.channel();
		SocketChannel socket = serverSocket.accept();
		
		if (socket != null) {
			socket.configureBlocking(false);
			SelectionKey socketSk = socket.register(serverSocketSelectionKey.selector(), SelectionKey.OP_READ);
			socketSk.attach(new ReadSocketHandler(socketSk));
		}
	}
}
