package com.github.apuex.ws.eventsource;

import com.google.gson.Gson;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.socket.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.out;

public class MessageHandler implements WebSocketHandler, MessageListener {
  private final JmsTemplate jmsTemplate;
  private final Map<String, WebSocketSession> sessionMap = new HashMap<>();
  private final Set<String> invalidSessionIds = new HashSet<>();
  private final Gson gson = new Gson();

  public MessageHandler(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof javax.jms.TextMessage) {
        javax.jms.TextMessage tm = (javax.jms.TextMessage) message;
        TextMessage msg = new TextMessage(tm.getText());

        sessionMap.entrySet().forEach(e -> {
          try {
            e.getValue().sendMessage(msg);
          } catch (Throwable t) {
            invalidSessionIds.add(e.getKey());
          }
        });
        invalidSessionIds.forEach(id -> sessionMap.remove(id));
        invalidSessionIds.clear();
      }
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    out.printf("%s, %s connected.\n", session.getId(), session.getUri());
    sessionMap.put(session.getId(), session);
  }

  @Override
  public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {
    String payload = (String) message.getPayload();
    jmsTemplate.send(jmsSession -> {
      out.println(payload);
      javax.jms.TextMessage msg = jmsSession.createTextMessage();
      msg.setText(payload);
      return msg;
    });
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    out.printf("%s, %s disconnected.\n", session.getId(), session.getUri());
    invalidSessionIds.add(session.getId());
    exception.printStackTrace();
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    out.printf("%s, %s disconnected.\n", session.getId(), session.getUri());
    invalidSessionIds.add(session.getId());
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }
}
