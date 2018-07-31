package com.github.apuex.ws.eventsource;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.*;
import org.springframework.web.socket.TextMessage;

import javax.jms.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.out;

public class MessageHandler implements WebSocketHandler {
  private final JmsTemplate jmsTemplate;
  private final Map<String, WebSocketSession> sessionMap = new HashMap<>();
  private final Set<String> invalidSessionIds = new HashSet<>();
  public static final String EVENT_NOTIFY_TOPIC = "EVENT_NOTIFY_TOPIC";
  private final Gson gson = new Gson();
  public final MessageListener listener = new MessageListener() {
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
  };

  public MessageHandler(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    out.printf("%s, %s connected.\n", session.getId(), session.getUri());
    sessionMap.put(session.getId(), session);
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    String payload = (String) message.getPayload();
    jmsTemplate.send(EVENT_NOTIFY_TOPIC, new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
        out.println(payload);
        javax.jms.TextMessage msg = session.createTextMessage();
        msg.setText(payload);
        return msg;
      }
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
