/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileFilter;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import org.apache.maven.plugin.AbstractMojo;
/*     */ import org.apache.maven.plugin.MojoExecutionException;
/*     */ import org.apache.maven.plugin.logging.Log;
/*     */ import org.apache.maven.project.MavenProject;
/*     */ 
/*     */ public class ProtoMojo extends AbstractMojo
/*     */ {
/*     */   protected MavenProject project;
/*     */   private File sourceDirectory;
/*     */   private File outputDirectory;
/*     */   private String type;
/*     */ 
/*     */   public void execute()
/*     */     throws MojoExecutionException
/*     */   {
/*  73 */     File[] files = this.sourceDirectory.listFiles(new FileFilter() {
/*     */       public boolean accept(File pathname) {
/*  75 */         return pathname.getName().endsWith(".proto");
/*     */       }
/*     */     });
/*  79 */     if ((files == null) || (files.length == 0)) {
/*  80 */       getLog().warn("No proto files found in directory: " + this.sourceDirectory.getPath());
/*  81 */       return;
/*     */     }
/*     */ 
/*  84 */     List recFiles = Arrays.asList(files);
/*  85 */     for (File file : recFiles) {
/*     */       try {
/*  87 */         getLog().info("Compiling: " + file.getPath());
/*  88 */         if ("default".equals(this.type)) {
/*  89 */           JavaGenerator generator = new JavaGenerator();
/*  90 */           generator.setOut(this.outputDirectory);
/*  91 */           generator.compile(file);
/*  92 */         } else if ("alt".equals(this.type)) {
/*  93 */           AltJavaGenerator generator = new AltJavaGenerator();
/*  94 */           generator.setOut(this.outputDirectory);
/*  95 */           generator.compile(file);
/*     */         }
/*     */       } catch (CompilerException e) {
/*  98 */         getLog().error("Protocol Buffer Compiler failed with the following error(s):");
/*  99 */         for (String error : e.getErrors()) {
/* 100 */           getLog().error("");
/* 101 */           getLog().error(error);
/*     */         }
/* 103 */         getLog().error("");
/* 104 */         throw new MojoExecutionException("Compile failed.  For more details see error messages listed above.", e);
/*     */       }
/*     */     }
/*     */ 
/* 108 */     this.project.addCompileSourceRoot(this.outputDirectory.getAbsolutePath());
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.ProtoMojo
 * JD-Core Version:    0.6.2
 */