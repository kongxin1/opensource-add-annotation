 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaRescheduleJobCommandBase<T> extends BaseMessage<T>
 {
   private String f_scheduler = null;
   private boolean b_scheduler;
   private String f_jobId = null;
   private boolean b_jobId;
   private long f_executionTime = 0L;
   private boolean b_executionTime;
   private long f_nextExecutionTime = 0L;
   private boolean b_nextExecutionTime;
   private int f_rescheduledCount = 0;
   private boolean b_rescheduledCount;
 
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
 
   public boolean hasExecutionTime()
   {
     return this.b_executionTime;
   }
 
   public long getExecutionTime() {
     return this.f_executionTime;
   }
 
   public T setExecutionTime(long executionTime) {
     loadAndClear();
     this.b_executionTime = true;
     this.f_executionTime = executionTime;
     return this;
   }
 
   public void clearExecutionTime() {
     loadAndClear();
     this.b_executionTime = false;
     this.f_executionTime = 0L;
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
 
   public boolean hasRescheduledCount()
   {
     return this.b_rescheduledCount;
   }
 
   public int getRescheduledCount() {
     return this.f_rescheduledCount;
   }
 
   public T setRescheduledCount(int rescheduledCount) {
     loadAndClear();
     this.b_rescheduledCount = true;
     this.f_rescheduledCount = rescheduledCount;
     return this;
   }
 
   public void clearRescheduledCount() {
     loadAndClear();
     this.b_rescheduledCount = false;
     this.f_rescheduledCount = 0;
   }
 }





