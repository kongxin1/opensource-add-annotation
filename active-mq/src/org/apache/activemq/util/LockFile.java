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
package org.apache.activemq.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to lock a File.
 * @author chirino
 */
/**
 * 文件锁，可以通过本对象获得文件锁和释放文件锁
 * @ClassName: LockFile
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月8日 下午11:38:20
 */
public class LockFile {
	// 表示是否需要文件锁
	private static final boolean DISABLE_FILE_LOCK = Boolean.getBoolean("java.nio.channels.FileLock.broken");
	final private File file;
	// 记录被锁定文件的最后修改时间
	private long lastModified;
	// 文件锁，可以对文件进行上锁
	private FileLock lock;
	// 被锁定的文件，lock属性是从本对象中获得的
	private RandomAccessFile randomAccessLockFile;
	// 表示目前获得锁的个数，lockCount最大值就是1，因为下面获得的锁是独占锁
	private int lockCounter;
	// 表示锁释放时，文件是否删除
	private final boolean deleteOnUnlock;
	// 表示是否已经锁定
	private volatile boolean locked;
	private String lockSystemPropertyName = "";
	private static final Logger LOG = LoggerFactory.getLogger(LockFile.class);

	public LockFile(File file, boolean deleteOnUnlock) {
		this.file = file;
		this.deleteOnUnlock = deleteOnUnlock;
	}
	/**
	 * @throws IOException
	 */
	/**
	 * 获得独占的文件锁，并将部分信息写入系统属性中，以表示这个锁已经被占用，同时也可以加快运行速度
	 * @Title: lock
	 * @Description: TODO
	 * @throws IOException
	 * @return: void
	 */
	synchronized public void lock() throws IOException {
		if (DISABLE_FILE_LOCK) {
			return;
		}
		if (lockCounter > 0) {
			return;
		}
		IOHelper.mkdirs(file.getParentFile());
		synchronized (LockFile.class) {
			// 得到一个锁的名字，下面会将名字和日期放入系统属性中，以表示这个文件已经有锁了
			lockSystemPropertyName = getVmLockKey();
			if (System.getProperty(lockSystemPropertyName) != null) {
				throw new IOException("File '" + file
						+ "' could not be locked as lock is already held for this jvm. Value: "
						+ System.getProperty(lockSystemPropertyName));
			}
			System.setProperty(lockSystemPropertyName, new Date().toString());
		}
		try {
			if (lock == null) {
				randomAccessLockFile = new RandomAccessFile(file, "rw");
				IOException reason = null;
				try {
					// 获取文件file的独占锁，锁定的区域大小是当前文件的大小
					lock = randomAccessLockFile.getChannel().tryLock(0,
							Math.max(1, randomAccessLockFile.getChannel().size()), false);
				} catch (OverlappingFileLockException e) {
					reason = IOExceptionSupport.create("File '" + file + "' could not be locked.", e);
				} catch (IOException ioe) {
					reason = ioe;
				}
				if (lock != null) {
					// 拿到锁之后，就将当前时间写入文件，lockCounter加1
					// track lastModified only if we are able to successfully obtain the lock.
					randomAccessLockFile.writeLong(System.currentTimeMillis());
					randomAccessLockFile.getChannel().force(true);
					lastModified = file.lastModified();
					lockCounter++;
					System.setProperty(lockSystemPropertyName, new Date().toString());
					locked = true;
				} else {
					// new read file for next attempt
					closeReadFile();
					if (reason != null) {
						throw reason;
					}
					throw new IOException("File '" + file + "' could not be locked.");
				}
			}
		} finally {
			synchronized (LockFile.class) {
				if (lock == null) {
					// 获得锁如果不成功，就将系统属性删除
					System.getProperties().remove(lockSystemPropertyName);
				}
			}
		}
	}
	/**
     */
	/**
	 * 当lockCount为0时，释放锁，并删除对应文件和系统属性
	 * @Title: unlock
	 * @Description: TODO
	 * @return: void
	 */
	public void unlock() {
		if (DISABLE_FILE_LOCK) {
			return;
		}
		lockCounter--;
		if (lockCounter != 0) {
			return;
		}
		// release the lock..
		if (lock != null) {
			try {
				lock.release();
			} catch (Throwable ignore) {
			} finally {
				if (lockSystemPropertyName != null) {
					System.getProperties().remove(lockSystemPropertyName);
				}
				lock = null;
			}
		}
		closeReadFile();
		if (locked && deleteOnUnlock) {
			file.delete();
		}
	}
	/**
	 * 返回值是类名+“.lock.”+入参的file路径名
	 * @Title: getVmLockKey
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: String
	 */
	private String getVmLockKey() throws IOException {
		return getClass().getName() + ".lock." + file.getCanonicalPath();
	}
	private void closeReadFile() {
		// close the file.
		if (randomAccessLockFile != null) {
			try {
				randomAccessLockFile.close();
			} catch (Throwable ignore) {
			}
			randomAccessLockFile = null;
		}
	}
	/**
	 * @return true if the lock file's last modified does not match the locally cached lastModified,
	 *         false otherwise
	 */
	/**
	 * 判断文件的最后修改时间和对象中记录的最后修改时间是否一致，不一致返回false
	 * @Title: hasBeenModified
	 * @Description: TODO
	 * @return
	 * @return: boolean
	 */
	private boolean hasBeenModified() {
		boolean modified = false;
		// Create a new instance of the File object so we can get the most up to date information on
		// the file.
		File localFile = new File(file.getAbsolutePath());
		if (localFile.exists()) {
			if (localFile.lastModified() != lastModified) {
				LOG.info("Lock file " + file.getAbsolutePath() + ", locked at " + new Date(lastModified)
						+ ", has been modified at " + new Date(localFile.lastModified()));
				modified = true;
			}
		} else {
			// The lock file is missing
			LOG.info("Lock file " + file.getAbsolutePath() + ", does not exist");
			modified = true;
		}
		return modified;
	}
	/**
	 * 判断锁是否还可用，如果没有上锁或者锁已经无效，或者文件的最后修改时间与对象中记录的最后修改时间不一致，都会认为锁已经无效
	 * @Title: keepAlive
	 * @Description: TODO
	 * @return
	 * @return: boolean
	 */
	public boolean keepAlive() {
		locked = locked && lock != null && lock.isValid() && !hasBeenModified();
		return locked;
	}
}
