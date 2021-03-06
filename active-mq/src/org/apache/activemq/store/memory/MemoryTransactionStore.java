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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.command.TransactionId;
import org.apache.activemq.command.XATransactionId;
import org.apache.activemq.store.InlineListenableFuture;
import org.apache.activemq.store.ListenableFuture;
import org.apache.activemq.store.MessageStore;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.ProxyMessageStore;
import org.apache.activemq.store.ProxyTopicMessageStore;
import org.apache.activemq.store.TopicMessageStore;
import org.apache.activemq.store.TransactionRecoveryListener;
import org.apache.activemq.store.TransactionStore;

/**
 * Provides a TransactionStore implementation that can create transaction aware
 * MessageStore objects from non transaction aware MessageStore objects.
 *
 *
 */
/**
 * 可以封装一个持久化适配器，将一个不支持事务操作的适配器转化为可以支持事务操作的适配器
 * @ClassName: MemoryTransactionStore
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月5日 下午10:54:06
 */
public class MemoryTransactionStore implements TransactionStore {
	// 存储还未提交的事务
	protected ConcurrentMap<Object, Tx> inflightTransactions = new ConcurrentHashMap<Object, Tx>();
	// 作用未知
	protected Map<TransactionId, Tx> preparedTransactions = Collections
			.synchronizedMap(new LinkedHashMap<TransactionId, Tx>());
	protected final PersistenceAdapter persistenceAdapter;
	private boolean doingRecover;

	/**
	 * 事务对象，里面含有一个commit方法
	 * @ClassName: Tx
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年8月12日 下午9:18:37
	 */
	public class Tx {
		public ArrayList<AddMessageCommand> messages = new ArrayList<AddMessageCommand>();
		public final ArrayList<RemoveMessageCommand> acks = new ArrayList<RemoveMessageCommand>();

		public void add(AddMessageCommand msg) {
			messages.add(msg);
		}
		public void add(RemoveMessageCommand ack) {
			acks.add(ack);
		}
		/**
		 * 得到当前事务中的所有的消息
		 * @Title: getMessages
		 * @Description: TODO
		 * @return
		 * @return: Message[]
		 */
		public Message[] getMessages() {
			Message rc[] = new Message[messages.size()];
			int count = 0;
			for (Iterator<AddMessageCommand> iter = messages.iterator(); iter.hasNext();) {
				AddMessageCommand cmd = iter.next();
				rc[count++] = cmd.getMessage();
			}
			return rc;
		}
		/**
		 * 得到所有消息的ack对象
		 * @Title: getAcks
		 * @Description: TODO
		 * @return
		 * @return: MessageAck[]
		 */
		public MessageAck[] getAcks() {
			MessageAck rc[] = new MessageAck[acks.size()];
			int count = 0;
			for (Iterator<RemoveMessageCommand> iter = acks.iterator(); iter.hasNext();) {
				RemoveMessageCommand cmd = iter.next();
				rc[count++] = cmd.getMessageAck();
			}
			return rc;
		}
		/**
		 * @throws IOException
		 */
		public void commit() throws IOException {
			// 创建一个连接上下文
			ConnectionContext ctx = new ConnectionContext();
			// 对于MemoryPersistenceAdapter来说，下面是一个空实现
			persistenceAdapter.beginTransaction(ctx);
			try {
				// Do all the message adds.
				// 执行所有未提交的事务，增加消息
				for (Iterator<AddMessageCommand> iter = messages.iterator(); iter.hasNext();) {
					AddMessageCommand cmd = iter.next();
					cmd.run(ctx);
				}
				// And removes..
				// 将已经ack和需要删除的事务执行，删除消息
				for (Iterator<RemoveMessageCommand> iter = acks.iterator(); iter.hasNext();) {
					RemoveMessageCommand cmd = iter.next();
					cmd.run(ctx);
				}
			} catch (IOException e) {
				// 内存操作不会失败
				persistenceAdapter.rollbackTransaction(ctx);
				throw e;
			}
			persistenceAdapter.commitTransaction(ctx);
		}
	}

	public interface AddMessageCommand {
		Message getMessage();
		MessageStore getMessageStore();
		void run(ConnectionContext context) throws IOException;
		void setMessageStore(MessageStore messageStore);
	}

	public interface RemoveMessageCommand {
		MessageAck getMessageAck();
		void run(ConnectionContext context) throws IOException;
		MessageStore getMessageStore();
	}

	public MemoryTransactionStore(PersistenceAdapter persistenceAdapter) {
		this.persistenceAdapter = persistenceAdapter;
	}
	/**
	 * 将入参封装成一个有事务的MessageStore对象
	 * @Title: proxy
	 * @Description: TODO
	 * @param messageStore
	 * @return
	 * @return: MessageStore
	 */
	public MessageStore proxy(MessageStore messageStore) {
		// 创建一个代理对象
		ProxyMessageStore proxyMessageStore = new ProxyMessageStore(messageStore) {
			@Override
			public void addMessage(ConnectionContext context, final Message send) throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), send);
			}
			@Override
			public void addMessage(ConnectionContext context, final Message send, boolean canOptimize)
					throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), send);
			}
			@Override
			public ListenableFuture<Object> asyncAddQueueMessage(ConnectionContext context, Message message)
					throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), message);
				return new InlineListenableFuture();
			}
			@Override
			public ListenableFuture<Object> asyncAddQueueMessage(ConnectionContext context, Message message,
					boolean canoptimize) throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), message);
				return new InlineListenableFuture();
			}
			@Override
			public void removeMessage(ConnectionContext context, final MessageAck ack) throws IOException {
				MemoryTransactionStore.this.removeMessage(getDelegate(), ack);
			}
			@Override
			public void removeAsyncMessage(ConnectionContext context, MessageAck ack) throws IOException {
				MemoryTransactionStore.this.removeMessage(getDelegate(), ack);
			}
		};
		onProxyQueueStore(proxyMessageStore);// 空实现
		return proxyMessageStore;
	}
	protected void onProxyQueueStore(ProxyMessageStore proxyMessageStore) {
	}
	/**
	 * 与上面的方法完全一样
	 * @Title: proxy
	 * @Description: TODO
	 * @param messageStore
	 * @return
	 * @return: TopicMessageStore
	 */
	public TopicMessageStore proxy(TopicMessageStore messageStore) {
		ProxyTopicMessageStore proxyTopicMessageStore = new ProxyTopicMessageStore(messageStore) {
			@Override
			public void addMessage(ConnectionContext context, final Message send) throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), send);
			}
			@Override
			public void addMessage(ConnectionContext context, final Message send, boolean canOptimize)
					throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), send);
			}
			@Override
			public ListenableFuture<Object> asyncAddTopicMessage(ConnectionContext context, Message message)
					throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), message);
				return new InlineListenableFuture();
			}
			@Override
			public ListenableFuture<Object> asyncAddTopicMessage(ConnectionContext context, Message message,
					boolean canOptimize) throws IOException {
				MemoryTransactionStore.this.addMessage(context, getDelegate(), message);
				return new InlineListenableFuture();
			}
			@Override
			public void removeMessage(ConnectionContext context, final MessageAck ack) throws IOException {
				MemoryTransactionStore.this.removeMessage(getDelegate(), ack);
			}
			@Override
			public void removeAsyncMessage(ConnectionContext context, MessageAck ack) throws IOException {
				MemoryTransactionStore.this.removeMessage(getDelegate(), ack);
			}
			@Override
			public void acknowledge(ConnectionContext context, String clientId, String subscriptionName,
					MessageId messageId, MessageAck ack) throws IOException {
				MemoryTransactionStore.this.acknowledge((TopicMessageStore) getDelegate(), clientId, subscriptionName,
						messageId, ack);
			}
		};
		onProxyTopicStore(proxyTopicMessageStore);
		return proxyTopicMessageStore;
	}
	protected void onProxyTopicStore(ProxyTopicMessageStore proxyTopicMessageStore) {
	}
	/**
	 * @see org.apache.activemq.store.TransactionStore#prepare(TransactionId)
	 */
	@Override
	public void prepare(TransactionId txid) throws IOException {
		Tx tx = inflightTransactions.remove(txid);
		if (tx == null) {
			return;
		}
		preparedTransactions.put(txid, tx);
	}
	public Tx getTx(Object txid) {
		Tx tx = inflightTransactions.get(txid);
		if (tx == null) {
			tx = new Tx();
			inflightTransactions.put(txid, tx);
		}
		return tx;
	}
	public Tx getPreparedTx(TransactionId txid) {
		Tx tx = preparedTransactions.get(txid);
		if (tx == null) {
			tx = new Tx();
			preparedTransactions.put(txid, tx);
		}
		return tx;
	}
	@Override
	public void commit(TransactionId txid, boolean wasPrepared, Runnable preCommit, Runnable postCommit)
			throws IOException {
		if (preCommit != null) {
			preCommit.run();
		}
		Tx tx;
		if (wasPrepared) {
			tx = preparedTransactions.remove(txid);
		} else {
			tx = inflightTransactions.remove(txid);
		}
		if (tx != null) {
			tx.commit();
		}
		if (postCommit != null) {
			postCommit.run();
		}
	}
	/**
	 * @see org.apache.activemq.store.TransactionStore#rollback(TransactionId)
	 */
	@Override
	/**
	 * 事务回滚就是将事务id从两个属性中删除
	 */
	public void rollback(TransactionId txid) throws IOException {
		preparedTransactions.remove(txid);
		inflightTransactions.remove(txid);
	}
	@Override
	public void start() throws Exception {
	}
	@Override
	public void stop() throws Exception {
	}
	@Override
	/**
	 * 执行事务恢复
	 */
	public synchronized void recover(TransactionRecoveryListener listener) throws IOException {
		// All the inflight transactions get rolled back..
		inflightTransactions.clear();
		this.doingRecover = true;
		try {
			for (Iterator<TransactionId> iter = preparedTransactions.keySet().iterator(); iter.hasNext();) {
				Object txid = iter.next();
				Tx tx = preparedTransactions.get(txid);
				listener.recover((XATransactionId) txid, tx.getMessages(), tx.getAcks());
				onRecovered(tx);// 空实现
			}
		} finally {
			this.doingRecover = false;
		}
	}
	protected void onRecovered(Tx tx) {
	}
	/**
	 * 添加消息，首先根据消息的txid得到对应的Tx对象，这个Tx对象位于inflightTransactions属性中
	 * @Title: addMessage
	 * @Description: TODO
	 * @param context
	 * @param destination
	 * @param message
	 * @throws IOException
	 * @return: void
	 */
	void addMessage(final ConnectionContext context, final MessageStore destination, final Message message)
			throws IOException {
		// 事务在做恢复期间，不对外服务
		if (doingRecover) {
			// 这个地方应该有异常，消息没有存储到内存中，而调用者并不知道，这会有问题
			return;
		}
		if (message.getTransactionId() != null) {
			// 进行事务操作
			Tx tx = getTx(message.getTransactionId());
			tx.add(new AddMessageCommand() {
				MessageStore messageStore = destination;

				@Override
				public Message getMessage() {
					return message;
				}
				@Override
				public MessageStore getMessageStore() {
					return destination;
				}
				@Override
				// 将消息直接放到存储器中
				public void run(ConnectionContext ctx) throws IOException {
					destination.addMessage(ctx, message);
				}
				@Override
				public void setMessageStore(MessageStore messageStore) {
					this.messageStore = messageStore;
				}
			});
		} else {
			// 没有事务
			destination.addMessage(context, message);
		}
	}
	/**
	 * @param ack
	 * @throws IOException
	 */
	/**
	 * 删除的消息必须有ack对象
	 * @Title: removeMessage
	 * @Description: TODO
	 * @param destination
	 * @param ack
	 * @throws IOException
	 * @return: void
	 */
	final void removeMessage(final MessageStore destination, final MessageAck ack) throws IOException {
		if (doingRecover) {
			return;
		}
		if (ack.isInTransaction()) {
			Tx tx = getTx(ack.getTransactionId());
			tx.add(new RemoveMessageCommand() {
				@Override
				public MessageAck getMessageAck() {
					return ack;
				}
				@Override
				public void run(ConnectionContext ctx) throws IOException {
					destination.removeMessage(ctx, ack);
				}
				@Override
				public MessageStore getMessageStore() {
					return destination;
				}
			});
		} else {
			destination.removeMessage(null, ack);
		}
	}
	/**
	 * 为Topic使用，将确认的消息删除
	 * @Title: acknowledge
	 * @Description: TODO
	 * @param destination
	 * @param clientId
	 * @param subscriptionName
	 * @param messageId
	 * @param ack
	 * @throws IOException
	 * @return: void
	 */
	public void acknowledge(final TopicMessageStore destination, final String clientId, final String subscriptionName,
			final MessageId messageId, final MessageAck ack) throws IOException {
		if (doingRecover) {
			return;
		}
		if (ack.isInTransaction()) {
			Tx tx = getTx(ack.getTransactionId());
			tx.add(new RemoveMessageCommand() {
				@Override
				public MessageAck getMessageAck() {
					return ack;
				}
				@Override
				public void run(ConnectionContext ctx) throws IOException {
					destination.acknowledge(ctx, clientId, subscriptionName, messageId, ack);
				}
				@Override
				public MessageStore getMessageStore() {
					return destination;
				}
			});
		} else {
			destination.acknowledge(null, clientId, subscriptionName, messageId, ack);
		}
	}
	/**
	 * 清空所有的记录
	 * @Title: delete
	 * @Description: TODO
	 * @return: void
	 */
	public void delete() {
		inflightTransactions.clear();
		preparedTransactions.clear();
		doingRecover = false;
	}
}
