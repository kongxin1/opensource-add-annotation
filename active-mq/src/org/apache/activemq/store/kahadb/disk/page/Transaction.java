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
package org.apache.activemq.store.kahadb.disk.page;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.apache.activemq.store.kahadb.disk.page.PageFile.PageWrite;
import org.apache.activemq.store.kahadb.disk.util.DataByteArrayInputStream;
import org.apache.activemq.store.kahadb.disk.util.DataByteArrayOutputStream;
import org.apache.activemq.store.kahadb.disk.util.Marshaller;
import org.apache.activemq.store.kahadb.disk.util.Sequence;
import org.apache.activemq.store.kahadb.disk.util.SequenceSet;
import org.apache.activemq.util.ByteSequence;
import org.apache.activemq.util.IOHelper;

/**
 * The class used to read/update a PageFile object.  Using a transaction allows you to
 * do multiple update operations in a single unit of work.
 */
/**
 * 事务类，可以将多个更新操作放在一个事务中
 * @ClassName: Transaction
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月8日 下午10:03:58
 */
public class Transaction implements Iterable<Page> {
	private RandomAccessFile tmpFile;
	private File txFile;
	// 记录下次写入临时文件的位置
	private long nextLocation = 0;

	/**
	 * The PageOverflowIOException occurs when a page write is requested and it's data is larger
	 * than what would fit into a single page.
	 */
	public class PageOverflowIOException extends IOException {
		private static final long serialVersionUID = 1L;

		public PageOverflowIOException(String message) {
			super(message);
		}
	}

	/**
	 * The InvalidPageIOException is thrown if try to load/store a a page with an invalid page id.
	 */
	public class InvalidPageIOException extends IOException {
		private static final long serialVersionUID = 1L;
		private final long page;

		public InvalidPageIOException(String message, long page) {
			super(message);
			this.page = page;
		}
		public long getPage() {
			return page;
		}
	}

	/**
	 * This closure interface is intended for the end user implement callbacks for the
	 * Transaction.exectue() method.
	 * @param <T> The type of exceptions that operation will throw.
	 */
	public interface Closure<T extends Throwable> {
		public void execute(Transaction tx) throws T;
	}

	/**
	 * This closure interface is intended for the end user implement callbacks for the
	 * Transaction.exectue() method.
	 * @param <R> The type of result that the closure produces.
	 * @param <T> The type of exceptions that operation will throw.
	 */
	public interface CallableClosure<R, T extends Throwable> {
		public R execute(Transaction tx) throws T;
	}

	// The page file that this Transaction operates against.
	// 可以缓存page对象
	private final PageFile pageFile;
	// If this transaction is updating stuff.. this is the tx of
	private long writeTransactionId = -1;
	// List of pages that this transaction has modified.
	private TreeMap<Long, PageWrite> writes = new TreeMap<Long, PageWrite>();
	// List of pages allocated in this transaction
	// 存储sequence对象，每一个sequence对象代表了一组页面
	private final SequenceSet allocateList = new SequenceSet();
	// List of pages freed in this transaction
	private final SequenceSet freeList = new SequenceSet();
	private long maxTransactionSize = Long.getLong("maxKahaDBTxSize", 10485760L);
	private long size = 0;

	Transaction(PageFile pageFile) {
		this.pageFile = pageFile;
	}
	/**
	 * @return the page file that created this Transaction
	 */
	public PageFile getPageFile() {
		return this.pageFile;
	}
	/**
	 * Allocates a free page that you can write data to.
	 * @return a newly allocated page.
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	/**
	 * 分配一个空页面
	 * @Title: allocate
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: Page<T>
	 */
	public <T> Page<T> allocate() throws IOException {
		return allocate(1);
	}
	/**
	 * Allocates a block of free pages that you can write data to.
	 * @param count the number of sequential pages to allocate
	 * @return the first page of the sequential set.
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	/**
	 * 分配count个free页面
	 * @Title: allocate
	 * @Description: TODO
	 * @param count
	 * @return
	 * @throws IOException
	 * @return: Page<T>
	 */
	public <T> Page<T> allocate(int count) throws IOException {
		Page<T> rc = pageFile.allocate(count);
		// 将本次获得的count个页面编制成一个sequence
		allocateList.add(new Sequence(rc.getPageId(), rc.getPageId() + count - 1));
		return rc;
	}
	/**
	 * Frees up a previously allocated page so that it can be re-allocated again.
	 * @param pageId the page to free up
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public void free(long pageId) throws IOException {
		free(load(pageId, null));
	}
	/**
	 * Frees up a previously allocated sequence of pages so that it can be re-allocated again.
	 * @param pageId the initial page of the sequence that will be getting freed
	 * @param count the number of pages in the sequence
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public void free(long pageId, int count) throws IOException {
		free(load(pageId, null), count);
	}
	/**
	 * Frees up a previously allocated sequence of pages so that it can be re-allocated again.
	 * @param page the initial page of the sequence that will be getting freed
	 * @param count the number of pages in the sequence
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public <T> void free(Page<T> page, int count) throws IOException {
		pageFile.assertLoaded();
		long offsetPage = page.getPageId();
		while (count-- > 0) {
			if (page == null) {
				page = load(offsetPage, null);
			}
			free(page);
			page = null;
			// Increment the offsetPage value since using it depends on the current count.
			offsetPage++;
		}
	}
	/**
	 * Frees up a previously allocated page so that it can be re-allocated again.
	 * @param page the page to free up
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public <T> void free(Page<T> page) throws IOException {
		pageFile.assertLoaded();
		// We may need loop to free up a page chain.
		while (page != null) {
			// Is it already free??
			if (page.getType() == Page.PAGE_FREE_TYPE) {
				return;
			}
			Page<T> next = null;
			if (page.getType() == Page.PAGE_PART_TYPE) {
				next = load(page.getNext(), null);
			}
			page.makeFree(getWriteTransactionId());
			// ensure free page is visible while write is pending
			pageFile.addToCache(page.copy());
			DataByteArrayOutputStream out = new DataByteArrayOutputStream(pageFile.getPageSize());
			page.write(out);
			write(page, out.getData());
			freeList.add(page.getPageId());
			page = next;
		}
	}
	/**
	 * @param page the page to write. The Page object must be fully populated with a valid pageId,
	 *            type, and data.
	 * @param marshaller the marshaler to use to load the data portion of the Page, may be null if
	 *            you do not wish to write the data.
	 * @param overflow If true, then if the page data marshalls to a bigger size than can fit in one
	 *            page, then additional overflow pages are automatically allocated and chained to
	 *            this page to store all the data. If false, and the overflow condition would occur,
	 *            then the PageOverflowIOException is thrown.
	 * @throws IOException If an disk error occurred.
	 * @throws PageOverflowIOException If the page data marshalls to size larger than maximum page
	 *             size and overflow was false.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	/**
	 * 将page中的数据写入out中，写入到临时文件和一些缓存对象中
	 * @Title: store
	 * @Description: TODO
	 * @param page
	 * @param marshaller
	 * @param overflow
	 * @throws IOException
	 * @return: void
	 */
	public <T> void store(Page<T> page, Marshaller<T> marshaller, final boolean overflow) throws IOException {
		DataByteArrayOutputStream out = (DataByteArrayOutputStream) openOutputStream(page, overflow);
		if (marshaller != null) {
			// 将page对象中的数据写入out中
			marshaller.writePayload(page.get(), out);
		}
		out.close();
	}
	/**
	 * @throws IOException
	 */
	/**
	 * 返回一个字节数组的字节流
	 * @Title: openOutputStream
	 * @Description: TODO
	 * @param page
	 * @param overflow
	 * @return
	 * @throws IOException
	 * @return: OutputStream
	 */
	public OutputStream openOutputStream(Page page, final boolean overflow) throws IOException {
		pageFile.assertLoaded();
		// Copy to protect against the end user changing
		// the page instance while we are doing a write.
		final Page copy = page.copy();
		// 将本页面加入缓存
		pageFile.addToCache(copy);
		//
		// To support writing VERY large data, we override the output stream so
		// that we
		// we do the page writes incrementally while the data is being
		// marshalled.
		// 一个字节流对象，存储在一个字节数组中
		DataByteArrayOutputStream out = new DataByteArrayOutputStream(pageFile.getPageSize() * 2) {
			Page current = copy;

			@SuppressWarnings("unchecked")
			@Override
			/**
			 * 将数据写入临时文件和一些缓存对象中
			 */
			protected void onWrite() throws IOException {
				// Are we at an overflow condition?
				final int pageSize = pageFile.getPageSize();
				if (pos >= pageSize) {
					// If overflow is allowed
					if (overflow) {
						// 做页面分裂操作
						do {
							Page next;
							if (current.getType() == Page.PAGE_PART_TYPE) {
								// 加载下一个页面
								next = load(current.getNext(), null);
							} else {
								next = allocate();
							}
							next.txId = current.txId;
							// Write the page header
							int oldPos = pos;
							pos = 0;
							// 设置当前页面的下一个页面为next
							current.makePagePart(next.getPageId(), getWriteTransactionId());
							current.write(this);
							// Do the page write..
							byte[] data = new byte[pageSize];
							System.arraycopy(buf, 0, data, 0, pageSize);
							Transaction.this.write(current, data);
							// make the new link visible
							pageFile.addToCache(current);
							// Reset for the next page chunk
							pos = 0;
							// The page header marshalled after the data is written.
							// 每两页中间需要空出下面这么多空间
							skip(Page.PAGE_HEADER_SIZE);
							// Move the overflow data after the header.
							// 将页面的剩余内容复制到本页面中
							System.arraycopy(buf, pageSize, buf, pos, oldPos - pageSize);
							pos += oldPos - pageSize;
							current = next;
						} while (pos > pageSize);
					} else {
						throw new PageOverflowIOException("Page overflow.");
					}
				}
			}
			@Override
			public void close() throws IOException {
				super.close();
				// We need to free up the rest of the page chain..
				if (current.getType() == Page.PAGE_PART_TYPE) {
					free(current.getNext());
				}
				// 设置当前页面大小和事务id
				current.makePageEnd(pos, getWriteTransactionId());
				// make visible as end page
				pageFile.addToCache(current);
				// Write the header..
				pos = 0;
				current.write(this);
				// 写入临时文件和writers属性中
				Transaction.this.write(current, buf);
			}
		};
		// The page header marshaled after the data is written.
		// 输出流跳过文件头
		out.skip(Page.PAGE_HEADER_SIZE);
		return out;
	}
	/**
	 * Loads a page from disk.
	 * @param pageId the id of the page to load
	 * @param marshaller the marshaler to use to load the data portion of the Page, may be null if
	 *            you do not wish to load the data.
	 * @return The page with the given id
	 * @throws IOException If an disk error occurred.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	/**
	 * 载入Page数据，可以从磁盘上读取
	 * @Title: load
	 * @Description: TODO
	 * @param pageId
	 * @param marshaller
	 * @return
	 * @throws IOException
	 * @return: Page<T>
	 */
	public <T> Page<T> load(long pageId, Marshaller<T> marshaller) throws IOException {
		pageFile.assertLoaded();
		Page<T> page = new Page<T>(pageId);
		load(page, marshaller);
		return page;
	}
	/**
	 * Loads a page from disk.
	 * @param page - The pageId field must be properly set
	 * @param marshaller the marshaler to use to load the data portion of the Page, may be null if
	 *            you do not wish to load the data.
	 * @throws IOException If an disk error occurred.
	 * @throws InvalidPageIOException If the page is is not valid.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	@SuppressWarnings("unchecked")
	/**
	 * 加载Page数据
	 * @Title: load 
	 * @Description: TODO
	 * @param page
	 * @param marshaller
	 * @throws IOException
	 * @return: void
	 */
	public <T> void load(Page<T> page, Marshaller<T> marshaller) throws IOException {
		pageFile.assertLoaded();
		// Can't load invalid offsets...
		long pageId = page.getPageId();
		if (pageId < 0) {
			throw new InvalidPageIOException("Page id is not valid", pageId);
		}
		// It might be a page this transaction has modified...
		PageWrite update = writes.get(pageId);
		if (update != null) {
			page.copy(update.getPage());
			return;
		}
		// We may be able to get it from the cache...
		// pageFile中有一个缓存属性，可以缓存Page对象
		Page<T> t = pageFile.getFromCache(pageId);
		if (t != null) {
			page.copy(t);
			return;
		}
		// 如果上述两种都找不到，就从文件中读取
		if (marshaller != null) {
			// Full page read..
			InputStream is = openInputStream(page);
			DataInputStream dataIn = new DataInputStream(is);
			// 从流中读取数据放入page中
			page.set(marshaller.readPayload(dataIn));
			is.close();
		} else {
			// Page header read.
			DataByteArrayInputStream in = new DataByteArrayInputStream(new byte[Page.PAGE_HEADER_SIZE]);
			// 读到的数据在 in.getRawData()数组中存储
			pageFile.readPage(pageId, in.getRawData());
			page.read(in);
			page.set(null);
		}
		// Cache it.
		if (marshaller != null) {
			pageFile.addToCache(page);
		}
	}
	/**
	 * @see org.apache.activemq.store.kahadb.disk.page.Transaction#load(org.apache.activemq.store.kahadb.disk.page.Page,
	 *      org.apache.activemq.store.kahadb.disk.util.Marshaller)
	 */
	/**
	 * 返回一个从文件读取一页数据的Page对象，然后放入到一个byte数组中，返回的流对象从这个数组中读取数据
	 * @Title: openInputStream
	 * @Description: TODO
	 * @param p
	 * @return
	 * @throws IOException
	 * @return: InputStream
	 */
	public InputStream openInputStream(final Page p) throws IOException {
		// 可以从文件中读取数据源
		return new InputStream() {
			private ByteSequence chunk = new ByteSequence(new byte[pageFile.getPageSize()]);
			private Page page = readPage(p);
			private int pageCount = 1;
			private Page markPage;
			private ByteSequence markChunk;

			private Page readPage(Page page) throws IOException {
				// Read the page data
				pageFile.readPage(page.getPageId(), chunk.getData());
				chunk.setOffset(0);
				chunk.setLength(pageFile.getPageSize());
				DataByteArrayInputStream in = new DataByteArrayInputStream(chunk);
				// 下面的数据都是从data中读到的
				page.read(in);
				chunk.setOffset(Page.PAGE_HEADER_SIZE);
				if (page.getType() == Page.PAGE_END_TYPE) {
					chunk.setLength((int) (page.getNext()));
				}
				if (page.getType() == Page.PAGE_FREE_TYPE) {
					throw new EOFException("Chunk stream does not exist, page: " + page.getPageId() + " is marked free");
				}
				return page;
			}
			public int read() throws IOException {
				if (!atEOF()) {
					return chunk.data[chunk.offset++] & 0xff;
				} else {
					return -1;
				}
			}
			private boolean atEOF() throws IOException {
				if (chunk.offset < chunk.length) {
					return false;
				}
				if (page.getType() == Page.PAGE_END_TYPE) {
					return true;
				}
				fill();
				return chunk.offset >= chunk.length;
			}
			private void fill() throws IOException {
				page = readPage(new Page(page.getNext()));
				pageCount++;
			}
			public int read(byte[] b) throws IOException {
				return read(b, 0, b.length);
			}
			public int read(byte b[], int off, int len) throws IOException {
				if (!atEOF()) {
					int rc = 0;
					while (!atEOF() && rc < len) {
						len = Math.min(len, chunk.length - chunk.offset);
						if (len > 0) {
							System.arraycopy(chunk.data, chunk.offset, b, off, len);
							chunk.offset += len;
						}
						rc += len;
					}
					return rc;
				} else {
					return -1;
				}
			}
			public long skip(long len) throws IOException {
				if (atEOF()) {
					int rc = 0;
					while (!atEOF() && rc < len) {
						len = Math.min(len, chunk.length - chunk.offset);
						if (len > 0) {
							chunk.offset += len;
						}
						rc += len;
					}
					return rc;
				} else {
					return -1;
				}
			}
			public int available() {
				return chunk.length - chunk.offset;
			}
			public boolean markSupported() {
				return true;
			}
			public void mark(int markpos) {
				markPage = page;
				byte data[] = new byte[pageFile.getPageSize()];
				System.arraycopy(chunk.getData(), 0, data, 0, pageFile.getPageSize());
				markChunk = new ByteSequence(data, chunk.getOffset(), chunk.getLength());
			}
			public void reset() {
				page = markPage;
				chunk = markChunk;
			}
		};
	}
	/**
	 * Allows you to iterate through all active Pages in this object. Pages with type Page.FREE_TYPE
	 * are not included in this iteration. Pages removed with Iterator.remove() will not actually
	 * get removed until the transaction commits.
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public Iterator<Page> iterator() {
		return (Iterator<Page>) iterator(false);
	}
	/**
	 * Allows you to iterate through all active Pages in this object. You can optionally include
	 * free pages in the pages iterated.
	 * @param includeFreePages - if true, free pages are included in the iteration
	 * @throws IllegalStateException if the PageFile is not loaded
	 */
	public Iterator<Page> iterator(final boolean includeFreePages) {
		pageFile.assertLoaded();
		return new Iterator<Page>() {
			long nextId;
			Page nextPage;
			Page lastPage;

			private void findNextPage() {
				if (!pageFile.isLoaded()) {
					throw new IllegalStateException("Cannot iterate the pages when the page file is not loaded");
				}
				if (nextPage != null) {
					return;
				}
				try {
					while (nextId < pageFile.getPageCount()) {
						Page page = load(nextId, null);
						if (includeFreePages || page.getType() != Page.PAGE_FREE_TYPE) {
							nextPage = page;
							return;
						} else {
							nextId++;
						}
					}
				} catch (IOException e) {
				}
			}
			public boolean hasNext() {
				findNextPage();
				return nextPage != null;
			}
			public Page next() {
				findNextPage();
				if (nextPage != null) {
					lastPage = nextPage;
					nextPage = null;
					nextId++;
					return lastPage;
				} else {
					throw new NoSuchElementException();
				}
			}
			@SuppressWarnings("unchecked")
			public void remove() {
				if (lastPage == null) {
					throw new IllegalStateException();
				}
				try {
					free(lastPage);
					lastPage = null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	// /////////////////////////////////////////////////////////////////
	// Commit / Rollback related methods..
	// /////////////////////////////////////////////////////////////////
	/**
	 * Commits the transaction to the PageFile as a single 'Unit of Work'. Either all page updates
	 * associated with the transaction are written to disk or none will.
	 */
	public void commit() throws IOException {
		if (writeTransactionId != -1) {
			if (tmpFile != null) {
				tmpFile.close();
				pageFile.removeTmpFile(getTempFile());
				tmpFile = null;
				txFile = null;
			}
			// Actually do the page writes...
			pageFile.write(writes.entrySet());
			// Release the pages that were freed up in the transaction..
			freePages(freeList);
			freeList.clear();
			allocateList.clear();
			writes.clear();
			writeTransactionId = -1;
		}
		size = 0;
	}
	/**
	 * Rolls back the transaction.
	 */
	public void rollback() throws IOException {
		if (writeTransactionId != -1) {
			if (tmpFile != null) {
				tmpFile.close();
				pageFile.removeTmpFile(getTempFile());
				tmpFile = null;
				txFile = null;
			}
			// Release the pages that were allocated in the transaction...
			freePages(allocateList);
			freeList.clear();
			allocateList.clear();
			writes.clear();
			writeTransactionId = -1;
		}
		size = 0;
	}
	/**
	 * 得到一个id
	 * @Title: getWriteTransactionId
	 * @Description: TODO
	 * @return
	 * @return: long
	 */
	private long getWriteTransactionId() {
		if (writeTransactionId == -1) {
			writeTransactionId = pageFile.getNextWriteTransactionId();
		}
		return writeTransactionId;
	}
	protected File getTempFile() {
		if (txFile == null) {
			txFile = new File(getPageFile().getDirectory(),
					IOHelper.toFileSystemSafeName("tx-" + Long.toString(getWriteTransactionId()) + "-"
							+ Long.toString(System.currentTimeMillis()) + ".tmp"));
		}
		return txFile;
	}
	/**
	 * Queues up a page write that should get done when commit() gets called.
	 */
	/**
	 * 将data写入临时文件中，并将pageid和page对象放入writers中
	 * @Title: write
	 * @Description: TODO
	 * @param page
	 * @param data
	 * @throws IOException
	 * @return: void
	 */
	private void write(final Page page, byte[] data) throws IOException {
		Long key = page.getPageId();
		// how much pages we have for this transaction
		size = writes.size() * pageFile.getPageSize();
		PageWrite write;
		if (size > maxTransactionSize) {
			if (tmpFile == null) {
				tmpFile = new RandomAccessFile(getTempFile(), "rw");
			}
			long location = nextLocation;
			tmpFile.seek(nextLocation);
			tmpFile.write(data);
			nextLocation = location + data.length;
			write = new PageWrite(page, location, data.length, getTempFile());
		} else {
			write = new PageWrite(page, data);
		}
		writes.put(key, write);
	}
	/**
	 * @param list
	 * @throws RuntimeException
	 */
	private void freePages(SequenceSet list) throws RuntimeException {
		Sequence seq = list.getHead();
		while (seq != null) {
			seq.each(new Sequence.Closure<RuntimeException>() {
				public void execute(long value) {
					pageFile.freePage(value);
				}
			});
			seq = seq.getNext();
		}
	}
	/**
	 * @return true if there are no uncommitted page file updates associated with this transaction.
	 */
	public boolean isReadOnly() {
		return writeTransactionId == -1;
	}
	// /////////////////////////////////////////////////////////////////
	// Transaction closure helpers...
	// /////////////////////////////////////////////////////////////////
	/**
	 * Executes a closure and if it does not throw any exceptions, then it commits the transaction.
	 * If the closure throws an Exception, then the transaction is rolled back.
	 * @param <T>
	 * @param closure - the work to get exectued.
	 * @throws T if the closure throws it
	 * @throws IOException If the commit fails.
	 */
	public <T extends Throwable> void execute(Closure<T> closure) throws T, IOException {
		boolean success = false;
		try {
			closure.execute(this);
			success = true;
		} finally {
			if (success) {
				commit();
			} else {
				rollback();
			}
		}
	}
	/**
	 * Executes a closure and if it does not throw any exceptions, then it commits the transaction.
	 * If the closure throws an Exception, then the transaction is rolled back.
	 * @param <T>
	 * @param closure - the work to get exectued.
	 * @throws T if the closure throws it
	 * @throws IOException If the commit fails.
	 */
	public <R, T extends Throwable> R execute(CallableClosure<R, T> closure) throws T, IOException {
		boolean success = false;
		try {
			R rc = closure.execute(this);
			success = true;
			return rc;
		} finally {
			if (success) {
				commit();
			} else {
				rollback();
			}
		}
	}
}
