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

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.command.ActiveMQDestination;

/**
 * Special converter for String -> List<ActiveMQDestination> to be used instead of a
 * {@link java.beans.PropertyEditor} which otherwise causes memory leaks as the JDK
 * {@link java.beans.PropertyEditorManager} is a static class and has strong references to classes,
 * causing problems in hot-deployment environments.
 */
public class StringToListOfActiveMQDestinationConverter {
	// value的toString方法返回值必须是以[]包裹，如果不是以[]包裹，就返回一个null
	// toString方法返回值以’，‘分割，每个分割出的元素创建一个ActiveMQDestination对象
	public static List<ActiveMQDestination> convertToActiveMQDestination(Object value) {
		if (value == null) {
			return null;
		}
		// text must be enclosed with []
		// value必须以[]包裹
		String text = value.toString();
		if (text.startsWith("[") && text.endsWith("]")) {
			text = text.substring(1, text.length() - 1).trim();
			if (text.isEmpty()) {
				return null;
			}
			String[] array = text.split(",");
			List<ActiveMQDestination> list = new ArrayList<ActiveMQDestination>();
			for (String item : array) {
				// 根据item的值或者QUEUE_TYPE创建四种队列中的一种，首先根据item的开头字符串创建对应的队列，
				// 如果都不符合，就创建一个普通的队列
				list.add(ActiveMQDestination.createDestination(item.trim(), ActiveMQDestination.QUEUE_TYPE));
			}
			return list;
		} else {
			return null;
		}
	}
	// 要求value是一个List对象，并且List中的元素都是ActiveMQDestination类型的
	// 然后将ActiveMQDestination类型的对象转化为String对象，并使用’，‘作为分割符拼接在一起
	// 如果value不是List对象，或者List中的所有元素都不是ActiveMQDestination类型的，那么就返回null
	public static String convertFromActiveMQDestination(Object value) {
		if (value == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder("[");
		if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				Object e = list.get(i);
				if (e instanceof ActiveMQDestination) {
					ActiveMQDestination destination = (ActiveMQDestination) e;
					sb.append(destination);
					if (i < list.size() - 1) {
						sb.append(", ");
					}
				}
			}
		}
		sb.append("]");
		if (sb.length() > 2) {
			return sb.toString();
		} else {
			return null;
		}
	}
}
