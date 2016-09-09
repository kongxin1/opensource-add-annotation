 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 import org.apache.activemq.protobuf.Buffer;
 
 abstract class KahaAckMessageFileMapCommandBase<T> extends BaseMessage<T>
 {
   private Buffer f_ackMessageFileMap = null;
   private boolean b_ackMessageFileMap;
 
   public boolean hasAckMessageFileMap()
   {
     return this.b_ackMessageFileMap;
   }
 
   public Buffer getAckMessageFileMap() {
     return this.f_ackMessageFileMap;
   }
 
   public T setAckMessageFileMap(Buffer ackMessageFileMap) {
     loadAndClear();
     this.b_ackMessageFileMap = true;
     this.f_ackMessageFileMap = ackMessageFileMap;
     return this;
   }
 
   public void clearAckMessageFileMap() {
     loadAndClear();
     this.b_ackMessageFileMap = false;
     this.f_ackMessageFileMap = null;
   }
 }





