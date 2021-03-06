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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.command.SubscriptionInfo;
import org.apache.activemq.store.MessageRecoveryListener;
import org.apache.activemq.store.MessageStoreStatistics;
import org.apache.activemq.store.TopicMessageStore;
import org.apache.activemq.util.LRUCache;
import org.apache.activemq.util.SubscriptionKey;

public class MemoryTopicMessageStore extends MemoryMessageStore implements TopicMessageStore {
	// 存储订阅者的消息信息
	private Map<SubscriptionKey, SubscriptionInfo> subscriberDatabase;
	// 就是一个经过同步处理的Map对象，存储尚未确认的消息，每一个订阅者对应一个消息存储器
	private Map<SubscriptionKey, MemoryTopicSub> topicSubMap;
	// 就是一个经过同步处理的Map对象，下面这个属性没有什么用途
	private final Map<MessageId, Message> originalMessageTable;

	public MemoryTopicMessageStore(ActiveMQDestination destination) {
		this(destination, new MemoryTopicMessageStoreLRUCache(100, 100, 0.75f, false), makeSubscriptionInfoMap());
		// Set the messageStoreStatistics after the super class is initialized so that the stats can
		// be
		// properly updated on cache eviction
		MemoryTopicMessageStoreLRUCache cache = (MemoryTopicMessageStoreLRUCache) originalMessageTable;
		cache.setMessageStoreStatistics(messageStoreStatistics);
	}
	public MemoryTopicMessageStore(ActiveMQDestination destination, Map<MessageId, Message> messageTable,
			Map<SubscriptionKey, SubscriptionInfo> subscriberDatabase) {
		super(destination, messageTable);
		this.subscriberDatabase = subscriberDatabase;
		this.topicSubMap = makeSubMap();
		// this is only necessary so that messageStoreStatistics can be set if necessary
		// We need the original reference since messageTable is wrapped in a synchronized map in the
		// parent class
		this.originalMessageTable = messageTable;
	}
	protected static Map<SubscriptionKey, SubscriptionInfo> makeSubscriptionInfoMap() {
		return Collections.synchronizedMap(new HashMap<SubscriptionKey, SubscriptionInfo>());
	}
	/**
	 * 生成一个同步处理过的Map对象
	 * @Title: makeSubMap
	 * @Description: TODO
	 * @return
	 * @return: Map<SubscriptionKey,MemoryTopicSub>
	 */
	protected static Map<SubscriptionKey, MemoryTopicSub> makeSubMap() {
		return Collections.synchronizedMap(new HashMap<SubscriptionKey, MemoryTopicSub>());
	}
	@Override
	/**
	 * 添加消息，将消息存储到每一个订阅者的存储器中
	 */
	public synchronized void addMessage(ConnectionContext context, Message message) throws IOException {
		super.addMessage(context, message);
		for (Iterator<MemoryTopicSub> i = topicSubMap.values().iterator(); i.hasNext();) {
			MemoryTopicSub sub = i.next();
			sub.addMessage(message.getMessageId(), message);
		}
	}
	@Override
	/**
	 * 将得到确认的消息从topicSubMap中删除，从这里可以看出已经确认的消息会被删除
	 */
	public synchronized void acknowledge(ConnectionContext context, String clientId, String subscriptionName,
			MessageId messageId, MessageAck ack) throws IOException {
		SubscriptionKey key = new SubscriptionKey(clientId, subscriptionName);
		MemoryTopicSub sub = topicSubMap.get(key);
		if (sub != null) {
			sub.removeMessage(messageId);
		}
	}
	@Override
	/**
	 * 查找订阅者的详细信息
	 */
	public synchronized SubscriptionInfo lookupSubscription(String clientId, String subscriptionName)
			throws IOException {
		return subscriberDatabase.get(new SubscriptionKey(clientId, subscriptionName));
	}
	@Override
	/**
	 * 添加订阅者，入参retroactive表示该订阅者是否消息可追溯，如果是可以追溯的，那么就将目前在内存中存储的所有消息放到订阅者对应的消息存储器中
	 */
	public synchronized void addSubscription(SubscriptionInfo info, boolean retroactive) throws IOException {
		SubscriptionKey key = new SubscriptionKey(info);
		MemoryTopicSub sub = new MemoryTopicSub();
		topicSubMap.put(key, sub);
		if (retroactive) {
			for (Iterator i = messageTable.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Entry) i.next();
				sub.addMessage((MessageId) entry.getKey(), (Message) entry.getValue());
			}
		}
		subscriberDatabase.put(key, info);
	}
	@Override
	public synchronized void deleteSubscription(String clientId, String subscriptionName) {
		org.apache.activemq.util.SubscriptionKey key = new SubscriptionKey(clientId, subscriptionName);
		subscriberDatabase.remove(key);
		topicSubMap.remove(key);
	}
	@Override
	/**
	 * 恢复每一个消息
	 */
	public synchronized void recoverSubscription(String clientId, String subscriptionName,
			MessageRecoveryListener listener) throws Exception {
		MemoryTopicSub sub = topicSubMap.get(new SubscriptionKey(clientId, subscriptionName));
		if (sub != null) {
			sub.recoverSubscription(listener);
		}
	}
	@Override
	public synchronized void delete() {
		super.delete();
		subscriberDatabase.clear();
		topicSubMap.clear();
	}
	@Override
	public SubscriptionInfo[] getAllSubscriptions() throws IOException {
		return subscriberDatabase.values().toArray(new SubscriptionInfo[subscriberDatabase.size()]);
	}
	@Override
	public synchronized int getMessageCount(String clientId, String subscriberName) throws IOException {
		int result = 0;
		MemoryTopicSub sub = topicSubMap.get(new SubscriptionKey(clientId, subscriberName));
		if (sub != null) {
			result = sub.size();
		}
		return result;
	}
	@Override
	public synchronized long getMessageSize(String clientId, String subscriberName) throws IOException {
		long result = 0;
		MemoryTopicSub sub = topicSubMap.get(new SubscriptionKey(clientId, subscriberName));
		if (sub != null) {
			result = sub.messageSize();
		}
		return result;
	}
	@Override
	/**
	 * 恢复指定订阅者中所有未被恢复的消息
	 */
	public synchronized void recoverNextMessages(String clientId, String subscriptionName, int maxReturned,
			MessageRecoveryListener listener) throws Exception {
		MemoryTopicSub sub = this.topicSubMap.get(new SubscriptionKey(clientId, subscriptionName));
		if (sub != null) {
			sub.recoverNextMessages(maxReturned, listener);
		}
	}
	@Override
	public void resetBatching(String clientId, String subscriptionName) {
		MemoryTopicSub sub = topicSubMap.get(new SubscriptionKey(clientId, subscriptionName));
		if (sub != null) {
			sub.resetBatching();
		}
	}

	/**
	 * Since we initialize the store with a LRUCache in some cases, we need to account for cache
	 * evictions when computing the message store statistics.
	 */
	/**
	 * 下面这个类没有用途
	 * @ClassName: MemoryTopicMessageStoreLRUCache
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年8月12日 下午8:26:13
	 */
	private static class MemoryTopicMessageStoreLRUCache extends LRUCache<MessageId, Message> {
		private static final long serialVersionUID = -342098639681884413L;
		private MessageStoreStatistics messageStoreStatistics;

		public MemoryTopicMessageStoreLRUCache(int initialCapacity, int maximumCacheSize, float loadFactor,
				boolean accessOrder) {
			super(initialCapacity, maximumCacheSize, loadFactor, accessOrder);
		}
		public void setMessageStoreStatistics(MessageStoreStatistics messageStoreStatistics) {
			this.messageStoreStatistics = messageStoreStatistics;
		}
		@Override
		/**
		 * 将入参从本对象的统计器中删除
		 */
		protected void onCacheEviction(Map.Entry<MessageId, Message> eldest) {
			decMessageStoreStatistics(messageStoreStatistics, eldest.getValue());
		}
	}
}
