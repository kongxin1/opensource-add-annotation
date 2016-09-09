 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaRemoveDestinationCommandBase<T> extends BaseMessage<T>
 {
   private KahaDestination f_destination = null;
 
   public boolean hasDestination() {
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
 }





