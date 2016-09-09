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
 * ���Է�װһ���־û�����������һ����֧�����������������ת��Ϊ����֧�����������������
 * @ClassName: MemoryTransactionStore
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��5�� ����10:54:06
 */
public class MemoryTransactionStore implements TransactionStore {
	// �洢��δ�ύ������
	protected ConcurrentMap<Object, Tx> inflightTransactions = new ConcurrentHashMap<Object, Tx>();
	// ����δ֪
	protected Map<TransactionId, Tx> preparedTransactions = Collections
			.synchronizedMap(new LinkedHashMap<TransactionId, Tx>());
	protected final PersistenceAdapter persistenceAdapter;
	private boolean doingRecover;

	/**
	 * ����������溬��һ��commit����
	 * @ClassName: Tx
	 * @Description: TODO
	 * @author: ����
	 * @date: 2016��8��12�� ����9:18:37
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
		 * �õ���ǰ�����е����е���Ϣ
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
		 * �õ�������Ϣ��ack����
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
			// ����һ������������
			ConnectionContext ctx = new ConnectionContext();
			// ����MemoryPersistenceAdapter��˵��������һ����ʵ��
			persistenceAdapter.beginTransaction(ctx);
			try {
				// Do all the message adds.
				// ִ������δ�ύ������������Ϣ
				for (Iterator<AddMessageCommand> iter = messages.iterator(); iter.hasNext();) {
					AddMessageCommand cmd = iter.next();
					cmd.run(ctx);
				}
				// And removes..
				// ���Ѿ�ack����Ҫɾ��������ִ�У�ɾ����Ϣ
				for (Iterator<RemoveMessageCommand> iter = acks.iterator(); iter.hasNext();) {
					RemoveMessageCommand cmd = iter.next();
					cmd.run(ctx);
				}
			} catch (IOException e) {
				// �ڴ��������ʧ��
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
	 * ����η�װ��һ���������MessageStore����
	 * @Title: proxy
	 * @Description: TODO
	 * @param messageStore
	 * @return
	 * @return: MessageStore
	 */
	public MessageStore proxy(MessageStore messageStore) {
		// ����һ����������
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
		onProxyQueueStore(proxyMessageStore);// ��ʵ��
		return proxyMessageStore;
	}
	protected void onProxyQueueStore(ProxyMessageStore proxyMessageStore) {
	}
	/**
	 * ������ķ�����ȫһ��
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
	 * ����ع����ǽ�����id������������ɾ��
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
	 * ִ������ָ�
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
				onRecovered(tx);// ��ʵ��
			}
		} finally {
			this.doingRecover = false;
		}
	}
	protected void onRecovered(Tx tx) {
	}
	/**
	 * ������Ϣ�����ȸ�����Ϣ��txid�õ���Ӧ��Tx�������Tx����λ��inflightTransactions������
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
		// ���������ָ��ڼ䣬���������
		if (doingRecover) {
			// ����ط�Ӧ�����쳣����Ϣû�д洢���ڴ��У��������߲���֪�������������
			return;
		}
		if (message.getTransactionId() != null) {
			// �����������
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
				// ����Ϣֱ�ӷŵ��洢����
				public void run(ConnectionContext ctx) throws IOException {
					destination.addMessage(ctx, message);
				}
				@Override
				public void setMessageStore(MessageStore messageStore) {
					this.messageStore = messageStore;
				}
			});
		} else {
			// û������
			destination.addMessage(context, message);
		}
	}
	/**
	 * @param ack
	 * @throws IOException
	 */
	/**
	 * ɾ������Ϣ������ack����
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
	 * ΪTopicʹ�ã���ȷ�ϵ���Ϣɾ��
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
	 * ������еļ�¼
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