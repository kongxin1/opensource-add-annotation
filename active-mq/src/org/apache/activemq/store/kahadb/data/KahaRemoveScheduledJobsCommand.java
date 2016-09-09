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
 
 public final class KahaRemoveScheduledJobsCommand extends KahaRemoveScheduledJobsCommandBase<KahaRemoveScheduledJobsCommand>
   implements JournalCommand<KahaRemoveScheduledJobsCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasScheduler()) {
       missingFields.add("scheduler");
     }
     if (!hasStartTime()) {
       missingFields.add("start_time");
     }
     if (!hasEndTime()) {
       missingFields.add("end_time");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearScheduler();
     clearStartTime();
     clearEndTime();
   }
 
   public KahaRemoveScheduledJobsCommand clone() {
     return new KahaRemoveScheduledJobsCommand().mergeFrom(this);
   }
 
   public KahaRemoveScheduledJobsCommand mergeFrom(KahaRemoveScheduledJobsCommand other) {
     if (other.hasScheduler()) {
       setScheduler(other.getScheduler());
     }
     if (other.hasStartTime()) {
       setStartTime(other.getStartTime());
     }
     if (other.hasEndTime()) {
       setEndTime(other.getEndTime());
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
     if (hasStartTime()) {
       size += CodedOutputStream.computeInt64Size(2, getStartTime());
     }
     if (hasEndTime()) {
       size += CodedOutputStream.computeInt64Size(3, getEndTime());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaRemoveScheduledJobsCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         break;
       case 16:
         setStartTime(input.readInt64());
         break;
       case 24:
         setEndTime(input.readInt64());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasScheduler()) {
       output.writeString(1, getScheduler());
     }
     if (hasStartTime()) {
       output.writeInt64(2, getStartTime());
     }
     if (hasEndTime())
       output.writeInt64(3, getEndTime());
   }
 
   public static KahaRemoveScheduledJobsCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobsCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobsCommand)((KahaRemoveScheduledJobsCommand)new KahaRemoveScheduledJobsCommand().mergeFramed(data)).checktInitialized();
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
     if (hasStartTime()) {
       sb.append(prefix + "start_time: ");
       sb.append(getStartTime());
       sb.append("\n");
     }
     if (hasEndTime()) {
       sb.append(prefix + "end_time: ");
       sb.append(getEndTime());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_REMOVE_SCHEDULED_JOBS_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaRemoveScheduledJobsCommand.class)) {
       return false;
     }
     return equals((KahaRemoveScheduledJobsCommand)obj);
   }
 
   public boolean equals(KahaRemoveScheduledJobsCommand obj) {
     if ((hasScheduler() ^ obj.hasScheduler()))
       return false;
     if ((hasScheduler()) && (!getScheduler().equals(obj.getScheduler())))
       return false;
     if ((hasStartTime() ^ obj.hasStartTime()))
       return false;
     if ((hasStartTime()) && (getStartTime() != obj.getStartTime()))
       return false;
     if ((hasEndTime() ^ obj.hasEndTime()))
       return false;
     if ((hasEndTime()) && (getEndTime() != obj.getEndTime()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 2030791195;
     if (hasScheduler()) {
       rc ^= 0x6DDDE09B ^ getScheduler().hashCode();
     }
     if (hasStartTime()) {
       rc ^= 0xF887AA2F ^ new Long(getStartTime()).hashCode();
     }
     if (hasEndTime()) {
       rc ^= 0x36C0228 ^ new Long(getEndTime()).hashCode();
     }
     return rc;
   }
 }





