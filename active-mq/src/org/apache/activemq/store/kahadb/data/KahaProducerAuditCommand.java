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
 
 public final class KahaProducerAuditCommand extends KahaProducerAuditCommandBase<KahaProducerAuditCommand>
   implements JournalCommand<KahaProducerAuditCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasAudit()) {
       missingFields.add("audit");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearAudit();
   }
 
   public KahaProducerAuditCommand clone() {
     return new KahaProducerAuditCommand().mergeFrom(this);
   }
 
   public KahaProducerAuditCommand mergeFrom(KahaProducerAuditCommand other) {
     if (other.hasAudit()) {
       setAudit(other.getAudit());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasAudit()) {
       size += CodedOutputStream.computeBytesSize(1, getAudit());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaProducerAuditCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setAudit(input.readBytes());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasAudit())
       output.writeBytes(1, getAudit());
   }
 
   public static KahaProducerAuditCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaProducerAuditCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaProducerAuditCommand)((KahaProducerAuditCommand)new KahaProducerAuditCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasAudit()) {
       sb.append(prefix + "audit: ");
       sb.append(getAudit());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_PRODUCER_AUDIT_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaProducerAuditCommand.class)) {
       return false;
     }
     return equals((KahaProducerAuditCommand)obj);
   }
 
   public boolean equals(KahaProducerAuditCommand obj) {
     if ((hasAudit() ^ obj.hasAudit()))
       return false;
     if ((hasAudit()) && (!getAudit().equals(obj.getAudit())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = 691941169;
     if (hasAudit()) {
       rc ^= 0x3CAABBB ^ getAudit().hashCode();
     }
     return rc;
   }
 }





