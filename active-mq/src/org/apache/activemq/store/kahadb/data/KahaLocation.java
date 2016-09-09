 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 
 public final class KahaLocation extends KahaLocationBase<KahaLocation>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasLogId()) {
       missingFields.add("log_id");
     }
     if (!hasOffset()) {
       missingFields.add("offset");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearLogId();
     clearOffset();
   }
 
   public KahaLocation clone() {
     return new KahaLocation().mergeFrom(this);
   }
 
   public KahaLocation mergeFrom(KahaLocation other) {
     if (other.hasLogId()) {
       setLogId(other.getLogId());
     }
     if (other.hasOffset()) {
       setOffset(other.getOffset());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasLogId()) {
       size += CodedOutputStream.computeInt32Size(1, getLogId());
     }
     if (hasOffset()) {
       size += CodedOutputStream.computeInt32Size(2, getOffset());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaLocation mergeUnframed(CodedInputStream input) throws IOException {
     while (true) {
       int tag = input.readTag();
       if ((tag & 0x7) == 4) {
         return this;
       }
       switch (tag) {
       case 0:
         return this;
       default:
         break;
       case 8:
         setLogId(input.readInt32());
         break;
       case 16:
         setOffset(input.readInt32());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasLogId()) {
       output.writeInt32(1, getLogId());
     }
     if (hasOffset())
       output.writeInt32(2, getOffset());
   }
 
   public static KahaLocation parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaLocation)new KahaLocation().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaLocation parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocation parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocation parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocation parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocation parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocation parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocation parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocation)((KahaLocation)new KahaLocation().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasLogId()) {
       sb.append(prefix + "log_id: ");
       sb.append(getLogId());
       sb.append("\n");
     }
     if (hasOffset()) {
       sb.append(prefix + "offset: ");
       sb.append(getOffset());
       sb.append("\n");
     }
     return sb;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaLocation.class)) {
       return false;
     }
     return equals((KahaLocation)obj);
   }
 
   public boolean equals(KahaLocation obj) {
     if ((hasLogId() ^ obj.hasLogId()))
       return false;
     if ((hasLogId()) && (getLogId() != obj.getLogId()))
       return false;
     if ((hasOffset() ^ obj.hasOffset()))
       return false;
     if ((hasOffset()) && (getOffset() != obj.getOffset()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -1935591996;
     if (hasLogId()) {
       rc ^= 0x462FB5F ^ getLogId();
     }
     if (hasOffset()) {
       rc ^= 0x8C9C50B3 ^ getOffset();
     }
     return rc;
   }
 }





