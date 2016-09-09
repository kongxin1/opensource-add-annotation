/*     */ package org.apache.activemq.protobuf;
/*     */ 
/*     */ import java.io.FilterOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ 
/*     */ public final class CodedOutputStream extends FilterOutputStream
/*     */ {
/*     */   private BufferOutputStream bos;
/*     */   public static final int LITTLE_ENDIAN_32_SIZE = 4;
/*     */   public static final int LITTLE_ENDIAN_64_SIZE = 8;
/*     */ 
/*     */   public CodedOutputStream(OutputStream os)
/*     */   {
/*  43 */     super(os);
/*  44 */     if ((os instanceof BufferOutputStream))
/*  45 */       this.bos = ((BufferOutputStream)os);
/*     */   }
/*     */ 
/*     */   public CodedOutputStream(byte[] data)
/*     */   {
/*  50 */     super(new BufferOutputStream(data));
/*     */   }
/*     */ 
/*     */   public CodedOutputStream(Buffer data) {
/*  54 */     super(new BufferOutputStream(data));
/*     */   }
/*     */ 
/*     */   public void writeDouble(int fieldNumber, double value)
/*     */     throws IOException
/*     */   {
/*  61 */     writeTag(fieldNumber, 1);
/*  62 */     writeRawLittleEndian64(Double.doubleToRawLongBits(value));
/*     */   }
/*     */ 
/*     */   public void writeFloat(int fieldNumber, float value) throws IOException
/*     */   {
/*  67 */     writeTag(fieldNumber, 5);
/*  68 */     writeRawLittleEndian32(Float.floatToRawIntBits(value));
/*     */   }
/*     */ 
/*     */   public void writeUInt64(int fieldNumber, long value) throws IOException
/*     */   {
/*  73 */     writeTag(fieldNumber, 0);
/*  74 */     writeRawVarint64(value);
/*     */   }
/*     */ 
/*     */   public void writeInt64(int fieldNumber, long value) throws IOException
/*     */   {
/*  79 */     writeTag(fieldNumber, 0);
/*  80 */     writeRawVarint64(value);
/*     */   }
/*     */ 
/*     */   public void writeInt32(int fieldNumber, int value) throws IOException
/*     */   {
/*  85 */     writeTag(fieldNumber, 0);
/*  86 */     if (value >= 0) {
/*  87 */       writeRawVarint32(value);
/*     */     }
/*     */     else
/*  90 */       writeRawVarint64(value);
/*     */   }
/*     */ 
/*     */   public void writeFixed64(int fieldNumber, long value)
/*     */     throws IOException
/*     */   {
/*  96 */     writeTag(fieldNumber, 1);
/*  97 */     writeRawLittleEndian64(value);
/*     */   }
/*     */ 
/*     */   public void writeFixed32(int fieldNumber, int value) throws IOException
/*     */   {
/* 102 */     writeTag(fieldNumber, 5);
/* 103 */     writeRawLittleEndian32(value);
/*     */   }
/*     */ 
/*     */   public void writeBool(int fieldNumber, boolean value) throws IOException
/*     */   {
/* 108 */     writeTag(fieldNumber, 0);
/* 109 */     writeRawByte(value ? 1 : 0);
/*     */   }
/*     */ 
/*     */   public void writeString(int fieldNumber, String value) throws IOException
/*     */   {
/* 114 */     writeTag(fieldNumber, 2);
/*     */ 
/* 120 */     byte[] bytes = value.getBytes("UTF-8");
/* 121 */     writeRawVarint32(bytes.length);
/* 122 */     writeRawBytes(bytes);
/*     */   }
/*     */ 
/*     */   public void writeBytes(int fieldNumber, Buffer value) throws IOException
/*     */   {
/* 127 */     writeTag(fieldNumber, 2);
/* 128 */     writeRawVarint32(value.length);
/* 129 */     writeRawBytes(value.data, value.offset, value.length);
/*     */   }
/*     */ 
/*     */   public void writeUInt32(int fieldNumber, int value) throws IOException
/*     */   {
/* 134 */     writeTag(fieldNumber, 0);
/* 135 */     writeRawVarint32(value);
/*     */   }
/*     */ 
/*     */   public void writeEnum(int fieldNumber, int value)
/*     */     throws IOException
/*     */   {
/* 143 */     writeTag(fieldNumber, 0);
/* 144 */     writeRawVarint32(value);
/*     */   }
/*     */ 
/*     */   public void writeSFixed32(int fieldNumber, int value) throws IOException
/*     */   {
/* 149 */     writeTag(fieldNumber, 5);
/* 150 */     writeRawLittleEndian32(value);
/*     */   }
/*     */ 
/*     */   public void writeSFixed64(int fieldNumber, long value) throws IOException
/*     */   {
/* 155 */     writeTag(fieldNumber, 1);
/* 156 */     writeRawLittleEndian64(value);
/*     */   }
/*     */ 
/*     */   public void writeSInt32(int fieldNumber, int value) throws IOException
/*     */   {
/* 161 */     writeTag(fieldNumber, 0);
/* 162 */     writeRawVarint32(encodeZigZag32(value));
/*     */   }
/*     */ 
/*     */   public void writeSInt64(int fieldNumber, long value) throws IOException
/*     */   {
/* 167 */     writeTag(fieldNumber, 0);
/* 168 */     writeRawVarint64(encodeZigZag64(value));
/*     */   }
/*     */ 
/*     */   public static int computeDoubleSize(int fieldNumber, double value)
/*     */   {
/* 178 */     return computeTagSize(fieldNumber) + 8;
/*     */   }
/*     */ 
/*     */   public static int computeFloatSize(int fieldNumber, float value)
/*     */   {
/* 186 */     return computeTagSize(fieldNumber) + 4;
/*     */   }
/*     */ 
/*     */   public static int computeUInt64Size(int fieldNumber, long value)
/*     */   {
/* 194 */     return computeTagSize(fieldNumber) + computeRawVarint64Size(value);
/*     */   }
/*     */ 
/*     */   public static int computeInt64Size(int fieldNumber, long value)
/*     */   {
/* 202 */     return computeTagSize(fieldNumber) + computeRawVarint64Size(value);
/*     */   }
/*     */ 
/*     */   public static int computeInt32Size(int fieldNumber, int value)
/*     */   {
/* 210 */     if (value >= 0) {
/* 211 */       return computeTagSize(fieldNumber) + computeRawVarint32Size(value);
/*     */     }
/*     */ 
/* 214 */     return computeTagSize(fieldNumber) + 10;
/*     */   }
/*     */ 
/*     */   public static int computeFixed64Size(int fieldNumber, long value)
/*     */   {
/* 223 */     return computeTagSize(fieldNumber) + 8;
/*     */   }
/*     */ 
/*     */   public static int computeFixed32Size(int fieldNumber, int value)
/*     */   {
/* 231 */     return computeTagSize(fieldNumber) + 4;
/*     */   }
/*     */ 
/*     */   public static int computeBoolSize(int fieldNumber, boolean value)
/*     */   {
/* 239 */     return computeTagSize(fieldNumber) + 1;
/*     */   }
/*     */ 
/*     */   public static int computeStringSize(int fieldNumber, String value)
/*     */   {
/*     */     try
/*     */     {
/* 248 */       byte[] bytes = value.getBytes("UTF-8");
/* 249 */       return computeTagSize(fieldNumber) + computeRawVarint32Size(bytes.length) + bytes.length;
/*     */     } catch (UnsupportedEncodingException e) {
/* 251 */       throw new RuntimeException("UTF-8 not supported.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int computeBytesSize(int fieldNumber, Buffer value)
/*     */   {
/* 260 */     return computeTagSize(fieldNumber) + computeRawVarint32Size(value.length) + value.length;
/*     */   }
/*     */ 
/*     */   public static int computeUInt32Size(int fieldNumber, int value)
/*     */   {
/* 268 */     return computeTagSize(fieldNumber) + computeRawVarint32Size(value);
/*     */   }
/*     */ 
/*     */   public static int computeEnumSize(int fieldNumber, int value)
/*     */   {
/* 277 */     return computeTagSize(fieldNumber) + computeRawVarint32Size(value);
/*     */   }
/*     */ 
/*     */   public static int computeSFixed32Size(int fieldNumber, int value)
/*     */   {
/* 285 */     return computeTagSize(fieldNumber) + 4;
/*     */   }
/*     */ 
/*     */   public static int computeSFixed64Size(int fieldNumber, long value)
/*     */   {
/* 293 */     return computeTagSize(fieldNumber) + 8;
/*     */   }
/*     */ 
/*     */   public static int computeSInt32Size(int fieldNumber, int value)
/*     */   {
/* 301 */     return computeTagSize(fieldNumber) + computeRawVarint32Size(encodeZigZag32(value));
/*     */   }
/*     */ 
/*     */   public static int computeSInt64Size(int fieldNumber, long value)
/*     */   {
/* 309 */     return computeTagSize(fieldNumber) + computeRawVarint64Size(encodeZigZag64(value));
/*     */   }
/*     */ 
/*     */   public void writeRawByte(byte value) throws IOException
/*     */   {
/* 314 */     this.out.write(value);
/*     */   }
/*     */ 
/*     */   public void writeRawByte(int value) throws IOException
/*     */   {
/* 319 */     writeRawByte((byte)value);
/*     */   }
/*     */ 
/*     */   public void writeRawBytes(byte[] value) throws IOException
/*     */   {
/* 324 */     writeRawBytes(value, 0, value.length);
/*     */   }
/*     */ 
/*     */   public void writeRawBytes(byte[] value, int offset, int length) throws IOException
/*     */   {
/* 329 */     this.out.write(value, offset, length);
/*     */   }
/*     */ 
/*     */   public void writeRawBytes(Buffer data) throws IOException {
/* 333 */     this.out.write(data.data, data.offset, data.length);
/*     */   }
/*     */ 
/*     */   public void writeTag(int fieldNumber, int wireType) throws IOException
/*     */   {
/* 338 */     writeRawVarint32(WireFormat.makeTag(fieldNumber, wireType));
/*     */   }
/*     */ 
/*     */   public static int computeTagSize(int fieldNumber)
/*     */   {
/* 343 */     return computeRawVarint32Size(WireFormat.makeTag(fieldNumber, 0));
/*     */   }
/*     */ 
/*     */   public void writeRawVarint32(int value)
/*     */     throws IOException
/*     */   {
/*     */     while (true)
/*     */     {
/* 352 */       if ((value & 0xFFFFFF80) == 0) {
/* 353 */         writeRawByte(value);
/* 354 */         return;
/*     */       }
/* 356 */       writeRawByte(value & 0x7F | 0x80);
/* 357 */       value >>>= 7;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int computeRawVarint32Size(int value)
/*     */   {
/* 368 */     if ((value & 0xFFFFFF80) == 0)
/* 369 */       return 1;
/* 370 */     if ((value & 0xFFFFC000) == 0)
/* 371 */       return 2;
/* 372 */     if ((value & 0xFFE00000) == 0)
/* 373 */       return 3;
/* 374 */     if ((value & 0xF0000000) == 0)
/* 375 */       return 4;
/* 376 */     return 5;
/*     */   }
/*     */ 
/*     */   public void writeRawVarint64(long value) throws IOException
/*     */   {
/*     */     while (true) {
/* 382 */       if ((value & 0xFFFFFF80) == 0L) {
/* 383 */         writeRawByte((int)value);
/* 384 */         return;
/*     */       }
/* 386 */       writeRawByte((int)value & 0x7F | 0x80);
/* 387 */       value >>>= 7;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int computeRawVarint64Size(long value)
/*     */   {
/* 394 */     if ((value & 0xFFFFFF80) == 0L)
/* 395 */       return 1;
/* 396 */     if ((value & 0xFFFFC000) == 0L)
/* 397 */       return 2;
/* 398 */     if ((value & 0xFFE00000) == 0L)
/* 399 */       return 3;
/* 400 */     if ((value & 0xF0000000) == 0L)
/* 401 */       return 4;
/* 402 */     if ((value & 0x0) == 0L)
/* 403 */       return 5;
/* 404 */     if ((value & 0x0) == 0L)
/* 405 */       return 6;
/* 406 */     if ((value & 0x0) == 0L)
/* 407 */       return 7;
/* 408 */     if ((value & 0x0) == 0L)
/* 409 */       return 8;
/* 410 */     if ((value & 0x0) == 0L)
/* 411 */       return 9;
/* 412 */     return 10;
/*     */   }
/*     */ 
/*     */   public void writeRawLittleEndian32(int value) throws IOException
/*     */   {
/* 417 */     writeRawByte(value & 0xFF);
/* 418 */     writeRawByte(value >> 8 & 0xFF);
/* 419 */     writeRawByte(value >> 16 & 0xFF);
/* 420 */     writeRawByte(value >> 24 & 0xFF);
/*     */   }
/*     */ 
/*     */   public void writeRawLittleEndian64(long value)
/*     */     throws IOException
/*     */   {
/* 427 */     writeRawByte((int)value & 0xFF);
/* 428 */     writeRawByte((int)(value >> 8) & 0xFF);
/* 429 */     writeRawByte((int)(value >> 16) & 0xFF);
/* 430 */     writeRawByte((int)(value >> 24) & 0xFF);
/* 431 */     writeRawByte((int)(value >> 32) & 0xFF);
/* 432 */     writeRawByte((int)(value >> 40) & 0xFF);
/* 433 */     writeRawByte((int)(value >> 48) & 0xFF);
/* 434 */     writeRawByte((int)(value >> 56) & 0xFF);
/*     */   }
/*     */ 
/*     */   public static int encodeZigZag32(int n)
/*     */   {
/* 452 */     return n << 1 ^ n >> 31;
/*     */   }
/*     */ 
/*     */   public static long encodeZigZag64(long n)
/*     */   {
/* 468 */     return n << 1 ^ n >> 63;
/*     */   }
/*     */ 
/*     */   public void checkNoSpaceLeft() {
/*     */   }
/*     */ 
/*     */   public Buffer getNextBuffer(int size) throws IOException {
/* 475 */     if (this.bos == null) {
/* 476 */       return null;
/*     */     }
/* 478 */     return this.bos.getNextBuffer(size);
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.CodedOutputStream
 * JD-Core Version:    0.6.2
 */