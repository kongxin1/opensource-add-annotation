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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.activemq.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to keep track of how much of something is being used so that a productive working set usage
 * can be controlled. Main use case is manage memory usage.
 * @org.apache.xbean.XBean
 */
/**
 * 对本对象中的大部分元素的访问都需要加上写锁。主要是对limit的操作，包括了回调对象和监听器，如果已经使用的百分比修改，会调用回调对象和监听器。这里面还包括了一个方法是，
 * 等待空间waitForSpace，如果空间不足，可以调用这个方法，这个方法会一直检查percentUsage，直到percentUsage符合要求才会返回
 * @ClassName: Usage
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月9日 下午7:45:02
 * @param <T>
 */
public abstract class Usage<T extends Usage> implements Service {
	private static final Logger LOG = LoggerFactory.getLogger(Usage.class);
	protected final ReentrantReadWriteLock usageLock = new ReentrantReadWriteLock();
	protected final Condition waitForSpaceCondition = usageLock.writeLock().newCondition();
	// 记录已经使用的百分比
	protected int percentUsage;
	// 可以有父Usage
	protected T parent;
	// 等于父Usage的名字加上本对象的名字
	protected String name;
	private UsageCapacity limiter = new DefaultUsageCapacity();
	// percentUsageMinDelta必须大于等于0，具体作用未发现
	private int percentUsageMinDelta = 1;
	// 监听器，当使用的百分比更改时，会调用
	private final List<UsageListener> listeners = new CopyOnWriteArrayList<UsageListener>();
	private final boolean debug = LOG.isDebugEnabled();
	// 当有父Usage时，usagePortion才会起作用，会影响limit值
	private float usagePortion = 1.0f;
	// 存储子Usage
	private final List<T> children = new CopyOnWriteArrayList<T>();
	// 回调集合，当使用的百分比更改时，会回调这些对象
	private final List<Runnable> callbacks = new LinkedList<Runnable>();
	private int pollingTime = 100;
	private final AtomicBoolean started = new AtomicBoolean();
	private ThreadPoolExecutor executor;

	public Usage(T parent, String name, float portion) {
		this.parent = parent;
		this.usagePortion = portion;
		if (parent != null) {
			this.limiter.setLimit((long) (parent.getLimit() * (double) portion));
			name = parent.name + ":" + name;
		}
		this.name = name;
	}
	protected abstract long retrieveUsage();
	/**
	 * @throws InterruptedException
	 */
	public void waitForSpace() throws InterruptedException {
		waitForSpace(0);
	}
	public boolean waitForSpace(long timeout) throws InterruptedException {
		return waitForSpace(timeout, 100);
	}
	/**
	 * @param timeout
	 * @throws InterruptedException
	 * @return true if space
	 */
	/**
	 * 在timeout要求的时间内，将使用的量降到highWaterMark要求的百分比以下，如果降不到返回false，降到返回true
	 * @Title: waitForSpace
	 * @Description: TODO
	 * @param timeout
	 * @param highWaterMark
	 * @return
	 * @throws InterruptedException
	 * @return: boolean
	 */
	public boolean waitForSpace(long timeout, int highWaterMark) throws InterruptedException {
		if (parent != null) {
			// 首先需要满足父Usage的空间要求，如果不能满足，返回false
			if (!parent.waitForSpace(timeout, highWaterMark)) {
				return false;
			}
		}
		// 加锁，只能有一个线程执行下面的操作
		usageLock.writeLock().lock();
		try {
			percentUsage = caclPercentUsage();
			// 如果使用的百分比超过最高要求，则不断进行尝试，如果timeout为0或负数，则表示没有超时时间，需要不断的尝试，直到降低到要求一下才可以
			if (percentUsage >= highWaterMark) {
				long deadline = timeout > 0 ? System.currentTimeMillis() + timeout : Long.MAX_VALUE;
				long timeleft = deadline;
				while (timeleft > 0) {
					percentUsage = caclPercentUsage();
					if (percentUsage >= highWaterMark) {
						waitForSpaceCondition.await(pollingTime, TimeUnit.MILLISECONDS);
						timeleft = deadline - System.currentTimeMillis();
					} else {
						break;
					}
				}
			}
			return percentUsage < highWaterMark;
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	public boolean isFull() {
		return isFull(100);
	}
	/**
	 * 计算当前使用量是否达到最高百分比，如果已经达到父usage的，则直接返回true，否则再计算是否达到本对象的最高百分比
	 * @Title: isFull
	 * @Description: TODO
	 * @param highWaterMark
	 * @return
	 * @return: boolean
	 */
	public boolean isFull(int highWaterMark) {
		if (parent != null && parent.isFull(highWaterMark)) {
			return true;
		}
		usageLock.writeLock().lock();
		try {
			percentUsage = caclPercentUsage();
			return percentUsage >= highWaterMark;
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	public void addUsageListener(UsageListener listener) {
		listeners.add(listener);
	}
	public void removeUsageListener(UsageListener listener) {
		listeners.remove(listener);
	}
	public int getNumUsageListeners() {
		return listeners.size();
	}
	/**
	 * 获取当前的最大限制
	 * @Title: getLimit
	 * @Description: TODO
	 * @return
	 * @return: long
	 */
	public long getLimit() {
		usageLock.readLock().lock();
		try {
			return limiter.getLimit();
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * Sets the memory limit in bytes. Setting the limit in bytes will set the usagePortion to 0
	 * since the UsageManager is not going to be portion based off the parent. When set using Xbean,
	 * values of the form "20 Mb", "1024kb", and "1g" can be used
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.MemoryPropertyEditor"
	 */
	/**
	 * 设置limit的值，并设置usagePortion=0
	 * @Title: setLimit
	 * @Description: TODO
	 * @param limit
	 * @return: void
	 */
	public void setLimit(long limit) {
		if (percentUsageMinDelta < 0) {
			throw new IllegalArgumentException("percentUsageMinDelta must be greater or equal to 0");
		}
		usageLock.writeLock().lock();
		try {
			this.limiter.setLimit(limit);
			this.usagePortion = 0;
		} finally {
			usageLock.writeLock().unlock();
		}
		onLimitChange();
	}
	/**
	 * 设置本对象的limit值，设置percentUsage值，通知子Usage对象，设置limit值，是在父Usage更改时，子的limit值才会更改
	 * @Title: onLimitChange
	 * @Description: TODO
	 * @return: void
	 */
	protected void onLimitChange() {
		// We may need to calculate the limit
		if (usagePortion > 0 && parent != null) {
			usageLock.writeLock().lock();
			try {
				// 根据parent和使用量的百分比设置本对象的limit值
				this.limiter.setLimit((long) (parent.getLimit() * (double) usagePortion));
			} finally {
				usageLock.writeLock().unlock();
			}
		}
		// Reset the percent currently being used.
		usageLock.writeLock().lock();
		try {
			// 更新使用的百分比数
			setPercentUsage(caclPercentUsage());
		} finally {
			usageLock.writeLock().unlock();
		}
		// Let the children know that the limit has changed. They may need to
		// set their limits based on ours.
		// 通知子Usage对象
		for (T child : children) {
			child.onLimitChange();
		}
	}
	public float getUsagePortion() {
		usageLock.readLock().lock();
		try {
			return usagePortion;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * 设置usagePortion属性，并更改limit值，当有父Usage时，usagePortion才会起作用，会影响limit值
	 * @Title: setUsagePortion
	 * @Description: TODO
	 * @param usagePortion
	 * @return: void
	 */
	public void setUsagePortion(float usagePortion) {
		usageLock.writeLock().lock();
		try {
			this.usagePortion = usagePortion;
		} finally {
			usageLock.writeLock().unlock();
		}
		onLimitChange();
	}
	public int getPercentUsage() {
		usageLock.readLock().lock();
		try {
			return percentUsage;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	public int getPercentUsageMinDelta() {
		usageLock.readLock().lock();
		try {
			return percentUsageMinDelta;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * Sets the minimum number of percentage points the usage has to change before a UsageListener
	 * event is fired by the manager.
	 * @param percentUsageMinDelta
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.MemoryPropertyEditor"
	 */
	/**
	 * 更改percentUsageMinDelta，并修改使用量的值
	 * @Title: setPercentUsageMinDelta
	 * @Description: TODO
	 * @param percentUsageMinDelta
	 * @return: void
	 */
	public void setPercentUsageMinDelta(int percentUsageMinDelta) {
		// 这个地方是不是有bug，异常描述和代码不一致
		if (percentUsageMinDelta < 1) {
			throw new IllegalArgumentException("percentUsageMinDelta must be greater than 0");
		}
		usageLock.writeLock().lock();
		try {
			this.percentUsageMinDelta = percentUsageMinDelta;
			setPercentUsage(caclPercentUsage());
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	public long getUsage() {
		usageLock.readLock().lock();
		try {
			return retrieveUsage();
		} finally {
			usageLock.readLock().unlock();
		}
	}
	/**
	 * 更新已经使用的百分比数，并发布事件
	 * @Title: setPercentUsage
	 * @Description: TODO
	 * @param value
	 * @return: void
	 */
	protected void setPercentUsage(int value) {
		usageLock.writeLock().lock();
		try {
			int oldValue = percentUsage;
			percentUsage = value;
			if (oldValue != value) {
				// 调用监听器和回调对象，并释放锁
				fireEvent(oldValue, value);
			}
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	/**
	 * 获取当前已经使用的空间百分比，用使用量除以limit属性值
	 * @Title: caclPercentUsage
	 * @Description: TODO
	 * @return
	 * @return: int
	 */
	protected int caclPercentUsage() {
		if (limiter.getLimit() == 0) {
			return 0;
		}
		return (int) ((((retrieveUsage() * 100) / limiter.getLimit()) / percentUsageMinDelta) * percentUsageMinDelta);
	}
	// Must be called with the usage lock's writeLock held.
	/**
	 * 调用需要回调的对象，调用所有的监听器，释放读锁，对回调对象的调用是有条件的
	 * @Title: fireEvent
	 * @Description: TODO
	 * @param oldPercentUsage
	 * @param newPercentUsage
	 * @return: void
	 */
	private void fireEvent(final int oldPercentUsage, final int newPercentUsage) {
		if (debug) {
			LOG.debug(getName() + ": usage change from: " + oldPercentUsage + "% of available memory, to: "
					+ newPercentUsage + "% of available memory");
		}
		// 如果已经启动
		if (started.get()) {
			// Switching from being full to not being full..
			if (oldPercentUsage >= 100 && newPercentUsage < 100) {
				// 发出通知
				waitForSpaceCondition.signalAll();
				// 对所有的回调对象进行回调
				if (!callbacks.isEmpty()) {
					for (Runnable callback : callbacks) {
						getExecutor().execute(callback);
					}
					callbacks.clear();
				}
			}
			// 异步调用监听器
			if (!listeners.isEmpty()) {
				// Let the listeners know on a separate thread
				Runnable listenerNotifier = new Runnable() {
					@Override
					public void run() {
						for (UsageListener listener : listeners) {
							listener.onUsageChanged(Usage.this, oldPercentUsage, newPercentUsage);
						}
					}
				};
				if (started.get()) {
					getExecutor().execute(listenerNotifier);
				} else {
					LOG.warn("Not notifying memory usage change to listeners on shutdown");
				}
			}
		}
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		return "Usage(" + getName() + ") percentUsage=" + percentUsage + "%, usage=" + retrieveUsage() + ", limit="
				+ limiter.getLimit() + ", percentUsageMinDelta=" + percentUsageMinDelta + "%"
				+ (parent != null ? ";Parent:" + parent.toString() : "");
	}
	@Override
	@SuppressWarnings("unchecked")
	/**
	 * 将本对象添加到父Usage中，并调用子Usage的start方法
	 */
	public void start() {
		if (started.compareAndSet(false, true)) {
			if (parent != null) {
				parent.addChild(this);
				if (getLimit() > parent.getLimit()) {
					LOG.info("Usage({}) limit={} should be smaller than its parent limit={}", new Object[] { getName(),
							getLimit(), parent.getLimit() });
				}
			}
			for (T t : children) {
				t.start();
			}
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	/**
	 * 将本对象从父Usage中移除，并同步调用回调对象，然后调用子Usage的stop方法
	 */
	public void stop() {
		if (started.compareAndSet(true, false)) {
			if (parent != null) {
				parent.removeChild(this);
			}
			// clear down any callbacks
			usageLock.writeLock().lock();
			try {
				waitForSpaceCondition.signalAll();
				for (Runnable callback : this.callbacks) {
					callback.run();
				}
				this.callbacks.clear();
			} finally {
				usageLock.writeLock().unlock();
			}
			for (T t : children) {
				t.stop();
			}
		}
	}
	protected void addChild(T child) {
		children.add(child);
		if (started.get()) {
			child.start();
		}
	}
	protected void removeChild(T child) {
		children.remove(child);
	}
	/**
	 * @param callback
	 * @return true if the UsageManager was full. The callback will only be called if this method
	 *         returns true.
	 */
	/**
	 * 如果使用的百分比大于等于100，返回true，这会首先检测父Usage，父返回false时，才会再检测子Usage，这个方法不会调用入参，
	 * 根据使用的百分比是否大于等于100将入参加入回调对象集合中
	 * @Title: notifyCallbackWhenNotFull
	 * @Description: TODO
	 * @param callback
	 * @return
	 * @return: boolean
	 */
	public boolean notifyCallbackWhenNotFull(final Runnable callback) {
		if (parent != null) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					usageLock.writeLock().lock();
					try {
						if (percentUsage >= 100) {
							callbacks.add(callback);
						} else {
							callback.run();
						}
					} finally {
						usageLock.writeLock().unlock();
					}
				}
			};
			if (parent.notifyCallbackWhenNotFull(r)) {
				return true;
			}
		}
		usageLock.writeLock().lock();
		try {
			if (percentUsage >= 100) {
				callbacks.add(callback);
				return true;
			} else {
				return false;
			}
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	/**
	 * @return the limiter
	 */
	public UsageCapacity getLimiter() {
		return this.limiter;
	}
	/**
	 * @param limiter the limiter to set
	 */
	public void setLimiter(UsageCapacity limiter) {
		this.limiter = limiter;
	}
	/**
	 * @return the pollingTime
	 */
	public int getPollingTime() {
		return this.pollingTime;
	}
	/**
	 * @param pollingTime the pollingTime to set
	 */
	public void setPollingTime(int pollingTime) {
		this.pollingTime = pollingTime;
	}
	public void setName(String name) {
		this.name = name;
	}
	public T getParent() {
		return parent;
	}
	public void setParent(T parent) {
		this.parent = parent;
	}
	public void setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
	}
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	public boolean isStarted() {
		return started.get();
	}
}
