/*     */ package org.apache.activemq.protobuf;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public abstract class BaseMessage<T>
/*     */   implements Message<T>
/*     */ {
/*  33 */   protected int memoizedSerializedSize = -1;
/*     */ 
/*     */   public abstract T clone() throws CloneNotSupportedException;
/*     */ 
/*     */   public void clear() {
/*  38 */     this.memoizedSerializedSize = -1;
/*     */   }
/*     */ 
/*     */   public boolean isInitialized() {
/*  42 */     return missingFields().isEmpty();
/*     */   }
/*     */ 
/*     */   public T assertInitialized() throws UninitializedMessageException
/*     */   {
/*  47 */     ArrayList missingFields = missingFields();
/*  48 */     if (!missingFields.isEmpty()) {
/*  49 */       throw new UninitializedMessageException(missingFields);
/*     */     }
/*  51 */     return getThis();
/*     */   }
/*     */ 
/*     */   protected T checktInitialized() throws InvalidProtocolBufferException
/*     */   {
/*  56 */     ArrayList missingFields = missingFields();
/*  57 */     if (!missingFields.isEmpty()) {
/*  58 */       throw new UninitializedMessageException(missingFields).asInvalidProtocolBufferException();
/*     */     }
/*  60 */     return getThis();
/*     */   }
/*     */ 
/*     */   public ArrayList<String> missingFields() {
/*  64 */     load();
/*  65 */     return new ArrayList();
/*     */   }
/*     */ 
/*     */   protected void loadAndClear() {
/*  69 */     this.memoizedSerializedSize = -1;
/*     */   }
/*     */ 
/*     */   protected void load()
/*     */   {
/*     */   }
/*     */ 
/*     */   public T mergeFrom(T other) {
/*  77 */     return getThis();
/*     */   }
/*     */ 
/*     */   public void writeUnframed(CodedOutputStream output)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void writeFramed(CodedOutputStream output)
/*     */     throws IOException
/*     */   {
/* 106 */     output.writeRawVarint32(serializedSizeUnframed());
/* 107 */     writeUnframed(output);
/*     */   }
/*     */ 
/*     */   public Buffer toUnframedBuffer() {
/*     */     try {
/* 112 */       int size = serializedSizeUnframed();
/* 113 */       BufferOutputStream baos = new BufferOutputStream(size);
/* 114 */       CodedOutputStream output = new CodedOutputStream(baos);
/* 115 */       writeUnframed(output);
/* 116 */       Buffer rc = baos.toBuffer();
/* 117 */       if (rc.length != size) {
/* 118 */         throw new IllegalStateException("Did not write as much data as expected.");
/*     */       }
/* 120 */       return rc;
/*     */     } catch (IOException e) {
/* 122 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Buffer toFramedBuffer() {
/*     */     try {
/* 128 */       int size = serializedSizeFramed();
/* 129 */       BufferOutputStream baos = new BufferOutputStream(size);
/* 130 */       CodedOutputStream output = new CodedOutputStream(baos);
/* 131 */       writeFramed(output);
/* 132 */       Buffer rc = baos.toBuffer();
/* 133 */       if (rc.length != size) {
/* 134 */         throw new IllegalStateException("Did not write as much data as expected.");
/*     */       }
/* 136 */       return rc;
/*     */     } catch (IOException e) {
/* 138 */       throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public byte[] toUnframedByteArray() {
/* 143 */     return toUnframedBuffer().toByteArray();
/*     */   }
/*     */ 
/*     */   public byte[] toFramedByteArray() {
/* 147 */     return toFramedBuffer().toByteArray();
/*     */   }
/*     */ 
/*     */   public void writeFramed(OutputStream output) throws IOException {
/* 151 */     CodedOutputStream codedOutput = new CodedOutputStream(output);
/* 152 */     writeFramed(codedOutput);
/* 153 */     codedOutput.flush();
/*     */   }
/*     */ 
/*     */   public void writeUnframed(OutputStream output) throws IOException {
/* 157 */     CodedOutputStream codedOutput = new CodedOutputStream(output);
/* 158 */     writeUnframed(codedOutput);
/* 159 */     codedOutput.flush();
/*     */   }
/*     */ 
/*     */   public int serializedSizeFramed() {
/* 163 */     int t = serializedSizeUnframed();
/* 164 */     return CodedOutputStream.computeRawVarint32Size(t) + t;
/*     */   }
/*     */ 
/*     */   public T mergeFramed(CodedInputStream input)
/*     */     throws IOException
/*     */   {
/* 173 */     int length = input.readRawVarint32();
/* 174 */     int oldLimit = input.pushLimit(length);
/* 175 */     Object rc = mergeUnframed(input);
/* 176 */     input.checkLastTagWas(0);
/* 177 */     input.popLimit(oldLimit);
/* 178 */     return rc;
/*     */   }
/*     */ 
/*     */   public T mergeUnframed(Buffer data) throws InvalidProtocolBufferException {
/*     */     try {
/* 183 */       CodedInputStream input = new CodedInputStream(data);
/* 184 */       mergeUnframed(input);
/* 185 */       input.checkLastTagWas(0);
/* 186 */       return getThis();
/*     */     } catch (InvalidProtocolBufferException e) {
/* 188 */       throw e;
/*     */     } catch (IOException e) {
/* 190 */       throw new RuntimeException("An IOException was thrown (should never happen in this method).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private T getThis()
/*     */   {
/* 196 */     return this;
/*     */   }
/*     */ 
/*     */   public T mergeFramed(Buffer data) throws InvalidProtocolBufferException {
/*     */     try {
/* 201 */       CodedInputStream input = new CodedInputStream(data);
/* 202 */       mergeFramed(input);
/* 203 */       input.checkLastTagWas(0);
/* 204 */       return getThis();
/*     */     } catch (InvalidProtocolBufferException e) {
/* 206 */       throw e;
/*     */     } catch (IOException e) {
/* 208 */       throw new RuntimeException("An IOException was thrown (should never happen in this method).", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public T mergeUnframed(byte[] data) throws InvalidProtocolBufferException {
/* 213 */     return mergeUnframed(new Buffer(data));
/*     */   }
/*     */ 
/*     */   public T mergeFramed(byte[] data) throws InvalidProtocolBufferException {
/* 217 */     return mergeFramed(new Buffer(data));
/*     */   }
/*     */ 
/*     */   public T mergeUnframed(InputStream input) throws IOException {
/* 221 */     CodedInputStream codedInput = new CodedInputStream(input);
/* 222 */     mergeUnframed(codedInput);
/* 223 */     return getThis();
/*     */   }
/*     */ 
/*     */   public T mergeFramed(InputStream input) throws IOException {
/* 227 */     int length = readRawVarint32(input);
/* 228 */     byte[] data = new byte[length];
/* 229 */     int pos = 0;
/* 230 */     while (pos < length) {
/* 231 */       int r = input.read(data, pos, length - pos);
/* 232 */       if (r < 0) {
/* 233 */         throw new InvalidProtocolBufferException("Input stream ended before a full message frame could be read.");
/*     */       }
/* 235 */       pos += r;
/*     */     }
/* 237 */     return mergeUnframed(data);
/*     */   }
/*     */ 
/*     */   protected static <T> void addAll(Iterable<T> values, Collection<? super T> list)
/*     */   {
/*     */     Iterator i$;
/* 244 */     if ((values instanceof Collection))
/*     */     {
/* 246 */       Collection collection = (Collection)values;
/* 247 */       list.addAll(collection);
/*     */     } else {
/* 249 */       for (i$ = values.iterator(); i$.hasNext(); ) { Object value = i$.next();
/* 250 */         list.add(value); }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void writeGroup(CodedOutputStream output, int tag, BaseMessage message) throws IOException
/*     */   {
/* 256 */     output.writeTag(tag, 3);
/* 257 */     message.writeUnframed(output);
/* 258 */     output.writeTag(tag, 4);
/*     */   }
/*     */ 
/*     */   protected static <T extends BaseMessage> T readGroup(CodedInputStream input, int tag, T group) throws IOException {
/* 262 */     group.mergeUnframed(input);
/* 263 */     input.checkLastTagWas(WireFormat.makeTag(tag, 4));
/* 264 */     return group;
/*     */   }
/*     */ 
/*     */   protected static int computeGroupSize(int tag, BaseMessage message) {
/* 268 */     return CodedOutputStream.computeTagSize(tag) * 2 + message.serializedSizeUnframed();
/*     */   }
/*     */ 
/*     */   protected static void writeMessage(CodedOutputStream output, int tag, BaseMessage message) throws IOException {
/* 272 */     output.writeTag(tag, 2);
/* 273 */     message.writeFramed(output);
/*     */   }
/*     */ 
/*     */   protected static int computeMessageSize(int tag, BaseMessage message) {
/* 277 */     return CodedOutputStream.computeTagSize(tag) + message.serializedSizeFramed();
/*     */   }
/*     */ 
/*     */   protected List<String> prefix(List<String> missingFields, String prefix) {
/* 281 */     ArrayList rc = new ArrayList(missingFields.size());
/* 282 */     for (String v : missingFields) {
/* 283 */       rc.add(prefix + v);
/*     */     }
/* 285 */     return rc;
/*     */   }
/*     */ 
/*     */   public static int readRawVarint32(InputStream is)
/*     */     throws IOException
/*     */   {
/* 293 */     byte tmp = readRawByte(is);
/* 294 */     if (tmp >= 0) {
/* 295 */       return tmp;
/*     */     }
/* 297 */     int result = tmp & 0x7F;
/* 298 */     if ((tmp = readRawByte(is)) >= 0) {
/* 299 */       result |= tmp << 7;
/*     */     } else {
/* 301 */       result |= (tmp & 0x7F) << 7;
/* 302 */       if ((tmp = readRawByte(is)) >= 0) {
/* 303 */         result |= tmp << 14;
/*     */       } else {
/* 305 */         result |= (tmp & 0x7F) << 14;
/* 306 */         if ((tmp = readRawByte(is)) >= 0) {
/* 307 */           result |= tmp << 21;
/*     */         } else {
/* 309 */           result |= (tmp & 0x7F) << 21;
/* 310 */           result |= (tmp = readRawByte(is)) << 28;
/* 311 */           if (tmp < 0)
/*     */           {
/* 313 */             for (int i = 0; i < 5; i++) {
/* 314 */               if (readRawByte(is) >= 0)
/* 315 */                 return result;
/*     */             }
/* 317 */             throw new InvalidProtocolBufferException("CodedInputStream encountered a malformed varint.");
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 322 */     return result;
/*     */   }
/*     */ 
/*     */   protected static byte readRawByte(InputStream is) throws IOException {
/* 326 */     int rc = is.read();
/* 327 */     if (rc == -1) {
/* 328 */       throw new InvalidProtocolBufferException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
/*     */     }
/*     */ 
/* 331 */     return (byte)rc;
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.BaseMessage
 * JD-Core Version:    0.6.2
 */