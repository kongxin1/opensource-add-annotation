/*    */ package org.apache.activemq.protobuf.compiler.parser;
/*    */ 
/*    */ public class Token
/*    */ {
/*    */   public int kind;
/*    */   public int beginLine;
/*    */   public int beginColumn;
/*    */   public int endLine;
/*    */   public int endColumn;
/*    */   public String image;
/*    */   public Token next;
/*    */   public Token specialToken;
/*    */ 
/*    */   public String toString()
/*    */   {
/* 74 */     return this.image;
/*    */   }
/*    */ 
/*    */   public static final Token newToken(int ofKind)
/*    */   {
/* 91 */     switch (ofKind) {
/*    */     }
/* 93 */     return new Token();
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.Token
 * JD-Core Version:    0.6.2
 */