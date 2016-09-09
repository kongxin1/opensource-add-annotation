 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 import org.apache.activemq.protobuf.UninitializedMessageException;
 
 public final class KahaTransactionInfo extends KahaTransactionInfoBase<KahaTransactionInfo>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (hasLocalTransactionId()) {
       try {
         getLocalTransactionId().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "local_transaction_id."));
       }
     }
     if (hasXaTransactionId()) {
       try {
         getXaTransactionId().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "xa_transaction_id."));
       }
     }
     if (hasPreviousEntry()) {
       try {
         getPreviousEntry().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "previous_entry."));
       }
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearLocalTransactionId();
     clearXaTransactionId();
     clearPreviousEntry();
   }
 
   public KahaTransactionInfo clone() {
     return new KahaTransactionInfo().mergeFrom(this);
   }
 
   public KahaTransactionInfo mergeFrom(KahaTransactionInfo other) {
     if (other.hasLocalTransactionId()) {
       if (hasLocalTransactionId())
         getLocalTransactionId().mergeFrom(other.getLocalTransactionId());
       else {
         setLocalTransactionId(other.getLocalTransactionId().clone());
       }
     }
     if (other.hasXaTransactionId()) {
       if (hasXaTransactionId())
         getXaTransactionId().mergeFrom(other.getXaTransactionId());
       else {
         setXaTransactionId(other.getXaTransactionId().clone());
       }
     }
     if (other.hasPreviousEntry()) {
       if (hasPreviousEntry())
         getPreviousEntry().mergeFrom(other.getPreviousEntry());
       else {
         setPreviousEntry(other.getPreviousEntry().clone());
       }
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasLocalTransactionId()) {
       size += computeMessageSize(1, getLocalTransactionId());
     }
     if (hasXaTransactionId()) {
       size += computeMessageSize(2, getXaTransactionId());
     }
     if (hasPreviousEntry()) {
       size += computeMessageSize(3, getPreviousEntry());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaTransactionInfo mergeUnframed(CodedInputStream input) throws IOException {
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
         if (hasLocalTransactionId())
           getLocalTransactionId().mergeFramed(input);
         else {
           setLocalTransactionId((KahaLocalTransactionId)new KahaLocalTransactionId().mergeFramed(input));
         }
         break;
       case 18:
         if (hasXaTransactionId())
           getXaTransactionId().mergeFramed(input);
         else {
           setXaTransactionId((KahaXATransactionId)new KahaXATransactionId().mergeFramed(input));
         }
         break;
       case 26:
         if (hasPreviousEntry())
           getPreviousEntry().mergeFramed(input);
         else
           setPreviousEntry((KahaLocation)new KahaLocation().mergeFramed(input));
         break;
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasLocalTransactionId()) {
       writeMessage(output, 1, getLocalTransactionId());
     }
     if (hasXaTransactionId()) {
       writeMessage(output, 2, getXaTransactionId());
     }
     if (hasPreviousEntry())
       writeMessage(output, 3, getPreviousEntry());
   }
 
   public static KahaTransactionInfo parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaTransactionInfo)new KahaTransactionInfo().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaTransactionInfo parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTransactionInfo parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTransactionInfo)((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasLocalTransactionId()) {
       sb.append(prefix + "local_transaction_id {\n");
       getLocalTransactionId().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     if (hasXaTransactionId()) {
       sb.append(prefix + "xa_transaction_id {\n");
       getXaTransactionId().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     if (hasPreviousEntry()) {
       sb.append(prefix + "previous_entry {\n");
       getPreviousEntry().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     return sb;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaTransactionInfo.class)) {
       return false;
     }
     return equals((KahaTransactionInfo)obj);
   }
 
   public boolean equals(KahaTransactionInfo obj) {
     if ((hasLocalTransactionId() ^ obj.hasLocalTransactionId()))
       return false;
     if ((hasLocalTransactionId()) && (!getLocalTransactionId().equals(obj.getLocalTransactionId())))
       return false;
     if ((hasXaTransactionId() ^ obj.hasXaTransactionId()))
       return false;
     if ((hasXaTransactionId()) && (!getXaTransactionId().equals(obj.getXaTransactionId())))
       return false;
     if ((hasPreviousEntry() ^ obj.hasPreviousEntry()))
       return false;
     if ((hasPreviousEntry()) && (!getPreviousEntry().equals(obj.getPreviousEntry())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 156129213;
     if (hasLocalTransactionId()) {
       rc ^= 0x306A4F0E ^ getLocalTransactionId().hashCode();
     }
     if (hasXaTransactionId()) {
       rc ^= 0xC2CCB810 ^ getXaTransactionId().hashCode();
     }
     if (hasPreviousEntry()) {
       rc ^= 0x1E4CCF9B ^ getPreviousEntry().hashCode();
     }
     return rc;
   }
 }





