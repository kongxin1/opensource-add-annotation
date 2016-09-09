package org.apache.activemq.protobuf;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface MessageBuffer<B, MB extends MessageBuffer> extends PBMessage<B, MB>
{
  public abstract int serializedSizeUnframed();

  public abstract int serializedSizeFramed();

  public abstract Buffer toUnframedBuffer();

  public abstract Buffer toFramedBuffer();

  public abstract byte[] toUnframedByteArray();

  public abstract byte[] toFramedByteArray();

  public abstract void writeUnframed(CodedOutputStream paramCodedOutputStream)
    throws IOException;

  public abstract void writeFramed(CodedOutputStream paramCodedOutputStream)
    throws IOException;

  public abstract void writeUnframed(OutputStream paramOutputStream)
    throws IOException;

  public abstract void writeFramed(OutputStream paramOutputStream)
    throws IOException;
}

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.MessageBuffer
 * JD-Core Version:    0.6.2
 */