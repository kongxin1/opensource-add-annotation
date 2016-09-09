/*      */ package org.apache.activemq.protobuf.compiler.parser;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ 
/*      */ public class ProtoParserTokenManager
/*      */   implements ProtoParserConstants
/*      */ {
/*   31 */   public PrintStream debugStream = System.out;
/*      */ 
/*  502 */   static final long[] jjbitVec0 = { 0L, 0L, -1L, -1L };
/*      */ 
/*  911 */   static final int[] jjnextStates = { 24, 25, 30, 31, 34, 35, 8, 0, 3, 23, 38, 10, 11, 13, 39, 41, 2, 4, 5, 8, 10, 11, 18, 13, 26, 27, 8, 34, 35, 8, 6, 7, 12, 14, 17, 19, 28, 29, 32, 33, 36, 37 };
/*      */ 
/*  916 */   public static final String[] jjstrLiteralImages = { "", null, null, null, null, null, null, null, "import", "package", "service", "rpc", "option", "message", "extensions", "extend", "enum", "group", "required", "optional", "repeated", "returns", "to", "max", "{", "}", "=", ";", "[", "]", "(", ")", ".", ",", null, null, null, null, null, null, null, null };
/*      */ 
/*  924 */   public static final String[] lexStateNames = { "DEFAULT", "COMMENT" };
/*      */ 
/*  928 */   public static final int[] jjnewLexState = { -1, -1, -1, -1, -1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
/*      */ 
/*  932 */   static final long[] jjtoToken = { 3607772528385L };
/*      */ 
/*  935 */   static final long[] jjtoSkip = { 126L };
/*      */ 
/*  938 */   static final long[] jjtoSpecial = { 96L };
/*      */ 
/*  941 */   static final long[] jjtoMore = { 128L };
/*      */   protected SimpleCharStream input_stream;
/*  945 */   private final int[] jjrounds = new int[43];
/*  946 */   private final int[] jjstateSet = new int[86];
/*      */   StringBuffer image;
/*      */   int jjimageLen;
/*      */   int lengthOfMatch;
/*      */   protected char curChar;
/* 1000 */   int curLexState = 0;
/* 1001 */   int defaultLexState = 0;
/*      */   int jjnewStateCnt;
/*      */   int jjround;
/*      */   int jjmatchedPos;
/*      */   int jjmatchedKind;
/*      */ 
/*      */   public void setDebugStream(PrintStream ds)
/*      */   {
/*   32 */     this.debugStream = ds;
/*      */   }
/*      */   private final int jjStopStringLiteralDfa_0(int pos, long active0) {
/*   35 */     switch (pos)
/*      */     {
/*      */     case 0:
/*   38 */       if ((active0 & 0xFFFF00) != 0L)
/*      */       {
/*   40 */         this.jjmatchedKind = 41;
/*   41 */         return 22;
/*      */       }
/*   43 */       if ((active0 & 0x0) != 0L)
/*   44 */         return 4;
/*   45 */       return -1;
/*      */     case 1:
/*   47 */       if ((active0 & 0x400000) != 0L)
/*   48 */         return 22;
/*   49 */       if ((active0 & 0xBFFF00) != 0L)
/*      */       {
/*   51 */         this.jjmatchedKind = 41;
/*   52 */         this.jjmatchedPos = 1;
/*   53 */         return 22;
/*      */       }
/*   55 */       return -1;
/*      */     case 2:
/*   57 */       if ((active0 & 0x800800) != 0L)
/*   58 */         return 22;
/*   59 */       if ((active0 & 0x3FF700) != 0L)
/*      */       {
/*   61 */         this.jjmatchedKind = 41;
/*   62 */         this.jjmatchedPos = 2;
/*   63 */         return 22;
/*      */       }
/*   65 */       return -1;
/*      */     case 3:
/*   67 */       if ((active0 & 0x10000) != 0L)
/*   68 */         return 22;
/*   69 */       if ((active0 & 0x3EF700) != 0L)
/*      */       {
/*   71 */         this.jjmatchedKind = 41;
/*   72 */         this.jjmatchedPos = 3;
/*   73 */         return 22;
/*      */       }
/*   75 */       return -1;
/*      */     case 4:
/*   77 */       if ((active0 & 0x20000) != 0L)
/*   78 */         return 22;
/*   79 */       if ((active0 & 0x3CF700) != 0L)
/*      */       {
/*   81 */         this.jjmatchedKind = 41;
/*   82 */         this.jjmatchedPos = 4;
/*   83 */         return 22;
/*      */       }
/*   85 */       return -1;
/*      */     case 5:
/*   87 */       if ((active0 & 0x89100) != 0L)
/*   88 */         return 22;
/*   89 */       if ((active0 & 0x346600) != 0L)
/*      */       {
/*   91 */         if (this.jjmatchedPos != 5)
/*      */         {
/*   93 */           this.jjmatchedKind = 41;
/*   94 */           this.jjmatchedPos = 5;
/*      */         }
/*   96 */         return 22;
/*      */       }
/*   98 */       return -1;
/*      */     case 6:
/*  100 */       if ((active0 & 0x202600) != 0L)
/*  101 */         return 22;
/*  102 */       if ((active0 & 0x1C4000) != 0L)
/*      */       {
/*  104 */         this.jjmatchedKind = 41;
/*  105 */         this.jjmatchedPos = 6;
/*  106 */         return 22;
/*      */       }
/*  108 */       return -1;
/*      */     case 7:
/*  110 */       if ((active0 & 0x1C0000) != 0L)
/*  111 */         return 22;
/*  112 */       if ((active0 & 0x4000) != 0L)
/*      */       {
/*  114 */         this.jjmatchedKind = 41;
/*  115 */         this.jjmatchedPos = 7;
/*  116 */         return 22;
/*      */       }
/*  118 */       return -1;
/*      */     case 8:
/*  120 */       if ((active0 & 0x4000) != 0L)
/*      */       {
/*  122 */         this.jjmatchedKind = 41;
/*  123 */         this.jjmatchedPos = 8;
/*  124 */         return 22;
/*      */       }
/*  126 */       return -1;
/*      */     }
/*  128 */     return -1;
/*      */   }
/*      */ 
/*      */   private final int jjStartNfa_0(int pos, long active0)
/*      */   {
/*  133 */     return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
/*      */   }
/*      */ 
/*      */   private final int jjStopAtPos(int pos, int kind) {
/*  137 */     this.jjmatchedKind = kind;
/*  138 */     this.jjmatchedPos = pos;
/*  139 */     return pos + 1;
/*      */   }
/*      */ 
/*      */   private final int jjStartNfaWithStates_0(int pos, int kind, int state) {
/*  143 */     this.jjmatchedKind = kind;
/*  144 */     this.jjmatchedPos = pos;
/*      */     try { this.curChar = this.input_stream.readChar(); } catch (IOException e) {
/*  146 */       return pos + 1;
/*  147 */     }return jjMoveNfa_0(state, pos + 1);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa0_0() {
/*  151 */     switch (this.curChar)
/*      */     {
/*      */     case '(':
/*  154 */       return jjStopAtPos(0, 30);
/*      */     case ')':
/*  156 */       return jjStopAtPos(0, 31);
/*      */     case ',':
/*  158 */       return jjStopAtPos(0, 33);
/*      */     case '.':
/*  160 */       return jjStartNfaWithStates_0(0, 32, 4);
/*      */     case '/':
/*  162 */       return jjMoveStringLiteralDfa1_0(32L);
/*      */     case ';':
/*  164 */       return jjStopAtPos(0, 27);
/*      */     case '=':
/*  166 */       return jjStopAtPos(0, 26);
/*      */     case '[':
/*  168 */       return jjStopAtPos(0, 28);
/*      */     case ']':
/*  170 */       return jjStopAtPos(0, 29);
/*      */     case 'e':
/*  172 */       return jjMoveStringLiteralDfa1_0(114688L);
/*      */     case 'g':
/*  174 */       return jjMoveStringLiteralDfa1_0(131072L);
/*      */     case 'i':
/*  176 */       return jjMoveStringLiteralDfa1_0(256L);
/*      */     case 'm':
/*  178 */       return jjMoveStringLiteralDfa1_0(8396800L);
/*      */     case 'o':
/*  180 */       return jjMoveStringLiteralDfa1_0(528384L);
/*      */     case 'p':
/*  182 */       return jjMoveStringLiteralDfa1_0(512L);
/*      */     case 'r':
/*  184 */       return jjMoveStringLiteralDfa1_0(3409920L);
/*      */     case 's':
/*  186 */       return jjMoveStringLiteralDfa1_0(1024L);
/*      */     case 't':
/*  188 */       return jjMoveStringLiteralDfa1_0(4194304L);
/*      */     case '{':
/*  190 */       return jjStopAtPos(0, 24);
/*      */     case '}':
/*  192 */       return jjStopAtPos(0, 25);
/*      */     case '*':
/*      */     case '+':
/*      */     case '-':
/*      */     case '0':
/*      */     case '1':
/*      */     case '2':
/*      */     case '3':
/*      */     case '4':
/*      */     case '5':
/*      */     case '6':
/*      */     case '7':
/*      */     case '8':
/*      */     case '9':
/*      */     case ':':
/*      */     case '<':
/*      */     case '>':
/*      */     case '?':
/*      */     case '@':
/*      */     case 'A':
/*      */     case 'B':
/*      */     case 'C':
/*      */     case 'D':
/*      */     case 'E':
/*      */     case 'F':
/*      */     case 'G':
/*      */     case 'H':
/*      */     case 'I':
/*      */     case 'J':
/*      */     case 'K':
/*      */     case 'L':
/*      */     case 'M':
/*      */     case 'N':
/*      */     case 'O':
/*      */     case 'P':
/*      */     case 'Q':
/*      */     case 'R':
/*      */     case 'S':
/*      */     case 'T':
/*      */     case 'U':
/*      */     case 'V':
/*      */     case 'W':
/*      */     case 'X':
/*      */     case 'Y':
/*      */     case 'Z':
/*      */     case '\\':
/*      */     case '^':
/*      */     case '_':
/*      */     case '`':
/*      */     case 'a':
/*      */     case 'b':
/*      */     case 'c':
/*      */     case 'd':
/*      */     case 'f':
/*      */     case 'h':
/*      */     case 'j':
/*      */     case 'k':
/*      */     case 'l':
/*      */     case 'n':
/*      */     case 'q':
/*      */     case 'u':
/*      */     case 'v':
/*      */     case 'w':
/*      */     case 'x':
/*      */     case 'y':
/*      */     case 'z':
/*  194 */     case '|': } return jjMoveNfa_0(9, 0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa1_0(long active0) {
/*      */     try {
/*  199 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  201 */       jjStopStringLiteralDfa_0(0, active0);
/*  202 */       return 1;
/*      */     }
/*  204 */     switch (this.curChar)
/*      */     {
/*      */     case '/':
/*  207 */       if ((active0 & 0x20) != 0L)
/*  208 */         return jjStopAtPos(1, 5);
/*      */       break;
/*      */     case 'a':
/*  211 */       return jjMoveStringLiteralDfa2_0(active0, 8389120L);
/*      */     case 'e':
/*  213 */       return jjMoveStringLiteralDfa2_0(active0, 3417088L);
/*      */     case 'm':
/*  215 */       return jjMoveStringLiteralDfa2_0(active0, 256L);
/*      */     case 'n':
/*  217 */       return jjMoveStringLiteralDfa2_0(active0, 65536L);
/*      */     case 'o':
/*  219 */       if ((active0 & 0x400000) != 0L)
/*  220 */         return jjStartNfaWithStates_0(1, 22, 22);
/*      */       break;
/*      */     case 'p':
/*  223 */       return jjMoveStringLiteralDfa2_0(active0, 530432L);
/*      */     case 'r':
/*  225 */       return jjMoveStringLiteralDfa2_0(active0, 131072L);
/*      */     case 'x':
/*  227 */       return jjMoveStringLiteralDfa2_0(active0, 49152L);
/*      */     }
/*      */ 
/*  231 */     return jjStartNfa_0(0, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa2_0(long old0, long active0) {
/*  235 */     if ((active0 &= old0) == 0L)
/*  236 */       return jjStartNfa_0(0, old0); try {
/*  237 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  239 */       jjStopStringLiteralDfa_0(1, active0);
/*  240 */       return 2;
/*      */     }
/*  242 */     switch (this.curChar)
/*      */     {
/*      */     case 'c':
/*  245 */       if ((active0 & 0x800) != 0L)
/*  246 */         return jjStartNfaWithStates_0(2, 11, 22);
/*  247 */       return jjMoveStringLiteralDfa3_0(active0, 512L);
/*      */     case 'o':
/*  249 */       return jjMoveStringLiteralDfa3_0(active0, 131072L);
/*      */     case 'p':
/*  251 */       return jjMoveStringLiteralDfa3_0(active0, 1048832L);
/*      */     case 'q':
/*  253 */       return jjMoveStringLiteralDfa3_0(active0, 262144L);
/*      */     case 'r':
/*  255 */       return jjMoveStringLiteralDfa3_0(active0, 1024L);
/*      */     case 's':
/*  257 */       return jjMoveStringLiteralDfa3_0(active0, 8192L);
/*      */     case 't':
/*  259 */       return jjMoveStringLiteralDfa3_0(active0, 2674688L);
/*      */     case 'u':
/*  261 */       return jjMoveStringLiteralDfa3_0(active0, 65536L);
/*      */     case 'x':
/*  263 */       if ((active0 & 0x800000) != 0L)
/*  264 */         return jjStartNfaWithStates_0(2, 23, 22); break;
/*      */     case 'd':
/*      */     case 'e':
/*      */     case 'f':
/*      */     case 'g':
/*      */     case 'h':
/*      */     case 'i':
/*      */     case 'j':
/*      */     case 'k':
/*      */     case 'l':
/*      */     case 'm':
/*      */     case 'n':
/*      */     case 'v':
/*  269 */     case 'w': } return jjStartNfa_0(1, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa3_0(long old0, long active0) {
/*  273 */     if ((active0 &= old0) == 0L)
/*  274 */       return jjStartNfa_0(1, old0); try {
/*  275 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  277 */       jjStopStringLiteralDfa_0(2, active0);
/*  278 */       return 3;
/*      */     }
/*  280 */     switch (this.curChar)
/*      */     {
/*      */     case 'e':
/*  283 */       return jjMoveStringLiteralDfa4_0(active0, 1097728L);
/*      */     case 'i':
/*  285 */       return jjMoveStringLiteralDfa4_0(active0, 528384L);
/*      */     case 'k':
/*  287 */       return jjMoveStringLiteralDfa4_0(active0, 512L);
/*      */     case 'm':
/*  289 */       if ((active0 & 0x10000) != 0L)
/*  290 */         return jjStartNfaWithStates_0(3, 16, 22);
/*      */       break;
/*      */     case 'o':
/*  293 */       return jjMoveStringLiteralDfa4_0(active0, 256L);
/*      */     case 's':
/*  295 */       return jjMoveStringLiteralDfa4_0(active0, 8192L);
/*      */     case 'u':
/*  297 */       return jjMoveStringLiteralDfa4_0(active0, 2490368L);
/*      */     case 'v':
/*  299 */       return jjMoveStringLiteralDfa4_0(active0, 1024L);
/*      */     case 'f':
/*      */     case 'g':
/*      */     case 'h':
/*      */     case 'j':
/*      */     case 'l':
/*      */     case 'n':
/*      */     case 'p':
/*      */     case 'q':
/*      */     case 'r':
/*  303 */     case 't': } return jjStartNfa_0(2, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa4_0(long old0, long active0) {
/*  307 */     if ((active0 &= old0) == 0L)
/*  308 */       return jjStartNfa_0(2, old0); try {
/*  309 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  311 */       jjStopStringLiteralDfa_0(3, active0);
/*  312 */       return 4;
/*      */     }
/*  314 */     switch (this.curChar)
/*      */     {
/*      */     case 'a':
/*  317 */       return jjMoveStringLiteralDfa5_0(active0, 1057280L);
/*      */     case 'i':
/*  319 */       return jjMoveStringLiteralDfa5_0(active0, 263168L);
/*      */     case 'n':
/*  321 */       return jjMoveStringLiteralDfa5_0(active0, 49152L);
/*      */     case 'o':
/*  323 */       return jjMoveStringLiteralDfa5_0(active0, 528384L);
/*      */     case 'p':
/*  325 */       if ((active0 & 0x20000) != 0L)
/*  326 */         return jjStartNfaWithStates_0(4, 17, 22);
/*      */       break;
/*      */     case 'r':
/*  329 */       return jjMoveStringLiteralDfa5_0(active0, 2097408L);
/*      */     case 'b':
/*      */     case 'c':
/*      */     case 'd':
/*      */     case 'e':
/*      */     case 'f':
/*      */     case 'g':
/*      */     case 'h':
/*      */     case 'j':
/*      */     case 'k':
/*      */     case 'l':
/*      */     case 'm':
/*  333 */     case 'q': } return jjStartNfa_0(3, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa5_0(long old0, long active0) {
/*  337 */     if ((active0 &= old0) == 0L)
/*  338 */       return jjStartNfa_0(3, old0); try {
/*  339 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  341 */       jjStopStringLiteralDfa_0(4, active0);
/*  342 */       return 5;
/*      */     }
/*  344 */     switch (this.curChar)
/*      */     {
/*      */     case 'c':
/*  347 */       return jjMoveStringLiteralDfa6_0(active0, 1024L);
/*      */     case 'd':
/*  349 */       if ((active0 & 0x8000) != 0L)
/*  350 */         return jjStartNfaWithStates_0(5, 15, 22);
/*      */       break;
/*      */     case 'g':
/*  353 */       return jjMoveStringLiteralDfa6_0(active0, 8704L);
/*      */     case 'n':
/*  355 */       if ((active0 & 0x1000) != 0L)
/*      */       {
/*  357 */         this.jjmatchedKind = 12;
/*  358 */         this.jjmatchedPos = 5;
/*      */       }
/*  360 */       return jjMoveStringLiteralDfa6_0(active0, 2621440L);
/*      */     case 'r':
/*  362 */       return jjMoveStringLiteralDfa6_0(active0, 262144L);
/*      */     case 's':
/*  364 */       return jjMoveStringLiteralDfa6_0(active0, 16384L);
/*      */     case 't':
/*  366 */       if ((active0 & 0x100) != 0L)
/*  367 */         return jjStartNfaWithStates_0(5, 8, 22);
/*  368 */       return jjMoveStringLiteralDfa6_0(active0, 1048576L);
/*      */     case 'e':
/*      */     case 'f':
/*      */     case 'h':
/*      */     case 'i':
/*      */     case 'j':
/*      */     case 'k':
/*      */     case 'l':
/*      */     case 'm':
/*      */     case 'o':
/*      */     case 'p':
/*  372 */     case 'q': } return jjStartNfa_0(4, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa6_0(long old0, long active0) {
/*  376 */     if ((active0 &= old0) == 0L)
/*  377 */       return jjStartNfa_0(4, old0); try {
/*  378 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  380 */       jjStopStringLiteralDfa_0(5, active0);
/*  381 */       return 6;
/*      */     }
/*  383 */     switch (this.curChar)
/*      */     {
/*      */     case 'a':
/*  386 */       return jjMoveStringLiteralDfa7_0(active0, 524288L);
/*      */     case 'e':
/*  388 */       if ((active0 & 0x200) != 0L)
/*  389 */         return jjStartNfaWithStates_0(6, 9, 22);
/*  390 */       if ((active0 & 0x400) != 0L)
/*  391 */         return jjStartNfaWithStates_0(6, 10, 22);
/*  392 */       if ((active0 & 0x2000) != 0L)
/*  393 */         return jjStartNfaWithStates_0(6, 13, 22);
/*  394 */       return jjMoveStringLiteralDfa7_0(active0, 1310720L);
/*      */     case 'i':
/*  396 */       return jjMoveStringLiteralDfa7_0(active0, 16384L);
/*      */     case 's':
/*  398 */       if ((active0 & 0x200000) != 0L) {
/*  399 */         return jjStartNfaWithStates_0(6, 21, 22);
/*      */       }
/*      */       break;
/*      */     }
/*      */ 
/*  404 */     return jjStartNfa_0(5, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa7_0(long old0, long active0) {
/*  408 */     if ((active0 &= old0) == 0L)
/*  409 */       return jjStartNfa_0(5, old0); try {
/*  410 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  412 */       jjStopStringLiteralDfa_0(6, active0);
/*  413 */       return 7;
/*      */     }
/*  415 */     switch (this.curChar)
/*      */     {
/*      */     case 'd':
/*  418 */       if ((active0 & 0x40000) != 0L)
/*  419 */         return jjStartNfaWithStates_0(7, 18, 22);
/*  420 */       if ((active0 & 0x100000) != 0L)
/*  421 */         return jjStartNfaWithStates_0(7, 20, 22);
/*      */       break;
/*      */     case 'l':
/*  424 */       if ((active0 & 0x80000) != 0L)
/*  425 */         return jjStartNfaWithStates_0(7, 19, 22);
/*      */       break;
/*      */     case 'o':
/*  428 */       return jjMoveStringLiteralDfa8_0(active0, 16384L);
/*      */     }
/*      */ 
/*  432 */     return jjStartNfa_0(6, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa8_0(long old0, long active0) {
/*  436 */     if ((active0 &= old0) == 0L)
/*  437 */       return jjStartNfa_0(6, old0); try {
/*  438 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  440 */       jjStopStringLiteralDfa_0(7, active0);
/*  441 */       return 8;
/*      */     }
/*  443 */     switch (this.curChar)
/*      */     {
/*      */     case 'n':
/*  446 */       return jjMoveStringLiteralDfa9_0(active0, 16384L);
/*      */     }
/*      */ 
/*  450 */     return jjStartNfa_0(7, active0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa9_0(long old0, long active0) {
/*  454 */     if ((active0 &= old0) == 0L)
/*  455 */       return jjStartNfa_0(7, old0); try {
/*  456 */       this.curChar = this.input_stream.readChar();
/*      */     } catch (IOException e) {
/*  458 */       jjStopStringLiteralDfa_0(8, active0);
/*  459 */       return 9;
/*      */     }
/*  461 */     switch (this.curChar)
/*      */     {
/*      */     case 's':
/*  464 */       if ((active0 & 0x4000) != 0L) {
/*  465 */         return jjStartNfaWithStates_0(9, 14, 22);
/*      */       }
/*      */       break;
/*      */     }
/*      */ 
/*  470 */     return jjStartNfa_0(8, active0);
/*      */   }
/*      */ 
/*      */   private final void jjCheckNAdd(int state) {
/*  474 */     if (this.jjrounds[state] != this.jjround)
/*      */     {
/*  476 */       this.jjstateSet[(this.jjnewStateCnt++)] = state;
/*  477 */       this.jjrounds[state] = this.jjround;
/*      */     }
/*      */   }
/*      */ 
/*      */   private final void jjAddStates(int start, int end) {
/*      */     do
/*  483 */       this.jjstateSet[(this.jjnewStateCnt++)] = jjnextStates[start];
/*  484 */     while (start++ != end);
/*      */   }
/*      */ 
/*      */   private final void jjCheckNAddTwoStates(int state1, int state2) {
/*  488 */     jjCheckNAdd(state1);
/*  489 */     jjCheckNAdd(state2);
/*      */   }
/*      */ 
/*      */   private final void jjCheckNAddStates(int start, int end) {
/*      */     do
/*  494 */       jjCheckNAdd(jjnextStates[start]);
/*  495 */     while (start++ != end);
/*      */   }
/*      */ 
/*      */   private final void jjCheckNAddStates(int start) {
/*  499 */     jjCheckNAdd(jjnextStates[start]);
/*  500 */     jjCheckNAdd(jjnextStates[(start + 1)]);
/*      */   }
/*      */ 
/*      */   private final int jjMoveNfa_0(int startState, int curPos)
/*      */   {
/*  508 */     int startsAt = 0;
/*  509 */     this.jjnewStateCnt = 43;
/*  510 */     int i = 1;
/*  511 */     this.jjstateSet[0] = startState;
/*  512 */     int kind = 2147483647;
/*      */     while (true)
/*      */     {
/*  515 */       if (++this.jjround == 2147483647)
/*  516 */         ReInitRounds();
/*  517 */       if (this.curChar < '@')
/*      */       {
/*  519 */         long l = 1L << this.curChar;
/*      */         do
/*      */         {
/*  522 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           case 9:
/*  525 */             if ((0x0 & l) != 0L)
/*  526 */               jjCheckNAddStates(0, 6);
/*  527 */             else if (this.curChar == '-')
/*  528 */               jjAddStates(7, 10);
/*  529 */             else if (this.curChar == '"')
/*  530 */               jjCheckNAddStates(11, 13);
/*  531 */             else if (this.curChar == '.')
/*  532 */               jjCheckNAdd(4);
/*  533 */             if ((0x0 & l) != 0L)
/*      */             {
/*  535 */               if (kind > 34)
/*  536 */                 kind = 34;
/*  537 */               jjCheckNAddTwoStates(1, 2);
/*      */             }
/*  539 */             else if (this.curChar == '0')
/*      */             {
/*  541 */               if (kind > 34)
/*  542 */                 kind = 34;
/*  543 */               jjCheckNAddStates(14, 16); } break;
/*      */           case 0:
/*  547 */             if ((0x0 & l) != 0L)
/*      */             {
/*  549 */               if (kind > 34)
/*  550 */                 kind = 34;
/*  551 */               jjCheckNAddTwoStates(1, 2);
/*  552 */             }break;
/*      */           case 1:
/*  554 */             if ((0x0 & l) != 0L)
/*      */             {
/*  556 */               if (kind > 34)
/*  557 */                 kind = 34;
/*  558 */               jjCheckNAddTwoStates(1, 2);
/*  559 */             }break;
/*      */           case 3:
/*  561 */             if (this.curChar == '.')
/*  562 */               jjCheckNAdd(4); break;
/*      */           case 4:
/*  565 */             if ((0x0 & l) != 0L)
/*      */             {
/*  567 */               if (kind > 38)
/*  568 */                 kind = 38;
/*  569 */               jjCheckNAddStates(17, 19);
/*  570 */             }break;
/*      */           case 6:
/*  572 */             if ((0x0 & l) != 0L)
/*  573 */               jjCheckNAdd(7); break;
/*      */           case 7:
/*  576 */             if ((0x0 & l) != 0L)
/*      */             {
/*  578 */               if (kind > 38)
/*  579 */                 kind = 38;
/*  580 */               jjCheckNAddTwoStates(7, 8);
/*  581 */             }break;
/*      */           case 10:
/*  583 */             if ((0xFFFFDBFF & l) != 0L)
/*  584 */               jjCheckNAddStates(11, 13); break;
/*      */           case 12:
/*  587 */             if ((0x0 & l) != 0L)
/*  588 */               jjCheckNAddStates(11, 13); break;
/*      */           case 13:
/*  591 */             if ((this.curChar == '"') && (kind > 40))
/*  592 */               kind = 40; break;
/*      */           case 15:
/*  595 */             if ((0x0 & l) != 0L)
/*  596 */               this.jjstateSet[(this.jjnewStateCnt++)] = 16; break;
/*      */           case 16:
/*  599 */             if ((0x0 & l) != 0L)
/*  600 */               jjCheckNAddStates(11, 13); break;
/*      */           case 17:
/*  603 */             if ((0x0 & l) != 0L)
/*  604 */               jjCheckNAddStates(20, 23); break;
/*      */           case 18:
/*  607 */             if ((0x0 & l) != 0L)
/*  608 */               jjCheckNAddStates(11, 13); break;
/*      */           case 19:
/*  611 */             if ((0x0 & l) != 0L)
/*  612 */               this.jjstateSet[(this.jjnewStateCnt++)] = 20; break;
/*      */           case 20:
/*  615 */             if ((0x0 & l) != 0L)
/*  616 */               jjCheckNAdd(18); break;
/*      */           case 22:
/*  619 */             if ((0x0 & l) != 0L)
/*      */             {
/*  621 */               if (kind > 41)
/*  622 */                 kind = 41;
/*  623 */               this.jjstateSet[(this.jjnewStateCnt++)] = 22;
/*  624 */             }break;
/*      */           case 23:
/*  626 */             if ((0x0 & l) != 0L)
/*  627 */               jjCheckNAddStates(0, 6); break;
/*      */           case 24:
/*  630 */             if ((0x0 & l) != 0L)
/*  631 */               jjCheckNAddTwoStates(24, 25); break;
/*      */           case 25:
/*  634 */             if (this.curChar == '.')
/*      */             {
/*  636 */               if (kind > 38)
/*  637 */                 kind = 38;
/*  638 */               jjCheckNAddStates(24, 26);
/*  639 */             }break;
/*      */           case 26:
/*  641 */             if ((0x0 & l) != 0L)
/*      */             {
/*  643 */               if (kind > 38)
/*  644 */                 kind = 38;
/*  645 */               jjCheckNAddStates(24, 26);
/*  646 */             }break;
/*      */           case 28:
/*  648 */             if ((0x0 & l) != 0L)
/*  649 */               jjCheckNAdd(29); break;
/*      */           case 29:
/*  652 */             if ((0x0 & l) != 0L)
/*      */             {
/*  654 */               if (kind > 38)
/*  655 */                 kind = 38;
/*  656 */               jjCheckNAddTwoStates(29, 8);
/*  657 */             }break;
/*      */           case 30:
/*  659 */             if ((0x0 & l) != 0L)
/*  660 */               jjCheckNAddTwoStates(30, 31); break;
/*      */           case 32:
/*  663 */             if ((0x0 & l) != 0L)
/*  664 */               jjCheckNAdd(33); break;
/*      */           case 33:
/*  667 */             if ((0x0 & l) != 0L)
/*      */             {
/*  669 */               if (kind > 38)
/*  670 */                 kind = 38;
/*  671 */               jjCheckNAddTwoStates(33, 8);
/*  672 */             }break;
/*      */           case 34:
/*  674 */             if ((0x0 & l) != 0L)
/*  675 */               jjCheckNAddStates(27, 29); break;
/*      */           case 36:
/*  678 */             if ((0x0 & l) != 0L)
/*  679 */               jjCheckNAdd(37); break;
/*      */           case 37:
/*  682 */             if ((0x0 & l) != 0L)
/*  683 */               jjCheckNAddTwoStates(37, 8); break;
/*      */           case 38:
/*  686 */             if (this.curChar == '0')
/*      */             {
/*  688 */               if (kind > 34)
/*  689 */                 kind = 34;
/*  690 */               jjCheckNAddStates(14, 16);
/*  691 */             }break;
/*      */           case 40:
/*  693 */             if ((0x0 & l) != 0L)
/*      */             {
/*  695 */               if (kind > 34)
/*  696 */                 kind = 34;
/*  697 */               jjCheckNAddTwoStates(40, 2);
/*  698 */             }break;
/*      */           case 41:
/*  700 */             if ((0x0 & l) != 0L)
/*      */             {
/*  702 */               if (kind > 34)
/*  703 */                 kind = 34;
/*  704 */               jjCheckNAddTwoStates(41, 2);
/*  705 */             }break;
/*      */           case 42:
/*  707 */             if (this.curChar == '-')
/*  708 */               jjAddStates(7, 10); break;
/*      */           case 2:
/*      */           case 5:
/*      */           case 8:
/*      */           case 11:
/*      */           case 14:
/*      */           case 21:
/*      */           case 27:
/*      */           case 31:
/*      */           case 35:
/*  712 */           case 39: }  } while (i != startsAt);
/*      */       }
/*  714 */       else if (this.curChar < '')
/*      */       {
/*  716 */         long l = 1L << (this.curChar & 0x3F);
/*      */         do
/*      */         {
/*  719 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           case 9:
/*  722 */             if ((0x7FFFFFE & l) != 0L)
/*      */             {
/*  724 */               if (kind > 41)
/*  725 */                 kind = 41;
/*  726 */               jjCheckNAdd(22);
/*  727 */             }break;
/*      */           case 2:
/*  729 */             if (((0x1000 & l) != 0L) && (kind > 34))
/*  730 */               kind = 34; break;
/*      */           case 5:
/*  733 */             if ((0x20 & l) != 0L)
/*  734 */               jjAddStates(30, 31); break;
/*      */           case 8:
/*  737 */             if (((0x50 & l) != 0L) && (kind > 38))
/*  738 */               kind = 38; break;
/*      */           case 10:
/*  741 */             if ((0xEFFFFFFF & l) != 0L)
/*  742 */               jjCheckNAddStates(11, 13); break;
/*      */           case 11:
/*  745 */             if (this.curChar == '\\')
/*  746 */               jjAddStates(32, 35); break;
/*      */           case 12:
/*  749 */             if ((0x10000000 & l) != 0L)
/*  750 */               jjCheckNAddStates(11, 13); break;
/*      */           case 14:
/*  753 */             if ((0x1000000 & l) != 0L)
/*  754 */               this.jjstateSet[(this.jjnewStateCnt++)] = 15; break;
/*      */           case 15:
/*  757 */             if ((0x7E & l) != 0L)
/*  758 */               this.jjstateSet[(this.jjnewStateCnt++)] = 16; break;
/*      */           case 16:
/*  761 */             if ((0x7E & l) != 0L)
/*  762 */               jjCheckNAddStates(11, 13); break;
/*      */           case 22:
/*  765 */             if ((0x87FFFFFE & l) != 0L)
/*      */             {
/*  767 */               if (kind > 41)
/*  768 */                 kind = 41;
/*  769 */               jjCheckNAdd(22);
/*  770 */             }break;
/*      */           case 27:
/*  772 */             if ((0x20 & l) != 0L)
/*  773 */               jjAddStates(36, 37); break;
/*      */           case 31:
/*  776 */             if ((0x20 & l) != 0L)
/*  777 */               jjAddStates(38, 39); break;
/*      */           case 35:
/*  780 */             if ((0x20 & l) != 0L)
/*  781 */               jjAddStates(40, 41); break;
/*      */           case 39:
/*  784 */             if ((0x1000000 & l) != 0L)
/*  785 */               jjCheckNAdd(40); break;
/*      */           case 40:
/*  788 */             if ((0x7E & l) != 0L)
/*      */             {
/*  790 */               if (kind > 34)
/*  791 */                 kind = 34;
/*  792 */               jjCheckNAddTwoStates(40, 2); } break;
/*      */           case 3:
/*      */           case 4:
/*      */           case 6:
/*      */           case 7:
/*      */           case 13:
/*      */           case 17:
/*      */           case 18:
/*      */           case 19:
/*      */           case 20:
/*      */           case 21:
/*      */           case 23:
/*      */           case 24:
/*      */           case 25:
/*      */           case 26:
/*      */           case 28:
/*      */           case 29:
/*      */           case 30:
/*      */           case 32:
/*      */           case 33:
/*      */           case 34:
/*      */           case 36:
/*      */           case 37:
/*  796 */           case 38: }  } while (i != startsAt);
/*      */       }
/*      */       else
/*      */       {
/*  800 */         int i2 = (this.curChar & 0xFF) >> '\006';
/*  801 */         long l2 = 1L << (this.curChar & 0x3F);
/*      */         do
/*      */         {
/*  804 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           case 10:
/*  807 */             if ((jjbitVec0[i2] & l2) != 0L)
/*  808 */               jjAddStates(11, 13);
/*      */             break;
/*      */           }
/*      */         }
/*  812 */         while (i != startsAt);
/*      */       }
/*  814 */       if (kind != 2147483647)
/*      */       {
/*  816 */         this.jjmatchedKind = kind;
/*  817 */         this.jjmatchedPos = curPos;
/*  818 */         kind = 2147483647;
/*      */       }
/*  820 */       curPos++;
/*  821 */       if ((i = this.jjnewStateCnt) == (startsAt = 43 - (this.jjnewStateCnt = startsAt)))
/*  822 */         return curPos; try {
/*  823 */         this.curChar = this.input_stream.readChar(); } catch (IOException e) {  }
/*  824 */     }return curPos;
/*      */   }
/*      */ 
/*      */   private final int jjMoveStringLiteralDfa0_1()
/*      */   {
/*  829 */     return jjMoveNfa_1(0, 0);
/*      */   }
/*      */ 
/*      */   private final int jjMoveNfa_1(int startState, int curPos)
/*      */   {
/*  834 */     int startsAt = 0;
/*  835 */     this.jjnewStateCnt = 3;
/*  836 */     int i = 1;
/*  837 */     this.jjstateSet[0] = startState;
/*  838 */     int kind = 2147483647;
/*      */     while (true)
/*      */     {
/*  841 */       if (++this.jjround == 2147483647)
/*  842 */         ReInitRounds();
/*  843 */       if (this.curChar < '@')
/*      */       {
/*  845 */         long l = 1L << this.curChar;
/*      */         do
/*      */         {
/*  848 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           case 0:
/*  851 */             if ((0x2400 & l) != 0L)
/*      */             {
/*  853 */               if (kind > 6)
/*  854 */                 kind = 6;
/*      */             }
/*  856 */             if (this.curChar == '\r')
/*  857 */               this.jjstateSet[(this.jjnewStateCnt++)] = 1; break;
/*      */           case 1:
/*  860 */             if ((this.curChar == '\n') && (kind > 6))
/*  861 */               kind = 6; break;
/*      */           case 2:
/*  864 */             if (this.curChar == '\r')
/*  865 */               this.jjstateSet[(this.jjnewStateCnt++)] = 1;
/*      */             break;
/*      */           }
/*      */         }
/*  869 */         while (i != startsAt);
/*      */       }
/*  871 */       else if (this.curChar < '')
/*      */       {
/*  873 */         long l = 1L << (this.curChar & 0x3F);
/*      */         do
/*      */         {
/*  876 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           case 0:
/*  879 */             if (this.curChar == '|')
/*  880 */               kind = 6;
/*      */             break;
/*      */           }
/*      */         }
/*  884 */         while (i != startsAt);
/*      */       }
/*      */       else
/*      */       {
/*  888 */         int i2 = (this.curChar & 0xFF) >> '\006';
/*  889 */         long l2 = 1L << (this.curChar & 0x3F);
/*      */         do
/*      */         {
/*  892 */           switch (this.jjstateSet[(--i)])
/*      */           {
/*      */           }
/*      */         }
/*  896 */         while (i != startsAt);
/*      */       }
/*  898 */       if (kind != 2147483647)
/*      */       {
/*  900 */         this.jjmatchedKind = kind;
/*  901 */         this.jjmatchedPos = curPos;
/*  902 */         kind = 2147483647;
/*      */       }
/*  904 */       curPos++;
/*  905 */       if ((i = this.jjnewStateCnt) == (startsAt = 3 - (this.jjnewStateCnt = startsAt)))
/*  906 */         return curPos; try {
/*  907 */         this.curChar = this.input_stream.readChar(); } catch (IOException e) {  }
/*  908 */     }return curPos;
/*      */   }
/*      */ 
/*      */   public ProtoParserTokenManager(SimpleCharStream stream)
/*      */   {
/*  954 */     this.input_stream = stream;
/*      */   }
/*      */   public ProtoParserTokenManager(SimpleCharStream stream, int lexState) {
/*  957 */     this(stream);
/*  958 */     SwitchTo(lexState);
/*      */   }
/*      */ 
/*      */   public void ReInit(SimpleCharStream stream) {
/*  962 */     this.jjmatchedPos = (this.jjnewStateCnt = 0);
/*  963 */     this.curLexState = this.defaultLexState;
/*  964 */     this.input_stream = stream;
/*  965 */     ReInitRounds();
/*      */   }
/*      */ 
/*      */   private final void ReInitRounds()
/*      */   {
/*  970 */     this.jjround = -2147483647;
/*  971 */     for (int i = 43; i-- > 0; )
/*  972 */       this.jjrounds[i] = -2147483648;
/*      */   }
/*      */ 
/*      */   public void ReInit(SimpleCharStream stream, int lexState) {
/*  976 */     ReInit(stream);
/*  977 */     SwitchTo(lexState);
/*      */   }
/*      */ 
/*      */   public void SwitchTo(int lexState) {
/*  981 */     if ((lexState >= 2) || (lexState < 0)) {
/*  982 */       throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
/*      */     }
/*  984 */     this.curLexState = lexState;
/*      */   }
/*      */ 
/*      */   protected Token jjFillToken()
/*      */   {
/*  989 */     Token t = Token.newToken(this.jjmatchedKind);
/*  990 */     t.kind = this.jjmatchedKind;
/*  991 */     String im = jjstrLiteralImages[this.jjmatchedKind];
/*  992 */     t.image = (im == null ? this.input_stream.GetImage() : im);
/*  993 */     t.beginLine = this.input_stream.getBeginLine();
/*  994 */     t.beginColumn = this.input_stream.getBeginColumn();
/*  995 */     t.endLine = this.input_stream.getEndLine();
/*  996 */     t.endColumn = this.input_stream.getEndColumn();
/*  997 */     return t;
/*      */   }
/*      */ 
/*      */   public Token getNextToken()
/*      */   {
/* 1010 */     Token specialToken = null;
/*      */ 
/* 1012 */     int curPos = 0;
/*      */     try
/*      */     {
/* 1019 */       this.curChar = this.input_stream.BeginToken();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1023 */       this.jjmatchedKind = 0;
/* 1024 */       Token matchedToken = jjFillToken();
/* 1025 */       matchedToken.specialToken = specialToken;
/* 1026 */       return matchedToken;
/*      */     }
/* 1028 */     this.image = null;
/* 1029 */     this.jjimageLen = 0;
/*      */     while (true)
/*      */     {
/* 1033 */       switch (this.curLexState) {
/*      */       case 0:
/*      */         try {
/* 1036 */           this.input_stream.backup(0);
/* 1037 */           while ((this.curChar <= ' ') && ((0x2600 & 1L << this.curChar) != 0L))
/* 1038 */             this.curChar = this.input_stream.BeginToken(); 
/*      */         } catch (IOException e1) {  }
/*      */ 
/* 1040 */         break;
/* 1041 */         this.jjmatchedKind = 2147483647;
/* 1042 */         this.jjmatchedPos = 0;
/* 1043 */         curPos = jjMoveStringLiteralDfa0_0();
/* 1044 */         break;
/*      */       case 1:
/* 1046 */         this.jjmatchedKind = 2147483647;
/* 1047 */         this.jjmatchedPos = 0;
/* 1048 */         curPos = jjMoveStringLiteralDfa0_1();
/* 1049 */         if ((this.jjmatchedPos == 0) && (this.jjmatchedKind > 7))
/*      */         {
/* 1051 */           this.jjmatchedKind = 7;
/*      */         }
/*      */ 
/*      */       default:
/* 1055 */         if (this.jjmatchedKind != 2147483647)
/*      */         {
/* 1057 */           if (this.jjmatchedPos + 1 < curPos)
/* 1058 */             this.input_stream.backup(curPos - this.jjmatchedPos - 1);
/* 1059 */           if ((jjtoToken[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) != 0L)
/*      */           {
/* 1061 */             Token matchedToken = jjFillToken();
/* 1062 */             matchedToken.specialToken = specialToken;
/* 1063 */             if (jjnewLexState[this.jjmatchedKind] != -1)
/* 1064 */               this.curLexState = jjnewLexState[this.jjmatchedKind];
/* 1065 */             return matchedToken;
/*      */           }
/* 1067 */           if ((jjtoSkip[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) != 0L)
/*      */           {
/* 1069 */             if ((jjtoSpecial[(this.jjmatchedKind >> 6)] & 1L << (this.jjmatchedKind & 0x3F)) != 0L)
/*      */             {
/* 1071 */               Token matchedToken = jjFillToken();
/* 1072 */               if (specialToken == null) {
/* 1073 */                 specialToken = matchedToken;
/*      */               }
/*      */               else {
/* 1076 */                 matchedToken.specialToken = specialToken;
/* 1077 */                 specialToken = specialToken.next = matchedToken;
/*      */               }
/* 1079 */               SkipLexicalActions(matchedToken);
/*      */             }
/*      */             else {
/* 1082 */               SkipLexicalActions(null);
/* 1083 */             }if (jjnewLexState[this.jjmatchedKind] == -1) break;
/* 1084 */             this.curLexState = jjnewLexState[this.jjmatchedKind]; break;
/*      */           }
/*      */ 
/* 1087 */           this.jjimageLen += this.jjmatchedPos + 1;
/* 1088 */           if (jjnewLexState[this.jjmatchedKind] != -1)
/* 1089 */             this.curLexState = jjnewLexState[this.jjmatchedKind];
/* 1090 */           curPos = 0;
/* 1091 */           this.jjmatchedKind = 2147483647;
/*      */           try {
/* 1093 */             this.curChar = this.input_stream.readChar(); } catch (IOException e1) {  }
/*      */         }
/*      */         break;
/*      */       }
/*      */     }
/* 1098 */     int error_line = this.input_stream.getEndLine();
/* 1099 */     int error_column = this.input_stream.getEndColumn();
/* 1100 */     String error_after = null;
/* 1101 */     boolean EOFSeen = false;
/*      */     try { this.input_stream.readChar(); this.input_stream.backup(1);
/*      */     } catch (IOException e1) {
/* 1104 */       EOFSeen = true;
/* 1105 */       error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
/* 1106 */       if ((this.curChar == '\n') || (this.curChar == '\r')) {
/* 1107 */         error_line++;
/* 1108 */         error_column = 0;
/*      */       }
/*      */       else {
/* 1111 */         error_column++;
/*      */       }
/*      */     }
/* 1113 */     if (!EOFSeen) {
/* 1114 */       this.input_stream.backup(1);
/* 1115 */       error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
/*      */     }
/* 1117 */     throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
/*      */   }
/*      */ 
/*      */   void SkipLexicalActions(Token matchedToken)
/*      */   {
/* 1124 */     switch (this.jjmatchedKind)
/*      */     {
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.parser.ProtoParserTokenManager
 * JD-Core Version:    0.6.2
 */