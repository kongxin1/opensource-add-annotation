/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class InvalidProtocolBufferException extends IOException
/*    */ {
/*    */   private static final long serialVersionUID = 5685337441004132240L;
/*    */ 
/*    */   public InvalidProtocolBufferException(String description)
/*    */   {
/* 31 */     super(description);
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException truncatedMessage() {
/* 35 */     return new InvalidProtocolBufferException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException negativeSize()
/*    */   {
/* 40 */     return new InvalidProtocolBufferException("CodedInputStream encountered an embedded string or message which claimed to have negative size.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException malformedVarint() {
/* 44 */     return new InvalidProtocolBufferException("CodedInputStream encountered a malformed varint.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException invalidTag() {
/* 48 */     return new InvalidProtocolBufferException("Protocol message contained an invalid tag (zero).");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException invalidEndTag() {
/* 52 */     return new InvalidProtocolBufferException("Protocol message end-group tag did not match expected tag.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException invalidWireType() {
/* 56 */     return new InvalidProtocolBufferException("Protocol message tag had invalid wire type.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException recursionLimitExceeded() {
/* 60 */     return new InvalidProtocolBufferException("Protocol message had too many levels of nesting.  May be malicious.  Use CodedInputStream.setRecursionLimit() to increase the depth limit.");
/*    */   }
/*    */ 
/*    */   static InvalidProtocolBufferException sizeLimitExceeded() {
/* 64 */     return new InvalidProtocolBufferException("Protocol message was too large.  May be malicious.  Use CodedInputStream.setSizeLimit() to increase the size limit.");
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.InvalidProtocolBufferException
 * JD-Core Version:    0.6.2
 */