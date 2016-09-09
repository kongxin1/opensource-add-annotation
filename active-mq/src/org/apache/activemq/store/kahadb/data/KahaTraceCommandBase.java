 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaTraceCommandBase<T> extends BaseMessage<T>
 {
   private String f_message = null;
   private boolean b_message;
 
   public boolean hasMessage()
   {
     return this.b_message;
   }
 
   public String getMessage() {
     return this.f_message;
   }
 
   public T setMessage(String message) {
     loadAndClear();
     this.b_message = true;
     this.f_message = message;
     return this;
   }
 
   public void clearMessage() {
     loadAndClear();
     this.b_message = false;
     this.f_message = null;
   }
 }





