/*      */ package org.apache.activemq.protobuf.compiler;
/*      */ 
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.PrintStream;
/*      */ import java.io.PrintWriter;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashSet;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.Map;
/*      */ import java.util.StringTokenizer;
/*      */ import org.apache.activemq.protobuf.WireFormat;
/*      */ import org.apache.activemq.protobuf.compiler.parser.ParseException;
/*      */ import org.apache.activemq.protobuf.compiler.parser.ProtoParser;
/*      */ 
/*      */ public class AltJavaGenerator
/*      */ {
/*      */   private File out;
/*      */   private File[] path;
/*      */   private ProtoDescriptor proto;
/*      */   private String javaPackage;
/*      */   private String outerClassName;
/*      */   private PrintWriter w;
/*      */   private int indent;
/*      */   private ArrayList<String> errors;
/*      */   private boolean multipleFiles;
/*      */   private boolean auto_clear_optional_fields;
/* 1998 */   static final char[] HEX_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*      */ 
/*      */   public AltJavaGenerator()
/*      */   {
/*   41 */     this.out = new File(".");
/*   42 */     this.path = new File[] { new File(".") };
/*      */ 
/*   49 */     this.errors = new ArrayList();
/*      */   }
/*      */ 
/*      */   public static void main(String[] args)
/*      */   {
/*   55 */     AltJavaGenerator generator = new AltJavaGenerator();
/*   56 */     args = CommandLineSupport.setOptions(generator, args);
/*      */ 
/*   58 */     if (args.length == 0) {
/*   59 */       System.out.println("No proto files specified.");
/*      */     }
/*   61 */     for (int i = 0; i < args.length; i++)
/*      */       try {
/*   63 */         System.out.println("Compiling: " + args[i]);
/*   64 */         generator.compile(new File(args[i]));
/*      */       } catch (CompilerException e) {
/*   66 */         System.out.println("Protocol Buffer Compiler failed with the following error(s):");
/*   67 */         for (String error : e.getErrors()) {
/*   68 */           System.out.println("");
/*   69 */           System.out.println(error);
/*      */         }
/*   71 */         System.out.println("");
/*   72 */         System.out.println("Compile failed.  For more details see error messages listed above.");
/*   73 */         return;
/*      */       }
/*      */   }
/*      */ 
/*      */   public void compile(File file)
/*      */     throws CompilerException
/*      */   {
/*   86 */     FileInputStream is = null;
/*      */     try {
/*   88 */       is = new FileInputStream(file);
/*   89 */       ProtoParser parser = new ProtoParser(is);
/*   90 */       this.proto = parser.ProtoDescriptor();
/*   91 */       this.proto.setName(file.getName());
/*   92 */       loadImports(this.proto, file.getParentFile());
/*   93 */       this.proto.validate(this.errors);
/*      */     } catch (FileNotFoundException e) {
/*   95 */       this.errors.add("Failed to open: " + file.getPath() + ":" + e.getMessage());
/*      */     } catch (ParseException e) {
/*   97 */       this.errors.add("Failed to parse: " + file.getPath() + ":" + e.getMessage()); } finally {
/*      */       try {
/*   99 */         is.close(); } catch (Throwable ignore) {
/*      */       }
/*      */     }
/*  102 */     if (!this.errors.isEmpty()) {
/*  103 */       throw new CompilerException(this.errors);
/*      */     }
/*      */ 
/*  107 */     this.javaPackage = javaPackage(this.proto);
/*  108 */     this.outerClassName = javaClassName(this.proto);
/*      */ 
/*  110 */     this.multipleFiles = isMultipleFilesEnabled(this.proto);
/*      */ 
/*  112 */     this.auto_clear_optional_fields = Boolean.parseBoolean(getOption(this.proto.getOptions(), "auto_clear_optional_fields", "false"));
/*      */ 
/*  114 */     if (this.multipleFiles)
/*  115 */       generateProtoFile();
/*      */     else {
/*  117 */       writeFile(this.outerClassName, new Closure() {
/*      */         public void execute() throws CompilerException {
/*  119 */           AltJavaGenerator.this.generateProtoFile();
/*      */         }
/*      */       });
/*      */     }
/*      */ 
/*  124 */     if (!this.errors.isEmpty())
/*  125 */       throw new CompilerException(this.errors);
/*      */   }
/*      */ 
/*      */   private void writeFile(String className, Closure closure)
/*      */     throws CompilerException
/*      */   {
/*  131 */     PrintWriter oldWriter = this.w;
/*      */ 
/*  133 */     File outputFile = this.out;
/*  134 */     if (this.javaPackage != null) {
/*  135 */       String packagePath = this.javaPackage.replace('.', '/');
/*  136 */       outputFile = new File(outputFile, packagePath);
/*      */     }
/*  138 */     outputFile = new File(outputFile, className + ".java");
/*      */ 
/*  141 */     outputFile.getParentFile().mkdirs();
/*      */ 
/*  143 */     FileOutputStream fos = null;
/*      */     try {
/*  145 */       fos = new FileOutputStream(outputFile);
/*  146 */       this.w = new PrintWriter(fos);
/*  147 */       closure.execute();
/*  148 */       this.w.flush();
/*      */     } catch (FileNotFoundException e) {
/*  150 */       this.errors.add("Failed to write to: " + outputFile.getPath() + ":" + e.getMessage()); } finally {
/*      */       try {
/*  152 */         fos.close(); } catch (Throwable ignore) {
/*  153 */       }this.w = oldWriter;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void loadImports(ProtoDescriptor proto, File protoDir) {
/*  158 */     LinkedHashMap children = new LinkedHashMap();
/*  159 */     for (String imp : proto.getImports()) {
/*  160 */       File file = new File(protoDir, imp);
/*  161 */       for (int i = 0; (i < this.path.length) && (!file.exists()); i++) {
/*  162 */         file = new File(this.path[i], imp);
/*      */       }
/*  164 */       if (!file.exists()) {
/*  165 */         this.errors.add("Cannot load import: " + imp);
/*      */       }
/*      */ 
/*  168 */       FileInputStream is = null;
/*      */       try {
/*  170 */         is = new FileInputStream(file);
/*  171 */         ProtoParser parser = new ProtoParser(is);
/*  172 */         ProtoDescriptor child = parser.ProtoDescriptor();
/*  173 */         child.setName(file.getName());
/*  174 */         loadImports(child, file.getParentFile());
/*  175 */         children.put(imp, child);
/*      */       } catch (ParseException e) {
/*  177 */         this.errors.add("Failed to parse: " + file.getPath() + ":" + e.getMessage());
/*      */       } catch (FileNotFoundException e) {
/*  179 */         this.errors.add("Failed to open: " + file.getPath() + ":" + e.getMessage()); } finally {
/*      */         try {
/*  181 */           is.close(); } catch (Throwable ignore) {  }
/*      */       }
/*      */     }
/*  184 */     proto.setImportProtoDescriptors(children);
/*      */   }
/*      */ 
/*      */   private void generateProtoFile() throws CompilerException
/*      */   {
/*  189 */     if (this.multipleFiles) {
/*  190 */       for (EnumDescriptor value : this.proto.getEnums().values()) {
/*  191 */         final EnumDescriptor o = value;
/*  192 */         String className = uCamel(o.getName());
/*  193 */         writeFile(className, new Closure() {
/*      */           public void execute() throws CompilerException {
/*  195 */             AltJavaGenerator.this.generateFileHeader();
/*  196 */             AltJavaGenerator.this.generateEnum(o);
/*      */           }
/*      */         });
/*      */       }
/*  200 */       for (MessageDescriptor value : this.proto.getMessages().values()) {
/*  201 */         final MessageDescriptor o = value;
/*  202 */         String className = uCamel(o.getName());
/*  203 */         writeFile(className, new Closure() {
/*      */           public void execute() throws CompilerException {
/*  205 */             AltJavaGenerator.this.generateFileHeader();
/*  206 */             AltJavaGenerator.this.generateMessageBean(o);
/*      */           }
/*      */         });
/*      */       }
/*      */     }
/*      */     else {
/*  212 */       generateFileHeader();
/*      */ 
/*  214 */       p("public class " + this.outerClassName + " {");
/*  215 */       indent();
/*      */ 
/*  217 */       for (EnumDescriptor enumType : this.proto.getEnums().values()) {
/*  218 */         generateEnum(enumType);
/*      */       }
/*  220 */       for (MessageDescriptor m : this.proto.getMessages().values()) {
/*  221 */         generateMessageBean(m);
/*      */       }
/*      */ 
/*  224 */       unindent();
/*  225 */       p("}");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateFileHeader() {
/*  230 */     p("//");
/*  231 */     p("// Generated by protoc, do not edit by hand.");
/*  232 */     p("//");
/*  233 */     if (this.javaPackage != null) {
/*  234 */       p("package " + this.javaPackage + ";");
/*  235 */       p("");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMessageBean(MessageDescriptor m)
/*      */   {
/*  241 */     String className = uCamel(m.getName());
/*  242 */     String beanClassName = className + "Bean";
/*  243 */     String bufferClassName = className + "Buffer";
/*  244 */     p();
/*      */ 
/*  246 */     String staticOption = "static ";
/*  247 */     if ((this.multipleFiles) && (m.getParent() == null)) {
/*  248 */       staticOption = "";
/*      */     }
/*      */ 
/*  251 */     String extendsClause = " extends org.apache.activemq.protobuf.PBMessage<" + className + "." + beanClassName + ", " + className + "." + bufferClassName + ">";
/*  252 */     for (EnumFieldDescriptor enumFeild : m.getAssociatedEnumFieldDescriptors()) {
/*  253 */       String name = uCamel(enumFeild.getParent().getName());
/*  254 */       name = name + "." + name + "Creatable";
/*  255 */       extendsClause = extendsClause + ", " + name;
/*      */     }
/*      */ 
/*  258 */     p(staticOption + "public interface " + className + extendsClause + " {");
/*  259 */     p();
/*  260 */     indent();
/*      */ 
/*  262 */     for (EnumDescriptor enumType : m.getEnums().values()) {
/*  263 */       generateEnum(enumType);
/*      */     }
/*      */ 
/*  267 */     for (MessageDescriptor subMessage : m.getMessages().values()) {
/*  268 */       generateMessageBean(subMessage);
/*      */     }
/*      */ 
/*  272 */     for (FieldDescriptor field : m.getFields().values()) {
/*  273 */       if (field.isGroup()) {
/*  274 */         generateMessageBean(field.getGroup());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  279 */     for (FieldDescriptor field : m.getFields().values()) {
/*  280 */       generateFieldGetterSignatures(field);
/*      */     }
/*      */ 
/*  283 */     p("public " + beanClassName + " copy();");
/*  284 */     p("public " + bufferClassName + " freeze();");
/*  285 */     p("public java.lang.StringBuilder toString(java.lang.StringBuilder sb, String prefix);");
/*      */ 
/*  288 */     p();
/*  289 */     p("static public final class " + beanClassName + " implements " + className + " {");
/*  290 */     p();
/*  291 */     indent();
/*      */ 
/*  293 */     p("" + bufferClassName + " frozen;");
/*  294 */     p("" + beanClassName + " bean;");
/*  295 */     p();
/*  296 */     p("public " + beanClassName + "() {");
/*  297 */     indent();
/*  298 */     p("this.bean = this;");
/*  299 */     unindent();
/*  300 */     p("}");
/*  301 */     p();
/*  302 */     p("public " + beanClassName + "(" + beanClassName + " copy) {");
/*  303 */     indent();
/*  304 */     p("this.bean = copy;");
/*  305 */     unindent();
/*  306 */     p("}");
/*  307 */     p();
/*  308 */     p("public " + beanClassName + " copy() {");
/*  309 */     indent();
/*  310 */     p("return new " + beanClassName + "(bean);");
/*  311 */     unindent();
/*  312 */     p("}");
/*  313 */     p();
/*      */ 
/*  315 */     generateMethodFreeze(m, bufferClassName);
/*      */ 
/*  317 */     p("private void copyCheck() {");
/*  318 */     indent();
/*  319 */     p("assert frozen==null : org.apache.activemq.protobuf.MessageBufferSupport.FORZEN_ERROR_MESSAGE;");
/*  320 */     p("if (bean != this) {");
/*  321 */     indent();
/*  322 */     p("copy(bean);");
/*  323 */     unindent();
/*  324 */     p("}");
/*  325 */     unindent();
/*  326 */     p("}");
/*  327 */     p();
/*      */ 
/*  329 */     generateMethodCopyFromBean(m, beanClassName);
/*      */ 
/*  332 */     for (FieldDescriptor field : m.getFields().values()) {
/*  333 */       generateFieldAccessor(beanClassName, field);
/*      */     }
/*      */ 
/*  336 */     generateMethodToString(m);
/*      */ 
/*  338 */     generateMethodMergeFromStream(m, beanClassName);
/*      */ 
/*  340 */     generateBeanEquals(m, beanClassName);
/*      */ 
/*  342 */     generateMethodMergeFromBean(m, className);
/*      */ 
/*  344 */     generateMethodClear(m);
/*      */ 
/*  346 */     generateReadWriteExternal(m);
/*      */ 
/*  348 */     for (EnumFieldDescriptor enumFeild : m.getAssociatedEnumFieldDescriptors()) {
/*  349 */       String enumName = uCamel(enumFeild.getParent().getName());
/*  350 */       p("public " + enumName + " to" + enumName + "() {");
/*  351 */       indent();
/*  352 */       p("return " + enumName + "." + enumFeild.getName() + ";");
/*  353 */       unindent();
/*  354 */       p("}");
/*  355 */       p();
/*      */     }
/*      */ 
/*  358 */     unindent();
/*  359 */     p("}");
/*  360 */     p();
/*      */ 
/*  362 */     p("static public final class " + bufferClassName + " implements org.apache.activemq.protobuf.MessageBuffer<" + className + "." + beanClassName + ", " + className + "." + bufferClassName + ">, " + className + " {");
/*  363 */     p();
/*  364 */     indent();
/*      */ 
/*  366 */     p("private " + beanClassName + " bean;");
/*  367 */     p("private org.apache.activemq.protobuf.Buffer buffer;");
/*  368 */     p("private int size=-1;");
/*  369 */     p("private int hashCode;");
/*  370 */     p();
/*  371 */     p("private " + bufferClassName + "(org.apache.activemq.protobuf.Buffer buffer) {");
/*  372 */     indent();
/*  373 */     p("this.buffer = buffer;");
/*  374 */     unindent();
/*  375 */     p("}");
/*  376 */     p();
/*  377 */     p("private " + bufferClassName + "(" + beanClassName + " bean) {");
/*  378 */     indent();
/*  379 */     p("this.bean = bean;");
/*  380 */     unindent();
/*  381 */     p("}");
/*  382 */     p();
/*  383 */     p("public " + beanClassName + " copy() {");
/*  384 */     indent();
/*  385 */     p("return bean().copy();");
/*  386 */     unindent();
/*  387 */     p("}");
/*  388 */     p();
/*  389 */     p("public " + bufferClassName + " freeze() {");
/*  390 */     indent();
/*  391 */     p("return this;");
/*  392 */     unindent();
/*  393 */     p("}");
/*  394 */     p();
/*  395 */     p("private " + beanClassName + " bean() {");
/*  396 */     indent();
/*  397 */     p("if (bean == null) {");
/*  398 */     indent();
/*  399 */     p("try {");
/*  400 */     indent();
/*  401 */     p("bean = new " + beanClassName + "().mergeUnframed(new org.apache.activemq.protobuf.CodedInputStream(buffer));");
/*  402 */     p("bean.frozen=this;");
/*  403 */     unindent();
/*  404 */     p("} catch (org.apache.activemq.protobuf.InvalidProtocolBufferException e) {");
/*  405 */     indent();
/*  406 */     p("throw new RuntimeException(e);");
/*  407 */     unindent();
/*  408 */     p("} catch (java.io.IOException e) {");
/*  409 */     indent();
/*  410 */     p("throw new RuntimeException(\"An IOException was thrown (should never happen in this method).\", e);");
/*  411 */     unindent();
/*  412 */     p("}");
/*  413 */     unindent();
/*  414 */     p("}");
/*  415 */     p("return bean;");
/*  416 */     unindent();
/*  417 */     p("}");
/*  418 */     p();
/*      */ 
/*  420 */     p("public String toString() {");
/*  421 */     indent();
/*  422 */     p("return bean().toString();");
/*  423 */     unindent();
/*  424 */     p("}");
/*  425 */     p();
/*  426 */     p("public java.lang.StringBuilder toString(java.lang.StringBuilder sb, String prefix) {");
/*  427 */     indent();
/*  428 */     p("return bean().toString(sb, prefix);");
/*  429 */     unindent();
/*  430 */     p("}");
/*  431 */     p();
/*      */ 
/*  433 */     for (FieldDescriptor field : m.getFields().values()) {
/*  434 */       generateBufferGetters(field);
/*      */     }
/*      */ 
/*  437 */     generateMethodWrite(m);
/*      */ 
/*  439 */     generateMethodSerializedSize(m);
/*      */ 
/*  441 */     generateMethodParseFrom(m, bufferClassName, beanClassName);
/*      */ 
/*  443 */     generateBufferEquals(m, bufferClassName);
/*      */ 
/*  445 */     p("public boolean frozen() {");
/*  446 */     indent();
/*  447 */     p("return true;");
/*  448 */     unindent();
/*  449 */     p("}");
/*      */ 
/*  451 */     for (EnumFieldDescriptor enumFeild : m.getAssociatedEnumFieldDescriptors()) {
/*  452 */       String enumName = uCamel(enumFeild.getParent().getName());
/*  453 */       p("public " + enumName + " to" + enumName + "() {");
/*  454 */       indent();
/*  455 */       p("return " + enumName + "." + enumFeild.getName() + ";");
/*  456 */       unindent();
/*  457 */       p("}");
/*  458 */       p();
/*      */     }
/*      */ 
/*  461 */     unindent();
/*  462 */     p("}");
/*  463 */     p();
/*      */ 
/*  470 */     unindent();
/*  471 */     p("}");
/*  472 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodFreeze(MessageDescriptor m, String bufferClassName)
/*      */   {
/*  478 */     p("public boolean frozen() {");
/*  479 */     indent();
/*  480 */     p("return frozen!=null;");
/*  481 */     unindent();
/*  482 */     p("}");
/*  483 */     p();
/*  484 */     p("public " + bufferClassName + " freeze() {");
/*  485 */     indent();
/*  486 */     p("if( frozen==null ) {");
/*  487 */     indent();
/*  488 */     p("frozen = new " + bufferClassName + "(bean);");
/*  489 */     p("assert deepFreeze();");
/*  490 */     unindent();
/*  491 */     p("}");
/*  492 */     p("return frozen;");
/*  493 */     unindent();
/*  494 */     p("}");
/*  495 */     p();
/*  496 */     p("private boolean deepFreeze() {");
/*  497 */     indent();
/*  498 */     p("frozen.serializedSizeUnframed();");
/*  499 */     p("return true;");
/*  500 */     unindent();
/*  501 */     p("}");
/*  502 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodCopyFromBean(MessageDescriptor m, String className)
/*      */   {
/*  511 */     p("private void copy(" + className + " other) {");
/*  512 */     indent();
/*  513 */     p("this.bean = this;");
/*  514 */     for (FieldDescriptor field : m.getFields().values()) {
/*  515 */       String lname = lCamel(field.getName());
/*  516 */       String type = field.getRule() == FieldDescriptor.REPEATED_RULE ? javaCollectionType(field) : javaType(field);
/*  517 */       boolean primitive = (field.getTypeDescriptor() == null) || (field.getTypeDescriptor().isEnum());
/*  518 */       if (field.isRepeated()) {
/*  519 */         if (primitive) {
/*  520 */           p("this.f_" + lname + " = other.f_" + lname + ";");
/*  521 */           p("if( this.f_" + lname + " !=null && !other.frozen()) {");
/*  522 */           indent();
/*  523 */           p("this.f_" + lname + " = new java.util.ArrayList<" + type + ">(this.f_" + lname + ");");
/*  524 */           unindent();
/*  525 */           p("}");
/*      */         } else {
/*  527 */           p("this.f_" + lname + " = other.f_" + lname + ";");
/*  528 */           p("if( this.f_" + lname + " !=null) {");
/*  529 */           indent();
/*  530 */           p("this.f_" + lname + " = new java.util.ArrayList<" + type + ">(other.f_" + lname + ".size());");
/*  531 */           p("for( " + type + " e :  other.f_" + lname + ") {");
/*  532 */           indent();
/*  533 */           p("this.f_" + lname + ".add(e.copy());");
/*  534 */           unindent();
/*  535 */           p("}");
/*  536 */           unindent();
/*  537 */           p("}");
/*      */         }
/*      */       }
/*  540 */       else if (primitive) {
/*  541 */         p("this.f_" + lname + " = other.f_" + lname + ";");
/*  542 */         p("this.b_" + lname + " = other.b_" + lname + ";");
/*      */       } else {
/*  544 */         p("this.f_" + lname + " = other.f_" + lname + ";");
/*  545 */         p("if( this.f_" + lname + " !=null ) {");
/*  546 */         indent();
/*  547 */         p("this.f_" + lname + " = this.f_" + lname + ".copy();");
/*  548 */         unindent();
/*  549 */         p("}");
/*      */       }
/*      */     }
/*      */ 
/*  553 */     unindent();
/*  554 */     p("}");
/*  555 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodVisitor(MessageDescriptor m)
/*      */   {
/*  580 */     String javaVisitor = getOption(m.getOptions(), "java_visitor", null);
/*  581 */     if (javaVisitor != null) {
/*  582 */       String returns = "void";
/*  583 */       String throwsException = null;
/*      */ 
/*  585 */       StringTokenizer st = new StringTokenizer(javaVisitor, ":");
/*  586 */       String vistorClass = st.nextToken();
/*  587 */       if (st.hasMoreTokens()) {
/*  588 */         returns = st.nextToken();
/*      */       }
/*  590 */       if (st.hasMoreTokens()) {
/*  591 */         throwsException = st.nextToken();
/*      */       }
/*      */ 
/*  594 */       String throwsClause = "";
/*  595 */       if (throwsException != null) {
/*  596 */         throwsClause = "throws " + throwsException + " ";
/*      */       }
/*      */ 
/*  599 */       p("public " + returns + " visit(" + vistorClass + " visitor) " + throwsClause + "{");
/*  600 */       indent();
/*  601 */       if ("void".equals(returns))
/*  602 */         p("visitor.visit(this);");
/*      */       else {
/*  604 */         p("return visitor.visit(this);");
/*      */       }
/*  606 */       unindent();
/*  607 */       p("}");
/*  608 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMethodType(MessageDescriptor m, String className) {
/*  613 */     String typeEnum = getOption(m.getOptions(), "java_type_method", null);
/*  614 */     if (typeEnum != null)
/*      */     {
/*  616 */       TypeDescriptor typeDescriptor = m.getType(typeEnum);
/*  617 */       if (typeDescriptor == null) {
/*  618 */         typeDescriptor = m.getProtoDescriptor().getType(typeEnum);
/*      */       }
/*  620 */       if ((typeDescriptor == null) || (!typeDescriptor.isEnum())) {
/*  621 */         this.errors.add("The java_type_method option on the " + m.getName() + " message does not point to valid enum type");
/*  622 */         return;
/*      */       }
/*      */ 
/*  626 */       String constant = constantCase(className);
/*  627 */       EnumDescriptor enumDescriptor = (EnumDescriptor)typeDescriptor;
/*  628 */       if (enumDescriptor.getFields().get(constant) == null) {
/*  629 */         this.errors.add("The java_type_method option on the " + m.getName() + " message does not points to the " + typeEnum + " enum but it does not have an entry for " + constant);
/*      */       }
/*      */ 
/*  632 */       String type = javaType(typeDescriptor);
/*      */ 
/*  634 */       p("public " + type + " type() {");
/*  635 */       indent();
/*  636 */       p("return " + type + "." + constant + ";");
/*  637 */       unindent();
/*  638 */       p("}");
/*  639 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMethodParseFrom(MessageDescriptor m, String bufferClassName, String beanClassName) {
/*  644 */     p("public static " + beanClassName + " parseUnframed(org.apache.activemq.protobuf.CodedInputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  645 */     indent();
/*  646 */     p("return new " + beanClassName + "().mergeUnframed(data);");
/*  647 */     unindent();
/*  648 */     p("}");
/*  649 */     p();
/*  650 */     p("public static " + beanClassName + " parseUnframed(java.io.InputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  651 */     indent();
/*  652 */     p("return parseUnframed(new org.apache.activemq.protobuf.CodedInputStream(data));");
/*  653 */     unindent();
/*  654 */     p("}");
/*  655 */     p();
/*  656 */     p("public static " + bufferClassName + " parseUnframed(org.apache.activemq.protobuf.Buffer data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  657 */     indent();
/*  658 */     p("return new " + bufferClassName + "(data);");
/*  659 */     unindent();
/*  660 */     p("}");
/*  661 */     p();
/*  662 */     p("public static " + bufferClassName + " parseUnframed(byte[] data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  663 */     indent();
/*  664 */     p("return parseUnframed(new org.apache.activemq.protobuf.Buffer(data));");
/*  665 */     unindent();
/*  666 */     p("}");
/*  667 */     p();
/*  668 */     p("public static " + bufferClassName + " parseFramed(org.apache.activemq.protobuf.CodedInputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  669 */     indent();
/*  670 */     p("int length = data.readRawVarint32();");
/*  671 */     p("int oldLimit = data.pushLimit(length);");
/*  672 */     p("" + bufferClassName + " rc = parseUnframed(data.readRawBytes(length));");
/*  673 */     p("data.popLimit(oldLimit);");
/*  674 */     p("return rc;");
/*  675 */     unindent();
/*  676 */     p("}");
/*  677 */     p();
/*  678 */     p("public static " + bufferClassName + " parseFramed(org.apache.activemq.protobuf.Buffer data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  679 */     indent();
/*  680 */     p("try {");
/*  681 */     indent();
/*  682 */     p("org.apache.activemq.protobuf.CodedInputStream input = new org.apache.activemq.protobuf.CodedInputStream(data);");
/*  683 */     p("" + bufferClassName + " rc = parseFramed(input);");
/*  684 */     p("input.checkLastTagWas(0);");
/*  685 */     p("return rc;");
/*  686 */     unindent();
/*  687 */     p("} catch (org.apache.activemq.protobuf.InvalidProtocolBufferException e) {");
/*  688 */     indent();
/*  689 */     p("throw e;");
/*  690 */     unindent();
/*  691 */     p("} catch (java.io.IOException e) {");
/*  692 */     indent();
/*  693 */     p("throw new RuntimeException(\"An IOException was thrown (should never happen in this method).\", e);");
/*  694 */     unindent();
/*  695 */     p("}");
/*  696 */     unindent();
/*  697 */     p("}");
/*  698 */     p();
/*  699 */     p("public static " + bufferClassName + " parseFramed(byte[] data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  700 */     indent();
/*  701 */     p("return parseFramed(new org.apache.activemq.protobuf.Buffer(data));");
/*  702 */     unindent();
/*  703 */     p("}");
/*  704 */     p();
/*  705 */     p("public static " + bufferClassName + " parseFramed(java.io.InputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  706 */     indent();
/*  707 */     p("return parseUnframed(org.apache.activemq.protobuf.MessageBufferSupport.readFrame(data));");
/*  708 */     unindent();
/*  709 */     p("}");
/*  710 */     p();
/*      */   }
/*      */ 
/*      */   private void generateBeanEquals(MessageDescriptor m, String className)
/*      */   {
/*  715 */     p("public boolean equals(Object obj) {");
/*  716 */     indent();
/*  717 */     p("if( obj==this )");
/*  718 */     p("   return true;");
/*  719 */     p("");
/*  720 */     p("if( obj==null || obj.getClass()!=" + className + ".class )");
/*  721 */     p("   return false;");
/*  722 */     p("");
/*  723 */     p("return equals((" + className + ")obj);");
/*  724 */     unindent();
/*  725 */     p("}");
/*  726 */     p("");
/*      */ 
/*  728 */     p("public boolean equals(" + className + " obj) {");
/*  729 */     indent();
/*  730 */     for (FieldDescriptor field : m.getFields().values()) {
/*  731 */       String uname = uCamel(field.getName());
/*  732 */       String getterMethod = "get" + uname + "()";
/*  733 */       String hasMethod = "has" + uname + "()";
/*      */ 
/*  735 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  736 */         getterMethod = "get" + uname + "List()";
/*      */       }
/*      */ 
/*  739 */       p("if (" + hasMethod + " ^ obj." + hasMethod + " ) ");
/*  740 */       p("   return false;");
/*      */ 
/*  744 */       if ((field.getRule() != FieldDescriptor.REPEATED_RULE) && ((field.isNumberType()) || (field.getType() == FieldDescriptor.BOOL_TYPE)))
/*  745 */         p("if (" + hasMethod + " && ( " + getterMethod + "!=obj." + getterMethod + " ))");
/*      */       else {
/*  747 */         p("if (" + hasMethod + " && ( !" + getterMethod + ".equals(obj." + getterMethod + ") ))");
/*      */       }
/*  749 */       p("   return false;");
/*      */     }
/*  751 */     p("return true;");
/*  752 */     unindent();
/*  753 */     p("}");
/*  754 */     p("");
/*  755 */     p("public int hashCode() {");
/*  756 */     indent();
/*  757 */     int hc = className.hashCode();
/*  758 */     p("int rc=" + hc + ";");
/*  759 */     int counter = 0;
/*  760 */     for (FieldDescriptor field : m.getFields().values()) {
/*  761 */       counter++;
/*      */ 
/*  763 */       String uname = uCamel(field.getName());
/*  764 */       String getterMethod = "get" + uname + "()";
/*  765 */       String hasMethod = "has" + uname + "()";
/*      */ 
/*  767 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  768 */         getterMethod = "get" + uname + "List()";
/*      */       }
/*      */ 
/*  771 */       p("if (" + hasMethod + ") {");
/*  772 */       indent();
/*      */ 
/*  774 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE)
/*  775 */         p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + ".hashCode() );");
/*  776 */       else if (field.isInteger32Type())
/*  777 */         p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + " );");
/*  778 */       else if (field.isInteger64Type())
/*  779 */         p("rc ^= ( " + uname.hashCode() + "^(new Long(" + getterMethod + ")).hashCode() );");
/*  780 */       else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/*  781 */         p("rc ^= ( " + uname.hashCode() + "^(new Double(" + getterMethod + ")).hashCode() );");
/*  782 */       else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/*  783 */         p("rc ^= ( " + uname.hashCode() + "^(new Double(" + getterMethod + ")).hashCode() );");
/*  784 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/*  785 */         p("rc ^= ( " + uname.hashCode() + "^ (" + getterMethod + "? " + counter + ":-" + counter + ") );");
/*      */       else {
/*  787 */         p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + ".hashCode() );");
/*      */       }
/*  789 */       unindent();
/*  790 */       p("}");
/*      */     }
/*  792 */     p("return rc;");
/*  793 */     unindent();
/*  794 */     p("}");
/*  795 */     p("");
/*      */   }
/*      */ 
/*      */   private void generateBufferEquals(MessageDescriptor m, String className) {
/*  799 */     p("public boolean equals(Object obj) {");
/*  800 */     indent();
/*  801 */     p("if( obj==this )");
/*  802 */     p("   return true;");
/*  803 */     p("");
/*  804 */     p("if( obj==null || obj.getClass()!=" + className + ".class )");
/*  805 */     p("   return false;");
/*  806 */     p("");
/*  807 */     p("return equals((" + className + ")obj);");
/*  808 */     unindent();
/*  809 */     p("}");
/*  810 */     p("");
/*      */ 
/*  812 */     p("public boolean equals(" + className + " obj) {");
/*  813 */     indent();
/*  814 */     p("return toUnframedBuffer().equals(obj.toUnframedBuffer());");
/*  815 */     unindent();
/*  816 */     p("}");
/*  817 */     p("");
/*  818 */     p("public int hashCode() {");
/*  819 */     indent();
/*  820 */     int hc = className.hashCode();
/*  821 */     p("if( hashCode==0 ) {");
/*  822 */     p("hashCode=" + hc + " ^ toUnframedBuffer().hashCode();");
/*  823 */     p("}");
/*  824 */     p("return hashCode;");
/*  825 */     unindent();
/*  826 */     p("}");
/*  827 */     p("");
/*      */   }
/*      */ 
/*      */   private void generateMethodSerializedSize(MessageDescriptor m)
/*      */   {
/*  835 */     p("public int serializedSizeFramed() {");
/*  836 */     indent();
/*  837 */     p("int t = serializedSizeUnframed();");
/*  838 */     p("return org.apache.activemq.protobuf.CodedOutputStream.computeRawVarint32Size(t) + t;");
/*  839 */     unindent();
/*  840 */     p("}");
/*  841 */     p();
/*  842 */     p("public int serializedSizeUnframed() {");
/*  843 */     indent();
/*  844 */     p("if (buffer != null) {");
/*  845 */     indent();
/*  846 */     p("return buffer.length;");
/*  847 */     unindent();
/*  848 */     p("}");
/*  849 */     p("if (size != -1)");
/*  850 */     p("   return size;");
/*  851 */     p();
/*  852 */     p("size = 0;");
/*  853 */     for (FieldDescriptor field : m.getFields().values())
/*      */     {
/*  855 */       String uname = uCamel(field.getName());
/*  856 */       String getter = "get" + uname + "()";
/*  857 */       String type = javaType(field);
/*      */ 
/*  859 */       if (!field.isRequired()) {
/*  860 */         p("if (has" + uname + "()) {");
/*  861 */         indent();
/*      */       }
/*      */ 
/*  864 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  865 */         p("for (" + type + " i : get" + uname + "List()) {");
/*  866 */         indent();
/*  867 */         getter = "i";
/*      */       }
/*      */ 
/*  870 */       if (field.getType() == FieldDescriptor.STRING_TYPE)
/*  871 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeStringSize(" + field.getTag() + ", " + getter + ");");
/*  872 */       else if (field.getType() == FieldDescriptor.BYTES_TYPE)
/*  873 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeBytesSize(" + field.getTag() + ", " + getter + ");");
/*  874 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/*  875 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeBoolSize(" + field.getTag() + ", " + getter + ");");
/*  876 */       else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/*  877 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeDoubleSize(" + field.getTag() + ", " + getter + ");");
/*  878 */       else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/*  879 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFloatSize(" + field.getTag() + ", " + getter + ");");
/*  880 */       else if (field.getType() == FieldDescriptor.INT32_TYPE)
/*  881 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeInt32Size(" + field.getTag() + ", " + getter + ");");
/*  882 */       else if (field.getType() == FieldDescriptor.INT64_TYPE)
/*  883 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeInt64Size(" + field.getTag() + ", " + getter + ");");
/*  884 */       else if (field.getType() == FieldDescriptor.SINT32_TYPE)
/*  885 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSInt32Size(" + field.getTag() + ", " + getter + ");");
/*  886 */       else if (field.getType() == FieldDescriptor.SINT64_TYPE)
/*  887 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSInt64Size(" + field.getTag() + ", " + getter + ");");
/*  888 */       else if (field.getType() == FieldDescriptor.UINT32_TYPE)
/*  889 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeUInt32Size(" + field.getTag() + ", " + getter + ");");
/*  890 */       else if (field.getType() == FieldDescriptor.UINT64_TYPE)
/*  891 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeUInt64Size(" + field.getTag() + ", " + getter + ");");
/*  892 */       else if (field.getType() == FieldDescriptor.FIXED32_TYPE)
/*  893 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFixed32Size(" + field.getTag() + ", " + getter + ");");
/*  894 */       else if (field.getType() == FieldDescriptor.FIXED64_TYPE)
/*  895 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFixed64Size(" + field.getTag() + ", " + getter + ");");
/*  896 */       else if (field.getType() == FieldDescriptor.SFIXED32_TYPE)
/*  897 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSFixed32Size(" + field.getTag() + ", " + getter + ");");
/*  898 */       else if (field.getType() == FieldDescriptor.SFIXED64_TYPE)
/*  899 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSFixed64Size(" + field.getTag() + ", " + getter + ");");
/*  900 */       else if (field.getTypeDescriptor().isEnum())
/*  901 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeEnumSize(" + field.getTag() + ", " + getter + ".getNumber());");
/*  902 */       else if (field.getGroup() != null) {
/*  903 */         this.errors.add("This code generator does not support group fields.");
/*      */       }
/*      */       else {
/*  906 */         p("size += org.apache.activemq.protobuf.MessageBufferSupport.computeMessageSize(" + field.getTag() + ", " + getter + ".freeze());");
/*      */       }
/*  908 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  909 */         unindent();
/*  910 */         p("}");
/*      */       }
/*      */ 
/*  913 */       if (!field.isRequired()) {
/*  914 */         unindent();
/*  915 */         p("}");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  921 */     p("return size;");
/*  922 */     unindent();
/*  923 */     p("}");
/*  924 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodWrite(MessageDescriptor m)
/*      */   {
/*  932 */     p("public org.apache.activemq.protobuf.Buffer toUnframedBuffer() {");
/*  933 */     indent();
/*  934 */     p("if( buffer !=null ) {");
/*  935 */     indent();
/*  936 */     p("return buffer;");
/*  937 */     unindent();
/*  938 */     p("}");
/*  939 */     p("return org.apache.activemq.protobuf.MessageBufferSupport.toUnframedBuffer(this);");
/*  940 */     unindent();
/*  941 */     p("}");
/*  942 */     p();
/*  943 */     p("public org.apache.activemq.protobuf.Buffer toFramedBuffer() {");
/*  944 */     indent();
/*  945 */     p("return org.apache.activemq.protobuf.MessageBufferSupport.toFramedBuffer(this);");
/*  946 */     unindent();
/*  947 */     p("}");
/*  948 */     p();
/*  949 */     p("public byte[] toUnframedByteArray() {");
/*  950 */     indent();
/*  951 */     p("return toUnframedBuffer().toByteArray();");
/*  952 */     unindent();
/*  953 */     p("}");
/*  954 */     p();
/*  955 */     p("public byte[] toFramedByteArray() {");
/*  956 */     indent();
/*  957 */     p("return toFramedBuffer().toByteArray();");
/*  958 */     unindent();
/*  959 */     p("}");
/*  960 */     p();
/*  961 */     p("public void writeFramed(org.apache.activemq.protobuf.CodedOutputStream output) throws java.io.IOException {");
/*  962 */     indent();
/*  963 */     p("output.writeRawVarint32(serializedSizeUnframed());");
/*  964 */     p("writeUnframed(output);");
/*  965 */     unindent();
/*  966 */     p("}");
/*  967 */     p();
/*  968 */     p("public void writeFramed(java.io.OutputStream output) throws java.io.IOException {");
/*  969 */     indent();
/*  970 */     p("org.apache.activemq.protobuf.CodedOutputStream codedOutput = new org.apache.activemq.protobuf.CodedOutputStream(output);");
/*  971 */     p("writeFramed(codedOutput);");
/*  972 */     p("codedOutput.flush();");
/*  973 */     unindent();
/*  974 */     p("}");
/*  975 */     p();
/*      */ 
/*  977 */     p("public void writeUnframed(java.io.OutputStream output) throws java.io.IOException {");
/*  978 */     indent();
/*  979 */     p("org.apache.activemq.protobuf.CodedOutputStream codedOutput = new org.apache.activemq.protobuf.CodedOutputStream(output);");
/*  980 */     p("writeUnframed(codedOutput);");
/*  981 */     p("codedOutput.flush();");
/*  982 */     unindent();
/*  983 */     p("}");
/*  984 */     p();
/*      */ 
/*  986 */     p("public void writeUnframed(org.apache.activemq.protobuf.CodedOutputStream output) throws java.io.IOException {");
/*  987 */     indent();
/*      */ 
/*  989 */     p("if (buffer == null) {");
/*  990 */     indent();
/*  991 */     p("int size = serializedSizeUnframed();");
/*  992 */     p("buffer = output.getNextBuffer(size);");
/*  993 */     p("org.apache.activemq.protobuf.CodedOutputStream original=null;");
/*  994 */     p("if( buffer == null ) {");
/*  995 */     indent();
/*  996 */     p("buffer = new org.apache.activemq.protobuf.Buffer(new byte[size]);");
/*  997 */     p("original = output;");
/*  998 */     p("output = new org.apache.activemq.protobuf.CodedOutputStream(buffer);");
/*  999 */     unindent();
/* 1000 */     p("}");
/*      */ 
/* 1002 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1003 */       String uname = uCamel(field.getName());
/* 1004 */       String getter = "bean.get" + uname + "()";
/* 1005 */       String type = javaType(field);
/*      */ 
/* 1007 */       if (!field.isRequired()) {
/* 1008 */         p("if (bean.has" + uname + "()) {");
/* 1009 */         indent();
/*      */       }
/*      */ 
/* 1012 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/* 1013 */         p("for (" + type + " i : bean.get" + uname + "List()) {");
/* 1014 */         indent();
/* 1015 */         getter = "i";
/*      */       }
/*      */ 
/* 1018 */       if (field.getType() == FieldDescriptor.STRING_TYPE)
/* 1019 */         p("output.writeString(" + field.getTag() + ", " + getter + ");");
/* 1020 */       else if (field.getType() == FieldDescriptor.BYTES_TYPE)
/* 1021 */         p("output.writeBytes(" + field.getTag() + ", " + getter + ");");
/* 1022 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/* 1023 */         p("output.writeBool(" + field.getTag() + ", " + getter + ");");
/* 1024 */       else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/* 1025 */         p("output.writeDouble(" + field.getTag() + ", " + getter + ");");
/* 1026 */       else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/* 1027 */         p("output.writeFloat(" + field.getTag() + ", " + getter + ");");
/* 1028 */       else if (field.getType() == FieldDescriptor.INT32_TYPE)
/* 1029 */         p("output.writeInt32(" + field.getTag() + ", " + getter + ");");
/* 1030 */       else if (field.getType() == FieldDescriptor.INT64_TYPE)
/* 1031 */         p("output.writeInt64(" + field.getTag() + ", " + getter + ");");
/* 1032 */       else if (field.getType() == FieldDescriptor.SINT32_TYPE)
/* 1033 */         p("output.writeSInt32(" + field.getTag() + ", " + getter + ");");
/* 1034 */       else if (field.getType() == FieldDescriptor.SINT64_TYPE)
/* 1035 */         p("output.writeSInt64(" + field.getTag() + ", " + getter + ");");
/* 1036 */       else if (field.getType() == FieldDescriptor.UINT32_TYPE)
/* 1037 */         p("output.writeUInt32(" + field.getTag() + ", " + getter + ");");
/* 1038 */       else if (field.getType() == FieldDescriptor.UINT64_TYPE)
/* 1039 */         p("output.writeUInt64(" + field.getTag() + ", " + getter + ");");
/* 1040 */       else if (field.getType() == FieldDescriptor.FIXED32_TYPE)
/* 1041 */         p("output.writeFixed32(" + field.getTag() + ", " + getter + ");");
/* 1042 */       else if (field.getType() == FieldDescriptor.FIXED64_TYPE)
/* 1043 */         p("output.writeFixed64(" + field.getTag() + ", " + getter + ");");
/* 1044 */       else if (field.getType() == FieldDescriptor.SFIXED32_TYPE)
/* 1045 */         p("output.writeSFixed32(" + field.getTag() + ", " + getter + ");");
/* 1046 */       else if (field.getType() == FieldDescriptor.SFIXED64_TYPE)
/* 1047 */         p("output.writeSFixed64(" + field.getTag() + ", " + getter + ");");
/* 1048 */       else if (field.getTypeDescriptor().isEnum())
/* 1049 */         p("output.writeEnum(" + field.getTag() + ", " + getter + ".getNumber());");
/* 1050 */       else if (field.getGroup() != null) {
/* 1051 */         this.errors.add("This code generator does not support group fields.");
/*      */       }
/*      */       else {
/* 1054 */         p("org.apache.activemq.protobuf.MessageBufferSupport.writeMessage(output, " + field.getTag() + ", " + getter + ".freeze());");
/*      */       }
/*      */ 
/* 1057 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/* 1058 */         unindent();
/* 1059 */         p("}");
/*      */       }
/*      */ 
/* 1062 */       if (!field.isRequired()) {
/* 1063 */         unindent();
/* 1064 */         p("}");
/*      */       }
/*      */     }
/*      */ 
/* 1068 */     p("if( original !=null ) {");
/* 1069 */     indent();
/* 1070 */     p("output.checkNoSpaceLeft();");
/* 1071 */     p("output = original;");
/* 1072 */     p("output.writeRawBytes(buffer);");
/* 1073 */     unindent();
/* 1074 */     p("}");
/* 1075 */     unindent();
/* 1076 */     p("} else {");
/* 1077 */     indent();
/* 1078 */     p("output.writeRawBytes(buffer);");
/* 1079 */     unindent();
/* 1080 */     p("}");
/*      */ 
/* 1082 */     unindent();
/* 1083 */     p("}");
/* 1084 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodMergeFromStream(MessageDescriptor m, String className)
/*      */   {
/* 1092 */     p("public " + className + " mergeUnframed(java.io.InputStream input) throws java.io.IOException {");
/* 1093 */     indent();
/* 1094 */     p("return mergeUnframed(new org.apache.activemq.protobuf.CodedInputStream(input));");
/* 1095 */     unindent();
/* 1096 */     p("}");
/* 1097 */     p();
/* 1098 */     p("public " + className + " mergeUnframed(org.apache.activemq.protobuf.CodedInputStream input) throws java.io.IOException {");
/* 1099 */     indent();
/*      */ 
/* 1101 */     p("copyCheck();");
/* 1102 */     p("while (true) {");
/* 1103 */     indent();
/*      */ 
/* 1105 */     p("int tag = input.readTag();");
/* 1106 */     p("if ((tag & 0x07) == 4) {");
/* 1107 */     p("   return this;");
/* 1108 */     p("}");
/*      */ 
/* 1110 */     p("switch (tag) {");
/* 1111 */     p("case 0:");
/* 1112 */     p("   return this;");
/* 1113 */     p("default: {");
/*      */ 
/* 1115 */     p("   break;");
/* 1116 */     p("}");
/*      */ 
/* 1118 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1119 */       String uname = uCamel(field.getName());
/* 1120 */       String setter = "set" + uname;
/* 1121 */       boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/* 1122 */       if (repeated) {
/* 1123 */         setter = "create" + uname + "List().add";
/*      */       }
/* 1125 */       if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1126 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/*      */ 
/* 1129 */         indent();
/* 1130 */         p(setter + "(input.readString());");
/* 1131 */       } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1132 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/* 1133 */         indent();
/* 1134 */         String override = getOption(field.getOptions(), "java_override_type", null);
/* 1135 */         if ("AsciiBuffer".equals(override))
/* 1136 */           p(setter + "(new org.apache.activemq.protobuf.AsciiBuffer(input.readBytes()));");
/* 1137 */         else if ("UTF8Buffer".equals(override))
/* 1138 */           p(setter + "(new org.apache.activemq.protobuf.UTF8Buffer(input.readBytes()));");
/*      */         else
/* 1140 */           p(setter + "(input.readBytes());");
/*      */       }
/* 1142 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1143 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/* 1144 */         indent();
/* 1145 */         p(setter + "(input.readBool());");
/* 1146 */       } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1147 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/* 1149 */         indent();
/* 1150 */         p(setter + "(input.readDouble());");
/* 1151 */       } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1152 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/* 1154 */         indent();
/* 1155 */         p(setter + "(input.readFloat());");
/* 1156 */       } else if (field.getType() == FieldDescriptor.INT32_TYPE) {
/* 1157 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1159 */         indent();
/* 1160 */         p(setter + "(input.readInt32());");
/* 1161 */       } else if (field.getType() == FieldDescriptor.INT64_TYPE) {
/* 1162 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1164 */         indent();
/* 1165 */         p(setter + "(input.readInt64());");
/* 1166 */       } else if (field.getType() == FieldDescriptor.SINT32_TYPE) {
/* 1167 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1169 */         indent();
/* 1170 */         p(setter + "(input.readSInt32());");
/* 1171 */       } else if (field.getType() == FieldDescriptor.SINT64_TYPE) {
/* 1172 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1174 */         indent();
/* 1175 */         p(setter + "(input.readSInt64());");
/* 1176 */       } else if (field.getType() == FieldDescriptor.UINT32_TYPE) {
/* 1177 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1179 */         indent();
/* 1180 */         p(setter + "(input.readUInt32());");
/* 1181 */       } else if (field.getType() == FieldDescriptor.UINT64_TYPE) {
/* 1182 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1184 */         indent();
/* 1185 */         p(setter + "(input.readUInt64());");
/* 1186 */       } else if (field.getType() == FieldDescriptor.FIXED32_TYPE) {
/* 1187 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/* 1189 */         indent();
/* 1190 */         p(setter + "(input.readFixed32());");
/* 1191 */       } else if (field.getType() == FieldDescriptor.FIXED64_TYPE) {
/* 1192 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/* 1194 */         indent();
/* 1195 */         p(setter + "(input.readFixed64());");
/* 1196 */       } else if (field.getType() == FieldDescriptor.SFIXED32_TYPE) {
/* 1197 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/* 1199 */         indent();
/* 1200 */         p(setter + "(input.readSFixed32());");
/* 1201 */       } else if (field.getType() == FieldDescriptor.SFIXED64_TYPE) {
/* 1202 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/* 1204 */         indent();
/* 1205 */         p(setter + "(input.readSFixed64());");
/* 1206 */       } else if (field.getTypeDescriptor().isEnum()) {
/* 1207 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/* 1209 */         indent();
/* 1210 */         String type = javaType(field);
/* 1211 */         p("{");
/* 1212 */         indent();
/* 1213 */         p("int t = input.readEnum();");
/* 1214 */         p("" + type + " value = " + type + ".valueOf(t);");
/* 1215 */         p("if( value !=null ) {");
/* 1216 */         indent();
/* 1217 */         p(setter + "(value);");
/* 1218 */         unindent();
/* 1219 */         p("}");
/*      */ 
/* 1222 */         unindent();
/* 1223 */         p("}");
/*      */       }
/* 1225 */       else if (field.getGroup() != null) {
/* 1226 */         this.errors.add("This code generator does not support group fields.");
/*      */       }
/*      */       else
/*      */       {
/* 1247 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/* 1248 */         indent();
/* 1249 */         String type = javaType(field);
/* 1250 */         if (repeated) {
/* 1251 */           p(setter + "(" + javaRelatedType(type, "Buffer") + ".parseFramed(input));");
/*      */         } else {
/* 1253 */           p("if (has" + uname + "()) {");
/* 1254 */           indent();
/* 1255 */           p("set" + uname + "(get" + uname + "().copy().mergeFrom(" + javaRelatedType(type, "Buffer") + ".parseFramed(input)));");
/* 1256 */           unindent();
/* 1257 */           p("} else {");
/* 1258 */           indent();
/* 1259 */           p(setter + "(" + javaRelatedType(type, "Buffer") + ".parseFramed(input));");
/* 1260 */           unindent();
/* 1261 */           p("}");
/*      */         }
/*      */       }
/* 1264 */       p("break;");
/* 1265 */       unindent();
/*      */     }
/* 1267 */     p("}");
/*      */ 
/* 1269 */     unindent();
/* 1270 */     p("}");
/*      */ 
/* 1272 */     unindent();
/* 1273 */     p("}");
/*      */   }
/*      */ 
/*      */   private void generateMethodMergeFromBean(MessageDescriptor m, String className)
/*      */   {
/* 1281 */     p("public " + className + "Bean mergeFrom(" + className + " other) {");
/* 1282 */     indent();
/* 1283 */     p("copyCheck();");
/* 1284 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1285 */       String uname = uCamel(field.getName());
/* 1286 */       p("if (other.has" + uname + "()) {");
/* 1287 */       indent();
/*      */ 
/* 1289 */       if ((field.isScalarType()) || (field.getTypeDescriptor().isEnum())) {
/* 1290 */         if (field.isRepeated())
/* 1291 */           p("get" + uname + "List().addAll(other.get" + uname + "List());");
/*      */         else
/* 1293 */           p("set" + uname + "(other.get" + uname + "());");
/*      */       }
/*      */       else
/*      */       {
/* 1297 */         String type = javaType(field);
/*      */ 
/* 1299 */         if (field.isRepeated()) {
/* 1300 */           p("for(" + type + " element: other.get" + uname + "List() ) {");
/* 1301 */           indent();
/* 1302 */           p("get" + uname + "List().add(element.copy());");
/* 1303 */           unindent();
/* 1304 */           p("}");
/*      */         } else {
/* 1306 */           p("if (has" + uname + "()) {");
/* 1307 */           indent();
/* 1308 */           p("set" + uname + "(get" + uname + "().copy().mergeFrom(other.get" + uname + "()));");
/* 1309 */           unindent();
/* 1310 */           p("} else {");
/* 1311 */           indent();
/* 1312 */           p("set" + uname + "(other.get" + uname + "().copy());");
/* 1313 */           unindent();
/* 1314 */           p("}");
/*      */         }
/*      */       }
/* 1317 */       unindent();
/* 1318 */       p("}");
/*      */     }
/* 1320 */     p("return this;");
/* 1321 */     unindent();
/* 1322 */     p("}");
/* 1323 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodClear(MessageDescriptor m)
/*      */   {
/* 1330 */     p("public void clear() {");
/* 1331 */     indent();
/* 1332 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1333 */       String uname = uCamel(field.getName());
/* 1334 */       p("clear" + uname + "();");
/*      */     }
/* 1336 */     unindent();
/* 1337 */     p("}");
/* 1338 */     p();
/*      */   }
/*      */ 
/*      */   private void generateReadWriteExternal(MessageDescriptor m)
/*      */   {
/* 1343 */     p("public void readExternal(java.io.DataInput in) throws java.io.IOException {");
/* 1344 */     indent();
/* 1345 */     p("assert frozen==null : org.apache.activemq.protobuf.MessageBufferSupport.FORZEN_ERROR_MESSAGE;");
/* 1346 */     p("bean = this;");
/* 1347 */     p("frozen = null;");
/*      */ 
/* 1349 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1350 */       String lname = lCamel(field.getName());
/* 1351 */       String type = javaType(field);
/* 1352 */       boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1355 */       if (repeated) {
/* 1356 */         p("{");
/* 1357 */         indent();
/* 1358 */         p("int size = in.readShort();");
/* 1359 */         p("if( size>=0 ) {");
/* 1360 */         indent();
/* 1361 */         p("f_" + lname + " = new java.util.ArrayList<" + javaCollectionType(field) + ">(size);");
/* 1362 */         p("for(int i=0; i<size; i++) {");
/* 1363 */         indent();
/* 1364 */         if (field.isInteger32Type()) {
/* 1365 */           p("f_" + lname + ".add(in.readInt());");
/* 1366 */         } else if (field.isInteger64Type()) {
/* 1367 */           p("f_" + lname + ".add(in.readLong());");
/* 1368 */         } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1369 */           p("f_" + lname + ".add(in.readDouble());");
/* 1370 */         } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1371 */           p("f_" + lname + ".add(in.readFloat());");
/* 1372 */         } else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1373 */           p("f_" + lname + ".add(in.readBoolean());");
/* 1374 */         } else if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1375 */           p("f_" + lname + ".add(in.readUTF());");
/* 1376 */         } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1377 */           p("byte b[] = new byte[in.readInt()];");
/* 1378 */           p("in.readFully(b);");
/* 1379 */           p("f_" + lname + ".add(new " + type + "(b));");
/* 1380 */         } else if (field.getTypeDescriptor().isEnum()) {
/* 1381 */           p("f_" + lname + ".add(" + type + ".valueOf(in.readShort()));");
/*      */         } else {
/* 1383 */           p("" + javaRelatedType(type, "Bean") + " o = new " + javaRelatedType(type, "Bean") + "();");
/* 1384 */           p("o.readExternal(in);");
/* 1385 */           p("f_" + lname + ".add(o);");
/*      */         }
/* 1387 */         unindent();
/* 1388 */         p("}");
/* 1389 */         unindent();
/* 1390 */         p("} else {");
/* 1391 */         indent();
/* 1392 */         p("f_" + lname + " = null;");
/* 1393 */         unindent();
/* 1394 */         p("}");
/* 1395 */         unindent();
/* 1396 */         p("}");
/*      */       }
/* 1399 */       else if (field.isInteger32Type()) {
/* 1400 */         p("f_" + lname + " = in.readInt();");
/* 1401 */         p("b_" + lname + " = true;");
/* 1402 */       } else if (field.isInteger64Type()) {
/* 1403 */         p("f_" + lname + " = in.readLong();");
/* 1404 */         p("b_" + lname + " = true;");
/* 1405 */       } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1406 */         p("f_" + lname + " = in.readDouble();");
/* 1407 */         p("b_" + lname + " = true;");
/* 1408 */       } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1409 */         p("f_" + lname + " = in.readFloat();");
/* 1410 */         p("b_" + lname + " = true;");
/* 1411 */       } else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1412 */         p("f_" + lname + " = in.readBoolean();");
/* 1413 */         p("b_" + lname + " = true;");
/* 1414 */       } else if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1415 */         p("if( in.readBoolean() ) {");
/* 1416 */         indent();
/* 1417 */         p("f_" + lname + " = in.readUTF();");
/* 1418 */         p("b_" + lname + " = true;");
/* 1419 */         unindent();
/* 1420 */         p("} else {");
/* 1421 */         indent();
/* 1422 */         p("f_" + lname + " = null;");
/* 1423 */         p("b_" + lname + " = false;");
/* 1424 */         unindent();
/* 1425 */         p("}");
/* 1426 */       } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1427 */         p("{");
/* 1428 */         indent();
/* 1429 */         p("int size = in.readInt();");
/* 1430 */         p("if( size>=0 ) {");
/* 1431 */         indent();
/* 1432 */         p("byte b[] = new byte[size];");
/* 1433 */         p("in.readFully(b);");
/* 1434 */         p("f_" + lname + " = new " + type + "(b);");
/* 1435 */         p("b_" + lname + " = true;");
/* 1436 */         unindent();
/* 1437 */         p("} else {");
/* 1438 */         indent();
/* 1439 */         p("f_" + lname + " = null;");
/* 1440 */         p("b_" + lname + " = false;");
/* 1441 */         unindent();
/* 1442 */         p("}");
/* 1443 */         unindent();
/* 1444 */         p("}");
/* 1445 */       } else if (field.getTypeDescriptor().isEnum()) {
/* 1446 */         p("if( in.readBoolean() ) {");
/* 1447 */         indent();
/* 1448 */         p("f_" + lname + " = " + type + ".valueOf(in.readShort());");
/* 1449 */         p("b_" + lname + " = true;");
/* 1450 */         unindent();
/* 1451 */         p("} else {");
/* 1452 */         indent();
/* 1453 */         p("f_" + lname + " = null;");
/* 1454 */         p("b_" + lname + " = false;");
/* 1455 */         unindent();
/* 1456 */         p("}");
/*      */       } else {
/* 1458 */         p("if( in.readBoolean() ) {");
/* 1459 */         indent();
/* 1460 */         p("" + javaRelatedType(type, "Bean") + " o = new " + javaRelatedType(type, "Bean") + "();");
/* 1461 */         p("o.readExternal(in);");
/* 1462 */         p("f_" + lname + " = o;");
/* 1463 */         unindent();
/* 1464 */         p("} else {");
/* 1465 */         indent();
/* 1466 */         p("f_" + lname + " = null;");
/* 1467 */         unindent();
/* 1468 */         p("}");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1473 */     unindent();
/* 1474 */     p("}");
/* 1475 */     p();
/* 1476 */     p("public void writeExternal(java.io.DataOutput out) throws java.io.IOException {");
/* 1477 */     indent();
/* 1478 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1479 */       String lname = lCamel(field.getName());
/* 1480 */       boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1483 */       if (repeated) {
/* 1484 */         p("if( bean.f_" + lname + "!=null ) {");
/* 1485 */         indent();
/* 1486 */         p("out.writeShort(bean.f_" + lname + ".size());");
/* 1487 */         p("for(" + javaCollectionType(field) + " o : bean.f_" + lname + ") {");
/* 1488 */         indent();
/*      */ 
/* 1490 */         if (field.isInteger32Type()) {
/* 1491 */           p("out.writeInt(o);");
/* 1492 */         } else if (field.isInteger64Type()) {
/* 1493 */           p("out.writeLong(o);");
/* 1494 */         } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1495 */           p("out.writeDouble(o);");
/* 1496 */         } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1497 */           p("out.writeFloat(o);");
/* 1498 */         } else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1499 */           p("out.writeBoolean(o);");
/* 1500 */         } else if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1501 */           p("out.writeUTF(o);");
/* 1502 */         } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1503 */           p("out.writeInt(o.getLength());");
/* 1504 */           p("out.write(o.getData(), o.getOffset(), o.getLength());");
/* 1505 */         } else if (field.getTypeDescriptor().isEnum()) {
/* 1506 */           p("out.writeShort(o.getNumber());");
/*      */         } else {
/* 1508 */           p("o.copy().writeExternal(out);");
/*      */         }
/* 1510 */         unindent();
/* 1511 */         p("}");
/* 1512 */         unindent();
/* 1513 */         p("} else {");
/* 1514 */         indent();
/* 1515 */         p("out.writeShort(-1);");
/* 1516 */         unindent();
/* 1517 */         p("}");
/*      */       }
/* 1520 */       else if (field.isInteger32Type()) {
/* 1521 */         p("out.writeInt(bean.f_" + lname + ");");
/* 1522 */       } else if (field.isInteger64Type()) {
/* 1523 */         p("out.writeLong(bean.f_" + lname + ");");
/* 1524 */       } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1525 */         p("out.writeDouble(bean.f_" + lname + ");");
/* 1526 */       } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1527 */         p("out.writeFloat(bean.f_" + lname + ");");
/* 1528 */       } else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1529 */         p("out.writeBoolean(bean.f_" + lname + ");");
/* 1530 */       } else if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1531 */         p("if( bean.f_" + lname + "!=null ) {");
/* 1532 */         indent();
/* 1533 */         p("out.writeBoolean(true);");
/* 1534 */         p("out.writeUTF(bean.f_" + lname + ");");
/* 1535 */         unindent();
/* 1536 */         p("} else {");
/* 1537 */         indent();
/* 1538 */         p("out.writeBoolean(false);");
/* 1539 */         unindent();
/* 1540 */         p("}");
/* 1541 */       } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1542 */         p("if( bean.f_" + lname + "!=null ) {");
/* 1543 */         indent();
/* 1544 */         p("out.writeInt(bean.f_" + lname + ".getLength());");
/* 1545 */         p("out.write(bean.f_" + lname + ".getData(), bean.f_" + lname + ".getOffset(), bean.f_" + lname + ".getLength());");
/* 1546 */         unindent();
/* 1547 */         p("} else {");
/* 1548 */         indent();
/* 1549 */         p("out.writeInt(-1);");
/* 1550 */         unindent();
/* 1551 */         p("}");
/* 1552 */       } else if (field.getTypeDescriptor().isEnum()) {
/* 1553 */         p("if( bean.f_" + lname + "!=null ) {");
/* 1554 */         indent();
/* 1555 */         p("out.writeBoolean(true);");
/* 1556 */         p("out.writeShort(bean.f_" + lname + ".getNumber());");
/* 1557 */         unindent();
/* 1558 */         p("} else {");
/* 1559 */         indent();
/* 1560 */         p("out.writeBoolean(false);");
/* 1561 */         unindent();
/* 1562 */         p("}");
/*      */       } else {
/* 1564 */         p("if( bean.f_" + lname + "!=null ) {");
/* 1565 */         indent();
/* 1566 */         p("out.writeBoolean(true);");
/* 1567 */         p("bean.f_" + lname + ".copy().writeExternal(out);");
/* 1568 */         unindent();
/* 1569 */         p("} else {");
/* 1570 */         indent();
/* 1571 */         p("out.writeBoolean(false);");
/* 1572 */         unindent();
/* 1573 */         p("}");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1578 */     unindent();
/* 1579 */     p("}");
/* 1580 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodToString(MessageDescriptor m)
/*      */   {
/* 1648 */     p("public String toString() {");
/* 1649 */     indent();
/* 1650 */     p("return toString(new java.lang.StringBuilder(), \"\").toString();");
/* 1651 */     unindent();
/* 1652 */     p("}");
/* 1653 */     p();
/*      */ 
/* 1655 */     p("public java.lang.StringBuilder toString(java.lang.StringBuilder sb, String prefix) {");
/* 1656 */     indent();
/* 1657 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1658 */       String uname = uCamel(field.getName());
/* 1659 */       p("if(  has" + uname + "() ) {");
/* 1660 */       indent();
/* 1661 */       if (field.isRepeated()) {
/* 1662 */         String type = javaCollectionType(field);
/* 1663 */         p("java.util.List<" + type + "> l = get" + uname + "List();");
/* 1664 */         p("for( int i=0; i < l.size(); i++ ) {");
/* 1665 */         indent();
/* 1666 */         if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1667 */           p("sb.append(prefix+\"" + field.getName() + "[\"+i+\"] {\\n\");");
/* 1668 */           p("l.get(i).toString(sb, prefix+\"  \");");
/* 1669 */           p("sb.append(prefix+\"}\\n\");");
/*      */         } else {
/* 1671 */           p("sb.append(prefix+\"" + field.getName() + "[\"+i+\"]: \");");
/* 1672 */           p("sb.append(l.get(i));");
/* 1673 */           p("sb.append(\"\\n\");");
/*      */         }
/* 1675 */         unindent();
/* 1676 */         p("}");
/*      */       }
/* 1678 */       else if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1679 */         p("sb.append(prefix+\"" + field.getName() + " {\\n\");");
/* 1680 */         p("get" + uname + "().toString(sb, prefix+\"  \");");
/* 1681 */         p("sb.append(prefix+\"}\\n\");");
/*      */       } else {
/* 1683 */         p("sb.append(prefix+\"" + field.getName() + ": \");");
/* 1684 */         p("sb.append(get" + uname + "());");
/* 1685 */         p("sb.append(\"\\n\");");
/*      */       }
/*      */ 
/* 1688 */       unindent();
/* 1689 */       p("}");
/*      */     }
/* 1691 */     p("return sb;");
/* 1692 */     unindent();
/* 1693 */     p("}");
/* 1694 */     p();
/*      */   }
/*      */ 
/*      */   private void generateBufferGetters(FieldDescriptor field)
/*      */   {
/* 1703 */     String uname = uCamel(field.getName());
/* 1704 */     String type = field.getRule() == FieldDescriptor.REPEATED_RULE ? javaCollectionType(field) : javaType(field);
/* 1705 */     boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1708 */     p("// " + field.getRule() + " " + field.getType() + " " + field.getName() + " = " + field.getTag() + ";");
/* 1709 */     if (repeated)
/*      */     {
/* 1711 */       p("public boolean has" + uname + "() {");
/* 1712 */       indent();
/* 1713 */       p("return bean().has" + uname + "();");
/* 1714 */       unindent();
/* 1715 */       p("}");
/* 1716 */       p();
/* 1717 */       p("public java.util.List<" + type + "> get" + uname + "List() {");
/* 1718 */       indent();
/* 1719 */       p("return bean().get" + uname + "List();");
/* 1720 */       unindent();
/* 1721 */       p("}");
/* 1722 */       p();
/* 1723 */       p("public int get" + uname + "Count() {");
/* 1724 */       indent();
/* 1725 */       p("return bean().get" + uname + "Count();");
/* 1726 */       unindent();
/* 1727 */       p("}");
/* 1728 */       p();
/* 1729 */       p("public " + type + " get" + uname + "(int index) {");
/* 1730 */       indent();
/* 1731 */       p("return bean().get" + uname + "(index);");
/* 1732 */       unindent();
/* 1733 */       p("}");
/* 1734 */       p();
/*      */     }
/*      */     else {
/* 1737 */       p("public boolean has" + uname + "() {");
/* 1738 */       indent();
/* 1739 */       p("return bean().has" + uname + "();");
/* 1740 */       unindent();
/* 1741 */       p("}");
/* 1742 */       p();
/* 1743 */       p("public " + type + " get" + uname + "() {");
/* 1744 */       indent();
/* 1745 */       p("return bean().get" + uname + "();");
/* 1746 */       unindent();
/* 1747 */       p("}");
/* 1748 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateFieldGetterSignatures(FieldDescriptor field)
/*      */   {
/* 1757 */     String uname = uCamel(field.getName());
/* 1758 */     String type = field.getRule() == FieldDescriptor.REPEATED_RULE ? javaCollectionType(field) : javaType(field);
/* 1759 */     boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1762 */     p("// " + field.getRule() + " " + field.getType() + " " + field.getName() + " = " + field.getTag() + ";");
/* 1763 */     if (repeated)
/*      */     {
/* 1765 */       p("public boolean has" + uname + "();");
/* 1766 */       p("public java.util.List<" + type + "> get" + uname + "List();");
/* 1767 */       p("public int get" + uname + "Count();");
/* 1768 */       p("public " + type + " get" + uname + "(int index);");
/*      */     }
/*      */     else {
/* 1771 */       p("public boolean has" + uname + "();");
/* 1772 */       p("public " + type + " get" + uname + "();");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateFieldAccessor(String beanClassName, FieldDescriptor field)
/*      */   {
/* 1783 */     String lname = lCamel(field.getName());
/* 1784 */     String uname = uCamel(field.getName());
/* 1785 */     String type = field.getRule() == FieldDescriptor.REPEATED_RULE ? javaCollectionType(field) : javaType(field);
/* 1786 */     String typeDefault = javaTypeDefault(field);
/* 1787 */     boolean primitive = (field.getTypeDescriptor() == null) || (field.getTypeDescriptor().isEnum());
/* 1788 */     boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1791 */     p("// " + field.getRule() + " " + field.getType() + " " + field.getName() + " = " + field.getTag() + ";");
/*      */ 
/* 1793 */     if (repeated) {
/* 1794 */       p("private java.util.List<" + type + "> f_" + lname + ";");
/* 1795 */       p();
/*      */ 
/* 1798 */       p("public boolean has" + uname + "() {");
/* 1799 */       indent();
/* 1800 */       p("return bean.f_" + lname + "!=null && !bean.f_" + lname + ".isEmpty();");
/* 1801 */       unindent();
/* 1802 */       p("}");
/* 1803 */       p();
/*      */ 
/* 1805 */       p("public java.util.List<" + type + "> get" + uname + "List() {");
/* 1806 */       indent();
/* 1807 */       p("return bean.f_" + lname + ";");
/* 1808 */       unindent();
/* 1809 */       p("}");
/* 1810 */       p();
/*      */ 
/* 1812 */       p("public java.util.List<" + type + "> create" + uname + "List() {");
/* 1813 */       indent();
/* 1814 */       p("copyCheck();");
/* 1815 */       p("if( this.f_" + lname + " == null ) {");
/* 1816 */       indent();
/* 1817 */       p("this.f_" + lname + " = new java.util.ArrayList<" + type + ">();");
/* 1818 */       unindent();
/* 1819 */       p("}");
/* 1820 */       p("return bean.f_" + lname + ";");
/* 1821 */       unindent();
/* 1822 */       p("}");
/* 1823 */       p();
/*      */ 
/* 1825 */       p("public " + beanClassName + " set" + uname + "List(java.util.List<" + type + "> " + lname + ") {");
/* 1826 */       indent();
/* 1827 */       p("copyCheck();");
/* 1828 */       p("this.f_" + lname + " = " + lname + ";");
/* 1829 */       p("return this;");
/* 1830 */       unindent();
/* 1831 */       p("}");
/* 1832 */       p();
/*      */ 
/* 1834 */       p("public int get" + uname + "Count() {");
/* 1835 */       indent();
/* 1836 */       p("if( bean.f_" + lname + " == null ) {");
/* 1837 */       indent();
/* 1838 */       p("return 0;");
/* 1839 */       unindent();
/* 1840 */       p("}");
/* 1841 */       p("return bean.f_" + lname + ".size();");
/* 1842 */       unindent();
/* 1843 */       p("}");
/* 1844 */       p();
/*      */ 
/* 1846 */       p("public " + type + " get" + uname + "(int index) {");
/* 1847 */       indent();
/* 1848 */       p("if( bean.f_" + lname + " == null ) {");
/* 1849 */       indent();
/* 1850 */       p("return null;");
/* 1851 */       unindent();
/* 1852 */       p("}");
/* 1853 */       p("return bean.f_" + lname + ".get(index);");
/* 1854 */       unindent();
/* 1855 */       p("}");
/* 1856 */       p();
/*      */ 
/* 1858 */       p("public " + beanClassName + " set" + uname + "(int index, " + type + " value) {");
/* 1859 */       indent();
/* 1860 */       p("this.create" + uname + "List().set(index, value);");
/* 1861 */       p("return this;");
/* 1862 */       unindent();
/* 1863 */       p("}");
/* 1864 */       p();
/*      */ 
/* 1866 */       p("public " + beanClassName + " add" + uname + "(" + type + " value) {");
/* 1867 */       indent();
/* 1868 */       p("this.create" + uname + "List().add(value);");
/* 1869 */       p("return this;");
/* 1870 */       unindent();
/* 1871 */       p("}");
/* 1872 */       p();
/*      */ 
/* 1874 */       p("public " + beanClassName + " addAll" + uname + "(java.lang.Iterable<? extends " + type + "> collection) {");
/* 1875 */       indent();
/* 1876 */       p("org.apache.activemq.protobuf.MessageBufferSupport.addAll(collection, this.create" + uname + "List());");
/* 1877 */       p("return this;");
/* 1878 */       unindent();
/* 1879 */       p("}");
/* 1880 */       p();
/*      */ 
/* 1882 */       p("public void clear" + uname + "() {");
/* 1883 */       indent();
/* 1884 */       p("copyCheck();");
/* 1885 */       p("this.f_" + lname + " = null;");
/* 1886 */       unindent();
/* 1887 */       p("}");
/* 1888 */       p();
/*      */     }
/*      */     else
/*      */     {
/* 1892 */       p("private " + type + " f_" + lname + " = " + typeDefault + ";");
/* 1893 */       if (primitive) {
/* 1894 */         p("private boolean b_" + lname + ";");
/*      */       }
/* 1896 */       p();
/*      */ 
/* 1899 */       p("public boolean has" + uname + "() {");
/* 1900 */       indent();
/* 1901 */       if (primitive)
/* 1902 */         p("return bean.b_" + lname + ";");
/*      */       else {
/* 1904 */         p("return bean.f_" + lname + "!=null;");
/*      */       }
/* 1906 */       unindent();
/* 1907 */       p("}");
/* 1908 */       p();
/*      */ 
/* 1910 */       p("public " + type + " get" + uname + "() {");
/* 1911 */       indent();
/* 1912 */       p("return bean.f_" + lname + ";");
/* 1913 */       unindent();
/* 1914 */       p("}");
/* 1915 */       p();
/*      */ 
/* 1917 */       p("public " + beanClassName + " set" + uname + "(" + type + " " + lname + ") {");
/* 1918 */       indent();
/* 1919 */       p("copyCheck();");
/* 1920 */       if (primitive) {
/* 1921 */         if ((this.auto_clear_optional_fields) && (field.isOptional())) {
/* 1922 */           if ((field.isStringType()) && (!"null".equals(typeDefault)))
/* 1923 */             p("this.b_" + lname + " = (" + lname + " != " + typeDefault + ");");
/*      */           else
/* 1925 */             p("this.b_" + lname + " = (" + lname + " != " + typeDefault + ");");
/*      */         }
/*      */         else {
/* 1928 */           p("this.b_" + lname + " = true;");
/*      */         }
/*      */       }
/* 1931 */       p("this.f_" + lname + " = " + lname + ";");
/* 1932 */       p("return this;");
/* 1933 */       unindent();
/* 1934 */       p("}");
/* 1935 */       p();
/*      */ 
/* 1937 */       p("public void clear" + uname + "() {");
/* 1938 */       indent();
/* 1939 */       p("copyCheck();");
/* 1940 */       if (primitive) {
/* 1941 */         p("this.b_" + lname + " = false;");
/*      */       }
/* 1943 */       p("this.f_" + lname + " = " + typeDefault + ";");
/* 1944 */       unindent();
/* 1945 */       p("}");
/* 1946 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private String javaTypeDefault(FieldDescriptor field)
/*      */   {
/* 1952 */     OptionDescriptor defaultOption = (OptionDescriptor)field.getOptions().get("default");
/* 1953 */     if (defaultOption != null) {
/* 1954 */       if (field.isStringType())
/* 1955 */         return asJavaString(defaultOption.getValue());
/* 1956 */       if (field.getType() == FieldDescriptor.BYTES_TYPE)
/* 1957 */         return "new " + javaType(field) + "(" + asJavaString(defaultOption.getValue()) + ")";
/* 1958 */       if (field.isInteger32Type())
/*      */       {
/*      */         int v;
/*      */         int v;
/* 1960 */         if (field.getType() == FieldDescriptor.UINT32_TYPE)
/* 1961 */           v = TextFormat.parseUInt32(defaultOption.getValue());
/*      */         else {
/* 1963 */           v = TextFormat.parseInt32(defaultOption.getValue());
/*      */         }
/* 1965 */         return "" + v;
/* 1966 */       }if (field.isInteger64Type())
/*      */       {
/*      */         long v;
/*      */         long v;
/* 1968 */         if (field.getType() == FieldDescriptor.UINT64_TYPE)
/* 1969 */           v = TextFormat.parseUInt64(defaultOption.getValue());
/*      */         else {
/* 1971 */           v = TextFormat.parseInt64(defaultOption.getValue());
/*      */         }
/* 1973 */         return "" + v + "l";
/* 1974 */       }if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1975 */         double v = Double.valueOf(defaultOption.getValue()).doubleValue();
/* 1976 */         return "" + v + "d";
/* 1977 */       }if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1978 */         float v = Float.valueOf(defaultOption.getValue()).floatValue();
/* 1979 */         return "" + v + "f";
/* 1980 */       }if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1981 */         boolean v = Boolean.valueOf(defaultOption.getValue()).booleanValue();
/* 1982 */         return "" + v;
/* 1983 */       }if ((field.getTypeDescriptor() != null) && (field.getTypeDescriptor().isEnum())) {
/* 1984 */         return javaType(field) + "." + defaultOption.getValue();
/*      */       }
/* 1986 */       return defaultOption.getValue();
/*      */     }
/* 1988 */     if (field.isNumberType()) {
/* 1989 */       return "0";
/*      */     }
/* 1991 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1992 */       return "false";
/*      */     }
/* 1994 */     return "null";
/*      */   }
/*      */ 
/*      */   private String asJavaString(String value)
/*      */   {
/* 2001 */     StringBuilder sb = new StringBuilder(value.length() + 2);
/* 2002 */     sb.append("\"");
/* 2003 */     for (int i = 0; i < value.length(); i++)
/*      */     {
/* 2005 */       char b = value.charAt(i);
/* 2006 */       switch (b) {
/*      */       case '\b':
/* 2008 */         sb.append("\\b"); break;
/*      */       case '\f':
/* 2009 */         sb.append("\\f"); break;
/*      */       case '\n':
/* 2010 */         sb.append("\\n"); break;
/*      */       case '\r':
/* 2011 */         sb.append("\\r"); break;
/*      */       case '\t':
/* 2012 */         sb.append("\\t"); break;
/*      */       case '\\':
/* 2013 */         sb.append("\\\\"); break;
/*      */       case '\'':
/* 2014 */         sb.append("\\'"); break;
/*      */       case '"':
/* 2015 */         sb.append("\\\""); break;
/*      */       default:
/* 2017 */         if ((b >= ' ') && (b < 'Z')) {
/* 2018 */           sb.append(b);
/*      */         } else {
/* 2020 */           sb.append("\\u");
/* 2021 */           sb.append(HEX_TABLE[(b >>> '\f' & 0xF)]);
/* 2022 */           sb.append(HEX_TABLE[(b >>> '\b' & 0xF)]);
/* 2023 */           sb.append(HEX_TABLE[(b >>> '\004' & 0xF)]);
/* 2024 */           sb.append(HEX_TABLE[(b & 0xF)]);
/*      */         }
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/* 2030 */     sb.append("\"");
/* 2031 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private void generateEnum(EnumDescriptor ed) {
/* 2035 */     String uname = uCamel(ed.getName());
/*      */ 
/* 2037 */     String staticOption = "static ";
/* 2038 */     if ((this.multipleFiles) && (ed.getParent() == null)) {
/* 2039 */       staticOption = "";
/*      */     }
/*      */ 
/* 2043 */     p();
/* 2044 */     p("public " + staticOption + "enum " + uname + " {");
/* 2045 */     indent();
/*      */ 
/* 2048 */     p();
/* 2049 */     int counter = 0;
/* 2050 */     for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 2051 */       boolean last = counter + 1 == ed.getFields().size();
/* 2052 */       p(field.getName() + "(\"" + field.getName() + "\", " + field.getValue() + ")" + (last ? ";" : ","));
/* 2053 */       counter++;
/*      */     }
/* 2055 */     p();
/* 2056 */     p("private final String name;");
/* 2057 */     p("private final int value;");
/* 2058 */     p();
/* 2059 */     p("private " + uname + "(String name, int value) {");
/* 2060 */     p("   this.name = name;");
/* 2061 */     p("   this.value = value;");
/* 2062 */     p("}");
/* 2063 */     p();
/* 2064 */     p("public final int getNumber() {");
/* 2065 */     p("   return value;");
/* 2066 */     p("}");
/* 2067 */     p();
/* 2068 */     p("public final String toString() {");
/* 2069 */     p("   return name;");
/* 2070 */     p("}");
/* 2071 */     p();
/* 2072 */     p("public static " + uname + " valueOf(int value) {");
/* 2073 */     p("   switch (value) {");
/*      */ 
/* 2077 */     HashSet values = new HashSet();
/* 2078 */     for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 2079 */       if (!values.contains(Integer.valueOf(field.getValue()))) {
/* 2080 */         p("   case " + field.getValue() + ":");
/* 2081 */         p("      return " + field.getName() + ";");
/* 2082 */         values.add(Integer.valueOf(field.getValue()));
/*      */       }
/*      */     }
/*      */ 
/* 2086 */     p("   default:");
/* 2087 */     p("      return null;");
/* 2088 */     p("   }");
/* 2089 */     p("}");
/* 2090 */     p();
/*      */ 
/* 2093 */     String createMessage = getOption(ed.getOptions(), "java_create_message", null);
/* 2094 */     if ("true".equals(createMessage))
/*      */     {
/* 2096 */       p("public interface " + uname + "Creatable {");
/* 2097 */       indent();
/* 2098 */       p("" + uname + " to" + uname + "();");
/* 2099 */       unindent();
/* 2100 */       p("}");
/* 2101 */       p();
/*      */ 
/* 2103 */       p("public " + uname + "Creatable createBean() {");
/* 2104 */       indent();
/* 2105 */       p("switch (this) {");
/* 2106 */       indent();
/* 2107 */       for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 2108 */         p("case " + field.getName() + ":");
/* 2109 */         String type = field.getAssociatedType().getName();
/* 2110 */         p("   return new " + javaRelatedType(type, "Bean") + "();");
/*      */       }
/* 2112 */       p("default:");
/* 2113 */       p("   return null;");
/* 2114 */       unindent();
/* 2115 */       p("}");
/* 2116 */       unindent();
/* 2117 */       p("}");
/* 2118 */       p();
/*      */ 
/* 2120 */       generateParseDelegate(ed, "parseUnframed", "org.apache.activemq.protobuf.Buffer", "org.apache.activemq.protobuf.InvalidProtocolBufferException");
/* 2121 */       generateParseDelegate(ed, "parseFramed", "org.apache.activemq.protobuf.Buffer", "org.apache.activemq.protobuf.InvalidProtocolBufferException");
/* 2122 */       generateParseDelegate(ed, "parseUnframed", "byte[]", "org.apache.activemq.protobuf.InvalidProtocolBufferException");
/* 2123 */       generateParseDelegate(ed, "parseFramed", "byte[]", "org.apache.activemq.protobuf.InvalidProtocolBufferException");
/* 2124 */       generateParseDelegate(ed, "parseFramed", "org.apache.activemq.protobuf.CodedInputStream", "org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException");
/* 2125 */       generateParseDelegate(ed, "parseFramed", "java.io.InputStream", "org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException");
/*      */     }
/*      */ 
/* 2128 */     unindent();
/* 2129 */     p("}");
/* 2130 */     p();
/*      */   }
/*      */ 
/*      */   private void generateParseDelegate(EnumDescriptor descriptor, String methodName, String inputType, String exceptions) {
/* 2134 */     p("public org.apache.activemq.protobuf.MessageBuffer " + methodName + "(" + inputType + " data) throws " + exceptions + " {");
/* 2135 */     indent();
/* 2136 */     p("switch (this) {");
/* 2137 */     indent();
/* 2138 */     for (EnumFieldDescriptor field : descriptor.getFields().values()) {
/* 2139 */       p("case " + field.getName() + ":");
/* 2140 */       String type = constantToUCamelCase(field.getName());
/* 2141 */       p("   return " + javaRelatedType(type, "Buffer") + "." + methodName + "(data);");
/*      */     }
/* 2143 */     p("default:");
/* 2144 */     p("   return null;");
/* 2145 */     unindent();
/* 2146 */     p("}");
/* 2147 */     unindent();
/* 2148 */     p("}");
/* 2149 */     p();
/*      */   }
/*      */ 
/*      */   private String javaCollectionType(FieldDescriptor field)
/*      */   {
/* 2155 */     if (field.isInteger32Type()) {
/* 2156 */       return "java.lang.Integer";
/*      */     }
/* 2158 */     if (field.isInteger64Type()) {
/* 2159 */       return "java.lang.Long";
/*      */     }
/* 2161 */     if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 2162 */       return "java.lang.Double";
/*      */     }
/* 2164 */     if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 2165 */       return "java.lang.Float";
/*      */     }
/* 2167 */     if (field.getType() == FieldDescriptor.STRING_TYPE)
/*      */     {
/* 2177 */       return "java.lang.String";
/*      */     }
/*      */ 
/* 2180 */     if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 2181 */       String override = getOption(field.getOptions(), "java_override_type", null);
/* 2182 */       if ("AsciiBuffer".equals(override))
/* 2183 */         return "org.apache.activemq.protobuf.AsciiBuffer";
/* 2184 */       if ("UTF8Buffer".equals(override)) {
/* 2185 */         return "org.apache.activemq.protobuf.UTF8Buffer";
/*      */       }
/* 2187 */       return "org.apache.activemq.protobuf.Buffer";
/*      */     }
/*      */ 
/* 2190 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 2191 */       return "java.lang.Boolean";
/*      */     }
/*      */ 
/* 2194 */     TypeDescriptor descriptor = field.getTypeDescriptor();
/* 2195 */     return javaType(descriptor);
/*      */   }
/*      */ 
/*      */   private String javaType(FieldDescriptor field) {
/* 2199 */     if (field.isInteger32Type()) {
/* 2200 */       return "int";
/*      */     }
/* 2202 */     if (field.isInteger64Type()) {
/* 2203 */       return "long";
/*      */     }
/* 2205 */     if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 2206 */       return "double";
/*      */     }
/* 2208 */     if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 2209 */       return "float";
/*      */     }
/* 2211 */     if (field.getType() == FieldDescriptor.STRING_TYPE)
/*      */     {
/* 2221 */       return "java.lang.String";
/*      */     }
/*      */ 
/* 2224 */     if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 2225 */       String override = getOption(field.getOptions(), "java_override_type", null);
/* 2226 */       if ("AsciiBuffer".equals(override))
/* 2227 */         return "org.apache.activemq.protobuf.AsciiBuffer";
/* 2228 */       if ("UTF8Buffer".equals(override)) {
/* 2229 */         return "org.apache.activemq.protobuf.UTF8Buffer";
/*      */       }
/* 2231 */       return "org.apache.activemq.protobuf.Buffer";
/*      */     }
/*      */ 
/* 2234 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 2235 */       return "boolean";
/*      */     }
/*      */ 
/* 2238 */     TypeDescriptor descriptor = field.getTypeDescriptor();
/* 2239 */     return javaType(descriptor);
/*      */   }
/*      */ 
/*      */   private String javaType(TypeDescriptor descriptor) {
/* 2243 */     ProtoDescriptor p = descriptor.getProtoDescriptor();
/* 2244 */     if (p != this.proto)
/*      */     {
/* 2246 */       String othePackage = javaPackage(p);
/* 2247 */       if (equals(othePackage, javaPackage(this.proto))) {
/* 2248 */         return javaClassName(p) + "." + descriptor.getQName();
/*      */       }
/*      */ 
/* 2251 */       return othePackage + "." + javaClassName(p) + "." + descriptor.getQName();
/*      */     }
/* 2253 */     return descriptor.getQName();
/*      */   }
/*      */ 
/*      */   private String javaRelatedType(String type, String suffix) {
/* 2257 */     int ix = type.lastIndexOf(".");
/* 2258 */     if (ix == -1)
/*      */     {
/* 2260 */       return type + "." + type + suffix;
/*      */     }
/*      */ 
/* 2263 */     return type + "." + type.substring(ix + 1) + suffix;
/*      */   }
/*      */ 
/*      */   private boolean equals(String o1, String o2) {
/* 2267 */     if (o1 == o2)
/* 2268 */       return true;
/* 2269 */     if ((o1 == null) || (o2 == null))
/* 2270 */       return false;
/* 2271 */     return o1.equals(o2);
/*      */   }
/*      */ 
/*      */   private String javaClassName(ProtoDescriptor proto) {
/* 2275 */     return getOption(proto.getOptions(), "java_outer_classname", uCamel(removeFileExtension(proto.getName())));
/*      */   }
/*      */ 
/*      */   private boolean isMultipleFilesEnabled(ProtoDescriptor proto) {
/* 2279 */     return "true".equals(getOption(proto.getOptions(), "java_multiple_files", "false"));
/*      */   }
/*      */ 
/*      */   private String javaPackage(ProtoDescriptor proto)
/*      */   {
/* 2284 */     String name = proto.getPackageName();
/* 2285 */     if (name != null) {
/* 2286 */       name = name.replace('-', '.');
/* 2287 */       name = name.replace('/', '.');
/*      */     }
/* 2289 */     return getOption(proto.getOptions(), "java_package", name);
/*      */   }
/*      */ 
/*      */   private void indent()
/*      */   {
/* 2298 */     this.indent += 1;
/*      */   }
/*      */ 
/*      */   private void unindent() {
/* 2302 */     this.indent -= 1;
/*      */   }
/*      */ 
/*      */   private void p(String line)
/*      */   {
/* 2307 */     for (int i = 0; i < this.indent; i++) {
/* 2308 */       this.w.print("   ");
/*      */     }
/*      */ 
/* 2311 */     this.w.println(line);
/*      */   }
/*      */ 
/*      */   private void p() {
/* 2315 */     this.w.println();
/*      */   }
/*      */ 
/*      */   private String getOption(Map<String, OptionDescriptor> options, String optionName, String defaultValue) {
/* 2319 */     OptionDescriptor optionDescriptor = (OptionDescriptor)options.get(optionName);
/* 2320 */     if (optionDescriptor == null) {
/* 2321 */       return defaultValue;
/*      */     }
/* 2323 */     return optionDescriptor.getValue();
/*      */   }
/*      */ 
/*      */   private static String removeFileExtension(String name) {
/* 2327 */     return name.replaceAll("\\..*", "");
/*      */   }
/*      */ 
/*      */   private static String uCamel(String name) {
/* 2331 */     boolean upNext = true;
/* 2332 */     StringBuilder sb = new StringBuilder();
/* 2333 */     for (int i = 0; i < name.length(); i++) {
/* 2334 */       char c = name.charAt(i);
/* 2335 */       if ((Character.isJavaIdentifierPart(c)) && (Character.isLetterOrDigit(c))) {
/* 2336 */         if (upNext) {
/* 2337 */           c = Character.toUpperCase(c);
/* 2338 */           upNext = false;
/*      */         }
/* 2340 */         sb.append(c);
/*      */       } else {
/* 2342 */         upNext = true;
/*      */       }
/*      */     }
/* 2345 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private static String lCamel(String name) {
/* 2349 */     if ((name == null) || (name.length() < 1))
/* 2350 */       return name;
/* 2351 */     String uCamel = uCamel(name);
/* 2352 */     return uCamel.substring(0, 1).toLowerCase() + uCamel.substring(1);
/*      */   }
/*      */ 
/*      */   private String constantToUCamelCase(String name)
/*      */   {
/* 2357 */     boolean upNext = true;
/* 2358 */     StringBuilder sb = new StringBuilder();
/* 2359 */     for (int i = 0; i < name.length(); i++) {
/* 2360 */       char c = name.charAt(i);
/* 2361 */       if ((Character.isJavaIdentifierPart(c)) && (Character.isLetterOrDigit(c))) {
/* 2362 */         if (upNext) {
/* 2363 */           c = Character.toUpperCase(c);
/* 2364 */           upNext = false;
/*      */         } else {
/* 2366 */           c = Character.toLowerCase(c);
/*      */         }
/* 2368 */         sb.append(c);
/*      */       } else {
/* 2370 */         upNext = true;
/*      */       }
/*      */     }
/* 2373 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private String constantCase(String name)
/*      */   {
/* 2378 */     StringBuilder sb = new StringBuilder();
/* 2379 */     for (int i = 0; i < name.length(); i++) {
/* 2380 */       char c = name.charAt(i);
/* 2381 */       if ((i != 0) && (Character.isUpperCase(c))) {
/* 2382 */         sb.append("_");
/*      */       }
/* 2384 */       sb.append(Character.toUpperCase(c));
/*      */     }
/* 2386 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public File getOut() {
/* 2390 */     return this.out;
/*      */   }
/*      */ 
/*      */   public void setOut(File outputDirectory) {
/* 2394 */     this.out = outputDirectory;
/*      */   }
/*      */ 
/*      */   public File[] getPath() {
/* 2398 */     return this.path;
/*      */   }
/*      */ 
/*      */   public void setPath(File[] path) {
/* 2402 */     this.path = path;
/*      */   }
/*      */ 
/*      */   static abstract interface Closure
/*      */   {
/*      */     public abstract void execute()
/*      */       throws CompilerException;
/*      */   }
/*      */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.AltJavaGenerator
 * JD-Core Version:    0.6.2
 */