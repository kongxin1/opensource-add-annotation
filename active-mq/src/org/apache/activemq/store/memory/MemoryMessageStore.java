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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.store.AbstractMessageStore;
import org.apache.activemq.store.IndexListener;
import org.apache.activemq.store.MessageRecoveryListener;
import org.apache.activemq.store.MessageStoreStatistics;

/**
 * An implementation of {@link org.apache.activemq.store.MessageStore} which
 * uses a
 *
 *
 */
/**
 * 用于在内存中存储数据的存储器，一个topic或者一个queue的所有消息都会存储到本对象中
 * @ClassName: MemoryMessageStore
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月5日 下午11:24:14
 */
public class MemoryMessageStore extends AbstractMessageStore {
	// MessageId在Message对象中，下面的对象就是存储消息的对象，所有的消息都在下面的对象中存储
	protected final Map<MessageId, Message> messageTable;
	// 记录下上述Map对象中经过恢复操作的最后一个消息id
	protected MessageId lastBatchId;
	protected long sequenceId;

	public MemoryMessageStore(ActiveMQDestination destination) {
		this(destination, new LinkedHashMap<MessageId, Message>());
	}
	public MemoryMessageStore(ActiveMQDestination destination, Map<MessageId, Message> messageTable) {
		// 队列或者主题的对象记录在父类中
		super(destination);
		// map必须是经过同步处理的
		this.messageTable = Collections.synchronizedMap(messageTable);
	}
	@Override
	/**
	 * 首先将消息添加到Map对象中，然后修改计数器，同时增加消息的引用次数，还有一些其他修改
	 */
	public synchronized void addMessage(ConnectionContext context, Message message) throws IOException {
		synchronized (messageTable) {
			messageTable.put(message.getMessageId(), message);
			incMessageStoreStatistics(getMessageStoreStatistics(), message);
		}
		// 增加消息的引用次数
		message.incrementReferenceCount();
		// 不知道作用
		message.getMessageId().setFutureOrSequenceLong(sequenceId++);
		if (indexListener != null) {
			indexListener.onAdd(new IndexListener.MessageContext(context, message, null));
		}
	}
	@Override
	/**
	 * 根据入参得到具体的消息对象
	 */
	public Message getMessage(MessageId identity) throws IOException {
		return messageTable.get(identity);
	}
	// public String getMessageReference(MessageId identity) throws IOException{
	// return (String)messageTable.get(identity);
	// }
	@Override
	/**
	 * 将已经得到ACK确认的消息删除
	 */
	public void removeMessage(ConnectionContext context, MessageAck ack) throws IOException {
		removeMessage(ack.getLastMessageId());
	}
	/**
	 * 与增加消息相反。将消息从map对象中删除，然后减小消息引用次数，减小计数器
	 * @Title: removeMessage
	 * @Description: TODO
	 * @param msgId
	 * @throws IOException
	 * @return: void
	 */
	public void removeMessage(MessageId msgId) throws IOException {
		synchronized (messageTable) {
			Message removed = messageTable.remove(msgId);
			if (removed != null) {
				removed.decrementReferenceCount();
				decMessageStoreStatistics(getMessageStoreStatistics(), removed);
			}
			if ((lastBatchId != null && lastBatchId.equals(msgId)) || messageTable.isEmpty()) {
				lastBatchId = null;
			}
		}
	}
	@Override
	/**
	 * 使用入参恢复本对象中的所有消息
	 */
	public void recover(MessageRecoveryListener listener) throws Exception {
		// the message table is a synchronizedMap - so just have to synchronize
		// here
		synchronized (messageTable) {
			for (Iterator<Message> iter = messageTable.values().iterator(); iter.hasNext();) {
				Object msg = iter.next();
				// if中的内容不会运行，因为value的值是Message对象
				if (msg.getClass() == MessageId.class) {
					listener.recoverMessageReference((MessageId) msg);
				} else {
					// 不知道具体作用
					listener.recoverMessage((Message) msg);
				}
			}
		}
	}
	@Override
	/**
	 * 删除所有的消息并重置计数器，入参没有什么用处
	 */
	public void removeAllMessages(ConnectionContext context) throws IOException {
		synchronized (messageTable) {
			messageTable.clear();
			getMessageStoreStatistics().reset();
		}
	}
	/**
	 * 清空存储的消息，并重置计数器
	 * @Title: delete
	 * @Description: TODO
	 * @return: void
	 */
	public void delete() {
		synchronized (messageTable) {
			messageTable.clear();
			getMessageStoreStatistics().reset();
		}
	}
	@Override
	/**
	 * 使用入参listener恢复消息，只恢复队列中还未经过恢复操作的消息
	 */
	public void recoverNextMessages(int maxReturned, MessageRecoveryListener listener) throws Exception {
		synchronized (messageTable) {
			boolean pastLackBatch = lastBatchId == null;
			// count变量没什么用
			int count = 0;
			for (Iterator iter = messageTable.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Entry) iter.next();
				if (pastLackBatch) {
					count++;
					Object msg = entry.getValue();
					lastBatchId = (MessageId) entry.getKey();
					if (msg.getClass() == MessageId.class) {
						listener.recoverMessageReference((MessageId) msg);
					} else {
						listener.recoverMessage((Message) msg);
					}
				} else {
					pastLackBatch = entry.getKey().equals(lastBatchId);
				}
			}
		}
	}
	@Override
	public void resetBatching() {
		lastBatchId = null;
	}
	@Override
	public void setBatch(MessageId messageId) {
		lastBatchId = messageId;
	}
	@Override
	/**
	 * 更新消息，同时修改计数器
	 */
	public void updateMessage(Message message) {
		synchronized (messageTable) {
			Message original = messageTable.get(message.getMessageId());
			// if can't be found then increment count, else remove old size
			if (original == null) {
				getMessageStoreStatistics().getMessageCount().increment();
			} else {
				getMessageStoreStatistics().getMessageSize().addSize(-original.getSize());
			}
			messageTable.put(message.getMessageId(), message);
			getMessageStoreStatistics().getMessageSize().addSize(message.getSize());
		}
	}
	@Override
	/**
	 * 重新设置本对象中的所有计数器
	 */
	public void recoverMessageStoreStatistics() throws IOException {
		synchronized (messageTable) {
			long size = 0;
			// count没有改变，程序有bug
			int count = 0;
			for (Iterator<Message> iter = messageTable.values().iterator(); iter.hasNext();) {
				Message msg = iter.next();
				size += msg.getSize();
			}
			getMessageStoreStatistics().reset();
			getMessageStoreStatistics().getMessageCount().setCount(count);
			getMessageStoreStatistics().getMessageSize().setTotalSize(size);
		}
	}
	/**
	 * 本对象中的计数器增加，个数计数器加一，空间大小计数器增加消息的大小
	 * @Title: incMessageStoreStatistics
	 * @Description: TODO
	 * @param stats
	 * @param message
	 * @return: void
	 */
	protected static final void incMessageStoreStatistics(final MessageStoreStatistics stats, final Message message) {
		if (stats != null && message != null) {
			stats.getMessageCount().increment();
			stats.getMessageSize().addSize(message.getSize());
		}
	}
	/**
	 * 本对象中的计数器减小，个数计数器减一，空间大小计数器减少消息的大小，这个在LRU中会使用
	 * @Title: incMessageStoreStatistics
	 * @Description: TODO
	 * @param stats
	 * @param message
	 * @return: void
	 */
	protected static final void decMessageStoreStatistics(final MessageStoreStatistics stats, final Message message) {
		if (stats != null && message != null) {
			stats.getMessageCount().decrement();
			stats.getMessageSize().addSize(-message.getSize());
		}
	}
}
