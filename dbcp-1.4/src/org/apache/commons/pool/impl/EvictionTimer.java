/*
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
package org.apache.commons.pool.impl;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * Provides a shared idle object eviction timer for all pools. This class wraps the standard
 * {@link Timer} and keeps track of how many pools are using it. If no pools are using the timer, it
 * is canceled. This prevents a thread being left running which, in application server environments,
 * can lead to memory leads and/or prevent applications from shutting down or reloading cleanly.
 * </p>
 * <p>
 * This class has package scope to prevent its inclusion in the pool public API. The class
 * declaration below should *not* be changed to public.
 * </p>
 */
class EvictionTimer {
	/** Timer instance */
	private static Timer _timer;
	/** Static usage count tracker */
	// 定时任务的个数
	private static int _usageCount;

	/** Prevent instantiation */
	private EvictionTimer() {
		// Hide the default constuctor
	}
	/**
	 * Add the specified eviction task to the timer. Tasks that are added with a call to this method
	 * *must* call {@link #cancel(TimerTask)} to cancel the task to prevent memory and/or thread
	 * leaks in application server environments.
	 * @param task Task to be scheduled
	 * @param delay Delay in milliseconds before task is executed
	 * @param period Time in milliseconds between executions
	 */
	static synchronized void schedule(TimerTask task, long delay, long period) {
		if (null == _timer) {
			// Timer是一个定时器，TimerTask是一个定时器任务，TimerTask是放在Timer中执行的
			_timer = new Timer(true);
		}
		_usageCount++;
		// delay表示延迟delay长的时间后开始执行，在延迟delay长的时间后，以后就是按照每过period长的时间执行一次，在以后的执行中，delay就不在起作用了
		_timer.schedule(task, delay, period);
	}
	/**
	 * Remove the specified eviction task from the timer.
	 * @param task Task to be scheduled
	 */
	/**
	 * 对定时任务作出一些清除工作
	 * @Title: cancel
	 * @Description: TODO
	 * @param task
	 * @return: void
	 */
	static synchronized void cancel(TimerTask task) {
		task.cancel();
		_usageCount--;
		if (_usageCount == 0) {
			// 取消并删除定时器
			_timer.cancel();
			_timer = null;
		}
	}
}
