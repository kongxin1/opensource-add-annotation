/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.transport.tcp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An optimized buffered input stream for Tcp
 */
/**
 * 缓存数据的流对象
 * @ClassName: TcpBufferedInputStream
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月17日 下午6:17:17
 */
public class TcpBufferedInputStream extends FilterInputStream {
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	protected byte internalBuffer[];// 用于缓存数据
	protected int count;// 缓存的总数据量
	protected int position;// 指针位置

	public TcpBufferedInputStream(InputStream in) {
		this(in, DEFAULT_BUFFER_SIZE);
	}
	public TcpBufferedInputStream(InputStream in, int size) {
		super(in);
		if (size <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		internalBuffer = new byte[size];
	}
	/**
	 * 将数据读入到本对象的缓存数组中
	 * @Title: fill
	 * @Description: TODO
	 * @throws IOException
	 * @return: void
	 */
	protected void fill() throws IOException {
		byte[] buffer = internalBuffer;
		count = 0;
		position = 0;
		int n = in.read(buffer, position, buffer.length - position);
		if (n > 0) {
			count = n + position;
		}
	}
	@Override
	/**
	 * 读取数据，一次返回一个byte
	 */
	public int read() throws IOException {
		if (position >= count) {
			fill();
			if (position >= count) {
				return -1;
			}
		}
		return internalBuffer[position++] & 0xff;
	}
	/**
	 * 将缓存的数据存储到b中，并返回存储到b中的字节数
	 * @Title: readStream
	 * @Description: TODO
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @return: int
	 */
	private int readStream(byte[] b, int off, int len) throws IOException {
		int avail = count - position;
		if (avail <= 0) {
			if (len >= internalBuffer.length) {
				// 如果缓存空间小，就直接从底层读取
				return in.read(b, off, len);
			}
			fill();
			avail = count - position;
			if (avail <= 0) {
				// 没有数据了
				return -1;
			}
		}
		int cnt = (avail < len) ? avail : len;
		System.arraycopy(internalBuffer, position, b, off, cnt);
		position += cnt;
		return cnt;
	}
	@Override
	/**
	 * 将读取的数据放到b中，并进全力读取len要求的字节数
	 */
	public int read(byte b[], int off, int len) throws IOException {
		if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		int n = 0;
		for (;;) {
			int nread = readStream(b, off + n, len - n);
			if (nread <= 0) {
				return (n == 0) ? nread : n;
			}
			n += nread;
			if (n >= len) {
				return n;
			}
			// if not closed but no bytes available, return
			InputStream input = in;
			if (input != null && input.available() <= 0) {
				return n;
			}
		}
	}
	@Override
	/**
	 * 跳过n字节数据
	 */
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		long avail = count - position;
		if (avail <= 0) {
			return in.skip(n);
		}
		long skipped = (avail < n) ? avail : n;
		position += skipped;
		return skipped;
	}
	@Override
	/**
	 * 返回有效的数据
	 */
	public int available() throws IOException {
		return in.available() + (count - position);
	}
	@Override
	public boolean markSupported() {
		return false;
	}
	@Override
	public void close() throws IOException {
		if (in != null) {
			in.close();
		}
	}
	/**
	 * @param array
	 * @throws IOException
	 */
	/**
	 * 将array中的数据读入到本对象的缓存数组中
	 * @Title: unread
	 * @Description: TODO
	 * @param array
	 * @throws IOException
	 * @return: void
	 */
	public void unread(byte[] array) throws IOException {
		int avail = internalBuffer.length - position;
		if (array.length > avail) {
			throw new IOException("Buffer is full, can't unread");
		}
		// 下面这个方法会抛出异常，因为array.length+position>array.length，除非position=0
		System.arraycopy(array, position, internalBuffer, 0, array.length);
		count += array.length;
	}
}
