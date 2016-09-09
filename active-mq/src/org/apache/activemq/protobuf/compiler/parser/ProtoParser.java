/*      */ package org.apache.activemq.protobuf.compiler.parser;
/*      */ 
/*      */ import java.io.InputStream;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.Vector;
/*      */ import org.apache.activemq.protobuf.compiler.EnumDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.EnumFieldDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.ExtensionsDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.FieldDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.MessageDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.MethodDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.OptionDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.ParserSupport;
/*      */ import org.apache.activemq.protobuf.compiler.ProtoDescriptor;
/*      */ import org.apache.activemq.protobuf.compiler.ServiceDescriptor;
/*      */ 
/*      */ public class ProtoParser
/*      */   implements ProtoParserConstants
/*      */ {
/*      */   public ProtoParserTokenManager token_source;
/*      */   SimpleCharStream jj_input_stream;
/*      */   public Token token;
/*      */   public Token jj_nt;
/*      */   private int jj_ntk;
/*      */   private Token jj_scanpos;
/*      */   private Token jj_lastpos;
/*      */   private int jj_la;
/*  861 */   public boolean lookingAhead = false;
/*      */   private boolean jj_semLA;
/*      */   private int jj_gen;
/*  864 */   private final int[] jj_la1 = new int[25];
/*      */   private static int[] jj_la1_0;
/*      */   private static int[] jj_la1_1;
/*  877 */   private final JJCalls[] jj_2_rtns = new JJCalls[2];
/*  878 */   private boolean jj_rescan = false;
/*  879 */   private int jj_gc = 0;
/*      */ 
/*  970 */   private final LookaheadSuccess jj_ls = new LookaheadSuccess(null);
/*      */ 
/* 1016 */   private Vector jj_expentries = new Vector();
/*      */   private int[] jj_expentry;
/* 1018 */   private int jj_kind = -1;
/* 1019 */   private int[] jj_lasttokens = new int[100];
/*      */   private int jj_endpos;
/*      */ 
/*      */   public final ProtoDescriptor ProtoDescriptor()
/*      */     throws ParseException
/*      */   {
/*   36 */     ProtoDescriptor proto = new ProtoDescriptor();
/*   37 */     String packageName = null;
/*   38 */     LinkedHashMap opts = new LinkedHashMap();
/*   39 */     LinkedHashMap messages = new LinkedHashMap();
/*   40 */     LinkedHashMap enums = new LinkedHashMap();
/*   41 */     ArrayList extendsList = new ArrayList();
/*   42 */     LinkedHashMap services = new LinkedHashMap();
/*   43 */     ArrayList imports = new ArrayList();
/*      */     while (true)
/*      */     {
/*   53 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */       case 9:
/*   55 */         jj_consume_token(9);
/*   56 */         packageName = PackageID();
/*   57 */         jj_consume_token(27);
/*   58 */         break;
/*      */       case 12:
/*   60 */         jj_consume_token(12);
/*   61 */         OptionDescriptor optionD = OptionDescriptor();
/*   62 */         jj_consume_token(27);
/*   63 */         opts.put(optionD.getName(), optionD);
/*   64 */         break;
/*      */       case 8:
/*   66 */         jj_consume_token(8);
/*   67 */         String o = StringLitteral();
/*   68 */         jj_consume_token(27);
/*   69 */         imports.add(o);
/*   70 */         break;
/*      */       case 13:
/*   72 */         MessageDescriptor messageD = MessageDescriptor(proto, null);
/*   73 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*   75 */           jj_consume_token(27);
/*   76 */           break;
/*      */         default:
/*   78 */           this.jj_la1[0] = this.jj_gen;
/*      */         }
/*      */ 
/*   81 */         messages.put(messageD.getName(), messageD);
/*   82 */         break;
/*      */       case 16:
/*   84 */         EnumDescriptor enumD = EnumDescriptor(proto, null);
/*   85 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*   87 */           jj_consume_token(27);
/*   88 */           break;
/*      */         default:
/*   90 */           this.jj_la1[1] = this.jj_gen;
/*      */         }
/*      */ 
/*   93 */         enums.put(enumD.getName(), enumD);
/*   94 */         break;
/*      */       case 10:
/*   96 */         ServiceDescriptor serviceD = ServiceDescriptor(proto);
/*   97 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*   99 */           jj_consume_token(27);
/*  100 */           break;
/*      */         default:
/*  102 */           this.jj_la1[2] = this.jj_gen;
/*      */         }
/*      */ 
/*  105 */         services.put(serviceD.getName(), serviceD);
/*  106 */         break;
/*      */       case 15:
/*  108 */         MessageDescriptor extendD = ExtendDescriptor(proto, null);
/*  109 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*  111 */           jj_consume_token(27);
/*  112 */           break;
/*      */         default:
/*  114 */           this.jj_la1[3] = this.jj_gen;
/*      */         }
/*      */ 
/*  117 */         extendsList.add(extendD);
/*  118 */         break;
/*      */       case 11:
/*      */       case 14:
/*      */       default:
/*  120 */         this.jj_la1[4] = this.jj_gen;
/*  121 */         jj_consume_token(-1);
/*  122 */         throw new ParseException();
/*      */       }
/*  124 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) { case 8:
/*      */       case 9:
/*      */       case 10:
/*      */       case 12:
/*      */       case 13:
/*      */       case 15:
/*      */       case 16:
/*      */       case 11:
/*      */       case 14: }
/*      */     }
/*  135 */     this.jj_la1[5] = this.jj_gen;
/*      */ 
/*  139 */     jj_consume_token(0);
/*  140 */     proto.setPackageName(packageName);
/*  141 */     proto.setImports(imports);
/*  142 */     proto.setOptions(opts);
/*  143 */     proto.setMessages(messages);
/*  144 */     proto.setEnums(enums);
/*  145 */     proto.setServices(services);
/*  146 */     proto.setExtends(extendsList);
/*  147 */     return proto;
/*      */   }
/*      */ 
/*      */   public final MessageDescriptor MessageDescriptor(ProtoDescriptor proto, MessageDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  153 */     LinkedHashMap fields = new LinkedHashMap();
/*  154 */     LinkedHashMap messages = new LinkedHashMap();
/*  155 */     LinkedHashMap enums = new LinkedHashMap();
/*  156 */     ArrayList extendsList = new ArrayList();
/*  157 */     LinkedHashMap opts = new LinkedHashMap();
/*      */ 
/*  159 */     MessageDescriptor rc = new MessageDescriptor(proto, parent);
/*      */ 
/*  161 */     ExtensionsDescriptor extensionsD = null;
/*      */ 
/*  166 */     jj_consume_token(13);
/*  167 */     String name = ID();
/*  168 */     jj_consume_token(24);
/*      */     while (true)
/*      */     {
/*  171 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 12:
/*      */       case 13:
/*      */       case 14:
/*      */       case 15:
/*      */       case 16:
/*      */       case 18:
/*      */       case 19:
/*      */       case 20:
/*  181 */         break;
/*      */       case 17:
/*      */       default:
/*  183 */         this.jj_la1[6] = this.jj_gen;
/*  184 */         break;
/*      */       }
/*  186 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */       case 12:
/*  188 */         jj_consume_token(12);
/*  189 */         OptionDescriptor optionD = OptionDescriptor();
/*  190 */         jj_consume_token(27);
/*  191 */         opts.put(optionD.getName(), optionD);
/*  192 */         break;
/*      */       case 18:
/*      */       case 19:
/*      */       case 20:
/*  196 */         FieldDescriptor fieldD = FieldDescriptor(rc);
/*  197 */         fields.put(fieldD.getName(), fieldD);
/*  198 */         break;
/*      */       case 13:
/*  200 */         MessageDescriptor messageD = MessageDescriptor(proto, rc);
/*  201 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*  203 */           jj_consume_token(27);
/*  204 */           break;
/*      */         default:
/*  206 */           this.jj_la1[7] = this.jj_gen;
/*      */         }
/*      */ 
/*  209 */         messages.put(messageD.getName(), messageD);
/*  210 */         break;
/*      */       case 16:
/*  212 */         EnumDescriptor enumD = EnumDescriptor(proto, rc);
/*  213 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*  215 */           jj_consume_token(27);
/*  216 */           break;
/*      */         default:
/*  218 */           this.jj_la1[8] = this.jj_gen;
/*      */         }
/*      */ 
/*  221 */         enums.put(enumD.getName(), enumD);
/*  222 */         break;
/*      */       case 14:
/*  224 */         extensionsD = ExtensionsDescriptor(rc);
/*  225 */         jj_consume_token(27);
/*  226 */         break;
/*      */       case 15:
/*  228 */         MessageDescriptor extendD = ExtendDescriptor(proto, rc);
/*  229 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*  231 */           jj_consume_token(27);
/*  232 */           break;
/*      */         default:
/*  234 */           this.jj_la1[9] = this.jj_gen;
/*      */         }
/*      */ 
/*  237 */         extendsList.add(extendD);
/*      */       case 17:
/*      */       }
/*      */     }
/*  240 */     this.jj_la1[10] = this.jj_gen;
/*  241 */     jj_consume_token(-1);
/*  242 */     throw new ParseException();
/*      */ 
/*  245 */     jj_consume_token(25);
/*  246 */     rc.setName(name);
/*  247 */     rc.setFields(fields);
/*  248 */     rc.setMessages(messages);
/*  249 */     rc.setEnums(enums);
/*  250 */     rc.setExtensions(extensionsD);
/*  251 */     rc.setOptions(opts);
/*  252 */     return rc;
/*      */   }
/*      */ 
/*      */   public final FieldDescriptor FieldDescriptor(MessageDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  265 */     LinkedHashMap opts = new LinkedHashMap();
/*  266 */     LinkedHashMap fields = new LinkedHashMap();
/*      */ 
/*  269 */     FieldDescriptor rc = new FieldDescriptor(parent);
/*  270 */     MessageDescriptor group = new MessageDescriptor(parent.getProtoDescriptor(), parent);
/*  271 */     String rule = Rule();
/*      */     String name;
/*      */     int tag;
/*      */     String type;
/*  272 */     if (jj_2_1(5)) {
/*  273 */       String type = PackageID();
/*  274 */       String name = ID();
/*  275 */       jj_consume_token(26);
/*  276 */       int tag = Integer();
/*  277 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */       case 28:
/*  279 */         jj_consume_token(28);
/*  280 */         OptionDescriptor optionD = OptionDescriptor();
/*  281 */         opts.put(optionD.getName(), optionD);
/*      */         while (true)
/*      */         {
/*  284 */           switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */           {
/*      */           case 33:
/*  287 */             break;
/*      */           default:
/*  289 */             this.jj_la1[11] = this.jj_gen;
/*  290 */             break;
/*      */           }
/*  292 */           jj_consume_token(33);
/*  293 */           optionD = OptionDescriptor();
/*  294 */           opts.put(optionD.getName(), optionD);
/*      */         }
/*  296 */         jj_consume_token(29);
/*  297 */         break;
/*      */       default:
/*  299 */         this.jj_la1[12] = this.jj_gen;
/*      */       }
/*      */ 
/*  302 */       jj_consume_token(27);
/*      */     } else {
/*  304 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 17:
/*  307 */         jj_consume_token(17);
/*  308 */         name = ID();
/*  309 */         jj_consume_token(26);
/*  310 */         tag = Integer();
/*  311 */         jj_consume_token(24);
/*      */         while (true)
/*      */         {
/*  314 */           switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */           {
/*      */           case 18:
/*      */           case 19:
/*      */           case 20:
/*  319 */             break;
/*      */           default:
/*  321 */             this.jj_la1[13] = this.jj_gen;
/*  322 */             break;
/*      */           }
/*  324 */           FieldDescriptor fieldD = FieldDescriptor(group);
/*  325 */           fields.put(fieldD.getName(), fieldD);
/*      */         }
/*  327 */         jj_consume_token(25);
/*  328 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 27:
/*  330 */           jj_consume_token(27);
/*  331 */           break;
/*      */         default:
/*  333 */           this.jj_la1[14] = this.jj_gen;
/*      */         }
/*      */ 
/*  336 */         type = name;
/*  337 */         group.setName(name);
/*  338 */         group.setFields(fields);
/*  339 */         rc.setGroup(group);
/*  340 */         break;
/*      */       default:
/*  342 */         this.jj_la1[15] = this.jj_gen;
/*  343 */         jj_consume_token(-1);
/*  344 */         throw new ParseException();
/*      */       }
/*      */     }
/*  347 */     rc.setName(name);
/*  348 */     rc.setType(type);
/*  349 */     rc.setRule(rule);
/*  350 */     rc.setTag(tag);
/*  351 */     rc.setOptions(opts);
/*  352 */     return rc;
/*      */   }
/*      */ 
/*      */   public final ServiceDescriptor ServiceDescriptor(ProtoDescriptor proto)
/*      */     throws ParseException
/*      */   {
/*  358 */     ArrayList methods = new ArrayList();
/*      */ 
/*  360 */     jj_consume_token(10);
/*  361 */     String name = ID();
/*  362 */     jj_consume_token(24);
/*      */     while (true)
/*      */     {
/*  365 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 11:
/*  368 */         break;
/*      */       default:
/*  370 */         this.jj_la1[16] = this.jj_gen;
/*  371 */         break;
/*      */       }
/*  373 */       MethodDescriptor method = MethodDescriptor(proto);
/*  374 */       jj_consume_token(27);
/*  375 */       methods.add(method);
/*      */     }
/*  377 */     jj_consume_token(25);
/*  378 */     ServiceDescriptor rc = new ServiceDescriptor(proto);
/*  379 */     rc.setName(name);
/*  380 */     rc.setMethods(methods);
/*  381 */     return rc;
/*      */   }
/*      */ 
/*      */   public final MethodDescriptor MethodDescriptor(ProtoDescriptor proto)
/*      */     throws ParseException
/*      */   {
/*  389 */     jj_consume_token(11);
/*  390 */     String name = ID();
/*  391 */     jj_consume_token(30);
/*  392 */     String input = PackageID();
/*  393 */     jj_consume_token(31);
/*  394 */     jj_consume_token(21);
/*  395 */     jj_consume_token(30);
/*  396 */     String output = PackageID();
/*  397 */     jj_consume_token(31);
/*  398 */     MethodDescriptor rc = new MethodDescriptor(proto);
/*  399 */     rc.setName(name);
/*  400 */     rc.setParameter(input);
/*  401 */     rc.setReturns(output);
/*  402 */     return rc;
/*      */   }
/*      */ 
/*      */   public final OptionDescriptor OptionDescriptor()
/*      */     throws ParseException
/*      */   {
/*  409 */     String name = ID();
/*  410 */     jj_consume_token(26);
/*  411 */     String value = Value();
/*  412 */     OptionDescriptor rc = new OptionDescriptor();
/*  413 */     rc.setName(name);
/*  414 */     rc.setValue(value);
/*  415 */     return rc;
/*      */   }
/*      */ 
/*      */   public final MessageDescriptor ExtendDescriptor(ProtoDescriptor proto, MessageDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  421 */     LinkedHashMap fields = new LinkedHashMap();
/*  422 */     MessageDescriptor rc = new MessageDescriptor(proto, parent);
/*      */ 
/*  424 */     jj_consume_token(15);
/*  425 */     String name = ID();
/*  426 */     jj_consume_token(24);
/*      */     while (true)
/*      */     {
/*  429 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 18:
/*      */       case 19:
/*      */       case 20:
/*  434 */         break;
/*      */       default:
/*  436 */         this.jj_la1[17] = this.jj_gen;
/*  437 */         break;
/*      */       }
/*  439 */       FieldDescriptor fieldD = FieldDescriptor(rc);
/*  440 */       fields.put(fieldD.getName(), fieldD);
/*      */     }
/*  442 */     jj_consume_token(25);
/*  443 */     rc.setName(name);
/*  444 */     rc.setFields(fields);
/*  445 */     return rc;
/*      */   }
/*      */ 
/*      */   public final ExtensionsDescriptor ExtensionsDescriptor(MessageDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  452 */     jj_consume_token(14);
/*  453 */     int first = Integer();
/*  454 */     jj_consume_token(22);
/*      */     int last;
/*  455 */     switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */     case 34:
/*  457 */       last = Integer();
/*  458 */       break;
/*      */     case 23:
/*  460 */       jj_consume_token(23);
/*  461 */       last = 536870911;
/*  462 */       break;
/*      */     default:
/*  464 */       this.jj_la1[18] = this.jj_gen;
/*  465 */       jj_consume_token(-1);
/*  466 */       throw new ParseException();
/*      */     }
/*  468 */     ExtensionsDescriptor rc = new ExtensionsDescriptor(parent);
/*  469 */     rc.setFirst(first);
/*  470 */     rc.setLast(last);
/*  471 */     return rc;
/*      */   }
/*      */ 
/*      */   public final EnumDescriptor EnumDescriptor(ProtoDescriptor proto, MessageDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  477 */     LinkedHashMap fields = new LinkedHashMap();
/*  478 */     EnumDescriptor rc = new EnumDescriptor(proto, parent);
/*  479 */     LinkedHashMap opts = new LinkedHashMap();
/*      */ 
/*  483 */     jj_consume_token(16);
/*  484 */     Token name = jj_consume_token(41);
/*  485 */     jj_consume_token(24);
/*      */     while (true)
/*      */     {
/*  488 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 8:
/*      */       case 9:
/*      */       case 10:
/*      */       case 11:
/*      */       case 12:
/*      */       case 13:
/*      */       case 14:
/*      */       case 15:
/*      */       case 16:
/*      */       case 17:
/*      */       case 18:
/*      */       case 19:
/*      */       case 20:
/*      */       case 21:
/*      */       case 22:
/*      */       case 23:
/*      */       case 41:
/*  507 */         break;
/*      */       case 24:
/*      */       case 25:
/*      */       case 26:
/*      */       case 27:
/*      */       case 28:
/*      */       case 29:
/*      */       case 30:
/*      */       case 31:
/*      */       case 32:
/*      */       case 33:
/*      */       case 34:
/*      */       case 35:
/*      */       case 36:
/*      */       case 37:
/*      */       case 38:
/*      */       case 39:
/*      */       case 40:
/*      */       default:
/*  509 */         this.jj_la1[19] = this.jj_gen;
/*  510 */         break;
/*      */       }
/*  512 */       if (jj_2_2(2)) {
/*  513 */         jj_consume_token(12);
/*  514 */         OptionDescriptor optionD = OptionDescriptor();
/*  515 */         jj_consume_token(27);
/*  516 */         opts.put(optionD.getName(), optionD);
/*      */       } else {
/*  518 */         switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */         case 8:
/*      */         case 9:
/*      */         case 10:
/*      */         case 11:
/*      */         case 12:
/*      */         case 13:
/*      */         case 14:
/*      */         case 15:
/*      */         case 16:
/*      */         case 17:
/*      */         case 18:
/*      */         case 19:
/*      */         case 20:
/*      */         case 21:
/*      */         case 22:
/*      */         case 23:
/*      */         case 41:
/*  536 */           EnumFieldDescriptor enumD = EnumFieldDescriptor(rc);
/*  537 */           jj_consume_token(27);
/*  538 */           fields.put(enumD.getName(), enumD);
/*      */         case 24:
/*      */         case 25:
/*      */         case 26:
/*      */         case 27:
/*      */         case 28:
/*      */         case 29:
/*      */         case 30:
/*      */         case 31:
/*      */         case 32:
/*      */         case 33:
/*      */         case 34:
/*      */         case 35:
/*      */         case 36:
/*      */         case 37:
/*      */         case 38:
/*      */         case 39:
/*  541 */         case 40: }  }  } this.jj_la1[20] = this.jj_gen;
/*  542 */     jj_consume_token(-1);
/*  543 */     throw new ParseException();
/*      */ 
/*  547 */     jj_consume_token(25);
/*  548 */     rc.setName(name.image);
/*  549 */     rc.setFields(fields);
/*  550 */     rc.setOptions(opts);
/*  551 */     return rc;
/*      */   }
/*      */ 
/*      */   public final EnumFieldDescriptor EnumFieldDescriptor(EnumDescriptor parent)
/*      */     throws ParseException
/*      */   {
/*  561 */     int value = 0;
/*  562 */     String name = ID();
/*  563 */     jj_consume_token(26);
/*  564 */     value = Integer();
/*  565 */     EnumFieldDescriptor rc = new EnumFieldDescriptor(parent);
/*  566 */     rc.setName(name);
/*  567 */     rc.setValue(value);
/*  568 */     return rc;
/*      */   }
/*      */ 
/*      */   public final int Integer()
/*      */     throws ParseException
/*      */   {
/*  574 */     Token t = jj_consume_token(34);
/*  575 */     return Integer.parseInt(t.image);
/*      */   }
/*      */ 
/*      */   public final String Rule()
/*      */     throws ParseException
/*      */   {
/*      */     Token t;
/*  581 */     switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */     case 18:
/*  583 */       t = jj_consume_token(18);
/*  584 */       break;
/*      */     case 19:
/*  586 */       t = jj_consume_token(19);
/*  587 */       break;
/*      */     case 20:
/*  589 */       t = jj_consume_token(20);
/*  590 */       break;
/*      */     default:
/*  592 */       this.jj_la1[21] = this.jj_gen;
/*  593 */       jj_consume_token(-1);
/*  594 */       throw new ParseException();
/*      */     }
/*  596 */     return t.image;
/*      */   }
/*      */ 
/*      */   public final String Value()
/*      */     throws ParseException
/*      */   {
/*  602 */     String value = null;
/*      */     Token t;
/*  603 */     switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */     case 40:
/*  605 */       value = StringLitteral();
/*  606 */       break;
/*      */     case 8:
/*      */     case 9:
/*      */     case 10:
/*      */     case 11:
/*      */     case 12:
/*      */     case 13:
/*      */     case 14:
/*      */     case 15:
/*      */     case 16:
/*      */     case 17:
/*      */     case 18:
/*      */     case 19:
/*      */     case 20:
/*      */     case 21:
/*      */     case 22:
/*      */     case 23:
/*      */     case 41:
/*  624 */       value = ID();
/*  625 */       break;
/*      */     case 34:
/*  627 */       t = jj_consume_token(34);
/*  628 */       value = t.image;
/*  629 */       break;
/*      */     case 38:
/*  631 */       t = jj_consume_token(38);
/*  632 */       value = t.image;
/*  633 */       break;
/*      */     case 24:
/*      */     case 25:
/*      */     case 26:
/*      */     case 27:
/*      */     case 28:
/*      */     case 29:
/*      */     case 30:
/*      */     case 31:
/*      */     case 32:
/*      */     case 33:
/*      */     case 35:
/*      */     case 36:
/*      */     case 37:
/*      */     case 39:
/*      */     default:
/*  635 */       this.jj_la1[22] = this.jj_gen;
/*  636 */       jj_consume_token(-1);
/*  637 */       throw new ParseException();
/*      */     }
/*  639 */     return value;
/*      */   }
/*      */ 
/*      */   public final String ID()
/*      */     throws ParseException
/*      */   {
/*      */     Token t;
/*  645 */     switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk) {
/*      */     case 41:
/*  647 */       t = jj_consume_token(41);
/*  648 */       break;
/*      */     case 17:
/*  650 */       t = jj_consume_token(17);
/*  651 */       break;
/*      */     case 8:
/*  653 */       t = jj_consume_token(8);
/*  654 */       break;
/*      */     case 9:
/*  656 */       t = jj_consume_token(9);
/*  657 */       break;
/*      */     case 10:
/*  659 */       t = jj_consume_token(10);
/*  660 */       break;
/*      */     case 11:
/*  662 */       t = jj_consume_token(11);
/*  663 */       break;
/*      */     case 12:
/*  665 */       t = jj_consume_token(12);
/*  666 */       break;
/*      */     case 13:
/*  668 */       t = jj_consume_token(13);
/*  669 */       break;
/*      */     case 14:
/*  671 */       t = jj_consume_token(14);
/*  672 */       break;
/*      */     case 15:
/*  674 */       t = jj_consume_token(15);
/*  675 */       break;
/*      */     case 16:
/*  677 */       t = jj_consume_token(16);
/*  678 */       break;
/*      */     case 18:
/*  680 */       t = jj_consume_token(18);
/*  681 */       break;
/*      */     case 19:
/*  683 */       t = jj_consume_token(19);
/*  684 */       break;
/*      */     case 20:
/*  686 */       t = jj_consume_token(20);
/*  687 */       break;
/*      */     case 21:
/*  689 */       t = jj_consume_token(21);
/*  690 */       break;
/*      */     case 22:
/*  692 */       t = jj_consume_token(22);
/*  693 */       break;
/*      */     case 23:
/*  695 */       t = jj_consume_token(23);
/*  696 */       break;
/*      */     case 24:
/*      */     case 25:
/*      */     case 26:
/*      */     case 27:
/*      */     case 28:
/*      */     case 29:
/*      */     case 30:
/*      */     case 31:
/*      */     case 32:
/*      */     case 33:
/*      */     case 34:
/*      */     case 35:
/*      */     case 36:
/*      */     case 37:
/*      */     case 38:
/*      */     case 39:
/*      */     case 40:
/*      */     default:
/*  698 */       this.jj_la1[23] = this.jj_gen;
/*  699 */       jj_consume_token(-1);
/*  700 */       throw new ParseException();
/*      */     }
/*  702 */     return t.image;
/*      */   }
/*      */ 
/*      */   public final String PackageID()
/*      */     throws ParseException
/*      */   {
/*  708 */     StringBuffer sb = new StringBuffer();
/*  709 */     String t = ID();
/*  710 */     sb.append(t);
/*      */     while (true)
/*      */     {
/*  713 */       switch (this.jj_ntk == -1 ? jj_ntk() : this.jj_ntk)
/*      */       {
/*      */       case 32:
/*  716 */         break;
/*      */       default:
/*  718 */         this.jj_la1[24] = this.jj_gen;
/*  719 */         break;
/*      */       }
/*  721 */       jj_consume_token(32);
/*  722 */       t = ID();
/*  723 */       sb.append(".");
/*  724 */       sb.append(t);
/*      */     }
/*  726 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public final String StringLitteral()
/*      */     throws ParseException
/*      */   {
/*  732 */     Token t = jj_consume_token(40);
/*  733 */     return ParserSupport.decodeString(t);
/*      */   }
/*      */ 
/*      */   private final boolean jj_2_1(int xla)
/*      */   {
/*  738 */     this.jj_la = xla; this.jj_lastpos = (this.jj_scanpos = this.token);
/*      */     try { return !jj_3_1(); } catch (LookaheadSuccess ls) {
/*  740 */       return true; } finally {
/*  741 */       jj_save(0, xla);
/*      */     }
/*      */   }
/*      */ 
/*  745 */   private final boolean jj_2_2(int xla) { this.jj_la = xla; this.jj_lastpos = (this.jj_scanpos = this.token);
/*      */     try { return !jj_3_2(); } catch (LookaheadSuccess ls) {
/*  747 */       return true; } finally {
/*  748 */       jj_save(1, xla);
/*      */     } }
/*      */ 
/*      */   private final boolean jj_3R_14() {
/*  752 */     if (jj_scan_token(32)) return true;
/*  753 */     if (jj_3R_10()) return true;
/*  754 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3_2() {
/*  758 */     if (jj_scan_token(12)) return true;
/*  759 */     if (jj_3R_13()) return true;
/*  760 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3R_10()
/*      */   {
/*  765 */     Token xsp = this.jj_scanpos;
/*  766 */     if (jj_scan_token(41)) {
/*  767 */       this.jj_scanpos = xsp;
/*  768 */       if (jj_scan_token(17)) {
/*  769 */         this.jj_scanpos = xsp;
/*  770 */         if (jj_scan_token(8)) {
/*  771 */           this.jj_scanpos = xsp;
/*  772 */           if (jj_scan_token(9)) {
/*  773 */             this.jj_scanpos = xsp;
/*  774 */             if (jj_scan_token(10)) {
/*  775 */               this.jj_scanpos = xsp;
/*  776 */               if (jj_scan_token(11)) {
/*  777 */                 this.jj_scanpos = xsp;
/*  778 */                 if (jj_scan_token(12)) {
/*  779 */                   this.jj_scanpos = xsp;
/*  780 */                   if (jj_scan_token(13)) {
/*  781 */                     this.jj_scanpos = xsp;
/*  782 */                     if (jj_scan_token(14)) {
/*  783 */                       this.jj_scanpos = xsp;
/*  784 */                       if (jj_scan_token(15)) {
/*  785 */                         this.jj_scanpos = xsp;
/*  786 */                         if (jj_scan_token(16)) {
/*  787 */                           this.jj_scanpos = xsp;
/*  788 */                           if (jj_scan_token(18)) {
/*  789 */                             this.jj_scanpos = xsp;
/*  790 */                             if (jj_scan_token(19)) {
/*  791 */                               this.jj_scanpos = xsp;
/*  792 */                               if (jj_scan_token(20)) {
/*  793 */                                 this.jj_scanpos = xsp;
/*  794 */                                 if (jj_scan_token(21)) {
/*  795 */                                   this.jj_scanpos = xsp;
/*  796 */                                   if (jj_scan_token(22)) {
/*  797 */                                     this.jj_scanpos = xsp;
/*  798 */                                     if (jj_scan_token(23)) return true;
/*      */                                   }
/*      */                                 }
/*      */                               }
/*      */                             }
/*      */                           }
/*      */                         }
/*      */                       }
/*      */                     }
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  815 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3R_11() {
/*  819 */     if (jj_scan_token(34)) return true;
/*  820 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3R_9() {
/*  824 */     if (jj_3R_10()) return true;
/*      */     Token xsp;
/*      */     do
/*  827 */       xsp = this.jj_scanpos;
/*  828 */     while (!jj_3R_14()); this.jj_scanpos = xsp;
/*      */ 
/*  830 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3R_12() {
/*  834 */     if (jj_scan_token(28)) return true;
/*  835 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3_1() {
/*  839 */     if (jj_3R_9()) return true;
/*  840 */     if (jj_3R_10()) return true;
/*  841 */     if (jj_scan_token(26)) return true;
/*  842 */     if (jj_3R_11()) return true;
/*      */ 
/*  844 */     Token xsp = this.jj_scanpos;
/*  845 */     if (jj_3R_12()) this.jj_scanpos = xsp;
/*  846 */     if (jj_scan_token(27)) return true;
/*  847 */     return false;
/*      */   }
/*      */ 
/*      */   private final boolean jj_3R_13() {
/*  851 */     if (jj_3R_10()) return true;
/*  852 */     return false;
/*      */   }
/*      */ 
/*      */   private static void jj_la1_0()
/*      */   {
/*  872 */     jj_la1_0 = new int[] { 134217728, 134217728, 134217728, 134217728, 112384, 112384, 1961984, 134217728, 134217728, 134217728, 1961984, 0, 268435456, 1835008, 134217728, 131072, 2048, 1835008, 8388608, 16776960, 16776960, 1835008, 16776960, 16776960, 0 };
/*      */   }
/*      */   private static void jj_la1_1() {
/*  875 */     jj_la1_1 = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 4, 512, 512, 0, 836, 512, 1 };
/*      */   }
/*      */ 
/*      */   public ProtoParser(InputStream stream)
/*      */   {
/*  882 */     this(stream, null);
/*      */   }
/*      */   public ProtoParser(InputStream stream, String encoding) {
/*      */     try { this.jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
/*  886 */     this.token_source = new ProtoParserTokenManager(this.jj_input_stream);
/*  887 */     this.token = new Token();
/*  888 */     this.jj_ntk = -1;
/*  889 */     this.jj_gen = 0;
/*  890 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  891 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls(); 
/*      */   }
/*      */ 
/*      */   public void ReInit(InputStream stream)
/*      */   {
/*  895 */     ReInit(stream, null);
/*      */   }
/*      */   public void ReInit(InputStream stream, String encoding) {
/*      */     try { this.jj_input_stream.ReInit(stream, encoding, 1, 1); } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
/*  899 */     this.token_source.ReInit(this.jj_input_stream);
/*  900 */     this.token = new Token();
/*  901 */     this.jj_ntk = -1;
/*  902 */     this.jj_gen = 0;
/*  903 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  904 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls(); 
/*      */   }
/*      */ 
/*      */   public ProtoParser(Reader stream)
/*      */   {
/*  908 */     this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
/*  909 */     this.token_source = new ProtoParserTokenManager(this.jj_input_stream);
/*  910 */     this.token = new Token();
/*  911 */     this.jj_ntk = -1;
/*  912 */     this.jj_gen = 0;
/*  913 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  914 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls(); 
/*      */   }
/*      */ 
/*      */   public void ReInit(Reader stream)
/*      */   {
/*  918 */     this.jj_input_stream.ReInit(stream, 1, 1);
/*  919 */     this.token_source.ReInit(this.jj_input_stream);
/*  920 */     this.token = new Token();
/*  921 */     this.jj_ntk = -1;
/*  922 */     this.jj_gen = 0;
/*  923 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  924 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls(); 
/*      */   }
/*      */ 
/*      */   public ProtoParser(ProtoParserTokenManager tm)
/*      */   {
/*  928 */     this.token_source = tm;
/*  929 */     this.token = new Token();
/*  930 */     this.jj_ntk = -1;
/*  931 */     this.jj_gen = 0;
/*  932 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  933 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls(); 
/*      */   }
/*      */ 
/*      */   public void ReInit(ProtoParserTokenManager tm)
/*      */   {
/*  937 */     this.token_source = tm;
/*  938 */     this.token = new Token();
/*  939 */     this.jj_ntk = -1;
/*  940 */     this.jj_gen = 0;
/*  941 */     for (int i = 0; i < 25; i++) this.jj_la1[i] = -1;
/*  942 */     for (int i = 0; i < this.jj_2_rtns.length; i++) this.jj_2_rtns[i] = new JJCalls();
/*      */   }
/*      */ 
/*      */   private final Token jj_consume_token(int kind)
/*      */     throws ParseException
/*      */   {
/*  947 */     Token oldToken;
/*  947 */     if ((oldToken = this.token).next != null) this.token = this.token.next; else
/*  948 */       this.token = (this.token.next = this.token_source.getNextToken());
/*  949 */     this.jj_ntk = -1;
/*  950 */     if (this.token.kind == kind) {
/*  951 */       this.jj_gen += 1;
/*  952 */       if (++this.jj_gc > 100) {
/*  953 */         this.jj_gc = 0;
/*  954 */         for (int i = 0; i < this.jj_2_rtns.length; i++) {
/*  955 */           JJCalls c = this.jj_2_rtns[i];
/*  956 */           while (c != null) {
/*  957 */             if (c.gen < this.jj_gen) c.first = null;
/*  958 */             c = c.next;
/*      */           }
/*      */         }
/*      */       }
/*  962 */       return this.token;
/*      */     }
/*  964 */     this.token = oldToken;
/*  965 */     this.jj_kind = kind;
/*  966 */     throw generateParseException();
/*      */   }
/*      */ 
/*      */   private final boolean jj_scan_token(int kind)
/*      */   {
/*  972 */     if (this.jj_scanpos == this.jj_lastpos) {
/*  973 */       this.jj_la -= 1;
/*  974 */       if (this.jj_scanpos.next == null)
/*  975 */         this.jj_lastpos = (this.jj_scanpos = this.jj_scanpos.next = this.token_source.getNextToken());
/*      */       else
/*  977 */         this.jj_lastpos = (this.jj_scanpos = this.jj_scanpos.next);
/*      */     }
/*      */     else {
/*  980 */       this.jj_scanpos = this.jj_scanpos.next;
/*      */     }
/*  982 */     if (this.jj_rescan) {
/*  983 */       int i = 0; for (Token tok = this.token; 
/*  984 */         (tok != null) && (tok != this.jj_scanpos); tok = tok.next) i++;
/*  985 */       if (tok != null) jj_add_error_token(kind, i);
/*      */     }
/*  987 */     if (this.jj_scanpos.kind != kind) return true;
/*  988 */     if ((this.jj_la == 0) && (this.jj_scanpos == this.jj_lastpos)) throw this.jj_ls;
/*  989 */     return false;
/*      */   }
/*      */ 
/*      */   public final Token getNextToken() {
/*  993 */     if (this.token.next != null) this.token = this.token.next; else
/*  994 */       this.token = (this.token.next = this.token_source.getNextToken());
/*  995 */     this.jj_ntk = -1;
/*  996 */     this.jj_gen += 1;
/*  997 */     return this.token;
/*      */   }
/*      */ 
/*      */   public final Token getToken(int index) {
/* 1001 */     Token t = this.lookingAhead ? this.jj_scanpos : this.token;
/* 1002 */     for (int i = 0; i < index; i++) {
/* 1003 */       if (t.next != null) t = t.next; else
/* 1004 */         t = t.next = this.token_source.getNextToken();
/*      */     }
/* 1006 */     return t;
/*      */   }
/*      */ 
/*      */   private final int jj_ntk() {
/* 1010 */     if ((this.jj_nt = this.token.next) == null) {
/* 1011 */       return this.jj_ntk = (this.token.next = this.token_source.getNextToken()).kind;
/*      */     }
/* 1013 */     return this.jj_ntk = this.jj_nt.kind;
/*      */   }
/*      */ 
/*      */   private void jj_add_error_token(int kind, int pos)
/*      */   {
/* 1023 */     if (pos >= 100) return;
/* 1024 */     if (pos == this.jj_endpos + 1) {
/* 1025 */       this.jj_lasttokens[(this.jj_endpos++)] = kind;
/* 1026 */     } else if (this.jj_endpos != 0) {
/* 1027 */       this.jj_expentry = new int[this.jj_endpos];
/* 1028 */       for (int i = 0; i < this.jj_endpos; i++) {
/* 1029 */         this.jj_expentry[i] = this.jj_lasttokens[i];
/*      */       }
/* 1031 */       boolean exists = false;
/* 1032 */       for (Enumeration e = this.jj_expentries.elements(); e.hasMoreElements(); ) {
/* 1033 */         int[] oldentry = (int[])e.nextElement();
/* 1034 */         if (oldentry.length == this.jj_expentry.length) {
/* 1035 */           exists = true;
/* 1036 */           for (int i = 0; i < this.jj_expentry.length; i++) {
/* 1037 */             if (oldentry[i] != this.jj_expentry[i]) {
/* 1038 */               exists = false;
/* 1039 */               break;
/*      */             }
/*      */           }
/* 1042 */           if (exists) break;
/*      */         }
/*      */       }
/* 1045 */       if (!exists) this.jj_expentries.addElement(this.jj_expentry);
/* 1046 */       if (pos != 0)
/*      */       {
/*      */         int tmp205_204 = pos; this.jj_endpos = tmp205_204; this.jj_lasttokens[(tmp205_204 - 1)] = kind;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/* 1051 */   public ParseException generateParseException() { this.jj_expentries.removeAllElements();
/* 1052 */     boolean[] la1tokens = new boolean[42];
/* 1053 */     for (int i = 0; i < 42; i++) {
/* 1054 */       la1tokens[i] = false;
/*      */     }
/* 1056 */     if (this.jj_kind >= 0) {
/* 1057 */       la1tokens[this.jj_kind] = true;
/* 1058 */       this.jj_kind = -1;
/*      */     }
/* 1060 */     for (int i = 0; i < 25; i++) {
/* 1061 */       if (this.jj_la1[i] == this.jj_gen) {
/* 1062 */         for (int j = 0; j < 32; j++) {
/* 1063 */           if ((jj_la1_0[i] & 1 << j) != 0) {
/* 1064 */             la1tokens[j] = true;
/*      */           }
/* 1066 */           if ((jj_la1_1[i] & 1 << j) != 0) {
/* 1067 */             la1tokens[(32 + j)] = true;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1072 */     for (int i = 0; i < 42; i++) {
/* 1073 */       if (la1tokens[i] != 0) {
/* 1074 */         this.jj_expentry = new int[1];
/* 1075 */         this.jj_expentry[0] = i;
/* 1076 */         this.jj_expentries.addElement(this.jj_expentry);
/*      */       }
/*      */     }
/* 1079 */     this.jj_endpos = 0;
/* 1080 */     jj_rescan_token();
/* 1081 */     jj_add_error_token(0, 0);
/* 1082 */     int[][] exptokseq = new int[this.jj_expentries.size()][];
/* 1083 */     for (int i = 0; i < this.jj_expentries.size(); i++) {
/* 1084 */       exptokseq[i] = ((int[])(int[])this.jj_expentries.elementAt(i));
/*      */     }
/* 1086 */     return new ParseException(this.token, exptokseq, tokenImage); }
/*      */ 
/*      */   public final void enable_tracing()
/*      */   {
/*      */   }
/*      */ 
/*      */   public final void disable_tracing() {
/*      */   }
/*      */ 
/*      */   private final void jj_rescan_token() {
/* 1096 */     this.jj_rescan = true;
/* 1097 */     for (int i = 0; i < 2; i++)
/*      */       try {
/* 1099 */         JJCalls p = this.jj_2_rtns[i];
/*      */         do {
/* 1101 */           if (p.gen > this.jj_gen) {
/* 1102 */             this.jj_la = p.arg; this.jj_lastpos = (this.jj_scanpos = p.first);
/* 1103 */             switch (i) { case 0:
/* 1104 */               jj_3_1(); break;
/*      */             case 1:
/* 1105 */               jj_3_2();
/*      */             }
/*      */           }
/* 1108 */           p = p.next;
/* 1109 */         }while (p != null);
/*      */       } catch (LookaheadSuccess ls) {
/*      */       }
/* 1112 */     this.jj_rescan = false;
/*      */   }
/*      */ 
/*      */   private final void jj_save(int index, int xla) {
/* 1116 */     JJCalls p = this.jj_2_rtns[index];
/* 1117 */     while (p.gen > this.jj_gen) {
/* 1118 */       if (p.next == null) { p = p.next = new JJCalls(); break; }
/* 1119 */       p = p.next;
/*      */     }
/* 1121 */     p.gen = (this.jj_gen + xla - this.jj_la); p.first = this.token; p.arg = xla;
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  868 */     jj_la1_0();
/*  869 */     jj_la1_1();
/*      */   }
/*      */ 
/*      */   static final class JJCalls
/*      */   {
/*      */     int gen;
/*      */     Token first;
/*      */     int arg;
/*      */     JJCalls next;
/*      */   }
/*      */ 
/*      */   private static final class LookaheadSuccess extends Error
/*      */   {
/*      */   }
/*      */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.ProtoParser
 * JD-Core Version:    0.6.2
 */