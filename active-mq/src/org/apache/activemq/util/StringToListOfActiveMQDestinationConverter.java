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
	/**
	 * @Title: convertToActiveMQDestination
	 * @Description: value��toString��������ֵ��������[]���������������[]�������ͷ���һ��null
	 *               toString��������ֵ�ԡ������ָÿ���ָ����Ԫ�ش���һ��ActiveMQDestination����
	 * @param value
	 * @return
	 * @return: List<ActiveMQDestination>
	 */
	public static List<ActiveMQDestination> convertToActiveMQDestination(Object value) {
		if (value == null) {
			return null;
		}
		// text must be enclosed with []
		// value������[]����
		String text = value.toString();
		if (text.startsWith("[") && text.endsWith("]")) {
			text = text.substring(1, text.length() - 1).trim();
			if (text.isEmpty()) {
				return null;
			}
			String[] array = text.split(",");
			List<ActiveMQDestination> list = new ArrayList<ActiveMQDestination>();
			for (String item : array) {
				// ����item��ֵ����QUEUE_TYPE�������ֶ����е�һ�֣����ȸ���item�Ŀ�ͷ�ַ���������Ӧ�Ķ��У�
				// ����������ϣ��ʹ���һ����ͨ�Ķ���
				list.add(ActiveMQDestination.createDestination(item.trim(), ActiveMQDestination.QUEUE_TYPE));
			}
			return list;
		} else {
			return null;
		}
	}
	//
	/**
	 * @Title: convertFromActiveMQDestination
	 * @Description: Ҫ��value��һ��List���󣬲���List�е�Ԫ�ض���ActiveMQDestination���͵�
	 *               Ȼ��ActiveMQDestination���͵Ķ���ת��ΪString���󣬲�ʹ�á�������Ϊ�ָ��ƴ����һ��
	 *               ���value����List���󣬻���List�е�����Ԫ�ض�����ActiveMQDestination���͵ģ���ô�ͷ���null
	 * @param value
	 * @return
	 * @return: String
	 */
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