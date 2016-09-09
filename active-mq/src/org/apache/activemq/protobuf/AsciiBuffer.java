/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ public final class AsciiBuffer extends Buffer
/*    */ {
/*    */   private int hashCode;
/*    */ 
/*    */   public AsciiBuffer(Buffer other)
/*    */   {
/* 25 */     super(other);
/*    */   }
/*    */ 
/*    */   public AsciiBuffer(byte[] data, int offset, int length) {
/* 29 */     super(data, offset, length);
/*    */   }
/*    */ 
/*    */   public AsciiBuffer(byte[] data) {
/* 33 */     super(data);
/*    */   }
/*    */ 
/*    */   public AsciiBuffer(String input) {
/* 37 */     super(encode(input));
/*    */   }
/*    */ 
/*    */   public AsciiBuffer compact() {
/* 41 */     if (this.length != this.data.length) {
/* 42 */       return new AsciiBuffer(toByteArray());
/*    */     }
/* 44 */     return this;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 49 */     return decode(this);
/*    */   }
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 54 */     if (obj == this) {
/* 55 */       return true;
/*    */     }
/* 57 */     if ((obj == null) || (obj.getClass() != AsciiBuffer.class)) {
/* 58 */       return false;
/*    */     }
/* 60 */     return equals((Buffer)obj);
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 65 */     if (this.hashCode == 0) {
/* 66 */       this.hashCode = super.hashCode();
/*    */     }
/* 68 */     return this.hashCode;
/*    */   }
/*    */ 
/*    */   public static byte[] encode(String value)
/*    */   {
/* 73 */     int size = value.length();
/* 74 */     byte[] rc = new byte[size];
/* 75 */     for (int i = 0; i < size; i++) {
/* 76 */       rc[i] = ((byte)(value.charAt(i) & 0xFF));
/*    */     }
/* 78 */     return rc;
/*    */   }
/*    */ 
/*    */   public static String decode(Buffer value) {
/* 82 */     int size = value.getLength();
/* 83 */     char[] rc = new char[size];
/* 84 */     for (int i = 0; i < size; i++) {
/* 85 */       rc[i] = ((char)(value.byteAt(i) & 0xFF));
/*    */     }
/* 87 */     return new String(rc);
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.AsciiBuffer
 * JD-Core Version:    0.6.2
 */