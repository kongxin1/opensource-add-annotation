/*     */ package org.apache.activemq.protobuf;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public final class CodedInputStream extends FilterInputStream
/*     */ {
/*  38 */   private int lastTag = 0;
/*  39 */   private int limit = 2147483647;
/*     */   private int pos;
/*     */   private BufferInputStream bis;
/*     */ 
/*     */   public CodedInputStream(InputStream in)
/*     */   {
/*  44 */     super(in);
/*  45 */     if (in.getClass() == BufferInputStream.class)
/*  46 */       this.bis = ((BufferInputStream)in);
/*     */   }
/*     */ 
/*     */   public CodedInputStream(Buffer data)
/*     */   {
/*  51 */     this(new BufferInputStream(data));
/*  52 */     this.limit = data.length;
/*     */   }
/*     */ 
/*     */   public CodedInputStream(byte[] data) {
/*  56 */     this(new BufferInputStream(data));
/*  57 */     this.limit = data.length;
/*     */   }
/*     */ 
/*     */   public int readTag()
/*     */     throws IOException
/*     */   {
/*  67 */     if (this.pos >= this.limit) {
/*  68 */       this.lastTag = 0;
/*  69 */       return 0;
/*     */     }
/*     */     try {
/*  72 */       this.lastTag = readRawVarint32();
/*  73 */       if (this.lastTag == 0)
/*     */       {
/*  75 */         throw InvalidProtocolBufferException.invalidTag();
/*     */       }
/*  77 */       return this.lastTag;
/*     */     } catch (EOFException e) {
/*  79 */       this.lastTag = 0;
/*  80 */     }return 0;
/*     */   }
/*     */ 
/*     */   public void checkLastTagWas(int value)
/*     */     throws InvalidProtocolBufferException
/*     */   {
/*  94 */     if (this.lastTag != value)
/*  95 */       throw InvalidProtocolBufferException.invalidEndTag();
/*     */   }
/*     */ 
/*     */   public boolean skipField(int tag)
/*     */     throws IOException
/*     */   {
/* 106 */     switch (WireFormat.getTagWireType(tag)) {
/*     */     case 0:
/* 108 */       readInt32();
/* 109 */       return true;
/*     */     case 1:
/* 111 */       readRawLittleEndian64();
/* 112 */       return true;
/*     */     case 2:
/* 114 */       skipRawBytes(readRawVarint32());
/* 115 */       return true;
/*     */     case 3:
/* 117 */       skipMessage();
/* 118 */       checkLastTagWas(WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), 4));
/* 119 */       return true;
/*     */     case 4:
/* 121 */       return false;
/*     */     case 5:
/* 123 */       readRawLittleEndian32();
/* 124 */       return true;
/*     */     }
/* 126 */     throw InvalidProtocolBufferException.invalidWireType();
/*     */   }
/*     */ 
/*     */   public void skipMessage()
/*     */     throws IOException
/*     */   {
/*     */     while (true)
/*     */     {
/* 136 */       int tag = readTag();
/* 137 */       if ((tag == 0) || (!skipField(tag)))
/* 138 */         return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public double readDouble()
/*     */     throws IOException
/*     */   {
/* 146 */     return Double.longBitsToDouble(readRawLittleEndian64());
/*     */   }
/*     */ 
/*     */   public float readFloat() throws IOException
/*     */   {
/* 151 */     return Float.intBitsToFloat(readRawLittleEndian32());
/*     */   }
/*     */ 
/*     */   public long readUInt64() throws IOException
/*     */   {
/* 156 */     return readRawVarint64();
/*     */   }
/*     */ 
/*     */   public long readInt64() throws IOException
/*     */   {
/* 161 */     return readRawVarint64();
/*     */   }
/*     */ 
/*     */   public int readInt32() throws IOException
/*     */   {
/* 166 */     return readRawVarint32();
/*     */   }
/*     */ 
/*     */   public long readFixed64() throws IOException
/*     */   {
/* 171 */     return readRawLittleEndian64();
/*     */   }
/*     */ 
/*     */   public int readFixed32() throws IOException
/*     */   {
/* 176 */     return readRawLittleEndian32();
/*     */   }
/*     */ 
/*     */   public boolean readBool() throws IOException
/*     */   {
/* 181 */     return readRawVarint32() != 0;
/*     */   }
/*     */ 
/*     */   public String readString() throws IOException
/*     */   {
/* 186 */     int size = readRawVarint32();
/* 187 */     Buffer data = readRawBytes(size);
/* 188 */     return new String(data.data, data.offset, data.length, "UTF-8");
/*     */   }
/*     */ 
/*     */   public Buffer readBytes() throws IOException
/*     */   {
/* 193 */     int size = readRawVarint32();
/* 194 */     return readRawBytes(size);
/*     */   }
/*     */ 
/*     */   public int readUInt32() throws IOException
/*     */   {
/* 199 */     return readRawVarint32();
/*     */   }
/*     */ 
/*     */   public int readEnum()
/*     */     throws IOException
/*     */   {
/* 207 */     return readRawVarint32();
/*     */   }
/*     */ 
/*     */   public int readSFixed32() throws IOException
/*     */   {
/* 212 */     return readRawLittleEndian32();
/*     */   }
/*     */ 
/*     */   public long readSFixed64() throws IOException
/*     */   {
/* 217 */     return readRawLittleEndian64();
/*     */   }
/*     */ 
/*     */   public int readSInt32() throws IOException
/*     */   {
/* 222 */     return decodeZigZag32(readRawVarint32());
/*     */   }
/*     */ 
/*     */   public long readSInt64() throws IOException
/*     */   {
/* 227 */     return decodeZigZag64(readRawVarint64());
/*     */   }
/*     */ 
/*     */   public int readRawVarint32()
/*     */     throws IOException
/*     */   {
/* 237 */     byte tmp = readRawByte();
/* 238 */     if (tmp >= 0) {
/* 239 */       return tmp;
/*     */     }
/* 241 */     int result = tmp & 0x7F;
/* 242 */     if ((tmp = readRawByte()) >= 0) {
/* 243 */       result |= tmp << 7;
/*     */     } else {
/* 245 */       result |= (tmp & 0x7F) << 7;
/* 246 */       if ((tmp = readRawByte()) >= 0) {
/* 247 */         result |= tmp << 14;
/*     */       } else {
/* 249 */         result |= (tmp & 0x7F) << 14;
/* 250 */         if ((tmp = readRawByte()) >= 0) {
/* 251 */           result |= tmp << 21;
/*     */         } else {
/* 253 */           result |= (tmp & 0x7F) << 21;
/* 254 */           result |= (tmp = readRawByte()) << 28;
/* 255 */           if (tmp < 0)
/*     */           {
/* 257 */             for (int i = 0; i < 5; i++) {
/* 258 */               if (readRawByte() >= 0)
/* 259 */                 return result;
/*     */             }
/* 261 */             throw InvalidProtocolBufferException.malformedVarint();
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 266 */     return result;
/*     */   }
/*     */ 
/*     */   public long readRawVarint64() throws IOException
/*     */   {
/* 271 */     int shift = 0;
/* 272 */     long result = 0L;
/* 273 */     while (shift < 64) {
/* 274 */       byte b = readRawByte();
/* 275 */       result |= (b & 0x7F) << shift;
/* 276 */       if ((b & 0x80) == 0)
/* 277 */         return result;
/* 278 */       shift += 7;
/*     */     }
/* 280 */     throw InvalidProtocolBufferException.malformedVarint();
/*     */   }
/*     */ 
/*     */   public int readRawLittleEndian32() throws IOException
/*     */   {
/* 285 */     byte b1 = readRawByte();
/* 286 */     byte b2 = readRawByte();
/* 287 */     byte b3 = readRawByte();
/* 288 */     byte b4 = readRawByte();
/* 289 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24;
/*     */   }
/*     */ 
/*     */   public long readRawLittleEndian64() throws IOException
/*     */   {
/* 294 */     byte b1 = readRawByte();
/* 295 */     byte b2 = readRawByte();
/* 296 */     byte b3 = readRawByte();
/* 297 */     byte b4 = readRawByte();
/* 298 */     byte b5 = readRawByte();
/* 299 */     byte b6 = readRawByte();
/* 300 */     byte b7 = readRawByte();
/* 301 */     byte b8 = readRawByte();
/* 302 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24 | (b5 & 0xFF) << 32 | (b6 & 0xFF) << 40 | (b7 & 0xFF) << 48 | (b8 & 0xFF) << 56;
/*     */   }
/*     */ 
/*     */   public static int decodeZigZag32(int n)
/*     */   {
/* 317 */     return n >>> 1 ^ -(n & 0x1);
/*     */   }
/*     */ 
/*     */   public static long decodeZigZag64(long n)
/*     */   {
/* 332 */     return n >>> 1 ^ -(n & 1L);
/*     */   }
/*     */ 
/*     */   public byte readRawByte()
/*     */     throws IOException
/*     */   {
/* 342 */     if (this.pos >= this.limit) {
/* 343 */       throw new EOFException();
/*     */     }
/* 345 */     int rc = this.in.read();
/* 346 */     if (rc < 0) {
/* 347 */       throw new EOFException();
/*     */     }
/* 349 */     this.pos += 1;
/* 350 */     return (byte)(rc & 0xFF);
/*     */   }
/*     */ 
/*     */   public Buffer readRawBytes(int size)
/*     */     throws IOException
/*     */   {
/* 360 */     if (size == 0) {
/* 361 */       return new Buffer(new byte[0]);
/*     */     }
/* 363 */     if (this.pos + size > this.limit) {
/* 364 */       throw new EOFException();
/*     */     }
/*     */ 
/* 369 */     if (this.bis != null) {
/* 370 */       Buffer rc = this.bis.readBuffer(size);
/* 371 */       if ((rc == null) || (rc.getLength() < size)) {
/* 372 */         throw new EOFException();
/*     */       }
/* 374 */       this.pos += rc.getLength();
/* 375 */       return rc;
/*     */     }
/*     */ 
/* 379 */     byte[] rc = new byte[size];
/*     */ 
/* 381 */     int pos = 0;
/* 382 */     while (pos < size) {
/* 383 */       int c = this.in.read(rc, pos, size - pos);
/* 384 */       if (c < 0) {
/* 385 */         throw new EOFException();
/*     */       }
/* 387 */       this.pos += c;
/* 388 */       pos += c;
/*     */     }
/*     */ 
/* 391 */     return new Buffer(rc);
/*     */   }
/*     */ 
/*     */   public void skipRawBytes(int size)
/*     */     throws IOException
/*     */   {
/* 401 */     int pos = 0;
/* 402 */     while (pos < size) {
/* 403 */       int n = (int)this.in.skip(size - pos);
/* 404 */       pos += n;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int pushLimit(int limit) {
/* 409 */     int rc = this.limit;
/* 410 */     this.limit = (this.pos + limit);
/* 411 */     return rc;
/*     */   }
/*     */ 
/*     */   public void popLimit(int limit) {
/* 415 */     this.limit = limit;
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.CodedInputStream
 * JD-Core Version:    0.6.2
 */