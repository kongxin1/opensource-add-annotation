/*     */ package org.apache.activemq.protobuf.compiler.parser;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ 
/*     */ public class SimpleCharStream
/*     */ {
/*     */   public static final boolean staticFlag = false;
/*     */   int bufsize;
/*     */   int available;
/*     */   int tokenBegin;
/*  31 */   public int bufpos = -1;
/*     */   protected int[] bufline;
/*     */   protected int[] bufcolumn;
/*  35 */   protected int column = 0;
/*  36 */   protected int line = 1;
/*     */ 
/*  38 */   protected boolean prevCharIsCR = false;
/*  39 */   protected boolean prevCharIsLF = false;
/*     */   protected Reader inputStream;
/*     */   protected char[] buffer;
/*  44 */   protected int maxNextCharInd = 0;
/*  45 */   protected int inBuf = 0;
/*  46 */   protected int tabSize = 8;
/*     */ 
/*  48 */   protected void setTabSize(int i) { this.tabSize = i; } 
/*  49 */   protected int getTabSize(int i) { return this.tabSize; }
/*     */ 
/*     */ 
/*     */   protected void ExpandBuff(boolean wrapAround)
/*     */   {
/*  54 */     char[] newbuffer = new char[this.bufsize + 2048];
/*  55 */     int[] newbufline = new int[this.bufsize + 2048];
/*  56 */     int[] newbufcolumn = new int[this.bufsize + 2048];
/*     */     try
/*     */     {
/*  60 */       if (wrapAround)
/*     */       {
/*  62 */         System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
/*  63 */         System.arraycopy(this.buffer, 0, newbuffer, this.bufsize - this.tokenBegin, this.bufpos);
/*     */ 
/*  65 */         this.buffer = newbuffer;
/*     */ 
/*  67 */         System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
/*  68 */         System.arraycopy(this.bufline, 0, newbufline, this.bufsize - this.tokenBegin, this.bufpos);
/*  69 */         this.bufline = newbufline;
/*     */ 
/*  71 */         System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
/*  72 */         System.arraycopy(this.bufcolumn, 0, newbufcolumn, this.bufsize - this.tokenBegin, this.bufpos);
/*  73 */         this.bufcolumn = newbufcolumn;
/*     */ 
/*  75 */         this.maxNextCharInd = (this.bufpos += this.bufsize - this.tokenBegin);
/*     */       }
/*     */       else
/*     */       {
/*  79 */         System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
/*  80 */         this.buffer = newbuffer;
/*     */ 
/*  82 */         System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
/*  83 */         this.bufline = newbufline;
/*     */ 
/*  85 */         System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
/*  86 */         this.bufcolumn = newbufcolumn;
/*     */ 
/*  88 */         this.maxNextCharInd = (this.bufpos -= this.tokenBegin);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  93 */       throw new Error(t.getMessage());
/*     */     }
/*     */ 
/*  97 */     this.bufsize += 2048;
/*  98 */     this.available = this.bufsize;
/*  99 */     this.tokenBegin = 0;
/*     */   }
/*     */ 
/*     */   protected void FillBuff() throws IOException
/*     */   {
/* 104 */     if (this.maxNextCharInd == this.available)
/*     */     {
/* 106 */       if (this.available == this.bufsize)
/*     */       {
/* 108 */         if (this.tokenBegin > 2048)
/*     */         {
/* 110 */           this.bufpos = (this.maxNextCharInd = 0);
/* 111 */           this.available = this.tokenBegin;
/*     */         }
/* 113 */         else if (this.tokenBegin < 0) {
/* 114 */           this.bufpos = (this.maxNextCharInd = 0);
/*     */         } else {
/* 116 */           ExpandBuff(false);
/*     */         }
/* 118 */       } else if (this.available > this.tokenBegin)
/* 119 */         this.available = this.bufsize;
/* 120 */       else if (this.tokenBegin - this.available < 2048)
/* 121 */         ExpandBuff(true);
/*     */       else
/* 123 */         this.available = this.tokenBegin;
/*     */     }
/*     */     try
/*     */     {
/*     */       int i;
/* 128 */       if ((i = this.inputStream.read(this.buffer, this.maxNextCharInd, this.available - this.maxNextCharInd)) == -1)
/*     */       {
/* 131 */         this.inputStream.close();
/* 132 */         throw new IOException();
/*     */       }
/*     */ 
/* 135 */       this.maxNextCharInd += i;
/* 136 */       return;
/*     */     }
/*     */     catch (IOException e) {
/* 139 */       this.bufpos -= 1;
/* 140 */       backup(0);
/* 141 */       if (this.tokenBegin == -1)
/* 142 */         this.tokenBegin = this.bufpos;
/* 143 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public char BeginToken() throws IOException
/*     */   {
/* 149 */     this.tokenBegin = -1;
/* 150 */     char c = readChar();
/* 151 */     this.tokenBegin = this.bufpos;
/*     */ 
/* 153 */     return c;
/*     */   }
/*     */ 
/*     */   protected void UpdateLineColumn(char c)
/*     */   {
/* 158 */     this.column += 1;
/*     */ 
/* 160 */     if (this.prevCharIsLF)
/*     */     {
/* 162 */       this.prevCharIsLF = false;
/* 163 */       this.line += (this.column = 1);
/*     */     }
/* 165 */     else if (this.prevCharIsCR)
/*     */     {
/* 167 */       this.prevCharIsCR = false;
/* 168 */       if (c == '\n')
/*     */       {
/* 170 */         this.prevCharIsLF = true;
/*     */       }
/*     */       else {
/* 173 */         this.line += (this.column = 1);
/*     */       }
/*     */     }
/* 176 */     switch (c)
/*     */     {
/*     */     case '\r':
/* 179 */       this.prevCharIsCR = true;
/* 180 */       break;
/*     */     case '\n':
/* 182 */       this.prevCharIsLF = true;
/* 183 */       break;
/*     */     case '\t':
/* 185 */       this.column -= 1;
/* 186 */       this.column += this.tabSize - this.column % this.tabSize;
/* 187 */       break;
/*     */     case '\013':
/*     */     case '\f':
/*     */     }
/*     */ 
/* 192 */     this.bufline[this.bufpos] = this.line;
/* 193 */     this.bufcolumn[this.bufpos] = this.column;
/*     */   }
/*     */ 
/*     */   public char readChar() throws IOException
/*     */   {
/* 198 */     if (this.inBuf > 0)
/*     */     {
/* 200 */       this.inBuf -= 1;
/*     */ 
/* 202 */       if (++this.bufpos == this.bufsize) {
/* 203 */         this.bufpos = 0;
/*     */       }
/* 205 */       return this.buffer[this.bufpos];
/*     */     }
/*     */ 
/* 208 */     if (++this.bufpos >= this.maxNextCharInd) {
/* 209 */       FillBuff();
/*     */     }
/* 211 */     char c = this.buffer[this.bufpos];
/*     */ 
/* 213 */     UpdateLineColumn(c);
/* 214 */     return c;
/*     */   }
/*     */ 
/*     */   /** @deprecated */
/*     */   public int getColumn()
/*     */   {
/* 223 */     return this.bufcolumn[this.bufpos];
/*     */   }
/*     */ 
/*     */   /** @deprecated */
/*     */   public int getLine()
/*     */   {
/* 232 */     return this.bufline[this.bufpos];
/*     */   }
/*     */ 
/*     */   public int getEndColumn() {
/* 236 */     return this.bufcolumn[this.bufpos];
/*     */   }
/*     */ 
/*     */   public int getEndLine() {
/* 240 */     return this.bufline[this.bufpos];
/*     */   }
/*     */ 
/*     */   public int getBeginColumn() {
/* 244 */     return this.bufcolumn[this.tokenBegin];
/*     */   }
/*     */ 
/*     */   public int getBeginLine() {
/* 248 */     return this.bufline[this.tokenBegin];
/*     */   }
/*     */ 
/*     */   public void backup(int amount)
/*     */   {
/* 253 */     this.inBuf += amount;
/* 254 */     if (this.bufpos -= amount < 0)
/* 255 */       this.bufpos += this.bufsize;
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(Reader dstream, int startline, int startcolumn, int buffersize)
/*     */   {
/* 261 */     this.inputStream = dstream;
/* 262 */     this.line = startline;
/* 263 */     this.column = (startcolumn - 1);
/*     */ 
/* 265 */     this.available = (this.bufsize = buffersize);
/* 266 */     this.buffer = new char[buffersize];
/* 267 */     this.bufline = new int[buffersize];
/* 268 */     this.bufcolumn = new int[buffersize];
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(Reader dstream, int startline, int startcolumn)
/*     */   {
/* 274 */     this(dstream, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(Reader dstream)
/*     */   {
/* 279 */     this(dstream, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(Reader dstream, int startline, int startcolumn, int buffersize)
/*     */   {
/* 284 */     this.inputStream = dstream;
/* 285 */     this.line = startline;
/* 286 */     this.column = (startcolumn - 1);
/*     */ 
/* 288 */     if ((this.buffer == null) || (buffersize != this.buffer.length))
/*     */     {
/* 290 */       this.available = (this.bufsize = buffersize);
/* 291 */       this.buffer = new char[buffersize];
/* 292 */       this.bufline = new int[buffersize];
/* 293 */       this.bufcolumn = new int[buffersize];
/*     */     }
/* 295 */     this.prevCharIsLF = (this.prevCharIsCR = 0);
/* 296 */     this.tokenBegin = (this.inBuf = this.maxNextCharInd = 0);
/* 297 */     this.bufpos = -1;
/*     */   }
/*     */ 
/*     */   public void ReInit(Reader dstream, int startline, int startcolumn)
/*     */   {
/* 303 */     ReInit(dstream, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(Reader dstream)
/*     */   {
/* 308 */     ReInit(dstream, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream, String encoding, int startline, int startcolumn, int buffersize) throws UnsupportedEncodingException
/*     */   {
/* 313 */     this(encoding == null ? new InputStreamReader(dstream) : new InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream, int startline, int startcolumn, int buffersize)
/*     */   {
/* 319 */     this(new InputStreamReader(dstream), startline, startcolumn, buffersize);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream, String encoding, int startline, int startcolumn)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 325 */     this(dstream, encoding, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream, int startline, int startcolumn)
/*     */   {
/* 331 */     this(dstream, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream, String encoding) throws UnsupportedEncodingException
/*     */   {
/* 336 */     this(dstream, encoding, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public SimpleCharStream(InputStream dstream)
/*     */   {
/* 341 */     this(dstream, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn, int buffersize)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 347 */     ReInit(encoding == null ? new InputStreamReader(dstream) : new InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream, int startline, int startcolumn, int buffersize)
/*     */   {
/* 353 */     ReInit(new InputStreamReader(dstream), startline, startcolumn, buffersize);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream, String encoding) throws UnsupportedEncodingException
/*     */   {
/* 358 */     ReInit(dstream, encoding, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream)
/*     */   {
/* 363 */     ReInit(dstream, 1, 1, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn) throws UnsupportedEncodingException
/*     */   {
/* 368 */     ReInit(dstream, encoding, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public void ReInit(InputStream dstream, int startline, int startcolumn)
/*     */   {
/* 373 */     ReInit(dstream, startline, startcolumn, 4096);
/*     */   }
/*     */ 
/*     */   public String GetImage() {
/* 377 */     if (this.bufpos >= this.tokenBegin) {
/* 378 */       return new String(this.buffer, this.tokenBegin, this.bufpos - this.tokenBegin + 1);
/*     */     }
/* 380 */     return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin) + new String(this.buffer, 0, this.bufpos + 1);
/*     */   }
/*     */ 
/*     */   public char[] GetSuffix(int len)
/*     */   {
/* 386 */     char[] ret = new char[len];
/*     */ 
/* 388 */     if (this.bufpos + 1 >= len) {
/* 389 */       System.arraycopy(this.buffer, this.bufpos - len + 1, ret, 0, len);
/*     */     }
/*     */     else {
/* 392 */       System.arraycopy(this.buffer, this.bufsize - (len - this.bufpos - 1), ret, 0, len - this.bufpos - 1);
/*     */ 
/* 394 */       System.arraycopy(this.buffer, 0, ret, len - this.bufpos - 1, this.bufpos + 1);
/*     */     }
/*     */ 
/* 397 */     return ret;
/*     */   }
/*     */ 
/*     */   public void Done()
/*     */   {
/* 402 */     this.buffer = null;
/* 403 */     this.bufline = null;
/* 404 */     this.bufcolumn = null;
/*     */   }
/*     */ 
/*     */   public void adjustBeginLineColumn(int newLine, int newCol)
/*     */   {
/* 412 */     int start = this.tokenBegin;
/*     */     int len;
/*     */     int len;
/* 415 */     if (this.bufpos >= this.tokenBegin)
/*     */     {
/* 417 */       len = this.bufpos - this.tokenBegin + this.inBuf + 1;
/*     */     }
/*     */     else
/*     */     {
/* 421 */       len = this.bufsize - this.tokenBegin + this.bufpos + 1 + this.inBuf;
/*     */     }
/*     */ 
/* 424 */     int i = 0; int j = 0; int k = 0;
/* 425 */     int nextColDiff = 0; int columnDiff = 0;
/*     */ 
/* 428 */     while ((i < len) && (this.bufline[(j = start % this.bufsize)] == this.bufline[(k = ++start % this.bufsize)]))
/*     */     {
/* 430 */       this.bufline[j] = newLine;
/* 431 */       nextColDiff = columnDiff + this.bufcolumn[k] - this.bufcolumn[j];
/* 432 */       this.bufcolumn[j] = (newCol + columnDiff);
/* 433 */       columnDiff = nextColDiff;
/* 434 */       i++;
/*     */     }
/*     */ 
/* 437 */     if (i < len)
/*     */     {
/* 439 */       this.bufline[j] = (newLine++);
/* 440 */       this.bufcolumn[j] = (newCol + columnDiff);
/*     */ 
/* 442 */       while (i++ < len)
/*     */       {
/* 444 */         if (this.bufline[(j = start % this.bufsize)] != this.bufline[(++start % this.bufsize)])
/* 445 */           this.bufline[j] = (newLine++);
/*     */         else {
/* 447 */           this.bufline[j] = newLine;
/*     */         }
/*     */       }
/*     */     }
/* 451 */     this.line = this.bufline[j];
/* 452 */     this.column = this.bufcolumn[j];
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.SimpleCharStream
 * JD-Core Version:    0.6.2
 */