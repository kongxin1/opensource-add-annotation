 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaRemoveScheduledJobCommandBase<T> extends BaseMessage<T>
 {
   private String f_scheduler = null;
   private boolean b_scheduler;
   private String f_jobId = null;
   private boolean b_jobId;
   private long f_nextExecutionTime = 0L;
   private boolean b_nextExecutionTime;
 
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
 
   public boolean hasJobId()
   {
     return this.b_jobId;
   }
 
   public String getJobId() {
     return this.f_jobId;
   }
 
   public T setJobId(String jobId) {
     loadAndClear();
     this.b_jobId = true;
     this.f_jobId = jobId;
     return this;
   }
 
   public void clearJobId() {
     loadAndClear();
     this.b_jobId = false;
     this.f_jobId = null;
   }
 
   public boolean hasNextExecutionTime()
   {
     return this.b_nextExecutionTime;
   }
 
   public long getNextExecutionTime() {
     return this.f_nextExecutionTime;
   }
 
   public T setNextExecutionTime(long nextExecutionTime) {
     loadAndClear();
     this.b_nextExecutionTime = true;
     this.f_nextExecutionTime = nextExecutionTime;
     return this;
   }
 
   public void clearNextExecutionTime() {
     loadAndClear();
     this.b_nextExecutionTime = false;
     this.f_nextExecutionTime = 0L;
   }
 }





