 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 
 public final class KahaLocalTransactionId extends KahaLocalTransactionIdBase<KahaLocalTransactionId>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasConnectionId()) {
       missingFields.add("connection_id");
     }
     if (!hasTransactionId()) {
       missingFields.add("transaction_id");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearConnectionId();
     clearTransactionId();
   }
 
   public KahaLocalTransactionId clone() {
     return new KahaLocalTransactionId().mergeFrom(this);
   }
 
   public KahaLocalTransactionId mergeFrom(KahaLocalTransactionId other) {
     if (other.hasConnectionId()) {
       setConnectionId(other.getConnectionId());
     }
     if (other.hasTransactionId()) {
       setTransactionId(other.getTransactionId());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasConnectionId()) {
       size += CodedOutputStream.computeStringSize(1, getConnectionId());
     }
     if (hasTransactionId()) {
       size += CodedOutputStream.computeInt64Size(1, getTransactionId());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaLocalTransactionId mergeUnframed(CodedInputStream input) throws IOException {
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
       case 10:
         setConnectionId(input.readString());
         break;
       case 8:
         setTransactionId(input.readInt64());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasConnectionId()) {
       output.writeString(1, getConnectionId());
     }
     if (hasTransactionId())
       output.writeInt64(1, getTransactionId());
   }
 
   public static KahaLocalTransactionId parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaLocalTransactionId)new KahaLocalTransactionId().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaLocalTransactionId parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaLocalTransactionId)((KahaLocalTransactionId)new KahaLocalTransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasConnectionId()) {
       sb.append(prefix + "connection_id: ");
       sb.append(getConnectionId());
       sb.append("\n");
     }
     if (hasTransactionId()) {
       sb.append(prefix + "transaction_id: ");
       sb.append(getTransactionId());
       sb.append("\n");
     }
     return sb;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaLocalTransactionId.class)) {
       return false;
     }
     return equals((KahaLocalTransactionId)obj);
   }
 
   public boolean equals(KahaLocalTransactionId obj) {
     if ((hasConnectionId() ^ obj.hasConnectionId()))
       return false;
     if ((hasConnectionId()) && (!getConnectionId().equals(obj.getConnectionId())))
       return false;
     if ((hasTransactionId() ^ obj.hasTransactionId()))
       return false;
     if ((hasTransactionId()) && (getTransactionId() != obj.getTransactionId()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 1725637181;
     if (hasConnectionId()) {
       rc ^= 0x7C6B9CB9 ^ getConnectionId().hashCode();
     }
     if (hasTransactionId()) {
       rc ^= 0x4A56CC79 ^ new Long(getTransactionId()).hashCode();
     }
     return rc;
   }
 }





