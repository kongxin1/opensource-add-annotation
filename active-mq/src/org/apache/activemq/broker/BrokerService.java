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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.activemq.ActiveMQConnectionMetaData;
import org.apache.activemq.ConfigurationException;
import org.apache.activemq.Service;
import org.apache.activemq.advisory.AdvisoryBroker;
import org.apache.activemq.broker.cluster.ConnectionSplitBroker;
import org.apache.activemq.broker.jmx.AnnotatedMBean;
import org.apache.activemq.broker.jmx.BrokerMBeanSupport;
import org.apache.activemq.broker.jmx.BrokerView;
import org.apache.activemq.broker.jmx.ConnectorView;
import org.apache.activemq.broker.jmx.ConnectorViewMBean;
import org.apache.activemq.broker.jmx.HealthView;
import org.apache.activemq.broker.jmx.HealthViewMBean;
import org.apache.activemq.broker.jmx.JmsConnectorView;
import org.apache.activemq.broker.jmx.JobSchedulerView;
import org.apache.activemq.broker.jmx.JobSchedulerViewMBean;
import org.apache.activemq.broker.jmx.Log4JConfigView;
import org.apache.activemq.broker.jmx.ManagedRegionBroker;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.jmx.NetworkConnectorView;
import org.apache.activemq.broker.jmx.NetworkConnectorViewMBean;
import org.apache.activemq.broker.jmx.ProxyConnectorView;
import org.apache.activemq.broker.region.CompositeDestinationInterceptor;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DestinationFactory;
import org.apache.activemq.broker.region.DestinationFactoryImpl;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.RegionBroker;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.virtual.MirroredQueue;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.broker.region.virtual.VirtualTopic;
import org.apache.activemq.broker.scheduler.JobSchedulerStore;
import org.apache.activemq.broker.scheduler.SchedulerBroker;
import org.apache.activemq.broker.scheduler.memory.InMemoryJobSchedulerStore;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.BrokerId;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.filter.DestinationFilter;
import org.apache.activemq.network.ConnectionFilter;
import org.apache.activemq.network.DiscoveryNetworkConnector;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.network.jms.JmsConnector;
import org.apache.activemq.openwire.OpenWireFormat;
import org.apache.activemq.proxy.ProxyConnector;
import org.apache.activemq.security.MessageAuthorizationPolicy;
import org.apache.activemq.selector.SelectorParser;
import org.apache.activemq.store.JournaledStore;
import org.apache.activemq.store.PListStore;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.PersistenceAdapterFactory;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.activemq.thread.Scheduler;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.activemq.transport.TransportFactorySupport;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.vm.VMTransportFactory;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.Usage;
import org.apache.activemq.util.BrokerSupport;
import org.apache.activemq.util.DefaultIOExceptionHandler;
import org.apache.activemq.util.IOExceptionHandler;
import org.apache.activemq.util.IOExceptionSupport;
import org.apache.activemq.util.IOHelper;
import org.apache.activemq.util.InetAddressUtil;
import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.util.StoreUtil;
import org.apache.activemq.util.ThreadPoolUtils;
import org.apache.activemq.util.TimeUtils;
import org.apache.activemq.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Manages the life-cycle of an ActiveMQ Broker. A BrokerService consists of a number of transport
 * connectors, network connectors and a bunch of properties which can be used to configure the
 * broker as its lazily created.
 * @org.apache.xbean.XBean
 */
public class BrokerService implements Service {
	public static final String DEFAULT_PORT = "61616";
	// 本地计算机名，我的计算机名是：DESKTOP-UM4TVRK，如果找到不到，就是localhost
	public static final String LOCAL_HOST_NAME;
	// 存储brokerService的版本号
	public static final String BROKER_VERSION;
	// broker的默认名字
	public static final String DEFAULT_BROKER_NAME = "localhost";
	public static final int DEFAULT_MAX_FILE_LENGTH = 1024 * 1024 * 32;
	public static final long DEFAULT_START_TIMEOUT = 600000L;
	private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 7353129142305630237L;
	private boolean useJmx = true;
	private boolean enableStatistics = true;
	// 表示是否需要持久化消息
	private boolean persistent = true;
	private boolean populateJMSXUserID;
	private boolean useAuthenticatedPrincipalForJMSXUserID;
	private boolean populateUserNameInMBeans;
	private long mbeanInvocationTimeout = 0;
	// 属性作用未知
	private boolean useShutdownHook = true;
	private boolean useLoggingForShutdownErrors;
	private boolean shutdownOnMasterFailure;
	private boolean shutdownOnSlaveFailure;
	private boolean waitForSlave;
	private long waitForSlaveTimeout = DEFAULT_START_TIMEOUT;
	private boolean passiveSlave;
	private String brokerName = DEFAULT_BROKER_NAME;
	private File dataDirectoryFile;
	private File tmpDataDirectory;
	// 按照默认的来说，下面这个属性就是RegionBroker对象
	private Broker broker;
	private BrokerView adminView;
	private ManagementContext managementContext;
	private ObjectName brokerObjectName;
	private TaskRunnerFactory taskRunnerFactory;
	private TaskRunnerFactory persistenceTaskRunnerFactory;
	private SystemUsage systemUsage;
	private SystemUsage producerSystemUsage;
	private SystemUsage consumerSystemUsaage;
	private PersistenceAdapter persistenceAdapter;
	private PersistenceAdapterFactory persistenceFactory;
	protected DestinationFactory destinationFactory;
	private MessageAuthorizationPolicy messageAuthorizationPolicy;
	// 存储系统中的所有TransportConnector对象，对应于xml文件中的对应节点，在系统启动的时候就会填入该集合中
	private final List<TransportConnector> transportConnectors = new CopyOnWriteArrayList<TransportConnector>();
	private final List<NetworkConnector> networkConnectors = new CopyOnWriteArrayList<NetworkConnector>();
	private final List<ProxyConnector> proxyConnectors = new CopyOnWriteArrayList<ProxyConnector>();
	private final List<JmsConnector> jmsConnectors = new CopyOnWriteArrayList<JmsConnector>();
	private final List<Service> services = new ArrayList<Service>();
	private transient Thread shutdownHook;
	private String[] transportConnectorURIs;
	private String[] networkConnectorURIs;
	private JmsConnector[] jmsBridgeConnectors; // these are Jms to Jms bridges
	// to other jms messaging systems
	// 在启动的过程中，是否删除所有的消息，默认false
	private boolean deleteAllMessagesOnStartup;
	private boolean advisorySupport = true;
	private URI vmConnectorURI;
	private String defaultSocketURIString;
	// 存储了一些对queue或者topic的一些策略，可以使queue或者topic具有一些额外的功能
	private PolicyMap destinationPolicy;
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean stopped = new AtomicBoolean(false);
	private final AtomicBoolean stopping = new AtomicBoolean(false);
	private BrokerPlugin[] plugins;
	private boolean keepDurableSubsActive = true;
	private boolean useVirtualTopics = true;
	private boolean useMirroredQueues = false;
	private boolean useTempMirroredQueues = true;
	/**
	 * Whether or not virtual destination subscriptions should cause network demand
	 */
	private boolean useVirtualDestSubs = false;
	/**
	 * Whether or no the creation of destinations that match virtual destinations should cause
	 * network demand
	 */
	private boolean useVirtualDestSubsOnCreation = false;
	private BrokerId brokerId;
	private volatile DestinationInterceptor[] destinationInterceptors;
	// queue和topic都是继承自ActiveMQDestination
	private ActiveMQDestination[] destinations;
	private PListStore tempDataStore;
	private int persistenceThreadPriority = Thread.MAX_PRIORITY;
	private boolean useLocalHostBrokerName;
	private final CountDownLatch stoppedLatch = new CountDownLatch(1);
	private final CountDownLatch startedLatch = new CountDownLatch(1);
	private Broker regionBroker;
	// 表示生产者可以使用存储资源的一个百分比
	private int producerSystemUsagePortion = 60;
	// 表示消费者可以使用存储资源的百分比
	private int consumerSystemUsagePortion = 40;
	// 生产者和消费者是否分开使用SystemUsage
	private boolean splitSystemUsageForProducersConsumers;
	private boolean monitorConnectionSplits = false;
	private int taskRunnerPriority = Thread.NORM_PRIORITY;
	private boolean dedicatedTaskRunner;
	private boolean cacheTempDestinations = false;// useful for failover
	// 在清除临时Destination对象周期时间，定时器会在该周期内定时清除临时Destination
	private int timeBeforePurgeTempDestinations = 5000;
	private final List<Runnable> shutdownHooks = new ArrayList<Runnable>();
	// 属性作用未知
	private boolean systemExitOnShutdown;
	private int systemExitOnShutdownExitCode;
	private SslContext sslContext;
	private boolean forceStart = false;
	private IOExceptionHandler ioExceptionHandler;
	private boolean schedulerSupport = false;
	private File schedulerDirectoryFile;
	private Scheduler scheduler;
	private ThreadPoolExecutor executor;
	private int schedulePeriodForDestinationPurge = 0;
	private int maxPurgedDestinationsPerSweep = 0;
	private int schedulePeriodForDiskUsageCheck = 0;
	private int diskUsageCheckRegrowThreshold = -1;
	private BrokerContext brokerContext;
	private boolean networkConnectorStartAsync = false;
	private boolean allowTempAutoCreationOnSend;
	private JobSchedulerStore jobSchedulerStore;
	private final AtomicLong totalConnections = new AtomicLong();
	private final AtomicInteger currentConnections = new AtomicInteger();
	// 如果topic订阅者在这个属性要求的时间内不活动，则认为这个订阅者已经死了，需要删除这个订阅者
	private long offlineDurableSubscriberTimeout = -1;
	// 记录清理不活动订阅者的清理周期，定时器会在下面这个属性要求的周期内执行
	private long offlineDurableSubscriberTaskSchedule = 300000;
	private DestinationFilter virtualConsumerDestinationFilter;
	// 当需要异步启动的时候，下面这个属性用于表示持久化适配器是否启动完毕，因为broker需要在持久化适配器启动完毕后才可以启动
	private final AtomicBoolean persistenceAdapterStarted = new AtomicBoolean(false);
	private Throwable startException = null;
	private boolean startAsync = false;
	private Date startDate;
	private boolean slave = true;
	private boolean restartAllowed = true;
	private boolean restartRequested = false;
	private boolean rejectDurableConsumers = false;
	private int storeOpenWireVersion = OpenWireFormat.DEFAULT_STORE_VERSION;
	// static块中做一些初始化工作，找到计算机名、版本号、以及加密算法
	static {
		try {
			// 获得类加载器
			ClassLoader loader = BrokerService.class.getClassLoader();
			// bouncycastle是一个轻量级的密码技术包，支持大量的密码技术算法
			Class<?> clazz = loader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
			Provider bouncycastle = (Provider) clazz.newInstance();
			// 一些用于加密的Provider（提供者）可以存储在Security中，Provider的序号从1开始，默认有10个
			// 这些provider是在配置文件中指定的，在安装目录的java.security下。
			// 关于bouncycastle稍后可以在看
			Security.insertProviderAt(bouncycastle, 2);
			LOG.info("Loaded the Bouncy Castle security provider.");
		} catch (Throwable e) {
			// No BouncyCastle found so we use the default Java Security Provider
		}
		String localHostName = "localhost";
		try {
			// 获得本地主机的主机名，就是电脑中的计算机名，我的计算机名是：DESKTOP-UM4TVRK
			localHostName = InetAddressUtil.getLocalHostName();
		} catch (UnknownHostException e) {
			LOG.error("Failed to resolve localhost");
		}
		LOCAL_HOST_NAME = localHostName;
		String version = null;
		// 获得系统的版本号，这是第二种获得版本号的方法，一种是在启动命令里写上，这种是写在配置文件中
		// try居然还有这种用法，必须是实现了Closeable接口的类才可以，而且在try()里面只能执行赋值操作，它可以自动执行close方法，以释放资源
		try (InputStream in = BrokerService.class.getResourceAsStream("/org/apache/activemq/version.txt")) {
			if (in != null) {
				try (InputStreamReader isr = new InputStreamReader(in); BufferedReader reader = new BufferedReader(isr)) {
					version = reader.readLine();
				}
			}
		} catch (IOException ie) {
			LOG.warn("Error reading broker version ", ie);
		}
		BROKER_VERSION = version;
	}

	@Override
	public String toString() {
		return "BrokerService[" + getBrokerName() + "]";
	}
	/**
	 * 获得版本号
	 * @Title: getBrokerVersion
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	private String getBrokerVersion() {
		String version = ActiveMQConnectionMetaData.PROVIDER_VERSION;
		if (version == null) {
			version = BROKER_VERSION;
		}
		return version;
	}
	/**
	 * Adds a new transport connector for the given bind address
	 * @return the newly created and added transport connector
	 * @throws Exception
	 */
	public TransportConnector addConnector(String bindAddress) throws Exception {
		return addConnector(new URI(bindAddress));
	}
	/**
	 * Adds a new transport connector for the given bind address
	 * @return the newly created and added transport connector
	 * @throws Exception
	 */
	/**
	 * 根据URI创建出TransportServer对象，然后封装到TransportConnector对象中，最后添加到TransportConnector集合中
	 * @Title: addConnector
	 * @Description: TODO
	 * @param bindAddress
	 * @return
	 * @throws Exception
	 * @return: TransportConnector
	 */
	public TransportConnector addConnector(URI bindAddress) throws Exception {
		return addConnector(createTransportConnector(bindAddress));
	}
	/**
	 * Adds a new transport connector for the given TransportServer transport
	 * @return the newly created and added transport connector
	 * @throws Exception
	 */
	/**
	 * 将TransportServer对象封装到TransportConnector对象中，并添加到TransportConnector集合中
	 * @Title: addConnector
	 * @Description: TODO
	 * @param transport
	 * @return
	 * @throws Exception
	 * @return: TransportConnector
	 */
	public TransportConnector addConnector(TransportServer transport) throws Exception {
		return addConnector(new TransportConnector(transport));
	}
	/**
	 * 下面的方法对应于在activemq.xml中配置的节点transportConnector，系统会将节点transportConnector转化为TransportConnector对象
	 * ，然后使用下面的方法将其注入到TransportConnector集合中
	 * @Title: addConnector
	 * @Description: TODO
	 * @param bindAddress
	 * @return
	 * @throws Exception
	 * @return: TransportConnector
	 */
	public TransportConnector addConnector(TransportConnector connector) throws Exception {
		transportConnectors.add(connector);
		return connector;
	}
	/**
	 * Stops and removes a transport connector from the broker.
	 * @param connector
	 * @return true if the connector has been previously added to the broker
	 * @throws Exception
	 */
	/**
	 * 从TransportConnector集合中去除指定的TransportConnector对象，并且还需要从JMX中也去除该对象
	 * @Title: removeConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @throws Exception
	 * @return: boolean
	 */
	public boolean removeConnector(TransportConnector connector) throws Exception {
		boolean rc = transportConnectors.remove(connector);
		if (rc) {
			unregisterConnectorMBean(connector);
		}
		return rc;
	}
	/**
	 * @Title: addNetworkConnector
	 * @Description: TODO
	 * @param discoveryAddress
	 * @return
	 * @throws Exception
	 * @return: NetworkConnector
	 */
	public NetworkConnector addNetworkConnector(String discoveryAddress) throws Exception {
		return addNetworkConnector(new URI(discoveryAddress));
	}
	/**
	 * Adds a new proxy connector using the given bind address
	 * @return the newly created and added network connector
	 * @throws Exception
	 */
	public ProxyConnector addProxyConnector(String bindAddress) throws Exception {
		return addProxyConnector(new URI(bindAddress));
	}
	/**
	 * Adds a new network connector using the given discovery address
	 * @return the newly created and added network connector
	 * @throws Exception
	 */
	/**
	 * NetworkConnector是应用于集群中的，是mq的高可用解决方案，NetworkConnector的具体作用目前还不知道
	 * @Title: addNetworkConnector
	 * @Description: TODO
	 * @param discoveryAddress
	 * @return
	 * @throws Exception
	 * @return: NetworkConnector
	 */
	public NetworkConnector addNetworkConnector(URI discoveryAddress) throws Exception {
		NetworkConnector connector = new DiscoveryNetworkConnector(discoveryAddress);
		return addNetworkConnector(connector);
	}
	/**
	 * Adds a new proxy connector using the given bind address
	 * @return the newly created and added network connector
	 * @throws Exception
	 */
	/**
	 * 具体作用未知
	 * @Title: addProxyConnector
	 * @Description: TODO
	 * @param bindAddress
	 * @return
	 * @throws Exception
	 * @return: ProxyConnector
	 */
	public ProxyConnector addProxyConnector(URI bindAddress) throws Exception {
		ProxyConnector connector = new ProxyConnector();
		connector.setBind(bindAddress);
		// 此协议的目的是发送消息到多个代理.其次,如果你使用的代理属于同一个代理网络,
		// 那么指定的消息消费者可能会接收到重复的消息.因此,通常情况下,fanout协议仅用于
		// 发布消息到多个相互之间没有连接在一起的代理.即多个代理之间的独立的。
		connector.setRemote(new URI("fanout:multicast://default"));
		return addProxyConnector(connector);
	}
	/**
	 * Adds a new network connector to connect this broker to a federated network
	 */
	/**
	 * 对NetworkConnector进行一些简单的设置，有设置BrokerService对象，vm的uri，以及的过滤器，然后将其添加到NetworkConnector集合中
	 * @Title: addNetworkConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @throws Exception
	 * @return: NetworkConnector
	 */
	public NetworkConnector addNetworkConnector(NetworkConnector connector) throws Exception {
		// 需要持有BrokerService对象
		connector.setBrokerService(this);
		// NetworkConnector还需要设置vm的uri
		connector.setLocalUri(getVmConnectorURI());
		// Set a connection filter so that the connector does not establish loop
		// back connections.
		// 设置过滤器，NetworkConnector集合中不能持有和TransportConnector的uri相同的对象
		connector.setConnectionFilter(new ConnectionFilter() {
			@Override
			/**
			 * NetworkConnector集合中不能持有和TransportConnector的uri相同的对象
			 */
			public boolean connectTo(URI location) {
				List<TransportConnector> transportConnectors = getTransportConnectors();
				for (Iterator<TransportConnector> iter = transportConnectors.iterator(); iter.hasNext();) {
					try {
						TransportConnector tc = iter.next();
						if (location.equals(tc.getConnectUri())) {
							return false;
						}
					} catch (Throwable e) {
					}
				}
				return true;
			}
		});
		networkConnectors.add(connector);
		return connector;
	}
	/**
	 * Removes the given network connector without stopping it. The caller should call
	 * {@link NetworkConnector#stop()} to close the connector
	 */
	/**
	 * 简单的删除操作
	 * @Title: removeNetworkConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @return: boolean
	 */
	public boolean removeNetworkConnector(NetworkConnector connector) {
		boolean answer = networkConnectors.remove(connector);
		if (answer) {
			unregisterNetworkConnectorMBean(connector);
		}
		return answer;
	}
	/**
	 * 添加代理，将ProxyConnector对象添加到ProxyConnector集合中
	 * @Title: addProxyConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @throws Exception
	 * @return: ProxyConnector
	 */
	public ProxyConnector addProxyConnector(ProxyConnector connector) throws Exception {
		URI uri = getVmConnectorURI();
		connector.setLocalUri(uri);
		proxyConnectors.add(connector);
		if (isUseJmx()) {
			registerProxyConnectorMBean(connector);
		}
		return connector;
	}
	/**
	 * 将JmsConnector对象添加到JmsConnector集合中
	 * @Title: addJmsConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @throws Exception
	 * @return: JmsConnector
	 */
	public JmsConnector addJmsConnector(JmsConnector connector) throws Exception {
		connector.setBrokerService(this);
		jmsConnectors.add(connector);
		if (isUseJmx()) {
			registerJmsConnectorMBean(connector);
		}
		return connector;
	}
	/**
	 * 简单的删除操作
	 * @Title: removeJmsConnector
	 * @Description: TODO
	 * @param connector
	 * @return
	 * @return: JmsConnector
	 */
	public JmsConnector removeJmsConnector(JmsConnector connector) {
		if (jmsConnectors.remove(connector)) {
			return connector;
		}
		return null;
	}
	/**
	 * 猜测作用是：如果shutdownOnMasterFailure=true在集群中，master失败了，集群中的其他机器就终止，如果为false，那么本机就成了master
	 * @Title: masterFailed
	 * @Description: TODO
	 * @return: void
	 */
	public void masterFailed() {
		if (shutdownOnMasterFailure) {
			LOG.error("The Master has failed ... shutting down");
			try {
				stop();
			} catch (Exception e) {
				LOG.error("Failed to stop for master failure", e);
			}
		} else {
			LOG.warn("Master Failed - starting all connectors");
			try {
				startAllConnectors();
				broker.nowMasterBroker();
			} catch (Exception e) {
				LOG.error("Failed to startAllConnectors", e);
			}
		}
	}
	/**
	 * 打印已经运行多长时间
	 * @Title: getUptime
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public String getUptime() {
		long delta = getUptimeMillis();
		if (delta == 0) {
			return "not started";
		}
		return TimeUtils.printDuration(delta);
	}
	/**
	 * 得到当前系统已经启动多长时间，如果未设置startDate属性，该方法返回0，startDate属性在start方法中会进行设置
	 * @Title: getUptimeMillis
	 * @Description: TODO
	 * @return
	 * @return: long
	 */
	public long getUptimeMillis() {
		if (startDate == null) {
			return 0;
		}
		return new Date().getTime() - startDate.getTime();
	}
	public boolean isStarted() {
		return started.get() && startedLatch.getCount() == 0;
	}
	/**
	 * Forces a start of the broker. By default a BrokerService instance that was previously stopped
	 * using BrokerService.stop() cannot be restarted using BrokerService.start(). This method
	 * enforces a restart. It is not recommended to force a restart of the broker and will not work
	 * for most but some very trivial broker configurations. For restarting a broker instance we
	 * recommend to first call stop() on the old instance and then recreate a new BrokerService
	 * instance.
	 * @param force - if true enforces a restart.
	 * @throws Exception
	 */
	/**
	 * force表示是否强制启动，用于强制启动BrokerService，但是不推荐使用
	 * @Title: start
	 * @Description: TODO
	 * @param force
	 * @throws Exception
	 * @return: void
	 */
	public void start(boolean force) throws Exception {
		forceStart = force;
		stopped.set(false);
		started.set(false);
		start();
	}
	// Service interface
	// -------------------------------------------------------------------------
	/**
	 * 是否可以自动启动，默认是true
	 * @Title: shouldAutostart
	 * @Description: TODO
	 * @return
	 * @return: boolean
	 */
	protected boolean shouldAutostart() {
		return true;
	}
	/**
	 * JSR-250 callback wrapper; converts checked exceptions to runtime exceptions delegates to
	 * autoStart, done to prevent backwards incompatible signature change
	 */
	@PostConstruct
	/**
	 * 在执行构造方法后执行，如果可以自动启动，则会启动BrokerService对象
	 * @Title: postConstruct 
	 * @Description: TODO
	 * @return: void
	 */
	private void postConstruct() {
		try {
			autoStart();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * @throws Exception
	 * @org. apache.xbean.InitMethod
	 */
	/**
	 * 自动启动
	 * @Title: autoStart
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	public void autoStart() throws Exception {
		if (shouldAutostart()) {
			start();
		}
	}
	/**
	 * 启动类
	 * @Title: start
	 * @Description: TODO
	 * @param force
	 * @throws Exception
	 * @return: void
	 */
	public void start() throws Exception {
		// 保证只能启动一次
		if (stopped.get() || !started.compareAndSet(false, true)) {
			// lets just ignore redundant start() calls
			// as its way too easy to not be completely sure if start() has been
			// called or not with the gazillion of different configuration
			// mechanisms
			// throw new IllegalStateException("Already started.");
			return;
		}
		stopping.set(false);
		startDate = new Date();
		// 用于打印
		MDC.put("activemq.broker", brokerName);
		try {
			// 下面这两属性作用未知
			if (systemExitOnShutdown && useShutdownHook) {
				throw new ConfigurationException(
						"'useShutdownHook' property cannot be be used with 'systemExitOnShutdown', please turn it off (useShutdownHook=false)");
			}
			// 将transportConnectorURI、networkConnectorURI和jmsBridgeConnector转化为对应的Connector对象，
			// 然后存入对应的Connector集合
			processHelperProperties();
			// jmx内容先不处理
			if (isUseJmx()) {
				// need to remove MDC during starting JMX, as that would otherwise causes leaks, as
				// spawned threads inheirt the MDC and
				// we cannot cleanup clear that during shutdown of the broker.
				MDC.remove("activemq.broker");
				try {
					startManagementContext();
					for (NetworkConnector connector : getNetworkConnectors()) {
						registerNetworkConnectorMBean(connector);
					}
				} finally {
					MDC.put("activemq.broker", brokerName);
				}
			}
			// in jvm master slave, lets not publish over existing broker till we get the lock
			// 获得一个单例的BrokerRegistry对象，这个对象中存储了BrokerService对象，换句话说，就是所有的BrokerService都会存储在下面的对象中
			final BrokerRegistry brokerRegistry = BrokerRegistry.getInstance();
			// 将本对象注入brokerRegistry中
			if (brokerRegistry.lookup(getBrokerName()) == null) {
				brokerRegistry.bind(getBrokerName(), BrokerService.this);
			}
			// startAsync默认是false，startAsync表示是否需要异步启动，如果异步启动，就创建一个单独线程进行启动
			startPersistenceAdapter(startAsync);
			// startAsync默认是false，startAsync表示是否需要异步启动，如果异步启动，就创建一个单独线程进行启动
			startBroker(startAsync);
			// 下面这个代码是一个意义不是很大的代码，我个人认为是这样
			brokerRegistry.bind(getBrokerName(), BrokerService.this);
		} catch (Exception e) {
			LOG.error("Failed to start Apache ActiveMQ ({}, {})", new Object[] { getBrokerName(), brokerId }, e);
			try {
				if (!stopped.get()) {
					stop();
				}
			} catch (Exception ex) {
				LOG.warn("Failed to stop broker after failure in start. This exception will be ignored.", ex);
			}
			throw e;
		} finally {
			MDC.remove("activemq.broker");
		}
	}
	/**
	 * 根据是否异步启动，调用doStartPersistenceAdapter方法，具体的处理过程都在doStartPersistenceAdapter方法中
	 * @Title: startPersistenceAdapter
	 * @Description: TODO
	 * @param async
	 * @throws Exception
	 * @return: void
	 */
	private void startPersistenceAdapter(boolean async) throws Exception {
		if (async) {
			new Thread("Persistence Adapter Starting Thread") {
				@Override
				public void run() {
					try {
						doStartPersistenceAdapter();
					} catch (Throwable e) {
						startException = e;
					} finally {
						synchronized (persistenceAdapterStarted) {
							// 如果是异步启动，需要修改属性persistenceAdapterStarted，以表示持久化适配器已经启动完毕，因为broker的启动需要持久化适配器启动完毕才可以启动
							persistenceAdapterStarted.set(true);
							persistenceAdapterStarted.notifyAll();
						}
					}
				}
			}.start();
		} else {
			doStartPersistenceAdapter();
		}
	}
	/**
	 * 启动持久化适配器，找到或者创建持久化适配器，然后启动它，后面还需要创建定时任务调度器的存储器，用于执行定时任务，其他就没了
	 * @Title: doStartPersistenceAdapter
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	private void doStartPersistenceAdapter() throws Exception {
		// 为生产者创建一个SystemUsage对象，为什么只有生产者的SystemUsage会设置到持久化适配器中？
		getPersistenceAdapter().setUsageManager(getProducerSystemUsage());
		// 设置持久化适配器的名字，与broker的名字保持一致
		getPersistenceAdapter().setBrokerName(getBrokerName());
		LOG.info("Using Persistence Adapter: {}", getPersistenceAdapter());
		// 在启动的过程中，需要判断是否删除消息
		if (deleteAllMessagesOnStartup) {
			// 会调用持久化适配器的删除消息方法删除消息
			deleteAllMessages();
		}
		// 持久化适配器启动
		getPersistenceAdapter().start();
		// 创建一个定时任务调度器的存储器，下面这个方法会根据是否需要持久化，而创建不同的存储器
		getJobSchedulerStore();
		if (jobSchedulerStore != null) {
			try {
				// 启动定时任务调度器的存储器
				jobSchedulerStore.start();
			} catch (Exception e) {
				RuntimeException exception = new RuntimeException("Failed to start job scheduler store: "
						+ jobSchedulerStore, e);
				LOG.error(exception.getLocalizedMessage(), e);
				throw exception;
			}
		}
	}
	/**
	 * 根据是否异步启动，调用doStartBroker方法，具体的处理过程都在doStartBroker方法中
	 * @Title: startBroker
	 * @Description: TODO
	 * @param async
	 * @throws Exception
	 * @return: void
	 */
	private void startBroker(boolean async) throws Exception {
		if (async) {
			new Thread("Broker Starting Thread") {
				@Override
				public void run() {
					try {
						// 当持久化适配器启动完毕后，属性persistenceAdapterStarted会在startPersistenceAdapter方法中修改
						synchronized (persistenceAdapterStarted) {
							if (!persistenceAdapterStarted.get()) {
								persistenceAdapterStarted.wait();
							}
						}
						doStartBroker();
					} catch (Throwable t) {
						startException = t;
					}
				}
			}.start();
		} else {
			doStartBroker();
		}
	}
	/**
	 * 看到这个方法
	 * @Title: doStartBroker
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	private void doStartBroker() throws Exception {
		if (startException != null) {
			return;
		}
		// 从这方法向下看
		startDestinations();
		// 下面这个方法先不管
		addShutdownHook();
		// 获得一个broker对象
		broker = getBroker();
		brokerId = broker.getBrokerId();
		// need to log this after creating the broker so we have its id and name
		LOG.info("Apache ActiveMQ {} ({}, {}) is starting", new Object[] { getBrokerVersion(), getBrokerName(),
				brokerId });
		broker.start();
		if (isUseJmx()) {
			if (getManagementContext().isCreateConnector() && !getManagementContext().isConnectorStarted()) {
				// try to restart management context
				// typical for slaves that use the same ports as master
				managementContext.stop();
				startManagementContext();
			}
			ManagedRegionBroker managedBroker = (ManagedRegionBroker) regionBroker;
			managedBroker.setContextBroker(broker);
			adminView.setBroker(managedBroker);
		}
		if (ioExceptionHandler == null) {
			setIoExceptionHandler(new DefaultIOExceptionHandler());
		}
		if (isUseJmx() && Log4JConfigView.isLog4JAvailable()) {
			ObjectName objectName = BrokerMBeanSupport.createLog4JConfigViewName(getBrokerObjectName().toString());
			Log4JConfigView log4jConfigView = new Log4JConfigView();
			AnnotatedMBean.registerMBean(getManagementContext(), log4jConfigView, objectName);
		}
		startAllConnectors();
		LOG.info("Apache ActiveMQ {} ({}, {}) started", new Object[] { getBrokerVersion(), getBrokerName(), brokerId });
		LOG.info("For help or more information please see: http://activemq.apache.org");
		getBroker().brokerServiceStarted();// 是一个空实现
		checkSystemUsageLimits();
		startedLatch.countDown();
		getBroker().nowMasterBroker();
	}
	/**
	 * JSR-250 callback wrapper; converts checked exceptions to runtime exceptions delegates to
	 * stop, done to prevent backwards incompatible signature change
	 */
	@PreDestroy
	private void preDestroy() {
		try {
			stop();
		} catch (Exception ex) {
			throw new RuntimeException();
		}
	}
	/**
	 * @throws Exception
	 * @org.apache .xbean.DestroyMethod
	 */
	@Override
	public void stop() throws Exception {
		if (!stopping.compareAndSet(false, true)) {
			LOG.trace("Broker already stopping/stopped");
			return;
		}
		MDC.put("activemq.broker", brokerName);
		if (systemExitOnShutdown) {
			new Thread() {
				@Override
				public void run() {
					System.exit(systemExitOnShutdownExitCode);
				}
			}.start();
		}
		LOG.info("Apache ActiveMQ {} ({}, {}) is shutting down", new Object[] { getBrokerVersion(), getBrokerName(),
				brokerId });
		removeShutdownHook();
		if (this.scheduler != null) {
			this.scheduler.stop();
			this.scheduler = null;
		}
		ServiceStopper stopper = new ServiceStopper();
		if (services != null) {
			for (Service service : services) {
				stopper.stop(service);
			}
		}
		stopAllConnectors(stopper);
		this.slave = true;
		// remove any VMTransports connected
		// this has to be done after services are stopped,
		// to avoid timing issue with discovery (spinning up a new instance)
		BrokerRegistry.getInstance().unbind(getBrokerName());
		VMTransportFactory.stopped(getBrokerName());
		if (broker != null) {
			stopper.stop(broker);
			broker = null;
		}
		if (jobSchedulerStore != null) {
			jobSchedulerStore.stop();
			jobSchedulerStore = null;
		}
		if (tempDataStore != null) {
			tempDataStore.stop();
			tempDataStore = null;
		}
		try {
			stopper.stop(persistenceAdapter);
			persistenceAdapter = null;
			if (isUseJmx()) {
				stopper.stop(getManagementContext());
				managementContext = null;
			}
			// Clear SelectorParser cache to free memory
			SelectorParser.clearCache();
		} finally {
			started.set(false);
			stopped.set(true);
			stoppedLatch.countDown();
		}
		if (this.taskRunnerFactory != null) {
			this.taskRunnerFactory.shutdown();
			this.taskRunnerFactory = null;
		}
		if (this.executor != null) {
			ThreadPoolUtils.shutdownNow(executor);
			this.executor = null;
		}
		this.destinationInterceptors = null;
		this.destinationFactory = null;
		if (startDate != null) {
			LOG.info("Apache ActiveMQ {} ({}, {}) uptime {}", new Object[] { getBrokerVersion(), getBrokerName(),
					brokerId, getUptime() });
		}
		LOG.info("Apache ActiveMQ {} ({}, {}) is shutdown", new Object[] { getBrokerVersion(), getBrokerName(),
				brokerId });
		synchronized (shutdownHooks) {
			for (Runnable hook : shutdownHooks) {
				try {
					hook.run();
				} catch (Throwable e) {
					stopper.onException(hook, e);
				}
			}
		}
		MDC.remove("activemq.broker");
		// and clear start date
		startDate = null;
		stopper.throwFirstException();
	}
	public boolean checkQueueSize(String queueName) {
		long count = 0;
		long queueSize = 0;
		Map<ActiveMQDestination, Destination> destinationMap = regionBroker.getDestinationMap();
		for (Map.Entry<ActiveMQDestination, Destination> entry : destinationMap.entrySet()) {
			if (entry.getKey().isQueue()) {
				if (entry.getValue().getName().matches(queueName)) {
					queueSize = entry.getValue().getDestinationStatistics().getMessages().getCount();
					count += queueSize;
					if (queueSize > 0) {
						LOG.info("Queue has pending message: {} queueSize is: {}", entry.getValue().getName(),
								queueSize);
					}
				}
			}
		}
		return count == 0;
	}
	/**
	 * This method (both connectorName and queueName are using regex to match) 1. stop the connector
	 * (supposed the user input the connector which the clients connect to) 2. to check whether
	 * there is any pending message on the queues defined by queueName 3. supposedly, after stop the
	 * connector, client should failover to other broker and pending messages should be forwarded.
	 * if no pending messages, the method finally call stop to stop the broker.
	 * @param connectorName
	 * @param queueName
	 * @param timeout
	 * @param pollInterval
	 * @throws Exception
	 */
	public void stopGracefully(String connectorName, String queueName, long timeout, long pollInterval)
			throws Exception {
		if (isUseJmx()) {
			if (connectorName == null || queueName == null || timeout <= 0) {
				throw new Exception(
						"connectorName and queueName cannot be null and timeout should be >0 for stopGracefully.");
			}
			if (pollInterval <= 0) {
				pollInterval = 30;
			}
			LOG.info("Stop gracefully with connectorName: {} queueName: {} timeout: {} pollInterval: {}", new Object[] {
					connectorName, queueName, timeout, pollInterval });
			TransportConnector connector;
			for (int i = 0; i < transportConnectors.size(); i++) {
				connector = transportConnectors.get(i);
				if (connector != null && connector.getName() != null && connector.getName().matches(connectorName)) {
					connector.stop();
				}
			}
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() - start < timeout * 1000) {
				// check quesize until it gets zero
				if (checkQueueSize(queueName)) {
					stop();
					break;
				} else {
					Thread.sleep(pollInterval * 1000);
				}
			}
			if (stopped.get()) {
				LOG.info("Successfully stop the broker.");
			} else {
				LOG.info("There is still pending message on the queue. Please check and stop the broker manually.");
			}
		}
	}
	/**
	 * A helper method to block the caller thread until the broker has been stopped
	 */
	public void waitUntilStopped() {
		while (isStarted() && !stopped.get()) {
			try {
				stoppedLatch.await();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
	public boolean isStopped() {
		return stopped.get();
	}
	/**
	 * A helper method to block the caller thread until the broker has fully started
	 * @return boolean true if wait succeeded false if broker was not started or was stopped
	 */
	public boolean waitUntilStarted() {
		return waitUntilStarted(DEFAULT_START_TIMEOUT);
	}
	/**
	 * A helper method to block the caller thread until the broker has fully started
	 * @param timeout the amount of time to wait before giving up and returning false.
	 * @return boolean true if wait succeeded false if broker was not started or was stopped
	 */
	public boolean waitUntilStarted(long timeout) {
		boolean waitSucceeded = isStarted();
		long expiration = Math.max(0, timeout + System.currentTimeMillis());
		while (!isStarted() && !stopped.get() && !waitSucceeded && expiration > System.currentTimeMillis()) {
			try {
				if (startException != null) {
					return waitSucceeded;
				}
				waitSucceeded = startedLatch.await(100L, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ignore) {
			}
		}
		return waitSucceeded;
	}
	// Properties
	// -------------------------------------------------------------------------
	/**
	 * Returns the message broker
	 */
	/**
	 * 如果属性broker为null，就创建一个Broker对象
	 * @Title: getBroker
	 * @Description: TODO
	 * @return
	 * @throws Exception
	 * @return: Broker
	 */
	public Broker getBroker() throws Exception {
		if (broker == null) {
			broker = createBroker();
		}
		return broker;
	}
	/**
	 * Returns the administration view of the broker; used to create and destroy resources such as
	 * queues and topics. Note this method returns null if JMX is disabled.
	 */
	public BrokerView getAdminView() throws Exception {
		if (adminView == null) {
			// force lazy creation
			getBroker();
		}
		return adminView;
	}
	public void setAdminView(BrokerView adminView) {
		this.adminView = adminView;
	}
	public String getBrokerName() {
		return brokerName;
	}
	/**
	 * Sets the name of this broker; which must be unique in the network
	 * @param brokerName
	 */
	public void setBrokerName(String brokerName) {
		if (brokerName == null) {
			throw new NullPointerException("The broker name cannot be null");
		}
		String str = brokerName.replaceAll("[^a-zA-Z0-9\\.\\_\\-\\:]", "_");
		if (!str.equals(brokerName)) {
			LOG.error("Broker Name: {} contained illegal characters - replaced with {}", brokerName, str);
		}
		this.brokerName = str.trim();
	}
	public PersistenceAdapterFactory getPersistenceFactory() {
		return persistenceFactory;
	}
	public File getDataDirectoryFile() {
		if (dataDirectoryFile == null) {
			dataDirectoryFile = new File(IOHelper.getDefaultDataDirectory());
		}
		return dataDirectoryFile;
	}
	public File getBrokerDataDirectory() {
		String brokerDir = getBrokerName();
		return new File(getDataDirectoryFile(), brokerDir);
	}
	/**
	 * Sets the directory in which the data files will be stored by default for the JDBC and Journal
	 * persistence adaptors.
	 * @param dataDirectory the directory to store data files
	 */
	public void setDataDirectory(String dataDirectory) {
		setDataDirectoryFile(new File(dataDirectory));
	}
	/**
	 * Sets the directory in which the data files will be stored by default for the JDBC and Journal
	 * persistence adaptors.
	 * @param dataDirectoryFile the directory to store data files
	 */
	public void setDataDirectoryFile(File dataDirectoryFile) {
		this.dataDirectoryFile = dataDirectoryFile;
	}
	/**
	 * @return the tmpDataDirectory
	 */
	/**
	 * 获得临时数据的存储目录
	 * @Title: getTmpDataDirectory
	 * @Description: TODO
	 * @return
	 * @return: File
	 */
	public File getTmpDataDirectory() {
		if (tmpDataDirectory == null) {
			// 默认的临时数据目录在data目录下有一个tmp_storage子目录
			tmpDataDirectory = new File(getBrokerDataDirectory(), "tmp_storage");
		}
		return tmpDataDirectory;
	}
	/**
	 * @param tmpDataDirectory the tmpDataDirectory to set
	 */
	public void setTmpDataDirectory(File tmpDataDirectory) {
		this.tmpDataDirectory = tmpDataDirectory;
	}
	public void setPersistenceFactory(PersistenceAdapterFactory persistenceFactory) {
		this.persistenceFactory = persistenceFactory;
	}
	public void setDestinationFactory(DestinationFactory destinationFactory) {
		this.destinationFactory = destinationFactory;
	}
	public boolean isPersistent() {
		return persistent;
	}
	/**
	 * Sets whether or not persistence is enabled or disabled.
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
	public boolean isPopulateJMSXUserID() {
		return populateJMSXUserID;
	}
	/**
	 * Sets whether or not the broker should populate the JMSXUserID header.
	 */
	public void setPopulateJMSXUserID(boolean populateJMSXUserID) {
		this.populateJMSXUserID = populateJMSXUserID;
	}
	/**
	 * 创建systemUsage对象，这个对象会对许多存储资源的使用量进行了设置，保证不能超过这些资源的设定值
	 * @Title: getSystemUsage
	 * @Description: TODO
	 * @return
	 * @return: SystemUsage
	 */
	public SystemUsage getSystemUsage() {
		try {
			if (systemUsage == null) {
				systemUsage = new SystemUsage("Main", getPersistenceAdapter(), getTempDataStore(),
						getJobSchedulerStore());
				systemUsage.setExecutor(getExecutor());
				systemUsage.getMemoryUsage().setLimit(1024L * 1024 * 1024 * 1); // 1 GB
				systemUsage.getTempUsage().setLimit(1024L * 1024 * 1024 * 50); // 50 GB
				systemUsage.getStoreUsage().setLimit(1024L * 1024 * 1024 * 100); // 100 GB
				systemUsage.getJobSchedulerUsage().setLimit(1024L * 1024 * 1024 * 50); // 50 GB
				addService(this.systemUsage);
			}
			return systemUsage;
		} catch (IOException e) {
			LOG.error("Cannot create SystemUsage", e);
			throw new RuntimeException("Fatally failed to create SystemUsage" + e.getMessage(), e);
		}
	}
	public void setSystemUsage(SystemUsage memoryManager) {
		if (this.systemUsage != null) {
			removeService(this.systemUsage);
		}
		this.systemUsage = memoryManager;
		if (this.systemUsage.getExecutor() == null) {
			this.systemUsage.setExecutor(getExecutor());
		}
		addService(this.systemUsage);
	}
	/**
	 * @return the consumerUsageManager
	 * @throws IOException
	 */
	/**
	 * 创建消费者的SystemUsage对象，这个对象会对许多存储资源的使用量进行了设置，保证不能超过这些资源的设定值
	 * @Title: getConsumerSystemUsage
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: SystemUsage
	 */
	public SystemUsage getConsumerSystemUsage() throws IOException {
		if (this.consumerSystemUsaage == null) {
			if (splitSystemUsageForProducersConsumers) {
				this.consumerSystemUsaage = new SystemUsage(getSystemUsage(), "Consumer");
				float portion = consumerSystemUsagePortion / 100f;
				this.consumerSystemUsaage.getMemoryUsage().setUsagePortion(portion);
				addService(this.consumerSystemUsaage);
			} else {
				consumerSystemUsaage = getSystemUsage();
			}
		}
		return this.consumerSystemUsaage;
	}
	/**
	 * @param consumerSystemUsaage the storeSystemUsage to set
	 */
	public void setConsumerSystemUsage(SystemUsage consumerSystemUsaage) {
		if (this.consumerSystemUsaage != null) {
			removeService(this.consumerSystemUsaage);
		}
		this.consumerSystemUsaage = consumerSystemUsaage;
		addService(this.consumerSystemUsaage);
	}
	/**
	 * @return the producerUsageManager
	 * @throws IOException
	 */
	/**
	 * 创建生产者的SystemUsage对象，这个对象会对许多存储资源的使用量进行了设置，保证不能超过这些资源的设定值
	 * @Title: getProducerSystemUsage
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: SystemUsage
	 */
	public SystemUsage getProducerSystemUsage() throws IOException {
		if (producerSystemUsage == null) {
			// 为true，则表示生产者和消费者需要使用不同的SystemUsage
			if (splitSystemUsageForProducersConsumers) {
				producerSystemUsage = new SystemUsage(getSystemUsage(), "Producer");
				// 不设置父Usage对象，下面这个值是不起作用，上面一行代码已经设置了父对象
				float portion = producerSystemUsagePortion / 100f;
				producerSystemUsage.getMemoryUsage().setUsagePortion(portion);
				addService(producerSystemUsage);
			} else {
				producerSystemUsage = getSystemUsage();
			}
		}
		return producerSystemUsage;
	}
	/**
	 * @param producerUsageManager the producerUsageManager to set
	 */
	public void setProducerSystemUsage(SystemUsage producerUsageManager) {
		if (this.producerSystemUsage != null) {
			removeService(this.producerSystemUsage);
		}
		this.producerSystemUsage = producerUsageManager;
		addService(this.producerSystemUsage);
	}
	/**
	 * 如果persistenceAdapter属性为null，就创建一个消息存储器，否则就直接返回persistenceAdapter属性值
	 * @Title: getPersistenceAdapter
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: PersistenceAdapter
	 */
	public PersistenceAdapter getPersistenceAdapter() throws IOException {
		// 我们可以在配置文件中配置一个适配器，如果没有配置的话，就会创建一个默认的
		if (persistenceAdapter == null) {
			// 如果持久化适配器为null，就创建一个
			persistenceAdapter = createPersistenceAdapter();
			// 将本对象注入persistenceAdapter中
			configureService(persistenceAdapter);
			// 下面的方法简简单单的将入参对象返回
			this.persistenceAdapter = registerPersistenceAdapterMBean(persistenceAdapter);
		}
		return persistenceAdapter;
	}
	/**
	 * Sets the persistence adaptor implementation to use for this broker
	 * @throws IOException
	 */
	public void setPersistenceAdapter(PersistenceAdapter persistenceAdapter) throws IOException {
		if (!isPersistent() && !(persistenceAdapter instanceof MemoryPersistenceAdapter)) {
			LOG.warn("persistent=\"false\", ignoring configured persistenceAdapter: {}", persistenceAdapter);
			return;
		}
		this.persistenceAdapter = persistenceAdapter;
		configureService(this.persistenceAdapter);
		this.persistenceAdapter = registerPersistenceAdapterMBean(persistenceAdapter);
	}
	/**
	 * 创建一个TaskRunnerFactory对象
	 * @Title: getTaskRunnerFactory
	 * @Description: TODO
	 * @return
	 * @return: TaskRunnerFactory
	 */
	public TaskRunnerFactory getTaskRunnerFactory() {
		if (this.taskRunnerFactory == null) {
			this.taskRunnerFactory = new TaskRunnerFactory("ActiveMQ BrokerService[" + getBrokerName() + "] Task",
					getTaskRunnerPriority(), true, 1000, isDedicatedTaskRunner());
			this.taskRunnerFactory.setThreadClassLoader(this.getClass().getClassLoader());
		}
		return this.taskRunnerFactory;
	}
	public void setTaskRunnerFactory(TaskRunnerFactory taskRunnerFactory) {
		this.taskRunnerFactory = taskRunnerFactory;
	}
	public TaskRunnerFactory getPersistenceTaskRunnerFactory() {
		if (taskRunnerFactory == null) {
			persistenceTaskRunnerFactory = new TaskRunnerFactory("Persistence Adaptor Task", persistenceThreadPriority,
					true, 1000, isDedicatedTaskRunner());
		}
		return persistenceTaskRunnerFactory;
	}
	public void setPersistenceTaskRunnerFactory(TaskRunnerFactory persistenceTaskRunnerFactory) {
		this.persistenceTaskRunnerFactory = persistenceTaskRunnerFactory;
	}
	public boolean isUseJmx() {
		return useJmx;
	}
	public boolean isEnableStatistics() {
		return enableStatistics;
	}
	/**
	 * Sets whether or not the Broker's services enable statistics or not.
	 */
	public void setEnableStatistics(boolean enableStatistics) {
		this.enableStatistics = enableStatistics;
	}
	/**
	 * Sets whether or not the Broker's services should be exposed into JMX or not.
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setUseJmx(boolean useJmx) {
		this.useJmx = useJmx;
	}
	public ObjectName getBrokerObjectName() throws MalformedObjectNameException {
		if (brokerObjectName == null) {
			brokerObjectName = createBrokerObjectName();
		}
		return brokerObjectName;
	}
	/**
	 * Sets the JMX ObjectName for this broker
	 */
	public void setBrokerObjectName(ObjectName brokerObjectName) {
		this.brokerObjectName = brokerObjectName;
	}
	/**
	 * 返回的是一个和jmx相关的管理上下文对象
	 * @Title: getManagementContext
	 * @Description: TODO
	 * @return
	 * @return: ManagementContext
	 */
	public ManagementContext getManagementContext() {
		if (managementContext == null) {
			managementContext = new ManagementContext();
		}
		return managementContext;
	}
	public void setManagementContext(ManagementContext managementContext) {
		this.managementContext = managementContext;
	}
	public NetworkConnector getNetworkConnectorByName(String connectorName) {
		for (NetworkConnector connector : networkConnectors) {
			if (connector.getName().equals(connectorName)) {
				return connector;
			}
		}
		return null;
	}
	public String[] getNetworkConnectorURIs() {
		return networkConnectorURIs;
	}
	public void setNetworkConnectorURIs(String[] networkConnectorURIs) {
		this.networkConnectorURIs = networkConnectorURIs;
	}
	public TransportConnector getConnectorByName(String connectorName) {
		for (TransportConnector connector : transportConnectors) {
			if (connector.getName().equals(connectorName)) {
				return connector;
			}
		}
		return null;
	}
	public Map<String, String> getTransportConnectorURIsAsMap() {
		Map<String, String> answer = new HashMap<String, String>();
		for (TransportConnector connector : transportConnectors) {
			try {
				URI uri = connector.getConnectUri();
				if (uri != null) {
					String scheme = uri.getScheme();
					if (scheme != null) {
						answer.put(scheme.toLowerCase(Locale.ENGLISH), uri.toString());
					}
				}
			} catch (Exception e) {
				LOG.debug("Failed to read URI to build transportURIsAsMap", e);
			}
		}
		return answer;
	}
	public ProducerBrokerExchange getProducerBrokerExchange(ProducerInfo producerInfo) {
		ProducerBrokerExchange result = null;
		for (TransportConnector connector : transportConnectors) {
			for (TransportConnection tc : connector.getConnections()) {
				result = tc.getProducerBrokerExchangeIfExists(producerInfo);
				if (result != null) {
					return result;
				}
			}
		}
		return result;
	}
	public String[] getTransportConnectorURIs() {
		return transportConnectorURIs;
	}
	public void setTransportConnectorURIs(String[] transportConnectorURIs) {
		this.transportConnectorURIs = transportConnectorURIs;
	}
	/**
	 * @return Returns the jmsBridgeConnectors.
	 */
	public JmsConnector[] getJmsBridgeConnectors() {
		return jmsBridgeConnectors;
	}
	/**
	 * @param jmsConnectors The jmsBridgeConnectors to set.
	 */
	public void setJmsBridgeConnectors(JmsConnector[] jmsConnectors) {
		this.jmsBridgeConnectors = jmsConnectors;
	}
	public Service[] getServices() {
		return services.toArray(new Service[0]);
	}
	/**
	 * Sets the services associated with this broker.
	 */
	public void setServices(Service[] services) {
		this.services.clear();
		if (services != null) {
			for (int i = 0; i < services.length; i++) {
				this.services.add(services[i]);
			}
		}
	}
	/**
	 * Adds a new service so that it will be started as part of the broker lifecycle
	 */
	/**
	 * 添加到本对象中的service集合中
	 * @Title: addService
	 * @Description: TODO
	 * @param service
	 * @return: void
	 */
	public void addService(Service service) {
		services.add(service);
	}
	public void removeService(Service service) {
		services.remove(service);
	}
	public boolean isUseLoggingForShutdownErrors() {
		return useLoggingForShutdownErrors;
	}
	/**
	 * Sets whether or not we should use commons-logging when reporting errors when shutting down
	 * the broker
	 */
	public void setUseLoggingForShutdownErrors(boolean useLoggingForShutdownErrors) {
		this.useLoggingForShutdownErrors = useLoggingForShutdownErrors;
	}
	public boolean isUseShutdownHook() {
		return useShutdownHook;
	}
	/**
	 * Sets whether or not we should use a shutdown handler to close down the broker cleanly if the
	 * JVM is terminated. It is recommended you leave this enabled.
	 */
	public void setUseShutdownHook(boolean useShutdownHook) {
		this.useShutdownHook = useShutdownHook;
	}
	public boolean isAdvisorySupport() {
		return advisorySupport;
	}
	/**
	 * Allows the support of advisory messages to be disabled for performance reasons.
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setAdvisorySupport(boolean advisorySupport) {
		this.advisorySupport = advisorySupport;
	}
	public List<TransportConnector> getTransportConnectors() {
		return new ArrayList<TransportConnector>(transportConnectors);
	}
	/**
	 * Sets the transport connectors which this broker will listen on for new clients
	 * @org.apache.xbean.Property nestedType="org.apache.activemq.broker.TransportConnector"
	 */
	/**
	 * 将xml文件配置的所有transport节点对应的对象添加到transportConnectors集合中
	 * @Title: setTransportConnectors
	 * @Description: TODO
	 * @param transportConnectors
	 * @throws Exception
	 * @return: void
	 */
	public void setTransportConnectors(List<TransportConnector> transportConnectors) throws Exception {
		for (TransportConnector connector : transportConnectors) {
			addConnector(connector);
		}
	}
	public TransportConnector getTransportConnectorByName(String name) {
		for (TransportConnector transportConnector : transportConnectors) {
			if (name.equals(transportConnector.getName())) {
				return transportConnector;
			}
		}
		return null;
	}
	public TransportConnector getTransportConnectorByScheme(String scheme) {
		for (TransportConnector transportConnector : transportConnectors) {
			if (scheme.equals(transportConnector.getUri().getScheme())) {
				return transportConnector;
			}
		}
		return null;
	}
	public List<NetworkConnector> getNetworkConnectors() {
		return new ArrayList<NetworkConnector>(networkConnectors);
	}
	public List<ProxyConnector> getProxyConnectors() {
		return new ArrayList<ProxyConnector>(proxyConnectors);
	}
	/**
	 * Sets the network connectors which this broker will use to connect to other brokers in a
	 * federated network
	 * @org.apache.xbean.Property nestedType="org.apache.activemq.network.NetworkConnector"
	 */
	public void setNetworkConnectors(List<?> networkConnectors) throws Exception {
		for (Object connector : networkConnectors) {
			addNetworkConnector((NetworkConnector) connector);
		}
	}
	/**
	 * Sets the network connectors which this broker will use to connect to other brokers in a
	 * federated network
	 */
	public void setProxyConnectors(List<?> proxyConnectors) throws Exception {
		for (Object connector : proxyConnectors) {
			addProxyConnector((ProxyConnector) connector);
		}
	}
	public PolicyMap getDestinationPolicy() {
		return destinationPolicy;
	}
	/**
	 * Sets the destination specific policies available either for exact destinations or for
	 * wildcard areas of destinations.
	 */
	public void setDestinationPolicy(PolicyMap policyMap) {
		this.destinationPolicy = policyMap;
	}
	public BrokerPlugin[] getPlugins() {
		return plugins;
	}
	/**
	 * Sets a number of broker plugins to install such as for security authentication or
	 * authorization
	 */
	public void setPlugins(BrokerPlugin[] plugins) {
		this.plugins = plugins;
	}
	public MessageAuthorizationPolicy getMessageAuthorizationPolicy() {
		return messageAuthorizationPolicy;
	}
	/**
	 * Sets the policy used to decide if the current connection is authorized to consume a given
	 * message
	 */
	public void setMessageAuthorizationPolicy(MessageAuthorizationPolicy messageAuthorizationPolicy) {
		this.messageAuthorizationPolicy = messageAuthorizationPolicy;
	}
	/**
	 * Delete all messages from the persistent store
	 * @throws IOException
	 */
	/**
	 * 在启动的过程中，如果需要删除以前的消息，需要调用持久化适配器的删除消息方法，将以前的消息删除
	 * @Title: deleteAllMessages
	 * @Description: TODO
	 * @throws IOException
	 * @return: void
	 */
	public void deleteAllMessages() throws IOException {
		getPersistenceAdapter().deleteAllMessages();
	}
	public boolean isDeleteAllMessagesOnStartup() {
		return deleteAllMessagesOnStartup;
	}
	/**
	 * Sets whether or not all messages are deleted on startup - mostly only useful for testing.
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setDeleteAllMessagesOnStartup(boolean deletePersistentMessagesOnStartup) {
		this.deleteAllMessagesOnStartup = deletePersistentMessagesOnStartup;
	}
	/**
	 * 获得vm的uri，如果vmConnectorURI已经存在，则直接返回，如果不存在，就根据brokerName的构造
	 * @Title: getVmConnectorURI
	 * @Description: TODO
	 * @return
	 * @return: URI
	 */
	public URI getVmConnectorURI() {
		if (vmConnectorURI == null) {
			try {
				// 将除字母数字以及.-_之外的其他字符全部替换为“_”
				vmConnectorURI = new URI("vm://" + getBrokerName().replaceAll("[^a-zA-Z0-9\\.\\_\\-]", "_"));
			} catch (URISyntaxException e) {
				LOG.error("Badly formed URI from {}", getBrokerName(), e);
			}
		}
		return vmConnectorURI;
	}
	public void setVmConnectorURI(URI vmConnectorURI) {
		this.vmConnectorURI = vmConnectorURI;
	}
	/**
	 * 从transportConnectors集合中挑选合适的url进行返回
	 * @Title: getDefaultSocketURIString
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public String getDefaultSocketURIString() {
		if (started.get()) {
			if (this.defaultSocketURIString == null) {
				for (TransportConnector tc : this.transportConnectors) {
					String result = null;
					try {
						// 获得可以发布的url
						result = tc.getPublishableConnectString();
					} catch (Exception e) {
						LOG.warn("Failed to get the ConnectURI for {}", tc, e);
					}
					if (result != null) {
						// find first publishable uri
						// 根据TransportConnector的要求，这个url是否可以发布进行设置
						if (tc.isUpdateClusterClients() || tc.isRebalanceClusterClients()) {
							this.defaultSocketURIString = result;
							break;
						} else {
							// or use the first defined
							if (this.defaultSocketURIString == null) {
								this.defaultSocketURIString = result;
							}
						}
					}
				}
			}
			return this.defaultSocketURIString;
		}
		return null;
	}
	/**
	 * @return Returns the shutdownOnMasterFailure.
	 */
	public boolean isShutdownOnMasterFailure() {
		return shutdownOnMasterFailure;
	}
	/**
	 * @param shutdownOnMasterFailure The shutdownOnMasterFailure to set.
	 */
	public void setShutdownOnMasterFailure(boolean shutdownOnMasterFailure) {
		this.shutdownOnMasterFailure = shutdownOnMasterFailure;
	}
	public boolean isKeepDurableSubsActive() {
		return keepDurableSubsActive;
	}
	public void setKeepDurableSubsActive(boolean keepDurableSubsActive) {
		this.keepDurableSubsActive = keepDurableSubsActive;
	}
	public boolean isUseVirtualTopics() {
		return useVirtualTopics;
	}
	/**
	 * Sets whether or not <a href="http://activemq.apache.org/virtual-destinations.html">Virtual
	 * Topics</a> should be supported by default if they have not been explicitly configured.
	 */
	public void setUseVirtualTopics(boolean useVirtualTopics) {
		this.useVirtualTopics = useVirtualTopics;
	}
	public DestinationInterceptor[] getDestinationInterceptors() {
		return destinationInterceptors;
	}
	public boolean isUseMirroredQueues() {
		return useMirroredQueues;
	}
	/**
	 * Sets whether or not <a href="http://activemq.apache.org/mirrored-queues.html">Mirrored
	 * Queues</a> should be supported by default if they have not been explicitly configured.
	 */
	public void setUseMirroredQueues(boolean useMirroredQueues) {
		this.useMirroredQueues = useMirroredQueues;
	}
	/**
	 * Sets the destination interceptors to use
	 */
	public void setDestinationInterceptors(DestinationInterceptor[] destinationInterceptors) {
		this.destinationInterceptors = destinationInterceptors;
	}
	public ActiveMQDestination[] getDestinations() {
		return destinations;
	}
	/**
	 * Sets the destinations which should be loaded/created on startup
	 */
	public void setDestinations(ActiveMQDestination[] destinations) {
		this.destinations = destinations;
	}
	/**
	 * @return the tempDataStore
	 */
	/**
	 * 创建临时数据存储器，临时数据存储器需要实现PListStore接口，临时数据存储器将数据存储在临时数据目录，如果不需要持久化存储，那么就不需要临时数据存储器
	 * @Title: getTempDataStore
	 * @Description: TODO
	 * @return
	 * @return: PListStore
	 */
	public synchronized PListStore getTempDataStore() {
		// 如果tempDataStore不为null，就直接返回
		if (tempDataStore == null) {
			// 如果不持久化存储就不需要临时存储器
			if (!isPersistent()) {
				return null;
			}
			try {
				// 如果持久化存储器已经实现了PListStore接口，那么就返回该持久化存储器对象
				PersistenceAdapter pa = getPersistenceAdapter();
				if (pa != null && pa instanceof PListStore) {
					return (PListStore) pa;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			// 记录临时数据目录下的文件是否完全删除，是则为true
			boolean result = true;
			// 记录临时数据目录下是否有文件或目录，有为false，没有为true
			boolean empty = true;
			try {
				// 获得临时数据的存储目录
				File directory = getTmpDataDirectory();
				if (directory.exists() && directory.isDirectory()) {
					File[] files = directory.listFiles();
					if (files != null && files.length > 0) {
						// 将本目录下的所有文件删除，目录不删除
						empty = false;
						for (int i = 0; i < files.length; i++) {
							File file = files[i];
							if (!file.isDirectory()) {
								result &= file.delete();
							}
						}
					}
				}
				if (!empty) {
					String str = result ? "Successfully deleted" : "Failed to delete";
					LOG.info("{} temporary storage", str);
				}
				String clazz = "org.apache.activemq.store.kahadb.plist.PListStoreImpl";
				// 创建PListStoreImpl对象，作为临时数据存储器，设置临时目录和将本对象注入PListStoreImpl对象中
				this.tempDataStore = (PListStore) getClass().getClassLoader().loadClass(clazz).newInstance();
				this.tempDataStore.setDirectory(getTmpDataDirectory());
				configureService(tempDataStore);
				// 启动PListStoreImpl对象
				this.tempDataStore.start();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return tempDataStore;
	}
	/**
	 * @param tempDataStore the tempDataStore to set
	 */
	public void setTempDataStore(PListStore tempDataStore) {
		this.tempDataStore = tempDataStore;
		configureService(tempDataStore);
		try {
			tempDataStore.start();
		} catch (Exception e) {
			RuntimeException exception = new RuntimeException("Failed to start provided temp data store: "
					+ tempDataStore, e);
			LOG.error(exception.getLocalizedMessage(), e);
			throw exception;
		}
	}
	public int getPersistenceThreadPriority() {
		return persistenceThreadPriority;
	}
	public void setPersistenceThreadPriority(int persistenceThreadPriority) {
		this.persistenceThreadPriority = persistenceThreadPriority;
	}
	/**
	 * @return the useLocalHostBrokerName
	 */
	public boolean isUseLocalHostBrokerName() {
		return this.useLocalHostBrokerName;
	}
	/**
	 * @param useLocalHostBrokerName the useLocalHostBrokerName to set
	 */
	public void setUseLocalHostBrokerName(boolean useLocalHostBrokerName) {
		this.useLocalHostBrokerName = useLocalHostBrokerName;
		if (useLocalHostBrokerName && !started.get() && brokerName == null || brokerName == DEFAULT_BROKER_NAME) {
			brokerName = LOCAL_HOST_NAME;
		}
	}
	/**
	 * Looks up and lazily creates if necessary the destination for the given JMS name
	 */
	public Destination getDestination(ActiveMQDestination destination) throws Exception {
		return getBroker().addDestination(getAdminConnectionContext(), destination, false);
	}
	public void removeDestination(ActiveMQDestination destination) throws Exception {
		getBroker().removeDestination(getAdminConnectionContext(), destination, 0);
	}
	public int getProducerSystemUsagePortion() {
		return producerSystemUsagePortion;
	}
	public void setProducerSystemUsagePortion(int producerSystemUsagePortion) {
		this.producerSystemUsagePortion = producerSystemUsagePortion;
	}
	public int getConsumerSystemUsagePortion() {
		return consumerSystemUsagePortion;
	}
	public void setConsumerSystemUsagePortion(int consumerSystemUsagePortion) {
		this.consumerSystemUsagePortion = consumerSystemUsagePortion;
	}
	public boolean isSplitSystemUsageForProducersConsumers() {
		return splitSystemUsageForProducersConsumers;
	}
	public void setSplitSystemUsageForProducersConsumers(boolean splitSystemUsageForProducersConsumers) {
		this.splitSystemUsageForProducersConsumers = splitSystemUsageForProducersConsumers;
	}
	public boolean isMonitorConnectionSplits() {
		return monitorConnectionSplits;
	}
	public void setMonitorConnectionSplits(boolean monitorConnectionSplits) {
		this.monitorConnectionSplits = monitorConnectionSplits;
	}
	public int getTaskRunnerPriority() {
		return taskRunnerPriority;
	}
	public void setTaskRunnerPriority(int taskRunnerPriority) {
		this.taskRunnerPriority = taskRunnerPriority;
	}
	public boolean isDedicatedTaskRunner() {
		return dedicatedTaskRunner;
	}
	public void setDedicatedTaskRunner(boolean dedicatedTaskRunner) {
		this.dedicatedTaskRunner = dedicatedTaskRunner;
	}
	public boolean isCacheTempDestinations() {
		return cacheTempDestinations;
	}
	public void setCacheTempDestinations(boolean cacheTempDestinations) {
		this.cacheTempDestinations = cacheTempDestinations;
	}
	public int getTimeBeforePurgeTempDestinations() {
		return timeBeforePurgeTempDestinations;
	}
	public void setTimeBeforePurgeTempDestinations(int timeBeforePurgeTempDestinations) {
		this.timeBeforePurgeTempDestinations = timeBeforePurgeTempDestinations;
	}
	public boolean isUseTempMirroredQueues() {
		return useTempMirroredQueues;
	}
	public void setUseTempMirroredQueues(boolean useTempMirroredQueues) {
		this.useTempMirroredQueues = useTempMirroredQueues;
	}
	/**
	 * 创建工作调用器，如果不需要持久化存储，那么就创建一个内存调度器，如果需要就根据持久化存储器创建对应的内存调度器
	 * @Title: getJobSchedulerStore
	 * @Description: TODO
	 * @return
	 * @return: JobSchedulerStore
	 */
	public synchronized JobSchedulerStore getJobSchedulerStore() {
		// If support is off don't allow any scheduler even is user configured their own.
		if (!isSchedulerSupport()) {
			return null;
		}
		// If the user configured their own we use it even if persistence is disabled since
		// we don't know anything about their implementation.
		if (jobSchedulerStore == null) {
			if (!isPersistent()) {
				// 非持久化存储的使用内存调度器
				this.jobSchedulerStore = new InMemoryJobSchedulerStore();
				configureService(jobSchedulerStore);
				return this.jobSchedulerStore;
			}
			try {
				// 使用了持久化适配器的，使用该适配器创建对应的工作调度器
				PersistenceAdapter pa = getPersistenceAdapter();
				if (pa != null) {
					this.jobSchedulerStore = pa.createJobSchedulerStore();
					jobSchedulerStore.setDirectory(getSchedulerDirectoryFile());
					configureService(jobSchedulerStore);
					return this.jobSchedulerStore;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedOperationException ex) {
				// It's ok if the store doesn't implement a scheduler.
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			try {
				// 如果该持久化适配器已经实现了JobSchedulerStore，就直接返回该JobSchedulerStore对象
				PersistenceAdapter pa = getPersistenceAdapter();
				if (pa != null && pa instanceof JobSchedulerStore) {
					// 在上面代码中抛出UnsupportedOperationException异常才会走下面的代码
					this.jobSchedulerStore = (JobSchedulerStore) pa;
					configureService(jobSchedulerStore);
					return this.jobSchedulerStore;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			// Load the KahaDB store as a last resort, this only works if KahaDB is
			// included at runtime, otherwise this will fail. User should disable
			// scheduler support if this fails.
			try {
				// 如果不存在持久化适配器，而且还是需要持久化存储的，那么就创建默认的调度器
				String clazz = "org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter";
				PersistenceAdapter adaptor = (PersistenceAdapter) getClass().getClassLoader().loadClass(clazz)
						.newInstance();
				jobSchedulerStore = adaptor.createJobSchedulerStore();
				jobSchedulerStore.setDirectory(getSchedulerDirectoryFile());
				configureService(jobSchedulerStore);
				LOG.info("JobScheduler using directory: {}", getSchedulerDirectoryFile());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return jobSchedulerStore;
	}
	public void setJobSchedulerStore(JobSchedulerStore jobSchedulerStore) {
		this.jobSchedulerStore = jobSchedulerStore;
		configureService(jobSchedulerStore);
	}
	//
	// Implementation methods
	// -------------------------------------------------------------------------
	/**
	 * Handles any lazy-creation helper properties which are added to make things easier to
	 * configure inside environments such as Spring
	 * @throws Exception
	 */
	/**
	 * 将transportConnectorURI、networkConnectorURI和jmsBridgeConnector转化为对应的Connector对象，
	 * 然后存入对应的Connector集合
	 * @Title: processHelperProperties
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	protected void processHelperProperties() throws Exception {
		if (transportConnectorURIs != null) {
			for (int i = 0; i < transportConnectorURIs.length; i++) {
				String uri = transportConnectorURIs[i];
				// 将uri转化为TransportConnector对象，然后存入TransportConnector集合中
				addConnector(uri);
			}
		}
		if (networkConnectorURIs != null) {
			for (int i = 0; i < networkConnectorURIs.length; i++) {
				String uri = networkConnectorURIs[i];
				addNetworkConnector(uri);
			}
		}
		if (jmsBridgeConnectors != null) {
			for (int i = 0; i < jmsBridgeConnectors.length; i++) {
				addJmsConnector(jmsBridgeConnectors[i]);
			}
		}
	}
	/**
	 * Check that the store usage limit is not greater than max usable space and adjust if it is
	 */
	protected void checkStoreUsageLimits() throws IOException {
		final SystemUsage usage = getSystemUsage();
		if (getPersistenceAdapter() != null) {
			PersistenceAdapter adapter = getPersistenceAdapter();
			checkUsageLimit(adapter.getDirectory(), usage.getStoreUsage(), usage.getStoreUsage().getPercentLimit());
			long maxJournalFileSize = 0;
			long storeLimit = usage.getStoreUsage().getLimit();
			if (adapter instanceof JournaledStore) {
				maxJournalFileSize = ((JournaledStore) adapter).getJournalMaxFileLength();
			}
			if (storeLimit > 0 && storeLimit < maxJournalFileSize) {
				LOG.error("Store limit is " + storeLimit / (1024 * 1024)
						+ " mb, whilst the max journal file size for the store is: " + maxJournalFileSize
						/ (1024 * 1024) + " mb, " + "the store will not accept any data when used.");
			}
		}
	}
	/**
	 * Check that temporary usage limit is not greater than max usable space and adjust if it is
	 */
	protected void checkTmpStoreUsageLimits() throws IOException {
		final SystemUsage usage = getSystemUsage();
		File tmpDir = getTmpDataDirectory();
		if (tmpDir != null) {
			checkUsageLimit(tmpDir, usage.getTempUsage(), usage.getTempUsage().getPercentLimit());
			if (isPersistent()) {
				long maxJournalFileSize;
				PListStore store = usage.getTempUsage().getStore();
				if (store != null && store instanceof JournaledStore) {
					maxJournalFileSize = ((JournaledStore) store).getJournalMaxFileLength();
				} else {
					maxJournalFileSize = DEFAULT_MAX_FILE_LENGTH;
				}
				long storeLimit = usage.getTempUsage().getLimit();
				if (storeLimit > 0 && storeLimit < maxJournalFileSize) {
					LOG.error("Temporary Store limit is " + storeLimit / (1024 * 1024)
							+ " mb, whilst the max journal file size for the temporary store is: " + maxJournalFileSize
							/ (1024 * 1024) + " mb, " + "the temp store will not accept any data when used.");
				}
			}
		}
	}
	protected void checkUsageLimit(File dir, Usage<?> storeUsage, int percentLimit) {
		if (dir != null) {
			dir = StoreUtil.findParentDirectory(dir);
			String storeName = storeUsage instanceof StoreUsage ? "Store" : "Temporary Store";
			long storeLimit = storeUsage.getLimit();
			long storeCurrent = storeUsage.getUsage();
			long totalSpace = dir.getTotalSpace();
			long totalUsableSpace = dir.getUsableSpace() + storeCurrent;
			// compute byte value of the percent limit
			long bytePercentLimit = totalSpace * percentLimit / 100;
			int oneMeg = 1024 * 1024;
			// Check if the store limit is less than the percent Limit that was set and also
			// the usable space...this means we can grow the store larger
			// Changes in partition size (total space) as well as changes in usable space should
			// be detected here
			if (diskUsageCheckRegrowThreshold > -1 && percentLimit > 0 && storeLimit < bytePercentLimit
					&& storeLimit < totalUsableSpace) {
				// set the limit to be bytePercentLimit or usableSpace if
				// usableSpace is less than the percentLimit
				long newLimit = bytePercentLimit > totalUsableSpace ? totalUsableSpace : bytePercentLimit;
				// To prevent changing too often, check threshold
				if (newLimit - storeLimit >= diskUsageCheckRegrowThreshold) {
					LOG.info("Usable disk space has been increased, attempting to regrow " + storeName + " limit to "
							+ percentLimit + "% of the partition size.");
					storeUsage.setLimit(newLimit);
					LOG.info(storeName + " limit has been increased to " + newLimit * 100 / totalSpace + "% ("
							+ newLimit / oneMeg + " mb) of the partition size.");
				}
				// check if the limit is too large for the amount of usable space
			} else if (storeLimit > totalUsableSpace) {
				if (percentLimit > 0) {
					LOG.warn(storeName + " limit has been set to " + percentLimit + "% (" + bytePercentLimit / oneMeg
							+ " mb)" + " of the partition size but there is not enough usable space."
							+ " The current store limit (which may have been adjusted by a"
							+ " previous usage limit check) is set to (" + storeLimit / oneMeg + " mb)" + " but only "
							+ totalUsableSpace * 100 / totalSpace + "% (" + totalUsableSpace / oneMeg + " mb)"
							+ " is available - resetting limit");
				}
				LOG.warn(storeName + " limit is " + storeLimit / oneMeg + " mb (current store usage is " + storeCurrent
						/ oneMeg + " mb). The data directory: " + dir.getAbsolutePath() + " only has "
						+ totalUsableSpace / oneMeg
						+ " mb of usable space - resetting to maximum available disk space: " + totalUsableSpace
						/ oneMeg + " mb");
				storeUsage.setLimit(totalUsableSpace);
			}
		}
	}
	/**
	 * Schedules a periodic task based on schedulePeriodForDiskLimitCheck to update store and
	 * temporary store limits if the amount of available space plus current store size is less than
	 * the existin configured limit
	 */
	protected void scheduleDiskUsageLimitsCheck() throws IOException {
		if (schedulePeriodForDiskUsageCheck > 0 && (getPersistenceAdapter() != null || getTmpDataDirectory() != null)) {
			Runnable diskLimitCheckTask = new Runnable() {
				@Override
				public void run() {
					try {
						checkStoreUsageLimits();
					} catch (IOException e) {
						LOG.error("Failed to check persistent disk usage limits", e);
					}
					try {
						checkTmpStoreUsageLimits();
					} catch (IOException e) {
						LOG.error("Failed to check temporary store usage limits", e);
					}
				}
			};
			scheduler.executePeriodically(diskLimitCheckTask, schedulePeriodForDiskUsageCheck);
		}
	}
	protected void checkSystemUsageLimits() throws IOException {
		final SystemUsage usage = getSystemUsage();
		long memLimit = usage.getMemoryUsage().getLimit();
		long jvmLimit = Runtime.getRuntime().maxMemory();
		if (memLimit > jvmLimit) {
			usage.getMemoryUsage().setPercentOfJvmHeap(70);
			LOG.warn("Memory Usage for the Broker (" + memLimit / (1024 * 1024)
					+ " mb) is more than the maximum available for the JVM: " + jvmLimit / (1024 * 1024)
					+ " mb - resetting to 70% of maximum available: "
					+ (usage.getMemoryUsage().getLimit() / (1024 * 1024)) + " mb");
		}
		// Check the persistent store and temp store limits if they exist
		// and schedule a periodic check to update disk limits if
		// schedulePeriodForDiskLimitCheck is set
		checkStoreUsageLimits();
		checkTmpStoreUsageLimits();
		scheduleDiskUsageLimitsCheck();
		if (getJobSchedulerStore() != null) {
			JobSchedulerStore scheduler = getJobSchedulerStore();
			File schedulerDir = scheduler.getDirectory();
			if (schedulerDir != null) {
				String schedulerDirPath = schedulerDir.getAbsolutePath();
				if (!schedulerDir.isAbsolute()) {
					schedulerDir = new File(schedulerDirPath);
				}
				while (schedulerDir != null && !schedulerDir.isDirectory()) {
					schedulerDir = schedulerDir.getParentFile();
				}
				long schedulerLimit = usage.getJobSchedulerUsage().getLimit();
				long dirFreeSpace = schedulerDir.getUsableSpace();
				if (schedulerLimit > dirFreeSpace) {
					LOG.warn("Job Scheduler Store limit is " + schedulerLimit / (1024 * 1024)
							+ " mb, whilst the data directory: " + schedulerDir.getAbsolutePath() + " only has "
							+ dirFreeSpace / (1024 * 1024) + " mb of usable space - resetting to " + dirFreeSpace
							/ (1024 * 1024) + " mb.");
					usage.getJobSchedulerUsage().setLimit(dirFreeSpace);
				}
			}
		}
	}
	public void stopAllConnectors(ServiceStopper stopper) {
		for (Iterator<NetworkConnector> iter = getNetworkConnectors().iterator(); iter.hasNext();) {
			NetworkConnector connector = iter.next();
			unregisterNetworkConnectorMBean(connector);
			stopper.stop(connector);
		}
		for (Iterator<ProxyConnector> iter = getProxyConnectors().iterator(); iter.hasNext();) {
			ProxyConnector connector = iter.next();
			stopper.stop(connector);
		}
		for (Iterator<JmsConnector> iter = jmsConnectors.iterator(); iter.hasNext();) {
			JmsConnector connector = iter.next();
			stopper.stop(connector);
		}
		for (Iterator<TransportConnector> iter = getTransportConnectors().iterator(); iter.hasNext();) {
			TransportConnector connector = iter.next();
			try {
				unregisterConnectorMBean(connector);
			} catch (IOException e) {
			}
			stopper.stop(connector);
		}
	}
	protected TransportConnector registerConnectorMBean(TransportConnector connector) throws IOException {
		try {
			ObjectName objectName = createConnectorObjectName(connector);
			connector = connector.asManagedConnector(getManagementContext(), objectName);
			ConnectorViewMBean view = new ConnectorView(connector);
			AnnotatedMBean.registerMBean(getManagementContext(), view, objectName);
			return connector;
		} catch (Throwable e) {
			throw IOExceptionSupport.create("Transport Connector could not be registered in JMX: " + e, e);
		}
	}
	protected void unregisterConnectorMBean(TransportConnector connector) throws IOException {
		if (isUseJmx()) {
			try {
				ObjectName objectName = createConnectorObjectName(connector);
				getManagementContext().unregisterMBean(objectName);
			} catch (Throwable e) {
				throw IOExceptionSupport.create(
						"Transport Connector could not be unregistered in JMX: " + e.getMessage(), e);
			}
		}
	}
	/**
	 * 空实现
	 * @Title: registerPersistenceAdapterMBean
	 * @Description: TODO
	 * @param adaptor
	 * @return
	 * @throws IOException
	 * @return: PersistenceAdapter
	 */
	protected PersistenceAdapter registerPersistenceAdapterMBean(PersistenceAdapter adaptor) throws IOException {
		return adaptor;
	}
	protected void unregisterPersistenceAdapterMBean(PersistenceAdapter adaptor) throws IOException {
		if (isUseJmx()) {
		}
	}
	private ObjectName createConnectorObjectName(TransportConnector connector) throws MalformedObjectNameException {
		return BrokerMBeanSupport.createConnectorName(getBrokerObjectName(), "clientConnectors", connector.getName());
	}
	public void registerNetworkConnectorMBean(NetworkConnector connector) throws IOException {
		NetworkConnectorViewMBean view = new NetworkConnectorView(connector);
		try {
			ObjectName objectName = createNetworkConnectorObjectName(connector);
			connector.setObjectName(objectName);
			AnnotatedMBean.registerMBean(getManagementContext(), view, objectName);
		} catch (Throwable e) {
			throw IOExceptionSupport.create("Network Connector could not be registered in JMX: " + e.getMessage(), e);
		}
	}
	protected ObjectName createNetworkConnectorObjectName(NetworkConnector connector)
			throws MalformedObjectNameException {
		return BrokerMBeanSupport.createNetworkConnectorName(getBrokerObjectName(), "networkConnectors",
				connector.getName());
	}
	public ObjectName createDuplexNetworkConnectorObjectName(String transport) throws MalformedObjectNameException {
		return BrokerMBeanSupport.createNetworkConnectorName(getBrokerObjectName(), "duplexNetworkConnectors",
				transport);
	}
	protected void unregisterNetworkConnectorMBean(NetworkConnector connector) {
		if (isUseJmx()) {
			try {
				ObjectName objectName = createNetworkConnectorObjectName(connector);
				getManagementContext().unregisterMBean(objectName);
			} catch (Exception e) {
				LOG.warn("Network Connector could not be unregistered from JMX due " + e.getMessage()
						+ ". This exception is ignored.", e);
			}
		}
	}
	protected void registerProxyConnectorMBean(ProxyConnector connector) throws IOException {
		ProxyConnectorView view = new ProxyConnectorView(connector);
		try {
			ObjectName objectName = BrokerMBeanSupport.createNetworkConnectorName(getBrokerObjectName(),
					"proxyConnectors", connector.getName());
			AnnotatedMBean.registerMBean(getManagementContext(), view, objectName);
		} catch (Throwable e) {
			throw IOExceptionSupport.create("Broker could not be registered in JMX: " + e.getMessage(), e);
		}
	}
	protected void registerJmsConnectorMBean(JmsConnector connector) throws IOException {
		JmsConnectorView view = new JmsConnectorView(connector);
		try {
			ObjectName objectName = BrokerMBeanSupport.createNetworkConnectorName(getBrokerObjectName(),
					"jmsConnectors", connector.getName());
			AnnotatedMBean.registerMBean(getManagementContext(), view, objectName);
		} catch (Throwable e) {
			throw IOExceptionSupport.create("Broker could not be registered in JMX: " + e.getMessage(), e);
		}
	}
	/**
	 * Factory method to create a new broker
	 * @throws Exception
	 */
	/**
	 * 创建Broker对象，并对broker对象使用brokerfilter做了多层封装
	 * @Title: createBroker
	 * @Description: TODO
	 * @return
	 * @throws Exception
	 * @return: Broker
	 */
	protected Broker createBroker() throws Exception {
		// 获得一个broker对象
		regionBroker = createRegionBroker();
		// 使用brokerfilter对regionBroker进行封装
		Broker broker = addInterceptors(regionBroker);
		// Add a filter that will stop access to the broker once stopped
		// 下面这个对象相当于对broker做了一层简单的封装，所有对下面这个对象的方法调用，全部都会直接转换到底层对象上去
		// 只有stop和start方法除外
		broker = new MutableBrokerFilter(broker) {
			Broker old;

			@Override
			public void stop() throws Exception {
				old = this.next.getAndSet(new ErrorBroker("Broker has been stopped: " + this) {
					// Just ignore additional stop actions.
					@Override
					public void stop() throws Exception {
					}
				});
				old.stop();
			}
			@Override
			public void start() throws Exception {
				// forceStart表示强制重启，这个属性是在start方法中设置的，通过start方法的入参设置
				if (forceStart && old != null) {
					// 只有强制重启和old不为null时，才会启动真正的原始对象
					this.next.set(old);
				}
				getNext().start();
			}
		};
		return broker;
	}
	/**
	 * Factory method to create the core region broker onto which interceptors are added
	 * @throws Exception
	 */
	/**
	 * 创建Destination拦截器，创建DestinationFactoryImpl，最后根据拦截器创建broker对象
	 * @Title: createRegionBroker
	 * @Description: TODO
	 * @return
	 * @throws Exception
	 * @return: Broker
	 */
	protected Broker createRegionBroker() throws Exception {
		if (destinationInterceptors == null) {
			// 拦截器就是virtualtopic和镜像队列，下面这个方法创建了两个virtualtopic和镜像队列
			destinationInterceptors = createDefaultDestinationInterceptor();
		}
		// 如果拦截器对象中有实现了BrokerServiceAware接口的，就将本对象设置到拦截器对象中
		configureServices(destinationInterceptors);
		// 创建一个复合拦截器，在复合拦截器中对所有的拦截器进行统一调用
		DestinationInterceptor destinationInterceptor = new CompositeDestinationInterceptor(destinationInterceptors);
		if (destinationFactory == null) {
			// 从这里看出就算是不需要持久化存储，也需要一个持久化适配器，因为下面的代码如果持久化适配器为null，会抛出异常
			destinationFactory = new DestinationFactoryImpl(this, getTaskRunnerFactory(), getPersistenceAdapter());
		}
		// 创建broker对象
		return createRegionBroker(destinationInterceptor);
	}
	/**
	 * 根据Destination拦截器对象创建RegionBroker对象
	 * @Title: createRegionBroker
	 * @Description: TODO
	 * @param destinationInterceptor
	 * @return
	 * @throws IOException
	 * @return: Broker
	 */
	protected Broker createRegionBroker(DestinationInterceptor destinationInterceptor) throws IOException {
		RegionBroker regionBroker;
		// jmx不管
		if (isUseJmx()) {
			try {
				regionBroker = new ManagedRegionBroker(this, getManagementContext(), getBrokerObjectName(),
						getTaskRunnerFactory(), getConsumerSystemUsage(), destinationFactory, destinationInterceptor,
						getScheduler(), getExecutor());
			} catch (MalformedObjectNameException me) {
				LOG.warn("Cannot create ManagedRegionBroker due " + me.getMessage(), me);
				throw new IOException(me);
			}
		} else {
			// 创建一个broker对象
			regionBroker = new RegionBroker(this, getTaskRunnerFactory(), getConsumerSystemUsage(), destinationFactory,
					destinationInterceptor, getScheduler(), getExecutor());
		}
		destinationFactory.setRegionBroker(regionBroker);
		regionBroker.setKeepDurableSubsActive(keepDurableSubsActive);
		regionBroker.setBrokerName(getBrokerName());
		// 设置计数器已经可以使用了
		regionBroker.getDestinationStatistics().setEnabled(enableStatistics);
		regionBroker.setAllowTempAutoCreationOnSend(isAllowTempAutoCreationOnSend());
		if (brokerId != null) {
			regionBroker.setBrokerId(brokerId);
		}
		return regionBroker;
	}
	/**
	 * Create the default destination interceptor
	 */
	/**
	 * 创建了virtualtopic和镜像队列，这两个都属于DestinationInterceptor
	 * @Title: createDefaultDestinationInterceptor
	 * @Description: TODO
	 * @return
	 * @return: DestinationInterceptor[]
	 */
	protected DestinationInterceptor[] createDefaultDestinationInterceptor() {
		List<DestinationInterceptor> answer = new ArrayList<DestinationInterceptor>();
		if (isUseVirtualTopics()) {
			// 创建一个虚拟topic
			VirtualDestinationInterceptor interceptor = new VirtualDestinationInterceptor();
			VirtualTopic virtualTopic = new VirtualTopic();
			virtualTopic.setName("VirtualTopic.>");
			VirtualDestination[] virtualDestinations = { virtualTopic };
			// 在拦截器中设置虚拟的Destination对象
			interceptor.setVirtualDestinations(virtualDestinations);
			answer.add(interceptor);
		}
		if (isUseMirroredQueues()) {
			// 创建一个镜像队列
			MirroredQueue interceptor = new MirroredQueue();
			answer.add(interceptor);
		}
		DestinationInterceptor[] array = new DestinationInterceptor[answer.size()];
		answer.toArray(array);
		return array;
	}
	/**
	 * Strategy method to add interceptors to the broker
	 * @throws IOException
	 */
	/**
	 * 对broker进行各种封装，封装broker的对象都是一些brokerfilter，下面这个方法使得broker可以有事务
	 * @Title: addInterceptors
	 * @Description: TODO
	 * @param broker
	 * @return
	 * @throws Exception
	 * @return: Broker
	 */
	protected Broker addInterceptors(Broker broker) throws Exception {
		if (isSchedulerSupport()) {
			// 如果要求支持调度器，则创建SchedulerBroker对象，这个是一个BrokerFilter，是一个包装类
			SchedulerBroker sb = new SchedulerBroker(this, broker, getJobSchedulerStore());
			if (isUseJmx()) {
				JobSchedulerViewMBean view = new JobSchedulerView(sb.getJobScheduler());
				try {
					ObjectName objectName = BrokerMBeanSupport.createJobSchedulerServiceName(getBrokerObjectName());
					AnnotatedMBean.registerMBean(getManagementContext(), view, objectName);
					this.adminView.setJMSJobScheduler(objectName);
				} catch (Throwable e) {
					throw IOExceptionSupport
							.create("JobScheduler could not be registered in JMX: " + e.getMessage(), e);
				}
			}
			broker = sb;
		}
		// jmx不看
		if (isUseJmx()) {
			HealthViewMBean statusView = new HealthView((ManagedRegionBroker) getRegionBroker());
			try {
				ObjectName objectName = BrokerMBeanSupport.createHealthServiceName(getBrokerObjectName());
				AnnotatedMBean.registerMBean(getManagementContext(), statusView, objectName);
			} catch (Throwable e) {
				throw IOExceptionSupport.create("Status MBean could not be registered in JMX: " + e.getMessage(), e);
			}
		}
		if (isAdvisorySupport()) {
			broker = new AdvisoryBroker(broker);
		}
		broker = new CompositeDestinationBroker(broker);
		broker = new TransactionBroker(broker, getPersistenceAdapter().createTransactionStore());
		if (isPopulateJMSXUserID()) {
			UserIDBroker userIDBroker = new UserIDBroker(broker);
			userIDBroker.setUseAuthenticatePrincipal(isUseAuthenticatedPrincipalForJMSXUserID());
			broker = userIDBroker;
		}
		if (isMonitorConnectionSplits()) {
			broker = new ConnectionSplitBroker(broker);
		}
		if (plugins != null) {
			for (int i = 0; i < plugins.length; i++) {
				BrokerPlugin plugin = plugins[i];
				broker = plugin.installPlugin(broker);
			}
		}
		return broker;
	}
	/**
	 * 根据是否需要持久化，创建不同的持久化对象
	 * @Title: createPersistenceAdapter
	 * @Description: TODO
	 * @return
	 * @throws IOException
	 * @return: PersistenceAdapter
	 */
	protected PersistenceAdapter createPersistenceAdapter() throws IOException {
		if (isPersistent()) {
			// 如果需要持久化消息
			PersistenceAdapterFactory fac = getPersistenceFactory();
			if (fac != null) {
				return fac.createPersistenceAdapter();
			} else {
				try {
					// 如果没有配置PersistenceAdapterFactory，就使用默认的PersistenceAdapterFactory对象
					String clazz = "org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter";
					PersistenceAdapter adaptor = (PersistenceAdapter) getClass().getClassLoader().loadClass(clazz)
							.newInstance();
					// 默认的持久化方式会在broker的data目录下，创建一个KahaDB目录，然后文件数据存储在该目录下
					File dir = new File(getBrokerDataDirectory(), "KahaDB");
					// 持久化适配器负责目录的创建
					adaptor.setDirectory(dir);
					return adaptor;
				} catch (Throwable e) {
					throw IOExceptionSupport.create(e);
				}
			}
		} else {
			// 如果不需要持久化消息，就创建下面的对象
			return new MemoryPersistenceAdapter();
		}
	}
	protected ObjectName createBrokerObjectName() throws MalformedObjectNameException {
		return BrokerMBeanSupport.createBrokerObjectName(getManagementContext().getJmxDomainName(), getBrokerName());
	}
	/**
	 * 根据URI创建出TransportFactory对象，并将brokerService对象注入前者，同时TransportFactory对象创建出TransportServer对象，
	 * 并完成IP，端口的绑定, 该对象中持有一个ServerSocket对象，可以对外提供服务，最后将TransportServer对象注入TransportConnector对象中并返回
	 * @Title: createTransportConnector
	 * @Description: TODO
	 * @param brokerURI
	 * @return
	 * @throws Exception
	 * @return: TransportConnector
	 */
	protected TransportConnector createTransportConnector(URI brokerURI) throws Exception {
		// 根据URI创建出TransportFactory对象，并将brokerService对象注入前者，同时TransportFactory对象创建出TransportServer对象，并完成IP，端口的绑定
		// 该对象中持有一个ServerSocket对象，可以对外提供服务
		TransportServer transport = TransportFactorySupport.bind(this, brokerURI);
		// 将TransportServer对象注入TransportConnector对象中，一个TransportConnector对象对应一个TransportServer对象
		return new TransportConnector(transport);
	}
	/**
	 * Extracts the port from the options
	 */
	protected Object getPort(Map<?, ?> options) {
		Object port = options.get("port");
		if (port == null) {
			port = DEFAULT_PORT;
			LOG.warn("No port specified so defaulting to: {}", port);
		}
		return port;
	}
	protected void addShutdownHook() {
		if (useShutdownHook) {
			shutdownHook = new Thread("ActiveMQ ShutdownHook") {
				@Override
				public void run() {
					containerShutdown();
				}
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
	}
	protected void removeShutdownHook() {
		if (shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			} catch (Exception e) {
				LOG.debug("Caught exception, must be shutting down. This exception is ignored.", e);
			}
		}
	}
	/**
	 * Sets hooks to be executed when broker shut down
	 * @org.apache.xbean.Property
	 */
	public void setShutdownHooks(List<Runnable> hooks) throws Exception {
		for (Runnable hook : hooks) {
			addShutdownHook(hook);
		}
	}
	/**
	 * Causes a clean shutdown of the container when the VM is being shut down
	 */
	protected void containerShutdown() {
		try {
			stop();
		} catch (IOException e) {
			Throwable linkedException = e.getCause();
			if (linkedException != null) {
				logError("Failed to shut down: " + e + ". Reason: " + linkedException, linkedException);
			} else {
				logError("Failed to shut down: " + e, e);
			}
			if (!useLoggingForShutdownErrors) {
				e.printStackTrace(System.err);
			}
		} catch (Exception e) {
			logError("Failed to shut down: " + e, e);
		}
	}
	protected void logError(String message, Throwable e) {
		if (useLoggingForShutdownErrors) {
			LOG.error("Failed to shut down: " + e);
		} else {
			System.err.println("Failed to shut down: " + e);
		}
	}
	/**
	 * Starts any configured destinations on startup
	 */
	/**
	 * @Title: startDestinations
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	protected void startDestinations() throws Exception {
		// destinations表示本对象中queue和tpoic的集合
		if (destinations != null) {
			// 下面的这个getAdminConnectionContext方法中可能会创建Broker对象
			ConnectionContext adminConnectionContext = getAdminConnectionContext();
			for (int i = 0; i < destinations.length; i++) {
				ActiveMQDestination destination = destinations[i];
				getBroker().addDestination(adminConnectionContext, destination, true);
			}
		}
		if (isUseVirtualTopics()) {
			startVirtualConsumerDestinations();
		}
	}
	/**
	 * Returns the broker's administration connection context used for configuring the broker at
	 * startup
	 */
	/**
	 * 首先获得broker对象，然后得到一个与broker对象相关的ConnectionContext连接上下文对象
	 * @Title: getAdminConnectionContext
	 * @Description: TODO
	 * @return
	 * @throws Exception
	 * @return: ConnectionContext
	 */
	public ConnectionContext getAdminConnectionContext() throws Exception {
		// 首先获得broker对象，然后得到一个与broker对象相关的ConnectionContext对象
		return BrokerSupport.getConnectionContext(getBroker());
	}
	protected void startManagementContext() throws Exception {
		getManagementContext().setBrokerName(brokerName);
		getManagementContext().start();
		adminView = new BrokerView(this, null);
		ObjectName objectName = getBrokerObjectName();
		AnnotatedMBean.registerMBean(getManagementContext(), adminView, objectName);
	}
	/**
	 * Start all transport and network connections, proxies and bridges
	 * @throws Exception
	 */
	/**
	 * 启动所有的connection对象，分别调用connector对象的start方法
	 * @Title: startAllConnectors
	 * @Description: TODO
	 * @throws Exception
	 * @return: void
	 */
	public void startAllConnectors() throws Exception {
		Set<ActiveMQDestination> durableDestinations = getBroker().getDurableDestinations();
		List<TransportConnector> al = new ArrayList<TransportConnector>();
		// 启动TransportConnector对象
		for (Iterator<TransportConnector> iter = getTransportConnectors().iterator(); iter.hasNext();) {
			TransportConnector connector = iter.next();
			al.add(startTransportConnector(connector));// 已经启动了所有的connector，这些connector只是设置了一些属性，并没有具体的启动逻辑
		}
		if (al.size() > 0) {
			// let's clear the transportConnectors list and replace it with
			// the started transportConnector instances
			this.transportConnectors.clear();
			setTransportConnectors(al);// 将启动完毕的connector集合设置到本对象中
		}
		this.slave = false;
		URI uri = getVmConnectorURI();
		Map<String, String> map = new HashMap<String, String>(URISupport.parseParameters(uri));
		map.put("async", "false");
		uri = URISupport.createURIWithQuery(uri, URISupport.createQueryString(map));
		if (!stopped.get()) {
			// 在本对象没有停止运行的情况下，执行下面的操作
			ThreadPoolExecutor networkConnectorStartExecutor = null;
			if (isNetworkConnectorStartAsync()) {
				// spin up as many threads as needed
				networkConnectorStartExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.SECONDS,
						new SynchronousQueue<Runnable>(), new ThreadFactory() {
							int count = 0;

							@Override
							public Thread newThread(Runnable runnable) {
								Thread thread = new Thread(runnable, "NetworkConnector Start Thread-" + (count++));
								thread.setDaemon(true);
								return thread;
							}
						});
			}
			// 启动NetworkConnector
			for (Iterator<NetworkConnector> iter = getNetworkConnectors().iterator(); iter.hasNext();) {
				final NetworkConnector connector = iter.next();
				connector.setLocalUri(uri);
				connector.setBrokerName(getBrokerName());
				connector.setDurableDestinations(durableDestinations);
				if (getDefaultSocketURIString() != null) {
					connector.setBrokerURL(getDefaultSocketURIString());
				}
				if (networkConnectorStartExecutor != null) {
					networkConnectorStartExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								LOG.info("Async start of {}", connector);
								connector.start();
							} catch (Exception e) {
								LOG.error("Async start of network connector: {} failed", connector, e);
							}
						}
					});
				} else {
					connector.start();
				}
			}
			if (networkConnectorStartExecutor != null) {
				// executor done when enqueued tasks are complete
				// 执行器已经使用完毕，关闭执行器
				ThreadPoolUtils.shutdown(networkConnectorStartExecutor);
			}
			// 启动ProxyConnector对象
			for (Iterator<ProxyConnector> iter = getProxyConnectors().iterator(); iter.hasNext();) {
				ProxyConnector connector = iter.next();
				connector.start();
			}
			// 启动JmsConnector
			for (Iterator<JmsConnector> iter = jmsConnectors.iterator(); iter.hasNext();) {
				JmsConnector connector = iter.next();
				connector.start();
			}
			// 启动其他的一些服务
			for (Service service : services) {
				configureService(service);
				service.start();
			}
		}
	}
	public TransportConnector startTransportConnector(TransportConnector connector) throws Exception {
		connector.setBrokerService(this);
		connector.setTaskRunnerFactory(getTaskRunnerFactory());
		MessageAuthorizationPolicy policy = getMessageAuthorizationPolicy();
		if (policy != null) {
			connector.setMessageAuthorizationPolicy(policy);
		}
		if (isUseJmx()) {
			connector = registerConnectorMBean(connector);
		}
		connector.getStatistics().setEnabled(enableStatistics);
		connector.start();
		return connector;
	}
	/**
	 * Perform any custom dependency injection
	 */
	protected void configureServices(Object[] services) {
		for (Object service : services) {
			// 如果service对象实现了BrokerServiceAware接口，就将本对象设置到service对象中
			configureService(service);
		}
	}
	/**
	 * Perform any custom dependency injection
	 */
	/**
	 * 如果入参实现了BrokerServiceAware接口，就将本对象注入入参的对象中
	 * @Title: configureService
	 * @Description: TODO
	 * @param service
	 * @return: void
	 */
	protected void configureService(Object service) {
		if (service instanceof BrokerServiceAware) {
			BrokerServiceAware serviceAware = (BrokerServiceAware) service;
			serviceAware.setBrokerService(this);
		}
	}
	public void handleIOException(IOException exception) {
		if (ioExceptionHandler != null) {
			ioExceptionHandler.handle(exception);
		} else {
			LOG.info("No IOExceptionHandler registered, ignoring IO exception", exception);
		}
	}
	protected void startVirtualConsumerDestinations() throws Exception {
		ConnectionContext adminConnectionContext = getAdminConnectionContext();
		Set<ActiveMQDestination> destinations = destinationFactory.getDestinations();
		DestinationFilter filter = getVirtualTopicConsumerDestinationFilter();
		if (!destinations.isEmpty()) {
			for (ActiveMQDestination destination : destinations) {
				if (filter.matches(destination) == true) {
					broker.addDestination(adminConnectionContext, destination, false);
				}
			}
		}
	}
	private DestinationFilter getVirtualTopicConsumerDestinationFilter() {
		// created at startup, so no sync needed
		if (virtualConsumerDestinationFilter == null) {
			Set<ActiveMQQueue> consumerDestinations = new HashSet<ActiveMQQueue>();
			if (destinationInterceptors != null) {
				for (DestinationInterceptor interceptor : destinationInterceptors) {
					if (interceptor instanceof VirtualDestinationInterceptor) {
						VirtualDestinationInterceptor virtualDestinationInterceptor = (VirtualDestinationInterceptor) interceptor;
						for (VirtualDestination virtualDestination : virtualDestinationInterceptor
								.getVirtualDestinations()) {
							if (virtualDestination instanceof VirtualTopic) {
								consumerDestinations.add(new ActiveMQQueue(((VirtualTopic) virtualDestination)
										.getPrefix() + DestinationFilter.ANY_DESCENDENT));
							}
							if (isUseVirtualDestSubs()) {
								try {
									broker.virtualDestinationAdded(getAdminConnectionContext(), virtualDestination);
									LOG.debug("Adding virtual destination: {}", virtualDestination);
								} catch (Exception e) {
									LOG.warn("Could not fire virtual destination consumer advisory", e);
								}
							}
						}
					}
				}
			}
			ActiveMQQueue filter = new ActiveMQQueue();
			filter.setCompositeDestinations(consumerDestinations.toArray(new ActiveMQDestination[] {}));
			virtualConsumerDestinationFilter = DestinationFilter.parseFilter(filter);
		}
		return virtualConsumerDestinationFilter;
	}
	/**
	 * 创建线程池，线程池对象是ThreadPoolExecutor
	 * @Title: getExecutor
	 * @Description: TODO
	 * @return
	 * @return: ThreadPoolExecutor
	 */
	protected synchronized ThreadPoolExecutor getExecutor() {
		if (this.executor == null) {
			/**
			 * corePoolSize - 池中所保存的线程数，包括空闲线程。 maximumPoolSize - 池中允许的最大线程数。 keepAliveTime -
			 * 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。 unit - keepAliveTime 参数的时间单位。 workQueue -
			 * 执行前用于保持任务的队列。此队列仅保持由 execute 方法提交的 Runnable 任务。 threadFactory - 执行程序创建新线程时使用的工厂。
			 * handler - 由于超出线程范围和队列容量而使执行被阻塞时所使用的处理程序。
			 */
			this.executor = new ThreadPoolExecutor(1, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
						private long i = 0;

						@Override
						public Thread newThread(Runnable runnable) {
							this.i++;
							Thread thread = new Thread(runnable, "ActiveMQ BrokerService.worker." + this.i);
							// 后台线程
							thread.setDaemon(true);
							// 如果线程中出现异常，将异常信息打印即可
							thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
								@Override
								public void uncaughtException(final Thread t, final Throwable e) {
									LOG.error("Error in thread '{}'", t.getName(), e);
								}
							});
							return thread;
						}
					}, new RejectedExecutionHandler() {// 本对象有必要吗？
						@Override
						public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
							try {
								// 如果任务不能插入队列中，则等待60s再插入。
								executor.getQueue().offer(r, 60, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								throw new RejectedExecutionException("Interrupted waiting for BrokerService.worker");
							}
							throw new RejectedExecutionException("Timed Out while attempting to enqueue Task.");
						}
					});
		}
		return this.executor;
	}
	/**
	 * 创建一个任务调度器，可以方便的使用该调度器创建定时任务
	 * @Title: getScheduler
	 * @Description: TODO
	 * @return
	 * @return: Scheduler
	 */
	public synchronized Scheduler getScheduler() {
		if (this.scheduler == null) {
			// 创建任务调度器
			this.scheduler = new Scheduler("ActiveMQ Broker[" + getBrokerName() + "] Scheduler");
			try {
				// start基本可以看作是一个空实现
				this.scheduler.start();
			} catch (Exception e) {
				LOG.error("Failed to start Scheduler", e);
			}
		}
		return this.scheduler;
	}
	public Broker getRegionBroker() {
		return regionBroker;
	}
	public void setRegionBroker(Broker regionBroker) {
		this.regionBroker = regionBroker;
	}
	public void addShutdownHook(Runnable hook) {
		synchronized (shutdownHooks) {
			shutdownHooks.add(hook);
		}
	}
	public void removeShutdownHook(Runnable hook) {
		synchronized (shutdownHooks) {
			shutdownHooks.remove(hook);
		}
	}
	public boolean isSystemExitOnShutdown() {
		return systemExitOnShutdown;
	}
	/**
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setSystemExitOnShutdown(boolean systemExitOnShutdown) {
		this.systemExitOnShutdown = systemExitOnShutdown;
	}
	public int getSystemExitOnShutdownExitCode() {
		return systemExitOnShutdownExitCode;
	}
	public void setSystemExitOnShutdownExitCode(int systemExitOnShutdownExitCode) {
		this.systemExitOnShutdownExitCode = systemExitOnShutdownExitCode;
	}
	public SslContext getSslContext() {
		return sslContext;
	}
	public void setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
	}
	public boolean isShutdownOnSlaveFailure() {
		return shutdownOnSlaveFailure;
	}
	/**
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setShutdownOnSlaveFailure(boolean shutdownOnSlaveFailure) {
		this.shutdownOnSlaveFailure = shutdownOnSlaveFailure;
	}
	public boolean isWaitForSlave() {
		return waitForSlave;
	}
	/**
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setWaitForSlave(boolean waitForSlave) {
		this.waitForSlave = waitForSlave;
	}
	public long getWaitForSlaveTimeout() {
		return this.waitForSlaveTimeout;
	}
	public void setWaitForSlaveTimeout(long waitForSlaveTimeout) {
		this.waitForSlaveTimeout = waitForSlaveTimeout;
	}
	/**
	 * Get the passiveSlave
	 * @return the passiveSlave
	 */
	public boolean isPassiveSlave() {
		return this.passiveSlave;
	}
	/**
	 * Set the passiveSlave
	 * @param passiveSlave the passiveSlave to set
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setPassiveSlave(boolean passiveSlave) {
		this.passiveSlave = passiveSlave;
	}
	/**
	 * override the Default IOException handler, called when persistence adapter has experiences
	 * File or JDBC I/O Exceptions
	 * @param ioExceptionHandler
	 */
	public void setIoExceptionHandler(IOExceptionHandler ioExceptionHandler) {
		configureService(ioExceptionHandler);
		this.ioExceptionHandler = ioExceptionHandler;
	}
	public IOExceptionHandler getIoExceptionHandler() {
		return ioExceptionHandler;
	}
	/**
	 * @return the schedulerSupport
	 */
	public boolean isSchedulerSupport() {
		return this.schedulerSupport;
	}
	/**
	 * @param schedulerSupport the schedulerSupport to set
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.BooleanEditor"
	 */
	public void setSchedulerSupport(boolean schedulerSupport) {
		this.schedulerSupport = schedulerSupport;
	}
	/**
	 * @return the schedulerDirectory
	 */
	public File getSchedulerDirectoryFile() {
		if (this.schedulerDirectoryFile == null) {
			this.schedulerDirectoryFile = new File(getBrokerDataDirectory(), "scheduler");
		}
		return schedulerDirectoryFile;
	}
	/**
	 * @param schedulerDirectory the schedulerDirectory to set
	 */
	public void setSchedulerDirectoryFile(File schedulerDirectory) {
		this.schedulerDirectoryFile = schedulerDirectory;
	}
	public void setSchedulerDirectory(String schedulerDirectory) {
		setSchedulerDirectoryFile(new File(schedulerDirectory));
	}
	public int getSchedulePeriodForDestinationPurge() {
		return this.schedulePeriodForDestinationPurge;
	}
	public void setSchedulePeriodForDestinationPurge(int schedulePeriodForDestinationPurge) {
		this.schedulePeriodForDestinationPurge = schedulePeriodForDestinationPurge;
	}
	/**
	 * @param schedulePeriodForDiskUsageCheck
	 */
	public void setSchedulePeriodForDiskUsageCheck(int schedulePeriodForDiskUsageCheck) {
		this.schedulePeriodForDiskUsageCheck = schedulePeriodForDiskUsageCheck;
	}
	public int getDiskUsageCheckRegrowThreshold() {
		return diskUsageCheckRegrowThreshold;
	}
	/**
	 * @param diskUsageCheckRegrowThreshold
	 * @org.apache.xbean.Property propertyEditor="org.apache.activemq.util.MemoryPropertyEditor"
	 */
	public void setDiskUsageCheckRegrowThreshold(int diskUsageCheckRegrowThreshold) {
		this.diskUsageCheckRegrowThreshold = diskUsageCheckRegrowThreshold;
	}
	public int getMaxPurgedDestinationsPerSweep() {
		return this.maxPurgedDestinationsPerSweep;
	}
	public void setMaxPurgedDestinationsPerSweep(int maxPurgedDestinationsPerSweep) {
		this.maxPurgedDestinationsPerSweep = maxPurgedDestinationsPerSweep;
	}
	public BrokerContext getBrokerContext() {
		return brokerContext;
	}
	public void setBrokerContext(BrokerContext brokerContext) {
		this.brokerContext = brokerContext;
	}
	public void setBrokerId(String brokerId) {
		this.brokerId = new BrokerId(brokerId);
	}
	public boolean isUseAuthenticatedPrincipalForJMSXUserID() {
		return useAuthenticatedPrincipalForJMSXUserID;
	}
	public void setUseAuthenticatedPrincipalForJMSXUserID(boolean useAuthenticatedPrincipalForJMSXUserID) {
		this.useAuthenticatedPrincipalForJMSXUserID = useAuthenticatedPrincipalForJMSXUserID;
	}
	/**
	 * Should MBeans that support showing the Authenticated User Name information have this value
	 * filled in or not.
	 * @return true if user names should be exposed in MBeans
	 */
	public boolean isPopulateUserNameInMBeans() {
		return this.populateUserNameInMBeans;
	}
	/**
	 * Sets whether Authenticated User Name information is shown in MBeans that support this field.
	 * @param value if MBeans should expose user name information.
	 */
	public void setPopulateUserNameInMBeans(boolean value) {
		this.populateUserNameInMBeans = value;
	}
	/**
	 * Gets the time in Milliseconds that an invocation of an MBean method will wait before failing.
	 * The default value is to wait forever (zero).
	 * @return timeout in milliseconds before MBean calls fail, (default is 0 or no timeout).
	 */
	public long getMbeanInvocationTimeout() {
		return mbeanInvocationTimeout;
	}
	/**
	 * Gets the time in Milliseconds that an invocation of an MBean method will wait before failing.
	 * The default value is to wait forever (zero).
	 * @param mbeanInvocationTimeout timeout in milliseconds before MBean calls fail, (default is 0
	 *            or no timeout).
	 */
	public void setMbeanInvocationTimeout(long mbeanInvocationTimeout) {
		this.mbeanInvocationTimeout = mbeanInvocationTimeout;
	}
	public boolean isNetworkConnectorStartAsync() {
		return networkConnectorStartAsync;
	}
	public void setNetworkConnectorStartAsync(boolean networkConnectorStartAsync) {
		this.networkConnectorStartAsync = networkConnectorStartAsync;
	}
	public boolean isAllowTempAutoCreationOnSend() {
		return allowTempAutoCreationOnSend;
	}
	/**
	 * enable if temp destinations need to be propagated through a network when
	 * advisorySupport==false. This is used in conjunction with the policy gcInactiveDestinations
	 * for matching temps so they can get removed when inactive
	 * @param allowTempAutoCreationOnSend
	 */
	public void setAllowTempAutoCreationOnSend(boolean allowTempAutoCreationOnSend) {
		this.allowTempAutoCreationOnSend = allowTempAutoCreationOnSend;
	}
	public long getOfflineDurableSubscriberTimeout() {
		return offlineDurableSubscriberTimeout;
	}
	public void setOfflineDurableSubscriberTimeout(long offlineDurableSubscriberTimeout) {
		this.offlineDurableSubscriberTimeout = offlineDurableSubscriberTimeout;
	}
	public long getOfflineDurableSubscriberTaskSchedule() {
		return offlineDurableSubscriberTaskSchedule;
	}
	public void setOfflineDurableSubscriberTaskSchedule(long offlineDurableSubscriberTaskSchedule) {
		this.offlineDurableSubscriberTaskSchedule = offlineDurableSubscriberTaskSchedule;
	}
	public boolean shouldRecordVirtualDestination(ActiveMQDestination destination) {
		return isUseVirtualTopics() && destination.isQueue()
				&& getVirtualTopicConsumerDestinationFilter().matches(destination);
	}
	public Throwable getStartException() {
		return startException;
	}
	public boolean isStartAsync() {
		return startAsync;
	}
	public void setStartAsync(boolean startAsync) {
		this.startAsync = startAsync;
	}
	public boolean isSlave() {
		return this.slave;
	}
	public boolean isStopping() {
		return this.stopping.get();
	}
	/**
	 * @return true if the broker allowed to restart on shutdown.
	 */
	public boolean isRestartAllowed() {
		return restartAllowed;
	}
	/**
	 * Sets if the broker allowed to restart on shutdown.
	 */
	public void setRestartAllowed(boolean restartAllowed) {
		this.restartAllowed = restartAllowed;
	}
	/**
	 * A lifecycle manager of the BrokerService should inspect this property after a broker shutdown
	 * has occurred to find out if the broker needs to be re-created and started again.
	 * @return true if the broker wants to be restarted after it shuts down.
	 */
	public boolean isRestartRequested() {
		return restartRequested;
	}
	public void requestRestart() {
		this.restartRequested = true;
	}
	public int getStoreOpenWireVersion() {
		return storeOpenWireVersion;
	}
	public void setStoreOpenWireVersion(int storeOpenWireVersion) {
		this.storeOpenWireVersion = storeOpenWireVersion;
	}
	/**
	 * @return the current number of connections on this Broker.
	 */
	public int getCurrentConnections() {
		return this.currentConnections.get();
	}
	/**
	 * @return the total number of connections this broker has handled since startup.
	 */
	public long getTotalConnections() {
		return this.totalConnections.get();
	}
	public void incrementCurrentConnections() {
		this.currentConnections.incrementAndGet();
	}
	public void decrementCurrentConnections() {
		this.currentConnections.decrementAndGet();
	}
	public void incrementTotalConnections() {
		this.totalConnections.incrementAndGet();
	}
	public boolean isRejectDurableConsumers() {
		return rejectDurableConsumers;
	}
	public void setRejectDurableConsumers(boolean rejectDurableConsumers) {
		this.rejectDurableConsumers = rejectDurableConsumers;
	}
	public boolean isUseVirtualDestSubs() {
		return useVirtualDestSubs;
	}
	public void setUseVirtualDestSubs(boolean useVirtualDestSubs) {
		this.useVirtualDestSubs = useVirtualDestSubs;
	}
	public boolean isUseVirtualDestSubsOnCreation() {
		return useVirtualDestSubsOnCreation;
	}
	public void setUseVirtualDestSubsOnCreation(boolean useVirtualDestSubsOnCreation) {
		this.useVirtualDestSubsOnCreation = useVirtualDestSubsOnCreation;
	}
}
