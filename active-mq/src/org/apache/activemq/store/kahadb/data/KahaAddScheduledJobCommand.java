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
 
 public final class KahaAddScheduledJobCommand extends KahaAddScheduledJobCommandBase<KahaAddScheduledJobCommand>
   implements JournalCommand<KahaAddScheduledJobCommand>
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
     if (!hasStartTime()) {
       missingFields.add("start_time");
     }
     if (!hasCronEntry()) {
       missingFields.add("cron_entry");
     }
     if (!hasDelay()) {
       missingFields.add("delay");
     }
     if (!hasPeriod()) {
       missingFields.add("period");
     }
     if (!hasRepeat()) {
       missingFields.add("repeat");
     }
     if (!hasPayload()) {
       missingFields.add("payload");
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
     clearStartTime();
     clearCronEntry();
     clearDelay();
     clearPeriod();
     clearRepeat();
     clearPayload();
     clearNextExecutionTime();
   }
 
   public KahaAddScheduledJobCommand clone() {
     return new KahaAddScheduledJobCommand().mergeFrom(this);
   }
 
   public KahaAddScheduledJobCommand mergeFrom(KahaAddScheduledJobCommand other) {
     if (other.hasScheduler()) {
       setScheduler(other.getScheduler());
     }
     if (other.hasJobId()) {
       setJobId(other.getJobId());
     }
     if (other.hasStartTime()) {
       setStartTime(other.getStartTime());
     }
     if (other.hasCronEntry()) {
       setCronEntry(other.getCronEntry());
     }
     if (other.hasDelay()) {
       setDelay(other.getDelay());
     }
     if (other.hasPeriod()) {
       setPeriod(other.getPeriod());
     }
     if (other.hasRepeat()) {
       setRepeat(other.getRepeat());
     }
     if (other.hasPayload()) {
       setPayload(other.getPayload());
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
     if (hasStartTime()) {
       size += CodedOutputStream.computeInt64Size(3, getStartTime());
     }
     if (hasCronEntry()) {
       size += CodedOutputStream.computeStringSize(4, getCronEntry());
     }
     if (hasDelay()) {
       size += CodedOutputStream.computeInt64Size(5, getDelay());
     }
     if (hasPeriod()) {
       size += CodedOutputStream.computeInt64Size(6, getPeriod());
     }
     if (hasRepeat()) {
       size += CodedOutputStream.computeInt32Size(7, getRepeat());
     }
     if (hasPayload()) {
       size += CodedOutputStream.computeBytesSize(8, getPayload());
     }
     if (hasNextExecutionTime()) {
       size += CodedOutputStream.computeInt64Size(9, getNextExecutionTime());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaAddScheduledJobCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setStartTime(input.readInt64());
         break;
       case 34:
         setCronEntry(input.readString());
         break;
       case 40:
         setDelay(input.readInt64());
         break;
       case 48:
         setPeriod(input.readInt64());
         break;
       case 56:
         setRepeat(input.readInt32());
         break;
       case 66:
         setPayload(input.readBytes());
         break;
       case 72:
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
     if (hasStartTime()) {
       output.writeInt64(3, getStartTime());
     }
     if (hasCronEntry()) {
       output.writeString(4, getCronEntry());
     }
     if (hasDelay()) {
       output.writeInt64(5, getDelay());
     }
     if (hasPeriod()) {
       output.writeInt64(6, getPeriod());
     }
     if (hasRepeat()) {
       output.writeInt32(7, getRepeat());
     }
     if (hasPayload()) {
       output.writeBytes(8, getPayload());
     }
     if (hasNextExecutionTime())
       output.writeInt64(9, getNextExecutionTime());
   }
 
   public static KahaAddScheduledJobCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAddScheduledJobCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAddScheduledJobCommand)((KahaAddScheduledJobCommand)new KahaAddScheduledJobCommand().mergeFramed(data)).checktInitialized();
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
     if (hasStartTime()) {
       sb.append(prefix + "start_time: ");
       sb.append(getStartTime());
       sb.append("\n");
     }
     if (hasCronEntry()) {
       sb.append(prefix + "cron_entry: ");
       sb.append(getCronEntry());
       sb.append("\n");
     }
     if (hasDelay()) {
       sb.append(prefix + "delay: ");
       sb.append(getDelay());
       sb.append("\n");
     }
     if (hasPeriod()) {
       sb.append(prefix + "period: ");
       sb.append(getPeriod());
       sb.append("\n");
     }
     if (hasRepeat()) {
       sb.append(prefix + "repeat: ");
       sb.append(getRepeat());
       sb.append("\n");
     }
     if (hasPayload()) {
       sb.append(prefix + "payload: ");
       sb.append(getPayload());
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
     return KahaEntryType.KAHA_ADD_SCHEDULED_JOB_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaAddScheduledJobCommand.class)) {
       return false;
     }
     return equals((KahaAddScheduledJobCommand)obj);
   }
 
   public boolean equals(KahaAddScheduledJobCommand obj) {
     if ((hasScheduler() ^ obj.hasScheduler()))
       return false;
     if ((hasScheduler()) && (!getScheduler().equals(obj.getScheduler())))
       return false;
     if ((hasJobId() ^ obj.hasJobId()))
       return false;
     if ((hasJobId()) && (!getJobId().equals(obj.getJobId())))
       return false;
     if ((hasStartTime() ^ obj.hasStartTime()))
       return false;
     if ((hasStartTime()) && (getStartTime() != obj.getStartTime()))
       return false;
     if ((hasCronEntry() ^ obj.hasCronEntry()))
       return false;
     if ((hasCronEntry()) && (!getCronEntry().equals(obj.getCronEntry())))
       return false;
     if ((hasDelay() ^ obj.hasDelay()))
       return false;
     if ((hasDelay()) && (getDelay() != obj.getDelay()))
       return false;
     if ((hasPeriod() ^ obj.hasPeriod()))
       return false;
     if ((hasPeriod()) && (getPeriod() != obj.getPeriod()))
       return false;
     if ((hasRepeat() ^ obj.hasRepeat()))
       return false;
     if ((hasRepeat()) && (getRepeat() != obj.getRepeat()))
       return false;
     if ((hasPayload() ^ obj.hasPayload()))
       return false;
     if ((hasPayload()) && (!getPayload().equals(obj.getPayload())))
       return false;
     if ((hasNextExecutionTime() ^ obj.hasNextExecutionTime()))
       return false;
     if ((hasNextExecutionTime()) && (getNextExecutionTime() != obj.getNextExecutionTime()))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -1874430263;
     if (hasScheduler()) {
       rc ^= 0x6DDDE09B ^ getScheduler().hashCode();
     }
     if (hasJobId()) {
       rc ^= 0x446B998 ^ getJobId().hashCode();
     }
     if (hasStartTime()) {
       rc ^= 0xF887AA2F ^ new Long(getStartTime()).hashCode();
     }
     if (hasCronEntry()) {
       rc ^= 0xBFB7E664 ^ getCronEntry().hashCode();
     }
     if (hasDelay()) {
       rc ^= 0x3EDC963 ^ new Long(getDelay()).hashCode();
     }
     if (hasPeriod()) {
       rc ^= 0x8E4861E1 ^ new Long(getPeriod()).hashCode();
     }
     if (hasRepeat()) {
       rc ^= 0x91B119BB ^ getRepeat();
     }
     if (hasPayload()) {
       rc ^= 0x3454796E ^ getPayload().hashCode();
     }
     if (hasNextExecutionTime()) {
       rc ^= 0x703C0DB2 ^ new Long(getNextExecutionTime()).hashCode();
     }
     return rc;
   }
 }





