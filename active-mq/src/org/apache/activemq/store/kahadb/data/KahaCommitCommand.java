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
 
 public final class KahaCommitCommand extends KahaCommitCommandBase<KahaCommitCommand>
   implements JournalCommand<KahaCommitCommand>
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
 
   public KahaCommitCommand clone() {
     return new KahaCommitCommand().mergeFrom(this);
   }
 
   public KahaCommitCommand mergeFrom(KahaCommitCommand other) {
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
 
   public KahaCommitCommand mergeUnframed(CodedInputStream input) throws IOException {
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
 
   public static KahaCommitCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaCommitCommand)new KahaCommitCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaCommitCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaCommitCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaCommitCommand)((KahaCommitCommand)new KahaCommitCommand().mergeFramed(data)).checktInitialized();
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
     return KahaEntryType.KAHA_COMMIT_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaCommitCommand.class)) {
       return false;
     }
     return equals((KahaCommitCommand)obj);
   }
 
   public boolean equals(KahaCommitCommand obj) {
     if ((hasTransactionInfo() ^ obj.hasTransactionInfo()))
       return false;
     if ((hasTransactionInfo()) && (!getTransactionInfo().equals(obj.getTransactionInfo())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -651907739;
     if (hasTransactionInfo()) {
       rc ^= 0xFD5C48C ^ getTransactionInfo().hashCode();
     }
     return rc;
   }
 }





