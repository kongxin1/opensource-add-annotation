 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 
 public final class KahaXATransactionId extends KahaXATransactionIdBase<KahaXATransactionId>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasFormatId()) {
       missingFields.add("format_id");
     }
     if (!hasBranchQualifier()) {
       missingFields.add("branch_qualifier");
     }
     if (!hasGlobalTransactionId()) {
       missingFields.add("global_transaction_id");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearFormatId();
     clearBranchQualifier();
     clearGlobalTransactionId();
   }
 
   public KahaXATransactionId clone() {
     return new KahaXATransactionId().mergeFrom(this);
   }
 
   public KahaXATransactionId mergeFrom(KahaXATransactionId other) {
     if (other.hasFormatId()) {
       setFormatId(other.getFormatId());
     }
     if (other.hasBranchQualifier()) {
       setBranchQualifier(other.getBranchQualifier());
     }
     if (other.hasGlobalTransactionId()) {
       setGlobalTransactionId(other.getGlobalTransactionId());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasFormatId()) {
       size += CodedOutputStream.computeInt32Size(1, getFormatId());
     }
     if (hasBranchQualifier()) {
       size += CodedOutputStream.computeBytesSize(2, getBranchQualifier());
     }
     if (hasGlobalTransactionId()) {
       size += CodedOutputStream.computeBytesSize(3, getGlobalTransactionId());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaXATransactionId mergeUnframed(CodedInputStream input) throws IOException {
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
         setFormatId(input.readInt32());
         break;
       case 18:
         setBranchQualifier(input.readBytes());
         break;
       case 26:
         setGlobalTransactionId(input.readBytes());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasFormatId()) {
       output.writeInt32(1, getFormatId());
     }
     if (hasBranchQualifier()) {
       output.writeBytes(2, getBranchQualifier());
     }
     if (hasGlobalTransactionId())
       output.writeBytes(3, getGlobalTransactionId());
   }
 
   public static KahaXATransactionId parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaXATransactionId)new KahaXATransactionId().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaXATransactionId parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaXATransactionId parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaXATransactionId)((KahaXATransactionId)new KahaXATransactionId().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasFormatId()) {
       sb.append(prefix + "format_id: ");
       sb.append(getFormatId());
       sb.append("\n");
     }
     if (hasBranchQualifier()) {
       sb.append(prefix + "branch_qualifier: ");
       sb.append(getBranchQualifier());
       sb.append("\n");
     }
     if (hasGlobalTransactionId()) {
       sb.append(prefix + "global_transaction_id: ");
       sb.append(getGlobalTransactionId());
       sb.append("\n");
     }
     return sb;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaXATransactionId.class)) {
       return false;
     }
     return equals((KahaXATransactionId)obj);
   }
 
   public boolean equals(KahaXATransactionId obj) {
     if ((hasFormatId() ^ obj.hasFormatId()))
       return false;
     if ((hasFormatId()) && (getFormatId() != obj.getFormatId()))
       return false;
     if ((hasBranchQualifier() ^ obj.hasBranchQualifier()))
       return false;
     if ((hasBranchQualifier()) && (!getBranchQualifier().equals(obj.getBranchQualifier())))
       return false;
     if ((hasGlobalTransactionId() ^ obj.hasGlobalTransactionId()))
       return false;
     if ((hasGlobalTransactionId()) && (!getGlobalTransactionId().equals(obj.getGlobalTransactionId())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -2138302623;
     if (hasFormatId()) {
       rc ^= 0x201C4392 ^ getFormatId();
     }
     if (hasBranchQualifier()) {
       rc ^= 0x6CA6D908 ^ getBranchQualifier().hashCode();
     }
     if (hasGlobalTransactionId()) {
       rc ^= 0xEB55D196 ^ getGlobalTransactionId().hashCode();
     }
     return rc;
   }
 }





