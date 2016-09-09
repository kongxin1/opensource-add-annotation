 package org.apache.activemq.store.kahadb.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.apache.activemq.protobuf.Buffer;
 import org.apache.activemq.protobuf.CodedInputStream;
 import org.apache.activemq.protobuf.CodedOutputStream;
 import org.apache.activemq.protobuf.InvalidProtocolBufferException;
 import org.apache.activemq.store.kahadb.JournalCommand;
 import org.apache.activemq.store.kahadb.Visitor;
 
 public final class KahaTraceCommand extends KahaTraceCommandBase<KahaTraceCommand>
   implements JournalCommand<KahaTraceCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasMessage()) {
       missingFields.add("message");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearMessage();
   }
 
   public KahaTraceCommand clone() {
     return new KahaTraceCommand().mergeFrom(this);
   }
 
   public KahaTraceCommand mergeFrom(KahaTraceCommand other) {
     if (other.hasMessage()) {
       setMessage(other.getMessage());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasMessage()) {
       size += CodedOutputStream.computeStringSize(1, getMessage());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaTraceCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setMessage(input.readString());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasMessage())
       output.writeString(1, getMessage());
   }
 
   public static KahaTraceCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaTraceCommand)new KahaTraceCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaTraceCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaTraceCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaTraceCommand)((KahaTraceCommand)new KahaTraceCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasMessage()) {
       sb.append(prefix + "message: ");
       sb.append(getMessage());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_TRACE_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaTraceCommand.class)) {
       return false;
     }
     return equals((KahaTraceCommand)obj);
   }
 
   public boolean equals(KahaTraceCommand obj) {
     if ((hasMessage() ^ obj.hasMessage()))
       return false;
     if ((hasMessage()) && (!getMessage().equals(obj.getMessage())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -1782549291;
     if (hasMessage()) {
       rc ^= 0x9C2397E7 ^ getMessage().hashCode();
     }
     return rc;
   }
 }





