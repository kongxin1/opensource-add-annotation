 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaRemoveScheduledJobsCommandBase<T> extends BaseMessage<T>
 {
   private String f_scheduler = null;
   private boolean b_scheduler;
   private long f_startTime = 0L;
   private boolean b_startTime;
   private long f_endTime = 0L;
   private boolean b_endTime;
 
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
 
   public boolean hasStartTime()
   {
     return this.b_startTime;
   }
 
   public long getStartTime() {
     return this.f_startTime;
   }
 
   public T setStartTime(long startTime) {
     loadAndClear();
     this.b_startTime = true;
     this.f_startTime = startTime;
     return this;
   }
 
   public void clearStartTime() {
     loadAndClear();
     this.b_startTime = false;
     this.f_startTime = 0L;
   }
 
   public boolean hasEndTime()
   {
     return this.b_endTime;
   }
 
   public long getEndTime() {
     return this.f_endTime;
   }
 
   public T setEndTime(long endTime) {
     loadAndClear();
     this.b_endTime = true;
     this.f_endTime = endTime;
     return this;
   }
 
   public void clearEndTime() {
     loadAndClear();
     this.b_endTime = false;
     this.f_endTime = 0L;
   }
 }





