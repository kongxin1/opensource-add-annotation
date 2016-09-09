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
package org.apache.activemq.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
/**
 * 使用一个线程运行Task
 * @ClassName: DedicatedTaskRunner
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月14日 下午8:28:24
 */
class DedicatedTaskRunner implements TaskRunner {
	private static final Logger LOG = LoggerFactory.getLogger(DedicatedTaskRunner.class);
	private final Task task;
	private final Thread thread;
	private final Object mutex = new Object();
	// 表示Task是否运行完毕
	private boolean threadTerminated;
	private boolean pending;
	// 是否关闭
	private boolean shutdown;

	/**
	 * 创建一个线程运行指定的task任务
	 * @Title:DedicatedTaskRunner
	 * @Description:TODO
	 * @param task
	 * @param name
	 * @param priority
	 * @param daemon
	 */
	public DedicatedTaskRunner(final Task task, String name, int priority, boolean daemon) {
		this.task = task;
		thread = new Thread(name) {
			@Override
			public void run() {
				try {
					runTask();
				} finally {
					LOG.trace("Run task done: {}", task);
				}
			}
		};
		thread.setDaemon(daemon);
		thread.setName(name);
		thread.setPriority(priority);
		thread.start();
	}
	/**
     */
	@Override
	/**
	 * 唤醒线程，对于本对象来说，调用下面的这个方法，会使runTask方法运行结束，也就是使线程运行结束
	 */
	public void wakeup() throws InterruptedException {
		synchronized (mutex) {
			if (shutdown) {
				return;
			}
			pending = true;
			mutex.notifyAll();
		}
	}
	/**
	 * shut down the task
	 * @param timeout
	 * @throws InterruptedException
	 */
	@Override
	/**
	 * 重新设置某些属性，表示本对象已经关闭，但不会主动的关闭线程
	 */
	public void shutdown(long timeout) throws InterruptedException {
		LOG.trace("Shutdown timeout: {} task: {}", timeout, task);
		synchronized (mutex) {
			shutdown = true;
			pending = true;
			mutex.notifyAll();
			// Wait till the thread stops ( no need to wait if shutdown
			// is called from thread that is shutting down)
			if (Thread.currentThread() != thread && !threadTerminated) {
				mutex.wait(timeout);
			}
		}
	}
	/**
	 * shut down the task
	 * @throws InterruptedException
	 */
	@Override
	public void shutdown() throws InterruptedException {
		shutdown(0);
	}
	/**
	 * 调用task的iterate方法，下面的这个方法如果不调用wakeup方法或者shutdown方法，会一直保持阻塞
	 * @Title: runTask
	 * @Description: TODO
	 * @return: void
	 */
	final void runTask() {
		try {
			while (true) {
				synchronized (mutex) {
					pending = false;
					if (shutdown) {
						return;
					}
				}
				LOG.trace("Running task {}", task);
				if (!task.iterate()) {
					// wait to be notified.
					synchronized (mutex) {
						if (shutdown) {
							return;
						}
						while (!pending) {
							mutex.wait();
						}
					}
				}
			}
		} catch (InterruptedException e) {
			// Someone really wants this thread to die off.
			Thread.currentThread().interrupt();
		} finally {
			// Make sure we notify any waiting threads that thread
			// has terminated.
			synchronized (mutex) {
				threadTerminated = true;
				mutex.notifyAll();
			}
		}
	}
}
