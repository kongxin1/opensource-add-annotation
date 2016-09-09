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

import java.io.File;

import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.util.StoreUtil;

/**
 * Used to keep track of how much of something is being used so that a productive working set usage
 * can be controlled. Main use case is manage memory usage.
 * @org.apache.xbean.XBean
 */
/**
 * 对持久化存储的使用量进行操作，本对象需要关联一个持久化适配器，本对象就是对持久化存储使用量进行限制
 * @ClassName: StoreUsage
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月10日 下午10:31:37
 */
public class StoreUsage extends PercentLimitUsage<StoreUsage> {
	// 持久化存储适配器
	private PersistenceAdapter store;

	public StoreUsage() {
		super(null, null, 1.0f);
	}
	public StoreUsage(String name, PersistenceAdapter store) {
		super(null, name, 1.0f);
		this.store = store;
		updateLimitBasedOnPercent();
	}
	public StoreUsage(StoreUsage parent, String name) {
		super(parent, name, 1.0f);
		this.store = parent.store;
		updateLimitBasedOnPercent();
	}
	@Override
	/**
	 * 获得使用量，其实就是持久化存储适配器使用的量
	 */
	protected long retrieveUsage() {
		if (store == null)
			return 0;
		return store.size();
	}
	public PersistenceAdapter getStore() {
		return store;
	}
	/**
	 * 设置持久化适配器，同时更新limit值
	 * @Title: setStore
	 * @Description: TODO
	 * @param store
	 * @return: void
	 */
	public void setStore(PersistenceAdapter store) {
		this.store = store;
		if (percentLimit > 0 && store != null) {
			// will trigger onLimitChange
			// 更新limit值
			updateLimitBasedOnPercent();
		} else {
			// 只要没有持久化适配器，limit就是0
			// 如果percentLimit<=0，但是持久化适配器不为null，那就可以更新limit的值
			onLimitChange();
		}
	}
	@Override
	public int getPercentUsage() {
		usageLock.writeLock().lock();
		try {
			percentUsage = caclPercentUsage();
			return super.getPercentUsage();
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	@Override
	public boolean waitForSpace(long timeout, int highWaterMark) throws InterruptedException {
		if (parent != null) {
			if (parent.waitForSpace(timeout, highWaterMark)) {
				return true;
			}
		}
		return super.waitForSpace(timeout, highWaterMark);
	}
	@Override
	/**
	 * 根据当前存储文件的父目录所在分区的大小以及percentLimit更新limit的值
	 */
	protected void updateLimitBasedOnPercent() {
		usageLock.writeLock().lock();
		try {
			if (percentLimit > 0 && store != null) {
				File dir = StoreUtil.findParentDirectory(store.getDirectory());
				// 父目录不能为空
				if (dir != null) {
					// getTotalSpace方法返回的是分区大小
					this.setLimit(dir.getTotalSpace() * percentLimit / 100);
				}
			}
		} finally {
			usageLock.writeLock().unlock();
		}
	}
}
