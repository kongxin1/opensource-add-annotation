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

import javax.management.j2ee.statistics.Statistic;

/**
 * Base class for a Statistic implementation
 */
/**
 * 类中方法基本都是一些getter和setter方法，没有具体的逻辑，本方法中没有计数器的属性，在其子类中会有，unit字段用处不大，仅仅是在打印的时候会打印出来，不参与计数器中的计算
 * @ClassName: StatisticImpl
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月6日 下午10:08:05
 */
public class StatisticImpl implements Statistic, Resettable {
	protected boolean enabled;
	private String name;
	private String unit;
	private String description;
	// 记录启用时间
	private long startTime;
	// 应该是计数器的最后更改时间
	private long lastSampleTime;
	private boolean doReset = true;

	public StatisticImpl(String name, String unit, String description) {
		this.name = name;
		this.unit = unit;
		this.description = description;
		this.startTime = System.currentTimeMillis();
		this.lastSampleTime = this.startTime;
	}
	/**
	 * 重置计数器，重置操作会更新计数器的开始时间和最后的修改时间
	 */
	public synchronized void reset() {
		if (isDoReset()) {
			this.startTime = System.currentTimeMillis();
			this.lastSampleTime = this.startTime;
		}
	}
	/**
	 * 更新最后的修改时间
	 * @Title: updateSampleTime
	 * @Description: TODO
	 * @return: void
	 */
	protected synchronized void updateSampleTime() {
		this.lastSampleTime = System.currentTimeMillis();
	}
	public synchronized String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name);
		buffer.append("{");
		appendFieldDescription(buffer);
		buffer.append(" }");
		return buffer.toString();
	}
	public String getName() {
		return this.name;
	}
	public String getUnit() {
		return this.unit;
	}
	public String getDescription() {
		return this.description;
	}
	public synchronized long getStartTime() {
		return this.startTime;
	}
	public synchronized long getLastSampleTime() {
		return this.lastSampleTime;
	}
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * @return the doReset
	 */
	/**
	 * 是否允许重置计数器
	 * @Title: isDoReset
	 * @Description: TODO
	 * @return
	 * @return: boolean
	 */
	public boolean isDoReset() {
		return this.doReset;
	}
	/**
	 * @param doReset the doReset to set
	 */
	public void setDoReset(boolean doReset) {
		this.doReset = doReset;
	}
	/**
	 * 可以数据本对象中的一些信息，算是一个打印类，打印的信息都在入参中
	 * @Title: appendFieldDescription
	 * @Description: TODO
	 * @param buffer
	 * @return: void
	 */
	protected synchronized void appendFieldDescription(StringBuffer buffer) {
		buffer.append(" unit: ");
		buffer.append(this.unit);
		buffer.append(" startTime: ");
		// buffer.append(new Date(startTime));
		buffer.append(this.startTime);
		buffer.append(" lastSampleTime: ");
		// buffer.append(new Date(lastSampleTime));
		buffer.append(this.lastSampleTime);
		buffer.append(" description: ");
		buffer.append(this.description);
	}
}
