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
package org.apache.activemq.transport.tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLEngine;

import org.apache.activemq.TransportLoggerSupport;
import org.apache.activemq.openwire.OpenWireFormat;
import org.apache.activemq.transport.InactivityMonitor;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFactory;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.WireFormatNegotiator;
import org.apache.activemq.transport.tcp.TcpTransport.InitBuffer;
import org.apache.activemq.util.IOExceptionSupport;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.URISupport;
import org.apache.activemq.wireformat.WireFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpTransportFactory extends TransportFactory {
	private static final Logger LOG = LoggerFactory.getLogger(TcpTransportFactory.class);

	/**
	 * 根据URI创建出ServerSocket，WireFormatFactory，并对ServerSocket进行IP,端口的绑定，同时还需要创建TcpTransportServer，
	 * 将上述两种对象注入TcpTransportServer中
	 * @Title: createTcpTransportServer
	 * @Description: TODO
	 * @param location
	 * @param serverSocketFactory
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @return: TcpTransportServer
	 */
	public TransportServer doBind(final URI location) throws IOException {
		try {
			// 得到uri中的参数
			Map<String, String> options = new HashMap<String, String>(URISupport.parseParameters(location));
			// 获得服务器套接字工厂
			ServerSocketFactory serverSocketFactory = createServerSocketFactory();
			// 获得TcpTransportServer对象
			TcpTransportServer server = createTcpTransportServer(location, serverSocketFactory);
			server.setWireFormatFactory(createWireFormatFactory(options));
			// 将参数设置到TcpTransportServer对象中
			IntrospectionSupport.setProperties(server, options);
			Map<String, Object> transportOptions = IntrospectionSupport.extractProperties(options, "transport.");
			server.setTransportOption(transportOptions);
			// 创建出一个对应于uri的ServerSocket对象
			server.bind();
			return server;
		} catch (URISyntaxException e) {
			throw IOExceptionSupport.create(e);
		}
	}
	/**
	 * Allows subclasses of TcpTransportFactory to create custom instances of TcpTransportServer.
	 * @param location
	 * @param serverSocketFactory
	 * @return a new TcpTransportServer instance.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	/**
	 * 根据入参创建TcpTransportServer对象
	 * @Title: createTcpTransportServer
	 * @Description: TODO
	 * @param location
	 * @param serverSocketFactory
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @return: TcpTransportServer
	 */
	protected TcpTransportServer createTcpTransportServer(final URI location, ServerSocketFactory serverSocketFactory)
			throws IOException, URISyntaxException {
		return new TcpTransportServer(this, location, serverSocketFactory);
	}
	@Override
	@SuppressWarnings("rawtypes")
	public Transport compositeConfigure(Transport transport, WireFormat format, Map options) {
		TcpTransport tcpTransport = transport.narrow(TcpTransport.class);
		IntrospectionSupport.setProperties(tcpTransport, options);
		Map<String, Object> socketOptions = IntrospectionSupport.extractProperties(options, "socket.");
		tcpTransport.setSocketOptions(socketOptions);
		if (tcpTransport.isTrace()) {
			try {
				transport = TransportLoggerSupport.createTransportLogger(transport, tcpTransport.getLogWriterName(),
						tcpTransport.isDynamicManagement(), tcpTransport.isStartLogging(), tcpTransport.getJmxPort());
			} catch (Throwable e) {
				LOG.error("Could not create TransportLogger object for: " + tcpTransport.getLogWriterName()
						+ ", reason: " + e, e);
			}
		}
		boolean useInactivityMonitor = "true".equals(getOption(options, "useInactivityMonitor", "true"));
		if (useInactivityMonitor && isUseInactivityMonitor(transport)) {
			transport = createInactivityMonitor(transport, format);
			IntrospectionSupport.setProperties(transport, options);
		}
		// Only need the WireFormatNegotiator if using openwire
		if (format instanceof OpenWireFormat) {
			transport = new WireFormatNegotiator(transport, (OpenWireFormat) format,
					tcpTransport.getMinmumWireFormatVersion());
		}
		return super.compositeConfigure(transport, format, options);
	}
	/**
	 * @return true if the inactivity monitor should be used on the transport
	 */
	protected boolean isUseInactivityMonitor(Transport transport) {
		return true;
	}
	@Override
	/**
	 * 得到Socket对象，并将空的socket对象设置到TcpTransport对象中
	 */
	protected Transport createTransport(URI location, WireFormat wf) throws UnknownHostException, IOException {
		URI localLocation = null;
		String path = location.getPath();
		// see if the path is a local URI location
		if (path != null && path.length() > 0) {
			int localPortIndex = path.indexOf(':');
			try {
				Integer.parseInt(path.substring(localPortIndex + 1, path.length()));
				String localString = location.getScheme() + ":/" + path;
				localLocation = new URI(localString);
			} catch (Exception e) {
				LOG.warn("path isn't a valid local location for TcpTransport to use", e.getMessage());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Failure detail", e);
				}
			}
		}
		SocketFactory socketFactory = createSocketFactory();
		return createTcpTransport(wf, socketFactory, location, localLocation);
	}
	public TcpTransport createTransport(WireFormat wireFormat, Socket socket, InitBuffer initBuffer) throws IOException {
		throw new IOException("createTransport() method not implemented!");
	}
	public TcpTransport createTransport(WireFormat wireFormat, Socket socket, SSLEngine engine, InitBuffer initBuffer,
			ByteBuffer inputBuffer) throws IOException {
		throw new IOException("createTransport() method not implemented!");
	}
	/**
	 * Allows subclasses of TcpTransportFactory to provide a create custom TcpTransport instances.
	 * @param wf
	 * @param socketFactory
	 * @param location
	 * @param localLocation
	 * @return a new TcpTransport instance connected to the given location.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	/**
	 * 创建出一个TcpTransport对象，在这个对象中封装了一个Sokcet对象，只不过这个对象现在还是空的
	 * @Title: createTcpTransport
	 * @Description: TODO
	 * @param wf
	 * @param socketFactory
	 * @param location
	 * @param localLocation
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @return: TcpTransport
	 */
	protected TcpTransport createTcpTransport(WireFormat wf, SocketFactory socketFactory, URI location,
			URI localLocation) throws UnknownHostException, IOException {
		return new TcpTransport(wf, socketFactory, location, localLocation);
	}
	/**
	 * 获得服务器套接字工厂，可以使用工厂得到服务器套接字
	 * @Title: createServerSocketFactory
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: ServerSocketFactory
	 */
	protected ServerSocketFactory createServerSocketFactory() throws IOException {
		return ServerSocketFactory.getDefault();
	}
	protected SocketFactory createSocketFactory() throws IOException {
		return SocketFactory.getDefault();
	}
	protected Transport createInactivityMonitor(Transport transport, WireFormat format) {
		return new InactivityMonitor(transport, format);
	}
}
