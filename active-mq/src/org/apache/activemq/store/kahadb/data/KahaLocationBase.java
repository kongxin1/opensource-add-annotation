 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 
 abstract class KahaLocationBase<T> extends BaseMessage<T>
 {
   private int f_logId = 0;
   private boolean b_logId;
   private int f_offset = 0;
   private boolean b_offset;
 
   public boolean hasLogId()
   {
     return this.b_logId;
   }
 
   public int getLogId() {
     return this.f_logId;
   }
 
   public T setLogId(int logId) {
     loadAndClear();
     this.b_logId = true;
     this.f_logId = logId;
     return this;
   }
 
   public void clearLogId() {
     loadAndClear();
     this.b_logId = false;
     this.f_logId = 0;
   }
 
   public boolean hasOffset()
   {
     return this.b_offset;
   }
 
   public int getOffset() {
     return this.f_offset;
   }
 
   public T setOffset(int offset) {
     loadAndClear();
     this.b_offset = true;
     this.f_offset = offset;
     return this;
   }
 
   public void clearOffset() {
     loadAndClear();
     this.b_offset = false;
     this.f_offset = 0;
   }
 }





