 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaRollbackCommandBase<T> extends BaseMessage<T>
 {
   private KahaTransactionInfo f_transactionInfo = null;
 
   public boolean hasTransactionInfo() {
     return this.f_transactionInfo != null;
   }
 
   public KahaTransactionInfo getTransactionInfo() {
     if (this.f_transactionInfo == null) {
       this.f_transactionInfo = new KahaTransactionInfo();
     }
     return this.f_transactionInfo;
   }
 
   public T setTransactionInfo(KahaTransactionInfo transactionInfo) {
     loadAndClear();
     this.f_transactionInfo = transactionInfo;
     return this;
   }
 
   public void clearTransactionInfo() {
     loadAndClear();
     this.f_transactionInfo = null;
   }
 }





