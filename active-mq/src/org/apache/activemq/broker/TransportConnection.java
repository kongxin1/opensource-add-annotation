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
package org.apache.activemq.broker;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.transaction.xa.XAResource;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.region.ConnectionStatistics;
import org.apache.activemq.broker.region.RegionBroker;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.BrokerInfo;
import org.apache.activemq.command.Command;
import org.apache.activemq.command.CommandTypes;
import org.apache.activemq.command.ConnectionControl;
import org.apache.activemq.command.ConnectionError;
import org.apache.activemq.command.ConnectionId;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerControl;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.ControlCommand;
import org.apache.activemq.command.DataArrayResponse;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.ExceptionResponse;
import org.apache.activemq.command.FlushCommand;
import org.apache.activemq.command.IntegerResponse;
import org.apache.activemq.command.KeepAliveInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.command.MessageDispatchNotification;
import org.apache.activemq.command.MessagePull;
import org.apache.activemq.command.ProducerAck;
import org.apache.activemq.command.ProducerId;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.activemq.command.RemoveSubscriptionInfo;
import org.apache.activemq.command.Response;
import org.apache.activemq.command.SessionId;
import org.apache.activemq.command.SessionInfo;
import org.apache.activemq.command.ShutdownInfo;
import org.apache.activemq.command.TransactionId;
import org.apache.activemq.command.TransactionInfo;
import org.apache.activemq.command.WireFormatInfo;
import org.apache.activemq.network.DemandForwardingBridge;
import org.apache.activemq.network.MBeanNetworkListener;
import org.apache.activemq.network.NetworkBridgeConfiguration;
import org.apache.activemq.network.NetworkBridgeFactory;
import org.apache.activemq.security.MessageAuthorizationPolicy;
import org.apache.activemq.state.CommandVisitor;
import org.apache.activemq.state.ConnectionState;
import org.apache.activemq.state.ConsumerState;
import org.apache.activemq.state.ProducerState;
import org.apache.activemq.state.SessionState;
import org.apache.activemq.state.TransactionState;
import org.apache.activemq.thread.Task;
import org.apache.activemq.thread.TaskRunner;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.activemq.transaction.Transaction;
import org.apache.activemq.transport.DefaultTransportListener;
import org.apache.activemq.transport.ResponseCorrelator;
import org.apache.activemq.transport.TransmitCallback;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportDisposedIOException;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.MarshallingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * ��������Ǵ�����Ϣ�ĺ����࣬������process��ͷ�������Ǵ�����Ϣ�ķ���������һ���ͻ������ӷ�����ʱ���ͻᴴ��һ��������������������TransportConnector���д�����
 * @ClassName: TransportConnection
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��22�� ����9:32:19
 */
public class TransportConnection implements Connection, Task, CommandVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(TransportConnection.class);
	private static final Logger TRANSPORTLOG = LoggerFactory.getLogger(TransportConnection.class.getName()
			+ ".Transport");
	private static final Logger SERVICELOG = LoggerFactory.getLogger(TransportConnection.class.getName() + ".Service");
	// Keeps track of the broker and connector that created this connection.
	protected final Broker broker;
	// �������������лᱣ���з������˵�������ͻ��˽��������ӣ�ÿһ�����Ӿ���һ��transportconnection����
	protected final TransportConnector connector;
	// Keeps track of the state of the connections.
	// protected final ConcurrentHashMap localConnectionStates=new
	// ConcurrentHashMap();
	protected final Map<ConnectionId, ConnectionState> brokerConnectionStates;
	// The broker and wireformat info that was exchanged.
	protected BrokerInfo brokerInfo;
	protected final List<Command> dispatchQueue = new LinkedList<Command>();
	protected TaskRunner taskRunner;
	protected final AtomicReference<Throwable> transportException = new AtomicReference<Throwable>();
	protected AtomicBoolean dispatchStopped = new AtomicBoolean(false);
	private final Transport transport;// ����TCP����������Գ���һ����װ��TcpTransport����Ķ���
	private MessageAuthorizationPolicy messageAuthorizationPolicy;
	private WireFormatInfo wireFormatInfo;
	// Used to do async dispatch.. this should perhaps be pushed down into the
	// transport layer..
	private boolean inServiceException;
	private final ConnectionStatistics statistics = new ConnectionStatistics();
	private boolean manageable;
	private boolean slow;
	private boolean markedCandidate;
	private boolean blockedCandidate;
	private boolean blocked;
	private boolean connected;
	private boolean active;
	private boolean starting;
	// ��ʾ���񼴽�����
	private boolean pendingStop;
	private long timeStamp;
	private final AtomicBoolean stopping = new AtomicBoolean(false);
	private final CountDownLatch stopped = new CountDownLatch(1);
	private final AtomicBoolean asyncException = new AtomicBoolean(false);
	// ProducerBrokerExchange�����м�¼�������ߵĵ�ǰ��Ϣ���е�ǰ�����ߴ��������һ��id��ConnectionContext�����������״̬�������Ϣ
	private final Map<ProducerId, ProducerBrokerExchange> producerExchanges = new HashMap<ProducerId, ProducerBrokerExchange>();
	private final Map<ConsumerId, ConsumerBrokerExchange> consumerExchanges = new HashMap<ConsumerId, ConsumerBrokerExchange>();
	private final CountDownLatch dispatchStoppedLatch = new CountDownLatch(1);
	private ConnectionContext context;
	private boolean networkConnection;
	// �Ƿ���Ҫ�ݴ�
	private boolean faultTolerantConnection;
	private final AtomicInteger protocolVersion = new AtomicInteger(CommandTypes.PROTOCOL_VERSION);
	private DemandForwardingBridge duplexBridge;
	private final TaskRunnerFactory taskRunnerFactory;
	private final TaskRunnerFactory stopTaskRunnerFactory;
	// �ͻ����������֮������ӿ��Խ����������������wireformatinfo����󣬱�transportconnection������Ѿ������ˣ�֮���ٴ��������Ӷ���洢�������������
	private TransportConnectionStateRegister connectionStateRegister = new SingleTransportConnectionStateRegister();
	private final ReentrantReadWriteLock serviceLock = new ReentrantReadWriteLock();
	private String duplexNetworkConnectorId;

	/**
	 * @param taskRunnerFactory - can be null if you want direct dispatch to the transport else
	 *            commands are sent async.
	 * @param stopTaskRunnerFactory - can <b>not</b> be null, used for stopping this connection.
	 */
	public TransportConnection(TransportConnector connector, final Transport transport, Broker broker,
			TaskRunnerFactory taskRunnerFactory, TaskRunnerFactory stopTaskRunnerFactory) {
		this.connector = connector;
		this.broker = broker;
		RegionBroker rb = (RegionBroker) broker.getAdaptor(RegionBroker.class);
		brokerConnectionStates = rb.getConnectionStates();
		if (connector != null) {
			this.statistics.setParent(connector.getStatistics());
			this.messageAuthorizationPolicy = connector.getMessageAuthorizationPolicy();
		}
		this.taskRunnerFactory = taskRunnerFactory;
		this.stopTaskRunnerFactory = stopTaskRunnerFactory;
		this.transport = transport;
		final BrokerService brokerService = this.broker.getBrokerService();
		if (this.transport instanceof BrokerServiceAware) {
			((BrokerServiceAware) this.transport).setBrokerService(brokerService);
		}
		this.transport.setTransportListener(new DefaultTransportListener() {
			@Override
			public void onCommand(Object o) {
				serviceLock.readLock().lock();
				try {
					if (!(o instanceof Command)) {
						throw new RuntimeException("Protocol violation - Command corrupted: " + o.toString());
					}
					Command command = (Command) o;
					System.out.println(command);
					if (!brokerService.isStopping()) {
						Response response = service(command);
						if (response != null && !brokerService.isStopping()) {
							dispatchSync(response);
						}
					} else {
						throw new BrokerStoppedException("Broker " + brokerService + " is being stopped");
					}
				} finally {
					serviceLock.readLock().unlock();
				}
			}
			@Override
			public void onException(IOException exception) {
				serviceLock.readLock().lock();
				try {
					serviceTransportException(exception);
				} finally {
					serviceLock.readLock().unlock();
				}
			}
		});
		connected = true;
	}
	/**
	 * Returns the number of messages to be dispatched to this connection
	 * @return size of dispatch queue
	 */
	@Override
	public int getDispatchQueueSize() {
		synchronized (dispatchQueue) {
			return dispatchQueue.size();
		}
	}
	public void serviceTransportException(IOException e) {
		BrokerService bService = connector.getBrokerService();
		if (bService.isShutdownOnSlaveFailure()) {
			if (brokerInfo != null) {
				if (brokerInfo.isSlaveBroker()) {
					LOG.error("Slave has exception: {} shutting down master now.", e.getMessage(), e);
					try {
						doStop();
						bService.stop();
					} catch (Exception ex) {
						LOG.warn("Failed to stop the master", ex);
					}
				}
			}
		}
		if (!stopping.get() && !pendingStop) {
			transportException.set(e);
			e.printStackTrace();
			if (TRANSPORTLOG.isDebugEnabled()) {
				TRANSPORTLOG.debug(this + " failed: " + e, e);
			} else if (TRANSPORTLOG.isWarnEnabled() && !expected(e)) {
				TRANSPORTLOG.warn(this + " failed: " + e);
			}
			stopAsync(e);
		}
	}
	private boolean expected(IOException e) {
		return isStomp()
				&& ((e instanceof SocketException && e.getMessage().indexOf("reset") != -1) || e instanceof EOFException);
	}
	private boolean isStomp() {
		URI uri = connector.getUri();
		return uri != null && uri.getScheme() != null && uri.getScheme().indexOf("stomp") != -1;
	}
	/**
	 * Calls the serviceException method in an async thread. Since handling a service exception
	 * closes a socket, we should not tie up broker threads since client sockets may hang or cause
	 * deadlocks.
	 */
	@Override
	public void serviceExceptionAsync(final IOException e) {
		if (asyncException.compareAndSet(false, true)) {
			new Thread("Async Exception Handler") {
				@Override
				public void run() {
					serviceException(e);
				}
			}.start();
		}
	}
	/**
	 * Closes a clients connection due to a detected error. Errors are ignored if: the client is
	 * closing or broker is closing. Otherwise, the connection error transmitted to the client
	 * before stopping it's transport.
	 */
	@Override
	public void serviceException(Throwable e) {
		// are we a transport exception such as not being able to dispatch
		// synchronously to a transport
		if (e instanceof IOException) {
			serviceTransportException((IOException) e);
		} else if (e.getClass() == BrokerStoppedException.class) {
			// Handle the case where the broker is stopped
			// But the client is still connected.
			if (!stopping.get()) {
				SERVICELOG.debug("Broker has been stopped.  Notifying client and closing his connection.");
				ConnectionError ce = new ConnectionError();
				ce.setException(e);
				dispatchSync(ce);
				// Record the error that caused the transport to stop
				transportException.set(e);
				// Wait a little bit to try to get the output buffer to flush
				// the exception notification to the client.
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				// Worst case is we just kill the connection before the
				// notification gets to him.
				stopAsync();
			}
		} else if (!stopping.get() && !inServiceException) {
			inServiceException = true;
			try {
				if (SERVICELOG.isDebugEnabled()) {
					SERVICELOG.debug("Async error occurred: " + e, e);
				} else {
					SERVICELOG.warn("Async error occurred: " + e);
				}
				ConnectionError ce = new ConnectionError();
				ce.setException(e);
				if (pendingStop) {
					dispatchSync(ce);
				} else {
					dispatchAsync(ce);
				}
			} finally {
				inServiceException = false;
			}
		}
	}
	@Override
	/**
	 * ���崦��ÿһ�����Ҳ����command����
	 */
	public Response service(Command command) {
		MDC.put("activemq.connector", connector.getUri().toString());
		Response response = null;
		boolean responseRequired = command.isResponseRequired();
		int commandId = command.getCommandId();
		try {
			if (!pendingStop) {
				// �������λ��
				response = command.visit(this);
			} else {
				response = new ExceptionResponse(transportException.get());
			}
		} catch (Throwable e) {
			if (SERVICELOG.isDebugEnabled() && e.getClass() != BrokerStoppedException.class) {
				SERVICELOG.debug("Error occured while processing " + (responseRequired ? "sync" : "async")
						+ " command: " + command + ", exception: " + e, e);
			}
			if (e instanceof SuppressReplyException || (e.getCause() instanceof SuppressReplyException)) {
				LOG.info("Suppressing reply to: " + command + " on: " + e + ", cause: " + e.getCause());
				responseRequired = false;
			}
			if (responseRequired) {
				if (e instanceof SecurityException || e.getCause() instanceof SecurityException) {
					SERVICELOG.warn("Security Error occurred on connection to: {}, {}", transport.getRemoteAddress(),
							e.getMessage());
				}
				response = new ExceptionResponse(e);
			} else {
				serviceException(e);
			}
		}
		if (responseRequired) {
			if (response == null) {
				response = new Response();
			}
			response.setCorrelationId(commandId);
		}
		// The context may have been flagged so that the response is not
		// sent.
		if (context != null) {
			if (context.isDontSendReponse()) {
				context.setDontSendReponse(false);
				response = null;
			}
			context = null;
		}
		MDC.remove("activemq.connector");
		return response;
	}
	@Override
	/**
	 * ��KeepAliveInfo���󲻽����κδ���
	 */
	public Response processKeepAlive(KeepAliveInfo info) throws Exception {
		return null;
	}
	@Override
	public Response processRemoveSubscription(RemoveSubscriptionInfo info) throws Exception {
		broker.removeSubscription(lookupConnectionState(info.getConnectionId()).getContext(), info);
		return null;
	}
	@Override
	/**
	 * ����WireFormatInfo����
	 */
	public Response processWireFormat(WireFormatInfo info) throws Exception {
		wireFormatInfo = info;
		protocolVersion.set(info.getVersion());
		return null;
	}
	@Override
	public Response processShutdown(ShutdownInfo info) throws Exception {
		stopAsync();
		return null;
	}
	@Override
	public Response processFlush(FlushCommand command) throws Exception {
		return null;
	}
	@Override
	public Response processBeginTransaction(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = null;
		if (cs != null) {
			context = cs.getContext();
		}
		if (cs == null) {
			throw new NullPointerException("Context is null");
		}
		// Avoid replaying dup commands
		if (cs.getTransactionState(info.getTransactionId()) == null) {
			cs.addTransactionState(info.getTransactionId());
			broker.beginTransaction(context, info.getTransactionId());
		}
		return null;
	}
	@Override
	public int getActiveTransactionCount() {
		int rc = 0;
		for (TransportConnectionState cs : connectionStateRegister.listConnectionStates()) {
			Collection<TransactionState> transactions = cs.getTransactionStates();
			for (TransactionState transaction : transactions) {
				rc++;
			}
		}
		return rc;
	}
	@Override
	public Long getOldestActiveTransactionDuration() {
		TransactionState oldestTX = null;
		for (TransportConnectionState cs : connectionStateRegister.listConnectionStates()) {
			Collection<TransactionState> transactions = cs.getTransactionStates();
			for (TransactionState transaction : transactions) {
				if (oldestTX == null || oldestTX.getCreatedAt() < transaction.getCreatedAt()) {
					oldestTX = transaction;
				}
			}
		}
		if (oldestTX == null) {
			return null;
		}
		return System.currentTimeMillis() - oldestTX.getCreatedAt();
	}
	@Override
	public Response processEndTransaction(TransactionInfo info) throws Exception {
		// No need to do anything. This packet is just sent by the client
		// make sure he is synced with the server as commit command could
		// come from a different connection.
		return null;
	}
	@Override
	public Response processPrepareTransaction(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = null;
		if (cs != null) {
			context = cs.getContext();
		}
		if (cs == null) {
			throw new NullPointerException("Context is null");
		}
		TransactionState transactionState = cs.getTransactionState(info.getTransactionId());
		if (transactionState == null) {
			throw new IllegalStateException(
					"Cannot prepare a transaction that had not been started or previously returned XA_RDONLY: "
							+ info.getTransactionId());
		}
		// Avoid dups.
		if (!transactionState.isPrepared()) {
			transactionState.setPrepared(true);
			int result = broker.prepareTransaction(context, info.getTransactionId());
			transactionState.setPreparedResult(result);
			if (result == XAResource.XA_RDONLY) {
				// we are done, no further rollback or commit from TM
				cs.removeTransactionState(info.getTransactionId());
			}
			IntegerResponse response = new IntegerResponse(result);
			return response;
		} else {
			IntegerResponse response = new IntegerResponse(transactionState.getPreparedResult());
			return response;
		}
	}
	@Override
	public Response processCommitTransactionOnePhase(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = cs.getContext();
		cs.removeTransactionState(info.getTransactionId());
		broker.commitTransaction(context, info.getTransactionId(), true);
		return null;
	}
	@Override
	public Response processCommitTransactionTwoPhase(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = cs.getContext();
		cs.removeTransactionState(info.getTransactionId());
		broker.commitTransaction(context, info.getTransactionId(), false);
		return null;
	}
	@Override
	public Response processRollbackTransaction(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = cs.getContext();
		cs.removeTransactionState(info.getTransactionId());
		broker.rollbackTransaction(context, info.getTransactionId());
		return null;
	}
	@Override
	public Response processForgetTransaction(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = cs.getContext();
		broker.forgetTransaction(context, info.getTransactionId());
		return null;
	}
	@Override
	public Response processRecoverTransactions(TransactionInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		context = cs.getContext();
		TransactionId[] preparedTransactions = broker.getPreparedTransactions(context);
		return new DataArrayResponse(preparedTransactions);
	}
	@Override
	/**
	 * ����ActiveMQMessage��Ϣ
	 */
	public Response processMessage(Message messageSend) throws Exception {
		// �õ������ߵ�id
		ProducerId producerId = messageSend.getProducerId();
		ProducerBrokerExchange producerExchange = getProducerBrokerExchange(producerId);
		if (producerExchange.canDispatch(messageSend)) {
			// �����Ϣ���Էַ�
			broker.send(producerExchange, messageSend);
		}
		return null;
	}
	@Override
	public Response processMessageAck(MessageAck ack) throws Exception {
		ConsumerBrokerExchange consumerExchange = getConsumerBrokerExchange(ack.getConsumerId());
		if (consumerExchange != null) {
			broker.acknowledge(consumerExchange, ack);
		} else if (ack.isInTransaction()) {
			LOG.warn("no matching consumer, ignoring ack {}", consumerExchange, ack);
		}
		return null;
	}
	@Override
	public Response processMessagePull(MessagePull pull) throws Exception {
		return broker.messagePull(lookupConnectionState(pull.getConsumerId()).getContext(), pull);
	}
	@Override
	public Response processMessageDispatchNotification(MessageDispatchNotification notification) throws Exception {
		broker.processDispatchNotification(notification);
		return null;
	}
	@Override
	public Response processAddDestination(DestinationInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		broker.addDestinationInfo(cs.getContext(), info);
		if (info.getDestination().isTemporary()) {
			cs.addTempDestination(info);
		}
		return null;
	}
	@Override
	public Response processRemoveDestination(DestinationInfo info) throws Exception {
		TransportConnectionState cs = lookupConnectionState(info.getConnectionId());
		broker.removeDestinationInfo(cs.getContext(), info);
		if (info.getDestination().isTemporary()) {
			cs.removeTempDestination(info.getDestination());
		}
		return null;
	}
	@Override
	/**
	 * ����ProdcerInfo����
	 */
	public Response processAddProducer(ProducerInfo info) throws Exception {
		// ��ö�Ӧ��session��Ϣ
		SessionId sessionId = info.getProducerId().getParentId();
		ConnectionId connectionId = sessionId.getParentId();
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs == null) {
			throw new IllegalStateException("Cannot add a producer to a connection that had not been registered: "
					+ connectionId);
		}
		SessionState ss = cs.getSessionState(sessionId);
		if (ss == null) {
			throw new IllegalStateException("Cannot add a producer to a session that had not been registered: "
					+ sessionId);
		}
		// Avoid replaying dup commands
		if (!ss.getProducerIds().contains(info.getProducerId())) {
			ActiveMQDestination destination = info.getDestination();
			// Do not check for null here as it would cause the count of max producers to exclude
			// anonymous producers. The isAdvisoryTopic method checks for null so it is safe to
			// call it from here with a null Destination value.
			if (!AdvisorySupport.isAdvisoryTopic(destination)) {
				if (getProducerCount(connectionId) >= connector.getMaximumProducersAllowedPerConnection()) {
					throw new IllegalStateException("Can't add producer on connection " + connectionId
							+ ": at maximum limit: " + connector.getMaximumProducersAllowedPerConnection());
				}
			}
			broker.addProducer(cs.getContext(), info);
			try {
				ss.addProducer(info);
			} catch (IllegalStateException e) {
				broker.removeProducer(cs.getContext(), info);
			}
		}
		return null;
	}
	@Override
	/**
	 * �Ƴ�������
	 */
	public Response processRemoveProducer(ProducerId id) throws Exception {
		SessionId sessionId = id.getParentId();
		ConnectionId connectionId = sessionId.getParentId();
		TransportConnectionState cs = lookupConnectionState(connectionId);
		SessionState ss = cs.getSessionState(sessionId);
		if (ss == null) {
			throw new IllegalStateException("Cannot remove a producer from a session that had not been registered: "
					+ sessionId);
		}
		ProducerState ps = ss.removeProducer(id);
		if (ps == null) {
			throw new IllegalStateException("Cannot remove a producer that had not been registered: " + id);
		}
		removeProducerBrokerExchange(id);
		broker.removeProducer(cs.getContext(), ps.getInfo());
		return null;
	}
	@Override
	/**
	 * ��ConsumerInfo������д�����
	 */
	public Response processAddConsumer(ConsumerInfo info) throws Exception {
		// ���ConsumerId��û������parentId�Ļ�����ôparentId�����Լ�
		SessionId sessionId = info.getConsumerId().getParentId();
		// ͬ�ϣ����Ϊnull�������Լ�
		ConnectionId connectionId = sessionId.getParentId();
		// �ҵ�connectionId����Ķ�Ӧ��TransportConnectionState��������ڴ���ConnectionInfoʱ���Ѿ������ȥ��
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs == null) {
			throw new IllegalStateException("Cannot add a consumer to a connection that had not been registered: "
					+ connectionId);
		}
		// ����ڴ���ConnectionInfoʱ���Ѿ������ȥ�ˣ������¼��һ������Ҫ����Ϣ����ConnectionId���ַ������ڴ���TransportConnectionState����ʱ�����Զ�����һ��SessionState����
		SessionState ss = cs.getSessionState(sessionId);
		if (ss == null) {
			throw new IllegalStateException(broker.getBrokerName()
					+ " Cannot add a consumer to a session that had not been registered: " + sessionId);
		}
		// Avoid replaying dup commands
		// ���⴦����ͬ�������������������Ͳ��ٴ���
		if (!ss.getConsumerIds().contains(info.getConsumerId())) {
			// �ڿͻ��˴��ݵ�����˵���Ϣ�л����һ��destination���ԣ��������ָ����ʵ����
			ActiveMQDestination destination = info.getDestination();
			// ����connectionid��һ���ж��ٸ�consumer������������ֵ�����׳��쳣
			if (destination != null && !AdvisorySupport.isAdvisoryTopic(destination)) {
				if (getConsumerCount(connectionId) >= connector.getMaximumConsumersAllowedPerConnection()) {
					throw new IllegalStateException("Can't add consumer on connection " + connectionId
							+ ": at maximum limit: " + connector.getMaximumConsumersAllowedPerConnection());
				}
			}
			// �����������ߣ����������߼��뵽Topic�������Queue��
			broker.addConsumer(cs.getContext(), info);
			try {
				// ��consumerinfo���뵽sessionstate��
				ss.addConsumer(info);
				// ����ConsumerBrokerExchange���󣬲���������뵽consumerExchanges�С�
				addConsumerBrokerExchange(info.getConsumerId());
			} catch (IllegalStateException e) {
				broker.removeConsumer(cs.getContext(), info);
			}
		}
		return null;
	}
	@Override
	/**
	 * ɾ��consumer
	 */
	public Response processRemoveConsumer(ConsumerId id, long lastDeliveredSequenceId) throws Exception {
		SessionId sessionId = id.getParentId();
		ConnectionId connectionId = sessionId.getParentId();
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs == null) {
			throw new IllegalStateException("Cannot remove a consumer from a connection that had not been registered: "
					+ connectionId);
		}
		SessionState ss = cs.getSessionState(sessionId);
		if (ss == null) {
			throw new IllegalStateException("Cannot remove a consumer from a session that had not been registered: "
					+ sessionId);
		}
		ConsumerState consumerState = ss.removeConsumer(id);
		if (consumerState == null) {
			throw new IllegalStateException("Cannot remove a consumer that had not been registered: " + id);
		}
		ConsumerInfo info = consumerState.getInfo();
		info.setLastDeliveredSequenceId(lastDeliveredSequenceId);
		broker.removeConsumer(cs.getContext(), consumerState.getInfo());
		// ��consumerExchanges�������Ƴ�id��Ӧ��ֵ
		removeConsumerBrokerExchange(id);
		return null;
	}
	@Override
	/**
	 * ����SessionInfo����
	 */
	public Response processAddSession(SessionInfo info) throws Exception {
		// info�е�sessionid�ڿͻ��˴��䵽���������������Ѿ�������
		ConnectionId connectionId = info.getSessionId().getParentId();
		// ����connectionid�ҵ���Ӧ��״̬����
		TransportConnectionState cs = lookupConnectionState(connectionId);
		// Avoid replaying dup commands
		if (cs != null && !cs.getSessionIds().contains(info.getSessionId())) {
			// ����regionbroker��˵���������������һ����ʵ��
			broker.addSession(cs.getContext(), info);
			try {
				// ����session��Ϣ
				cs.addSession(info);
			} catch (IllegalStateException e) {
				LOG.warn("Failed to add session: {}", info.getSessionId(), e);
				broker.removeSession(cs.getContext(), info);
			}
		}
		return null;
	}
	@Override
	/**
	 *��session�Ƴ�
	 */
	public Response processRemoveSession(SessionId id, long lastDeliveredSequenceId) throws Exception {
		ConnectionId connectionId = id.getParentId();
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs == null) {
			throw new IllegalStateException("Cannot remove session from connection that had not been registered: "
					+ connectionId);
		}
		SessionState session = cs.getSessionState(id);
		if (session == null) {
			throw new IllegalStateException("Cannot remove session that had not been registered: " + id);
		}
		// Don't let new consumers or producers get added while we are closing
		// this down.
		session.shutdown();
		// Cascade the connection stop to the consumers and producers.
		for (ConsumerId consumerId : session.getConsumerIds()) {
			try {
				processRemoveConsumer(consumerId, lastDeliveredSequenceId);
			} catch (Throwable e) {
				LOG.warn("Failed to remove consumer: {}", consumerId, e);
			}
		}
		for (ProducerId producerId : session.getProducerIds()) {
			try {
				processRemoveProducer(producerId);
			} catch (Throwable e) {
				LOG.warn("Failed to remove producer: {}", producerId, e);
			}
		}
		cs.removeSession(id);
		broker.removeSession(cs.getContext(), session.getInfo());// ��ʵ��
		return null;
	}
	@Override
	/**
	 * ���ͻ��˵���connection.start����ʱ����������������������˴���ConnectionInfoʱ�������������������������Ҫ�ǶԿͻ��˺ͷ���˵�һЩ��Ϣ���б���洢��ָ���ļ��������У�brokerConnectionStates����¼��ǰ������״̬��Ϣ������clientId��ConnectionContext���󱣴浽clientIdSet�������Լ���ConnectionContext���浽connections�����У�������RegionBroker�����ģ�
	 */
	public Response processAddConnection(ConnectionInfo info) throws Exception {
		// Older clients should have been defaulting this field to true.. but
		// they were not.
		// ���������Ϊ��ǰ���Ͽͻ���ʹ�õ�
		if (wireFormatInfo != null && wireFormatInfo.getVersion() <= 2) {
			info.setClientMaster(true);
		}
		TransportConnectionState state;
		// Make sure 2 concurrent connections by the same ID only generate 1
		// TransportConnectionState object.
		// �������������Ҫ��Ϊ��ֹ�ͻ���ͬʱ���������������󣬶���������������ֻ��һ��������Դ���TransportConnectionState����
		synchronized (brokerConnectionStates) {
			// ���state=null��˵���ͻ��˵�ǰû�����ӹ���������
			state = (TransportConnectionState) brokerConnectionStates.get(info.getConnectionId());
			if (state == null) {
				state = new TransportConnectionState(info, this);
				brokerConnectionStates.put(info.getConnectionId(), state);
			}
			state.incrementReference();
		}
		// If there are 2 concurrent connections for the same connection id,
		// then last one in wins, we need to sync here
		// to figure out the winner.
		synchronized (state.getConnectionMutex()) {
			// ���������brokerConnectionStates�����޸ģ�ע���˲��Ǳ�������������󣬷�������Ϊ������һ��û�б�Ҫ
			if (state.getConnection() != this) {
				LOG.debug("Killing previous stale connection: {}", state.getConnection().getRemoteAddress());
				state.getConnection().stop();
				LOG.debug("Connection {} taking over previous connection: {}", getRemoteAddress(), state
						.getConnection().getRemoteAddress());
				state.setConnection(this);
				state.reset(info);
			}
		}
		// �����ע�뵽TransportConnectionStateRegister����
		registerConnectionState(info.getConnectionId(), state);
		LOG.debug("Setting up new connection id: {}, address: {}, info: {}", new Object[] { info.getConnectionId(),
				getRemoteAddress(), info });
		this.faultTolerantConnection = info.isFaultTolerant();
		// Setup the context.
		String clientId = info.getClientId();
		context = new ConnectionContext();
		context.setBroker(broker);
		context.setClientId(clientId);
		context.setClientMaster(info.isClientMaster());
		context.setConnection(this);
		context.setConnectionId(info.getConnectionId());
		context.setConnector(connector);
		context.setMessageAuthorizationPolicy(getMessageAuthorizationPolicy());
		context.setNetworkConnection(networkConnection);
		context.setFaultTolerant(faultTolerantConnection);
		context.setTransactions(new ConcurrentHashMap<TransactionId, Transaction>());
		context.setUserName(info.getUserName());
		context.setWireFormatInfo(wireFormatInfo);
		context.setReconnect(info.isFailoverReconnect());
		this.manageable = info.isManageable();
		context.setConnectionState(state);
		state.setContext(context);
		state.setConnection(this);
		if (info.getClientIp() == null) {
			info.setClientIp(getRemoteAddress());
		}
		try {
			broker.addConnection(context, info);
		} catch (Exception e) {
			synchronized (brokerConnectionStates) {
				brokerConnectionStates.remove(info.getConnectionId());
			}
			unregisterConnectionState(info.getConnectionId());
			LOG.warn("Failed to add Connection {} due to {}", info.getConnectionId(), e);
			if (e instanceof SecurityException) {
				// close this down - in case the peer of this transport doesn't play nice
				delayedStop(2000, "Failed with SecurityException: " + e.getLocalizedMessage(), e);
			}
			throw e;
		}
		if (info.isManageable()) {
			// send ConnectionCommand
			ConnectionControl command = this.connector.getConnectionControl();
			command.setFaultTolerant(broker.isFaultTolerantConfiguration());
			if (info.isFailoverReconnect()) {
				command.setRebalanceConnection(false);
			}
			// ������������������Կ�����һ����ʵ��
			dispatchAsync(command);
		}
		return null;
	}
	@Override
	public synchronized Response processRemoveConnection(ConnectionId id, long lastDeliveredSequenceId)
			throws InterruptedException {
		LOG.debug("remove connection id: {}", id);
		TransportConnectionState cs = lookupConnectionState(id);
		if (cs != null) {
			// Don't allow things to be added to the connection state while we
			// are shutting down.
			cs.shutdown();
			// Cascade the connection stop to the sessions.
			for (SessionId sessionId : cs.getSessionIds()) {
				try {
					processRemoveSession(sessionId, lastDeliveredSequenceId);
				} catch (Throwable e) {
					SERVICELOG.warn("Failed to remove session {}", sessionId, e);
				}
			}
			// Cascade the connection stop to temp destinations.
			for (Iterator<DestinationInfo> iter = cs.getTempDestinations().iterator(); iter.hasNext();) {
				DestinationInfo di = iter.next();
				try {
					broker.removeDestination(cs.getContext(), di.getDestination(), 0);
				} catch (Throwable e) {
					SERVICELOG.warn("Failed to remove tmp destination {}", di.getDestination(), e);
				}
				iter.remove();
			}
			try {
				broker.removeConnection(cs.getContext(), cs.getInfo(), transportException.get());
			} catch (Throwable e) {
				SERVICELOG.warn("Failed to remove connection {}", cs.getInfo(), e);
			}
			TransportConnectionState state = unregisterConnectionState(id);
			if (state != null) {
				synchronized (brokerConnectionStates) {
					// If we are the last reference, we should remove the state
					// from the broker.
					if (state.decrementReference() == 0) {
						brokerConnectionStates.remove(id);
					}
				}
			}
		}
		return null;
	}
	@Override
	public Response processProducerAck(ProducerAck ack) throws Exception {
		// A broker should not get ProducerAck messages.
		return null;
	}
	@Override
	public Connector getConnector() {
		return connector;
	}
	@Override
	/**
	 * ͬ��������Ϣ
	 */
	public void dispatchSync(Command message) {
		try {
			processDispatch(message);
		} catch (IOException e) {
			serviceExceptionAsync(e);
		}
	}
	@Override
	public void dispatchAsync(Command message) {
		if (!stopping.get()) {
			if (taskRunner == null) {
				dispatchSync(message);
			} else {
				synchronized (dispatchQueue) {
					dispatchQueue.add(message);
				}
				try {
					taskRunner.wakeup();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} else {
			if (message.isMessageDispatch()) {
				MessageDispatch md = (MessageDispatch) message;
				TransmitCallback sub = md.getTransmitCallback();
				broker.postProcessDispatch(md);
				if (sub != null) {
					sub.onFailure();
				}
			}
		}
	}
	/**
	 * ����ConnectionControl����ʱ�������������������һ����ʵ��
	 * @Title: processDispatch
	 * @Description: TODO
	 * @param command
	 * @throws IOException
	 * @return: void
	 */
	protected void processDispatch(Command command) throws IOException {
		// ��������COmmand������ConnectionControlʱ��command.isMessageDispatch()����false
		MessageDispatch messageDispatch = (MessageDispatch) (command.isMessageDispatch() ? command : null);
		try {
			if (!stopping.get()) {
				if (messageDispatch != null) {
					try {
						broker.preProcessDispatch(messageDispatch);
					} catch (RuntimeException convertToIO) {
						throw new IOException(convertToIO);
					}
				}
				dispatch(command);
			}
		} catch (IOException e) {
			if (messageDispatch != null) {
				TransmitCallback sub = messageDispatch.getTransmitCallback();
				broker.postProcessDispatch(messageDispatch);
				if (sub != null) {
					sub.onFailure();
				}
				messageDispatch = null;
				throw e;
			}
		} finally {
			if (messageDispatch != null) {
				TransmitCallback sub = messageDispatch.getTransmitCallback();
				broker.postProcessDispatch(messageDispatch);
				if (sub != null) {
					sub.onSuccess();
				}
			}
		}
	}
	@Override
	/**
	 * �������е���Ϣ�ַ���ȥ
	 */
	public boolean iterate() {
		try {
			if (pendingStop || stopping.get()) {
				if (dispatchStopped.compareAndSet(false, true)) {
					if (transportException.get() == null) {
						try {
							dispatch(new ShutdownInfo());
						} catch (Throwable ignore) {
						}
					}
					dispatchStoppedLatch.countDown();
				}
				return false;
			}
			if (!dispatchStopped.get()) {
				Command command = null;
				synchronized (dispatchQueue) {
					if (dispatchQueue.isEmpty()) {
						return false;
					}
					command = dispatchQueue.remove(0);
				}
				processDispatch(command);
				return true;
			}
			return false;
		} catch (IOException e) {
			if (dispatchStopped.compareAndSet(false, true)) {
				dispatchStoppedLatch.countDown();
			}
			serviceExceptionAsync(e);
			return false;
		}
	}
	/**
	 * Returns the statistics for this connection
	 */
	@Override
	public ConnectionStatistics getStatistics() {
		return statistics;
	}
	public MessageAuthorizationPolicy getMessageAuthorizationPolicy() {
		return messageAuthorizationPolicy;
	}
	public void setMessageAuthorizationPolicy(MessageAuthorizationPolicy messageAuthorizationPolicy) {
		this.messageAuthorizationPolicy = messageAuthorizationPolicy;
	}
	@Override
	public boolean isManageable() {
		return manageable;
	}
	@Override
	public void start() throws Exception {
		try {
			synchronized (this) {
				starting = true;
				if (taskRunnerFactory != null) {
					taskRunner = taskRunnerFactory.createTaskRunner(this, "ActiveMQ Connection Dispatcher: "
							+ getRemoteAddress());
				} else {
					taskRunner = null;
				}
				transport.start();// û�о�������
				active = true;
				BrokerInfo info = connector.getBrokerInfo().copy();
				if (connector.isUpdateClusterClients()) {
					info.setPeerBrokerInfos(this.broker.getPeerBrokerInfos());
				} else {
					info.setPeerBrokerInfos(null);
				}
				dispatchAsync(info);
				connector.onStarted(this);
			}
		} catch (Exception e) {
			// Force clean up on an error starting up.
			pendingStop = true;
			throw e;
		} finally {
			// stop() can be called from within the above block,
			// but we want to be sure start() completes before
			// stop() runs, so queue the stop until right now:
			setStarting(false);
			if (isPendingStop()) {
				LOG.debug("Calling the delayed stop() after start() {}", this);
				stop();
			}
		}
	}
	@Override
	public void stop() throws Exception {
		// do not stop task the task runner factories (taskRunnerFactory, stopTaskRunnerFactory)
		// as their lifecycle is handled elsewhere
		stopAsync();
		while (!stopped.await(5, TimeUnit.SECONDS)) {
			LOG.info("The connection to '{}' is taking a long time to shutdown.", transport.getRemoteAddress());
		}
	}
	public void delayedStop(final int waitTime, final String reason, Throwable cause) {
		if (waitTime > 0) {
			synchronized (this) {
				pendingStop = true;
				transportException.set(cause);
			}
			try {
				stopTaskRunnerFactory.execute(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(waitTime);
							stopAsync();
							LOG.info("Stopping {} because {}", transport.getRemoteAddress(), reason);
						} catch (InterruptedException e) {
						}
					}
				});
			} catch (Throwable t) {
				LOG.warn("Cannot create stopAsync. This exception will be ignored.", t);
			}
		}
	}
	public void stopAsync(Throwable cause) {
		transportException.set(cause);
		stopAsync();
	}
	public void stopAsync() {
		// If we're in the middle of starting then go no further... for now.
		synchronized (this) {
			pendingStop = true;
			if (starting) {
				LOG.debug("stopAsync() called in the middle of start(). Delaying till start completes..");
				return;
			}
		}
		if (stopping.compareAndSet(false, true)) {
			// Let all the connection contexts know we are shutting down
			// so that in progress operations can notice and unblock.
			List<TransportConnectionState> connectionStates = listConnectionStates();
			for (TransportConnectionState cs : connectionStates) {
				ConnectionContext connectionContext = cs.getContext();
				if (connectionContext != null) {
					connectionContext.getStopping().set(true);
				}
			}
			try {
				stopTaskRunnerFactory.execute(new Runnable() {
					@Override
					public void run() {
						serviceLock.writeLock().lock();
						try {
							doStop();
						} catch (Throwable e) {
							LOG.debug("Error occurred while shutting down a connection {}", this, e);
						} finally {
							stopped.countDown();
							serviceLock.writeLock().unlock();
						}
					}
				});
			} catch (Throwable t) {
				LOG.warn(
						"Cannot create async transport stopper thread. This exception is ignored. Not waiting for stop to complete",
						t);
				stopped.countDown();
			}
		}
	}
	@Override
	public String toString() {
		return "Transport Connection to: " + transport.getRemoteAddress();
	}
	/**
	 * ִ�йر�
	 * @Title: doStop
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	protected void doStop() throws Exception {
		LOG.debug("Stopping connection: {}", transport.getRemoteAddress());
		connector.onStopped(this);// �������Ӷ����connector��������Ӽ�����ɾ��
		try {
			synchronized (this) {
				if (duplexBridge != null) {
					duplexBridge.stop();
				}
			}
		} catch (Exception ignore) {
			LOG.trace("Exception caught stopping. This exception is ignored.", ignore);
		}
		try {
			transport.stop();
			LOG.debug("Stopped transport: {}", transport.getRemoteAddress());
		} catch (Exception e) {
			LOG.debug("Could not stop transport to {}. This exception is ignored.", transport.getRemoteAddress(), e);
		}
		if (taskRunner != null) {
			taskRunner.shutdown(1);
			taskRunner = null;
		}
		active = false;
		// Run the MessageDispatch callbacks so that message references get
		// cleaned up.
		synchronized (dispatchQueue) {
			for (Iterator<Command> iter = dispatchQueue.iterator(); iter.hasNext();) {
				Command command = iter.next();
				if (command.isMessageDispatch()) {
					MessageDispatch md = (MessageDispatch) command;
					TransmitCallback sub = md.getTransmitCallback();
					broker.postProcessDispatch(md);
					if (sub != null) {
						sub.onFailure();
					}
				}
			}
			dispatchQueue.clear();
		}
		//
		// Remove all logical connection associated with this connection
		// from the broker.
		if (!broker.isStopped()) {
			List<TransportConnectionState> connectionStates = listConnectionStates();
			connectionStates = listConnectionStates();
			for (TransportConnectionState cs : connectionStates) {
				cs.getContext().getStopping().set(true);
				try {
					LOG.debug("Cleaning up connection resources: {}", getRemoteAddress());
					processRemoveConnection(cs.getInfo().getConnectionId(), RemoveInfo.LAST_DELIVERED_UNKNOWN);
				} catch (Throwable ignore) {
					LOG.debug("Exception caught removing connection {}. This exception is ignored.", cs.getInfo()
							.getConnectionId(), ignore);
				}
			}
		}
		LOG.debug("Connection Stopped: {}", getRemoteAddress());
	}
	/**
	 * @return Returns the blockedCandidate.
	 */
	public boolean isBlockedCandidate() {
		return blockedCandidate;
	}
	/**
	 * @param blockedCandidate The blockedCandidate to set.
	 */
	public void setBlockedCandidate(boolean blockedCandidate) {
		this.blockedCandidate = blockedCandidate;
	}
	/**
	 * @return Returns the markedCandidate.
	 */
	public boolean isMarkedCandidate() {
		return markedCandidate;
	}
	/**
	 * @param markedCandidate The markedCandidate to set.
	 */
	public void setMarkedCandidate(boolean markedCandidate) {
		this.markedCandidate = markedCandidate;
		if (!markedCandidate) {
			timeStamp = 0;
			blockedCandidate = false;
		}
	}
	/**
	 * @param slow The slow to set.
	 */
	public void setSlow(boolean slow) {
		this.slow = slow;
	}
	/**
	 * @return true if the Connection is slow
	 */
	@Override
	public boolean isSlow() {
		return slow;
	}
	/**
	 * @return true if the Connection is potentially blocked
	 */
	public boolean isMarkedBlockedCandidate() {
		return markedCandidate;
	}
	/**
	 * Mark the Connection, so we can deem if it's collectable on the next sweep
	 */
	public void doMark() {
		if (timeStamp == 0) {
			timeStamp = System.currentTimeMillis();
		}
	}
	/**
	 * @return if after being marked, the Connection is still writing
	 */
	@Override
	public boolean isBlocked() {
		return blocked;
	}
	/**
	 * @return true if the Connection is connected
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}
	/**
	 * @param blocked The blocked to set.
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	/**
	 * @param connected The connected to set.
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	/**
	 * @return true if the Connection is active
	 */
	@Override
	public boolean isActive() {
		return active;
	}
	/**
	 * @param active The active to set.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * @return true if the Connection is starting
	 */
	public synchronized boolean isStarting() {
		return starting;
	}
	@Override
	public synchronized boolean isNetworkConnection() {
		return networkConnection;
	}
	@Override
	public boolean isFaultTolerantConnection() {
		return this.faultTolerantConnection;
	}
	protected synchronized void setStarting(boolean starting) {
		this.starting = starting;
	}
	/**
	 * @return true if the Connection needs to stop
	 */
	public synchronized boolean isPendingStop() {
		return pendingStop;
	}
	protected synchronized void setPendingStop(boolean pendingStop) {
		this.pendingStop = pendingStop;
	}
	@Override
	public Response processBrokerInfo(BrokerInfo info) {
		if (info.isSlaveBroker()) {
			LOG.error(" Slave Brokers are no longer supported - slave trying to attach is: {}", info.getBrokerName());
		} else if (info.isNetworkConnection() && info.isDuplexConnection()) {
			// so this TransportConnection is the rear end of a network bridge
			// We have been requested to create a two way pipe ...
			try {
				Properties properties = MarshallingSupport.stringToProperties(info.getNetworkProperties());
				Map<String, String> props = createMap(properties);
				NetworkBridgeConfiguration config = new NetworkBridgeConfiguration();
				IntrospectionSupport.setProperties(config, props, "");
				config.setBrokerName(broker.getBrokerName());
				// check for existing duplex connection hanging about
				// We first look if existing network connection already exists for the same broker
				// Id and network connector name
				// It's possible in case of brief network fault to have this transport connector
				// side of the connection always active
				// and the duplex network connector side wanting to open a new one
				// In this case, the old connection must be broken
				String duplexNetworkConnectorId = config.getName() + "@" + info.getBrokerId();
				CopyOnWriteArrayList<TransportConnection> connections = this.connector.getConnections();
				synchronized (connections) {
					for (Iterator<TransportConnection> iter = connections.iterator(); iter.hasNext();) {
						TransportConnection c = iter.next();
						if ((c != this) && (duplexNetworkConnectorId.equals(c.getDuplexNetworkConnectorId()))) {
							LOG.warn("Stopping an existing active duplex connection [{}] for network connector ({}).",
									c, duplexNetworkConnectorId);
							c.stopAsync();
							// better to wait for a bit rather than get connection id already in use
							// and failure to start new bridge
							c.getStopped().await(1, TimeUnit.SECONDS);
						}
					}
					setDuplexNetworkConnectorId(duplexNetworkConnectorId);
				}
				Transport localTransport = NetworkBridgeFactory.createLocalTransport(broker);
				Transport remoteBridgeTransport = transport;
				if (!(remoteBridgeTransport instanceof ResponseCorrelator)) {
					// the vm transport case is already wrapped
					remoteBridgeTransport = new ResponseCorrelator(remoteBridgeTransport);
				}
				String duplexName = localTransport.toString();
				if (duplexName.contains("#")) {
					duplexName = duplexName.substring(duplexName.lastIndexOf("#"));
				}
				MBeanNetworkListener listener = new MBeanNetworkListener(broker.getBrokerService(), config, broker
						.getBrokerService().createDuplexNetworkConnectorObjectName(duplexName));
				listener.setCreatedByDuplex(true);
				duplexBridge = NetworkBridgeFactory.createBridge(config, localTransport, remoteBridgeTransport,
						listener);
				duplexBridge.setBrokerService(broker.getBrokerService());
				// now turn duplex off this side
				info.setDuplexConnection(false);
				duplexBridge.setCreatedByDuplex(true);
				duplexBridge.duplexStart(this, brokerInfo, info);
				LOG.info("Started responder end of duplex bridge {}", duplexNetworkConnectorId);
				return null;
			} catch (TransportDisposedIOException e) {
				LOG.warn("Duplex bridge {} was stopped before it was correctly started.", duplexNetworkConnectorId);
				return null;
			} catch (Exception e) {
				LOG.error("Failed to create responder end of duplex network bridge {}", duplexNetworkConnectorId, e);
				return null;
			}
		}
		// We only expect to get one broker info command per connection
		if (this.brokerInfo != null) {
			LOG.warn("Unexpected extra broker info command received: {}", info);
		}
		this.brokerInfo = info;
		networkConnection = true;
		List<TransportConnectionState> connectionStates = listConnectionStates();
		for (TransportConnectionState cs : connectionStates) {
			cs.getContext().setNetworkConnection(true);
		}
		return null;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap<String, String> createMap(Properties properties) {
		return new HashMap(properties);
	}
	protected void dispatch(Command command) throws IOException {
		try {
			setMarkedCandidate(true);
			transport.oneway(command);
		} finally {
			setMarkedCandidate(false);
		}
	}
	@Override
	public String getRemoteAddress() {
		return transport.getRemoteAddress();
	}
	public Transport getTransport() {
		return transport;
	}
	@Override
	public String getConnectionId() {
		List<TransportConnectionState> connectionStates = listConnectionStates();
		for (TransportConnectionState cs : connectionStates) {
			if (cs.getInfo().getClientId() != null) {
				return cs.getInfo().getClientId();
			}
			return cs.getInfo().getConnectionId().toString();
		}
		return null;
	}
	@Override
	public void updateClient(ConnectionControl control) {
		if (isActive() && isBlocked() == false && isFaultTolerantConnection() && this.wireFormatInfo != null
				&& this.wireFormatInfo.getVersion() >= 6) {
			dispatchAsync(control);
		}
	}
	public ProducerBrokerExchange getProducerBrokerExchangeIfExists(ProducerInfo producerInfo) {
		ProducerBrokerExchange result = null;
		if (producerInfo != null && producerInfo.getProducerId() != null) {
			synchronized (producerExchanges) {
				result = producerExchanges.get(producerInfo.getProducerId());
			}
		}
		return result;
	}
	/**
	 * ������εõ�ProducerBrokerExchange������������ھʹ���һ���µ�
	 * @Title: getProducerBrokerExchange
	 * @Description: TODO
	 * @param id
	 * @return
	 * @throws IOException
	 * @return: ProducerBrokerExchange
	 */
	private ProducerBrokerExchange getProducerBrokerExchange(ProducerId id) throws IOException {
		ProducerBrokerExchange result = producerExchanges.get(id);
		if (result == null) {
			synchronized (producerExchanges) {
				result = new ProducerBrokerExchange();
				TransportConnectionState state = lookupConnectionState(id);
				context = state.getContext();
				result.setConnectionContext(context);
				if (context.isReconnect() || (context.isNetworkConnection() && connector.isAuditNetworkProducers())) {
					// �������id���͵����һ����Ϣid
					result.setLastStoredSequenceId(broker.getBrokerService().getPersistenceAdapter()
							.getLastProducerSequenceId(id));
				}
				SessionState ss = state.getSessionState(id.getParentId());
				if (ss != null) {
					// �ڴ���ProducerInfo����ʱ��ProducerState�����Ѿ����ý�ȥ��
					result.setProducerState(ss.getProducerState(id));
					ProducerState producerState = ss.getProducerState(id);
					if (producerState != null && producerState.getInfo() != null) {
						ProducerInfo info = producerState.getInfo();
						result.setMutable(info.getDestination() == null || info.getDestination().isComposite());
					}
				}
				producerExchanges.put(id, result);
			}
		} else {
			// ���õ�ǰ���µ�context�����Ļ���
			context = result.getConnectionContext();
		}
		return result;
	}
	private void removeProducerBrokerExchange(ProducerId id) {
		synchronized (producerExchanges) {
			producerExchanges.remove(id);
		}
	}
	private ConsumerBrokerExchange getConsumerBrokerExchange(ConsumerId id) {
		ConsumerBrokerExchange result = consumerExchanges.get(id);
		return result;
	}
	/**
	 * ����ConsumerBrokerExchange���󣬲���������뵽consumerExchanges�С�
	 * @Title: addConsumerBrokerExchange
	 * @Description: TODO
	 * @param id
	 * @return
	 * @return: ConsumerBrokerExchange
	 */
	private ConsumerBrokerExchange addConsumerBrokerExchange(ConsumerId id) {
		ConsumerBrokerExchange result = consumerExchanges.get(id);
		if (result == null) {
			synchronized (consumerExchanges) {
				result = new ConsumerBrokerExchange();
				TransportConnectionState state = lookupConnectionState(id);
				context = state.getContext();
				result.setConnectionContext(context);
				SessionState ss = state.getSessionState(id.getParentId());
				if (ss != null) {
					ConsumerState cs = ss.getConsumerState(id);
					if (cs != null) {
						ConsumerInfo info = cs.getInfo();
						if (info != null) {
							if (info.getDestination() != null && info.getDestination().isPattern()) {
								result.setWildcard(true);
							}
						}
					}
				}
				consumerExchanges.put(id, result);
			}
		}
		return result;
	}
	private void removeConsumerBrokerExchange(ConsumerId id) {
		synchronized (consumerExchanges) {
			consumerExchanges.remove(id);
		}
	}
	public int getProtocolVersion() {
		return protocolVersion.get();
	}
	@Override
	public Response processControlCommand(ControlCommand command) throws Exception {
		return null;
	}
	@Override
	public Response processMessageDispatch(MessageDispatch dispatch) throws Exception {
		return null;
	}
	@Override
	public Response processConnectionControl(ConnectionControl control) throws Exception {
		if (control != null) {
			faultTolerantConnection = control.isFaultTolerant();
		}
		return null;
	}
	@Override
	public Response processConnectionError(ConnectionError error) throws Exception {
		return null;
	}
	@Override
	public Response processConsumerControl(ConsumerControl control) throws Exception {
		ConsumerBrokerExchange consumerExchange = getConsumerBrokerExchange(control.getConsumerId());
		broker.processConsumerControl(consumerExchange, control);
		return null;
	}
	/**
	 * �����ע�ᵽTransportConnectionStateRegister����
	 * @Title: registerConnectionState
	 * @Description: TODO
	 * @param connectionId
	 * @param state
	 * @return
	 * @return: TransportConnectionState
	 */
	protected synchronized TransportConnectionState registerConnectionState(ConnectionId connectionId,
			TransportConnectionState state) {
		TransportConnectionState cs = null;
		if (!connectionStateRegister.isEmpty() && !connectionStateRegister.doesHandleMultipleConnectionStates()) {
			// swap implementations
			TransportConnectionStateRegister newRegister = new MapTransportConnectionStateRegister();
			newRegister.intialize(connectionStateRegister);
			connectionStateRegister = newRegister;
		}
		cs = connectionStateRegister.registerConnectionState(connectionId, state);
		return cs;
	}
	protected synchronized TransportConnectionState unregisterConnectionState(ConnectionId connectionId) {
		return connectionStateRegister.unregisterConnectionState(connectionId);
	}
	protected synchronized List<TransportConnectionState> listConnectionStates() {
		return connectionStateRegister.listConnectionStates();
	}
	protected synchronized TransportConnectionState lookupConnectionState(String connectionId) {
		return connectionStateRegister.lookupConnectionState(connectionId);
	}
	protected synchronized TransportConnectionState lookupConnectionState(ConsumerId id) {
		return connectionStateRegister.lookupConnectionState(id);
	}
	protected synchronized TransportConnectionState lookupConnectionState(ProducerId id) {
		return connectionStateRegister.lookupConnectionState(id);
	}
	protected synchronized TransportConnectionState lookupConnectionState(SessionId id) {
		return connectionStateRegister.lookupConnectionState(id);
	}
	// public only for testing
	/**
	 * ��connectionStateRegister��������ѰConnectionId�����TransportConnectionState����
	 * @Title: lookupConnectionState
	 * @Description: TODO
	 * @param connectionId
	 * @return
	 * @return: TransportConnectionState
	 */
	public synchronized TransportConnectionState lookupConnectionState(ConnectionId connectionId) {
		return connectionStateRegister.lookupConnectionState(connectionId);
	}
	protected synchronized void setDuplexNetworkConnectorId(String duplexNetworkConnectorId) {
		this.duplexNetworkConnectorId = duplexNetworkConnectorId;
	}
	protected synchronized String getDuplexNetworkConnectorId() {
		return this.duplexNetworkConnectorId;
	}
	public boolean isStopping() {
		return stopping.get();
	}
	protected CountDownLatch getStopped() {
		return stopped;
	}
	private int getProducerCount(ConnectionId connectionId) {
		int result = 0;
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs != null) {
			for (SessionId sessionId : cs.getSessionIds()) {
				SessionState sessionState = cs.getSessionState(sessionId);
				if (sessionState != null) {
					result += sessionState.getProducerIds().size();
				}
			}
		}
		return result;
	}
	/**
	 * ������ͬһ��ConnectionId��һ���ж��ٸ�consumerid
	 * @Title: getConsumerCount
	 * @Description: TODO
	 * @param connectionId
	 * @return
	 * @return: int
	 */
	private int getConsumerCount(ConnectionId connectionId) {
		int result = 0;
		TransportConnectionState cs = lookupConnectionState(connectionId);
		if (cs != null) {
			for (SessionId sessionId : cs.getSessionIds()) {
				SessionState sessionState = cs.getSessionState(sessionId);
				if (sessionState != null) {
					result += sessionState.getConsumerIds().size();
				}
			}
		}
		return result;
	}
	public WireFormatInfo getRemoteWireFormatInfo() {
		return wireFormatInfo;
	}
}