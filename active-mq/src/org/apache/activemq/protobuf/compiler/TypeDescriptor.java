package org.apache.activemq.protobuf.compiler;

public abstract interface TypeDescriptor
{
  public abstract String getName();

  public abstract String getQName();

  public abstract ProtoDescriptor getProtoDescriptor();

  public abstract boolean isEnum();

  public abstract void associate(EnumFieldDescriptor paramEnumFieldDescriptor);
}

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.TypeDescriptor
 * JD-Core Version:    0.6.2
 */