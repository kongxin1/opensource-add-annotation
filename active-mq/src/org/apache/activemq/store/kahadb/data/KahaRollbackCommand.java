 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 import org.apache.activemq.protobuf.UninitializedMessageException;
 import org.apache.activemq.store.kahadb.JournalCommand;
 import org.apache.activemq.store.kahadb.Visitor;
 
 public final class KahaRollbackCommand extends KahaRollbackCommandBase<KahaRollbackCommand>
   implements JournalCommand<KahaRollbackCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasTransactionInfo()) {
       missingFields.add("transaction_info");
     }
     if (hasTransactionInfo()) {
       try {
         getTransactionInfo().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "transaction_info."));
       }
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearTransactionInfo();
   }
 
   public KahaRollbackCommand clone() {
     return new KahaRollbackCommand().mergeFrom(this);
   }
 
   public KahaRollbackCommand mergeFrom(KahaRollbackCommand other) {
     if (other.hasTransactionInfo()) {
       if (hasTransactionInfo())
         getTransactionInfo().mergeFrom(other.getTransactionInfo());
       else {
         setTransactionInfo(other.getTransactionInfo().clone());
       }
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasTransactionInfo()) {
       size += computeMessageSize(1, getTransactionInfo());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaRollbackCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         if (hasTransactionInfo())
           getTransactionInfo().mergeFramed(input);
         else
           setTransactionInfo((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(input));
         break;
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasTransactionInfo())
       writeMessage(output, 1, getTransactionInfo());
   }
 
   public static KahaRollbackCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaRollbackCommand)new KahaRollbackCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaRollbackCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRollbackCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRollbackCommand)((KahaRollbackCommand)new KahaRollbackCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasTransactionInfo()) {
       sb.append(prefix + "transaction_info {\n");
       getTransactionInfo().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_ROLLBACK_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaRollbackCommand.class)) {
       return false;
     }
     return equals((KahaRollbackCommand)obj);
   }
 
   public boolean equals(KahaRollbackCommand obj) {
     if ((hasTransactionInfo() ^ obj.hasTransactionInfo()))
       return false;
     if ((hasTransactionInfo()) && (!getTransactionInfo().equals(obj.getTransactionInfo())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 2079783640;
     if (hasTransactionInfo()) {
       rc ^= 0xFD5C48C ^ getTransactionInfo().hashCode();
     }
     return rc;
   }
 }





