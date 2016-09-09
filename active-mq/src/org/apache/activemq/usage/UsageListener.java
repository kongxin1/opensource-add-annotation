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
package org.apache.activemq.usage;

/**
 * Usage对象的监听器接口，在Usage对象中有监听器集合，可以注册进去，当Usage对象中已经使用量的百分比值percentUsage更新时，会调用监听器
 * @ClassName: UsageListener
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月10日 下午10:50:17
 */
public interface UsageListener {
	void onUsageChanged(Usage usage, int oldPercentUsage, int newPercentUsage);
}
