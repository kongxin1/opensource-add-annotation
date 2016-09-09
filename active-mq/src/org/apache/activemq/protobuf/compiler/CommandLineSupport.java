/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ 
/*     */ public class CommandLineSupport
/*     */ {
/*     */   public static String[] setOptions(Object target, String[] args)
/*     */   {
/*  49 */     ArrayList rc = new ArrayList();
/*     */ 
/*  51 */     for (int i = 0; i < args.length; i++) {
/*  52 */       if (args[i] != null)
/*     */       {
/*  55 */         if (args[i].startsWith("--"))
/*     */         {
/*  58 */           String value = "true";
/*  59 */           String name = args[i].substring(2);
/*     */ 
/*  62 */           int p = name.indexOf("=");
/*  63 */           if (p > 0) {
/*  64 */             value = name.substring(p + 1);
/*  65 */             name = name.substring(0, p);
/*     */           }
/*     */ 
/*  69 */           if (name.length() == 0) {
/*  70 */             rc.add(args[i]);
/*     */           }
/*     */           else
/*     */           {
/*  74 */             String propName = convertOptionToPropertyName(name);
/*  75 */             if (!IntrospectionSupport.setProperty(target, propName, value))
/*  76 */               rc.add(args[i]);
/*     */           }
/*     */         }
/*     */         else {
/*  80 */           rc.add(args[i]);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  85 */     String[] r = new String[rc.size()];
/*  86 */     rc.toArray(r);
/*  87 */     return r;
/*     */   }
/*     */ 
/*     */   private static String convertOptionToPropertyName(String name)
/*     */   {
/*  96 */     String rc = "";
/*     */ 
/*  99 */     int p = name.indexOf("-");
/* 100 */     while (p > 0)
/*     */     {
/* 102 */       rc = rc + name.substring(0, p);
/* 103 */       name = name.substring(p + 1);
/*     */ 
/* 106 */       if (name.length() > 0) {
/* 107 */         rc = rc + name.substring(0, 1).toUpperCase();
/* 108 */         name = name.substring(1);
/*     */       }
/*     */ 
/* 111 */       p = name.indexOf("-");
/*     */     }
/* 113 */     return rc + name;
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.CommandLineSupport
 * JD-Core Version:    0.6.2
 */