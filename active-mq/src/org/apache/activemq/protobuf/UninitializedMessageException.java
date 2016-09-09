/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ import java.util.Collections;
/*    */ import java.util.List;
/*    */ 
/*    */ public class UninitializedMessageException extends RuntimeException
/*    */ {
/*    */   private final List<String> missingFields;
/*    */ 
/*    */   public UninitializedMessageException(List<String> missingFields)
/*    */   {
/* 38 */     super(buildDescription(missingFields));
/* 39 */     this.missingFields = missingFields;
/*    */   }
/*    */ 
/*    */   public List<String> getMissingFields()
/*    */   {
/* 49 */     return Collections.unmodifiableList(this.missingFields);
/*    */   }
/*    */ 
/*    */   public InvalidProtocolBufferException asInvalidProtocolBufferException()
/*    */   {
/* 58 */     return new InvalidProtocolBufferException(getMessage());
/*    */   }
/*    */ 
/*    */   private static String buildDescription(List<String> missingFields)
/*    */   {
/* 63 */     StringBuilder description = new StringBuilder("Message missing required fields: ");
/* 64 */     boolean first = true;
/* 65 */     for (String field : missingFields) {
/* 66 */       if (first)
/* 67 */         first = false;
/*    */       else {
/* 69 */         description.append(", ");
/*    */       }
/* 71 */       description.append(field);
/*    */     }
/* 73 */     return description.toString();
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.UninitializedMessageException
 * JD-Core Version:    0.6.2
 */