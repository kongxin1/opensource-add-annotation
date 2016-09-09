/*     */ package org.apache.activemq.protobuf;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ public final class MessageBufferSupport
/*     */ {
/*     */   public static final String FORZEN_ERROR_MESSAGE = "Modification not allowed after object has been fozen.  Try modifying a copy of this object.";
/*     */ 
/*     */   public static Buffer toUnframedBuffer(MessageBuffer message)
/*     */   {
/*     */     try
/*     */     {
/*  32 */       int size = message.serializedSizeUnframed();
/*  33 */       BufferOutputStream baos = new BufferOutputStream(size);
/*  34 */       CodedOutputStream output = new CodedOutputStream(baos);
/*  35 */       message.writeUnframed(output);
/*  36 */       Buffer rc = baos.toBuffer();
/*  37 */       assert (rc.length == size) : "Did not write as much data as expected.";
/*  38 */       return rc;
/*     */     } catch (IOException e) {
/*  40 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Buffer toFramedBuffer(MessageBuffer message) {
/*     */     try {
/*  46 */       int size = message.serializedSizeFramed();
/*  47 */       BufferOutputStream baos = new BufferOutputStream(size);
/*  48 */       CodedOutputStream output = new CodedOutputStream(baos);
/*  49 */       message.writeFramed(output);
/*  50 */       Buffer rc = baos.toBuffer();
/*  51 */       assert (rc.length == size) : "Did not write as much data as expected.";
/*  52 */       return rc;
/*     */     } catch (IOException e) {
/*  54 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void writeMessage(CodedOutputStream output, int tag, MessageBuffer message) throws IOException {
/*  59 */     output.writeTag(tag, 2);
/*  60 */     message.writeFramed(output);
/*     */   }
/*     */ 
/*     */   public static int computeMessageSize(int tag, MessageBuffer message) {
/*  64 */     return CodedOutputStream.computeTagSize(tag) + message.serializedSizeFramed();
/*     */   }
/*     */ 
/*     */   public static Buffer readFrame(InputStream input) throws IOException {
/*  68 */     int length = readRawVarint32(input);
/*  69 */     byte[] data = new byte[length];
/*  70 */     int pos = 0;
/*  71 */     while (pos < length) {
/*  72 */       int r = input.read(data, pos, length - pos);
/*  73 */       if (r < 0) {
/*  74 */         throw new InvalidProtocolBufferException("Input stream ended before a full message frame could be read.");
/*     */       }
/*  76 */       pos += r;
/*     */     }
/*  78 */     return new Buffer(data);
/*     */   }
/*     */ 
/*     */   public static int readRawVarint32(InputStream is)
/*     */     throws IOException
/*     */   {
/*  86 */     byte tmp = readRawByte(is);
/*  87 */     if (tmp >= 0) {
/*  88 */       return tmp;
/*     */     }
/*  90 */     int result = tmp & 0x7F;
/*  91 */     if ((tmp = readRawByte(is)) >= 0) {
/*  92 */       result |= tmp << 7;
/*     */     } else {
/*  94 */       result |= (tmp & 0x7F) << 7;
/*  95 */       if ((tmp = readRawByte(is)) >= 0) {
/*  96 */         result |= tmp << 14;
/*     */       } else {
/*  98 */         result |= (tmp & 0x7F) << 14;
/*  99 */         if ((tmp = readRawByte(is)) >= 0) {
/* 100 */           result |= tmp << 21;
/*     */         } else {
/* 102 */           result |= (tmp & 0x7F) << 21;
/* 103 */           result |= (tmp = readRawByte(is)) << 28;
/* 104 */           if (tmp < 0)
/*     */           {
/* 106 */             for (int i = 0; i < 5; i++) {
/* 107 */               if (readRawByte(is) >= 0)
/* 108 */                 return result;
/*     */             }
/* 110 */             throw new InvalidProtocolBufferException("CodedInputStream encountered a malformed varint.");
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 115 */     return result;
/*     */   }
/*     */ 
/*     */   public static byte readRawByte(InputStream is) throws IOException {
/* 119 */     int rc = is.read();
/* 120 */     if (rc == -1) {
/* 121 */       throw new InvalidProtocolBufferException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
/*     */     }
/*     */ 
/* 124 */     return (byte)rc;
/*     */   }
/*     */ 
/*     */   public static <T> void addAll(Iterable<T> values, Collection<? super T> list)
/*     */   {
/*     */     Iterator i$;
/* 128 */     if ((values instanceof Collection))
/*     */     {
/* 130 */       Collection collection = (Collection)values;
/* 131 */       list.addAll(collection);
/*     */     } else {
/* 133 */       for (i$ = values.iterator(); i$.hasNext(); ) { Object value = i$.next();
/* 134 */         list.add(value);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.MessageBufferSupport
 * JD-Core Version:    0.6.2
 */