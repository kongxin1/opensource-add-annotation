/*     */package org.apache.activemq.protobuf;

/*     */
/*     */import java.util.List;

/**
 * 缓存数据的对象，可以将数据以二进制形式存储起来
 * @ClassName: Buffer
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年8月15日 下午9:33:49
 */
/*     */public class Buffer
/*     */implements Comparable<Buffer>
/*     */{
	/*     */public final byte[] data;
	/*     */public final int offset;
	/*     */public final int length;

	/*     */
	/*     */public Buffer(Buffer other)
	/*     */{
		/* 29 */this(other.data, other.offset, other.length);
		/*     */}
	/*     */
	/*     */public Buffer(byte[] data) {
		/* 33 */this(data, 0, data.length);
		/*     */}
	/*     */
	/*     */public Buffer(byte[] data, int offset, int length) {
		/* 37 */this.data = data;
		/* 38 */this.offset = offset;
		/* 39 */this.length = length;
		/*     */}
	/*     */
	/*     */@Deprecated
	/*     */public Buffer(String value) {
		/* 44 */this(UTF8Buffer.encode(value));
		/*     */}
	/*     */
	/*     */public final Buffer slice(int low, int high)
	/*     */{
		/*     */int sz;
		/*     */
		/* 50 */if (high < 0)
			/* 51 */sz = this.length + high;
		/*     */else {
			/* 53 */sz = high - low;
			/*     */}
		/*     */
		/* 56 */if (sz < 0) {
			/* 57 */sz = 0;
			/*     */}
		/*     */
		/* 60 */return new Buffer(this.data, this.offset + low, sz);
		/*     */}
	/*     */
	/*     */public final byte[] getData() {
		/* 64 */return this.data;
		/*     */}
	/*     */
	/*     */public final int getLength() {
		/* 68 */return this.length;
		/*     */}
	/*     */
	/*     */public final int getOffset() {
		/* 72 */return this.offset;
		/*     */}
	/*     */
	/*     */public Buffer compact() {
		/* 76 */if (this.length != this.data.length) {
			/* 77 */return new Buffer(toByteArray());
			/*     */}
		/* 79 */return this;
		/*     */}
	/*     */
	/*     */public final byte[] toByteArray() {
		/* 83 */byte[] data = this.data;
		/* 84 */int length = this.length;
		/* 85 */if (length != data.length) {
			/* 86 */byte[] t = new byte[length];
			/* 87 */System.arraycopy(data, this.offset, t, 0, length);
			/* 88 */data = t;
			/*     */}
		/* 90 */return data;
		/*     */}
	/*     */
	/*     */public byte byteAt(int i) {
		/* 94 */return this.data[(this.offset + i)];
		/*     */}
	/*     */
	/*     */public int hashCode()
	/*     */{
		/* 100 */byte[] target = new byte[4];
		/* 101 */for (int i = 0; i < this.length; i++)
		/*     */{
			/*     */int tmp18_17 = (i % 4);
			/*     */byte[] tmp18_14 = target;
			tmp18_14[tmp18_17] = ((byte) (tmp18_14[tmp18_17] ^ this.data[(this.offset + i)]));
			/*     */}
		/* 104 */return target[0] << 24 | target[1] << 16 | target[2] << 8 | target[3];
		/*     */}
	/*     */
	/*     */public boolean equals(Object obj)
	/*     */{
		/* 109 */if (obj == this) {
			/* 110 */return true;
			/*     */}
		/* 112 */if ((obj == null) || (obj.getClass() != Buffer.class)) {
			/* 113 */return false;
			/*     */}
		/* 115 */return equals((Buffer) obj);
		/*     */}
	/*     */
	/*     */public final boolean equals(Buffer obj) {
		/* 119 */if (this.length != obj.length) {
			/* 120 */return false;
			/*     */}
		/* 122 */for (int i = 0; i < this.length; i++) {
			/* 123 */if (obj.data[(obj.offset + i)] != this.data[(this.offset + i)]) {
				/* 124 */return false;
				/*     */}
			/*     */}
		/* 127 */return true;
		/*     */}
	/*     */
	/*     */public final BufferInputStream newInput() {
		/* 131 */return new BufferInputStream(this);
		/*     */}
	/*     */
	/*     */public final BufferOutputStream newOutput() {
		/* 135 */return new BufferOutputStream(this);
		/*     */}
	/*     */
	/*     */public final boolean isEmpty() {
		/* 139 */return this.length == 0;
		/*     */}
	/*     */
	/*     */public final boolean contains(byte value) {
		/* 143 */return indexOf(value, 0) >= 0;
		/*     */}
	/*     */
	/*     */public final int indexOf(byte value, int pos) {
		/* 147 */for (int i = pos; i < this.length; i++) {
			/* 148 */if (this.data[(this.offset + i)] == value) {
				/* 149 */return i;
				/*     */}
			/*     */}
		/* 152 */return -1;
		/*     */}
	/*     */
	/*     */public static final Buffer join(List<Buffer> items, Buffer seperator) {
		/* 156 */if (items.isEmpty()) {
			/* 157 */return new Buffer(seperator.data, 0, 0);
			/*     */}
		/* 159 */int size = 0;
		/* 160 */for (Buffer item : items) {
			/* 161 */size += item.length;
			/*     */}
		/* 163 */size += seperator.length * (items.size() - 1);
		/*     */
		/* 165 */int pos = 0;
		/* 166 */byte[] data = new byte[size];
		/* 167 */for (Buffer item : items) {
			/* 168 */if (pos != 0) {
				/* 169 */System.arraycopy(seperator.data, seperator.offset, data, pos, seperator.length);
				/* 170 */pos += seperator.length;
				/*     */}
			/* 172 */System.arraycopy(item.data, item.offset, data, pos, item.length);
			/* 173 */pos += item.length;
			/*     */}
		/*     */
		/* 176 */return new Buffer(data, 0, size);
		/*     */}
	/*     */
	/*     */@Deprecated
	/*     */public String toStringUtf8() {
		/* 181 */return UTF8Buffer.decode(this);
		/*     */}
	/*     */
	/*     */public int compareTo(Buffer o) {
		/* 185 */int minLength = Math.min(this.length, o.length);
		/* 186 */if (this.offset == o.offset) {
			/* 187 */int pos = this.offset;
			/* 188 */int limit = minLength + this.offset;
			/* 189 */while (pos < limit) {
				/* 190 */byte b1 = this.data[pos];
				/* 191 */byte b2 = o.data[pos];
				/* 192 */if (b1 != b2) {
					/* 193 */return b1 - b2;
					/*     */}
				/* 195 */pos++;
				/*     */}
			/*     */} else {
			/* 198 */int offset1 = this.offset;
			/* 199 */int offset2 = o.offset;
			/* 200 */while (minLength-- != 0) {
				/* 201 */byte b1 = this.data[(offset1++)];
				/* 202 */byte b2 = o.data[(offset2++)];
				/* 203 */if (b1 != b2) {
					/* 204 */return b1 - b2;
					/*     */}
				/*     */}
			/*     */}
		/* 208 */return this.length - o.length;
		/*     */}
	/*     */
}
/*
 * Location: C:\Users\瀛旀柊\Desktop\apache-activemq-5.13.0\activemq-all-5.13.0\ Qualified Name:
 * org.apache.activemq.protobuf.Buffer JD-Core Version: 0.6.2
 */
