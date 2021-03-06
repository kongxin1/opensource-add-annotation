/*     */package org.apache.activemq.protobuf;

/*     */
/*     */import java.io.IOException;
/*     */
import java.io.InputStream;

/**
 * 一个字节输入流
 * @ClassName: BufferInputStream
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年8月15日 下午9:34:41
 */
/*     */public final class BufferInputStream extends InputStream
/*     */{
	/*     */byte[] buffer;
	/*     */int limit;
	/*     */int pos;
	/*     */int mark;

	/*     */
	/*     */public BufferInputStream(byte[] data)
	/*     */{
		/* 34 */this(data, 0, data.length);
		/*     */}
	/*     */
	/*     */public BufferInputStream(Buffer sequence) {
		/* 38 */this(sequence.getData(), sequence.getOffset(), sequence.getLength());
		/*     */}
	/*     */
	/*     */public BufferInputStream(byte[] data, int offset, int size) {
		/* 42 */this.buffer = data;
		/* 43 */this.mark = offset;
		/* 44 */this.pos = offset;
		/* 45 */this.limit = (offset + size);
		/*     */}
	/*     */
	/*     */public int read() throws IOException {
		/* 49 */if (this.pos < this.limit) {
			/* 50 */return this.buffer[(this.pos++)] & 0xFF;
			/*     */}
		/* 52 */return -1;
		/*     */}
	/*     */
	/*     */public int read(byte[] b) throws IOException
	/*     */{
		/* 57 */return read(b, 0, b.length);
		/*     */}
	/*     */
	/*     */public int read(byte[] b, int off, int len) {
		/* 61 */if (this.pos < this.limit) {
			/* 62 */len = Math.min(len, this.limit - this.pos);
			/* 63 */System.arraycopy(this.buffer, this.pos, b, off, len);
			/* 64 */this.pos += len;
			/* 65 */return len;
			/*     */}
		/* 67 */return -1;
		/*     */}
	/*     */
	/*     */public Buffer readBuffer(int len)
	/*     */{
		/* 72 */Buffer rc = null;
		/* 73 */if (this.pos < this.limit) {
			/* 74 */len = Math.min(len, this.limit - this.pos);
			/* 75 */rc = new Buffer(this.buffer, this.pos, len);
			/* 76 */this.pos += len;
			/*     */}
		/* 78 */return rc;
		/*     */}
	/*     */
	/*     */public long skip(long len) throws IOException {
		/* 82 */if (this.pos < this.limit) {
			/* 83 */len = Math.min(len, this.limit - this.pos);
			/* 84 */if (len > 0L) {
				/* 85 */this.pos = ((int) (this.pos + len));
				/*     */}
			/* 87 */return len;
			/*     */}
		/* 89 */return -1L;
		/*     */}
	/*     */
	/*     */public int available()
	/*     */{
		/* 94 */return this.limit - this.pos;
		/*     */}
	/*     */
	/*     */public boolean markSupported() {
		/* 98 */return true;
		/*     */}
	/*     */
	/*     */public void mark(int markpos) {
		/* 102 */this.mark = this.pos;
		/*     */}
	/*     */
	/*     */public void reset() {
		/* 106 */this.pos = this.mark;
		/*     */}
	/*     */
}
/*
 * Location: C:\Users\瀛旀柊\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\ Qualified Name:
 * org.apache.activemq.protobuf.BufferInputStream JD-Core Version: 0.6.2
 */
