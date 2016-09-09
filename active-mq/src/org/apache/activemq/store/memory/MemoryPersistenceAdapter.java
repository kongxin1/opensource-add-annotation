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
 * 将所有的消息存储在内存中，是持久化适配器的一种，不支持事务操作，不过通过内部封装，使其支持
 * @ClassName: MemoryPersistenceAdapter
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月5日 下午10:40:02
 */
// 本对象中有三个对象需要解析
public class MemoryPersistenceAdapter implements PersistenceAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(MemoryPersistenceAdapter.class);
	// 可以将TopicMessageStore和MessageStore封装，转化为可以支持事务的存储器，没有提供相关set方法进行设置该对象
	MemoryTransactionStore transactionStore;
	ConcurrentMap<ActiveMQDestination, TopicMessageStore> topics = new ConcurrentHashMap<ActiveMQDestination, TopicMessageStore>();
	ConcurrentMap<ActiveMQDestination, MessageStore> queues = new ConcurrentHashMap<ActiveMQDestination, MessageStore>();
	// 具体做什么用不知道
	private boolean useExternalMessageReferences;

	/**
	 * 返回本适配器中的所有ActiveMQDestination对象
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
	 * 参数没有使用，就是构造方法创建出一个对象
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
	 * 创建一个MemoryMessageStore对象，用于存储消息，如果需要事务，可以进行事务包装，以使存储对象支持事务
	 */
	public MessageStore createQueueMessageStore(ActiveMQQueue destination) throws IOException {
		MessageStore rc = queues.get(destination);
		if (rc == null) {
			// 以ActiveMQQueue对象为入参创建内存存储器
			rc = new MemoryMessageStore(destination);
			if (transactionStore != null) {
				// 是否需要进行事务包装，包装后，可以支持事务操作
				rc = transactionStore.proxy(rc);
			}
			queues.put(destination, rc);
		}
		return rc;
	}
	@Override
	/**
	 * 创建一个MemoryTopicMessageStore对象，用于存储消息，如果需要事务，可以进行事务包装，以使存储对象支持事务
	 * 怀疑入参就是真实的队列或者主题对象
	 */
	public TopicMessageStore createTopicMessageStore(ActiveMQTopic destination) throws IOException {
		TopicMessageStore rc = topics.get(destination);
		if (rc == null) {
			// 创建一个消息内存存储器，可以用来存储消息，下面的对象还负责记录消息个数，以及消息的总大小
			rc = new MemoryTopicMessageStore(destination);
			if (transactionStore != null) {
				// 如果存储器不支持事务，可以使用使用下面的事务存储器进行包装
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
	 * 将存储在指定Queue对象的消息删除
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
	 * 将存储在指定Topic对象的消息删除
	 */
	public void removeTopicMessageStore(ActiveMQTopic destination) {
		topics.remove(destination);
	}
	@Override
	/**
	 * 创建一个事务存储器，可以对真正对消息就行存储的对象进行包装，以使其支持事务
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
	 * 将本对象中的所有存储消息的存储器中的消息删除
	 */
	public void deleteAllMessages() throws IOException {
		for (Iterator<TopicMessageStore> iter = topics.values().iterator(); iter.hasNext();) {
			// 获得真实的MemoryMessageStore对象，如果找不到，就会得到null
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
	 * 返回MemoryMessageStore对象，如果从入参中找不到这种对象，就返回null
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
	 * SystemUsage可以控制broker的内存使用，但是这里没有设置
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
	 * 如果需要TransactionStore（此时create为true），则创建一个默认的
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
	 * 不知道具体作用
	 */
	public long getLastProducerSequenceId(ProducerId id) {
		// memory map does duplicate suppression
		return -1;
	}
	@Override
	/**
	 * 未获支持的操作
	 */
	public JobSchedulerStore createJobSchedulerStore() throws IOException, UnsupportedOperationException {
		// We could eventuall implement an in memory scheduler.
		throw new UnsupportedOperationException();
	}
}
