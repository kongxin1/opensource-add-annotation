/*    */ package org.apache.activemq.protobuf.compiler;
/*    */ 
/*    */ public class EnumFieldDescriptor
/*    */ {
/*    */   private String name;
/*    */   private int value;
/*    */   private final EnumDescriptor parent;
/*    */   private TypeDescriptor associatedType;
/*    */ 
/*    */   public EnumFieldDescriptor(EnumDescriptor parent)
/*    */   {
/* 27 */     this.parent = parent;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 31 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public void setValue(int value) {
/* 35 */     this.value = value;
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 39 */     return this.name;
/*    */   }
/*    */ 
/*    */   public int getValue() {
/* 43 */     return this.value;
/*    */   }
/*    */ 
/*    */   public EnumDescriptor getParent() {
/* 47 */     return this.parent;
/*    */   }
/*    */ 
/*    */   public TypeDescriptor getAssociatedType() {
/* 51 */     return this.associatedType;
/*    */   }
/*    */ 
/*    */   public void associate(TypeDescriptor associatedType) {
/* 55 */     this.associatedType = associatedType;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.EnumFieldDescriptor
 * JD-Core Version:    0.6.2
 */