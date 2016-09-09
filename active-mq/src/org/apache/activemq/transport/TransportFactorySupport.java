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
package org.apache.activemq.transport;

import java.io.IOException;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.BrokerServiceAware;
import org.apache.activemq.broker.SslContext;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
/**
 * 根据URI创建出TransportFactory对象，并将brokerService对象注入前者，同时TransportFactory对象创建出TransportServer对象，并完成IP，
 * 端口的绑定
 * @ClassName: TransportFactorySupport
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月3日 下午10:48:01
 */
public class TransportFactorySupport {
	public static TransportServer bind(BrokerService brokerService, URI location) throws IOException {
		// 获得对应的TransportFactory对象，如果schema是tcp，那么对象就是org.apache.activemq.transport.tcp.TcpTransportFactory
		TransportFactory tf = TransportFactory.findTransportFactory(location);
		// 如果该TransportFactory对象实现了BrokerServiceAware接口，那么就将brokerService对象注入进去
		// 所以实现了BrokerServiceAware接口的对象注入brokerService对象是在这里注入的，不是在创建对象时注入的
		if (brokerService != null && tf instanceof BrokerServiceAware) {
			((BrokerServiceAware) tf).setBrokerService(brokerService);
		}
		try {
			if (brokerService != null) {
				// 使用默认的配置文件，brokerService.getSslContext()返回null，SSLContext是安全套接字
				SslContext.setCurrentSslContext(brokerService.getSslContext());
			}
			// 根据URI创建出ServerSocket，WireFormatFactory，并对ServerSocket进行IP,端口的绑定，
			// 同时还需要创建TcpTransportServer，将上述两种对象注入TcpTransportServer中
			return tf.doBind(location);
		} finally {
			// 这一步操作不知道是什么作用
			SslContext.setCurrentSslContext(null);
		}
	}
}
