/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ import java.util.List;
/*    */ 
/*    */ public class CompilerException extends Exception
/*    */ {
/*    */   private final List<String> errors;
/*    */ 
/*    */   public CompilerException(List<String> errors)
/*    */   {
/* 25 */     this.errors = errors;
/*    */   }
/*    */ 
/*    */   public List<String> getErrors() {
/* 29 */     return this.errors;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.CompilerException
 * JD-Core Version:    0.6.2
 */