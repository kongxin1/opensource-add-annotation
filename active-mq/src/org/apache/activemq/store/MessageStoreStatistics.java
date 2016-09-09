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
 * ��Ϣ�洢��������������������һ��������������������һ�����ڼ�¼��Ϣ������һ�����ڼ�¼Ŀǰʹ�õĿռ��С
 * @ClassName: MessageStoreStatistics
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��6�� ����11:18:25
 */
public class MessageStoreStatistics extends StatsImpl {
	// ����������¼�����л���topic�е���Ϣ����
	protected CountStatisticImpl messageCount;
	// �洢�ռ����������¼�����л���topic����Ϣ��ռ�ռ���ܴ�С
	protected SizeStatisticImpl messageSize;

	public MessageStoreStatistics() {
		this(true);
	}
	public MessageStoreStatistics(boolean enabled) {
		// ������
		messageCount = new CountStatisticImpl("messageCount",
				"The number of messages in the store passing through the destination");
		// ռ�ÿռ��С������
		messageSize = new SizeStatisticImpl("messageSize",
				"Size of messages in the store passing through the destination");
		// ���Ӽ���������һ������û��
		addStatistic("messageCount", messageCount);
		addStatistic("messageSize", messageSize);
		// ���ü������Ƿ����
		this.setEnabled(enabled);
	}
	public CountStatisticImpl getMessageCount() {
		return messageCount;
	}
	public SizeStatisticImpl getMessageSize() {
		return messageSize;
	}
	/**
	 * ����������ʱ��
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
	 * ���ø���������������������Ҫ����
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