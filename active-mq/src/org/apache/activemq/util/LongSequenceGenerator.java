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
 * 可以获取一个自增的long型整数序列，保证唯一
 * @ClassName: LongSequenceGenerator
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午10:46:18
 */
public class LongSequenceGenerator {
	private long lastSequenceId;

	public synchronized long getNextSequenceId() {
		return ++lastSequenceId;
	}
	public synchronized long getLastSequenceId() {
		return lastSequenceId;
	}
	public synchronized void setLastSequenceId(long l) {
		lastSequenceId = l;
	}
}
