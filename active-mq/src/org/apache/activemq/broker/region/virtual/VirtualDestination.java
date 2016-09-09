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
package org.apache.activemq.broker.region.virtual;

import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.command.ActiveMQDestination;

/**
 * Represents some kind of virtual destination.
 */
/**
 * 虚拟的Destination
 * @ClassName: VirtualDestination
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月13日 下午9:23:09
 */
public interface VirtualDestination extends DestinationInterceptor {
	/**
	 * Returns the virtual destination
	 */
	ActiveMQDestination getVirtualDestination();
	/**
	 * Creates a virtual destination from the physical destination
	 */
	@Override
	Destination intercept(Destination destination);
	/**
	 * Returns mapped destination(s)
	 */
	ActiveMQDestination getMappedDestinations();
	/**
	 * Creates a mapped destination
	 */
	Destination interceptMappedDestination(Destination destination);
}
