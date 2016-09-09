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

/**
 * 对使用量的百分比进行记录和限制的类
 * @ClassName: PercentLimitUsage
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月10日 下午10:52:40
 * @param <T>
 */
public abstract class PercentLimitUsage<T extends Usage> extends Usage<T> {
	// 已使用量的百分比
	protected int percentLimit = 0;

	/**
	 * @param parent
	 * @param name
	 * @param portion
	 */
	public PercentLimitUsage(T parent, String name, float portion) {
		super(parent, name, portion);
	}
	/**
	 * 设置已使用量的百分比，同时根据这个百分比更新limit值
	 * @Title: setPercentLimit
	 * @Description: TODO
	 * @param percentLimit
	 * @return: void
	 */
	public void setPercentLimit(int percentLimit) {
		usageLock.writeLock().lock();
		try {
			this.percentLimit = percentLimit;
			updateLimitBasedOnPercent();
		} finally {
			usageLock.writeLock().unlock();
		}
	}
	/**
	 * 获得本对象中的已使用量的百分比的值，也就是percentLimit属性值
	 * @Title: getPercentLimit
	 * @Description: TODO
	 * @return
	 * @return: int
	 */
	public int getPercentLimit() {
		usageLock.readLock().lock();
		try {
			return percentLimit;
		} finally {
			usageLock.readLock().unlock();
		}
	}
	protected abstract void updateLimitBasedOnPercent();
}
