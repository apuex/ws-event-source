package com.github.apuex.ws.eventsource;

import com.google.protobuf.MessageOrBuilder;

import java.util.Objects;

public class MessageEnvelope {
  private final String msgType;
  private final MessageOrBuilder msg;

  public MessageEnvelope(String msgType, MessageOrBuilder msg) {
    this.msgType = msgType;
    this.msg = msg;
  }

  public String getMsgType() {
    return msgType;
  }

  public MessageOrBuilder getMsg() {
    return msg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageEnvelope that = (MessageEnvelope) o;
    return Objects.equals(msgType, that.msgType) &&
        Objects.equals(msg, that.msg);
  }

  @Override
  public int hashCode() {

    return Objects.hash(msgType, msg);
  }

  @Override
  public String toString() {
    return "MessageEnvelope{" +
        "msgType='" + msgType + '\'' +
        ", msg=" + msg +
        '}';
  }
}
