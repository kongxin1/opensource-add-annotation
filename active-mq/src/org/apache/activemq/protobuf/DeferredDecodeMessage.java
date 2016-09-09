/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ import java.io.IOException;
/*    */ 
/*    */ public abstract class DeferredDecodeMessage<T> extends BaseMessage<T>
/*    */ {
/*    */   protected Buffer encodedForm;
/* 24 */   protected boolean decoded = true;
/*    */ 
/*    */   public T mergeFramed(CodedInputStream input) throws IOException
/*    */   {
/* 28 */     int length = input.readRawVarint32();
/* 29 */     int oldLimit = input.pushLimit(length);
/* 30 */     Object rc = mergeUnframed(input.readRawBytes(length));
/* 31 */     input.popLimit(oldLimit);
/* 32 */     return rc;
/*    */   }
/*    */ 
/*    */   public T mergeUnframed(Buffer data)
/*    */     throws InvalidProtocolBufferException
/*    */   {
/* 38 */     this.encodedForm = data;
/* 39 */     this.decoded = false;
/* 40 */     return this;
/*    */   }
/*    */ 
/*    */   public Buffer toUnframedBuffer()
/*    */   {
/* 45 */     if (this.encodedForm == null) {
/* 46 */       this.encodedForm = super.toUnframedBuffer();
/*    */     }
/* 48 */     return this.encodedForm;
/*    */   }
/*    */ 
/*    */   protected void load() {
/* 52 */     if (!this.decoded) {
/* 53 */       this.decoded = true;
/*    */       try {
/* 55 */         Buffer originalForm = this.encodedForm;
/* 56 */         this.encodedForm = null;
/* 57 */         CodedInputStream input = new CodedInputStream(originalForm);
/* 58 */         mergeUnframed(input);
/* 59 */         input.checkLastTagWas(0);
/*    */ 
/* 62 */         this.encodedForm = originalForm;
/* 63 */         checktInitialized();
/*    */       } catch (Throwable e) {
/* 65 */         throw new RuntimeException("Deferred message decoding failed: " + e.getMessage(), e);
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   protected void loadAndClear() {
/* 71 */     super.loadAndClear();
/* 72 */     load();
/* 73 */     this.encodedForm = null;
/*    */   }
/*    */ 
/*    */   public void clear() {
/* 77 */     super.clear();
/* 78 */     this.encodedForm = null;
/* 79 */     this.decoded = true;
/*    */   }
/*    */ 
/*    */   public boolean isDecoded() {
/* 83 */     return this.decoded;
/*    */   }
/*    */ 
/*    */   public boolean isEncoded() {
/* 87 */     return this.encodedForm != null;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.DeferredDecodeMessage
 * JD-Core Version:    0.6.2
 */