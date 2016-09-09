/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class ServiceDescriptor
/*    */ {
/*    */   private final ProtoDescriptor protoDescriptor;
/* 25 */   private List<MethodDescriptor> methods = new ArrayList();
/*    */   private String name;
/*    */ 
/*    */   public ServiceDescriptor(ProtoDescriptor protoDescriptor)
/*    */   {
/* 29 */     this.protoDescriptor = protoDescriptor;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 33 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public void setMethods(List<MethodDescriptor> methods) {
/* 37 */     this.methods = methods;
/*    */   }
/*    */ 
/*    */   public ProtoDescriptor getProtoDescriptor() {
/* 41 */     return this.protoDescriptor;
/*    */   }
/*    */ 
/*    */   public List<MethodDescriptor> getMethods() {
/* 45 */     return this.methods;
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 49 */     return this.name;
/*    */   }
/*    */ 
/*    */   public void validate(List<String> errors)
/*    */   {
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.ServiceDescriptor
 * JD-Core Version:    0.6.2
 */