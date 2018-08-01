package com.github.apuex.ws.eventsource;

import com.sun.messaging.ConnectionFactory;

import javax.jms.JMSException;
import java.util.Properties;

public class SunConnectionFactory extends ConnectionFactory {
  public SunConnectionFactory () {

  }

  public void setConfiguration(Properties configuration) {
    configuration.entrySet().forEach(e -> {
      try {
        setProperty((String)e.getKey(), (String)e.getValue());
      } catch (JMSException ex) {
        throw new RuntimeException(ex);
      }
    });
  }
}
