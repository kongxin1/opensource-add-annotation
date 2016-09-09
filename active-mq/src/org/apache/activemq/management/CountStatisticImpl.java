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
package org.apache.activemq.management;

import java.util.concurrent.atomic.AtomicLong;

import javax.management.j2ee.statistics.CountStatistic;

/**
 * A count statistic implementation
 */
/**
 * 计数器类，可以进行计数，里面有一个count属性可以计数，使用计数器的前提是计数器是允许使用的，即enabled属性为true，否则计数的方法都不执行
 * @ClassName: CountStatisticImpl
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月6日 下午10:13:13
 */
public class CountStatisticImpl extends StatisticImpl implements CountStatistic {
	// 计数器
	private final AtomicLong counter = new AtomicLong(0);
	// 可以持有父计数器
	private CountStatisticImpl parent;

	public CountStatisticImpl(CountStatisticImpl parent, String name, String description) {
		this(name, description);
		this.parent = parent;
	}
	public CountStatisticImpl(String name, String description) {
		this(name, "count", description);
	}
	public CountStatisticImpl(String name, String unit, String description) {
		super(name, unit, description);
	}
	/**
	 * 将计数器重置为0
	 */
	public void reset() {
		if (isDoReset()) {
			super.reset();
			counter.set(0);
		}
	}
	public long getCount() {
		return counter.get();
	}
	/**
	 * 将计数器设置为入参值
	 * @Title: setCount
	 * @Description: TODO
	 * @param count
	 * @return: void
	 */
	public void setCount(long count) {
		if (isEnabled()) {
			counter.set(count);
		}
	}
	/**
	 * 将计数器加上入参值，并更新时间，如果父计数器不为空，同时父计数器也增加入参值
	 * @Title: add
	 * @Description: TODO
	 * @param amount
	 * @return: void
	 */
	public void add(long amount) {
		if (isEnabled()) {
			counter.addAndGet(amount);
			updateSampleTime();
			if (parent != null) {
				parent.add(amount);
			}
		}
	}
	/**
	 * 计数器加一，并更新时间，如果父计数器不为空，同时父计数器也增加1
	 * @Title: increment
	 * @Description: TODO
	 * @return: void
	 */
	public void increment() {
		if (isEnabled()) {
			counter.incrementAndGet();
			updateSampleTime();
			if (parent != null) {
				parent.increment();
			}
		}
	}
	/**
	 * 将计数器减入参值，并更新时间，如果父计数器不为空，同时父计数器也减入参值
	 * @Title: subtract
	 * @Description: TODO
	 * @param amount
	 * @return: void
	 */
	public void subtract(long amount) {
		if (isEnabled()) {
			counter.addAndGet(-amount);
			updateSampleTime();
			if (parent != null) {
				parent.subtract(amount);
			}
		}
	}
	/**
	 * 计数器减一，并更新时间，如果父计数器不为空，同时父计数器也减1
	 * @Title: decrement
	 * @Description: TODO
	 * @return: void
	 */
	public void decrement() {
		if (isEnabled()) {
			counter.decrementAndGet();
			updateSampleTime();
			if (parent != null) {
				parent.decrement();
			}
		}
	}
	public CountStatisticImpl getParent() {
		return parent;
	}
	/**
	 * 设置父计数器，同时父计数器减去子计数器此时的值，不清楚为什么这么减
	 * @Title: setParent
	 * @Description: TODO
	 * @param parent
	 * @return: void
	 */
	public void setParent(CountStatisticImpl parent) {
		if (this.parent != null) {
			this.parent.subtract(this.getCount());
		}
		this.parent = parent;
	}
	protected void appendFieldDescription(StringBuffer buffer) {
		buffer.append(" count: ");
		buffer.append(Long.toString(counter.get()));
		super.appendFieldDescription(buffer);
	}
	/**
	 * @return the average time period that elapses between counter increments since the last reset.
	 */
	/**
	 * 返回计数器增加1需要用多少秒
	 * @Title: getPeriod
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public double getPeriod() {
		double count = counter.get();
		if (count == 0) {
			return 0;
		}
		double time = System.currentTimeMillis() - getStartTime();
		return time / (count * 1000.0);
	}
	/**
	 * @return the number of times per second that the counter is incrementing since the last reset.
	 */
	/**
	 * 返回计数器平均每秒增加多少
	 * @Title: getFrequency
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public double getFrequency() {
		double count = counter.get();
		double time = System.currentTimeMillis() - getStartTime();
		return count * 1000.0 / time;
	}
}
