 package org.apache.activemq.store.kahadb.data;
 
 import org.apache.activemq.protobuf.BaseMessage;
 import org.apache.activemq.protobuf.Buffer;
 
 abstract class KahaXATransactionIdBase<T> extends BaseMessage<T>
 {
   private int f_formatId = 0;
   private boolean b_formatId;
   private Buffer f_branchQualifier = null;
   private boolean b_branchQualifier;
   private Buffer f_globalTransactionId = null;
   private boolean b_globalTransactionId;
 
   public boolean hasFormatId()
   {
     return this.b_formatId;
   }
 
   public int getFormatId() {
     return this.f_formatId;
   }
 
   public T setFormatId(int formatId) {
     loadAndClear();
     this.b_formatId = true;
     this.f_formatId = formatId;
     return this;
   }
 
   public void clearFormatId() {
     loadAndClear();
     this.b_formatId = false;
     this.f_formatId = 0;
   }
 
   public boolean hasBranchQualifier()
   {
     return this.b_branchQualifier;
   }
 
   public Buffer getBranchQualifier() {
     return this.f_branchQualifier;
   }
 
   public T setBranchQualifier(Buffer branchQualifier) {
     loadAndClear();
     this.b_branchQualifier = true;
     this.f_branchQualifier = branchQualifier;
     return this;
   }
 
   public void clearBranchQualifier() {
     loadAndClear();
     this.b_branchQualifier = false;
     this.f_branchQualifier = null;
   }
 
   public boolean hasGlobalTransactionId()
   {
     return this.b_globalTransactionId;
   }
 
   public Buffer getGlobalTransactionId() {
     return this.f_globalTransactionId;
   }
 
   public T setGlobalTransactionId(Buffer globalTransactionId) {
     loadAndClear();
     this.b_globalTransactionId = true;
     this.f_globalTransactionId = globalTransactionId;
     return this;
   }
 
   public void clearGlobalTransactionId() {
     loadAndClear();
     this.b_globalTransactionId = false;
     this.f_globalTransactionId = null;
   }
 }





