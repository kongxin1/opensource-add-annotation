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
/*      */ public class JavaGenerator
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
/*      */   private boolean deferredDecode;
/*      */   private boolean auto_clear_optional_fields;
/* 1387 */   static final char[] HEX_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*      */ 
/*      */   public JavaGenerator()
/*      */   {
/*   43 */     this.out = new File(".");
/*   44 */     this.path = new File[] { new File(".") };
/*      */ 
/*   51 */     this.errors = new ArrayList();
/*      */   }
/*      */ 
/*      */   public static void main(String[] args)
/*      */   {
/*   58 */     JavaGenerator generator = new JavaGenerator();
/*   59 */     args = CommandLineSupport.setOptions(generator, args);
/*      */ 
/*   61 */     if (args.length == 0) {
/*   62 */       System.out.println("No proto files specified.");
/*      */     }
/*   64 */     for (int i = 0; i < args.length; i++)
/*      */       try {
/*   66 */         System.out.println("Compiling: " + args[i]);
/*   67 */         generator.compile(new File(args[i]));
/*      */       } catch (CompilerException e) {
/*   69 */         System.out.println("Protocol Buffer Compiler failed with the following error(s):");
/*   70 */         for (String error : e.getErrors()) {
/*   71 */           System.out.println("");
/*   72 */           System.out.println(error);
/*      */         }
/*   74 */         System.out.println("");
/*   75 */         System.out.println("Compile failed.  For more details see error messages listed above.");
/*   76 */         return;
/*      */       }
/*      */   }
/*      */ 
/*      */   public void compile(File file)
/*      */     throws CompilerException
/*      */   {
/*   89 */     FileInputStream is = null;
/*      */     try {
/*   91 */       is = new FileInputStream(file);
/*   92 */       ProtoParser parser = new ProtoParser(is);
/*   93 */       this.proto = parser.ProtoDescriptor();
/*   94 */       this.proto.setName(file.getName());
/*   95 */       loadImports(this.proto, file.getParentFile());
/*   96 */       this.proto.validate(this.errors);
/*      */     } catch (FileNotFoundException e) {
/*   98 */       this.errors.add("Failed to open: " + file.getPath() + ":" + e.getMessage());
/*      */     } catch (ParseException e) {
/*  100 */       this.errors.add("Failed to parse: " + file.getPath() + ":" + e.getMessage()); } finally {
/*      */       try {
/*  102 */         is.close(); } catch (Throwable ignore) {
/*      */       }
/*      */     }
/*  105 */     if (!this.errors.isEmpty()) {
/*  106 */       throw new CompilerException(this.errors);
/*      */     }
/*      */ 
/*  110 */     this.javaPackage = javaPackage(this.proto);
/*  111 */     this.outerClassName = javaClassName(this.proto);
/*      */ 
/*  113 */     this.multipleFiles = isMultipleFilesEnabled(this.proto);
/*  114 */     this.deferredDecode = Boolean.parseBoolean(getOption(this.proto.getOptions(), "deferred_decode", "false"));
/*  115 */     this.auto_clear_optional_fields = Boolean.parseBoolean(getOption(this.proto.getOptions(), "auto_clear_optional_fields", "false"));
/*      */ 
/*  117 */     if (this.multipleFiles)
/*  118 */       generateProtoFile();
/*      */     else {
/*  120 */       writeFile(this.outerClassName, new Closure() {
/*      */         public void execute() throws CompilerException {
/*  122 */           JavaGenerator.this.generateProtoFile();
/*      */         }
/*      */       });
/*      */     }
/*      */ 
/*  127 */     if (!this.errors.isEmpty())
/*  128 */       throw new CompilerException(this.errors);
/*      */   }
/*      */ 
/*      */   private void writeFile(String className, Closure closure)
/*      */     throws CompilerException
/*      */   {
/*  134 */     PrintWriter oldWriter = this.w;
/*      */ 
/*  136 */     File outputFile = this.out;
/*  137 */     if (this.javaPackage != null) {
/*  138 */       String packagePath = this.javaPackage.replace('.', '/');
/*  139 */       outputFile = new File(outputFile, packagePath);
/*      */     }
/*  141 */     outputFile = new File(outputFile, className + ".java");
/*      */ 
/*  144 */     outputFile.getParentFile().mkdirs();
/*      */ 
/*  146 */     FileOutputStream fos = null;
/*      */     try {
/*  148 */       fos = new FileOutputStream(outputFile);
/*  149 */       this.w = new PrintWriter(fos);
/*  150 */       closure.execute();
/*  151 */       this.w.flush();
/*      */     } catch (FileNotFoundException e) {
/*  153 */       this.errors.add("Failed to write to: " + outputFile.getPath() + ":" + e.getMessage()); } finally {
/*      */       try {
/*  155 */         fos.close(); } catch (Throwable ignore) {
/*  156 */       }this.w = oldWriter;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void loadImports(ProtoDescriptor proto, File protoDir) {
/*  161 */     LinkedHashMap children = new LinkedHashMap();
/*  162 */     for (String imp : proto.getImports()) {
/*  163 */       File file = new File(protoDir, imp);
/*  164 */       for (int i = 0; (i < this.path.length) && (!file.exists()); i++) {
/*  165 */         file = new File(this.path[i], imp);
/*      */       }
/*  167 */       if (!file.exists()) {
/*  168 */         this.errors.add("Cannot load import: " + imp);
/*      */       }
/*      */ 
/*  171 */       FileInputStream is = null;
/*      */       try {
/*  173 */         is = new FileInputStream(file);
/*  174 */         ProtoParser parser = new ProtoParser(is);
/*  175 */         ProtoDescriptor child = parser.ProtoDescriptor();
/*  176 */         child.setName(file.getName());
/*  177 */         loadImports(child, file.getParentFile());
/*  178 */         children.put(imp, child);
/*      */       } catch (ParseException e) {
/*  180 */         this.errors.add("Failed to parse: " + file.getPath() + ":" + e.getMessage());
/*      */       } catch (FileNotFoundException e) {
/*  182 */         this.errors.add("Failed to open: " + file.getPath() + ":" + e.getMessage()); } finally {
/*      */         try {
/*  184 */           is.close(); } catch (Throwable ignore) {  }
/*      */       }
/*      */     }
/*  187 */     proto.setImportProtoDescriptors(children);
/*      */   }
/*      */ 
/*      */   private void generateProtoFile() throws CompilerException
/*      */   {
/*  192 */     if (this.multipleFiles) {
/*  193 */       for (EnumDescriptor value : this.proto.getEnums().values()) {
/*  194 */         final EnumDescriptor o = value;
/*  195 */         String className = uCamel(o.getName());
/*  196 */         writeFile(className, new Closure() {
/*      */           public void execute() throws CompilerException {
/*  198 */             JavaGenerator.this.generateFileHeader();
/*  199 */             JavaGenerator.this.generateEnum(o);
/*      */           }
/*      */         });
/*      */       }
/*  203 */       for (MessageDescriptor value : this.proto.getMessages().values()) {
/*  204 */         final MessageDescriptor o = value;
/*  205 */         String className = uCamel(o.getName());
/*  206 */         writeFile(className, new Closure() {
/*      */           public void execute() throws CompilerException {
/*  208 */             JavaGenerator.this.generateFileHeader();
/*  209 */             JavaGenerator.this.generateMessageBean(o);
/*      */           }
/*      */         });
/*      */       }
/*      */     }
/*      */     else {
/*  215 */       generateFileHeader();
/*      */ 
/*  217 */       p("public class " + this.outerClassName + " {");
/*  218 */       indent();
/*      */ 
/*  220 */       for (EnumDescriptor enumType : this.proto.getEnums().values()) {
/*  221 */         generateEnum(enumType);
/*      */       }
/*  223 */       for (MessageDescriptor m : this.proto.getMessages().values()) {
/*  224 */         generateMessageBean(m);
/*      */       }
/*      */ 
/*  227 */       unindent();
/*  228 */       p("}");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateFileHeader() {
/*  233 */     p("//");
/*  234 */     p("// Generated by protoc, do not edit by hand.");
/*  235 */     p("//");
/*  236 */     if (this.javaPackage != null) {
/*  237 */       p("package " + this.javaPackage + ";");
/*  238 */       p("");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMessageBean(MessageDescriptor m)
/*      */   {
/*  244 */     String className = uCamel(m.getName());
/*  245 */     p();
/*      */ 
/*  247 */     String staticOption = "static ";
/*  248 */     if ((this.multipleFiles) && (m.getParent() == null)) {
/*  249 */       staticOption = "";
/*      */     }
/*      */ 
/*  252 */     String javaImplements = getOption(m.getOptions(), "java_implments", null);
/*      */ 
/*  254 */     String implementsExpression = "";
/*  255 */     if (javaImplements != null) {
/*  256 */       implementsExpression = "implements " + javaImplements + " ";
/*      */     }
/*      */ 
/*  259 */     String baseClass = "org.apache.activemq.protobuf.BaseMessage";
/*  260 */     if (this.deferredDecode) {
/*  261 */       baseClass = "org.apache.activemq.protobuf.DeferredDecodeMessage";
/*      */     }
/*  263 */     if (m.getBaseType() != null) {
/*  264 */       baseClass = javaType(m.getBaseType()) + "Base";
/*      */     }
/*      */ 
/*  267 */     p(staticOption + "public final class " + className + " extends " + className + "Base<" + className + "> " + implementsExpression + "{");
/*  268 */     p();
/*      */ 
/*  270 */     indent();
/*      */ 
/*  272 */     for (EnumDescriptor enumType : m.getEnums().values()) {
/*  273 */       generateEnum(enumType);
/*      */     }
/*      */ 
/*  277 */     for (MessageDescriptor subMessage : m.getMessages().values()) {
/*  278 */       generateMessageBean(subMessage);
/*      */     }
/*      */ 
/*  282 */     for (FieldDescriptor field : m.getFields().values()) {
/*  283 */       if (!isInBaseClass(m, field))
/*      */       {
/*  286 */         if (field.isGroup()) {
/*  287 */           generateMessageBean(field.getGroup());
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  292 */     generateMethodAssertInitialized(m, className);
/*      */ 
/*  294 */     generateMethodClear(m);
/*      */ 
/*  296 */     p("public " + className + " clone() {");
/*  297 */     p("   return new " + className + "().mergeFrom(this);");
/*  298 */     p("}");
/*  299 */     p();
/*      */ 
/*  301 */     generateMethodMergeFromBean(m, className);
/*      */ 
/*  303 */     generateMethodSerializedSize(m);
/*      */ 
/*  305 */     generateMethodMergeFromStream(m, className);
/*      */ 
/*  307 */     generateMethodWriteTo(m);
/*      */ 
/*  309 */     generateMethodParseFrom(m, className);
/*      */ 
/*  311 */     generateMethodToString(m);
/*      */ 
/*  313 */     generateMethodVisitor(m);
/*      */ 
/*  315 */     generateMethodType(m, className);
/*      */ 
/*  317 */     generateMethodEquals(m, className);
/*      */ 
/*  319 */     unindent();
/*  320 */     p("}");
/*  321 */     p();
/*      */ 
/*  323 */     p(staticOption + "abstract class " + className + "Base<T> extends " + baseClass + "<T> {");
/*  324 */     p();
/*  325 */     indent();
/*      */ 
/*  328 */     for (FieldDescriptor field : m.getFields().values()) {
/*  329 */       if (!isInBaseClass(m, field))
/*      */       {
/*  332 */         generateFieldAccessor(field);
/*      */       }
/*      */     }
/*  335 */     unindent();
/*  336 */     p("}");
/*  337 */     p();
/*      */   }
/*      */ 
/*      */   private boolean isInBaseClass(MessageDescriptor m, FieldDescriptor field) {
/*  341 */     if (m.getBaseType() == null)
/*  342 */       return false;
/*  343 */     return m.getBaseType().getFields().containsKey(field.getName());
/*      */   }
/*      */ 
/*      */   private void generateMethodVisitor(MessageDescriptor m)
/*      */   {
/*  367 */     String javaVisitor = getOption(m.getOptions(), "java_visitor", null);
/*  368 */     if (javaVisitor != null) {
/*  369 */       String returns = "void";
/*  370 */       String throwsException = null;
/*      */ 
/*  372 */       StringTokenizer st = new StringTokenizer(javaVisitor, ":");
/*  373 */       String vistorClass = st.nextToken();
/*  374 */       if (st.hasMoreTokens()) {
/*  375 */         returns = st.nextToken();
/*      */       }
/*  377 */       if (st.hasMoreTokens()) {
/*  378 */         throwsException = st.nextToken();
/*      */       }
/*      */ 
/*  381 */       String throwsClause = "";
/*  382 */       if (throwsException != null) {
/*  383 */         throwsClause = "throws " + throwsException + " ";
/*      */       }
/*      */ 
/*  386 */       p("public " + returns + " visit(" + vistorClass + " visitor) " + throwsClause + "{");
/*  387 */       indent();
/*  388 */       if ("void".equals(returns))
/*  389 */         p("visitor.visit(this);");
/*      */       else {
/*  391 */         p("return visitor.visit(this);");
/*      */       }
/*  393 */       unindent();
/*  394 */       p("}");
/*  395 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMethodType(MessageDescriptor m, String className) {
/*  400 */     String typeEnum = getOption(m.getOptions(), "java_type_method", null);
/*  401 */     if (typeEnum != null)
/*      */     {
/*  403 */       TypeDescriptor typeDescriptor = m.getType(typeEnum);
/*  404 */       if (typeDescriptor == null) {
/*  405 */         typeDescriptor = m.getProtoDescriptor().getType(typeEnum);
/*      */       }
/*  407 */       if ((typeDescriptor == null) || (!typeDescriptor.isEnum())) {
/*  408 */         this.errors.add("The java_type_method option on the " + m.getName() + " message does not point to valid enum type");
/*  409 */         return;
/*      */       }
/*      */ 
/*  413 */       String constant = constantCase(className);
/*  414 */       EnumDescriptor enumDescriptor = (EnumDescriptor)typeDescriptor;
/*  415 */       if (enumDescriptor.getFields().get(constant) == null) {
/*  416 */         this.errors.add("The java_type_method option on the " + m.getName() + " message does not points to the " + typeEnum + " enum but it does not have an entry for " + constant);
/*      */       }
/*      */ 
/*  419 */       String type = javaType(typeDescriptor);
/*      */ 
/*  421 */       p("public " + type + " type() {");
/*  422 */       indent();
/*  423 */       p("return " + type + "." + constant + ";");
/*  424 */       unindent();
/*  425 */       p("}");
/*  426 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void generateMethodParseFrom(MessageDescriptor m, String className)
/*      */   {
/*  432 */     String postMergeProcessing = ".checktInitialized()";
/*  433 */     if (this.deferredDecode) {
/*  434 */       postMergeProcessing = "";
/*      */     }
/*      */ 
/*  437 */     p("public static " + className + " parseUnframed(org.apache.activemq.protobuf.CodedInputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  438 */     indent();
/*  439 */     p("return new " + className + "().mergeUnframed(data)" + postMergeProcessing + ";");
/*  440 */     unindent();
/*  441 */     p("}");
/*  442 */     p();
/*      */ 
/*  444 */     p("public static " + className + " parseUnframed(org.apache.activemq.protobuf.Buffer data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  445 */     indent();
/*  446 */     p("return new " + className + "().mergeUnframed(data)" + postMergeProcessing + ";");
/*  447 */     unindent();
/*  448 */     p("}");
/*  449 */     p();
/*      */ 
/*  451 */     p("public static " + className + " parseUnframed(byte[] data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  452 */     indent();
/*  453 */     p("return new " + className + "().mergeUnframed(data)" + postMergeProcessing + ";");
/*  454 */     unindent();
/*  455 */     p("}");
/*  456 */     p();
/*      */ 
/*  458 */     p("public static " + className + " parseUnframed(java.io.InputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  459 */     indent();
/*  460 */     p("return new " + className + "().mergeUnframed(data)" + postMergeProcessing + ";");
/*  461 */     unindent();
/*  462 */     p("}");
/*  463 */     p();
/*      */ 
/*  465 */     p("public static " + className + " parseFramed(org.apache.activemq.protobuf.CodedInputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  466 */     indent();
/*  467 */     p("return new " + className + "().mergeFramed(data)" + postMergeProcessing + ";");
/*  468 */     unindent();
/*  469 */     p("}");
/*  470 */     p();
/*      */ 
/*  472 */     p("public static " + className + " parseFramed(org.apache.activemq.protobuf.Buffer data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  473 */     indent();
/*  474 */     p("return new " + className + "().mergeFramed(data)" + postMergeProcessing + ";");
/*  475 */     unindent();
/*  476 */     p("}");
/*  477 */     p();
/*      */ 
/*  479 */     p("public static " + className + " parseFramed(byte[] data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException {");
/*  480 */     indent();
/*  481 */     p("return new " + className + "().mergeFramed(data)" + postMergeProcessing + ";");
/*  482 */     unindent();
/*  483 */     p("}");
/*  484 */     p();
/*      */ 
/*  486 */     p("public static " + className + " parseFramed(java.io.InputStream data) throws org.apache.activemq.protobuf.InvalidProtocolBufferException, java.io.IOException {");
/*  487 */     indent();
/*  488 */     p("return new " + className + "().mergeFramed(data)" + postMergeProcessing + ";");
/*  489 */     unindent();
/*  490 */     p("}");
/*  491 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodEquals(MessageDescriptor m, String className) {
/*  495 */     p("public boolean equals(Object obj) {");
/*  496 */     indent();
/*  497 */     p("if( obj==this )");
/*  498 */     p("   return true;");
/*  499 */     p("");
/*  500 */     p("if( obj==null || obj.getClass()!=" + className + ".class )");
/*  501 */     p("   return false;");
/*  502 */     p("");
/*  503 */     p("return equals((" + className + ")obj);");
/*  504 */     unindent();
/*  505 */     p("}");
/*  506 */     p("");
/*      */ 
/*  508 */     p("public boolean equals(" + className + " obj) {");
/*  509 */     indent();
/*  510 */     if (this.deferredDecode) {
/*  511 */       p("return toUnframedBuffer().equals(obj.toUnframedBuffer());");
/*      */     } else {
/*  513 */       for (FieldDescriptor field : m.getFields().values()) {
/*  514 */         String uname = uCamel(field.getName());
/*  515 */         String getterMethod = "get" + uname + "()";
/*  516 */         String hasMethod = "has" + uname + "()";
/*      */ 
/*  518 */         if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  519 */           getterMethod = "get" + uname + "List()";
/*      */         }
/*      */ 
/*  522 */         p("if (" + hasMethod + " ^ obj." + hasMethod + " ) ");
/*  523 */         p("   return false;");
/*      */ 
/*  527 */         if ((field.getRule() != FieldDescriptor.REPEATED_RULE) && ((field.isNumberType()) || (field.getType() == FieldDescriptor.BOOL_TYPE)))
/*  528 */           p("if (" + hasMethod + " && ( " + getterMethod + "!=obj." + getterMethod + " ))");
/*      */         else {
/*  530 */           p("if (" + hasMethod + " && ( !" + getterMethod + ".equals(obj." + getterMethod + ") ))");
/*      */         }
/*  532 */         p("   return false;");
/*      */       }
/*  534 */       p("return true;");
/*      */     }
/*  536 */     unindent();
/*  537 */     p("}");
/*  538 */     p("");
/*  539 */     p("public int hashCode() {");
/*  540 */     indent();
/*  541 */     int hc = className.hashCode();
/*  542 */     if (this.deferredDecode) {
/*  543 */       p("return " + hc + " ^ toUnframedBuffer().hashCode();");
/*      */     } else {
/*  545 */       p("int rc=" + hc + ";");
/*  546 */       int counter = 0;
/*  547 */       for (FieldDescriptor field : m.getFields().values()) {
/*  548 */         counter++;
/*      */ 
/*  550 */         String uname = uCamel(field.getName());
/*  551 */         String getterMethod = "get" + uname + "()";
/*  552 */         String hasMethod = "has" + uname + "()";
/*      */ 
/*  554 */         if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  555 */           getterMethod = "get" + uname + "List()";
/*      */         }
/*      */ 
/*  558 */         p("if (" + hasMethod + ") {");
/*  559 */         indent();
/*      */ 
/*  561 */         if (field.getRule() == FieldDescriptor.REPEATED_RULE)
/*  562 */           p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + ".hashCode() );");
/*  563 */         else if (field.isInteger32Type())
/*  564 */           p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + " );");
/*  565 */         else if (field.isInteger64Type())
/*  566 */           p("rc ^= ( " + uname.hashCode() + "^(new Long(" + getterMethod + ")).hashCode() );");
/*  567 */         else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/*  568 */           p("rc ^= ( " + uname.hashCode() + "^(new Double(" + getterMethod + ")).hashCode() );");
/*  569 */         else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/*  570 */           p("rc ^= ( " + uname.hashCode() + "^(new Double(" + getterMethod + ")).hashCode() );");
/*  571 */         else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/*  572 */           p("rc ^= ( " + uname.hashCode() + "^ (" + getterMethod + "? " + counter + ":-" + counter + ") );");
/*      */         else {
/*  574 */           p("rc ^= ( " + uname.hashCode() + "^" + getterMethod + ".hashCode() );");
/*      */         }
/*      */ 
/*  577 */         unindent();
/*  578 */         p("}");
/*      */       }
/*      */ 
/*  581 */       p("return rc;");
/*      */     }
/*  583 */     unindent();
/*  584 */     p("}");
/*  585 */     p("");
/*      */   }
/*      */ 
/*      */   private void generateMethodSerializedSize(MessageDescriptor m)
/*      */   {
/*  592 */     p("public int serializedSizeUnframed() {");
/*  593 */     indent();
/*  594 */     if (this.deferredDecode) {
/*  595 */       p("if (encodedForm != null) {");
/*  596 */       indent();
/*  597 */       p("return encodedForm.length;");
/*  598 */       unindent();
/*  599 */       p("}");
/*      */     }
/*  601 */     p("if (memoizedSerializedSize != -1)");
/*  602 */     p("   return memoizedSerializedSize;");
/*  603 */     p();
/*  604 */     p("int size = 0;");
/*  605 */     for (FieldDescriptor field : m.getFields().values())
/*      */     {
/*  607 */       String uname = uCamel(field.getName());
/*  608 */       String getter = "get" + uname + "()";
/*  609 */       String type = javaType(field);
/*  610 */       p("if (has" + uname + "()) {");
/*  611 */       indent();
/*      */ 
/*  613 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  614 */         p("for (" + type + " i : get" + uname + "List()) {");
/*  615 */         indent();
/*  616 */         getter = "i";
/*      */       }
/*      */ 
/*  619 */       if (field.getType() == FieldDescriptor.STRING_TYPE)
/*  620 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeStringSize(" + field.getTag() + ", " + getter + ");");
/*  621 */       else if (field.getType() == FieldDescriptor.BYTES_TYPE)
/*  622 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeBytesSize(" + field.getTag() + ", " + getter + ");");
/*  623 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/*  624 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeBoolSize(" + field.getTag() + ", " + getter + ");");
/*  625 */       else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/*  626 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeDoubleSize(" + field.getTag() + ", " + getter + ");");
/*  627 */       else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/*  628 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFloatSize(" + field.getTag() + ", " + getter + ");");
/*  629 */       else if (field.getType() == FieldDescriptor.INT32_TYPE)
/*  630 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeInt32Size(" + field.getTag() + ", " + getter + ");");
/*  631 */       else if (field.getType() == FieldDescriptor.INT64_TYPE)
/*  632 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeInt64Size(" + field.getTag() + ", " + getter + ");");
/*  633 */       else if (field.getType() == FieldDescriptor.SINT32_TYPE)
/*  634 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSInt32Size(" + field.getTag() + ", " + getter + ");");
/*  635 */       else if (field.getType() == FieldDescriptor.SINT64_TYPE)
/*  636 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSInt64Size(" + field.getTag() + ", " + getter + ");");
/*  637 */       else if (field.getType() == FieldDescriptor.UINT32_TYPE)
/*  638 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeUInt32Size(" + field.getTag() + ", " + getter + ");");
/*  639 */       else if (field.getType() == FieldDescriptor.UINT64_TYPE)
/*  640 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeUInt64Size(" + field.getTag() + ", " + getter + ");");
/*  641 */       else if (field.getType() == FieldDescriptor.FIXED32_TYPE)
/*  642 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFixed32Size(" + field.getTag() + ", " + getter + ");");
/*  643 */       else if (field.getType() == FieldDescriptor.FIXED64_TYPE)
/*  644 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeFixed64Size(" + field.getTag() + ", " + getter + ");");
/*  645 */       else if (field.getType() == FieldDescriptor.SFIXED32_TYPE)
/*  646 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSFixed32Size(" + field.getTag() + ", " + getter + ");");
/*  647 */       else if (field.getType() == FieldDescriptor.SFIXED64_TYPE)
/*  648 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeSFixed64Size(" + field.getTag() + ", " + getter + ");");
/*  649 */       else if (field.getTypeDescriptor().isEnum())
/*  650 */         p("size += org.apache.activemq.protobuf.CodedOutputStream.computeEnumSize(" + field.getTag() + ", " + getter + ".getNumber());");
/*  651 */       else if (field.getGroup() != null)
/*  652 */         p("size += computeGroupSize(" + field.getTag() + ", " + getter + ");");
/*      */       else {
/*  654 */         p("size += computeMessageSize(" + field.getTag() + ", " + getter + ");");
/*      */       }
/*  656 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  657 */         unindent();
/*  658 */         p("}");
/*      */       }
/*      */ 
/*  661 */       unindent();
/*  662 */       p("}");
/*      */     }
/*      */ 
/*  667 */     p("memoizedSerializedSize = size;");
/*  668 */     p("return size;");
/*  669 */     unindent();
/*  670 */     p("}");
/*  671 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodWriteTo(MessageDescriptor m)
/*      */   {
/*  678 */     p("public void writeUnframed(org.apache.activemq.protobuf.CodedOutputStream output) throws java.io.IOException {");
/*  679 */     indent();
/*      */ 
/*  681 */     if (this.deferredDecode) {
/*  682 */       p("if (encodedForm == null) {");
/*  683 */       indent();
/*  684 */       p("int size = serializedSizeUnframed();");
/*  685 */       p("encodedForm = output.getNextBuffer(size);");
/*  686 */       p("org.apache.activemq.protobuf.CodedOutputStream original=null;");
/*  687 */       p("if( encodedForm == null ) {");
/*  688 */       indent();
/*  689 */       p("encodedForm = new org.apache.activemq.protobuf.Buffer(new byte[size]);");
/*  690 */       p("original = output;");
/*  691 */       p("output = new org.apache.activemq.protobuf.CodedOutputStream(encodedForm);");
/*  692 */       unindent();
/*  693 */       p("}");
/*      */     }
/*      */ 
/*  697 */     for (FieldDescriptor field : m.getFields().values()) {
/*  698 */       String uname = uCamel(field.getName());
/*  699 */       String getter = "get" + uname + "()";
/*  700 */       String type = javaType(field);
/*  701 */       p("if (has" + uname + "()) {");
/*  702 */       indent();
/*      */ 
/*  704 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  705 */         p("for (" + type + " i : get" + uname + "List()) {");
/*  706 */         indent();
/*  707 */         getter = "i";
/*      */       }
/*      */ 
/*  710 */       if (field.getType() == FieldDescriptor.STRING_TYPE)
/*  711 */         p("output.writeString(" + field.getTag() + ", " + getter + ");");
/*  712 */       else if (field.getType() == FieldDescriptor.BYTES_TYPE)
/*  713 */         p("output.writeBytes(" + field.getTag() + ", " + getter + ");");
/*  714 */       else if (field.getType() == FieldDescriptor.BOOL_TYPE)
/*  715 */         p("output.writeBool(" + field.getTag() + ", " + getter + ");");
/*  716 */       else if (field.getType() == FieldDescriptor.DOUBLE_TYPE)
/*  717 */         p("output.writeDouble(" + field.getTag() + ", " + getter + ");");
/*  718 */       else if (field.getType() == FieldDescriptor.FLOAT_TYPE)
/*  719 */         p("output.writeFloat(" + field.getTag() + ", " + getter + ");");
/*  720 */       else if (field.getType() == FieldDescriptor.INT32_TYPE)
/*  721 */         p("output.writeInt32(" + field.getTag() + ", " + getter + ");");
/*  722 */       else if (field.getType() == FieldDescriptor.INT64_TYPE)
/*  723 */         p("output.writeInt64(" + field.getTag() + ", " + getter + ");");
/*  724 */       else if (field.getType() == FieldDescriptor.SINT32_TYPE)
/*  725 */         p("output.writeSInt32(" + field.getTag() + ", " + getter + ");");
/*  726 */       else if (field.getType() == FieldDescriptor.SINT64_TYPE)
/*  727 */         p("output.writeSInt64(" + field.getTag() + ", " + getter + ");");
/*  728 */       else if (field.getType() == FieldDescriptor.UINT32_TYPE)
/*  729 */         p("output.writeUInt32(" + field.getTag() + ", " + getter + ");");
/*  730 */       else if (field.getType() == FieldDescriptor.UINT64_TYPE)
/*  731 */         p("output.writeUInt64(" + field.getTag() + ", " + getter + ");");
/*  732 */       else if (field.getType() == FieldDescriptor.FIXED32_TYPE)
/*  733 */         p("output.writeFixed32(" + field.getTag() + ", " + getter + ");");
/*  734 */       else if (field.getType() == FieldDescriptor.FIXED64_TYPE)
/*  735 */         p("output.writeFixed64(" + field.getTag() + ", " + getter + ");");
/*  736 */       else if (field.getType() == FieldDescriptor.SFIXED32_TYPE)
/*  737 */         p("output.writeSFixed32(" + field.getTag() + ", " + getter + ");");
/*  738 */       else if (field.getType() == FieldDescriptor.SFIXED64_TYPE)
/*  739 */         p("output.writeSFixed64(" + field.getTag() + ", " + getter + ");");
/*  740 */       else if (field.getTypeDescriptor().isEnum())
/*  741 */         p("output.writeEnum(" + field.getTag() + ", " + getter + ".getNumber());");
/*  742 */       else if (field.getGroup() != null)
/*  743 */         p("writeGroup(output, " + field.getTag() + ", " + getter + ");");
/*      */       else {
/*  745 */         p("writeMessage(output, " + field.getTag() + ", " + getter + ");");
/*      */       }
/*      */ 
/*  748 */       if (field.getRule() == FieldDescriptor.REPEATED_RULE) {
/*  749 */         unindent();
/*  750 */         p("}");
/*      */       }
/*      */ 
/*  753 */       unindent();
/*  754 */       p("}");
/*      */     }
/*      */ 
/*  757 */     if (this.deferredDecode) {
/*  758 */       p("if( original !=null ) {");
/*  759 */       indent();
/*  760 */       p("output.checkNoSpaceLeft();");
/*  761 */       p("output = original;");
/*  762 */       p("output.writeRawBytes(encodedForm);");
/*  763 */       unindent();
/*  764 */       p("}");
/*  765 */       unindent();
/*  766 */       p("} else {");
/*  767 */       indent();
/*  768 */       p("output.writeRawBytes(encodedForm);");
/*  769 */       unindent();
/*  770 */       p("}");
/*      */     }
/*      */ 
/*  773 */     unindent();
/*  774 */     p("}");
/*  775 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodMergeFromStream(MessageDescriptor m, String className)
/*      */   {
/*  783 */     p("public " + className + " mergeUnframed(org.apache.activemq.protobuf.CodedInputStream input) throws java.io.IOException {");
/*  784 */     indent();
/*      */ 
/*  786 */     p("while (true) {");
/*  787 */     indent();
/*      */ 
/*  789 */     p("int tag = input.readTag();");
/*  790 */     p("if ((tag & 0x07) == 4) {");
/*  791 */     p("   return this;");
/*  792 */     p("}");
/*      */ 
/*  794 */     p("switch (tag) {");
/*  795 */     p("case 0:");
/*  796 */     p("   return this;");
/*  797 */     p("default: {");
/*      */ 
/*  799 */     p("   break;");
/*  800 */     p("}");
/*      */ 
/*  802 */     for (FieldDescriptor field : m.getFields().values()) {
/*  803 */       String uname = uCamel(field.getName());
/*  804 */       String setter = "set" + uname;
/*  805 */       boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*  806 */       if (repeated) {
/*  807 */         setter = "get" + uname + "List().add";
/*      */       }
/*  809 */       if (field.getType() == FieldDescriptor.STRING_TYPE) {
/*  810 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/*      */ 
/*  813 */         indent();
/*  814 */         p(setter + "(input.readString());");
/*  815 */       } else if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/*  816 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/*      */ 
/*  819 */         indent();
/*  820 */         p(setter + "(input.readBytes());");
/*  821 */       } else if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/*  822 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  824 */         indent();
/*  825 */         p(setter + "(input.readBool());");
/*  826 */       } else if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/*  827 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/*  829 */         indent();
/*  830 */         p(setter + "(input.readDouble());");
/*  831 */       } else if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/*  832 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/*  834 */         indent();
/*  835 */         p(setter + "(input.readFloat());");
/*  836 */       } else if (field.getType() == FieldDescriptor.INT32_TYPE) {
/*  837 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  839 */         indent();
/*  840 */         p(setter + "(input.readInt32());");
/*  841 */       } else if (field.getType() == FieldDescriptor.INT64_TYPE) {
/*  842 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  844 */         indent();
/*  845 */         p(setter + "(input.readInt64());");
/*  846 */       } else if (field.getType() == FieldDescriptor.SINT32_TYPE) {
/*  847 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  849 */         indent();
/*  850 */         p(setter + "(input.readSInt32());");
/*  851 */       } else if (field.getType() == FieldDescriptor.SINT64_TYPE) {
/*  852 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  854 */         indent();
/*  855 */         p(setter + "(input.readSInt64());");
/*  856 */       } else if (field.getType() == FieldDescriptor.UINT32_TYPE) {
/*  857 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  859 */         indent();
/*  860 */         p(setter + "(input.readUInt32());");
/*  861 */       } else if (field.getType() == FieldDescriptor.UINT64_TYPE) {
/*  862 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  864 */         indent();
/*  865 */         p(setter + "(input.readUInt64());");
/*  866 */       } else if (field.getType() == FieldDescriptor.FIXED32_TYPE) {
/*  867 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/*  869 */         indent();
/*  870 */         p(setter + "(input.readFixed32());");
/*  871 */       } else if (field.getType() == FieldDescriptor.FIXED64_TYPE) {
/*  872 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/*  874 */         indent();
/*  875 */         p(setter + "(input.readFixed64());");
/*  876 */       } else if (field.getType() == FieldDescriptor.SFIXED32_TYPE) {
/*  877 */         p("case " + WireFormat.makeTag(field.getTag(), 5) + ":");
/*      */ 
/*  879 */         indent();
/*  880 */         p(setter + "(input.readSFixed32());");
/*  881 */       } else if (field.getType() == FieldDescriptor.SFIXED64_TYPE) {
/*  882 */         p("case " + WireFormat.makeTag(field.getTag(), 1) + ":");
/*      */ 
/*  884 */         indent();
/*  885 */         p(setter + "(input.readSFixed64());");
/*  886 */       } else if (field.getTypeDescriptor().isEnum()) {
/*  887 */         p("case " + WireFormat.makeTag(field.getTag(), 0) + ":");
/*      */ 
/*  889 */         indent();
/*  890 */         String type = javaType(field);
/*  891 */         p("{");
/*  892 */         indent();
/*  893 */         p("int t = input.readEnum();");
/*  894 */         p("" + type + " value = " + type + ".valueOf(t);");
/*  895 */         p("if( value !=null ) {");
/*  896 */         indent();
/*  897 */         p(setter + "(value);");
/*  898 */         unindent();
/*  899 */         p("}");
/*      */ 
/*  902 */         unindent();
/*  903 */         p("}");
/*      */       }
/*  905 */       else if (field.getGroup() != null) {
/*  906 */         p("case " + WireFormat.makeTag(field.getTag(), 3) + ":");
/*      */ 
/*  909 */         indent();
/*  910 */         String type = javaType(field);
/*  911 */         if (repeated) {
/*  912 */           p(setter + "(readGroup(input, " + field.getTag() + ", new " + type + "()));");
/*      */         }
/*      */         else {
/*  915 */           p("if (has" + uname + "()) {");
/*  916 */           indent();
/*  917 */           p("readGroup(input, " + field.getTag() + ", get" + uname + "());");
/*      */ 
/*  919 */           unindent();
/*  920 */           p("} else {");
/*  921 */           indent();
/*  922 */           p(setter + "(readGroup(input, " + field.getTag() + ",new " + type + "()));");
/*      */ 
/*  924 */           unindent();
/*  925 */           p("}");
/*      */         }
/*  927 */         p("");
/*      */       } else {
/*  929 */         p("case " + WireFormat.makeTag(field.getTag(), 2) + ":");
/*      */ 
/*  932 */         indent();
/*  933 */         String type = javaType(field);
/*  934 */         if (repeated) {
/*  935 */           p(setter + "(new " + type + "().mergeFramed(input));");
/*      */         }
/*      */         else {
/*  938 */           p("if (has" + uname + "()) {");
/*  939 */           indent();
/*  940 */           p("get" + uname + "().mergeFramed(input);");
/*  941 */           unindent();
/*  942 */           p("} else {");
/*  943 */           indent();
/*  944 */           p(setter + "(new " + type + "().mergeFramed(input));");
/*      */ 
/*  946 */           unindent();
/*  947 */           p("}");
/*      */         }
/*      */       }
/*  950 */       p("break;");
/*  951 */       unindent();
/*      */     }
/*  953 */     p("}");
/*      */ 
/*  955 */     unindent();
/*  956 */     p("}");
/*      */ 
/*  958 */     unindent();
/*  959 */     p("}");
/*      */   }
/*      */ 
/*      */   private void generateMethodMergeFromBean(MessageDescriptor m, String className)
/*      */   {
/*  967 */     p("public " + className + " mergeFrom(" + className + " other) {");
/*  968 */     indent();
/*  969 */     for (FieldDescriptor field : m.getFields().values()) {
/*  970 */       String uname = uCamel(field.getName());
/*  971 */       p("if (other.has" + uname + "()) {");
/*  972 */       indent();
/*      */ 
/*  974 */       if ((field.isScalarType()) || (field.getTypeDescriptor().isEnum())) {
/*  975 */         if (field.isRepeated())
/*  976 */           p("get" + uname + "List().addAll(other.get" + uname + "List());");
/*      */         else
/*  978 */           p("set" + uname + "(other.get" + uname + "());");
/*      */       }
/*      */       else
/*      */       {
/*  982 */         String type = javaType(field);
/*      */ 
/*  984 */         if (field.isRepeated()) {
/*  985 */           p("for(" + type + " element: other.get" + uname + "List() ) {");
/*  986 */           indent();
/*  987 */           p("get" + uname + "List().add(element.clone());");
/*  988 */           unindent();
/*  989 */           p("}");
/*      */         } else {
/*  991 */           p("if (has" + uname + "()) {");
/*  992 */           indent();
/*  993 */           p("get" + uname + "().mergeFrom(other.get" + uname + "());");
/*  994 */           unindent();
/*  995 */           p("} else {");
/*  996 */           indent();
/*  997 */           p("set" + uname + "(other.get" + uname + "().clone());");
/*  998 */           unindent();
/*  999 */           p("}");
/*      */         }
/*      */       }
/* 1002 */       unindent();
/* 1003 */       p("}");
/*      */     }
/* 1005 */     p("return this;");
/* 1006 */     unindent();
/* 1007 */     p("}");
/* 1008 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodClear(MessageDescriptor m)
/*      */   {
/* 1015 */     p("public void clear() {");
/* 1016 */     indent();
/* 1017 */     p("super.clear();");
/* 1018 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1019 */       String uname = uCamel(field.getName());
/* 1020 */       p("clear" + uname + "();");
/*      */     }
/* 1022 */     unindent();
/* 1023 */     p("}");
/* 1024 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodAssertInitialized(MessageDescriptor m, String className)
/*      */   {
/* 1029 */     p("public java.util.ArrayList<String> missingFields() {");
/* 1030 */     indent();
/* 1031 */     p("java.util.ArrayList<String> missingFields = super.missingFields();");
/*      */ 
/* 1033 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1034 */       String uname = uCamel(field.getName());
/* 1035 */       if (field.isRequired()) {
/* 1036 */         p("if(  !has" + uname + "() ) {");
/* 1037 */         indent();
/* 1038 */         p("missingFields.add(\"" + field.getName() + "\");");
/* 1039 */         unindent();
/* 1040 */         p("}");
/*      */       }
/*      */     }
/*      */ 
/* 1044 */     if (!this.deferredDecode) {
/* 1045 */       for (FieldDescriptor field : m.getFields().values()) {
/* 1046 */         if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1047 */           String uname = uCamel(field.getName());
/* 1048 */           p("if( has" + uname + "() ) {");
/* 1049 */           indent();
/* 1050 */           if (!field.isRepeated()) {
/* 1051 */             p("try {");
/* 1052 */             indent();
/* 1053 */             p("get" + uname + "().assertInitialized();");
/* 1054 */             unindent();
/* 1055 */             p("} catch (org.apache.activemq.protobuf.UninitializedMessageException e){");
/* 1056 */             indent();
/* 1057 */             p("missingFields.addAll(prefix(e.getMissingFields(),\"" + field.getName() + ".\"));");
/* 1058 */             unindent();
/* 1059 */             p("}");
/*      */           } else {
/* 1061 */             String type = javaCollectionType(field);
/* 1062 */             p("java.util.List<" + type + "> l = get" + uname + "List();");
/* 1063 */             p("for( int i=0; i < l.size(); i++ ) {");
/* 1064 */             indent();
/* 1065 */             p("try {");
/* 1066 */             indent();
/* 1067 */             p("l.get(i).assertInitialized();");
/* 1068 */             unindent();
/* 1069 */             p("} catch (org.apache.activemq.protobuf.UninitializedMessageException e){");
/* 1070 */             indent();
/* 1071 */             p("missingFields.addAll(prefix(e.getMissingFields(),\"" + field.getName() + "[\"+i+\"]\"));");
/* 1072 */             unindent();
/* 1073 */             p("}");
/* 1074 */             unindent();
/* 1075 */             p("}");
/*      */           }
/* 1077 */           unindent();
/* 1078 */           p("}");
/*      */         }
/*      */       }
/*      */     }
/* 1082 */     p("return missingFields;");
/* 1083 */     unindent();
/* 1084 */     p("}");
/* 1085 */     p();
/*      */   }
/*      */ 
/*      */   private void generateMethodToString(MessageDescriptor m)
/*      */   {
/* 1090 */     p("public String toString() {");
/* 1091 */     indent();
/* 1092 */     p("return toString(new java.lang.StringBuilder(), \"\").toString();");
/* 1093 */     unindent();
/* 1094 */     p("}");
/* 1095 */     p();
/*      */ 
/* 1097 */     p("public java.lang.StringBuilder toString(java.lang.StringBuilder sb, String prefix) {");
/* 1098 */     indent();
/*      */ 
/* 1100 */     if (this.deferredDecode) {
/* 1101 */       p("load();");
/*      */     }
/* 1103 */     for (FieldDescriptor field : m.getFields().values()) {
/* 1104 */       String uname = uCamel(field.getName());
/* 1105 */       p("if(  has" + uname + "() ) {");
/* 1106 */       indent();
/* 1107 */       if (field.isRepeated()) {
/* 1108 */         String type = javaCollectionType(field);
/* 1109 */         p("java.util.List<" + type + "> l = get" + uname + "List();");
/* 1110 */         p("for( int i=0; i < l.size(); i++ ) {");
/* 1111 */         indent();
/* 1112 */         if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1113 */           p("sb.append(prefix+\"" + field.getName() + "[\"+i+\"] {\\n\");");
/* 1114 */           p("l.get(i).toString(sb, prefix+\"  \");");
/* 1115 */           p("sb.append(prefix+\"}\\n\");");
/*      */         } else {
/* 1117 */           p("sb.append(prefix+\"" + field.getName() + "[\"+i+\"]: \");");
/* 1118 */           p("sb.append(l.get(i));");
/* 1119 */           p("sb.append(\"\\n\");");
/*      */         }
/* 1121 */         unindent();
/* 1122 */         p("}");
/*      */       }
/* 1124 */       else if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1125 */         p("sb.append(prefix+\"" + field.getName() + " {\\n\");");
/* 1126 */         p("get" + uname + "().toString(sb, prefix+\"  \");");
/* 1127 */         p("sb.append(prefix+\"}\\n\");");
/*      */       } else {
/* 1129 */         p("sb.append(prefix+\"" + field.getName() + ": \");");
/* 1130 */         p("sb.append(get" + uname + "());");
/* 1131 */         p("sb.append(\"\\n\");");
/*      */       }
/*      */ 
/* 1134 */       unindent();
/* 1135 */       p("}");
/*      */     }
/*      */ 
/* 1139 */     p("return sb;");
/* 1140 */     unindent();
/* 1141 */     p("}");
/* 1142 */     p();
/*      */   }
/*      */ 
/*      */   private void generateFieldAccessor(FieldDescriptor field)
/*      */   {
/* 1152 */     String lname = lCamel(field.getName());
/* 1153 */     String uname = uCamel(field.getName());
/* 1154 */     String type = field.getRule() == FieldDescriptor.REPEATED_RULE ? javaCollectionType(field) : javaType(field);
/* 1155 */     String typeDefault = javaTypeDefault(field);
/* 1156 */     boolean primitive = (field.getTypeDescriptor() == null) || (field.getTypeDescriptor().isEnum());
/* 1157 */     boolean repeated = field.getRule() == FieldDescriptor.REPEATED_RULE;
/*      */ 
/* 1160 */     p("// " + field.getRule() + " " + field.getType() + " " + field.getName() + " = " + field.getTag() + ";");
/*      */ 
/* 1162 */     if (repeated) {
/* 1163 */       p("private java.util.List<" + type + "> f_" + lname + ";");
/* 1164 */       p();
/*      */ 
/* 1167 */       p("public boolean has" + uname + "() {");
/* 1168 */       indent();
/* 1169 */       if (this.deferredDecode) {
/* 1170 */         p("load();");
/*      */       }
/* 1172 */       p("return this.f_" + lname + "!=null && !this.f_" + lname + ".isEmpty();");
/* 1173 */       unindent();
/* 1174 */       p("}");
/* 1175 */       p();
/*      */ 
/* 1177 */       p("public java.util.List<" + type + "> get" + uname + "List() {");
/* 1178 */       indent();
/* 1179 */       if (this.deferredDecode) {
/* 1180 */         p("load();");
/*      */       }
/* 1182 */       p("if( this.f_" + lname + " == null ) {");
/* 1183 */       indent();
/* 1184 */       p("this.f_" + lname + " = new java.util.ArrayList<" + type + ">();");
/* 1185 */       unindent();
/* 1186 */       p("}");
/* 1187 */       p("return this.f_" + lname + ";");
/* 1188 */       unindent();
/* 1189 */       p("}");
/* 1190 */       p();
/*      */ 
/* 1192 */       p("public T set" + uname + "List(java.util.List<" + type + "> " + lname + ") {");
/* 1193 */       indent();
/* 1194 */       p("loadAndClear();");
/* 1195 */       p("this.f_" + lname + " = " + lname + ";");
/* 1196 */       p("return (T)this;");
/* 1197 */       unindent();
/* 1198 */       p("}");
/* 1199 */       p();
/*      */ 
/* 1201 */       p("public int get" + uname + "Count() {");
/* 1202 */       indent();
/* 1203 */       if (this.deferredDecode) {
/* 1204 */         p("load();");
/*      */       }
/* 1206 */       p("if( this.f_" + lname + " == null ) {");
/* 1207 */       indent();
/* 1208 */       p("return 0;");
/* 1209 */       unindent();
/* 1210 */       p("}");
/* 1211 */       p("return this.f_" + lname + ".size();");
/* 1212 */       unindent();
/* 1213 */       p("}");
/* 1214 */       p();
/*      */ 
/* 1216 */       p("public " + type + " get" + uname + "(int index) {");
/* 1217 */       indent();
/* 1218 */       if (this.deferredDecode) {
/* 1219 */         p("load();");
/*      */       }
/* 1221 */       p("if( this.f_" + lname + " == null ) {");
/* 1222 */       indent();
/* 1223 */       p("return null;");
/* 1224 */       unindent();
/* 1225 */       p("}");
/* 1226 */       p("return this.f_" + lname + ".get(index);");
/* 1227 */       unindent();
/* 1228 */       p("}");
/* 1229 */       p();
/*      */ 
/* 1231 */       p("public T set" + uname + "(int index, " + type + " value) {");
/* 1232 */       indent();
/* 1233 */       p("loadAndClear();");
/* 1234 */       p("get" + uname + "List().set(index, value);");
/* 1235 */       p("return (T)this;");
/* 1236 */       unindent();
/* 1237 */       p("}");
/* 1238 */       p();
/*      */ 
/* 1240 */       p("public T add" + uname + "(" + type + " value) {");
/* 1241 */       indent();
/* 1242 */       p("loadAndClear();");
/* 1243 */       p("get" + uname + "List().add(value);");
/* 1244 */       p("return (T)this;");
/* 1245 */       unindent();
/* 1246 */       p("}");
/* 1247 */       p();
/*      */ 
/* 1249 */       p("public T addAll" + uname + "(java.lang.Iterable<? extends " + type + "> collection) {");
/* 1250 */       indent();
/* 1251 */       p("loadAndClear();");
/* 1252 */       p("super.addAll(collection, get" + uname + "List());");
/* 1253 */       p("return (T)this;");
/* 1254 */       unindent();
/* 1255 */       p("}");
/* 1256 */       p();
/*      */ 
/* 1258 */       p("public void clear" + uname + "() {");
/* 1259 */       indent();
/* 1260 */       p("loadAndClear();");
/* 1261 */       p("this.f_" + lname + " = null;");
/* 1262 */       unindent();
/* 1263 */       p("}");
/* 1264 */       p();
/*      */     }
/*      */     else
/*      */     {
/* 1268 */       p("private " + type + " f_" + lname + " = " + typeDefault + ";");
/* 1269 */       if (primitive) {
/* 1270 */         p("private boolean b_" + lname + ";");
/*      */       }
/* 1272 */       p();
/*      */ 
/* 1275 */       p("public boolean has" + uname + "() {");
/* 1276 */       indent();
/* 1277 */       if (this.deferredDecode) {
/* 1278 */         p("load();");
/*      */       }
/* 1280 */       if (primitive)
/* 1281 */         p("return this.b_" + lname + ";");
/*      */       else {
/* 1283 */         p("return this.f_" + lname + "!=null;");
/*      */       }
/* 1285 */       unindent();
/* 1286 */       p("}");
/* 1287 */       p();
/*      */ 
/* 1289 */       p("public " + type + " get" + uname + "() {");
/* 1290 */       indent();
/* 1291 */       if (this.deferredDecode) {
/* 1292 */         p("load();");
/*      */       }
/* 1294 */       if ((field.getTypeDescriptor() != null) && (!field.getTypeDescriptor().isEnum())) {
/* 1295 */         p("if( this.f_" + lname + " == null ) {");
/* 1296 */         indent();
/* 1297 */         p("this.f_" + lname + " = new " + type + "();");
/* 1298 */         unindent();
/* 1299 */         p("}");
/*      */       }
/* 1301 */       p("return this.f_" + lname + ";");
/* 1302 */       unindent();
/* 1303 */       p("}");
/* 1304 */       p();
/*      */ 
/* 1306 */       p("public T set" + uname + "(" + type + " " + lname + ") {");
/* 1307 */       indent();
/* 1308 */       p("loadAndClear();");
/* 1309 */       if (primitive) {
/* 1310 */         if ((this.auto_clear_optional_fields) && (field.isOptional())) {
/* 1311 */           if ((field.isStringType()) && (!"null".equals(typeDefault)))
/* 1312 */             p("this.b_" + lname + " = (" + lname + " != " + typeDefault + ");");
/*      */           else
/* 1314 */             p("this.b_" + lname + " = (" + lname + " != " + typeDefault + ");");
/*      */         }
/*      */         else {
/* 1317 */           p("this.b_" + lname + " = true;");
/*      */         }
/*      */       }
/* 1320 */       p("this.f_" + lname + " = " + lname + ";");
/* 1321 */       p("return (T)this;");
/* 1322 */       unindent();
/* 1323 */       p("}");
/* 1324 */       p();
/*      */ 
/* 1326 */       p("public void clear" + uname + "() {");
/* 1327 */       indent();
/* 1328 */       p("loadAndClear();");
/* 1329 */       if (primitive) {
/* 1330 */         p("this.b_" + lname + " = false;");
/*      */       }
/* 1332 */       p("this.f_" + lname + " = " + typeDefault + ";");
/* 1333 */       unindent();
/* 1334 */       p("}");
/* 1335 */       p();
/*      */     }
/*      */   }
/*      */ 
/*      */   private String javaTypeDefault(FieldDescriptor field)
/*      */   {
/* 1341 */     OptionDescriptor defaultOption = (OptionDescriptor)field.getOptions().get("default");
/* 1342 */     if (defaultOption != null) {
/* 1343 */       if (field.isStringType())
/* 1344 */         return asJavaString(defaultOption.getValue());
/* 1345 */       if (field.getType() == FieldDescriptor.BYTES_TYPE)
/* 1346 */         return "new org.apache.activemq.protobuf.Buffer(" + asJavaString(defaultOption.getValue()) + ")";
/* 1347 */       if (field.isInteger32Type())
/*      */       {
/*      */         int v;
/*      */         int v;
/* 1349 */         if (field.getType() == FieldDescriptor.UINT32_TYPE)
/* 1350 */           v = TextFormat.parseUInt32(defaultOption.getValue());
/*      */         else {
/* 1352 */           v = TextFormat.parseInt32(defaultOption.getValue());
/*      */         }
/* 1354 */         return "" + v;
/* 1355 */       }if (field.isInteger64Type())
/*      */       {
/*      */         long v;
/*      */         long v;
/* 1357 */         if (field.getType() == FieldDescriptor.UINT64_TYPE)
/* 1358 */           v = TextFormat.parseUInt64(defaultOption.getValue());
/*      */         else {
/* 1360 */           v = TextFormat.parseInt64(defaultOption.getValue());
/*      */         }
/* 1362 */         return "" + v + "l";
/* 1363 */       }if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1364 */         double v = Double.valueOf(defaultOption.getValue()).doubleValue();
/* 1365 */         return "" + v + "d";
/* 1366 */       }if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1367 */         float v = Float.valueOf(defaultOption.getValue()).floatValue();
/* 1368 */         return "" + v + "f";
/* 1369 */       }if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1370 */         boolean v = Boolean.valueOf(defaultOption.getValue()).booleanValue();
/* 1371 */         return "" + v;
/* 1372 */       }if ((field.getTypeDescriptor() != null) && (field.getTypeDescriptor().isEnum())) {
/* 1373 */         return javaType(field) + "." + defaultOption.getValue();
/*      */       }
/* 1375 */       return defaultOption.getValue();
/*      */     }
/* 1377 */     if (field.isNumberType()) {
/* 1378 */       return "0";
/*      */     }
/* 1380 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1381 */       return "false";
/*      */     }
/* 1383 */     return "null";
/*      */   }
/*      */ 
/*      */   private String asJavaString(String value)
/*      */   {
/* 1390 */     StringBuilder sb = new StringBuilder(value.length() + 2);
/* 1391 */     sb.append("\"");
/* 1392 */     for (int i = 0; i < value.length(); i++)
/*      */     {
/* 1394 */       char b = value.charAt(i);
/* 1395 */       switch (b) {
/*      */       case '\b':
/* 1397 */         sb.append("\\b"); break;
/*      */       case '\f':
/* 1398 */         sb.append("\\f"); break;
/*      */       case '\n':
/* 1399 */         sb.append("\\n"); break;
/*      */       case '\r':
/* 1400 */         sb.append("\\r"); break;
/*      */       case '\t':
/* 1401 */         sb.append("\\t"); break;
/*      */       case '\\':
/* 1402 */         sb.append("\\\\"); break;
/*      */       case '\'':
/* 1403 */         sb.append("\\'"); break;
/*      */       case '"':
/* 1404 */         sb.append("\\\""); break;
/*      */       default:
/* 1406 */         if ((b >= ' ') && (b < 'Z')) {
/* 1407 */           sb.append(b);
/*      */         } else {
/* 1409 */           sb.append("\\u");
/* 1410 */           sb.append(HEX_TABLE[(b >>> '\f' & 0xF)]);
/* 1411 */           sb.append(HEX_TABLE[(b >>> '\b' & 0xF)]);
/* 1412 */           sb.append(HEX_TABLE[(b >>> '\004' & 0xF)]);
/* 1413 */           sb.append(HEX_TABLE[(b & 0xF)]);
/*      */         }
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/* 1419 */     sb.append("\"");
/* 1420 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private void generateEnum(EnumDescriptor ed) {
/* 1424 */     String uname = uCamel(ed.getName());
/*      */ 
/* 1426 */     String staticOption = "static ";
/* 1427 */     if ((this.multipleFiles) && (ed.getParent() == null)) {
/* 1428 */       staticOption = "";
/*      */     }
/*      */ 
/* 1432 */     p();
/* 1433 */     p("public " + staticOption + "enum " + uname + " {");
/* 1434 */     indent();
/*      */ 
/* 1437 */     p();
/* 1438 */     int counter = 0;
/* 1439 */     for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 1440 */       boolean last = counter + 1 == ed.getFields().size();
/* 1441 */       p(field.getName() + "(\"" + field.getName() + "\", " + field.getValue() + ")" + (last ? ";" : ","));
/* 1442 */       counter++;
/*      */     }
/* 1444 */     p();
/* 1445 */     p("private final String name;");
/* 1446 */     p("private final int value;");
/* 1447 */     p();
/* 1448 */     p("private " + uname + "(String name, int value) {");
/* 1449 */     p("   this.name = name;");
/* 1450 */     p("   this.value = value;");
/* 1451 */     p("}");
/* 1452 */     p();
/* 1453 */     p("public final int getNumber() {");
/* 1454 */     p("   return value;");
/* 1455 */     p("}");
/* 1456 */     p();
/* 1457 */     p("public final String toString() {");
/* 1458 */     p("   return name;");
/* 1459 */     p("}");
/* 1460 */     p();
/* 1461 */     p("public static " + uname + " valueOf(int value) {");
/* 1462 */     p("   switch (value) {");
/*      */ 
/* 1466 */     HashSet values = new HashSet();
/* 1467 */     for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 1468 */       if (!values.contains(Integer.valueOf(field.getValue()))) {
/* 1469 */         p("   case " + field.getValue() + ":");
/* 1470 */         p("      return " + field.getName() + ";");
/* 1471 */         values.add(Integer.valueOf(field.getValue()));
/*      */       }
/*      */     }
/*      */ 
/* 1475 */     p("   default:");
/* 1476 */     p("      return null;");
/* 1477 */     p("   }");
/* 1478 */     p("}");
/* 1479 */     p();
/*      */ 
/* 1482 */     String createMessage = getOption(ed.getOptions(), "java_create_message", null);
/* 1483 */     if ("true".equals(createMessage)) {
/* 1484 */       p("public org.apache.activemq.protobuf.Message createMessage() {");
/* 1485 */       indent();
/* 1486 */       p("switch (this) {");
/* 1487 */       indent();
/* 1488 */       for (EnumFieldDescriptor field : ed.getFields().values()) {
/* 1489 */         p("case " + field.getName() + ":");
/* 1490 */         String type = constantToUCamelCase(field.getName());
/* 1491 */         p("   return new " + type + "();");
/*      */       }
/* 1493 */       p("default:");
/* 1494 */       p("   return null;");
/* 1495 */       unindent();
/* 1496 */       p("}");
/* 1497 */       unindent();
/* 1498 */       p("}");
/* 1499 */       p();
/*      */     }
/*      */ 
/* 1502 */     unindent();
/* 1503 */     p("}");
/* 1504 */     p();
/*      */   }
/*      */ 
/*      */   private String javaCollectionType(FieldDescriptor field)
/*      */   {
/* 1510 */     if (field.isInteger32Type()) {
/* 1511 */       return "java.lang.Integer";
/*      */     }
/* 1513 */     if (field.isInteger64Type()) {
/* 1514 */       return "java.lang.Long";
/*      */     }
/* 1516 */     if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1517 */       return "java.lang.Double";
/*      */     }
/* 1519 */     if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1520 */       return "java.lang.Float";
/*      */     }
/* 1522 */     if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1523 */       return "java.lang.String";
/*      */     }
/* 1525 */     if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1526 */       return "org.apache.activemq.protobuf.Buffer";
/*      */     }
/* 1528 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1529 */       return "java.lang.Boolean";
/*      */     }
/*      */ 
/* 1532 */     TypeDescriptor descriptor = field.getTypeDescriptor();
/* 1533 */     return javaType(descriptor);
/*      */   }
/*      */ 
/*      */   private String javaType(FieldDescriptor field) {
/* 1537 */     if (field.isInteger32Type()) {
/* 1538 */       return "int";
/*      */     }
/* 1540 */     if (field.isInteger64Type()) {
/* 1541 */       return "long";
/*      */     }
/* 1543 */     if (field.getType() == FieldDescriptor.DOUBLE_TYPE) {
/* 1544 */       return "double";
/*      */     }
/* 1546 */     if (field.getType() == FieldDescriptor.FLOAT_TYPE) {
/* 1547 */       return "float";
/*      */     }
/* 1549 */     if (field.getType() == FieldDescriptor.STRING_TYPE) {
/* 1550 */       return "java.lang.String";
/*      */     }
/* 1552 */     if (field.getType() == FieldDescriptor.BYTES_TYPE) {
/* 1553 */       return "org.apache.activemq.protobuf.Buffer";
/*      */     }
/* 1555 */     if (field.getType() == FieldDescriptor.BOOL_TYPE) {
/* 1556 */       return "boolean";
/*      */     }
/*      */ 
/* 1559 */     TypeDescriptor descriptor = field.getTypeDescriptor();
/* 1560 */     return javaType(descriptor);
/*      */   }
/*      */ 
/*      */   private String javaType(TypeDescriptor descriptor) {
/* 1564 */     ProtoDescriptor p = descriptor.getProtoDescriptor();
/* 1565 */     if (p != this.proto)
/*      */     {
/* 1567 */       String othePackage = javaPackage(p);
/* 1568 */       if (equals(othePackage, javaPackage(this.proto))) {
/* 1569 */         return javaClassName(p) + "." + descriptor.getQName();
/*      */       }
/*      */ 
/* 1572 */       return othePackage + "." + javaClassName(p) + "." + descriptor.getQName();
/*      */     }
/* 1574 */     return descriptor.getQName();
/*      */   }
/*      */ 
/*      */   private boolean equals(String o1, String o2) {
/* 1578 */     if (o1 == o2)
/* 1579 */       return true;
/* 1580 */     if ((o1 == null) || (o2 == null))
/* 1581 */       return false;
/* 1582 */     return o1.equals(o2);
/*      */   }
/*      */ 
/*      */   private String javaClassName(ProtoDescriptor proto) {
/* 1586 */     return getOption(proto.getOptions(), "java_outer_classname", uCamel(removeFileExtension(proto.getName())));
/*      */   }
/*      */ 
/*      */   private boolean isMultipleFilesEnabled(ProtoDescriptor proto) {
/* 1590 */     return "true".equals(getOption(proto.getOptions(), "java_multiple_files", "false"));
/*      */   }
/*      */ 
/*      */   private String javaPackage(ProtoDescriptor proto)
/*      */   {
/* 1595 */     String name = proto.getPackageName();
/* 1596 */     if (name != null) {
/* 1597 */       name = name.replace('-', '.');
/* 1598 */       name = name.replace('/', '.');
/*      */     }
/* 1600 */     return getOption(proto.getOptions(), "java_package", name);
/*      */   }
/*      */ 
/*      */   private void indent()
/*      */   {
/* 1609 */     this.indent += 1;
/*      */   }
/*      */ 
/*      */   private void unindent() {
/* 1613 */     this.indent -= 1;
/*      */   }
/*      */ 
/*      */   private void p(String line)
/*      */   {
/* 1618 */     for (int i = 0; i < this.indent; i++) {
/* 1619 */       this.w.print("   ");
/*      */     }
/*      */ 
/* 1622 */     this.w.println(line);
/*      */   }
/*      */ 
/*      */   private void p() {
/* 1626 */     this.w.println();
/*      */   }
/*      */ 
/*      */   private String getOption(Map<String, OptionDescriptor> options, String optionName, String defaultValue) {
/* 1630 */     OptionDescriptor optionDescriptor = (OptionDescriptor)options.get(optionName);
/* 1631 */     if (optionDescriptor == null) {
/* 1632 */       return defaultValue;
/*      */     }
/* 1634 */     return optionDescriptor.getValue();
/*      */   }
/*      */ 
/*      */   private static String removeFileExtension(String name) {
/* 1638 */     return name.replaceAll("\\..*", "");
/*      */   }
/*      */ 
/*      */   private static String uCamel(String name) {
/* 1642 */     boolean upNext = true;
/* 1643 */     StringBuilder sb = new StringBuilder();
/* 1644 */     for (int i = 0; i < name.length(); i++) {
/* 1645 */       char c = name.charAt(i);
/* 1646 */       if ((Character.isJavaIdentifierPart(c)) && (Character.isLetterOrDigit(c))) {
/* 1647 */         if (upNext) {
/* 1648 */           c = Character.toUpperCase(c);
/* 1649 */           upNext = false;
/*      */         }
/* 1651 */         sb.append(c);
/*      */       } else {
/* 1653 */         upNext = true;
/*      */       }
/*      */     }
/* 1656 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private static String lCamel(String name) {
/* 1660 */     if ((name == null) || (name.length() < 1))
/* 1661 */       return name;
/* 1662 */     String uCamel = uCamel(name);
/* 1663 */     return uCamel.substring(0, 1).toLowerCase() + uCamel.substring(1);
/*      */   }
/*      */ 
/*      */   private String constantToUCamelCase(String name)
/*      */   {
/* 1668 */     boolean upNext = true;
/* 1669 */     StringBuilder sb = new StringBuilder();
/* 1670 */     for (int i = 0; i < name.length(); i++) {
/* 1671 */       char c = name.charAt(i);
/* 1672 */       if ((Character.isJavaIdentifierPart(c)) && (Character.isLetterOrDigit(c))) {
/* 1673 */         if (upNext) {
/* 1674 */           c = Character.toUpperCase(c);
/* 1675 */           upNext = false;
/*      */         } else {
/* 1677 */           c = Character.toLowerCase(c);
/*      */         }
/* 1679 */         sb.append(c);
/*      */       } else {
/* 1681 */         upNext = true;
/*      */       }
/*      */     }
/* 1684 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   private String constantCase(String name)
/*      */   {
/* 1689 */     StringBuilder sb = new StringBuilder();
/* 1690 */     for (int i = 0; i < name.length(); i++) {
/* 1691 */       char c = name.charAt(i);
/* 1692 */       if ((i != 0) && (Character.isUpperCase(c))) {
/* 1693 */         sb.append("_");
/*      */       }
/* 1695 */       sb.append(Character.toUpperCase(c));
/*      */     }
/* 1697 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public File getOut() {
/* 1701 */     return this.out;
/*      */   }
/*      */ 
/*      */   public void setOut(File outputDirectory) {
/* 1705 */     this.out = outputDirectory;
/*      */   }
/*      */ 
/*      */   public File[] getPath() {
/* 1709 */     return this.path;
/*      */   }
/*      */ 
/*      */   public void setPath(File[] path) {
/* 1713 */     this.path = path;
/*      */   }
/*      */ 
/*      */   static abstract interface Closure
/*      */   {
/*      */     public abstract void execute()
/*      */       throws CompilerException;
/*      */   }
/*      */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.JavaGenerator
 * JD-Core Version:    0.6.2
 */