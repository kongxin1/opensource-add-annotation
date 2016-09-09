 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 import org.apache.activemq.protobuf.Buffer;
 
 abstract class KahaRemoveMessageCommandBase<T> extends BaseMessage<T>
 {
   private KahaTransactionInfo f_transactionInfo = null;
 
   private KahaDestination f_destination = null;
 
   private String f_messageId = null;
   private boolean b_messageId;
   private Buffer f_ack = null;
   private boolean b_ack;
   private String f_subscriptionKey = null;
   private boolean b_subscriptionKey;
 
   public boolean hasTransactionInfo()
   {
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
 
   public boolean hasDestination()
   {
     return this.f_destination != null;
   }
 
   public KahaDestination getDestination() {
     if (this.f_destination == null) {
       this.f_destination = new KahaDestination();
     }
     return this.f_destination;
   }
 
   public T setDestination(KahaDestination destination) {
     loadAndClear();
     this.f_destination = destination;
     return this;
   }
 
   public void clearDestination() {
     loadAndClear();
     this.f_destination = null;
   }
 
   public boolean hasMessageId()
   {
     return this.b_messageId;
   }
 
   public String getMessageId() {
     return this.f_messageId;
   }
 
   public T setMessageId(String messageId) {
     loadAndClear();
     this.b_messageId = true;
     this.f_messageId = messageId;
     return this;
   }
 
   public void clearMessageId() {
     loadAndClear();
     this.b_messageId = false;
     this.f_messageId = null;
   }
 
   public boolean hasAck()
   {
     return this.b_ack;
   }
 
   public Buffer getAck() {
     return this.f_ack;
   }
 
   public T setAck(Buffer ack) {
     loadAndClear();
     this.b_ack = true;
     this.f_ack = ack;
     return this;
   }
 
   public void clearAck() {
     loadAndClear();
     this.b_ack = false;
     this.f_ack = null;
   }
 
   public boolean hasSubscriptionKey()
   {
     return this.b_subscriptionKey;
   }
 
   public String getSubscriptionKey() {
     return this.f_subscriptionKey;
   }
 
   public T setSubscriptionKey(String subscriptionKey) {
     loadAndClear();
     this.b_subscriptionKey = true;
     this.f_subscriptionKey = subscriptionKey;
     return this;
   }
 
   public void clearSubscriptionKey() {
     loadAndClear();
     this.b_subscriptionKey = false;
     this.f_subscriptionKey = null;
   }
 }





