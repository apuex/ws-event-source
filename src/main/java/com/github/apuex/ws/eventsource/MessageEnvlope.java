package com.github.apuex.ws.eventsource;

import java.util.Objects;

public class MessageEnvlope {
  private final String msgType;
  private final Object msg;

  public MessageEnvlope(String msgType, Object msg) {
    this.msgType = msgType;
    this.msg = msg;
  }

  public String getMsgType() {
    return msgType;
  }

  public Object getMsg() {
    return msg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageEnvlope that = (MessageEnvlope) o;
    return Objects.equals(msgType, that.msgType) &&
        Objects.equals(msg, that.msg);
  }

  @Override
  public int hashCode() {

    return Objects.hash(msgType, msg);
  }

  @Override
  public String toString() {
    return "MessageEnvlope{" +
        "msgType='" + msgType + '\'' +
        ", msg=" + msg +
        '}';
  }
}
