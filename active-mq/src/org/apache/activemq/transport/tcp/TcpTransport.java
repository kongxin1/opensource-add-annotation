/**
gxfdgvdfg * Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;

import org.apache.activemq.Service;
import org.apache.activemq.TransportLoggerSupport;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportThreadSupport;
import org.apache.activemq.util.InetAddressUtil;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.wireformat.WireFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link Transport} interface using raw tcp/ip
 * @author David Martin Clavo david(dot)martin(dot)clavo(at)gmail.com (logging improvement
 *         modifications)
 */
/**
 * 一个对象对应一个客户端，一个socket对象对应一个本对象，一个socket就直接链接着客户端
 * @ClassName: TcpTransport
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月22日 下午9:43:51
 */
public class TcpTransport extends TransportThreadSupport implements Transport, Service, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(TcpTransport.class);
	protected final URI remoteLocation;
	protected final URI localLocation;
	protected final WireFormat wireFormat;
	protected int connectionTimeout = 30000;
	protected int soTimeout;
	protected int socketBufferSize = 64 * 1024;
	protected int ioBufferSize = 8 * 1024;
	protected boolean closeAsync = true;
	protected Socket socket;
	protected DataOutputStream dataOut;// 输出流
	protected DataInputStream dataIn;// 输入流
	protected TimeStampStream buffOut = null;// 带写入底层流时间的输出流
	protected final InitBuffer initBuffer;
	/**
	 * The Traffic Class to be set on the socket.
	 */
	protected int trafficClass = 0;
	/**
	 * Keeps track of attempts to set the Traffic Class on the socket.
	 */
	// 表示是否设置了TrafficClass
	private boolean trafficClassSet = false;
	/**
	 * Prevents setting both the Differentiated Services and Type of Service transport options at
	 * the same time, since they share the same spot in the TCP/IP packet headers.
	 */
	protected boolean diffServChosen = false;
	protected boolean typeOfServiceChosen = false;
	/**
	 * trace=true -> the Transport stack where this TcpTransport object will be, will have a
	 * TransportLogger layer trace=false -> the Transport stack where this TcpTransport object will
	 * be, will NOT have a TransportLogger layer, and therefore will never be able to print logging
	 * messages. This parameter is most probably set in Connection or TransportConnector URIs.
	 */
	protected boolean trace = false;
	/**
	 * Name of the LogWriter implementation to use. Names are mapped to classes in the
	 * resources/META-INF/services/org/apache/activemq/transport/logwriters directory. This
	 * parameter is most probably set in Connection or TransportConnector URIs.
	 */
	protected String logWriterName = TransportLoggerSupport.defaultLogWriterName;
	/**
	 * Specifies if the TransportLogger will be manageable by JMX or not. Also, as long as there is
	 * at least 1 TransportLogger which is manageable, a TransportLoggerControl MBean will me
	 * created.
	 */
	protected boolean dynamicManagement = false;
	/**
	 * startLogging=true -> the TransportLogger object of the Transport stack will initially write
	 * messages to the log. startLogging=false -> the TransportLogger object of the Transport stack
	 * will initially NOT write messages to the log. This parameter only has an effect if trace ==
	 * true. This parameter is most probably set in Connection or TransportConnector URIs.
	 */
	protected boolean startLogging = true;
	/**
	 * Specifies the port that will be used by the JMX server to manage the TransportLoggers. This
	 * should only be set in an URI by a client (producer or consumer) since a broker will already
	 * create a JMX server. It is useful for people who test a broker and clients in the same
	 * machine and want to control both via JMX; a different port will be needed.
	 */
	protected int jmxPort = 1099;
	protected boolean useLocalHost = false;
	protected int minmumWireFormatVersion;
	protected SocketFactory socketFactory;
	protected final AtomicReference<CountDownLatch> stoppedLatch = new AtomicReference<CountDownLatch>();
	protected volatile int receiveCounter;// 记录从底层流对象中读取数据的次数
	private Map<String, Object> socketOptions;
	private int soLinger = Integer.MIN_VALUE;
	private Boolean keepAlive;
	private Boolean tcpNoDelay;
	private Thread runnerThread;// 记录本对象中线程的启动时间，就是run方法的运行时间

	/**
	 * Connect to a remote Node - e.g. a Broker
	 * @param wireFormat
	 * @param socketFactory
	 * @param remoteLocation
	 * @param localLocation - e.g. local InetAddress and local port
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	/**
	 * 在下面这个方法中可以创建出一个空的socket
	 * @Title:TcpTransport
	 * @Description:TODO
	 * @param wireFormat
	 * @param socketFactory
	 * @param remoteLocation
	 * @param localLocation
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TcpTransport(WireFormat wireFormat, SocketFactory socketFactory, URI remoteLocation, URI localLocation)
			throws UnknownHostException, IOException {
		this.wireFormat = wireFormat;
		this.socketFactory = socketFactory;
		try {
			this.socket = socketFactory.createSocket();
		} catch (SocketException e) {
			this.socket = null;
		}
		this.remoteLocation = remoteLocation;
		this.localLocation = localLocation;
		this.initBuffer = null;
		setDaemon(false);
	}
	/**
	 * Initialize from a server Socket
	 * @param wireFormat
	 * @param socket
	 * @throws IOException
	 */
	public TcpTransport(WireFormat wireFormat, Socket socket) throws IOException {
		this(wireFormat, socket, null);
	}
	public TcpTransport(WireFormat wireFormat, Socket socket, InitBuffer initBuffer) throws IOException {
		this.wireFormat = wireFormat;
		this.socket = socket;
		this.remoteLocation = null;
		this.localLocation = null;
		this.initBuffer = initBuffer;
		setDaemon(true);
	}
	/**
	 * A one way asynchronous send 发送消息的方法
	 */
	@Override
	public void oneway(Object command) throws IOException {
		checkStarted();
		wireFormat.marshal(command, dataOut);
		dataOut.flush();
	}
	/**
	 * @return pretty print of 'this'
	 */
	@Override
	public String toString() {
		return ""
				+ (socket.isConnected() ? "tcp://" + socket.getInetAddress() + ":" + socket.getPort() + "@"
						+ socket.getLocalPort() : (localLocation != null ? localLocation : remoteLocation));
	}
	/**
	 * reads packets from a Socket
	 */
	@Override
	/**
	 * 获得服务器端返回的消息并进行处理，这是一个线程，不断的运行，不断的从socket的输入流中读取数据，构成Command对象
	 */
	public void run() {
		LOG.trace("TCP consumer thread for " + this + " starting");
		this.runnerThread = Thread.currentThread();
		try {
			while (!isStopped()) {
				doRun();
			}
		} catch (IOException e) {
			stoppedLatch.get().countDown();
			onException(e);
		} catch (Throwable e) {
			stoppedLatch.get().countDown();
			IOException ioe = new IOException("Unexpected error occurred: " + e);
			ioe.initCause(e);
			onException(ioe);
		} finally {
			stoppedLatch.get().countDown();
		}
	}
	/**
	 * 获得服务器端返回的消息并进行处理
	 * @Title: doRun
	 * @Description: TODO
	 * @throws IOException
	 * @return: void
	 */
	protected void doRun() throws IOException {
		try {
			// 读取服务器端返回的消息
			Object command = readCommand();
			// 通过监听器处理消息
			doConsume(command);
		} catch (SocketTimeoutException e) {
		} catch (InterruptedIOException e) {
		}
	}
	/**
	 * 根据底层流对象获得Message对象，这是读取了数据之后，一个一个设置上去的
	 * @Title: readCommand
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: Object
	 */
	protected Object readCommand() throws IOException {
		return wireFormat.unmarshal(dataIn);
	}
	// Properties
	// -------------------------------------------------------------------------
	public String getDiffServ() {
		// This is the value requested by the user by setting the Tcp Transport
		// options. If the socket hasn't been created, then this value may not
		// reflect the value returned by Socket.getTrafficClass().
		return Integer.toString(this.trafficClass);
	}
	public void setDiffServ(String diffServ) throws IllegalArgumentException {
		this.trafficClass = QualityOfServiceUtils.getDSCP(diffServ);
		this.diffServChosen = true;
	}
	public int getTypeOfService() {
		// This is the value requested by the user by setting the Tcp Transport
		// options. If the socket hasn't been created, then this value may not
		// reflect the value returned by Socket.getTrafficClass().
		return this.trafficClass;
	}
	public void setTypeOfService(int typeOfService) {
		this.trafficClass = QualityOfServiceUtils.getToS(typeOfService);
		this.typeOfServiceChosen = true;
	}
	public boolean isTrace() {
		return trace;
	}
	public void setTrace(boolean trace) {
		this.trace = trace;
	}
	public String getLogWriterName() {
		return logWriterName;
	}
	public void setLogWriterName(String logFormat) {
		this.logWriterName = logFormat;
	}
	public boolean isDynamicManagement() {
		return dynamicManagement;
	}
	public void setDynamicManagement(boolean useJmx) {
		this.dynamicManagement = useJmx;
	}
	public boolean isStartLogging() {
		return startLogging;
	}
	public void setStartLogging(boolean startLogging) {
		this.startLogging = startLogging;
	}
	public int getJmxPort() {
		return jmxPort;
	}
	public void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}
	public int getMinmumWireFormatVersion() {
		return minmumWireFormatVersion;
	}
	public void setMinmumWireFormatVersion(int minmumWireFormatVersion) {
		this.minmumWireFormatVersion = minmumWireFormatVersion;
	}
	public boolean isUseLocalHost() {
		return useLocalHost;
	}
	/**
	 * Sets whether 'localhost' or the actual local host name should be used to make local
	 * connections. On some operating systems such as Macs its not possible to connect as the local
	 * host name so localhost is better.
	 */
	public void setUseLocalHost(boolean useLocalHost) {
		this.useLocalHost = useLocalHost;
	}
	public int getSocketBufferSize() {
		return socketBufferSize;
	}
	/**
	 * Sets the buffer size to use on the socket
	 */
	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
	}
	public int getSoTimeout() {
		return soTimeout;
	}
	/**
	 * Sets the socket timeout
	 */
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	/**
	 * Sets the timeout used to connect to the socket
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public Boolean getKeepAlive() {
		return keepAlive;
	}
	/**
	 * Enable/disable TCP KEEP_ALIVE mode
	 */
	public void setKeepAlive(Boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	/**
	 * Enable/disable soLinger
	 * @param soLinger enabled if > -1, disabled if == -1, system default otherwise
	 */
	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}
	public int getSoLinger() {
		return soLinger;
	}
	public Boolean getTcpNoDelay() {
		return tcpNoDelay;
	}
	/**
	 * Enable/disable the TCP_NODELAY option on the socket
	 */
	public void setTcpNoDelay(Boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}
	/**
	 * @return the ioBufferSize
	 */
	public int getIoBufferSize() {
		return this.ioBufferSize;
	}
	/**
	 * @param ioBufferSize the ioBufferSize to set
	 */
	public void setIoBufferSize(int ioBufferSize) {
		this.ioBufferSize = ioBufferSize;
	}
	/**
	 * @return the closeAsync
	 */
	public boolean isCloseAsync() {
		return closeAsync;
	}
	/**
	 * @param closeAsync the closeAsync to set
	 */
	public void setCloseAsync(boolean closeAsync) {
		this.closeAsync = closeAsync;
	}
	// Implementation methods
	// -------------------------------------------------------------------------
	protected String resolveHostName(String host) throws UnknownHostException {
		if (isUseLocalHost()) {
			String localName = InetAddressUtil.getLocalHostName();
			if (localName != null && localName.equals(host)) {
				return "localhost";
			}
		}
		return host;
	}
	/**
	 * Configures the socket for use
	 * @param sock the socket
	 * @throws SocketException, IllegalArgumentException if setting the options on the socket
	 *             failed.
	 */
	/**
	 * 设置sokcet的属性
	 * @Title: initialiseSocket
	 * @Description: TODO
	 * @param sock
	 * @throws SocketException
	 * @throws IllegalArgumentException
	 * @return: void
	 */
	protected void initialiseSocket(Socket sock) throws SocketException, IllegalArgumentException {
		// 如果socketOptions不为null，就将里面的属性设置到socket中
		if (socketOptions != null) {
			// copy the map as its used values is being removed when calling setProperties
			// and we need to be able to set the options again in case socket is re-initailized
			Map<String, Object> copy = new HashMap<String, Object>(socketOptions);
			IntrospectionSupport.setProperties(socket, copy);
			if (!copy.isEmpty()) {
				throw new IllegalArgumentException("Invalid socket parameters: " + copy);
			}
		}
		try {
			sock.setReceiveBufferSize(socketBufferSize);
			sock.setSendBufferSize(socketBufferSize);
		} catch (SocketException se) {
			LOG.warn("Cannot set socket buffer size = " + socketBufferSize);
			LOG.debug("Cannot set socket buffer size. Reason: " + se.getMessage() + ". This exception is ignored.", se);
		}
		// 设置超时时间
		sock.setSoTimeout(soTimeout);
		if (keepAlive != null) {
			// 设置持久连接
			sock.setKeepAlive(keepAlive.booleanValue());
		}
		if (soLinger > -1) {
			/*
			 * 设置该选项: public void setSoLinger(boolean on, int seconds) throws SocketException;
			 * 读取该选项：public int getSoLinger() throws SocketException
			 * SO_LINGER选项用来控制Socket关闭时的行为，默认情况下
			 * ，执行Socket的close方法，该方法会立即返回，但底层的Socket实际上并不会立即关闭，他会立即延迟一段时间
			 * ，知道发送完剩余的数据，才会真正的关闭Socket，断开连接。 setSoLinger（true, 0):
			 * 执行该方法，那么执行Socket的close方法，该方法也会立即返回，但底层的Socket也会立即关闭，所有未发送完的剩余数据被丢弃 setSoLinger(true,
			 * 3600):
			 * 那么执行Socket的close方法，该方法不会立即返回，而进入阻塞状态，同时，底层的Socket也会尝试发送剩余的数据，只有满足下面的两个条件之一，close方法才会返回
			 * ： （1）：底层的Socket已经发送完所有的剩余数据 （2）：
			 * 尽管底层的Socket还没有发送完所有的剩余数据，但已经阻塞了3600秒，close()方法的阻塞时间超过3600秒，也会返回，剩余未发送的数据被丢弃。
			 */
			sock.setSoLinger(true, soLinger);
		} else if (soLinger == -1) {
			sock.setSoLinger(false, 0);
		}
		if (tcpNoDelay != null) {
			sock.setTcpNoDelay(tcpNoDelay.booleanValue());
		}
		if (!this.trafficClassSet) {
			this.trafficClassSet = setTrafficClass(sock);
		}
	}
	@Override
	/**
	 * 与服务器建立链接，并调用父类的启动方法，这个方法会调用本对象的run方法
	 */
	protected void doStart() throws Exception {
		connect();
		stoppedLatch.set(new CountDownLatch(1));
		super.doStart();
	}
	/**
	 * 使用socket创建与服务器的链接，并设置socket，创建socket的输入流和输出流
	 * @Title: connect
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	protected void connect() throws Exception {
		if (socket == null && socketFactory == null) {
			throw new IllegalStateException("Cannot connect if the socket or socketFactory have not been set");
		}
		InetSocketAddress localAddress = null;
		InetSocketAddress remoteAddress = null;
		// 根据不同的address，按不同的方式创建不同的InetSocketAddress对象
		if (localLocation != null) {
			localAddress = new InetSocketAddress(InetAddress.getByName(localLocation.getHost()),
					localLocation.getPort());
		}
		if (remoteLocation != null) {
			String host = resolveHostName(remoteLocation.getHost());
			remoteAddress = new InetSocketAddress(host, remoteLocation.getPort());
		}
		// Set the traffic class before the socket is connected when possible so
		// that the connection packets are given the correct traffic class.
		this.trafficClassSet = setTrafficClass(socket);
		if (socket != null) {
			// Sokcet绑定地址
			if (localAddress != null) {
				socket.bind(localAddress);
			}
			// If it's a server accepted socket.. we don't need to connect it
			// to a remote address.
			if (remoteAddress != null) {
				if (connectionTimeout >= 0) {
					// 将此socket连接到服务器上
					socket.connect(remoteAddress, connectionTimeout);
				} else {
					socket.connect(remoteAddress);
				}
			}
		} else {
			// For SSL sockets.. you can't create an unconnected socket :(
			// This means the timout option are not supported either.
			if (localAddress != null) {
				socket = socketFactory.createSocket(remoteAddress.getAddress(), remoteAddress.getPort(),
						localAddress.getAddress(), localAddress.getPort());
			} else {
				socket = socketFactory.createSocket(remoteAddress.getAddress(), remoteAddress.getPort());
			}
		}
		// 设置socket的属性
		initialiseSocket(socket);
		// 创建socket对象需要的输入流和输出流，这两个流都带有缓存
		initializeStreams();
	}
	@Override
	protected void doStop(ServiceStopper stopper) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Stopping transport " + this);
		}
		// Closing the streams flush the sockets before closing.. if the socket
		// is hung.. then this hangs the close.
		// closeStreams();
		if (socket != null) {
			if (closeAsync) {
				// closing the socket can hang also
				final CountDownLatch latch = new CountDownLatch(1);
				// need a async task for this
				final TaskRunnerFactory taskRunnerFactory = new TaskRunnerFactory();
				taskRunnerFactory.execute(new Runnable() {
					@Override
					public void run() {
						LOG.trace("Closing socket {}", socket);
						try {
							socket.close();
							LOG.debug("Closed socket {}", socket);
						} catch (IOException e) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Caught exception closing socket " + socket
										+ ". This exception will be ignored.", e);
							}
						} finally {
							latch.countDown();
						}
					}
				});
				try {
					latch.await(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					taskRunnerFactory.shutdownNow();
				}
			} else {
				// close synchronously
				LOG.trace("Closing socket {}", socket);
				try {
					socket.close();
					LOG.debug("Closed socket {}", socket);
				} catch (IOException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Caught exception closing socket " + socket + ". This exception will be ignored.", e);
					}
				}
			}
		}
	}
	/**
	 * Override so that stop() blocks until the run thread is no longer running.
	 */
	@Override
	public void stop() throws Exception {
		super.stop();
		CountDownLatch countDownLatch = stoppedLatch.get();
		if (countDownLatch != null && Thread.currentThread() != this.runnerThread) {
			countDownLatch.await(1, TimeUnit.SECONDS);
		}
	}
	/**
	 * 创建socket对象的输入流和输出流
	 * @Title: initializeStreams
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	protected void initializeStreams() throws Exception {
		// 创建一个可以缓存数据的对象
		TcpBufferedInputStream buffIn = new TcpBufferedInputStream(socket.getInputStream(), ioBufferSize) {
			@Override
			public int read() throws IOException {
				receiveCounter++;
				return super.read();
			}
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				receiveCounter++;
				return super.read(b, off, len);
			}
			@Override
			public long skip(long n) throws IOException {
				receiveCounter++;
				return super.skip(n);
			}
			@Override
			protected void fill() throws IOException {
				receiveCounter++;
				super.fill();
			}
		};
		// Unread the initBuffer that was used for protocol detection if it exists
		// so the stream can start over
		if (initBuffer != null) {
			// 将入参中的数据读入到流对象的缓存数组中
			buffIn.unread(initBuffer.buffer.array());
		}
		// 创建一个输入流
		this.dataIn = new DataInputStream(buffIn);
		TcpBufferedOutputStream outputStream = new TcpBufferedOutputStream(socket.getOutputStream(), ioBufferSize);
		// 创建一个输出流
		this.dataOut = new DataOutputStream(outputStream);
		this.buffOut = outputStream;
	}
	protected void closeStreams() throws IOException {
		if (dataOut != null) {
			dataOut.close();
		}
		if (dataIn != null) {
			dataIn.close();
		}
	}
	public void setSocketOptions(Map<String, Object> socketOptions) {
		this.socketOptions = new HashMap<String, Object>(socketOptions);
	}
	@Override
	public String getRemoteAddress() {
		if (socket != null) {
			SocketAddress address = socket.getRemoteSocketAddress();
			if (address instanceof InetSocketAddress) {
				return "tcp://" + ((InetSocketAddress) address).getAddress().getHostAddress() + ":"
						+ ((InetSocketAddress) address).getPort();
			} else {
				return "" + socket.getRemoteSocketAddress();
			}
		}
		return null;
	}
	@Override
	public <T> T narrow(Class<T> target) {
		if (target == Socket.class) {
			return target.cast(socket);
		} else if (target == TimeStampStream.class) {
			return target.cast(buffOut);
		}
		return super.narrow(target);
	}
	@Override
	public int getReceiveCounter() {
		return receiveCounter;
	}

	public static class InitBuffer {
		public final int readSize;
		public final ByteBuffer buffer;

		public InitBuffer(int readSize, ByteBuffer buffer) {
			if (buffer == null) {
				throw new IllegalArgumentException("Null buffer not allowed.");
			}
			this.readSize = readSize;
			this.buffer = buffer;
		}
	}

	/**
	 * @param sock The socket on which to set the Traffic Class.
	 * @return Whether or not the Traffic Class was set on the given socket.
	 * @throws SocketException if the system does not support setting the Traffic Class.
	 * @throws IllegalArgumentException if both the Differentiated Services and Type of Services
	 *             transport options have been set on the same connection.
	 */
	/**
	 * 设置socket中的TrafficClass属性
	 * @Title: setTrafficClass
	 * @Description: TODO
	 * @param sock
	 * @return
	 * @throws SocketException
	 * @throws IllegalArgumentException
	 * @return: boolean
	 */
	private boolean setTrafficClass(Socket sock) throws SocketException, IllegalArgumentException {
		if (sock == null || (!this.diffServChosen && !this.typeOfServiceChosen)) {
			return false;
		}
		if (this.diffServChosen && this.typeOfServiceChosen) {
			throw new IllegalArgumentException("Cannot set both the "
					+ " Differentiated Services and Type of Services transport " + " options on the same connection.");
		}
		// 下面这个值是tcp的一种规定
		sock.setTrafficClass(this.trafficClass);
		int resultTrafficClass = sock.getTrafficClass();
		if (this.trafficClass != resultTrafficClass) {
			// In the case where the user has specified the ECN bits (e.g. in
			// Type of Service) but the system won't allow the ECN bits to be
			// set or in the case where setting the traffic class failed for
			// other reasons, emit a warning.
			if ((this.trafficClass >> 2) == (resultTrafficClass >> 2)
					&& (this.trafficClass & 3) != (resultTrafficClass & 3)) {
				LOG.warn("Attempted to set the Traffic Class to " + this.trafficClass
						+ " but the result Traffic Class was " + resultTrafficClass
						+ ". Please check that your system " + "allows you to set the ECN bits (the first two bits).");
			} else {
				LOG.warn("Attempted to set the Traffic Class to " + this.trafficClass
						+ " but the result Traffic Class was " + resultTrafficClass
						+ ". Please check that your system " + "supports java.net.setTrafficClass.");
			}
			return false;
		}
		// Reset the guards that prevent both the Differentiated Services
		// option and the Type of Service option from being set on the same
		// connection.
		this.diffServChosen = false;
		this.typeOfServiceChosen = false;
		return true;
	}
	public WireFormat getWireFormat() {
		return wireFormat;
	}
}
