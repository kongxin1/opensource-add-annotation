/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ import java.io.EOFException;
/*    */ import java.io.IOException;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public final class BufferOutputStream extends OutputStream
/*    */ {
/*    */   byte[] buffer;
/*    */   int offset;
/*    */   int limit;
/*    */   int pos;
/*    */ 
/*    */   public BufferOutputStream(int size)
/*    */   {
/* 38 */     this(new byte[size]);
/*    */   }
/*    */ 
/*    */   public BufferOutputStream(byte[] buffer) {
/* 42 */     this.buffer = buffer;
/* 43 */     this.limit = buffer.length;
/*    */   }
/*    */ 
/*    */   public BufferOutputStream(Buffer data) {
/* 47 */     this.buffer = data.data;
/* 48 */     this.pos = (this.offset = data.offset);
/* 49 */     this.limit = (data.offset + data.length);
/*    */   }
/*    */ 
/*    */   public void write(int b) throws IOException
/*    */   {
/* 54 */     int newPos = this.pos + 1;
/* 55 */     checkCapacity(newPos);
/* 56 */     this.buffer[this.pos] = ((byte)b);
/* 57 */     this.pos = newPos;
/*    */   }
/*    */ 
/*    */   public void write(byte[] b, int off, int len) throws IOException {
/* 61 */     int newPos = this.pos + len;
/* 62 */     checkCapacity(newPos);
/* 63 */     System.arraycopy(b, off, this.buffer, this.pos, len);
/* 64 */     this.pos = newPos;
/*    */   }
/*    */ 
/*    */   public Buffer getNextBuffer(int len) throws IOException {
/* 68 */     int newPos = this.pos + len;
/* 69 */     checkCapacity(newPos);
/* 70 */     return new Buffer(this.buffer, this.pos, len);
/*    */   }
/*    */ 
/*    */   private void checkCapacity(int minimumCapacity)
/*    */     throws IOException
/*    */   {
/* 79 */     if (minimumCapacity > this.limit)
/* 80 */       throw new EOFException("Buffer limit reached.");
/*    */   }
/*    */ 
/*    */   public void reset()
/*    */   {
/* 85 */     this.pos = this.offset;
/*    */   }
/*    */ 
/*    */   public Buffer toBuffer() {
/* 89 */     return new Buffer(this.buffer, this.offset, this.pos);
/*    */   }
/*    */ 
/*    */   public byte[] toByteArray() {
/* 93 */     return toBuffer().toByteArray();
/*    */   }
/*    */ 
/*    */   public int size() {
/* 97 */     return this.offset - this.pos;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.BufferOutputStream
 * JD-Core Version:    0.6.2
 */