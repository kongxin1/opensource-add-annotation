/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ public class MethodDescriptor
/*    */ {
/*    */   private final ProtoDescriptor protoDescriptor;
/*    */   private String name;
/*    */   private String parameter;
/*    */   private String returns;
/*    */ 
/*    */   public MethodDescriptor(ProtoDescriptor protoDescriptor)
/*    */   {
/* 27 */     this.protoDescriptor = protoDescriptor;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 31 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public void setParameter(String parameter) {
/* 35 */     this.parameter = parameter;
/*    */   }
/*    */ 
/*    */   public void setReturns(String returns) {
/* 39 */     this.returns = returns;
/*    */   }
/*    */ 
/*    */   public ProtoDescriptor getProtoDescriptor() {
/* 43 */     return this.protoDescriptor;
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 47 */     return this.name;
/*    */   }
/*    */ 
/*    */   public String getParameter() {
/* 51 */     return this.parameter;
/*    */   }
/*    */ 
/*    */   public String getReturns() {
/* 55 */     return this.returns;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.MethodDescriptor
 * JD-Core Version:    0.6.2
 */