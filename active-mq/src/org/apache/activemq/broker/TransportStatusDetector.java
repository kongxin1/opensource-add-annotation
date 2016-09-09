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

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.activemq.Service;
import org.apache.activemq.ThreadPriorities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to provide information on the status of the Connection
 */
public class TransportStatusDetector implements Service, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(TransportStatusDetector.class);
	private TransportConnector connector;
	private Set<TransportConnection> collectionCandidates = new CopyOnWriteArraySet<TransportConnection>();
	private AtomicBoolean started = new AtomicBoolean(false);
	private Thread runner;
	private int sweepInterval = 5000;

	TransportStatusDetector(TransportConnector connector) {
		this.connector = connector;
	}
	/**
	 * @return Returns the sweepInterval.
	 */
	public int getSweepInterval() {
		return sweepInterval;
	}
	/**
	 * The sweepInterval to set.
	 * @param sweepInterval
	 */
	public void setSweepInterval(int sweepInterval) {
		this.sweepInterval = sweepInterval;
	}
	/**
	 * 根据TransportConnection对象的状态，是否从collectionCandidates集合中删除该对象
	 * @Title: doCollection
	 * @Description: TODO
	 * @return: void
	 */
	protected void doCollection() {
		for (Iterator<TransportConnection> i = collectionCandidates.iterator(); i.hasNext();) {
			TransportConnection tc = i.next();
			if (tc.isMarkedCandidate()) {
				if (tc.isBlockedCandidate()) {
					collectionCandidates.remove(tc);
					doCollection(tc);
				} else {
					tc.doMark();
				}
			} else {
				collectionCandidates.remove(tc);
			}
		}
	}
	/**
	 * 将connector
	 * @Title: doSweep
	 * @Description: TODO
	 * @return: void
	 */
	protected void doSweep() {
		for (Iterator i = connector.getConnections().iterator(); i.hasNext();) {
			TransportConnection connection = (TransportConnection) i.next();
			if (connection.isMarkedCandidate()) {
				connection.doMark();
				collectionCandidates.add(connection);
			}
		}
	}
	/**
	 * 调用入参的stop方法，终止tc的运行
	 * @Title: doCollection
	 * @Description: TODO
	 * @param tc
	 * @return: void
	 */
	protected void doCollection(TransportConnection tc) {
		LOG.warn("found a blocked client - stopping: {}", tc);
		try {
			tc.stop();
		} catch (Exception e) {
			LOG.error("Error stopping {}", tc, e);
		}
	}
	/**
	 * 后台运行本方法，每sweepInterval长时间运行一次
	 */
	public void run() {
		while (started.get()) {
			try {
				doCollection();
				doSweep();
				Thread.sleep(sweepInterval);
			} catch (Throwable e) {
				LOG.error("failed to complete a sweep for blocked clients", e);
			}
		}
	}
	/**
	 * 启动本对象，处理线程是后台运行，调用本对象中的run方法
	 */
	public void start() throws Exception {
		if (started.compareAndSet(false, true)) {
			runner = new Thread(this, "ActiveMQ Transport Status Monitor: " + connector);
			runner.setDaemon(true);
			runner.setPriority(ThreadPriorities.BROKER_MANAGEMENT);
			runner.start();
		}
	}
	public void stop() throws Exception {
		started.set(false);
		if (runner != null) {
			runner.join(getSweepInterval() * 5);
		}
	}
}
