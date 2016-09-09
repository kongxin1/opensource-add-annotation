/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class EnumDescriptor
/*     */   implements TypeDescriptor
/*     */ {
/*     */   private String name;
/*  26 */   private Map<String, EnumFieldDescriptor> fields = new LinkedHashMap();
/*     */   private final ProtoDescriptor protoDescriptor;
/*     */   private final MessageDescriptor parent;
/*  29 */   private Map<String, OptionDescriptor> options = new LinkedHashMap();
/*     */ 
/*     */   public EnumDescriptor(ProtoDescriptor protoDescriptor, MessageDescriptor parent) {
/*  32 */     this.protoDescriptor = protoDescriptor;
/*  33 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   public String getName() {
/*  37 */     return this.name;
/*     */   }
/*     */ 
/*     */   public Map<String, EnumFieldDescriptor> getFields() {
/*  41 */     return this.fields;
/*     */   }
/*     */ 
/*     */   public void setName(String name) {
/*  45 */     this.name = name;
/*     */   }
/*     */ 
/*     */   public void setFields(Map<String, EnumFieldDescriptor> fields) {
/*  49 */     this.fields = fields;
/*     */   }
/*     */   public ProtoDescriptor getProtoDescriptor() {
/*  52 */     return this.protoDescriptor;
/*     */   }
/*     */ 
/*     */   private String getOption(Map<String, OptionDescriptor> options, String optionName, String defaultValue) {
/*  56 */     OptionDescriptor optionDescriptor = (OptionDescriptor)options.get(optionName);
/*  57 */     if (optionDescriptor == null) {
/*  58 */       return defaultValue;
/*     */     }
/*  60 */     return optionDescriptor.getValue();
/*     */   }
/*     */ 
/*     */   private String constantToUCamelCase(String name) {
/*  64 */     boolean upNext = true;
/*  65 */     StringBuilder sb = new StringBuilder();
/*  66 */     for (int i = 0; i < name.length(); i++) {
/*  67 */       char c = name.charAt(i);
/*  68 */       if ((Character.isJavaIdentifierPart(c)) && (Character.isLetterOrDigit(c))) {
/*  69 */         if (upNext) {
/*  70 */           c = Character.toUpperCase(c);
/*  71 */           upNext = false;
/*     */         } else {
/*  73 */           c = Character.toLowerCase(c);
/*     */         }
/*  75 */         sb.append(c);
/*     */       } else {
/*  77 */         upNext = true;
/*     */       }
/*     */     }
/*  80 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public void validate(List<String> errors) {
/*  84 */     String createMessage = getOption(getOptions(), "java_create_message", null);
/*  85 */     if ("true".equals(createMessage))
/*  86 */       for (EnumFieldDescriptor field : getFields().values()) {
/*  87 */         String type = constantToUCamelCase(field.getName());
/*     */ 
/*  89 */         TypeDescriptor typeDescriptor = null;
/*     */ 
/*  91 */         if (this.parent != null) {
/*  92 */           typeDescriptor = this.parent.getType(type);
/*     */         }
/*  94 */         if (typeDescriptor == null) {
/*  95 */           typeDescriptor = this.protoDescriptor.getType(type);
/*     */         }
/*  97 */         if (typeDescriptor == null) {
/*  98 */           errors.add("ENUM constant '" + field.getName() + "' did not find expected associated message: " + type);
/*     */         } else {
/* 100 */           field.associate(typeDescriptor);
/* 101 */           typeDescriptor.associate(field);
/*     */         }
/*     */       }
/*     */   }
/*     */ 
/*     */   public MessageDescriptor getParent()
/*     */   {
/* 108 */     return this.parent;
/*     */   }
/*     */ 
/*     */   public String getQName() {
/* 112 */     if (this.parent == null) {
/* 113 */       return this.name;
/*     */     }
/* 115 */     return this.parent.getQName() + "." + this.name;
/*     */   }
/*     */ 
/*     */   public boolean isEnum()
/*     */   {
/* 120 */     return true;
/*     */   }
/*     */ 
/*     */   public Map<String, OptionDescriptor> getOptions() {
/* 124 */     return this.options;
/*     */   }
/*     */ 
/*     */   public void setOptions(Map<String, OptionDescriptor> options) {
/* 128 */     this.options = options;
/*     */   }
/*     */ 
/*     */   public void associate(EnumFieldDescriptor desc) {
/* 132 */     throw new RuntimeException("not supported.");
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.EnumDescriptor
 * JD-Core Version:    0.6.2
 */