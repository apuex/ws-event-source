package com.github.apuex.ws.eventsource;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.TopicConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import static com.github.apuex.ws.eventsource.MessageHandler.EVENT_NOTIFY_TOPIC;
import static javax.jms.Session.DUPS_OK_ACKNOWLEDGE;


@Configuration
@EnableWebSocket
@EnableJms
public class WebSocketBrokerConfig implements WebSocketConfigurer {

  @Autowired
  private MessageHandler messageHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(messageHandler, "/event-store");
  }

  @Bean
  public MessageHandler messageHandler(JmsListenerContainerFactory<?> factory, JmsTemplate template) {
    return new MessageHandler(template);
  }

  @Bean
  public JmsTemplate jmsTemplate(TopicConnectionFactory factory) {
    return new JmsTemplate(factory);
  }

  @Bean
  public MessageListenerContainer listenerContainer() throws JMSException {
    DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    container.setConnectionFactory(getTopicConnectionFactory());
    container.setDestinationName(EVENT_NOTIFY_TOPIC);
    container.setMessageListener(messageHandler.listener);
    container.setSessionTransacted(false);
    container.setSessionAcknowledgeMode(DUPS_OK_ACKNOWLEDGE);
    return container;
  }

  @Bean
  public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                  DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    configurer.configure(factory, connectionFactory);
    return factory;
  }

  @Bean
  public TopicConnectionFactory getTopicConnectionFactory() throws JMSException {
    TopicConnectionFactory factory = new TopicConnectionFactory();
    factory.setProperty(ConnectionConfiguration.imqBrokerHostName, "192.168.0.166");
    factory.setProperty(ConnectionConfiguration.imqBrokerHostPort, "7676");
    factory.setProperty(ConnectionConfiguration.imqDefaultUsername, "admin");
    factory.setProperty(ConnectionConfiguration.imqDefaultPassword, "admin");
    return factory;
  }
}
