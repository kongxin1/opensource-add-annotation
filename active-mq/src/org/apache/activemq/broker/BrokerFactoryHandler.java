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
package org.apache.activemq.broker;

import java.net.URI;

/**
 * Represents the interface used to create a broker from a URI scheme.
 *
 * 
 */
/**
 * ͨ��������Դ�����Ӧ��BrokerService����DefaultBrokerFactoryʵ���˸ýӿ�
 * @ClassName: BrokerFactoryHandler
 * @Description: ����brokerURI������Ӧ��BrokerService
 * @author: ����
 * @date: 2016��7��3�� ����12:22:37
 */
public interface BrokerFactoryHandler {
	BrokerService createBroker(URI brokerURI) throws Exception;
}