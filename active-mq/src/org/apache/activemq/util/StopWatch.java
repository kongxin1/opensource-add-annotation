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
package org.apache.activemq.util;

/**
 * A very simple stop watch.
 * <p/>
 * This implementation is not thread safe and can only time one task at any given time.
 */
/**
 * 记录开始时间和结束时间
 * @ClassName: StopWatch
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月14日 下午8:02:11
 */
public final class StopWatch {
	private long start;
	private long stop;

	/**
	 * Starts the stop watch
	 */
	public StopWatch() {
		this(true);
	}
	/**
	 * Creates the stop watch
	 * @param started whether it should start immediately
	 */
	public StopWatch(boolean started) {
		if (started) {
			restart();
		}
	}
	/**
	 * Starts or restarts the stop watch
	 */
	public void restart() {
		start = System.currentTimeMillis();
		stop = 0;
	}
	/**
	 * Stops the stop watch
	 * @return the time taken in milliseconds.
	 */
	public long stop() {
		stop = System.currentTimeMillis();
		return taken();
	}
	/**
	 * Returns the time taken in milliseconds.
	 * @return time in milliseconds
	 */
	/**
	 * 返回执行了多长时间
	 * @Title: taken
	 * @Description: TODO
	 * @return
	 * @return: long
	 */
	public long taken() {
		if (start > 0 && stop > 0) {
			return stop - start;
		} else if (start > 0) {
			return System.currentTimeMillis() - start;
		} else {
			return 0;
		}
	}
}
