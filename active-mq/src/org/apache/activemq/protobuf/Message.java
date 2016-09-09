package org.apache.activemq.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract interface Message<T>
{
  public abstract T clone()
    throws CloneNotSupportedException;

  public abstract int serializedSizeUnframed();

  public abstract int serializedSizeFramed();

  public abstract void clear();

  public abstract T assertInitialized()
    throws UninitializedMessageException;

  public abstract T mergeFrom(T paramT);

  public abstract T mergeUnframed(byte[] paramArrayOfByte)
    throws InvalidProtocolBufferException;

  public abstract T mergeFramed(byte[] paramArrayOfByte)
    throws InvalidProtocolBufferException;

  public abstract T mergeUnframed(Buffer paramBuffer)
    throws InvalidProtocolBufferException;

  public abstract T mergeFramed(Buffer paramBuffer)
    throws InvalidProtocolBufferException;

  public abstract T mergeUnframed(InputStream paramInputStream)
    throws IOException;

  public abstract T mergeFramed(InputStream paramInputStream)
    throws IOException;

  public abstract T mergeUnframed(CodedInputStream paramCodedInputStream)
    throws IOException;

  public abstract T mergeFramed(CodedInputStream paramCodedInputStream)
    throws IOException;

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
 * Qualified Name:     org.apache.activemq.protobuf.Message
 * JD-Core Version:    0.6.2
 */