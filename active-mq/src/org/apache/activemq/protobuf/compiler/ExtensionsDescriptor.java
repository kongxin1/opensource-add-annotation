/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ public class ExtensionsDescriptor
/*    */ {
/*    */   private int first;
/*    */   private int last;
/*    */   private final MessageDescriptor parent;
/*    */ 
/*    */   public ExtensionsDescriptor(MessageDescriptor parent)
/*    */   {
/* 26 */     this.parent = parent;
/*    */   }
/*    */ 
/*    */   public void setFirst(int first) {
/* 30 */     this.first = first;
/*    */   }
/*    */ 
/*    */   public void setLast(int last) {
/* 34 */     this.last = last;
/*    */   }
/*    */ 
/*    */   public int getFirst() {
/* 38 */     return this.first;
/*    */   }
/*    */ 
/*    */   public int getLast() {
/* 42 */     return this.last;
/*    */   }
/*    */ 
/*    */   public MessageDescriptor getParent() {
/* 46 */     return this.parent;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.ExtensionsDescriptor
 * JD-Core Version:    0.6.2
 */