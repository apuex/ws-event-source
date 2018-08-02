package com.github.apuex.ws.eventsource;

import com.github.apuex.springbootsolution.runtime.Messages;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.socket.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class MessageHandler implements WebSocketHandler, MessageListener {
  private Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private final JmsTemplate jmsTemplate;
  private final Executor executor;
  private final Map<String, WsConnection> sessionMap = new HashMap<>();
  private final WsConnectionEventHandler handler = new WsConnectionEventHandler() {

    @Override
    public void handleConnectionBroken(WebSocketSession session) {
      sessionMap.remove(session.getId());
    }

    @Override
    public void handleConnectionError(WebSocketSession session, Throwable t) {
      sessionMap.remove(session.getId());
    }
  };

  public MessageHandler(JmsTemplate jmsTemplate, Executor executor) {
    this.jmsTemplate = jmsTemplate;
    this.executor = executor;
  }

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof javax.jms.BytesMessage) {
        javax.jms.BytesMessage tm = (javax.jms.BytesMessage) message;
        String type = tm.getStringProperty("type");
        byte[] bytes = new byte[(int) tm.getBodyLength()];
        Any any = Any.parseFrom(bytes);
        TextMessage msg = new TextMessage(JsonFormat.printer().print(any));

        sessionMap.entrySet().forEach(e -> e.getValue().enque(msg));
      } else {
        Any any = Any.pack(Messages.QueryCommand.newBuilder().build());
        JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder()
            .add(StringValue.getDescriptor())
            .add(com.github.apuex.springbootsolution.runtime.Messages.getDescriptor().getMessageTypes())
            .build();
        log.warn(JsonFormat.printer().usingTypeRegistry(registry).print(any));
      }
    } catch (JMSException e) {
      throw new RuntimeException(e);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("{}, {} connected.\n", session.getId(), session.getUri());
    sessionMap.put(session.getId(), new WsConnection(session, handler, executor));
  }

  @Override
  public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws Exception {
    String payload = (String) message.getPayload();
    jmsTemplate.send(jmsSession -> {
      javax.jms.TextMessage msg = jmsSession.createTextMessage();
      msg.setText(payload);
      return msg;
    });
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    log.info("{}, {} disconnected.\n", session.getId(), session.getUri());
    exception.printStackTrace();
    sessionMap.remove(session.getId());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    log.info("{}, {} disconnected.\n", session.getId(), session.getUri());
    sessionMap.remove(session.getId());
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }
}
