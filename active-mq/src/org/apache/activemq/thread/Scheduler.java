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
 * ����һ�������������������һ��Timer��ʱ�������Զ�ʱִ�����񣬿�����Ϊ�ǽ�Timer�����˷�װ��ʹ֮������Է���Ĵ���ָ���Ķ�ʱ����
 * @ClassName: Scheduler
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��13�� ����9:54:31
 */
public final class Scheduler extends ServiceSupport {
	private final String name;
	private Timer timer;
	private final HashMap<Runnable, TimerTask> timerTasks = new HashMap<Runnable, TimerTask>();

	public Scheduler(String name) {
		this.name = name;
	}
	/**
	 * ������������ִ�е����񣬲���������뵽�������Map��
	 * @Title: executePeriodically
	 * @Description: TODO
	 * @param task
	 * @param period
	 * @return: void
	 */
	public synchronized void executePeriodically(final Runnable task, long period) {
		TimerTask timerTask = new SchedulerTimerTask(task);
		// ����ָ���������ָ�����ӳٺ�ʼ�����ظ��Ĺ̶�����ִ�С�
		timer.schedule(timerTask, period, period);
		// ��Runnable�����TimerTask���뵽�������map��
		timerTasks.put(task, timerTask);
	}
	/**
	 * ȡ��ָ�������ִ��
	 * @Title: cancel
	 * @Description: TODO
	 * @param task
	 * @return: void
	 */
	public synchronized void cancel(Runnable task) {
		TimerTask ticket = timerTasks.remove(task);
		if (ticket != null) {
			ticket.cancel();
			// ɾ���Ѿ�ȡ��������
			timer.purge(); // remove cancelled TimerTasks
		}
	}
	/**
	 * ����һ���ڹ̶��ӳ�֮��ִ�е�����������񲻻���뵽Map�����У���Ϊִֻ��һ��
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
