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
 * ��Usage������Ĭ���ǡ�default���������еķ���������ʹ�ÿռ�ͼ���ʹ�ÿռ��Լ��ȴ��ռ併�͵�����Ҫ��ķ���
 * @ClassName: MemoryUsage
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��10�� ����8:09:40
 */
public class MemoryUsage extends Usage<MemoryUsage> {
	// Ŀǰ��Ϊ������Ա�ʾ�����Ѿ�ʹ�õ���
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
	 * �ȴ��ռ��ͷţ���ʹ�ռ�ʹ�ñ���С��100���������������ÿ��ȥ�鿴�ռ�ʹ�ñ������ԣ���������㣬�͵ȴ�
	 */
	public void waitForSpace() throws InterruptedException {
		// ���ȵ��ø������waitForSpace������Ҫʹ��������������
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
						// �����е�fireEvent���������signalAll��������ʹ����Ĵ����˳��ȴ�
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
	 * ���г�ʱʱ��ĵȴ��������ʱʱ����ָwaitForSpaceCondition.await�ĵȴ�ʱ�䣬һ����ʱ������ռ�ʹ�ñ����Ƿ�С��100�������㣬������ȴ���ֱ������Ϊֹ������ռ�ʹ�ñ���С��100���᷵��true��������false
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
	 * ���ռ��Ƿ��Ѿ���ȫʹ�ã����ȼ�鸸����Ŀռ�ʹ�ñ��������������û��ʹ���꣬�Ż��鱾�����
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
	 * ���ʹ�ÿռ�����Ƿ�С��100�������С�ھ�һֱ�ȴ������С��100���ռ�ʹ�������������Ҫ�������
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
	 * �ռ�ʹ�����������Ҫ������֣�Ҳ����usage����value��ͬʱ���¿ռ�ʹ��������
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
	 * �ռ�ʹ�����������Ҫ������֣�ͬʱ���¿ռ�ʹ��������
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
	 * ����limit�Ĵ�СΪjvm����ʹ�õ�����ڴ�İٷ�֮percentOfJvmHeap
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
