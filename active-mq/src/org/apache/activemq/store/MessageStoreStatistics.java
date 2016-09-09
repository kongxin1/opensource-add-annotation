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
package org.apache.activemq.store;

import org.apache.activemq.management.CountStatisticImpl;
import org.apache.activemq.management.SizeStatisticImpl;
import org.apache.activemq.management.StatsImpl;

/**
 * The J2EE Statistics for a Message Sore
 */
/**
 * 消息存储器计数器，本计数器中一共包含有两个计数器，一个用于记录消息个数，一个用于记录目前使用的空间大小
 * @ClassName: MessageStoreStatistics
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月6日 下午11:18:25
 */
public class MessageStoreStatistics extends StatsImpl {
	// 计数器，记录本队列或者topic中的消息总数
	protected CountStatisticImpl messageCount;
	// 存储空间计数器，记录本队列或者topic中消息所占空间的总大小
	protected SizeStatisticImpl messageSize;

	public MessageStoreStatistics() {
		this(true);
	}
	public MessageStoreStatistics(boolean enabled) {
		// 计数器
		messageCount = new CountStatisticImpl("messageCount",
				"The number of messages in the store passing through the destination");
		// 占用空间大小计数器
		messageSize = new SizeStatisticImpl("messageSize",
				"Size of messages in the store passing through the destination");
		// 添加计数器，第一个参数没用
		addStatistic("messageCount", messageCount);
		addStatistic("messageSize", messageSize);
		// 设置计数器是否可用
		this.setEnabled(enabled);
	}
	public CountStatisticImpl getMessageCount() {
		return messageCount;
	}
	public SizeStatisticImpl getMessageSize() {
		return messageSize;
	}
	/**
	 * 重置两个计时器
	 */
	public void reset() {
		if (this.isDoReset()) {
			super.reset();
			messageCount.reset();
			messageSize.reset();
		}
	}
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		messageCount.setEnabled(enabled);
		messageSize.setEnabled(enabled);
	}
	/**
	 * 设置父计数器，两个计数器都要设置
	 * @Title: setParent
	 * @Description: TODO
	 * @param parent
	 * @return: void
	 */
	public void setParent(MessageStoreStatistics parent) {
		if (parent != null) {
			messageCount.setParent(parent.messageCount);
			messageSize.setParent(parent.messageSize);
		} else {
			messageCount.setParent(null);
			messageSize.setParent(null);
		}
	}
}
