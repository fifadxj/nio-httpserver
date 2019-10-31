package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.handler.aio.AIOEventHandler;
import server.handler.nio.AcceptSocketConnectionHandler;
import server.handler.nio.NIOEventHandler;

import com.google.common.base.Strings;

public class NioHttpServer {
	private static Logger logger = LoggerFactory.getLogger(NioHttpServer.class);
	private Selector selector;
	private static List<AIOEventHandler> handlers = new ArrayList<AIOEventHandler>();
	private ServerSocketChannel serverSocketChannel;
	
	private static String docRoot;
	
	static {
	    docRoot = System.getProperty("root");
        if (Strings.isNullOrEmpty(docRoot) || docRoot.trim().length() == 0) {
            docRoot = System.getProperty("user.home") + "/nio-root";
        }
	}
	
	public static String docRoot() {
	    return docRoot;
	}

	public static void main(String[] args) throws IOException {
		NioHttpServer server = new NioHttpServer(8888);
		server.eventLoop();
	}

	public static void addHandler(AIOEventHandler handler) throws IOException {
		synchronized (handlers) {
			handlers.add(handler);
		}
	}

	public NioHttpServer(int port) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		
		selector = Selector.open();
		SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		AcceptSocketConnectionHandler acceptConnectionHandler = new AcceptSocketConnectionHandler(selectionKey);
		selectionKey.attach(acceptConnectionHandler);
	}

	/*
	 * Alternatively, use explicit SPI provider: SelectorProvider p =
	 * SelectorProvider.provider(); selector = p.openSelector(); serverSocket =
	 * p.openServerSocketChannel();
	 */

	public void eventLoop() throws IOException {
		while (!Thread.interrupted()) {
			selector.select();
			Set<SelectionKey> selected = selector.selectedKeys();
			Iterator<SelectionKey> it = selected.iterator();
			while (it.hasNext()) {
				handleEvent(it.next());
			}
			selected.clear();

			synchronized (handlers) {
				List<AIOEventHandler> resolvedHandlers = new ArrayList<AIOEventHandler>();
				for (AIOEventHandler handler : handlers) {
					try {
						handler.handle();
					} catch (Exception e) {
						logger.error("handle event failed", e);
					}
					resolvedHandlers.add(handler);
				}
				for (AIOEventHandler handler : resolvedHandlers) {
					handlers.remove(handler);
				}
			}
		}
	}

	private void handleEvent(SelectionKey k) throws IOException {
		NIOEventHandler r = (NIOEventHandler) (k.attachment());
		if (r != null)
			try {
				r.handle();
			} catch (Exception e) {
				logger.error("handle nio event failed", e);
			}
	}
}