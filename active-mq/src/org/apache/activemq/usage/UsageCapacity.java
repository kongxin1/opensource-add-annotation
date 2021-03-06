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
 Identify if a limit has been reached
 * 
 * @org.apache.xbean.XBean
 * 
 * 
 */
/**
 * 存储某一个有限制值的属性，并提供了查询这个属性的方法，以及和这个属性的比较方法
 * @ClassName: UsageCapacity
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月9日 下午6:03:22
 */
public interface UsageCapacity {
	/**
	 * Has the limit been reached ?
	 * @param size
	 * @return true if it has
	 */
	boolean isLimit(long size);
	/**
	 * @return the limit
	 */
	long getLimit();
	/**
	 * @param limit the limit to set
	 */
	void setLimit(long limit);
}
