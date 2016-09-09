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
 * Matches messages which contain wildcards like "A.B.*.*"
 */
public class WildcardDestinationFilter extends DestinationFilter {
	private String[] prefixes;
	private byte destinationType;

	/**
	 * An array of paths containing * characters
	 * @param prefixes
	 */
	/**
	 * 将去掉了‘*’后的prefixes中的所有元素放入本对象的prefixes中
	 * @Title:WildcardDestinationFilter
	 * @Description:TODO
	 * @param prefixes
	 * @param destinationType
	 */
	public WildcardDestinationFilter(String[] prefixes, byte destinationType) {
		this.prefixes = new String[prefixes.length];
		for (int i = 0; i < prefixes.length; i++) {
			String prefix = prefixes[i];
			if (!prefix.equals("*")) {
				this.prefixes[i] = prefix;
			}
		}
		this.destinationType = destinationType;
	}
	/**
	 * 入参中的地址必须和本对象中的prefixes中的元素完全一致，只要有不符 就返回false
	 */
	public boolean matches(ActiveMQDestination destination) {
		if (destination.getDestinationType() != destinationType)
			return false;
		String[] path = DestinationPath.getDestinationPaths(destination);
		int length = prefixes.length;
		if (path.length == length) {
			for (int i = 0; i < length; i++) {
				String prefix = prefixes[i];
				if (prefix != null && !prefix.equals(path[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
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
