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

import java.util.TimerTask;

/**
 * A TimeTask for a Runnable object
 *
 */
/**
 * TimerTask的子类，很简单的实现，里面只有一个Runnable属性，可以定时执行Runnable中的run方法，一个RUnnable对应一个本对象
 * @ClassName: SchedulerTimerTask
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午9:46:38
 */
public class SchedulerTimerTask extends TimerTask {
	private final Runnable task;

	public SchedulerTimerTask(Runnable task) {
		this.task = task;
	}
	public void run() {
		this.task.run();
	}
}
