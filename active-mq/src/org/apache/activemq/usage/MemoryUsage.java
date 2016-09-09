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
package org.apache.activemq.usage;

import java.util.concurrent.TimeUnit;

/**
 * Used to keep track of how much of something is being used so that a productive working set usage
 * can be controlled. Main use case is manage memory usage.
 * @org.apache.xbean.XBean
 */
/**
 * 本Usage的名字默认是“default”，本类中的方法有增加使用空间和减少使用空间以及等待空间降低到符合要求的方法
 * @ClassName: MemoryUsage
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月10日 下午8:09:40
 */
public class MemoryUsage extends Usage<MemoryUsage> {
	// 目前认为这个属性表示的是已经使用的量
	private long usage;

	public MemoryUsage() {
		this(null, null);
	}
	/**
	 * Create the memory manager linked to a parent. When the memory manager is linked to a parent
	 * then when usage increased or decreased, the parent's usage is also increased or decreased.
	 * @param parent
	 */
	public MemoryUsage(MemoryUsage parent) {
		this(parent, "default");
	}
	public MemoryUsage(String name) {
		this(null, name);
	}
	public MemoryUsage(MemoryUsage parent, String name) {
		this(parent, name, 1.0f);
	}
	public MemoryUsage(MemoryUsage parent, String name, float portion) {
		super(parent, name, portion);
	}
	/**
	 * @throws InterruptedException
	 */
	@Override
	/**
	 * 等待空间释放，以使空间使用比例小于100，这里面仅仅就是每次去查看空间使用比例属性，如果不满足，就等待
	 */
	public void waitForSpace() throws InterruptedException {
		// 首先调用父对象的waitForSpace方法，要使父对象首先满足
		if (parent != null) {
			parent.waitForSpace();
		}
		usageLock.readLock().lock();
		try {
			if (percentUsage >= 100 && isStarted()) {
				usageLock.readLock().unlock();
				usageLock.writeLock().lock();
				try {
					while (percentUsage >= 100 && isStarted()) {
						// 父类中的fireEvent方法会调用signalAll方法，以使下面的代码退出等待
						waitForSpaceCondition.await();
					}
				} finally {
					usageLock.writeLock().unlock();
					usageLock.readLock().lock();
				}
			}
			if (percentUsage >= 100 && !isStarted()) {
				throw new InterruptedException("waitForSpace stopped during wait.");
			}
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * @param timeout
	 * @throws InterruptedException
	 * @return true if space
	 */
	@Override
	/**
	 * 带有超时时间的等待，这个超时时间是指waitForSpaceCondition.await的等待时间，一旦超时，会检查空间使用比例是否小于100，不满足，会继续等待，直到满足为止，如果空间使用比例小于100，会返回true，否则是false
	 */
	public boolean waitForSpace(long timeout) throws InterruptedException {
		if (parent != null) {
			if (!parent.waitForSpace(timeout)) {
				return false;
			}
		}
		usageLock.readLock().lock();
		try {
			if (percentUsage >= 100) {
				usageLock.readLock().unlock();
				usageLock.writeLock().lock();
				try {
					while (percentUsage >= 100) {
						waitForSpaceCondition.await(timeout, TimeUnit.MILLISECONDS);
					}
					usageLock.readLock().lock();
				} finally {
					usageLock.writeLock().unlock();
				}
			}
			return percentUsage < 100;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	@Override
	/**
	 * 检查空间是否已经完全使用，首先检查父对象的空间使用比例，如果父对象没有使用完，才会检查本对象的
	 */
	public boolean isFull() {
		if (parent != null && parent.isFull()) {
			return true;
		}
		usageLock.readLock().lock();
		try {
			return percentUsage >= 100;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * Tries to increase the usage by value amount but blocks if this object is currently full.
	 * @param value
	 * @throws InterruptedException
	 */
	/**
	 * 检查使用空间比例是否小于100，如果不小于就一直等待，如果小于100，空间使用量就增加入参要求的数字
	 * @Title: enqueueUsage
	 * @Description: TODO
	 * @param value
	 * @throws InterruptedException
	 * @return: void
	 */
	public void enqueueUsage(long value) throws InterruptedException {
		waitForSpace();
		increaseUsage(value);
	}
	/**
	 * Increases the usage by the value amount.
	 * @param value
	 */
	/**
	 * 空间使用量增加入参要求的数字，也就是usage增加value，同时更新空间使用量比例
	 * @Title: increaseUsage
	 * @Description: TODO
	 * @param value
	 * @return: void
	 */
	public void increaseUsage(long value) {
		if (value == 0) {
			return;
		}
		usageLock.writeLock().lock();
		try {
			usage += value;
			setPercentUsage(caclPercentUsage());
		} finally {
			usageLock.writeLock().unlock();
		}
		if (parent != null) {
			parent.increaseUsage(value);
		}
	}
	/**
	 * Decreases the usage by the value amount.
	 * @param value
	 */
	/**
	 * 空间使用量减少入参要求的数字，同时更新空间使用量比例
	 * @Title: decreaseUsage
	 * @Description: TODO
	 * @param value
	 * @return: void
	 */
	public void decreaseUsage(long value) {
		if (value == 0) {
			return;
		}
		usageLock.writeLock().lock();
		try {
			usage -= value;
			setPercentUsage(caclPercentUsage());
		} finally {
			usageLock.writeLock().unlock();
		}
		if (parent != null) {
			parent.decreaseUsage(value);
		}
	}
	@Override
	protected long retrieveUsage() {
		return usage;
	}
	@Override
	public long getUsage() {
		return usage;
	}
	public void setUsage(long usage) {
		this.usage = usage;
	}
	/**
	 * 设置limit的大小为jvm可以使用的最大内存的百分之percentOfJvmHeap
	 * @Title: setPercentOfJvmHeap
	 * @Description: TODO
	 * @param percentOfJvmHeap
	 * @return: void
	 */
	public void setPercentOfJvmHeap(int percentOfJvmHeap) {
		if (percentOfJvmHeap > 0) {
			setLimit(Math.round(Runtime.getRuntime().maxMemory() * percentOfJvmHeap / 100.0));
		}
	}
}
