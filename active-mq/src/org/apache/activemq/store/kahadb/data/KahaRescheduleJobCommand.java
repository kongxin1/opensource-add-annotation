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
 
 public final class KahaRescheduleJobCommand extends KahaRescheduleJobCommandBase<KahaRescheduleJobCommand>
   implements JournalCommand<KahaRescheduleJobCommand>
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
     if (!hasExecutionTime()) {
       missingFields.add("execution_time");
     }
     if (!hasNextExecutionTime()) {
       missingFields.add("next_execution_time");
     }
     if (!hasRescheduledCount()) {
       missingFields.add("rescheduled_count");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearScheduler();
     clearJobId();
     clearExecutionTime();
     clearNextExecutionTime();
     clearRescheduledCount();
   }
 
   public KahaRescheduleJobCommand clone() {
     return new KahaRescheduleJobCommand().mergeFrom(this);
   }
 
   public KahaRescheduleJobCommand mergeFrom(KahaRescheduleJobCommand other) {
     if (other.hasScheduler()) {
       setScheduler(other.getScheduler());
     }
     if (other.hasJobId()) {
       setJobId(other.getJobId());
     }
     if (other.hasExecutionTime()) {
       setExecutionTime(other.getExecutionTime());
     }
     if (other.hasNextExecutionTime()) {
       setNextExecutionTime(other.getNextExecutionTime());
     }
     if (other.hasRescheduledCount()) {
       setRescheduledCount(other.getRescheduledCount());
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
     if (hasExecutionTime()) {
       size += CodedOutputStream.computeInt64Size(3, getExecutionTime());
     }
     if (hasNextExecutionTime()) {
       size += CodedOutputStream.computeInt64Size(4, getNextExecutionTime());
     }
     if (hasRescheduledCount()) {
       size += CodedOutputStream.computeInt32Size(5, getRescheduledCount());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaRescheduleJobCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setExecutionTime(input.readInt64());
         break;
       case 32:
         setNextExecutionTime(input.readInt64());
         break;
       case 40:
         setRescheduledCount(input.readInt32());
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
     if (hasExecutionTime()) {
       output.writeInt64(3, getExecutionTime());
     }
     if (hasNextExecutionTime()) {
       output.writeInt64(4, getNextExecutionTime());
     }
     if (hasRescheduledCount())
       output.writeInt32(5, getRescheduledCount());
   }
 
   public static KahaRescheduleJobCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaRescheduleJobCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaRescheduleJobCommand)((KahaRescheduleJobCommand)new KahaRescheduleJobCommand().mergeFramed(data)).checktInitialized();
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
     if (hasExecutionTime()) {
       sb.append(prefix + "execution_time: ");
       sb.append(getExecutionTime());
       sb.append("\n");
     }
     if (hasNextExecutionTime()) {
       sb.append(prefix + "next_execution_time: ");
       sb.append(getNextExecutionTime());
       sb.append("\n");
     }
     if (hasRescheduledCount()) {
       sb.append(prefix + "rescheduled_count: ");
       sb.append(getRescheduledCount());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_RESCHEDULE_JOB_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaRescheduleJobCommand.class)) {
       return false;
     }
     return equals((KahaRescheduleJobCommand)obj);
   }
 
   public boolean equals(KahaRescheduleJobCommand obj) {
     if ((hasScheduler() ^ obj.hasScheduler()))
       return false;
     if ((hasScheduler()) && (!getScheduler().equals(obj.getScheduler())))
       return false;
     if ((hasJobId() ^ obj.hasJobId()))
       return false;
     if ((hasJobId()) && (!getJobId().equals(obj.getJobId())))
       return false;
     if ((hasExecutionTime() ^ obj.hasExecutionTime()))
       return false;
     if ((hasExecutionTime()) && (getExecutionTime() != obj.getExecutionTime()))
       return false;
     if ((hasNextExecutionTime() ^ obj.hasNextExecutionTime()))
       return false;
     if ((hasNextExecutionTime()) && (getNextExecutionTime() != obj.getNextExecutionTime()))
       return false;
     if ((hasRescheduledCount() ^ obj.hasRescheduledCount()))
       return false;
     if ((hasRescheduledCount()) && (getRescheduledCount() != obj.getRescheduledCount()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -900859449;
     if (hasScheduler()) {
       rc ^= 0x6DDDE09B ^ getScheduler().hashCode();
     }
     if (hasJobId()) {
       rc ^= 0x446B998 ^ getJobId().hashCode();
     }
     if (hasExecutionTime()) {
       rc ^= 0x6694B5E5 ^ new Long(getExecutionTime()).hashCode();
     }
     if (hasNextExecutionTime()) {
       rc ^= 0x703C0DB2 ^ new Long(getNextExecutionTime()).hashCode();
     }
     if (hasRescheduledCount()) {
       rc ^= 0xD6510175 ^ getRescheduledCount();
     }
     return rc;
   }
 }





