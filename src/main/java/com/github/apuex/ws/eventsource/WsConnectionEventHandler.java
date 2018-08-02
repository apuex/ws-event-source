package com.github.apuex.ws.eventsource;

import org.springframework.web.socket.WebSocketSession;

public interface WsConnectionEventHandler {
  void handleConnectionBroken(WebSocketSession session);
  void handleConnectionError(WebSocketSession session, Throwable t);
}
