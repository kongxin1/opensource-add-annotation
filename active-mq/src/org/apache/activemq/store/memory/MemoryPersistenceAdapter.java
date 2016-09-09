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
package org.apache.activemq.store.memory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.scheduler.JobSchedulerStore;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ProducerId;
import org.apache.activemq.store.MessageStore;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.ProxyMessageStore;
import org.apache.activemq.store.TopicMessageStore;
import org.apache.activemq.store.TransactionStore;
import org.apache.activemq.usage.SystemUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * �����е���Ϣ�洢���ڴ��У��ǳ־û���������һ�֣���֧���������������ͨ���ڲ���װ��ʹ��֧��
 * @ClassName: MemoryPersistenceAdapter
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��5�� ����10:40:02
 */
// ��������������������Ҫ����
public class MemoryPersistenceAdapter implements PersistenceAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(MemoryPersistenceAdapter.class);
	// ���Խ�TopicMessageStore��MessageStore��װ��ת��Ϊ����֧������Ĵ洢����û���ṩ���set�����������øö���
	MemoryTransactionStore transactionStore;
	ConcurrentMap<ActiveMQDestination, TopicMessageStore> topics = new ConcurrentHashMap<ActiveMQDestination, TopicMessageStore>();
	ConcurrentMap<ActiveMQDestination, MessageStore> queues = new ConcurrentHashMap<ActiveMQDestination, MessageStore>();
	// ������ʲô�ò�֪��
	private boolean useExternalMessageReferences;

	/**
	 * ���ر��������е�����ActiveMQDestination����
	 * @Title: newInstance
	 * @Description: TODO
	 * @param file
	 * @return
	 * @return: MemoryPersistenceAdapter
	 */
	public Set<ActiveMQDestination> getDestinations() {
		Set<ActiveMQDestination> rc = new HashSet<ActiveMQDestination>(queues.size() + topics.size());
		for (Iterator<ActiveMQDestination> iter = queues.keySet().iterator(); iter.hasNext();) {
			rc.add(iter.next());
		}
		for (Iterator<ActiveMQDestination> iter = topics.keySet().iterator(); iter.hasNext();) {
			rc.add(iter.next());
		}
		return rc;
	}
	/**
	 * ����û��ʹ�ã����ǹ��췽��������һ������
	 * @Title: newInstance
	 * @Description: TODO
	 * @param file
	 * @return
	 * @return: MemoryPersistenceAdapter
	 */
	public static MemoryPersistenceAdapter newInstance(File file) {
		return new MemoryPersistenceAdapter();
	}
	@Override
	/**
	 * ����һ��MemoryMessageStore�������ڴ洢��Ϣ�������Ҫ���񣬿��Խ��������װ����ʹ�洢����֧������
	 */
	public MessageStore createQueueMessageStore(ActiveMQQueue destination) throws IOException {
		MessageStore rc = queues.get(destination);
		if (rc == null) {
			// ��ActiveMQQueue����Ϊ��δ����ڴ�洢��
			rc = new MemoryMessageStore(destination);
			if (transactionStore != null) {
				// �Ƿ���Ҫ���������װ����װ�󣬿���֧���������
				rc = transactionStore.proxy(rc);
			}
			queues.put(destination, rc);
		}
		return rc;
	}
	@Override
	/**
	 * ����һ��MemoryTopicMessageStore�������ڴ洢��Ϣ�������Ҫ���񣬿��Խ��������װ����ʹ�洢����֧������
	 * ������ξ�����ʵ�Ķ��л����������
	 */
	public TopicMessageStore createTopicMessageStore(ActiveMQTopic destination) throws IOException {
		TopicMessageStore rc = topics.get(destination);
		if (rc == null) {
			// ����һ����Ϣ�ڴ�洢�������������洢��Ϣ������Ķ��󻹸����¼��Ϣ�������Լ���Ϣ���ܴ�С
			rc = new MemoryTopicMessageStore(destination);
			if (transactionStore != null) {
				// ����洢����֧�����񣬿���ʹ��ʹ�����������洢�����а�װ
				rc = transactionStore.proxy(rc);
			}
			topics.put(destination, rc);
		}
		return rc;
	}
	/**
	 * Cleanup method to remove any state associated with the given destination
	 * @param destination Destination to forget
	 */
	/**
	 * ���洢��ָ��Queue�������Ϣɾ��
	 */
	public void removeQueueMessageStore(ActiveMQQueue destination) {
		queues.remove(destination);
	}
	/**
	 * Cleanup method to remove any state associated with the given destination
	 * @param destination Destination to forget
	 */
	@Override
	/**
	 * ���洢��ָ��Topic�������Ϣɾ��
	 */
	public void removeTopicMessageStore(ActiveMQTopic destination) {
		topics.remove(destination);
	}
	@Override
	/**
	 * ����һ������洢�������Զ���������Ϣ���д洢�Ķ�����а�װ����ʹ��֧������
	 */
	public TransactionStore createTransactionStore() throws IOException {
		if (transactionStore == null) {
			transactionStore = new MemoryTransactionStore(this);
		}
		return transactionStore;
	}
	@Override
	public void beginTransaction(ConnectionContext context) {
	}
	@Override
	public void commitTransaction(ConnectionContext context) {
	}
	@Override
	public void rollbackTransaction(ConnectionContext context) {
	}
	@Override
	public void start() throws Exception {
	}
	@Override
	public void stop() throws Exception {
	}
	@Override
	public long getLastMessageBrokerSequenceId() throws IOException {
		return 0;
	}
	@Override
	/**
	 * ���������е����д洢��Ϣ�Ĵ洢���е���Ϣɾ��
	 */
	public void deleteAllMessages() throws IOException {
		for (Iterator<TopicMessageStore> iter = topics.values().iterator(); iter.hasNext();) {
			// �����ʵ��MemoryMessageStore��������Ҳ������ͻ�õ�null
			MemoryMessageStore store = asMemoryMessageStore(iter.next());
			if (store != null) {
				store.delete();
			}
		}
		for (Iterator<MessageStore> iter = queues.values().iterator(); iter.hasNext();) {
			MemoryMessageStore store = asMemoryMessageStore(iter.next());
			if (store != null) {
				store.delete();
			}
		}
		if (transactionStore != null) {
			transactionStore.delete();
		}
	}
	public boolean isUseExternalMessageReferences() {
		return useExternalMessageReferences;
	}
	public void setUseExternalMessageReferences(boolean useExternalMessageReferences) {
		this.useExternalMessageReferences = useExternalMessageReferences;
	}
	/**
	 * ����MemoryMessageStore���������������Ҳ������ֶ��󣬾ͷ���null
	 * @Title: asMemoryMessageStore
	 * @Description: TODO
	 * @param value
	 * @return
	 * @return: MemoryMessageStore
	 */
	protected MemoryMessageStore asMemoryMessageStore(Object value) {
		if (value instanceof MemoryMessageStore) {
			return (MemoryMessageStore) value;
		}
		if (value instanceof ProxyMessageStore) {
			MessageStore delegate = ((ProxyMessageStore) value).getDelegate();
			if (delegate instanceof MemoryMessageStore) {
				return (MemoryMessageStore) delegate;
			}
		}
		LOG.warn("Expected an instance of MemoryMessageStore but was: " + value);
		return null;
	}
	/**
	 * @param usageManager The UsageManager that is controlling the broker's memory usage.
	 */
	@Override
	/**
	 * SystemUsage���Կ���broker���ڴ�ʹ�ã���������û������
	 */
	public void setUsageManager(SystemUsage usageManager) {
	}
	@Override
	public String toString() {
		return "MemoryPersistenceAdapter";
	}
	@Override
	public void setBrokerName(String brokerName) {
	}
	@Override
	public void setDirectory(File dir) {
	}
	@Override
	public File getDirectory() {
		return null;
	}
	@Override
	public void checkpoint(boolean sync) throws IOException {
	}
	@Override
	public long size() {
		return 0;
	}
	/**
	 * �����ҪTransactionStore����ʱcreateΪtrue�����򴴽�һ��Ĭ�ϵ�
	 * @Title: setCreateTransactionStore
	 * @Description: TODO
	 * @param create
	 * @throws IOException
	 * @return: void
	 */
	public void setCreateTransactionStore(boolean create) throws IOException {
		if (create) {
			createTransactionStore();
		}
	}
	@Override
	/**
	 * ��֪����������
	 */
	public long getLastProducerSequenceId(ProducerId id) {
		// memory map does duplicate suppression
		return -1;
	}
	@Override
	/**
	 * δ��֧�ֵĲ���
	 */
	public JobSchedulerStore createJobSchedulerStore() throws IOException, UnsupportedOperationException {
		// We could eventuall implement an in memory scheduler.
		throw new UnsupportedOperationException();
	}
}
