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
package org.apache.activemq.broker.scheduler.memory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.activemq.broker.scheduler.JobScheduler;
import org.apache.activemq.broker.scheduler.JobSchedulerStore;
import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.util.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory JobSchedulerStore implementation used for Brokers that have persistence
 * disabled or when the JobSchedulerStore usage doesn't require a file or DB based store
 * implementation allowing for better performance.
 */
/**
 * 用于管理内存定时任务调度器，内部有一个Map对象，存储了若干个定时任务调度器，可以对定时任务调度器进行启动，停止，删除，获得相应调度器等操作
 * @ClassName: InMemoryJobSchedulerStore
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月9日 上午9:02:55
 */
public class InMemoryJobSchedulerStore extends ServiceSupport implements JobSchedulerStore {
	private static final Logger LOG = LoggerFactory.getLogger(InMemoryJobSchedulerStore.class);
	private final ReentrantLock lock = new ReentrantLock();
	private final Map<String, InMemoryJobScheduler> schedulers = new HashMap<String, InMemoryJobScheduler>();

	@Override
	/**
	 * 终止本对象中的所有工作调度器
	 */
	protected void doStop(ServiceStopper stopper) throws Exception {
		for (InMemoryJobScheduler scheduler : schedulers.values()) {
			try {
				scheduler.stop();
			} catch (Exception e) {
				LOG.error("Failed to stop scheduler: {}", scheduler.getName(), e);
			}
		}
	}
	@Override
	/**
	 * 启动本对象中的所有工作调度器
	 */
	protected void doStart() throws Exception {
		for (InMemoryJobScheduler scheduler : schedulers.values()) {
			try {
				scheduler.start();
			} catch (Exception e) {
				LOG.error("Failed to start scheduler: {}", scheduler.getName(), e);
			}
		}
	}
	@Override
	/**
	 * 获得与入参对应的工作调度器，如果当前对象中不存在这样的调度器，那就创建一个，并存入本对象的调度器Map中
	 */
	public JobScheduler getJobScheduler(String name) throws Exception {
		this.lock.lock();
		try {
			InMemoryJobScheduler result = this.schedulers.get(name);
			if (result == null) {
				LOG.debug("Creating new in-memory scheduler: {}", name);
				result = new InMemoryJobScheduler(name);
				this.schedulers.put(name, result);
				if (isStarted()) {
					result.start();
				}
			}
			return result;
		} finally {
			this.lock.unlock();
		}
	}
	@Override
	/**
	 * 从Map对象中移除与入参对应的调度器
	 */
	public boolean removeJobScheduler(String name) throws Exception {
		boolean result = false;
		this.lock.lock();
		try {
			InMemoryJobScheduler scheduler = this.schedulers.remove(name);
			result = scheduler != null;
			if (result) {
				LOG.debug("Removing in-memory Job Scheduler: {}", name);
				scheduler.stop();
				this.schedulers.remove(name);
			}
		} finally {
			this.lock.unlock();
		}
		return result;
	}
	// ---------- Methods that don't really apply to this implementation ------//
	@Override
	public long size() {
		return 0;
	}
	@Override
	public File getDirectory() {
		return null;
	}
	@Override
	/**
	 * 内存中的调度器不需要目录
	 */
	public void setDirectory(File directory) {
	}
}
