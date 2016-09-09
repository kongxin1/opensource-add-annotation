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

import java.io.IOException;
import java.net.URI;

import org.apache.activemq.util.FactoryFinder;
import org.apache.activemq.util.IOExceptionSupport;

/**
 * A helper class to create a fully configured broker service using a URI. The list of currently
 * supported URI syntaxes is described <a
 * href="http://activemq.apache.org/how-do-i-embed-a-broker-inside-a-connection.html">here</a>
 */
public final class BrokerFactory {
	// 工厂类搜寻器
	// 根据broker目录下的文件加载不同的BrokerFactoryHandler类
	private static final FactoryFinder BROKER_FACTORY_HANDLER_FINDER = new FactoryFinder(
			"META-INF/services/org/apache/activemq/broker/");

	private BrokerFactory() {
	}
	/**
	 * @Title: createBrokerFactoryHandler
	 * @Description: 创建出XBeanBrokerFactory对象并返回，这个对象是在配置文件中配置的
	 * @param type
	 * @return
	 * @throws IOException
	 * @return: BrokerFactoryHandler
	 */
	public static BrokerFactoryHandler createBrokerFactoryHandler(String type) throws IOException {
		try {
			return (BrokerFactoryHandler) BROKER_FACTORY_HANDLER_FINDER.newInstance(type);
		} catch (Throwable e) {
			throw IOExceptionSupport.create("Could not load " + type + " factory:" + e, e);
		}
	}
	/**
	 * @Title: createBroker
	 * @Description:调用createBroker(brokerURI, 
	 *                                        false)方法，默认情况下，broker不启动，默认时，brokerURI的值是xbean:activemq
	 *                                        .xml
	 * @param brokerURI
	 * @return
	 * @throws Exception
	 * @return: BrokerService
	 */
	public static BrokerService createBroker(URI brokerURI) throws Exception {
		return createBroker(brokerURI, false);
	}
	/**
	 * Creates a broker from a URI configuration
	 * @param brokerURI the URI scheme to configure the broker
	 * @param startBroker whether or not the broker should have its {@link BrokerService#start()}
	 *            method called after construction
	 * @throws Exception
	 */
	/**
	 * 根据brokerURI的schema获得对应的DefaultBrokerFactory对象，这个对象是在配置文件中指定的，然后通过这个对象加载activemq.xml配置文件，
	 * 并获得BrokerService对象，如果BrokerService未启动，那么就调用start方法启动，然后返回启动的BrokerService对象
	 * @Title: createBroker
	 * @Description: TODO
	 * @param brokerURI
	 * @param startBroker
	 * @return
	 * @throws Exception
	 * @return: BrokerService
	 */
	public static BrokerService createBroker(URI brokerURI, boolean startBroker) throws Exception {
		if (brokerURI.getScheme() == null) {
			throw new IllegalArgumentException("Invalid broker URI, no scheme specified: " + brokerURI);
		}
		// 获得DefaultBrokerFactory对象
		BrokerFactoryHandler handler = createBrokerFactoryHandler(brokerURI.getScheme());
		BrokerService broker = handler.createBroker(brokerURI);
		if (startBroker) {
			broker.start();
		}
		return broker;
	}
	/**
	 * Creates a broker from a URI configuration
	 * @param brokerURI the URI scheme to configure the broker
	 * @throws Exception
	 */
	public static BrokerService createBroker(String brokerURI) throws Exception {
		return createBroker(new URI(brokerURI));
	}
	/**
	 * Creates a broker from a URI configuration
	 * @param brokerURI the URI scheme to configure the broker
	 * @param startBroker whether or not the broker should have its {@link BrokerService#start()}
	 *            method called after construction
	 * @throws Exception
	 */
	public static BrokerService createBroker(String brokerURI, boolean startBroker) throws Exception {
		return createBroker(new URI(brokerURI), startBroker);
	}

	private static final ThreadLocal<Boolean> START_DEFAULT = new ThreadLocal<Boolean>();

	public static void setStartDefault(boolean startDefault) {
		START_DEFAULT.set(startDefault);
	}
	public static void resetStartDefault() {
		START_DEFAULT.remove();
	}
	public static boolean getStartDefault() {
		Boolean value = START_DEFAULT.get();
		if (value == null) {
			return true;
		}
		return value.booleanValue();
	}
}
