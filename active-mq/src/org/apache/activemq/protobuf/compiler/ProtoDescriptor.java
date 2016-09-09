/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ProtoDescriptor
/*     */ {
/*     */   private String packageName;
/*  27 */   private Map<String, OptionDescriptor> options = new LinkedHashMap();
/*  28 */   private Map<String, MessageDescriptor> messages = new LinkedHashMap();
/*  29 */   private Map<String, EnumDescriptor> enums = new LinkedHashMap();
/*  30 */   private List<MessageDescriptor> extendsList = new ArrayList();
/*  31 */   private Map<String, ServiceDescriptor> services = new LinkedHashMap();
/*  32 */   List<String> imports = new ArrayList();
/*  33 */   Map<String, ProtoDescriptor> importProtoDescriptors = new LinkedHashMap();
/*     */   private String name;
/*     */ 
/*     */   public void setPackageName(String packageName)
/*     */   {
/*  37 */     this.packageName = packageName;
/*     */   }
/*     */ 
/*     */   public void setOptions(Map<String, OptionDescriptor> options) {
/*  41 */     this.options = options;
/*     */   }
/*     */ 
/*     */   public void setMessages(Map<String, MessageDescriptor> messages) {
/*  45 */     this.messages = messages;
/*     */   }
/*     */ 
/*     */   public void setEnums(Map<String, EnumDescriptor> enums) {
/*  49 */     this.enums = enums;
/*     */   }
/*     */ 
/*     */   public void setExtends(List<MessageDescriptor> extendsList) {
/*  53 */     this.extendsList = extendsList;
/*     */   }
/*     */ 
/*     */   public List<MessageDescriptor> getExtends() {
/*  57 */     return this.extendsList;
/*     */   }
/*     */ 
/*     */   public String getPackageName() {
/*  61 */     return this.packageName;
/*     */   }
/*     */ 
/*     */   public Map<String, OptionDescriptor> getOptions() {
/*  65 */     return this.options;
/*     */   }
/*     */ 
/*     */   public Map<String, MessageDescriptor> getMessages() {
/*  69 */     return this.messages;
/*     */   }
/*     */ 
/*     */   public Map<String, EnumDescriptor> getEnums() {
/*  73 */     return this.enums;
/*     */   }
/*     */ 
/*     */   public void setServices(Map<String, ServiceDescriptor> services) {
/*  77 */     this.services = services;
/*     */   }
/*     */ 
/*     */   public Map<String, ServiceDescriptor> getServices() {
/*  81 */     return this.services;
/*     */   }
/*     */ 
/*     */   public void validate(List<String> errors)
/*     */   {
/*  91 */     for (ProtoDescriptor o : this.importProtoDescriptors.values()) {
/*  92 */       o.validate(errors);
/*     */     }
/*  94 */     for (OptionDescriptor o : this.options.values()) {
/*  95 */       o.validate(errors);
/*     */     }
/*  97 */     for (MessageDescriptor o : this.messages.values()) {
/*  98 */       o.validate(errors);
/*     */     }
/* 100 */     for (EnumDescriptor o : this.enums.values()) {
/* 101 */       o.validate(errors);
/*     */     }
/* 103 */     for (MessageDescriptor o : this.extendsList) {
/* 104 */       o.validate(errors);
/*     */     }
/* 106 */     for (ServiceDescriptor o : this.services.values())
/* 107 */       o.validate(errors);
/*     */   }
/*     */ 
/*     */   public List<String> getImports()
/*     */   {
/* 112 */     return this.imports;
/*     */   }
/*     */ 
/*     */   public void setImports(List<String> imports) {
/* 116 */     this.imports = imports;
/*     */   }
/*     */ 
/*     */   public Map<String, ProtoDescriptor> getImportProtoDescriptors() {
/* 120 */     return this.importProtoDescriptors;
/*     */   }
/*     */ 
/*     */   public void setImportProtoDescriptors(Map<String, ProtoDescriptor> importProtoDescriptors) {
/* 124 */     this.importProtoDescriptors = importProtoDescriptors;
/*     */   }
/*     */ 
/*     */   public TypeDescriptor getType(String type) {
/* 128 */     for (MessageDescriptor o : this.messages.values()) {
/* 129 */       if (type.equals(o.getName())) {
/* 130 */         return o;
/*     */       }
/* 132 */       if (type.startsWith(o.getName() + ".")) {
/* 133 */         return o.getType(type.substring(o.getName().length() + 1));
/*     */       }
/*     */     }
/* 136 */     for (EnumDescriptor o : this.enums.values()) {
/* 137 */       if (type.equals(o.getName())) {
/* 138 */         return o;
/*     */       }
/*     */     }
/*     */ 
/* 142 */     for (ProtoDescriptor o : this.importProtoDescriptors.values()) {
/* 143 */       if ((o.getPackageName() != null) && (type.startsWith(o.getPackageName() + "."))) {
/* 144 */         return o.getType(type.substring(o.getPackageName().length() + 1));
/*     */       }
/*     */     }
/* 147 */     for (ProtoDescriptor o : this.importProtoDescriptors.values()) {
/* 148 */       TypeDescriptor rc = o.getType(type);
/* 149 */       if (rc != null) {
/* 150 */         return rc;
/*     */       }
/*     */     }
/* 153 */     return null;
/*     */   }
/*     */ 
/*     */   public String getName() {
/* 157 */     return this.name;
/*     */   }
/*     */ 
/*     */   public void setName(String name) {
/* 161 */     this.name = name;
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.ProtoDescriptor
 * JD-Core Version:    0.6.2
 */