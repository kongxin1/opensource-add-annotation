package org.apache.activemq.protobuf;

public abstract interface PBMessage<Bean, Buffer extends MessageBuffer>
{
  public abstract Bean copy();

  public abstract boolean frozen();

  public abstract Buffer freeze();
}

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.PBMessage
 * JD-Core Version:    0.6.2
 */