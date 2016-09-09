/*     */ package org.apache.activemq.protobuf.compiler;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.math.BigInteger;
/*     */ import java.nio.CharBuffer;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.apache.activemq.protobuf.Buffer;
/*     */ import org.apache.activemq.protobuf.UTF8Buffer;
/*     */ 
/*     */ public final class TextFormat
/*     */ {
/*     */   private static final int BUFFER_SIZE = 4096;
/*     */ 
/*     */   private static String unsignedToString(int value)
/*     */   {
/*  43 */     if (value >= 0) {
/*  44 */       return Integer.toString(value);
/*     */     }
/*  46 */     return Long.toString(value & 0xFFFFFFFF);
/*     */   }
/*     */ 
/*     */   private static String unsignedToString(long value)
/*     */   {
/*  52 */     if (value >= 0L) {
/*  53 */       return Long.toString(value);
/*     */     }
/*     */ 
/*  57 */     return BigInteger.valueOf(value & 0xFFFFFFFF).setBit(63).toString();
/*     */   }
/*     */ 
/*     */   private static StringBuilder toStringBuilder(Readable input)
/*     */     throws IOException
/*     */   {
/* 453 */     StringBuilder text = new StringBuilder();
/* 454 */     CharBuffer buffer = CharBuffer.allocate(4096);
/*     */     while (true) {
/* 456 */       int n = input.read(buffer);
/* 457 */       if (n == -1) {
/*     */         break;
/*     */       }
/* 460 */       buffer.flip();
/* 461 */       text.append(buffer, 0, n);
/*     */     }
/* 463 */     return text;
/*     */   }
/*     */ 
/*     */   static String escapeBytes(Buffer input)
/*     */   {
/* 482 */     StringBuilder builder = new StringBuilder(input.getLength());
/* 483 */     for (int i = 0; i < input.getLength(); i++) {
/* 484 */       byte b = input.byteAt(i);
/* 485 */       switch (b) {
/*     */       case 7:
/* 487 */         builder.append("\\a"); break;
/*     */       case 8:
/* 488 */         builder.append("\\b"); break;
/*     */       case 12:
/* 489 */         builder.append("\\f"); break;
/*     */       case 10:
/* 490 */         builder.append("\\n"); break;
/*     */       case 13:
/* 491 */         builder.append("\\r"); break;
/*     */       case 9:
/* 492 */         builder.append("\\t"); break;
/*     */       case 11:
/* 493 */         builder.append("\\v"); break;
/*     */       case 92:
/* 494 */         builder.append("\\\\"); break;
/*     */       case 39:
/* 495 */         builder.append("\\'"); break;
/*     */       case 34:
/* 496 */         builder.append("\\\""); break;
/*     */       default:
/* 498 */         if (b >= 32) {
/* 499 */           builder.append((char)b);
/*     */         } else {
/* 501 */           builder.append('\\');
/* 502 */           builder.append((char)(48 + (b >>> 6 & 0x3)));
/* 503 */           builder.append((char)(48 + (b >>> 3 & 0x7)));
/* 504 */           builder.append((char)(48 + (b & 0x7)));
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 509 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   static Buffer unescapeBytes(CharSequence input)
/*     */     throws TextFormat.InvalidEscapeSequence
/*     */   {
/* 519 */     byte[] result = new byte[input.length()];
/* 520 */     int pos = 0;
/* 521 */     for (int i = 0; i < input.length(); i++) {
/* 522 */       char c = input.charAt(i);
/* 523 */       if (c == '\\') {
/* 524 */         if (i + 1 < input.length()) {
/* 525 */           i++;
/* 526 */           c = input.charAt(i);
/* 527 */           if (isOctal(c))
/*     */           {
/* 529 */             int code = digitValue(c);
/* 530 */             if ((i + 1 < input.length()) && (isOctal(input.charAt(i + 1)))) {
/* 531 */               i++;
/* 532 */               code = code * 8 + digitValue(input.charAt(i));
/*     */             }
/* 534 */             if ((i + 1 < input.length()) && (isOctal(input.charAt(i + 1)))) {
/* 535 */               i++;
/* 536 */               code = code * 8 + digitValue(input.charAt(i));
/*     */             }
/* 538 */             result[(pos++)] = ((byte)code);
/*     */           } else {
/* 540 */             switch (c) { case 'a':
/* 541 */               result[(pos++)] = 7; break;
/*     */             case 'b':
/* 542 */               result[(pos++)] = 8; break;
/*     */             case 'f':
/* 543 */               result[(pos++)] = 12; break;
/*     */             case 'n':
/* 544 */               result[(pos++)] = 10; break;
/*     */             case 'r':
/* 545 */               result[(pos++)] = 13; break;
/*     */             case 't':
/* 546 */               result[(pos++)] = 9; break;
/*     */             case 'v':
/* 547 */               result[(pos++)] = 11; break;
/*     */             case '\\':
/* 548 */               result[(pos++)] = 92; break;
/*     */             case '\'':
/* 549 */               result[(pos++)] = 39; break;
/*     */             case '"':
/* 550 */               result[(pos++)] = 34; break;
/*     */             case 'x':
/* 554 */               int code = 0;
/* 555 */               if ((i + 1 < input.length()) && (isHex(input.charAt(i + 1)))) {
/* 556 */                 i++;
/* 557 */                 code = digitValue(input.charAt(i));
/*     */               } else {
/* 559 */                 throw new InvalidEscapeSequence("Invalid escape sequence: '\\x' with no digits");
/*     */               }
/*     */ 
/* 562 */               if ((i + 1 < input.length()) && (isHex(input.charAt(i + 1)))) {
/* 563 */                 i++;
/* 564 */                 code = code * 16 + digitValue(input.charAt(i));
/*     */               }
/* 566 */               result[(pos++)] = ((byte)code);
/* 567 */               break;
/*     */             default:
/* 570 */               throw new InvalidEscapeSequence("Invalid escape sequence: '\\" + c + "'"); }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 575 */           throw new InvalidEscapeSequence("Invalid escape sequence: '\\' at end of string.");
/*     */         }
/*     */       }
/*     */       else {
/* 579 */         result[(pos++)] = ((byte)c);
/*     */       }
/*     */     }
/*     */ 
/* 583 */     return new Buffer(result, 0, pos);
/*     */   }
/*     */ 
/*     */   static String escapeText(String input)
/*     */   {
/* 602 */     return escapeBytes(new UTF8Buffer(input));
/*     */   }
/*     */ 
/*     */   static String unescapeText(String input)
/*     */     throws TextFormat.InvalidEscapeSequence
/*     */   {
/* 610 */     return new UTF8Buffer(unescapeBytes(input)).toString();
/*     */   }
/*     */ 
/*     */   private static boolean isOctal(char c)
/*     */   {
/* 615 */     return ('0' <= c) && (c <= '7');
/*     */   }
/*     */ 
/*     */   private static boolean isHex(char c)
/*     */   {
/* 620 */     return (('0' <= c) && (c <= '9')) || (('a' <= c) && (c <= 'f')) || (('A' <= c) && (c <= 'F'));
/*     */   }
/*     */ 
/*     */   private static int digitValue(char c)
/*     */   {
/* 631 */     if (('0' <= c) && (c <= '9'))
/* 632 */       return c - '0';
/* 633 */     if (('a' <= c) && (c <= 'z')) {
/* 634 */       return c - 'a' + 10;
/*     */     }
/* 636 */     return c - 'A' + 10;
/*     */   }
/*     */ 
/*     */   static int parseInt32(String text)
/*     */     throws NumberFormatException
/*     */   {
/* 646 */     return (int)parseInteger(text, true, false);
/*     */   }
/*     */ 
/*     */   static int parseUInt32(String text)
/*     */     throws NumberFormatException
/*     */   {
/* 657 */     return (int)parseInteger(text, false, false);
/*     */   }
/*     */ 
/*     */   static long parseInt64(String text)
/*     */     throws NumberFormatException
/*     */   {
/* 666 */     return parseInteger(text, true, true);
/*     */   }
/*     */ 
/*     */   static long parseUInt64(String text)
/*     */     throws NumberFormatException
/*     */   {
/* 677 */     return parseInteger(text, false, true);
/*     */   }
/*     */ 
/*     */   private static long parseInteger(String text, boolean isSigned, boolean isLong)
/*     */     throws NumberFormatException
/*     */   {
/* 684 */     int pos = 0;
/*     */ 
/* 686 */     boolean negative = false;
/* 687 */     if (text.startsWith("-", pos)) {
/* 688 */       if (!isSigned) {
/* 689 */         throw new NumberFormatException("Number must be positive: " + text);
/*     */       }
/* 691 */       pos++;
/* 692 */       negative = true;
/*     */     }
/*     */ 
/* 695 */     int radix = 10;
/* 696 */     if (text.startsWith("0x", pos)) {
/* 697 */       pos += 2;
/* 698 */       radix = 16;
/* 699 */     } else if (text.startsWith("0", pos)) {
/* 700 */       radix = 8;
/*     */     }
/*     */ 
/* 703 */     String numberText = text.substring(pos);
/*     */ 
/* 705 */     long result = 0L;
/* 706 */     if (numberText.length() < 16)
/*     */     {
/* 708 */       result = Long.parseLong(numberText, radix);
/* 709 */       if (negative) {
/* 710 */         result = -result;
/*     */       }
/*     */ 
/* 716 */       if (!isLong) {
/* 717 */         if (isSigned) {
/* 718 */           if ((result > 2147483647L) || (result < -2147483648L)) {
/* 719 */             throw new NumberFormatException("Number out of range for 32-bit signed integer: " + text);
/*     */           }
/*     */ 
/*     */         }
/* 723 */         else if ((result >= 4294967296L) || (result < 0L)) {
/* 724 */           throw new NumberFormatException("Number out of range for 32-bit unsigned integer: " + text);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 730 */       BigInteger bigValue = new BigInteger(numberText, radix);
/* 731 */       if (negative) {
/* 732 */         bigValue = bigValue.negate();
/*     */       }
/*     */ 
/* 736 */       if (!isLong) {
/* 737 */         if (isSigned) {
/* 738 */           if (bigValue.bitLength() > 31) {
/* 739 */             throw new NumberFormatException("Number out of range for 32-bit signed integer: " + text);
/*     */           }
/*     */ 
/*     */         }
/* 743 */         else if (bigValue.bitLength() > 32) {
/* 744 */           throw new NumberFormatException("Number out of range for 32-bit unsigned integer: " + text);
/*     */         }
/*     */ 
/*     */       }
/* 749 */       else if (isSigned) {
/* 750 */         if (bigValue.bitLength() > 63) {
/* 751 */           throw new NumberFormatException("Number out of range for 64-bit signed integer: " + text);
/*     */         }
/*     */ 
/*     */       }
/* 755 */       else if (bigValue.bitLength() > 64) {
/* 756 */         throw new NumberFormatException("Number out of range for 64-bit unsigned integer: " + text);
/*     */       }
/*     */ 
/* 762 */       result = bigValue.longValue();
/*     */     }
/*     */ 
/* 765 */     return result;
/*     */   }
/*     */ 
/*     */   static class InvalidEscapeSequence extends IOException
/*     */   {
/*     */     public InvalidEscapeSequence(String description)
/*     */     {
/* 592 */       super();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class ParseException extends IOException
/*     */   {
/*     */     public ParseException(String message)
/*     */     {
/* 443 */       super();
/*     */     }
/*     */   }
/*     */ 
/*     */   private static final class Tokenizer
/*     */   {
/*     */     private final CharSequence text;
/*     */     private final Matcher matcher;
/*     */     private String currentToken;
/*  99 */     private int pos = 0;
/*     */ 
/* 102 */     private int line = 0;
/* 103 */     private int column = 0;
/*     */ 
/* 107 */     private int previousLine = 0;
/* 108 */     private int previousColumn = 0;
/*     */ 
/* 110 */     private static Pattern WHITESPACE = Pattern.compile("(\\s|(#.*$))+", 8);
/*     */ 
/* 112 */     private static Pattern TOKEN = Pattern.compile("[a-zA-Z_][0-9a-zA-Z_+-]*|[0-9+-][0-9a-zA-Z_.+-]*|\"([^\"\n\\\\]|\\\\.)*(\"|\\\\?$)|'([^\"\n\\\\]|\\\\.)*('|\\\\?$)", 8);
/*     */ 
/* 119 */     private static Pattern DOUBLE_INFINITY = Pattern.compile("-?inf(inity)?", 2);
/*     */ 
/* 122 */     private static Pattern FLOAT_INFINITY = Pattern.compile("-?inf(inity)?f?", 2);
/*     */ 
/* 125 */     private static Pattern FLOAT_NAN = Pattern.compile("nanf?", 2);
/*     */ 
/*     */     public Tokenizer(CharSequence text)
/*     */     {
/* 131 */       this.text = text;
/* 132 */       this.matcher = WHITESPACE.matcher(text);
/* 133 */       skipWhitespace();
/* 134 */       nextToken();
/*     */     }
/*     */ 
/*     */     public boolean atEnd()
/*     */     {
/* 139 */       return this.currentToken.length() == 0;
/*     */     }
/*     */ 
/*     */     public void nextToken()
/*     */     {
/* 144 */       this.previousLine = this.line;
/* 145 */       this.previousColumn = this.column;
/*     */ 
/* 148 */       while (this.pos < this.matcher.regionStart()) {
/* 149 */         if (this.text.charAt(this.pos) == '\n') {
/* 150 */           this.line += 1;
/* 151 */           this.column = 0;
/*     */         } else {
/* 153 */           this.column += 1;
/*     */         }
/* 155 */         this.pos += 1;
/*     */       }
/*     */ 
/* 159 */       if (this.matcher.regionStart() == this.matcher.regionEnd())
/*     */       {
/* 161 */         this.currentToken = "";
/*     */       } else {
/* 163 */         this.matcher.usePattern(TOKEN);
/* 164 */         if (this.matcher.lookingAt()) {
/* 165 */           this.currentToken = this.matcher.group();
/* 166 */           this.matcher.region(this.matcher.end(), this.matcher.regionEnd());
/*     */         }
/*     */         else {
/* 169 */           this.currentToken = String.valueOf(this.text.charAt(this.pos));
/* 170 */           this.matcher.region(this.pos + 1, this.matcher.regionEnd());
/*     */         }
/*     */ 
/* 173 */         skipWhitespace();
/*     */       }
/*     */     }
/*     */ 
/*     */     private void skipWhitespace()
/*     */     {
/* 182 */       this.matcher.usePattern(WHITESPACE);
/* 183 */       if (this.matcher.lookingAt())
/* 184 */         this.matcher.region(this.matcher.end(), this.matcher.regionEnd());
/*     */     }
/*     */ 
/*     */     public boolean tryConsume(String token)
/*     */     {
/* 193 */       if (this.currentToken.equals(token)) {
/* 194 */         nextToken();
/* 195 */         return true;
/*     */       }
/* 197 */       return false;
/*     */     }
/*     */ 
/*     */     public void consume(String token)
/*     */       throws TextFormat.ParseException
/*     */     {
/* 206 */       if (!tryConsume(token))
/* 207 */         throw parseException("Expected \"" + token + "\".");
/*     */     }
/*     */ 
/*     */     public boolean lookingAtInteger()
/*     */     {
/* 216 */       if (this.currentToken.length() == 0) {
/* 217 */         return false;
/*     */       }
/*     */ 
/* 220 */       char c = this.currentToken.charAt(0);
/* 221 */       return (('0' <= c) && (c <= '9')) || (c == '-') || (c == '+');
/*     */     }
/*     */ 
/*     */     public String consumeIdentifier()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 230 */       for (int i = 0; i < this.currentToken.length(); i++) {
/* 231 */         char c = this.currentToken.charAt(i);
/* 232 */         if ((('a' > c) || (c > 'z')) && (('A' > c) || (c > 'Z')) && (('0' > c) || (c > '9')) && (c != '_') && (c != '.'))
/*     */         {
/* 238 */           throw parseException("Expected identifier.");
/*     */         }
/*     */       }
/*     */ 
/* 242 */       String result = this.currentToken;
/* 243 */       nextToken();
/* 244 */       return result;
/*     */     }
/*     */ 
/*     */     public int consumeInt32()
/*     */       throws TextFormat.ParseException
/*     */     {
/*     */       try
/*     */       {
/* 253 */         int result = TextFormat.parseInt32(this.currentToken);
/* 254 */         nextToken();
/* 255 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 257 */         throw integerParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public int consumeUInt32()
/*     */       throws TextFormat.ParseException
/*     */     {
/*     */       try
/*     */       {
/* 267 */         int result = TextFormat.parseUInt32(this.currentToken);
/* 268 */         nextToken();
/* 269 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 271 */         throw integerParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public long consumeInt64()
/*     */       throws TextFormat.ParseException
/*     */     {
/*     */       try
/*     */       {
/* 281 */         long result = TextFormat.parseInt64(this.currentToken);
/* 282 */         nextToken();
/* 283 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 285 */         throw integerParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public long consumeUInt64()
/*     */       throws TextFormat.ParseException
/*     */     {
/*     */       try
/*     */       {
/* 295 */         long result = TextFormat.parseUInt64(this.currentToken);
/* 296 */         nextToken();
/* 297 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 299 */         throw integerParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public double consumeDouble()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 310 */       if (DOUBLE_INFINITY.matcher(this.currentToken).matches()) {
/* 311 */         boolean negative = this.currentToken.startsWith("-");
/* 312 */         nextToken();
/* 313 */         return negative ? (-1.0D / 0.0D) : (1.0D / 0.0D);
/*     */       }
/* 315 */       if (this.currentToken.equalsIgnoreCase("nan")) {
/* 316 */         nextToken();
/* 317 */         return (0.0D / 0.0D);
/*     */       }
/*     */       try {
/* 320 */         double result = Double.parseDouble(this.currentToken);
/* 321 */         nextToken();
/* 322 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 324 */         throw floatParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public float consumeFloat()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 335 */       if (FLOAT_INFINITY.matcher(this.currentToken).matches()) {
/* 336 */         boolean negative = this.currentToken.startsWith("-");
/* 337 */         nextToken();
/* 338 */         return negative ? (1.0F / -1.0F) : (1.0F / 1.0F);
/*     */       }
/* 340 */       if (FLOAT_NAN.matcher(this.currentToken).matches()) {
/* 341 */         nextToken();
/* 342 */         return (0.0F / 0.0F);
/*     */       }
/*     */       try {
/* 345 */         float result = Float.parseFloat(this.currentToken);
/* 346 */         nextToken();
/* 347 */         return result;
/*     */       } catch (NumberFormatException e) {
/* 349 */         throw floatParseException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public boolean consumeBoolean()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 358 */       if (this.currentToken.equals("true")) {
/* 359 */         nextToken();
/* 360 */         return true;
/* 361 */       }if (this.currentToken.equals("false")) {
/* 362 */         nextToken();
/* 363 */         return false;
/*     */       }
/* 365 */       throw parseException("Expected \"true\" or \"false\".");
/*     */     }
/*     */ 
/*     */     public String consumeString()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 374 */       return new UTF8Buffer(consumeBuffer()).toString();
/*     */     }
/*     */ 
/*     */     public Buffer consumeBuffer()
/*     */       throws TextFormat.ParseException
/*     */     {
/* 383 */       char quote = this.currentToken.length() > 0 ? this.currentToken.charAt(0) : '\000';
/* 384 */       if ((quote != '"') && (quote != '\'')) {
/* 385 */         throw parseException("Expected string.");
/*     */       }
/*     */ 
/* 388 */       if ((this.currentToken.length() < 2) || (this.currentToken.charAt(this.currentToken.length() - 1) != quote))
/*     */       {
/* 390 */         throw parseException("String missing ending quote.");
/*     */       }
/*     */       try
/*     */       {
/* 394 */         String escaped = this.currentToken.substring(1, this.currentToken.length() - 1);
/* 395 */         Buffer result = TextFormat.unescapeBytes(escaped);
/* 396 */         nextToken();
/* 397 */         return result;
/*     */       } catch (TextFormat.InvalidEscapeSequence e) {
/* 399 */         throw parseException(e.getMessage());
/*     */       }
/*     */     }
/*     */ 
/*     */     public TextFormat.ParseException parseException(String description)
/*     */     {
/* 409 */       return new TextFormat.ParseException(this.line + 1 + ":" + (this.column + 1) + ": " + description);
/*     */     }
/*     */ 
/*     */     public TextFormat.ParseException parseExceptionPreviousToken(String description)
/*     */     {
/* 419 */       return new TextFormat.ParseException(this.previousLine + 1 + ":" + (this.previousColumn + 1) + ": " + description);
/*     */     }
/*     */ 
/*     */     private TextFormat.ParseException integerParseException(NumberFormatException e)
/*     */     {
/* 428 */       return parseException("Couldn't parse integer: " + e.getMessage());
/*     */     }
/*     */ 
/*     */     private TextFormat.ParseException floatParseException(NumberFormatException e)
/*     */     {
/* 436 */       return parseException("Couldn't parse number: " + e.getMessage());
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Users\孔新\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\
 * Qualified Name:     org.apache.activemq.protobuf.compiler.TextFormat
 * JD-Core Version:    0.6.2
 */