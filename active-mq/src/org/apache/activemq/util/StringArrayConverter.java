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
import java.util.StringTokenizer;

/**
 * Class for converting to/from String[] to be used instead of a {@link java.beans.PropertyEditor}
 * which otherwise causes memory leaks as the JDK {@link java.beans.PropertyEditorManager} is a
 * static class and has strong references to classes, causing problems in hot-deployment
 * environments.
 */
public class StringArrayConverter {
	/**
	 * @ClassName: StringArrayConverter
	 * @Description: ��valueת��Ϊ��ӦString���飬Ҫ��value��toString��������ֵ���ԡ������ָ�ָ����ÿһ��ֵ��ΪString������ ��һ��Ԫ��
	 * @author: ����
	 * @date: 2016��7��1�� ����10:08:33
	 */
	public static String[] convertToStringArray(Object value) {
		if (value == null) {
			return null;
		}
		String text = value.toString();
		if (text == null || text.length() == 0) {
			return null;
		}
		StringTokenizer stok = new StringTokenizer(text, ",");
		final List<String> list = new ArrayList<String>();
		while (stok.hasMoreTokens()) {
			list.add(stok.nextToken());
		}
		String[] array = list.toArray(new String[list.size()]);
		return array;
	}
	/**
	 * @Title: convertToString
	 * @Description: ���ڽ�valueת��ΪString����ʵ���ǽ������е�ֱֵ��ƴ�ӳ�һ���ַ������м�ʹ�á������ָ�
	 * @param value
	 * @return
	 * @return: String
	 */
	public static String convertToString(String[] value) {
		if (value == null || value.length == 0) {
			return null;
		}
		StringBuffer result = new StringBuffer(String.valueOf(value[0]));
		for (int i = 1; i < value.length; i++) {
			result.append(",").append(value[i]);
		}
		return result.toString();
	}
}
