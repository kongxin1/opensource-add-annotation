/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class FieldDescriptor
/*     */ {
/*  27 */   public static final String STRING_TYPE = "string".intern();
/*  28 */   public static final String BOOL_TYPE = "bool".intern();
/*  29 */   public static final String BYTES_TYPE = "bytes".intern();
/*  30 */   public static final String DOUBLE_TYPE = "double".intern();
/*  31 */   public static final String FLOAT_TYPE = "float".intern();
/*     */ 
/*  33 */   public static final String INT32_TYPE = "int32".intern();
/*  34 */   public static final String INT64_TYPE = "int64".intern();
/*  35 */   public static final String UINT32_TYPE = "uint32".intern();
/*  36 */   public static final String UINT64_TYPE = "uint64".intern();
/*  37 */   public static final String SINT32_TYPE = "sint32".intern();
/*  38 */   public static final String SINT64_TYPE = "sint64".intern();
/*  39 */   public static final String FIXED32_TYPE = "fixed32".intern();
/*  40 */   public static final String FIXED64_TYPE = "fixed64".intern();
/*  41 */   public static final String SFIXED32_TYPE = "sfixed32".intern();
/*  42 */   public static final String SFIXED64_TYPE = "sfixed64".intern();
/*     */ 
/*  44 */   public static final String REQUIRED_RULE = "required".intern();
/*  45 */   public static final String OPTIONAL_RULE = "optional".intern();
/*  46 */   public static final String REPEATED_RULE = "repeated".intern();
/*     */ 
/*  48 */   public static final Set<String> INT32_TYPES = new HashSet();
/*  49 */   public static final Set<String> INT64_TYPES = new HashSet();
/*  50 */   public static final Set<String> INTEGER_TYPES = new HashSet();
/*  51 */   public static final Set<String> NUMBER_TYPES = new HashSet();
/*  52 */   public static final Set<String> SCALAR_TYPES = new HashSet();
/*     */ 
/*  54 */   public static final Set<String> SIGNED_TYPES = new HashSet();
/*  55 */   public static final Set<String> UNSIGNED_TYPES = new HashSet();
/*     */   private String name;
/*     */   private String type;
/*     */   private String rule;
/*     */   private int tag;
/*     */   private Map<String, OptionDescriptor> options;
/*     */   private TypeDescriptor typeDescriptor;
/*     */   private final MessageDescriptor parent;
/*     */   private MessageDescriptor group;
/*     */ 
/*     */   public FieldDescriptor(MessageDescriptor parent)
/*     */   {
/*  94 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   public void validate(List<String> errors) {
/*  98 */     if (this.group != null) {
/*  99 */       this.typeDescriptor = this.group;
/*     */     }
/* 101 */     if (!SCALAR_TYPES.contains(this.type))
/*     */     {
/* 103 */       if (this.typeDescriptor == null) {
/* 104 */         this.typeDescriptor = this.parent.getType(this.type);
/*     */       }
/* 106 */       if (this.typeDescriptor == null) {
/* 107 */         this.typeDescriptor = this.parent.getProtoDescriptor().getType(this.type);
/*     */       }
/* 109 */       if (this.typeDescriptor == null)
/* 110 */         errors.add("Field type not found: " + this.type);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isGroup()
/*     */   {
/* 116 */     return this.group != null;
/*     */   }
/*     */ 
/*     */   public String getName() {
/* 120 */     return this.name;
/*     */   }
/*     */   public void setName(String name) {
/* 123 */     this.name = name;
/*     */   }
/*     */ 
/*     */   public String getRule() {
/* 127 */     return this.rule;
/*     */   }
/*     */   public void setRule(String rule) {
/* 130 */     this.rule = rule.intern();
/*     */   }
/*     */ 
/*     */   public boolean isOptional() {
/* 134 */     return this.rule == OPTIONAL_RULE;
/*     */   }
/*     */   public boolean isRequired() {
/* 137 */     return this.rule == REQUIRED_RULE;
/*     */   }
/*     */   public boolean isRepeated() {
/* 140 */     return this.rule == REPEATED_RULE;
/*     */   }
/*     */ 
/*     */   public int getTag() {
/* 144 */     return this.tag;
/*     */   }
/*     */   public void setTag(int tag) {
/* 147 */     this.tag = tag;
/*     */   }
/*     */ 
/*     */   public Map<String, OptionDescriptor> getOptions() {
/* 151 */     return this.options;
/*     */   }
/*     */   public void setOptions(Map<String, OptionDescriptor> options) {
/* 154 */     this.options = options;
/*     */   }
/*     */ 
/*     */   public String getType() {
/* 158 */     return this.type;
/*     */   }
/*     */   public void setType(String type) {
/* 161 */     this.type = type.intern();
/*     */   }
/*     */ 
/*     */   public boolean isMessageType() {
/* 165 */     return !SCALAR_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isScalarType() {
/* 169 */     return SCALAR_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isNumberType() {
/* 173 */     return NUMBER_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isIntegerType() {
/* 177 */     return INTEGER_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isInteger32Type() {
/* 181 */     return INT32_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isInteger64Type() {
/* 185 */     return INT64_TYPES.contains(this.type);
/*     */   }
/*     */ 
/*     */   public boolean isStringType() {
/* 189 */     return this.type == STRING_TYPE;
/*     */   }
/*     */ 
/*     */   public TypeDescriptor getTypeDescriptor() {
/* 193 */     return this.typeDescriptor;
/*     */   }
/*     */ 
/*     */   public void setTypeDescriptor(TypeDescriptor typeDescriptor) {
/* 197 */     this.typeDescriptor = typeDescriptor;
/*     */   }
/*     */ 
/*     */   public MessageDescriptor getGroup() {
/* 201 */     return this.group;
/*     */   }
/*     */   public void setGroup(MessageDescriptor group) {
/* 204 */     this.group = group;
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  58 */     INT32_TYPES.add(INT32_TYPE);
/*  59 */     INT32_TYPES.add(UINT32_TYPE);
/*  60 */     INT32_TYPES.add(SINT32_TYPE);
/*  61 */     INT32_TYPES.add(FIXED32_TYPE);
/*  62 */     INT32_TYPES.add(SFIXED32_TYPE);
/*     */ 
/*  64 */     INT64_TYPES.add(INT64_TYPE);
/*  65 */     INT64_TYPES.add(UINT64_TYPE);
/*  66 */     INT64_TYPES.add(SINT64_TYPE);
/*  67 */     INT64_TYPES.add(FIXED64_TYPE);
/*  68 */     INT64_TYPES.add(SFIXED64_TYPE);
/*     */ 
/*  70 */     INTEGER_TYPES.addAll(INT32_TYPES);
/*  71 */     INTEGER_TYPES.addAll(INT64_TYPES);
/*     */ 
/*  73 */     NUMBER_TYPES.addAll(INTEGER_TYPES);
/*  74 */     NUMBER_TYPES.add(DOUBLE_TYPE);
/*  75 */     NUMBER_TYPES.add(FLOAT_TYPE);
/*     */ 
/*  77 */     SCALAR_TYPES.addAll(NUMBER_TYPES);
/*  78 */     SCALAR_TYPES.add(STRING_TYPE);
/*  79 */     SCALAR_TYPES.add(BOOL_TYPE);
/*  80 */     SCALAR_TYPES.add(BYTES_TYPE);
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.FieldDescriptor
 * JD-Core Version:    0.6.2
 */