 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaDestroySchedulerCommandBase<T> extends BaseMessage<T>
 {
   private String f_scheduler = null;
   private boolean b_scheduler;
 
   public boolean hasScheduler()
   {
     return this.b_scheduler;
   }
 
   public String getScheduler() {
     return this.f_scheduler;
   }
 
   public T setScheduler(String scheduler) {
     loadAndClear();
     this.b_scheduler = true;
     this.f_scheduler = scheduler;
     return this;
   }
 
   public void clearScheduler() {
     loadAndClear();
     this.b_scheduler = false;
     this.f_scheduler = null;
   }
 }





