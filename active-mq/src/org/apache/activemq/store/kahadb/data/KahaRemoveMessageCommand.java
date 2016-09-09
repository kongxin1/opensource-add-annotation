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
 
 public final class KahaRemoveMessageCommand extends KahaRemoveMessageCommandBase<KahaRemoveMessageCommand>
   implements JournalCommand<KahaRemoveMessageCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasDestination()) {
       missingFields.add("destination");
     }
     if (!hasMessageId()) {
       missingFields.add("messageId");
     }
     if (hasTransactionInfo()) {
       try {
         getTransactionInfo().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "transaction_info."));
       }
     }
     if (hasDestination()) {
       try {
         getDestination().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "destination."));
       }
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearTransactionInfo();
     clearDestination();
     clearMessageId();
     clearAck();
     clearSubscriptionKey();
   }
 
   public KahaRemoveMessageCommand clone() {
     return new KahaRemoveMessageCommand().mergeFrom(this);
   }
 
   public KahaRemoveMessageCommand mergeFrom(KahaRemoveMessageCommand other) {
     if (other.hasTransactionInfo()) {
       if (hasTransactionInfo())
         getTransactionInfo().mergeFrom(other.getTransactionInfo());
       else {
         setTransactionInfo(other.getTransactionInfo().clone());
       }
     }
     if (other.hasDestination()) {
       if (hasDestination())
         getDestination().mergeFrom(other.getDestination());
       else {
         setDestination(other.getDestination().clone());
       }
     }
     if (other.hasMessageId()) {
       setMessageId(other.getMessageId());
     }
     if (other.hasAck()) {
       setAck(other.getAck());
     }
     if (other.hasSubscriptionKey()) {
       setSubscriptionKey(other.getSubscriptionKey());
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
     if (hasDestination()) {
       size += computeMessageSize(2, getDestination());
     }
     if (hasMessageId()) {
       size += CodedOutputStream.computeStringSize(3, getMessageId());
     }
     if (hasAck()) {
       size += CodedOutputStream.computeBytesSize(4, getAck());
     }
     if (hasSubscriptionKey()) {
       size += CodedOutputStream.computeStringSize(5, getSubscriptionKey());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaRemoveMessageCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         else {
           setTransactionInfo((KahaTransactionInfo)new KahaTransactionInfo().mergeFramed(input));
         }
         break;
       case 18:
         if (hasDestination())
           getDestination().mergeFramed(input);
         else {
           setDestination((KahaDestination)new KahaDestination().mergeFramed(input));
         }
         break;
       case 26:
         setMessageId(input.readString());
         break;
       case 34:
         setAck(input.readBytes());
         break;
       case 42:
         setSubscriptionKey(input.readString());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasTransactionInfo()) {
       writeMessage(output, 1, getTransactionInfo());
     }
     if (hasDestination()) {
       writeMessage(output, 2, getDestination());
     }
     if (hasMessageId()) {
       output.writeString(3, getMessageId());
     }
     if (hasAck()) {
       output.writeBytes(4, getAck());
     }
     if (hasSubscriptionKey())
       output.writeString(5, getSubscriptionKey());
   }
 
   public static KahaRemoveMessageCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveMessageCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveMessageCommand)((KahaRemoveMessageCommand)new KahaRemoveMessageCommand().mergeFramed(data)).checktInitialized();
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
     if (hasDestination()) {
       sb.append(prefix + "destination {\n");
       getDestination().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     if (hasMessageId()) {
       sb.append(prefix + "messageId: ");
       sb.append(getMessageId());
       sb.append("\n");
     }
     if (hasAck()) {
       sb.append(prefix + "ack: ");
       sb.append(getAck());
       sb.append("\n");
     }
     if (hasSubscriptionKey()) {
       sb.append(prefix + "subscriptionKey: ");
       sb.append(getSubscriptionKey());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_REMOVE_MESSAGE_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaRemoveMessageCommand.class)) {
       return false;
     }
     return equals((KahaRemoveMessageCommand)obj);
   }
 
   public boolean equals(KahaRemoveMessageCommand obj) {
     if ((hasTransactionInfo() ^ obj.hasTransactionInfo()))
       return false;
     if ((hasTransactionInfo()) && (!getTransactionInfo().equals(obj.getTransactionInfo())))
       return false;
     if ((hasDestination() ^ obj.hasDestination()))
       return false;
     if ((hasDestination()) && (!getDestination().equals(obj.getDestination())))
       return false;
     if ((hasMessageId() ^ obj.hasMessageId()))
       return false;
     if ((hasMessageId()) && (!getMessageId().equals(obj.getMessageId())))
       return false;
     if ((hasAck() ^ obj.hasAck()))
       return false;
     if ((hasAck()) && (!getAck().equals(obj.getAck())))
       return false;
     if ((hasSubscriptionKey() ^ obj.hasSubscriptionKey()))
       return false;
     if ((hasSubscriptionKey()) && (!getSubscriptionKey().equals(obj.getSubscriptionKey())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -64211337;
     if (hasTransactionInfo()) {
       rc ^= 0xFD5C48C ^ getTransactionInfo().hashCode();
     }
     if (hasDestination()) {
       rc ^= 0xE2FEBEE ^ getDestination().hashCode();
     }
     if (hasMessageId()) {
       rc ^= 0x219D4362 ^ getMessageId().hashCode();
     }
     if (hasAck()) {
       rc ^= 0x10069 ^ getAck().hashCode();
     }
     if (hasSubscriptionKey()) {
       rc ^= 0x710013E2 ^ getSubscriptionKey().hashCode();
     }
     return rc;
   }
 }





