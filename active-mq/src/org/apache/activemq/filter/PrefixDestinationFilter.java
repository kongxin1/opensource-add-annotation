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
package org.apache.activemq.filter;

import org.apache.activemq.command.ActiveMQDestination;

/**
 * Matches messages which match a prefix like "A.B.>"
 */
/**
 * 是一个地址过滤器，对destination的地址进行判断是否匹配
 * @ClassName: PrefixDestinationFilter
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月16日 下午9:10:53
 */
public class PrefixDestinationFilter extends DestinationFilter {
	private String[] prefixes;
	private byte destinationType;

	/**
	 * An array of paths, the last path is '>'
	 * @param prefixes
	 */
	/**
	 * 将prefixes中‘>’这个元素之前的所有内容复制到本对象的字符串数组中，尽管多复制了两个，但是在匹配的时候多复制的这两个是不使用的
	 * @Title:PrefixDestinationFilter
	 * @Description:TODO
	 * @param prefixes
	 * @param destinationType
	 */
	public PrefixDestinationFilter(String[] prefixes, byte destinationType) {
		// collapse duplicate '>' at the end of the path
		int lastIndex = prefixes.length - 1;
		while (lastIndex >= 0 && ANY_DESCENDENT.equals(prefixes[lastIndex])) {
			lastIndex--;
		}
		this.prefixes = new String[lastIndex + 2];
		System.arraycopy(prefixes, 0, this.prefixes, 0, this.prefixes.length);
		this.destinationType = destinationType;
	}
	public boolean matches(ActiveMQDestination destination) {
		if (destination.getDestinationType() != destinationType)
			return false;
		String[] path = DestinationPath.getDestinationPaths(destination.getPhysicalName());
		int length = prefixes.length;
		if (path.length >= length) {
			int size = length - 1;
			// 入参的地址和内部地址的每一个都作比较
			for (int i = 0; i < size; i++) {
				// 判断是否匹配，只要有一个不匹配的就返回false
				if (!matches(prefixes[i], path[i])) {
					return false;
				}
			}
			return true;
		} else {
			// want to look for the case where A matches A.>
			boolean match = true;
			// 每一个都做比较，判断是否匹配
			for (int i = 0; (i < path.length && match); i++) {
				match = matches(prefixes[i], path[i]);
			}
			// paths get compacted - e.g. A.*.> will be compacted to A.> and by definition - the
			// last element on
			// the prefix will be >
			// 需要内部的prefixes的长度要比入参的地址长度大一才行
			if (match && prefixes.length == (path.length + 1)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 判断入参path是不是匹配。
	 * @Title: matches
	 * @Description: TODO
	 * @param prefix
	 * @param path
	 * @return
	 * @return: boolean
	 */
	private boolean matches(String prefix, String path) {
		return path.equals(ANY_CHILD) || prefix.equals(ANY_CHILD) || prefix.equals(path);
	}
	public String getText() {
		return DestinationPath.toString(prefixes);
	}
	public String toString() {
		return super.toString() + "[destination: " + getText() + "]";
	}
	public boolean isWildcard() {
		return true;
	}
}
