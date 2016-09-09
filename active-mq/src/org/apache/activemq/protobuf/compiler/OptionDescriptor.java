/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ import java.util.List;
/*    */ 
/*    */ public class OptionDescriptor
/*    */ {
/*    */   private String name;
/*    */   private String value;
/*    */ 
/*    */   public String getName()
/*    */   {
/* 30 */     return this.name;
/*    */   }
/*    */ 
/*    */   public String getValue() {
/* 34 */     return this.value;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 38 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public void setValue(String value) {
/* 42 */     this.value = value;
/*    */   }
/*    */ 
/*    */   public void validate(List<String> errors)
/*    */   {
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.OptionDescriptor
 * JD-Core Version:    0.6.2
 */