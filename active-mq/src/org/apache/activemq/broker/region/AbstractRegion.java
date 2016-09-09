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
package org.apache.activemq.broker.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.apache.activemq.DestinationDoesNotExistException;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ConsumerBrokerExchange;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.virtual.CompositeDestinationFilter;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerControl;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageDispatchNotification;
import org.apache.activemq.command.MessagePull;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveSubscriptionInfo;
import org.apache.activemq.command.Response;
import org.apache.activemq.filter.DestinationFilter;
import org.apache.activemq.filter.DestinationMap;
import org.apache.activemq.security.SecurityContext;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.activemq.usage.SystemUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
/**
 * 需要依赖一个broker对象
 * @ClassName: AbstractRegion
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午11:13:47
 */
public abstract class AbstractRegion implements Region {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRegion.class);
	// 下面这个属性的key是客户端在消息中传递过来的对象，value是根据key创建出的对应的对象
	protected final Map<ActiveMQDestination, Destination> destinations = new ConcurrentHashMap<ActiveMQDestination, Destination>();
	// 下面这个属性的key是客户端在消息中传递过来的对象，value是根据key创建出的对应的对象
	protected final DestinationMap destinationMap = new DestinationMap();
	// 存储消费者对应的订阅者对象
	protected final Map<ConsumerId, Subscription> subscriptions = new ConcurrentHashMap<ConsumerId, Subscription>();
	protected final SystemUsage usageManager;
	protected final DestinationFactory destinationFactory;
	protected final DestinationStatistics destinationStatistics;
	// 记录消息发送的个数
	protected final RegionStatistics regionStatistics = new RegionStatistics();
	protected final RegionBroker broker;
	protected boolean autoCreateDestinations = true;
	protected final TaskRunnerFactory taskRunnerFactory;
	protected final ReentrantReadWriteLock destinationsLock = new ReentrantReadWriteLock();
	// 当对消费者处理时，用于加锁
	protected final Map<ConsumerId, Object> consumerChangeMutexMap = new HashMap<ConsumerId, Object>();
	protected boolean started;

	public AbstractRegion(RegionBroker broker, DestinationStatistics destinationStatistics, SystemUsage memoryManager,
			TaskRunnerFactory taskRunnerFactory, DestinationFactory destinationFactory) {
		if (broker == null) {
			throw new IllegalArgumentException("null broker");
		}
		this.broker = broker;
		this.destinationStatistics = destinationStatistics;
		this.usageManager = memoryManager;
		this.taskRunnerFactory = taskRunnerFactory;
		if (destinationFactory == null) {
			throw new IllegalArgumentException("null destinationFactory");
		}
		this.destinationFactory = destinationFactory;
	}
	@Override
	public final void start() throws Exception {
		started = true;
		Set<ActiveMQDestination> inactiveDests = getInactiveDestinations();
		for (Iterator<ActiveMQDestination> iter = inactiveDests.iterator(); iter.hasNext();) {
			ActiveMQDestination dest = iter.next();
			ConnectionContext context = new ConnectionContext();
			context.setBroker(broker.getBrokerService().getBroker());
			context.setSecurityContext(SecurityContext.BROKER_SECURITY_CONTEXT);
			context.getBroker().addDestination(context, dest, false);
		}
		destinationsLock.readLock().lock();
		try {
			for (Iterator<Destination> i = destinations.values().iterator(); i.hasNext();) {
				Destination dest = i.next();
				dest.start();
			}
		} finally {
			destinationsLock.readLock().unlock();
		}
	}
	@Override
	public void stop() throws Exception {
		started = false;
		destinationsLock.readLock().lock();
		try {
			for (Iterator<Destination> i = destinations.values().iterator(); i.hasNext();) {
				Destination dest = i.next();
				dest.stop();
			}
		} finally {
			destinationsLock.readLock().unlock();
		}
		destinationsLock.writeLock().lock();
		try {
			destinations.clear();
			regionStatistics.getAdvisoryDestinations().reset();
			regionStatistics.getDestinations().reset();
			regionStatistics.getAllDestinations().reset();
		} finally {
			destinationsLock.writeLock().unlock();
		}
	}
	@Override
	/**
	 * 创建一个和入参destination对应的Destination对象
	 */
	public Destination addDestination(ConnectionContext context, ActiveMQDestination destination,
			boolean createIfTemporary) throws Exception {
		destinationsLock.writeLock().lock();
		try {
			Destination dest = destinations.get(destination);
			if (dest == null) {
				if (destination.isTemporary() == false || createIfTemporary) {
					// Limit the number of destinations that can be created if
					// maxDestinations has been set on a policy
					// 对于master的标记信息，下面这一步什么都没做
					validateMaxDestinations(destination);
					LOG.debug("{} adding destination: {}", broker.getBrokerName(), destination);
					// 得到一个与入参的destination相对应的Queue或者Topic对象
					dest = createDestination(context, destination);
					// intercept if there is a valid interceptor defined
					// 得到Destination过滤器，过滤器可以对消息的地址进行过滤
					DestinationInterceptor destinationInterceptor = broker.getDestinationInterceptor();
					if (destinationInterceptor != null) {
						dest = destinationInterceptor.intercept(dest);
					}
					dest.start();// 启动Topic或者Queue对象
					destinations.put(destination, dest);
					updateRegionDestCounts(destination, 1);// 消息个数加1
					destinationMap.put(destination, dest);
					addSubscriptionsForDestination(context, dest);
				}
				if (dest == null) {
					throw new DestinationDoesNotExistException(destination.getQualifiedName());
				}
			}
			return dest;
		} finally {
			destinationsLock.writeLock().unlock();
		}
	}
	public Map<ConsumerId, Subscription> getSubscriptions() {
		return subscriptions;
	}
	/**
	 * Updates the counts in RegionStatistics based on whether or not the destination is an Advisory
	 * Destination or not
	 * @param destination the destination being used to determine which counters to update
	 * @param count the count to add to the counters
	 */
	/**
	 * 增加入参destination对应的RegionDestination个数
	 * @Title: updateRegionDestCounts
	 * @Description: TODO
	 * @param destination
	 * @param count
	 * @return: void
	 */
	protected void updateRegionDestCounts(ActiveMQDestination destination, int count) {
		if (destination != null) {
			if (AdvisorySupport.isAdvisoryTopic(destination)) {
				regionStatistics.getAdvisoryDestinations().add(count);
			} else {
				regionStatistics.getDestinations().add(count);
			}
			regionStatistics.getAllDestinations().add(count);
		}
	}
	/**
	 * This method checks whether or not the destination can be created based on
	 * {@link PolicyEntry#getMaxDestinations}, if it has been set. Advisory topics are ignored.
	 * @param destination
	 * @throws Exception
	 */
	protected void validateMaxDestinations(ActiveMQDestination destination) throws Exception {
		if (broker.getDestinationPolicy() != null) {
			// 得到与入参相一致的策略，这个和xml文件中相对应的节点是对应的
			PolicyEntry entry = broker.getDestinationPolicy().getEntryFor(destination);
			// Make sure the destination is not an advisory topic
			if (entry != null && entry.getMaxDestinations() >= 0 && !AdvisorySupport.isAdvisoryTopic(destination)) {
				// If there is an entry for this destination, look up the set of
				// destinations associated with this policy
				// If a destination isn't specified, then just count up
				// non-advisory destinations (ie count all destinations)
				int destinationSize = (int) (entry.getDestination() != null ? destinationMap
						.get(entry.getDestination()).size() : regionStatistics.getDestinations().getCount());
				if (destinationSize >= entry.getMaxDestinations()) {
					if (entry.getDestination() != null) {
						throw new IllegalStateException("The maxmimum number of destinations allowed ("
								+ entry.getMaxDestinations() + ") for the policy " + entry.getDestination()
								+ " has already been reached.");
						// No destination has been set (default policy)
					} else {
						throw new IllegalStateException("The maxmimum number of destinations allowed ("
								+ entry.getMaxDestinations() + ") has already been reached.");
					}
				}
			}
		}
	}
	/**
	 * 查看目前所有的订阅者中有没有地址与入参的Destination地址相匹配的，如果有则将订阅者加入到入参Destination中
	 * @Title: addSubscriptionsForDestination
	 * @Description: TODO
	 * @param context
	 * @param dest
	 * @return
	 * @throws Exception
	 * @return: List<Subscription>
	 */
	protected List<Subscription> addSubscriptionsForDestination(ConnectionContext context, Destination dest)
			throws Exception {
		List<Subscription> rc = new ArrayList<Subscription>();
		// Add all consumers that are interested in the destination.
		for (Iterator<Subscription> iter = subscriptions.values().iterator(); iter.hasNext();) {
			Subscription sub = iter.next();
			if (sub.matches(dest.getActiveMQDestination())) {
				try {
					ConnectionContext originalContext = sub.getContext() != null ? sub.getContext() : context;
					dest.addSubscription(originalContext, sub);
					rc.add(sub);
				} catch (SecurityException e) {
					if (sub.isWildcard()) {
						LOG.debug("Subscription denied for " + sub + " to destination " + dest.getActiveMQDestination()
								+ ": " + e.getMessage());
					} else {
						throw e;
					}
				}
			}
		}
		return rc;
	}
	@Override
	public void removeDestination(ConnectionContext context, ActiveMQDestination destination, long timeout)
			throws Exception {
		// No timeout.. then try to shut down right way, fails if there are
		// current subscribers.
		if (timeout == 0) {
			for (Iterator<Subscription> iter = subscriptions.values().iterator(); iter.hasNext();) {
				Subscription sub = iter.next();
				if (sub.matches(destination)) {
					throw new JMSException("Destination still has an active subscription: " + destination);
				}
			}
		}
		if (timeout > 0) {
			// TODO: implement a way to notify the subscribers that we want to
			// take the down
			// the destination and that they should un-subscribe.. Then wait up
			// to timeout time before
			// dropping the subscription.
		}
		LOG.debug("{} removing destination: {}", broker.getBrokerName(), destination);
		destinationsLock.writeLock().lock();
		try {
			Destination dest = destinations.remove(destination);
			if (dest != null) {
				updateRegionDestCounts(destination, -1);
				// timeout<0 or we timed out, we now force any remaining
				// subscriptions to un-subscribe.
				for (Iterator<Subscription> iter = subscriptions.values().iterator(); iter.hasNext();) {
					Subscription sub = iter.next();
					if (sub.matches(destination)) {
						dest.removeSubscription(context, sub, 0l);
					}
				}
				destinationMap.remove(destination, dest);
				dispose(context, dest);
				DestinationInterceptor destinationInterceptor = broker.getDestinationInterceptor();
				if (destinationInterceptor != null) {
					destinationInterceptor.remove(dest);
				}
			} else {
				LOG.debug("Cannot remove a destination that doesn't exist: {}", destination);
			}
		} finally {
			destinationsLock.writeLock().unlock();
		}
	}
	/**
	 * Provide an exact or wildcard lookup of destinations in the region
	 * @return a set of matching destination objects.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Set<Destination> getDestinations(ActiveMQDestination destination) {
		destinationsLock.readLock().lock();
		try {
			return destinationMap.get(destination);
		} finally {
			destinationsLock.readLock().unlock();
		}
	}
	@Override
	public Map<ActiveMQDestination, Destination> getDestinationMap() {
		return destinations;
	}
	@Override
	@SuppressWarnings("unchecked")
	/**
	 * 首先创建info对应的Topic或者Queue对象，前提是没有，然后创建对应的订阅者Subscription对象，并将订阅者对象加入到Topic对象或者queue对象中
	 */
	public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
		LOG.debug("{} adding consumer: {} for destination: {}",
				new Object[] { broker.getBrokerName(), info.getConsumerId(), info.getDestination() });
		ActiveMQDestination destination = info.getDestination();
		if (destination != null && !destination.isPattern() && !destination.isComposite()) {
			// lets auto-create the destination
			// 从destinations找到destination对应的对象，如果没有就创建一个对应的Topic或者Queue对象
			lookup(context, destination, true);
		}
		Object addGuard;
		synchronized (consumerChangeMutexMap) {
			addGuard = consumerChangeMutexMap.get(info.getConsumerId());
			if (addGuard == null) {
				addGuard = new Object();
				consumerChangeMutexMap.put(info.getConsumerId(), addGuard);
			}
		}
		synchronized (addGuard) {
			// 找到消费者对应的对象
			Subscription o = subscriptions.get(info.getConsumerId());
			if (o != null) {
				LOG.warn("A duplicate subscription was detected. Clients may be misbehaving. Later warnings you may see about subscription removal are a consequence of this.");
				return o;
			}
			// We may need to add some destinations that are in persistent store
			// but not active
			// in the broker.
			//
			// TODO: think about this a little more. This is good cause
			// destinations are not loaded into
			// memory until a client needs to use the queue, but a management
			// agent viewing the
			// broker will not see a destination that exists in persistent
			// store. We may want to
			// eagerly load all destinations into the broker but have an
			// inactive state for the
			// destination which has reduced memory usage.
			//
			// 算是一个空实现
			DestinationFilter.parseFilter(info.getDestination());
			Subscription sub = createSubscription(context, info);
			// At this point we're done directly manipulating subscriptions,
			// but we need to retain the synchronized block here. Consider
			// otherwise what would happen if at this point a second
			// thread added, then removed, as would be allowed with
			// no mutex held. Remove is only essentially run once
			// so everything after this point would be leaked.
			// Add the subscription to all the matching queues.
			// But copy the matches first - to prevent deadlocks
			List<Destination> addList = new ArrayList<Destination>();
			destinationsLock.readLock().lock();
			try {
				// 获取到与info中的destination对象对应的所有的destination对象
				// 新创建出Topic或者Queue对象在上面的lookup方法中已经加入到destinationMap中了
				for (Destination dest : (Set<Destination>) destinationMap.get(info.getDestination())) {
					addList.add(dest);
				}
			} finally {
				destinationsLock.readLock().unlock();
			}
			List<Destination> removeList = new ArrayList<Destination>();
			for (Destination dest : addList) {
				try {
					// 将订阅者加入到所有的destination对象中
					dest.addSubscription(context, sub);
					removeList.add(dest);
				} catch (SecurityException e) {
					if (sub.isWildcard()) {
						LOG.debug("Subscription denied for " + sub + " to destination " + dest.getActiveMQDestination()
								+ ": " + e.getMessage());
					} else {
						// remove partial subscriptions
						for (Destination remove : removeList) {
							try {
								remove.removeSubscription(context, sub, info.getLastDeliveredSequenceId());
							} catch (Exception ex) {
								LOG.error("Error unsubscribing " + sub + " from " + remove + ": " + ex.getMessage(), ex);
							}
						}
						throw e;
					}
				}
			}
			removeList.clear();
			if (info.isBrowser()) {
				((QueueBrowserSubscription) sub).destinationsAdded();
			}
			subscriptions.put(info.getConsumerId(), sub);
			return sub;
		}
	}
	/**
	 * Get all the Destinations that are in storage
	 * @return Set of all stored destinations
	 */
	@SuppressWarnings("rawtypes")
	public Set getDurableDestinations() {
		return destinationFactory.getDestinations();
	}
	/**
	 * @return all Destinations that don't have active consumers
	 */
	/**
	 * 返回没有活跃的消费者的Destination集合
	 * @Title: getInactiveDestinations
	 * @Description: TODO
	 * @return
	 * @return: Set<ActiveMQDestination>
	 */
	protected Set<ActiveMQDestination> getInactiveDestinations() {
		Set<ActiveMQDestination> inactiveDests = destinationFactory.getDestinations();
		destinationsLock.readLock().lock();
		try {
			inactiveDests.removeAll(destinations.keySet());
		} finally {
			destinationsLock.readLock().unlock();
		}
		return inactiveDests;
	}
	@Override
	@SuppressWarnings("unchecked")
	public void removeConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
		LOG.debug("{} removing consumer: {} for destination: {}",
				new Object[] { broker.getBrokerName(), info.getConsumerId(), info.getDestination() });
		Subscription sub = subscriptions.remove(info.getConsumerId());// 移除消息的订阅者
		// The sub could be removed elsewhere - see ConnectionSplitBroker
		if (sub != null) {
			// remove the subscription from all the matching queues.
			List<Destination> removeList = new ArrayList<Destination>();
			destinationsLock.readLock().lock();
			try {
				for (Destination dest : (Set<Destination>) destinationMap.get(info.getDestination())) {
					removeList.add(dest);
				}
			} finally {
				destinationsLock.readLock().unlock();
			}
			for (Destination dest : removeList) {
				dest.removeSubscription(context, sub, info.getLastDeliveredSequenceId());
			}
			destroySubscription(sub);
		}
		synchronized (consumerChangeMutexMap) {
			consumerChangeMutexMap.remove(info.getConsumerId());
		}
	}
	protected void destroySubscription(Subscription sub) {
		sub.destroy();
	}
	@Override
	public void removeSubscription(ConnectionContext context, RemoveSubscriptionInfo info) throws Exception {
		throw new JMSException("Invalid operation.");
	}
	@Override
	/**
	 * 消息发送
	 */
	public void send(final ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {
		final ConnectionContext context = producerExchange.getConnectionContext();
		if (producerExchange.isMutable() || producerExchange.getRegionDestination() == null) {
			// 找到一个对应的Topic或者Queue对象
			final Destination regionDestination = lookup(context, messageSend.getDestination(), false);
			producerExchange.setRegionDestination(regionDestination);
		}
		// 使用Topic或者Queue对象发送消息
		producerExchange.getRegionDestination().send(producerExchange, messageSend);
		if (producerExchange.getProducerState() != null && producerExchange.getProducerState().getInfo() != null) {
			// 生产者消息计数器加1
			producerExchange.getProducerState().getInfo().incrementSentCount();
		}
	}
	@Override
	public void acknowledge(ConsumerBrokerExchange consumerExchange, MessageAck ack) throws Exception {
		Subscription sub = consumerExchange.getSubscription();
		if (sub == null) {
			sub = subscriptions.get(ack.getConsumerId());
			if (sub == null) {
				if (!consumerExchange.getConnectionContext().isInRecoveryMode()) {
					LOG.warn("Ack for non existent subscription, ack: {}", ack);
					throw new IllegalArgumentException("The subscription does not exist: " + ack.getConsumerId());
				} else {
					LOG.debug("Ack for non existent subscription in recovery, ack: {}", ack);
					return;
				}
			}
			consumerExchange.setSubscription(sub);
		}
		sub.acknowledge(consumerExchange.getConnectionContext(), ack);
	}
	@Override
	public Response messagePull(ConnectionContext context, MessagePull pull) throws Exception {
		Subscription sub = subscriptions.get(pull.getConsumerId());
		if (sub == null) {
			throw new IllegalArgumentException("The subscription does not exist: " + pull.getConsumerId());
		}
		return sub.pullMessage(context, pull);
	}
	/**
	 * 从destinations找到destination对应的对象，如果没有就创建一个对应的Topic或者Queue对象
	 * @Title: lookup
	 * @Description: TODO
	 * @param context
	 * @param destination
	 * @param createTemporary
	 * @return
	 * @throws Exception
	 * @return: Destination
	 */
	protected Destination lookup(ConnectionContext context, ActiveMQDestination destination, boolean createTemporary)
			throws Exception {
		Destination dest = null;
		destinationsLock.readLock().lock();
		try {
			dest = destinations.get(destination);
		} finally {
			destinationsLock.readLock().unlock();
		}
		if (dest == null) {
			if (isAutoCreateDestinations()) {
				// Try to auto create the destination... re-invoke broker
				// from the
				// top so that the proper security checks are performed.
				// 如果找不到destination对应的topic或者queue，就创建一个新的
				context.getBroker().addDestination(context, destination, createTemporary);
				// 得到destination对应的topic或者queue
				dest = addDestination(context, destination, false);
				// We should now have the dest created.
				destinationsLock.readLock().lock();
				try {
					dest = destinations.get(destination);
				} finally {
					destinationsLock.readLock().unlock();
				}
			}
			if (dest == null) {
				throw new JMSException("The destination " + destination + " does not exist.");
			}
		}
		return dest;
	}
	@Override
	public void processDispatchNotification(MessageDispatchNotification messageDispatchNotification) throws Exception {
		Subscription sub = subscriptions.get(messageDispatchNotification.getConsumerId());
		if (sub != null) {
			sub.processMessageDispatchNotification(messageDispatchNotification);
		} else {
			throw new JMSException("Slave broker out of sync with master - Subscription: "
					+ messageDispatchNotification.getConsumerId() + " on "
					+ messageDispatchNotification.getDestination() + " does not exist for dispatch of message: "
					+ messageDispatchNotification.getMessageId());
		}
	}
	/*
	 * For a Queue/TempQueue, dispatch order is imperative to match acks, so the dispatch is
	 * deferred till the notification to ensure that the subscription chosen by the master is used.
	 * AMQ-2102
	 */
	protected void processDispatchNotificationViaDestination(MessageDispatchNotification messageDispatchNotification)
			throws Exception {
		Destination dest = null;
		destinationsLock.readLock().lock();
		try {
			dest = destinations.get(messageDispatchNotification.getDestination());
		} finally {
			destinationsLock.readLock().unlock();
		}
		if (dest != null) {
			dest.processDispatchNotification(messageDispatchNotification);
		} else {
			throw new JMSException("Slave broker out of sync with master - Destination: "
					+ messageDispatchNotification.getDestination() + " does not exist for consumer "
					+ messageDispatchNotification.getConsumerId() + " with message: "
					+ messageDispatchNotification.getMessageId());
		}
	}
	@Override
	public void gc() {
		for (Subscription sub : subscriptions.values()) {
			sub.gc();
		}
		destinationsLock.readLock().lock();
		try {
			for (Destination dest : destinations.values()) {
				dest.gc();
			}
		} finally {
			destinationsLock.readLock().unlock();
		}
	}
	protected abstract Subscription createSubscription(ConnectionContext context, ConsumerInfo info) throws Exception;
	/**
	 * 创建出一个Destination对象并进行一些配置
	 * @Title: createDestination
	 * @Description: TODO
	 * @param context
	 * @param destination
	 * @return
	 * @throws Exception
	 * @return: Destination
	 */
	protected Destination createDestination(ConnectionContext context, ActiveMQDestination destination)
			throws Exception {
		return destinationFactory.createDestination(context, destination, destinationStatistics);
	}
	public boolean isAutoCreateDestinations() {
		return autoCreateDestinations;
	}
	public void setAutoCreateDestinations(boolean autoCreateDestinations) {
		this.autoCreateDestinations = autoCreateDestinations;
	}
	@Override
	@SuppressWarnings("unchecked")
	public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {
		destinationsLock.readLock().lock();
		try {
			for (Destination dest : (Set<Destination>) destinationMap.get(info.getDestination())) {
				dest.addProducer(context, info);
			}
		} finally {
			destinationsLock.readLock().unlock();
		}
	}
	/**
	 * Removes a Producer.
	 * @param context the environment the operation is being executed under.
	 * @throws Exception TODO
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void removeProducer(ConnectionContext context, ProducerInfo info) throws Exception {
		destinationsLock.readLock().lock();
		try {
			for (Destination dest : (Set<Destination>) destinationMap.get(info.getDestination())) {
				dest.removeProducer(context, info);
			}
		} finally {
			destinationsLock.readLock().unlock();
		}
	}
	protected void dispose(ConnectionContext context, Destination dest) throws Exception {
		dest.dispose(context);
		dest.stop();
		destinationFactory.removeDestination(dest);
	}
	@Override
	public void processConsumerControl(ConsumerBrokerExchange consumerExchange, ConsumerControl control) {
		Subscription sub = subscriptions.get(control.getConsumerId());
		if (sub != null && sub instanceof AbstractSubscription) {
			((AbstractSubscription) sub).setPrefetchSize(control.getPrefetch());
			if (broker.getDestinationPolicy() != null) {
				PolicyEntry entry = broker.getDestinationPolicy().getEntryFor(control.getDestination());
				if (entry != null) {
					entry.configurePrefetch(sub);
				}
			}
			LOG.debug("setting prefetch: {}, on subscription: {}; resulting value: {}",
					new Object[] { control.getPrefetch(), control.getConsumerId(),
							sub.getConsumerInfo().getCurrentPrefetchSize() });
			try {
				lookup(consumerExchange.getConnectionContext(), control.getDestination(), false).wakeup();
			} catch (Exception e) {
				LOG.warn("failed to deliver post consumerControl dispatch-wakeup, to destination: {}",
						control.getDestination(), e);
			}
		}
	}
	@Override
	public void reapplyInterceptor() {
		destinationsLock.writeLock().lock();
		try {
			DestinationInterceptor destinationInterceptor = broker.getDestinationInterceptor();
			Map<ActiveMQDestination, Destination> map = getDestinationMap();
			for (ActiveMQDestination key : map.keySet()) {
				Destination destination = map.get(key);
				if (destination instanceof CompositeDestinationFilter) {
					destination = ((CompositeDestinationFilter) destination).next;
				}
				if (destinationInterceptor != null) {
					destination = destinationInterceptor.intercept(destination);
				}
				getDestinationMap().put(key, destination);
				Destination prev = destinations.put(key, destination);
				if (prev == null) {
					updateRegionDestCounts(key, 1);
				}
			}
		} finally {
			destinationsLock.writeLock().unlock();
		}
	}
}
