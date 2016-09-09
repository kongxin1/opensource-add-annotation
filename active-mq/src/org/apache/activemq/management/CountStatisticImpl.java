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
 * �������࣬���Խ��м�����������һ��count���Կ��Լ�����ʹ�ü�������ǰ���Ǽ�����������ʹ�õģ���enabled����Ϊtrue����������ķ�������ִ��
 * @ClassName: CountStatisticImpl
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��6�� ����10:13:13
 */
public class CountStatisticImpl extends StatisticImpl implements CountStatistic {
	// ������
	private final AtomicLong counter = new AtomicLong(0);
	// ���Գ��и�������
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
	 * ������������Ϊ0
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
	 * ������������Ϊ���ֵ
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
	 * ���������������ֵ��������ʱ�䣬�������������Ϊ�գ�ͬʱ��������Ҳ�������ֵ
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
	 * ��������һ��������ʱ�䣬�������������Ϊ�գ�ͬʱ��������Ҳ����1
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
	 * �������������ֵ��������ʱ�䣬�������������Ϊ�գ�ͬʱ��������Ҳ�����ֵ
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
	 * ��������һ��������ʱ�䣬�������������Ϊ�գ�ͬʱ��������Ҳ��1
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
	 * ���ø���������ͬʱ����������ȥ�Ӽ�������ʱ��ֵ�������Ϊʲô��ô��
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
	 * ���ؼ���������1��Ҫ�ö�����
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
	 * ���ؼ�����ƽ��ÿ�����Ӷ���
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
