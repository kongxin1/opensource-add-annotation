/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ import java.io.UnsupportedEncodingException;
/*    */ 
/*    */ public final class UTF8Buffer extends Buffer
/*    */ {
/*    */   int hashCode;
/*    */   String value;
/*    */ 
/*    */   public UTF8Buffer(Buffer other)
/*    */   {
/* 27 */     super(other);
/*    */   }
/*    */ 
/*    */   public UTF8Buffer(byte[] data, int offset, int length) {
/* 31 */     super(data, offset, length);
/*    */   }
/*    */ 
/*    */   public UTF8Buffer(byte[] data) {
/* 35 */     super(data);
/*    */   }
/*    */ 
/*    */   public UTF8Buffer(String input) {
/* 39 */     super(encode(input));
/*    */   }
/*    */ 
/*    */   public UTF8Buffer compact() {
/* 43 */     if (this.length != this.data.length) {
/* 44 */       return new UTF8Buffer(toByteArray());
/*    */     }
/* 46 */     return this;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 51 */     if (this.value == null) {
/* 52 */       this.value = decode(this);
/*    */     }
/* 54 */     return this.value;
/*    */   }
/*    */ 
/*    */   public int compareTo(Buffer other)
/*    */   {
/* 60 */     return toString().compareTo(other.toString());
/*    */   }
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 65 */     if (obj == this) {
/* 66 */       return true;
/*    */     }
/* 68 */     if ((obj == null) || (obj.getClass() != UTF8Buffer.class)) {
/* 69 */       return false;
/*    */     }
/* 71 */     return equals((Buffer)obj);
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 76 */     if (this.hashCode == 0) {
/* 77 */       this.hashCode = super.hashCode();
/*    */     }
/* 79 */     return this.hashCode;
/*    */   }
/*    */ 
/*    */   public static byte[] encode(String value)
/*    */   {
/*    */     try {
/* 85 */       return value.getBytes("UTF-8"); } catch (UnsupportedEncodingException e) {
/*    */     }
/* 87 */     throw new RuntimeException("A UnsupportedEncodingException was thrown for teh UTF-8 encoding. (This should never happen)");
/*    */   }
/*    */ 
/*    */   public static String decode(Buffer buffer)
/*    */   {
/*    */     try
/*    */     {
/* 94 */       return new String(buffer.getData(), buffer.getOffset(), buffer.getLength(), "UTF-8"); } catch (UnsupportedEncodingException e) {
/*    */     }
/* 96 */     throw new RuntimeException("A UnsupportedEncodingException was thrown for teh UTF-8 encoding. (This should never happen)");
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.UTF8Buffer
 * JD-Core Version:    0.6.2
 */