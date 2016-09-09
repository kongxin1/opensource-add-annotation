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
 
 public final class KahaRemoveScheduledJobCommand extends KahaRemoveScheduledJobCommandBase<KahaRemoveScheduledJobCommand>
   implements JournalCommand<KahaRemoveScheduledJobCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasScheduler()) {
       missingFields.add("scheduler");
     }
     if (!hasJobId()) {
       missingFields.add("job_id");
     }
     if (!hasNextExecutionTime()) {
       missingFields.add("next_execution_time");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearScheduler();
     clearJobId();
     clearNextExecutionTime();
   }
 
   public KahaRemoveScheduledJobCommand clone() {
     return new KahaRemoveScheduledJobCommand().mergeFrom(this);
   }
 
   public KahaRemoveScheduledJobCommand mergeFrom(KahaRemoveScheduledJobCommand other) {
     if (other.hasScheduler()) {
       setScheduler(other.getScheduler());
     }
     if (other.hasJobId()) {
       setJobId(other.getJobId());
     }
     if (other.hasNextExecutionTime()) {
       setNextExecutionTime(other.getNextExecutionTime());
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
     if (hasJobId()) {
       size += CodedOutputStream.computeStringSize(2, getJobId());
     }
     if (hasNextExecutionTime()) {
       size += CodedOutputStream.computeInt64Size(3, getNextExecutionTime());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaRemoveScheduledJobCommand mergeUnframed(CodedInputStream input) throws IOException {
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
       case 18:
         setJobId(input.readString());
         break;
       case 24:
         setNextExecutionTime(input.readInt64());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasScheduler()) {
       output.writeString(1, getScheduler());
     }
     if (hasJobId()) {
       output.writeString(2, getJobId());
     }
     if (hasNextExecutionTime())
       output.writeInt64(3, getNextExecutionTime());
   }
 
   public static KahaRemoveScheduledJobCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRemoveScheduledJobCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRemoveScheduledJobCommand)((KahaRemoveScheduledJobCommand)new KahaRemoveScheduledJobCommand().mergeFramed(data)).checktInitialized();
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
     if (hasJobId()) {
       sb.append(prefix + "job_id: ");
       sb.append(getJobId());
       sb.append("\n");
     }
     if (hasNextExecutionTime()) {
       sb.append(prefix + "next_execution_time: ");
       sb.append(getNextExecutionTime());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_REMOVE_SCHEDULED_JOB_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaRemoveScheduledJobCommand.class)) {
       return false;
     }
     return equals((KahaRemoveScheduledJobCommand)obj);
   }
 
   public boolean equals(KahaRemoveScheduledJobCommand obj) {
     if ((hasScheduler() ^ obj.hasScheduler()))
       return false;
     if ((hasScheduler()) && (!getScheduler().equals(obj.getScheduler())))
       return false;
     if ((hasJobId() ^ obj.hasJobId()))
       return false;
     if ((hasJobId()) && (!getJobId().equals(obj.getJobId())))
       return false;
     if ((hasNextExecutionTime() ^ obj.hasNextExecutionTime()))
       return false;
     if ((hasNextExecutionTime()) && (getNextExecutionTime() != obj.getNextExecutionTime()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 425904136;
     if (hasScheduler()) {
       rc ^= 0x6DDDE09B ^ getScheduler().hashCode();
     }
     if (hasJobId()) {
       rc ^= 0x446B998 ^ getJobId().hashCode();
     }
     if (hasNextExecutionTime()) {
       rc ^= 0x703C0DB2 ^ new Long(getNextExecutionTime()).hashCode();
     }
     return rc;
   }
 }





