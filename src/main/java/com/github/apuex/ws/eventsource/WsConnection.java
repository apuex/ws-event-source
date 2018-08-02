package com.github.apuex.ws.eventsource;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class WsConnection {
  private final WebSocketSession session;
  private final WsConnectionEventHandler handler;
  private final Executor executor;
  private final ConcurrentLinkedQueue<TextMessage> queue;

  public WsConnection(WebSocketSession session, WsConnectionEventHandler handler, Executor executor) {
    this.session = session;
    this.handler = handler;
    this.executor = executor;
    this.queue = new ConcurrentLinkedQueue<>();
  }

  public void enque(TextMessage message) {
    final boolean empty = queue.isEmpty();
    queue.offer(message);
    if (empty) {
      executor.execute(() -> {
        while (!queue.isEmpty()) {
          try {
            TextMessage m = queue.peek();
            session.sendMessage(m);
            queue.remove();
          } catch (InvalidProtocolBufferException e) {
          } catch (IOException e) {
            handler.handleConnectionError(session, e);
          }
        }
      });
    }
  }
}
