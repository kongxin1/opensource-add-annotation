/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.beans.PropertyEditor;
/*     */ import java.beans.PropertyEditorManager;
/*     */ import java.io.File;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.lang.reflect.Modifier;
/*     */ import java.net.URI;
/*     */ import java.net.URISyntaxException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public final class IntrospectionSupport
/*     */ {
/*     */   public static boolean getProperties(Object target, Map props, String optionPrefix)
/*     */   {
/*  49 */     boolean rc = false;
/*  50 */     if (target == null) {
/*  51 */       throw new IllegalArgumentException("target was null.");
/*     */     }
/*  53 */     if (props == null) {
/*  54 */       throw new IllegalArgumentException("props was null.");
/*     */     }
/*     */ 
/*  57 */     if (optionPrefix == null) {
/*  58 */       optionPrefix = "";
/*     */     }
/*     */ 
/*  61 */     Class clazz = target.getClass();
/*  62 */     Method[] methods = clazz.getMethods();
/*  63 */     for (int i = 0; i < methods.length; i++) {
/*  64 */       Method method = methods[i];
/*  65 */       String name = method.getName();
/*  66 */       Class type = method.getReturnType();
/*  67 */       Class[] params = method.getParameterTypes();
/*  68 */       if ((name.startsWith("get")) && (params.length == 0) && (type != null) && (isSettableType(type)))
/*     */       {
/*     */         try
/*     */         {
/*  72 */           Object value = method.invoke(target, new Object[0]);
/*  73 */           if (value != null)
/*     */           {
/*  77 */             String strValue = convertToString(value, type);
/*  78 */             if (strValue != null)
/*     */             {
/*  82 */               name = name.substring(3, 4).toLowerCase() + name.substring(4);
/*  83 */               props.put(optionPrefix + name, strValue);
/*  84 */               rc = true;
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Throwable ignore) {
/*     */         }
/*     */       }
/*     */     }
/*  92 */     return rc;
/*     */   }
/*     */ 
/*     */   public static boolean setProperties(Object target, Map<String, ?> props, String optionPrefix) {
/*  96 */     boolean rc = false;
/*  97 */     if (target == null) {
/*  98 */       throw new IllegalArgumentException("target was null.");
/*     */     }
/* 100 */     if (props == null) {
/* 101 */       throw new IllegalArgumentException("props was null.");
/*     */     }
/*     */ 
/* 104 */     for (Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
/* 105 */       String name = (String)iter.next();
/* 106 */       if (name.startsWith(optionPrefix)) {
/* 107 */         Object value = props.get(name);
/* 108 */         name = name.substring(optionPrefix.length());
/* 109 */         if (setProperty(target, name, value)) {
/* 110 */           iter.remove();
/* 111 */           rc = true;
/*     */         }
/*     */       }
/*     */     }
/* 115 */     return rc;
/*     */   }
/*     */ 
/*     */   public static Map<String, Object> extractProperties(Map props, String optionPrefix) {
/* 119 */     if (props == null) {
/* 120 */       throw new IllegalArgumentException("props was null.");
/*     */     }
/*     */ 
/* 123 */     HashMap rc = new HashMap(props.size());
/*     */ 
/* 125 */     for (Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
/* 126 */       String name = (String)iter.next();
/* 127 */       if (name.startsWith(optionPrefix)) {
/* 128 */         Object value = props.get(name);
/* 129 */         name = name.substring(optionPrefix.length());
/* 130 */         rc.put(name, value);
/* 131 */         iter.remove();
/*     */       }
/*     */     }
/*     */ 
/* 135 */     return rc;
/*     */   }
/*     */ 
/*     */   public static boolean setProperties(Object target, Map props) {
/* 139 */     boolean rc = false;
/*     */ 
/* 141 */     if (target == null) {
/* 142 */       throw new IllegalArgumentException("target was null.");
/*     */     }
/* 144 */     if (props == null) {
/* 145 */       throw new IllegalArgumentException("props was null.");
/*     */     }
/*     */ 
/* 148 */     for (Iterator iter = props.entrySet().iterator(); iter.hasNext(); ) {
/* 149 */       Map.Entry entry = (Map.Entry)iter.next();
/* 150 */       if (setProperty(target, (String)entry.getKey(), entry.getValue())) {
/* 151 */         iter.remove();
/* 152 */         rc = true;
/*     */       }
/*     */     }
/*     */ 
/* 156 */     return rc;
/*     */   }
/*     */ 
/*     */   public static boolean setProperty(Object target, String name, Object value) {
/*     */     try {
/* 161 */       Class clazz = target.getClass();
/* 162 */       Method setter = findSetterMethod(clazz, name);
/* 163 */       if (setter == null) {
/* 164 */         return false;
/*     */       }
/*     */ 
/* 169 */       if ((value == null) || (value.getClass() == setter.getParameterTypes()[0])) {
/* 170 */         setter.invoke(target, new Object[] { value });
/*     */       }
/*     */       else {
/* 173 */         setter.invoke(target, new Object[] { convert(value, setter.getParameterTypes()[0]) });
/*     */       }
/* 175 */       return true; } catch (Throwable ignore) {
/*     */     }
/* 177 */     return false;
/*     */   }
/*     */ 
/*     */   private static Object convert(Object value, Class type) throws URISyntaxException
/*     */   {
/* 182 */     PropertyEditor editor = PropertyEditorManager.findEditor(type);
/* 183 */     if (editor != null) {
/* 184 */       editor.setAsText(value.toString());
/* 185 */       return editor.getValue();
/*     */     }
/* 187 */     if (type == URI.class) {
/* 188 */       return new URI(value.toString());
/*     */     }
/* 190 */     if (type == File.class) {
/* 191 */       return new File(value.toString());
/*     */     }
/* 193 */     if (type == [Ljava.io.File.class) {
/* 194 */       ArrayList files = new ArrayList();
/* 195 */       StringTokenizer st = new StringTokenizer(value.toString(), ":");
/* 196 */       while (st.hasMoreTokens()) {
/* 197 */         String t = st.nextToken();
/* 198 */         if ((t != null) && (t.trim().length() > 0)) {
/* 199 */           files.add(new File(t.trim()));
/*     */         }
/*     */       }
/* 202 */       File[] rc = new File[files.size()];
/* 203 */       files.toArray(rc);
/* 204 */       return rc;
/*     */     }
/* 206 */     return null;
/*     */   }
/*     */ 
/*     */   private static String convertToString(Object value, Class type) throws URISyntaxException {
/* 210 */     PropertyEditor editor = PropertyEditorManager.findEditor(type);
/* 211 */     if (editor != null) {
/* 212 */       editor.setValue(value);
/* 213 */       return editor.getAsText();
/*     */     }
/* 215 */     if (type == URI.class) {
/* 216 */       return ((URI)value).toString();
/*     */     }
/* 218 */     return null;
/*     */   }
/*     */ 
/*     */   private static Method findSetterMethod(Class clazz, String name)
/*     */   {
/* 223 */     name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
/* 224 */     Method[] methods = clazz.getMethods();
/* 225 */     for (int i = 0; i < methods.length; i++) {
/* 226 */       Method method = methods[i];
/* 227 */       Class[] params = method.getParameterTypes();
/* 228 */       if ((method.getName().equals(name)) && (params.length == 1) && (isSettableType(params[0]))) {
/* 229 */         return method;
/*     */       }
/*     */     }
/* 232 */     return null;
/*     */   }
/*     */ 
/*     */   private static boolean isSettableType(Class clazz) {
/* 236 */     if (PropertyEditorManager.findEditor(clazz) != null) {
/* 237 */       return true;
/*     */     }
/* 239 */     if (clazz == URI.class) {
/* 240 */       return true;
/*     */     }
/* 242 */     if (clazz == File.class) {
/* 243 */       return true;
/*     */     }
/* 245 */     if (clazz == [Ljava.io.File.class) {
/* 246 */       return true;
/*     */     }
/* 248 */     if (clazz == Boolean.class) {
/* 249 */       return true;
/*     */     }
/* 251 */     return false;
/*     */   }
/*     */ 
/*     */   public static String toString(Object target) {
/* 255 */     return toString(target, Object.class);
/*     */   }
/*     */ 
/*     */   public static String toString(Object target, Class stopClass) {
/* 259 */     LinkedHashMap map = new LinkedHashMap();
/* 260 */     addFields(target, target.getClass(), stopClass, map);
/* 261 */     StringBuffer buffer = new StringBuffer(simpleName(target.getClass()));
/* 262 */     buffer.append(" {");
/* 263 */     Set entrySet = map.entrySet();
/* 264 */     boolean first = true;
/* 265 */     for (Iterator iter = entrySet.iterator(); iter.hasNext(); ) {
/* 266 */       Map.Entry entry = (Map.Entry)iter.next();
/* 267 */       if (first)
/* 268 */         first = false;
/*     */       else {
/* 270 */         buffer.append(", ");
/*     */       }
/* 272 */       buffer.append(entry.getKey());
/* 273 */       buffer.append(" = ");
/* 274 */       appendToString(buffer, entry.getValue());
/*     */     }
/* 276 */     buffer.append("}");
/* 277 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   protected static void appendToString(StringBuffer buffer, Object value) {
/* 281 */     buffer.append(value);
/*     */   }
/*     */ 
/*     */   public static String simpleName(Class clazz) {
/* 285 */     String name = clazz.getName();
/* 286 */     int p = name.lastIndexOf(".");
/* 287 */     if (p >= 0) {
/* 288 */       name = name.substring(p + 1);
/*     */     }
/* 290 */     return name;
/*     */   }
/*     */ 
/*     */   private static void addFields(Object target, Class startClass, Class<Object> stopClass, LinkedHashMap<String, Object> map)
/*     */   {
/* 295 */     if (startClass != stopClass) {
/* 296 */       addFields(target, startClass.getSuperclass(), stopClass, map);
/*     */     }
/*     */ 
/* 299 */     Field[] fields = startClass.getDeclaredFields();
/* 300 */     for (int i = 0; i < fields.length; i++) {
/* 301 */       Field field = fields[i];
/* 302 */       if ((!Modifier.isStatic(field.getModifiers())) && (!Modifier.isTransient(field.getModifiers())) && (!Modifier.isPrivate(field.getModifiers())))
/*     */       {
/*     */         try
/*     */         {
/* 308 */           field.setAccessible(true);
/* 309 */           Object o = field.get(target);
/* 310 */           if ((o != null) && (o.getClass().isArray()))
/*     */             try {
/* 312 */               o = Arrays.asList((Object[])o);
/*     */             }
/*     */             catch (Throwable e) {
/*     */             }
/* 316 */           map.put(field.getName(), o);
/*     */         } catch (Throwable e) {
/* 318 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.IntrospectionSupport
 * JD-Core Version:    0.6.2
 */