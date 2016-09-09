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
 
 public final class KahaDestroySchedulerCommand extends KahaDestroySchedulerCommandBase<KahaDestroySchedulerCommand>
   implements JournalCommand<KahaDestroySchedulerCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasScheduler()) {
       missingFields.add("scheduler");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearScheduler();
   }
 
   public KahaDestroySchedulerCommand clone() {
     return new KahaDestroySchedulerCommand().mergeFrom(this);
   }
 
   public KahaDestroySchedulerCommand mergeFrom(KahaDestroySchedulerCommand other) {
     if (other.hasScheduler()) {
       setScheduler(other.getScheduler());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasScheduler()) {
       size += CodedOutputStream.computeStringSize(1, getScheduler());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaDestroySchedulerCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setScheduler(input.readString());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasScheduler())
       output.writeString(1, getScheduler());
   }
 
   public static KahaDestroySchedulerCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaDestroySchedulerCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaDestroySchedulerCommand)((KahaDestroySchedulerCommand)new KahaDestroySchedulerCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasScheduler()) {
       sb.append(prefix + "scheduler: ");
       sb.append(getScheduler());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_DESTROY_SCHEDULER_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaDestroySchedulerCommand.class)) {
       return false;
     }
     return equals((KahaDestroySchedulerCommand)obj);
   }
 
   public boolean equals(KahaDestroySchedulerCommand obj) {
     if ((hasScheduler() ^ obj.hasScheduler()))
       return false;
     if ((hasScheduler()) && (!getScheduler().equals(obj.getScheduler())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 1034868795;
     if (hasScheduler()) {
       rc ^= 0x6DDDE09B ^ getScheduler().hashCode();
     }
     return rc;
   }
 }





