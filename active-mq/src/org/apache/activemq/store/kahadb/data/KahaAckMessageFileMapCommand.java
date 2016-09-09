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
 
 public final class KahaAckMessageFileMapCommand extends KahaAckMessageFileMapCommandBase<KahaAckMessageFileMapCommand>
   implements JournalCommand<KahaAckMessageFileMapCommand>
 {
   public ArrayList<String> missingFields()
   {
     ArrayList missingFields = super.missingFields();
     if (!hasAckMessageFileMap()) {
       missingFields.add("ackMessageFileMap");
     }
     return missingFields;
   }
 
   public void clear() {
     super.clear();
     clearAckMessageFileMap();
   }
 
   public KahaAckMessageFileMapCommand clone() {
     return new KahaAckMessageFileMapCommand().mergeFrom(this);
   }
 
   public KahaAckMessageFileMapCommand mergeFrom(KahaAckMessageFileMapCommand other) {
     if (other.hasAckMessageFileMap()) {
       setAckMessageFileMap(other.getAckMessageFileMap());
     }
     return this;
   }
 
   public int serializedSizeUnframed() {
     if (this.memoizedSerializedSize != -1) {
       return this.memoizedSerializedSize;
     }
     int size = 0;
     if (hasAckMessageFileMap()) {
       size += CodedOutputStream.computeBytesSize(1, getAckMessageFileMap());
     }
     this.memoizedSerializedSize = size;
     return size;
   }
 
   public KahaAckMessageFileMapCommand mergeUnframed(CodedInputStream input) throws IOException {
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
         setAckMessageFileMap(input.readBytes());
       }
     }
   }
 
   public void writeUnframed(CodedOutputStream output) throws IOException {
     if (hasAckMessageFileMap())
       output.writeBytes(1, getAckMessageFileMap());
   }
 
   public static KahaAckMessageFileMapCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException, IOException
   {
     return (KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeUnframed(data).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeUnframed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeFramed(data)).checktInitialized();
   }
 
   public static KahaAckMessageFileMapCommand parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
     return (KahaAckMessageFileMapCommand)((KahaAckMessageFileMapCommand)new KahaAckMessageFileMapCommand().mergeFramed(data)).checktInitialized();
   }
 
   public String toString() {
     return toString(new StringBuilder(), "").toString();
   }
 
   public StringBuilder toString(StringBuilder sb, String prefix) {
     if (hasAckMessageFileMap()) {
       sb.append(prefix + "ackMessageFileMap: ");
       sb.append(getAckMessageFileMap());
       sb.append("\n");
     }
     return sb;
   }
 
   public void visit(Visitor visitor) throws IOException {
     visitor.visit(this);
   }
 
   public KahaEntryType type() {
     return KahaEntryType.KAHA_ACK_MESSAGE_FILE_MAP_COMMAND;
   }
 
   public boolean equals(Object obj) {
     if (obj == this) {
       return true;
     }
     if ((obj == null) || (obj.getClass() != KahaAckMessageFileMapCommand.class)) {
       return false;
     }
     return equals((KahaAckMessageFileMapCommand)obj);
   }
 
   public boolean equals(KahaAckMessageFileMapCommand obj) {
     if ((hasAckMessageFileMap() ^ obj.hasAckMessageFileMap()))
       return false;
     if ((hasAckMessageFileMap()) && (!getAckMessageFileMap().equals(obj.getAckMessageFileMap())))
       return false;
     return true;
   }
 
   public int hashCode() {
     int rc = -259621928;
     if (hasAckMessageFileMap()) {
       rc ^= 0x9A5A902 ^ getAckMessageFileMap().hashCode();
     }
     return rc;
   }
 }





