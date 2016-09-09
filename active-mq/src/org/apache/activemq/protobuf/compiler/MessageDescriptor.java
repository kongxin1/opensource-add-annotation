/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class MessageDescriptor
/*     */   implements TypeDescriptor
/*     */ {
/*     */   private String name;
/*     */   private ExtensionsDescriptor extensions;
/*  32 */   private Map<String, FieldDescriptor> fields = new LinkedHashMap();
/*  33 */   private Map<String, MessageDescriptor> messages = new LinkedHashMap();
/*  34 */   private Map<String, EnumDescriptor> enums = new LinkedHashMap();
/*     */   private final ProtoDescriptor protoDescriptor;
/*  36 */   private List<MessageDescriptor> extendsList = new ArrayList();
/*  37 */   private Map<String, OptionDescriptor> options = new LinkedHashMap();
/*  38 */   private List<EnumFieldDescriptor> associatedEnumFieldDescriptors = new ArrayList();
/*     */   private final MessageDescriptor parent;
/*     */   private MessageDescriptor baseType;
/*     */ 
/*     */   public MessageDescriptor(ProtoDescriptor protoDescriptor, MessageDescriptor parent)
/*     */   {
/*  44 */     this.protoDescriptor = protoDescriptor;
/*  45 */     this.parent = parent;
/*     */   }
/*     */ 
/*     */   public void validate(List<String> errors) {
/*  49 */     String baseName = getOption(getOptions(), "base_type", null);
/*  50 */     if (baseName != null) {
/*  51 */       if (this.baseType == null) {
/*  52 */         this.baseType = ((MessageDescriptor)getType(baseName));
/*     */       }
/*  54 */       if (this.baseType == null) {
/*  55 */         this.baseType = ((MessageDescriptor)getProtoDescriptor().getType(baseName));
/*     */       }
/*  57 */       if (this.baseType == null) {
/*  58 */         errors.add("base_type option not valid, type not found: " + baseName);
/*     */       }
/*     */ 
/*  62 */       HashSet baseFieldNames = new HashSet(this.baseType.getFields().keySet());
/*  63 */       baseFieldNames.removeAll(getFields().keySet());
/*     */ 
/*  66 */       if (!baseFieldNames.isEmpty()) {
/*  67 */         for (String fieldName : baseFieldNames) {
/*  68 */           errors.add("base_type " + baseName + " field " + fieldName + " not defined in " + getName());
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  73 */     for (FieldDescriptor field : this.fields.values()) {
/*  74 */       field.validate(errors);
/*     */     }
/*  76 */     for (EnumDescriptor o : this.enums.values()) {
/*  77 */       o.validate(errors);
/*     */     }
/*  79 */     for (MessageDescriptor o : this.messages.values())
/*  80 */       o.validate(errors);
/*     */   }
/*     */ 
/*     */   public String getOption(Map<String, OptionDescriptor> options, String optionName, String defaultValue)
/*     */   {
/*  85 */     OptionDescriptor optionDescriptor = (OptionDescriptor)options.get(optionName);
/*  86 */     if (optionDescriptor == null) {
/*  87 */       return defaultValue;
/*     */     }
/*  89 */     return optionDescriptor.getValue();
/*     */   }
/*     */ 
/*     */   public void setName(String name) {
/*  93 */     this.name = name;
/*     */   }
/*     */ 
/*     */   public void setExtensions(ExtensionsDescriptor extensions) {
/*  97 */     this.extensions = extensions;
/*     */   }
/*     */ 
/*     */   public void setExtends(List<MessageDescriptor> extendsList) {
/* 101 */     this.extendsList = extendsList;
/*     */   }
/*     */   public List<MessageDescriptor> getExtends() {
/* 104 */     return this.extendsList;
/*     */   }
/*     */ 
/*     */   public void setFields(Map<String, FieldDescriptor> fields) {
/* 108 */     this.fields = fields;
/*     */   }
/*     */ 
/*     */   public void setMessages(Map<String, MessageDescriptor> messages) {
/* 112 */     this.messages = messages;
/*     */   }
/*     */ 
/*     */   public void setEnums(Map<String, EnumDescriptor> enums) {
/* 116 */     this.enums = enums;
/*     */   }
/*     */ 
/*     */   public String getName() {
/* 120 */     return this.name;
/*     */   }
/*     */ 
/*     */   public String getQName() {
/* 124 */     if (this.parent == null) {
/* 125 */       return this.name;
/*     */     }
/* 127 */     return this.parent.getQName() + "." + this.name;
/*     */   }
/*     */ 
/*     */   public ExtensionsDescriptor getExtensions()
/*     */   {
/* 132 */     return this.extensions;
/*     */   }
/*     */ 
/*     */   public Map<String, FieldDescriptor> getFields() {
/* 136 */     return this.fields;
/*     */   }
/*     */ 
/*     */   public Map<String, MessageDescriptor> getMessages() {
/* 140 */     return this.messages;
/*     */   }
/*     */ 
/*     */   public Map<String, EnumDescriptor> getEnums() {
/* 144 */     return this.enums;
/*     */   }
/*     */ 
/*     */   public ProtoDescriptor getProtoDescriptor() {
/* 148 */     return this.protoDescriptor;
/*     */   }
/*     */ 
/*     */   public Map<String, OptionDescriptor> getOptions() {
/* 152 */     return this.options;
/*     */   }
/*     */ 
/*     */   public void setOptions(Map<String, OptionDescriptor> options) {
/* 156 */     this.options = options;
/*     */   }
/*     */ 
/*     */   public MessageDescriptor getParent() {
/* 160 */     return this.parent;
/*     */   }
/*     */ 
/*     */   public TypeDescriptor getType(String t) {
/* 164 */     for (MessageDescriptor o : this.messages.values()) {
/* 165 */       if (t.equals(o.getName())) {
/* 166 */         return o;
/*     */       }
/* 168 */       if (t.startsWith(o.getName() + ".")) {
/* 169 */         return o.getType(t.substring(o.getName().length() + 1));
/*     */       }
/*     */     }
/* 172 */     for (EnumDescriptor o : this.enums.values()) {
/* 173 */       if (t.equals(o.getName())) {
/* 174 */         return o;
/*     */       }
/*     */     }
/* 177 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean isEnum() {
/* 181 */     return false;
/*     */   }
/*     */ 
/*     */   public MessageDescriptor getBaseType() {
/* 185 */     return this.baseType;
/*     */   }
/*     */ 
/*     */   public void associate(EnumFieldDescriptor desc) {
/* 189 */     this.associatedEnumFieldDescriptors.add(desc);
/*     */   }
/*     */ 
/*     */   public List<EnumFieldDescriptor> getAssociatedEnumFieldDescriptors() {
/* 193 */     return this.associatedEnumFieldDescriptors;
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.MessageDescriptor
 * JD-Core Version:    0.6.2
 */