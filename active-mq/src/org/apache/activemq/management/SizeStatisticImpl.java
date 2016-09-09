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

/**
 * 计数器，本计数器是用于记录使用的存储空间大小的计数器
 * @ClassName: SizeStatisticImpl
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月6日 下午10:28:10
 */
public class SizeStatisticImpl extends StatisticImpl {
	// 记录空间大小改变的次数
	private long count;
	// 记录一次性添加的空间大小的最大值
	private long maxSize;
	// 记录一次性添加的空间大小的最小值
	private long minSize;
	// 记录使用空间总大小
	private long totalSize;
	private SizeStatisticImpl parent;

	/**
	 * 默认以字节记存储空间
	 * @Title:SizeStatisticImpl
	 * @Description:TODO
	 * @param name
	 * @param description
	 */
	public SizeStatisticImpl(String name, String description) {
		this(name, "bytes", description);
	}
	public SizeStatisticImpl(SizeStatisticImpl parent, String name, String description) {
		this(name, description);
		this.parent = parent;
	}
	public SizeStatisticImpl(String name, String unit, String description) {
		super(name, unit, description);
	}
	@Override
	/**
	 * 重置计数器
	 */
	public synchronized void reset() {
		if (isDoReset()) {
			super.reset();
			count = 0;
			maxSize = 0;
			minSize = 0;
			totalSize = 0;
		}
	}
	public synchronized long getCount() {
		return count;
	}
	/**
	 * 空间大小增加
	 * @Title: addSize
	 * @Description: TODO
	 * @param size
	 * @return: void
	 */
	public synchronized void addSize(long size) {
		count++;
		totalSize += size;
		if (size > maxSize) {
			maxSize = size;
		}
		if (size < minSize || minSize == 0) {
			minSize = size;
		}
		updateSampleTime();
		if (parent != null) {
			parent.addSize(size);
		}
	}
	/**
	 * Reset the total size to the new value
	 * @param size
	 */
	/**
	 * 设置空间的总大小
	 * @Title: setTotalSize
	 * @Description: TODO
	 * @param size
	 * @return: void
	 */
	public synchronized void setTotalSize(long size) {
		count++;
		totalSize = size;
		if (size > maxSize) {
			maxSize = size;
		}
		if (size < minSize || minSize == 0) {
			minSize = size;
		}
		updateSampleTime();
	}
	/**
	 * @return the maximum size of any step
	 */
	public long getMaxSize() {
		return maxSize;
	}
	/**
	 * @return the minimum size of any step
	 */
	public synchronized long getMinSize() {
		return minSize;
	}
	/**
	 * @return the total size of all the steps added together
	 */
	public synchronized long getTotalSize() {
		return totalSize;
	}
	/**
	 * @return the average size calculated by dividing the total size by the number of counts
	 */
	/**
	 * 得到平均每次增加的空间大小
	 * @Title: getAverageSize
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public synchronized double getAverageSize() {
		if (count == 0) {
			return 0;
		}
		double d = totalSize;
		return d / count;
	}
	/**
	 * @return the average size calculated by dividing the total size by the number of counts but
	 *         excluding the minimum and maximum sizes.
	 */
	/**
	 * 除了最大值和最小值之外，平均每次增加的空间大小
	 * @Title: getAverageSizeExcludingMinMax
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public synchronized double getAverageSizeExcludingMinMax() {
		if (count <= 2) {
			return 0;
		}
		double d = totalSize - minSize - maxSize;
		return d / (count - 2);
	}
	/**
	 * @return the average number of steps per second
	 */
	/**
	 * 不明白
	 * @Title: getAveragePerSecond
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public double getAveragePerSecond() {
		double d = 1000;
		double averageSize = getAverageSize();
		if (averageSize == 0) {
			return 0;
		}
		return d / averageSize;
	}
	/**
	 * @return the average number of steps per second excluding the min & max values
	 */
	/**
	 * 不明白
	 * @Title: getAveragePerSecondExcludingMinMax
	 * @Description: TODO
	 * @return
	 * @return: double
	 */
	public double getAveragePerSecondExcludingMinMax() {
		double d = 1000;
		double average = getAverageSizeExcludingMinMax();
		if (average == 0) {
			return 0;
		}
		return d / average;
	}
	public SizeStatisticImpl getParent() {
		return parent;
	}
	public void setParent(SizeStatisticImpl parent) {
		this.parent = parent;
	}
	@Override
	protected synchronized void appendFieldDescription(StringBuffer buffer) {
		buffer.append(" count: ");
		buffer.append(Long.toString(count));
		buffer.append(" maxSize: ");
		buffer.append(Long.toString(maxSize));
		buffer.append(" minSize: ");
		buffer.append(Long.toString(minSize));
		buffer.append(" totalSize: ");
		buffer.append(Long.toString(totalSize));
		buffer.append(" averageSize: ");
		buffer.append(Double.toString(getAverageSize()));
		buffer.append(" averageTimeExMinMax: ");
		buffer.append(Double.toString(getAveragePerSecondExcludingMinMax()));
		buffer.append(" averagePerSecond: ");
		buffer.append(Double.toString(getAveragePerSecond()));
		buffer.append(" averagePerSecondExMinMax: ");
		buffer.append(Double.toString(getAveragePerSecondExcludingMinMax()));
		super.appendFieldDescription(buffer);
	}
}
