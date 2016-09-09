 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaTransactionInfoBase<T> extends BaseMessage<T>
 {
   private KahaLocalTransactionId f_localTransactionId = null;
 
   private KahaXATransactionId f_xaTransactionId = null;
 
   private KahaLocation f_previousEntry = null;
 
   public boolean hasLocalTransactionId()
   {
     return this.f_localTransactionId != null;
   }
 
   public KahaLocalTransactionId getLocalTransactionId() {
     if (this.f_localTransactionId == null) {
       this.f_localTransactionId = new KahaLocalTransactionId();
     }
     return this.f_localTransactionId;
   }
 
   public T setLocalTransactionId(KahaLocalTransactionId localTransactionId) {
     loadAndClear();
     this.f_localTransactionId = localTransactionId;
     return this;
   }
 
   public void clearLocalTransactionId() {
     loadAndClear();
     this.f_localTransactionId = null;
   }
 
   public boolean hasXaTransactionId()
   {
     return this.f_xaTransactionId != null;
   }
 
   public KahaXATransactionId getXaTransactionId() {
     if (this.f_xaTransactionId == null) {
       this.f_xaTransactionId = new KahaXATransactionId();
     }
     return this.f_xaTransactionId;
   }
 
   public T setXaTransactionId(KahaXATransactionId xaTransactionId) {
     loadAndClear();
     this.f_xaTransactionId = xaTransactionId;
     return this;
   }
 
   public void clearXaTransactionId() {
     loadAndClear();
     this.f_xaTransactionId = null;
   }
 
   public boolean hasPreviousEntry()
   {
     return this.f_previousEntry != null;
   }
 
   public KahaLocation getPreviousEntry() {
     if (this.f_previousEntry == null) {
       this.f_previousEntry = new KahaLocation();
     }
     return this.f_previousEntry;
   }
 
   public T setPreviousEntry(KahaLocation previousEntry) {
     loadAndClear();
     this.f_previousEntry = previousEntry;
     return this;
   }
 
   public void clearPreviousEntry() {
     loadAndClear();
     this.f_previousEntry = null;
   }
 }





