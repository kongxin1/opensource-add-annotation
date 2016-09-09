 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 import org.apache.activemq.protobuf.Buffer;
 
 abstract class KahaSubscriptionCommandBase<T> extends BaseMessage<T>
 {
   private KahaDestination f_destination = null;
 
   private String f_subscriptionKey = null;
   private boolean b_subscriptionKey;
   private boolean f_retroactive = false;
   private boolean b_retroactive;
   private Buffer f_subscriptionInfo = null;
   private boolean b_subscriptionInfo;
 
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
 
   public boolean hasRetroactive()
   {
     return this.b_retroactive;
   }
 
   public boolean getRetroactive() {
     return this.f_retroactive;
   }
 
   public T setRetroactive(boolean retroactive) {
     loadAndClear();
     this.b_retroactive = true;
     this.f_retroactive = retroactive;
     return this;
   }
 
   public void clearRetroactive() {
     loadAndClear();
     this.b_retroactive = false;
     this.f_retroactive = false;
   }
 
   public boolean hasSubscriptionInfo()
   {
     return this.b_subscriptionInfo;
   }
 
   public Buffer getSubscriptionInfo() {
     return this.f_subscriptionInfo;
   }
 
   public T setSubscriptionInfo(Buffer subscriptionInfo) {
     loadAndClear();
     this.b_subscriptionInfo = true;
     this.f_subscriptionInfo = subscriptionInfo;
     return this;
   }
 
   public void clearSubscriptionInfo() {
     loadAndClear();
     this.b_subscriptionInfo = false;
     this.f_subscriptionInfo = null;
   }
 }





