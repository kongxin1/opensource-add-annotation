/*     */ package org.apache.activemq.protobuf.compiler.parser;
/*     */ 
/*     */ public class ParseException extends Exception
/*     */ {
/*     */   protected boolean specialConstructor;
/*     */   public Token currentToken;
/*     */   public int[][] expectedTokenSequences;
/*     */   public String[] tokenImage;
/* 156 */   protected String eol = System.getProperty("line.separator", "\n");
/*     */ 
/*     */   public ParseException(Token currentTokenVal, int[][] expectedTokenSequencesVal, String[] tokenImageVal)
/*     */   {
/*  48 */     super("");
/*  49 */     this.specialConstructor = true;
/*  50 */     this.currentToken = currentTokenVal;
/*  51 */     this.expectedTokenSequences = expectedTokenSequencesVal;
/*  52 */     this.tokenImage = tokenImageVal;
/*     */   }
/*     */ 
/*     */   public ParseException()
/*     */   {
/*  67 */     this.specialConstructor = false;
/*     */   }
/*     */ 
/*     */   public ParseException(String message) {
/*  71 */     super(message);
/*  72 */     this.specialConstructor = false;
/*     */   }
/*     */ 
/*     */   public String getMessage()
/*     */   {
/* 114 */     if (!this.specialConstructor) {
/* 115 */       return super.getMessage();
/*     */     }
/* 117 */     StringBuffer expected = new StringBuffer();
/* 118 */     int maxSize = 0;
/* 119 */     for (int i = 0; i < this.expectedTokenSequences.length; i++) {
/* 120 */       if (maxSize < this.expectedTokenSequences[i].length) {
/* 121 */         maxSize = this.expectedTokenSequences[i].length;
/*     */       }
/* 123 */       for (int j = 0; j < this.expectedTokenSequences[i].length; j++) {
/* 124 */         expected.append(this.tokenImage[this.expectedTokenSequences[i][j]]).append(" ");
/*     */       }
/* 126 */       if (this.expectedTokenSequences[i][(this.expectedTokenSequences[i].length - 1)] != 0) {
/* 127 */         expected.append("...");
/*     */       }
/* 129 */       expected.append(this.eol).append("    ");
/*     */     }
/* 131 */     String retval = "Encountered \"";
/* 132 */     Token tok = this.currentToken.next;
/* 133 */     for (int i = 0; i < maxSize; i++) {
/* 134 */       if (i != 0) retval = retval + " ";
/* 135 */       if (tok.kind == 0) {
/* 136 */         retval = retval + this.tokenImage[0];
/* 137 */         break;
/*     */       }
/* 139 */       retval = retval + add_escapes(tok.image);
/* 140 */       tok = tok.next;
/*     */     }
/* 142 */     retval = retval + "\" at line " + this.currentToken.next.beginLine + ", column " + this.currentToken.next.beginColumn;
/* 143 */     retval = retval + "." + this.eol;
/* 144 */     if (this.expectedTokenSequences.length == 1)
/* 145 */       retval = retval + "Was expecting:" + this.eol + "    ";
/*     */     else {
/* 147 */       retval = retval + "Was expecting one of:" + this.eol + "    ";
/*     */     }
/* 149 */     retval = retval + expected.toString();
/* 150 */     return retval;
/*     */   }
/*     */ 
/*     */   protected String add_escapes(String str)
/*     */   {
/* 164 */     StringBuffer retval = new StringBuffer();
/*     */ 
/* 166 */     for (int i = 0; i < str.length(); i++) {
/* 167 */       switch (str.charAt(i))
/*     */       {
/*     */       case '\000':
/* 170 */         break;
/*     */       case '\b':
/* 172 */         retval.append("\\b");
/* 173 */         break;
/*     */       case '\t':
/* 175 */         retval.append("\\t");
/* 176 */         break;
/*     */       case '\n':
/* 178 */         retval.append("\\n");
/* 179 */         break;
/*     */       case '\f':
/* 181 */         retval.append("\\f");
/* 182 */         break;
/*     */       case '\r':
/* 184 */         retval.append("\\r");
/* 185 */         break;
/*     */       case '"':
/* 187 */         retval.append("\\\"");
/* 188 */         break;
/*     */       case '\'':
/* 190 */         retval.append("\\'");
/* 191 */         break;
/*     */       case '\\':
/* 193 */         retval.append("\\\\");
/* 194 */         break;
/*     */       default:
/*     */         char ch;
/* 196 */         if (((ch = str.charAt(i)) < ' ') || (ch > '~')) {
/* 197 */           String s = "0000" + Integer.toString(ch, 16);
/* 198 */           retval.append("\\u" + s.substring(s.length() - 4, s.length()));
/*     */         } else {
/* 200 */           retval.append(ch);
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 205 */     return retval.toString();
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.ParseException
 * JD-Core Version:    0.6.2
 */