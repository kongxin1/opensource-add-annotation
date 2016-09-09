/*    */ package org.apache.activemq.protobuf;
/*    */ 
/*    */ public final class WireFormat
/*    */ {
/*    */   public static final int WIRETYPE_VARINT = 0;
/*    */   public static final int WIRETYPE_FIXED64 = 1;
/*    */   public static final int WIRETYPE_LENGTH_DELIMITED = 2;
/*    */   public static final int WIRETYPE_START_GROUP = 3;
/*    */   public static final int WIRETYPE_END_GROUP = 4;
/*    */   public static final int WIRETYPE_FIXED32 = 5;
/*    */   public static final int TAG_TYPE_BITS = 3;
/*    */   public static final int TAG_TYPE_MASK = 7;
/*    */   public static final int MESSAGE_SET_ITEM = 1;
/*    */   public static final int MESSAGE_SET_TYPE_ID = 2;
/*    */   public static final int MESSAGE_SET_MESSAGE = 3;
/* 66 */   public static final int MESSAGE_SET_ITEM_TAG = makeTag(1, 3);
/* 67 */   public static final int MESSAGE_SET_ITEM_END_TAG = makeTag(1, 4);
/* 68 */   public static final int MESSAGE_SET_TYPE_ID_TAG = makeTag(2, 0);
/* 69 */   public static final int MESSAGE_SET_MESSAGE_TAG = makeTag(3, 2);
/*    */ 
/*    */   public static int getTagWireType(int tag)
/*    */   {
/* 47 */     return tag & 0x7;
/*    */   }
/*    */ 
/*    */   public static int getTagFieldNumber(int tag)
/*    */   {
/* 52 */     return tag >>> 3;
/*    */   }
/*    */ 
/*    */   public static int makeTag(int fieldNumber, int wireType)
/*    */   {
/* 57 */     return fieldNumber << 3 | wireType;
/*    */   }
/*    */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.WireFormat
 * JD-Core Version:    0.6.2
 */