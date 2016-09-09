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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.activemq.usage.SystemUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
/**
 * @ClassName: AbstractTempRegion
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午11:13:14
 */
public abstract class AbstractTempRegion extends AbstractRegion {
	private static final Logger LOG = LoggerFactory.getLogger(TempQueueRegion.class);
	private Map<CachedDestination, Destination> cachedDestinations = new HashMap<CachedDestination, Destination>();
	// 是否缓存临时Destination对象
	private final boolean doCacheTempDestinations;
	private final int purgeTime;
	private Timer purgeTimer;
	private TimerTask purgeTask;

	/**
	 * @param broker
	 * @param destinationStatistics
	 * @param memoryManager
	 * @param taskRunnerFactory
	 * @param destinationFactory
	 */
	public AbstractTempRegion(RegionBroker broker, DestinationStatistics destinationStatistics,
			SystemUsage memoryManager, TaskRunnerFactory taskRunnerFactory, DestinationFactory destinationFactory) {
		super(broker, destinationStatistics, memoryManager, taskRunnerFactory, destinationFactory);
		this.doCacheTempDestinations = broker.getBrokerService().isCacheTempDestinations();
		this.purgeTime = broker.getBrokerService().getTimeBeforePurgeTempDestinations();
		if (this.doCacheTempDestinations) {
			// 如果需要清理临时Destination对象
			this.purgeTimer = new Timer("ActiveMQ Temp destination purge timer", true);
			this.purgeTask = new TimerTask() {
				public void run() {
					// 定时销毁缓存Destination对象
					doPurge();
				}
			};
			this.purgeTimer.schedule(purgeTask, purgeTime, purgeTime);
		}
	}
	public void stop() throws Exception {
		super.stop();
		if (purgeTimer != null) {
			purgeTimer.cancel();
		}
	}
	protected synchronized Destination createDestination(ConnectionContext context, ActiveMQDestination destination)
			throws Exception {
		Destination result = cachedDestinations.remove(new CachedDestination(destination));
		if (result == null) {
			result = destinationFactory.createDestination(context, destination, destinationStatistics);
		}
		return result;
	}
	protected final synchronized void dispose(ConnectionContext context, Destination dest) throws Exception {
		// add to cache
		if (this.doCacheTempDestinations) {
			cachedDestinations.put(new CachedDestination(dest.getActiveMQDestination()), dest);
		} else {
			try {
				dest.dispose(context);
				dest.stop();
			} catch (Exception e) {
				LOG.warn("Failed to dispose of {}", dest, e);
			}
		}
	}
	/**
	 * 销毁指定的Destination对象
	 * @Title: doDispose
	 * @Description: TODO
	 * @param dest
	 * @return: void
	 */
	private void doDispose(Destination dest) {
		ConnectionContext context = new ConnectionContext();
		try {
			dest.dispose(context);
			dest.stop();
		} catch (Exception e) {
			LOG.warn("Failed to dispose of {}", dest, e);
		}
	}
	/**
	 * 缓存的Destination对象只能存活一段时间，超过这段时间就从缓存集合中删除，然后销毁这个对象
	 * @Title: doPurge
	 * @Description: TODO
	 * @return: void
	 */
	private synchronized void doPurge() {
		long currentTime = System.currentTimeMillis();
		if (cachedDestinations.size() > 0) {
			Set<CachedDestination> tmp = new HashSet<CachedDestination>(cachedDestinations.keySet());
			for (CachedDestination key : tmp) {
				// 缓存的Destination对象只能存活一段时间，超过了就需要清除
				if ((key.timeStamp + purgeTime) < currentTime) {
					// 将Destination对象从缓存的Destination集合中删除
					Destination dest = cachedDestinations.remove(key);
					// 销毁Destination对象
					if (dest != null) {
						doDispose(dest);
					}
				}
			}
		}
	}

	static class CachedDestination {
		// 该对象的创建时间
		long timeStamp;
		ActiveMQDestination destination;

		CachedDestination(ActiveMQDestination destination) {
			this.destination = destination;
			this.timeStamp = System.currentTimeMillis();
		}
		public int hashCode() {
			return destination.hashCode();
		}
		public boolean equals(Object o) {
			if (o instanceof CachedDestination) {
				CachedDestination other = (CachedDestination) o;
				return other.destination.equals(this.destination);
			}
			return false;
		}
	}
}
