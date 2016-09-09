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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.util.ServiceSupport;

/**
 * 这是一个任务调度器，里面有一个Timer定时器，可以定时执行任务，可以认为是将Timer进行了封装，使之对外可以方便的创建指定的定时任务
 * @ClassName: Scheduler
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午9:54:31
 */
public final class Scheduler extends ServiceSupport {
	private final String name;
	private Timer timer;
	private final HashMap<Runnable, TimerTask> timerTasks = new HashMap<Runnable, TimerTask>();

	public Scheduler(String name) {
		this.name = name;
	}
	/**
	 * 创建可以周期执行的任务，并将任务加入到本对象的Map中
	 * @Title: executePeriodically
	 * @Description: TODO
	 * @param task
	 * @param period
	 * @return: void
	 */
	public synchronized void executePeriodically(final Runnable task, long period) {
		TimerTask timerTask = new SchedulerTimerTask(task);
		// 安排指定的任务从指定的延迟后开始进行重复的固定周期执行。
		timer.schedule(timerTask, period, period);
		// 将Runnable对象和TimerTask加入到本对象的map中
		timerTasks.put(task, timerTask);
	}
	/**
	 * 取消指定任务的执行
	 * @Title: cancel
	 * @Description: TODO
	 * @param task
	 * @return: void
	 */
	public synchronized void cancel(Runnable task) {
		TimerTask ticket = timerTasks.remove(task);
		if (ticket != null) {
			ticket.cancel();
			// 删除已经取消的任务
			timer.purge(); // remove cancelled TimerTasks
		}
	}
	/**
	 * 创建一个在固定延迟之后执行的任务，这个任务不会加入到Map对象中，因为只执行一次
	 * @Title: executeAfterDelay
	 * @Description: TODO
	 * @param task
	 * @param redeliveryDelay
	 * @return: void
	 */
	public synchronized void executeAfterDelay(final Runnable task, long redeliveryDelay) {
		TimerTask timerTask = new SchedulerTimerTask(task);
		timer.schedule(timerTask, redeliveryDelay);
	}
	public void shutdown() {
		timer.cancel();
	}
	@Override
	protected synchronized void doStart() throws Exception {
		this.timer = new Timer(name, true);
	}
	@Override
	protected synchronized void doStop(ServiceStopper stopper) throws Exception {
		if (this.timer != null) {
			this.timer.cancel();
		}
	}
	public String getName() {
		return name;
	}
}
