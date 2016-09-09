/*     */ package org.apache.activemq.protobuf.compiler.parser;
/*     */ 
/*     */ public class TokenMgrError extends Error
/*     */ {
/*     */   static final int LEXICAL_ERROR = 0;
/*     */   static final int STATIC_LEXER_ERROR = 1;
/*     */   static final int INVALID_LEXICAL_STATE = 2;
/*     */   static final int LOOP_DETECTED = 3;
/*     */   int errorCode;
/*     */ 
/*     */   protected static final String addEscapes(String str)
/*     */   {
/*  57 */     StringBuffer retval = new StringBuffer();
/*     */ 
/*  59 */     for (int i = 0; i < str.length(); i++) {
/*  60 */       switch (str.charAt(i))
/*     */       {
/*     */       case '\000':
/*  63 */         break;
/*     */       case '\b':
/*  65 */         retval.append("\\b");
/*  66 */         break;
/*     */       case '\t':
/*  68 */         retval.append("\\t");
/*  69 */         break;
/*     */       case '\n':
/*  71 */         retval.append("\\n");
/*  72 */         break;
/*     */       case '\f':
/*  74 */         retval.append("\\f");
/*  75 */         break;
/*     */       case '\r':
/*  77 */         retval.append("\\r");
/*  78 */         break;
/*     */       case '"':
/*  80 */         retval.append("\\\"");
/*  81 */         break;
/*     */       case '\'':
/*  83 */         retval.append("\\'");
/*  84 */         break;
/*     */       case '\\':
/*  86 */         retval.append("\\\\");
/*  87 */         break;
/*     */       default:
/*     */         char ch;
/*  89 */         if (((ch = str.charAt(i)) < ' ') || (ch > '~')) {
/*  90 */           String s = "0000" + Integer.toString(ch, 16);
/*  91 */           retval.append("\\u" + s.substring(s.length() - 4, s.length()));
/*     */         } else {
/*  93 */           retval.append(ch);
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/*  98 */     return retval.toString();
/*     */   }
/*     */ 
/*     */   protected static String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar)
/*     */   {
/* 114 */     return "Lexical error at line " + errorLine + ", column " + errorColumn + ".  Encountered: " + (EOFSeen ? "<EOF> " : new StringBuilder().append("\"").append(addEscapes(String.valueOf(curChar))).append("\"").append(" (").append(curChar).append("), ").toString()) + "after : \"" + addEscapes(errorAfter) + "\"";
/*     */   }
/*     */ 
/*     */   public String getMessage()
/*     */   {
/* 131 */     return super.getMessage();
/*     */   }
/*     */ 
/*     */   public TokenMgrError()
/*     */   {
/*     */   }
/*     */ 
/*     */   public TokenMgrError(String message, int reason)
/*     */   {
/* 142 */     super(message);
/* 143 */     this.errorCode = reason;
/*     */   }
/*     */ 
/*     */   public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
/* 147 */     this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.TokenMgrError
 * JD-Core Version:    0.6.2
 */