 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 import org.apache.activemq.protobuf.Buffer;
 
 abstract class KahaProducerAuditCommandBase<T> extends BaseMessage<T>
 {
   private Buffer f_audit = null;
   private boolean b_audit;
 
   public boolean hasAudit()
   {
     return this.b_audit;
   }
 
   public Buffer getAudit() {
     return this.f_audit;
   }
 
   public T setAudit(Buffer audit) {
     loadAndClear();
     this.b_audit = true;
     this.f_audit = audit;
     return this;
   }
 
   public void clearAudit() {
     loadAndClear();
     this.b_audit = false;
     this.f_audit = null;
   }
 }





