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
 
 public final class KahaUpdateMessageCommand extends KahaUpdateMessageCommandBase<KahaUpdateMessageCommand>
   implements JournalCommand<KahaUpdateMessageCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasMessage()) {
       missingFields.add("message");
     }
     if (hasMessage()) {
       try {
         getMessage().assertInitialized();
       } catch (UninitializedMessageException e) {
         missingFields.addAll(prefix(e.getMissingFields(), "message."));
       }
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearMessage();
   }
 
   public KahaUpdateMessageCommand clone() {
     return new KahaUpdateMessageCommand().mergeFrom(this);
   }
 
   public KahaUpdateMessageCommand mergeFrom(KahaUpdateMessageCommand other) {
     if (other.hasMessage()) {
       if (hasMessage())
         getMessage().mergeFrom(other.getMessage());
       else {
         setMessage(other.getMessage().clone());
       }
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasMessage()) {
       size += computeMessageSize(1, getMessage());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaUpdateMessageCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         if (hasMessage())
           getMessage().mergeFramed(input);
         else
           setMessage((KahaAddMessageCommand)new KahaAddMessageCommand().mergeFramed(input));
         break;
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasMessage())
       writeMessage(output, 1, getMessage());
   }
 
   public static KahaUpdateMessageCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaUpdateMessageCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaUpdateMessageCommand)((KahaUpdateMessageCommand)new KahaUpdateMessageCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasMessage()) {
       sb.append(prefix + "message {\n");
       getMessage().toString(sb, prefix + "  ");
       sb.append(prefix + "}\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_UPDATE_MESSAGE_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaUpdateMessageCommand.class)) {
       return false;
     }
     return equals((KahaUpdateMessageCommand)obj);
   }
 
   public boolean equals(KahaUpdateMessageCommand obj) {
     if ((hasMessage() ^ obj.hasMessage()))
       return false;
     if ((hasMessage()) && (!getMessage().equals(obj.getMessage())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 1943345660;
     if (hasMessage()) {
       rc ^= 0x9C2397E7 ^ getMessage().hashCode();
     }
     return rc;
   }
 }





