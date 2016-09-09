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
package org.apache.activemq.state;

import org.apache.activemq.command.ConsumerInfo;

/**
 * 本对象中只保存了ConsumerInfo对象，这个ConsumerInfo对象是客户端传递过来的对象
 * @ClassName: ConsumerState
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月23日 下午9:56:42
 */
public class ConsumerState {
	final ConsumerInfo info;

	public ConsumerState(ConsumerInfo info) {
		this.info = info;
	}
	public String toString() {
		return info.toString();
	}
	public ConsumerInfo getInfo() {
		return info;
	}
}
