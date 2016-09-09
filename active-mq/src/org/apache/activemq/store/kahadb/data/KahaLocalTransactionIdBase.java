 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaLocalTransactionIdBase<T> extends BaseMessage<T>
 {
   private String f_connectionId = null;
   private boolean b_connectionId;
   private long f_transactionId = 0L;
   private boolean b_transactionId;
 
   public boolean hasConnectionId()
   {
     return this.b_connectionId;
   }
 
   public String getConnectionId() {
     return this.f_connectionId;
   }
 
   public T setConnectionId(String connectionId) {
     loadAndClear();
     this.b_connectionId = true;
     this.f_connectionId = connectionId;
     return this;
   }
 
   public void clearConnectionId() {
     loadAndClear();
     this.b_connectionId = false;
     this.f_connectionId = null;
   }
 
   public boolean hasTransactionId()
   {
     return this.b_transactionId;
   }
 
   public long getTransactionId() {
     return this.f_transactionId;
   }
 
   public T setTransactionId(long transactionId) {
     loadAndClear();
     this.b_transactionId = true;
     this.f_transactionId = transactionId;
     return this;
   }
 
   public void clearTransactionId() {
     loadAndClear();
     this.b_transactionId = false;
     this.f_transactionId = 0L;
   }
 }





