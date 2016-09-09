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

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.security.SecurityContext;
import org.apache.activemq.state.ProducerState;

/**
 * Utility class for broker operations
 */
public final class BrokerSupport {
	private BrokerSupport() {
	}
	public static void resendNoCopy(final ConnectionContext context, Message originalMessage,
			ActiveMQDestination deadLetterDestination) throws Exception {
		doResend(context, originalMessage, deadLetterDestination, false);
	}
	/**
	 * @param context
	 * @param originalMessage
	 * @param deadLetterDestination
	 * @throws Exception
	 */
	public static void resend(final ConnectionContext context, Message originalMessage,
			ActiveMQDestination deadLetterDestination) throws Exception {
		doResend(context, originalMessage, deadLetterDestination, true);
	}
	public static void doResend(final ConnectionContext context, Message originalMessage,
			ActiveMQDestination deadLetterDestination, boolean copy) throws Exception {
		Message message = copy ? originalMessage.copy() : originalMessage;
		message.setOriginalDestination(message.getDestination());
		message.setOriginalTransactionId(message.getTransactionId());
		message.setDestination(deadLetterDestination);
		message.setTransactionId(null);
		message.setMemoryUsage(null);
		message.setRedeliveryCounter(0);
		boolean originalFlowControl = context.isProducerFlowControl();
		try {
			context.setProducerFlowControl(false);
			ProducerInfo info = new ProducerInfo();
			ProducerState state = new ProducerState(info);
			ProducerBrokerExchange producerExchange = new ProducerBrokerExchange();
			producerExchange.setProducerState(state);
			producerExchange.setMutable(true);
			producerExchange.setConnectionContext(context);
			context.getBroker().send(producerExchange, message);
		} finally {
			context.setProducerFlowControl(originalFlowControl);
		}
	}
	/**
	 * Returns the broker's administration connection context used for configuring the broker at
	 * startup
	 */
	/**
	 * 首先查看入参broker对象中是否已经有ConnectionContext对象，如果没有，就创建一个简单的连接上下文对象，并设置到broker对象中
	 * @Title: getConnectionContext
	 * @Description: TODO
	 * @param broker
	 * @return
	 * @return: ConnectionContext
	 */
	public static ConnectionContext getConnectionContext(Broker broker) {
		// 获得broker对象的ConnectionContext连接上下文
		ConnectionContext adminConnectionContext = broker.getAdminConnectionContext();
		if (adminConnectionContext == null) {
			// 如果不存在，就根据broker创建一个，下面创建的这个上线文比较简单
			adminConnectionContext = createAdminConnectionContext(broker);
			// 将上线文设置到broker对象中
			broker.setAdminConnectionContext(adminConnectionContext);
		}
		return adminConnectionContext;
	}
	/**
	 * Factory method to create the new administration connection context object. Note this method
	 * is here rather than inside a default broker implementation to ensure that the broker
	 * reference inside it is the outer most interceptor
	 */
	/**
	 * 创建一个ConnectionContext连接上下文，并设置一个简单的安全上线文
	 * @Title: createAdminConnectionContext
	 * @Description: TODO
	 * @param broker
	 * @return
	 * @return: ConnectionContext
	 */
	protected static ConnectionContext createAdminConnectionContext(Broker broker) {
		// 创建一个连接上下文
		ConnectionContext context = new ConnectionContext();
		context.setBroker(broker);
		// 设置一个简单的安全上线文，这个安全上线文是空的
		context.setSecurityContext(SecurityContext.BROKER_SECURITY_CONTEXT);
		return context;
	}
}
