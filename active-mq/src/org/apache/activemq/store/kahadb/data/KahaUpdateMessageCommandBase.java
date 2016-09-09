 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaUpdateMessageCommandBase<T> extends BaseMessage<T>
 {
   private KahaAddMessageCommand f_message = null;
 
   public boolean hasMessage() {
     return this.f_message != null;
   }
 
   public KahaAddMessageCommand getMessage() {
     if (this.f_message == null) {
       this.f_message = new KahaAddMessageCommand();
     }
     return this.f_message;
   }
 
   public T setMessage(KahaAddMessageCommand message) {
     loadAndClear();
     this.f_message = message;
     return this;
   }
 
   public void clearMessage() {
     loadAndClear();
     this.f_message = null;
   }
 }





