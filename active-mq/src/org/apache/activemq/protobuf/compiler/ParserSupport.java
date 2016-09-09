/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ import org.apache.activemq.protobuf.compiler.parser.ParseException;
/*    */ import org.apache.activemq.protobuf.compiler.parser.Token;
/*    */ 
/*    */ public class ParserSupport
/*    */ {
/*    */   public static String decodeString(Token token)
/*    */     throws ParseException
/*    */   {
/*    */     try
/*    */     {
/* 77 */       return TextFormat.unescapeText(token.image.substring(1, token.image.length() - 1));
/*    */     } catch (TextFormat.InvalidEscapeSequence e) {
/* 79 */       throw new ParseException("Invalid string litteral at line " + token.next.beginLine + ", column " + token.next.beginColumn + ": " + e.getMessage());
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.ParserSupport
 * JD-Core Version:    0.6.2
 */