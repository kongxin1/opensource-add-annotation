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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generator for Globally unique Strings.
 */
/**
 * 产生全局唯一的字符串
 * @ClassName: IdGenerator
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月9日 上午9:17:53
 */
public class IdGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(IdGenerator.class);
	private static final String UNIQUE_STUB;
	// 记录本类实例化的个数
	private static int instanceCount;
	// 主机名
	private static String hostName;
	private String seed;
	// sequence每获得一个id，sequence会自增长1
	private final AtomicLong sequence = new AtomicLong(1);
	private int length;
	public static final String PROPERTY_IDGENERATOR_HOSTNAME = "activemq.idgenerator.hostname";
	public static final String PROPERTY_IDGENERATOR_LOCALPORT = "activemq.idgenerator.localport";
	public static final String PROPERTY_IDGENERATOR_PORT = "activemq.idgenerator.port";
	static {
		String stub = "";
		// 是否可以访问系统属性
		boolean canAccessSystemProps = true;
		try {
			// 获得安全管理器
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				// 使用安全管理器检查是否允许本应用程序访问系统属性
				sm.checkPropertiesAccess();
			}
		} catch (SecurityException se) {
			// 如果抛出异常，则表示不允许应用程序访问系统属性
			canAccessSystemProps = false;
		}
		if (canAccessSystemProps) {
			hostName = System.getProperty(PROPERTY_IDGENERATOR_HOSTNAME);
			int localPort = Integer.parseInt(System.getProperty(PROPERTY_IDGENERATOR_LOCALPORT, "0"));
			int idGeneratorPort = 0;
			ServerSocket ss = null;
			try {
				if (hostName == null) {
					hostName = InetAddressUtil.getLocalHostName();
				}
				if (localPort == 0) {
					idGeneratorPort = Integer.parseInt(System.getProperty(PROPERTY_IDGENERATOR_PORT, "0"));
					LOG.trace("Using port {}", idGeneratorPort);
					// 创建绑定特定接口的服务器套接字，不知道为什么创建一个ServerSocket对象，感觉这个ServerSocket对象没什么作用，入参为0表示选择一个空闲端口创建
					ss = new ServerSocket(idGeneratorPort);
					// 通过使用ServerSocket进一步保证了stub的唯一
					localPort = ss.getLocalPort();
					stub = "-" + localPort + "-" + System.currentTimeMillis() + "-";
					Thread.sleep(100);
				} else {
					stub = "-" + localPort + "-" + System.currentTimeMillis() + "-";
				}
			} catch (Exception e) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("could not generate unique stub by using DNS and binding to local port", e);
				} else {
					LOG.warn("could not generate unique stub by using DNS and binding to local port: {} {}", e
							.getClass().getCanonicalName(), e.getMessage());
				}
				// Restore interrupted state so higher level code can deal with it.
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
			} finally {
				if (ss != null) {
					try {
						// TODO: replace the following line with IOHelper.close(ss) when Java 6
						// support is dropped
						ss.close();
					} catch (IOException ioe) {
						if (LOG.isTraceEnabled()) {
							LOG.trace("Closing the server socket failed", ioe);
						} else {
							LOG.warn("Closing the server socket failed" + " due " + ioe.getMessage());
						}
					}
				}
			}
		}
		// fallback
		if (hostName == null) {
			hostName = "localhost";
		}
		// 获得删除非ASCII码字符后的字符串
		hostName = sanitizeHostName(hostName);
		if (stub.length() == 0) {
			stub = "-1-" + System.currentTimeMillis() + "-";
		}
		UNIQUE_STUB = stub;
	}

	/**
	 * Construct an IdGenerator
	 */
	public IdGenerator(String prefix) {
		synchronized (UNIQUE_STUB) {
			this.seed = prefix + UNIQUE_STUB + (instanceCount++) + ":";
			this.length = this.seed.length() + ("" + Long.MAX_VALUE).length();
		}
	}
	public IdGenerator() {
		this("ID:" + hostName);
	}
	/**
	 * As we have to find the hostname as a side-affect of generating a unique stub, we allow it's
	 * easy retrieval here
	 * @return the local host name
	 */
	public static String getHostName() {
		return hostName;
	}
	/**
	 * Generate a unique id
	 * @return a unique id
	 */
	public synchronized String generateId() {
		StringBuilder sb = new StringBuilder(length);
		sb.append(seed);
		sb.append(sequence.getAndIncrement());
		return sb.toString();
	}
	/**
	 * 将hostName中的非ASCII码字符删除，并返回删除后的字符串
	 * @Title: sanitizeHostName
	 * @Description: TODO
	 * @param hostName
	 * @return
	 * @return: String
	 */
	public static String sanitizeHostName(String hostName) {
		boolean changed = false;
		StringBuilder sb = new StringBuilder();
		for (char ch : hostName.toCharArray()) {
			// only include ASCII chars
			if (ch < 127) {
				sb.append(ch);
			} else {
				changed = true;
			}
		}
		if (changed) {
			String newHost = sb.toString();
			LOG.info("Sanitized hostname from: {} to: {}", hostName, newHost);
			return newHost;
		} else {
			return hostName;
		}
	}
	/**
	 * Generate a unique ID - that is friendly for a URL or file system
	 * @return a unique id
	 */
	/**
	 * 生成一个id，但是这个id是经过处理的，将id中的:_.全部替换为-
	 * @Title: generateSanitizedId
	 * @Description: TODO
	 * @return
	 * @return: String
	 */
	public String generateSanitizedId() {
		String result = generateId();
		result = result.replace(':', '-');
		result = result.replace('_', '-');
		result = result.replace('.', '-');
		return result;
	}
	/**
	 * From a generated id - return the seed (i.e. minus the count)
	 * @param id the generated identifer
	 * @return the seed
	 */
	/**
	 * 从入参中拿到种子，种子就是入参中最后一个“:”前面的字符串
	 * @Title: getSeedFromId
	 * @Description: TODO
	 * @param id
	 * @return
	 * @return: String
	 */
	public static String getSeedFromId(String id) {
		String result = id;
		if (id != null) {
			int index = id.lastIndexOf(':');
			if (index > 0 && (index + 1) < id.length()) {
				result = id.substring(0, index);
			}
		}
		return result;
	}
	/**
	 * From a generated id - return the generator count
	 * @param id
	 * @return the count
	 */
	/**
	 * 从入参中拿到Sequence，Sequence就是入参中最后一个":"之后的字符串，并将字符串转化为long，如果不存在":",则返回-1
	 * @Title: getSequenceFromId
	 * @Description: TODO
	 * @param id
	 * @return
	 * @return: long
	 */
	public static long getSequenceFromId(String id) {
		long result = -1;
		if (id != null) {
			int index = id.lastIndexOf(':');
			if (index > 0 && (index + 1) < id.length()) {
				String numStr = id.substring(index + 1, id.length());
				result = Long.parseLong(numStr);
			}
		}
		return result;
	}
	/**
	 * Does a proper compare on the ids
	 * @param id1
	 * @param id2
	 * @return 0 if equal else a positive if id1 is > id2 ...
	 */
	/**
	 * 比较两个id的大小，首先以字符串中“:”前面的字符串为准，如果两个相等，那就继续比较":"后面的字符串
	 * @Title: compare
	 * @Description: TODO
	 * @param id1
	 * @param id2
	 * @return
	 * @return: int
	 */
	public static int compare(String id1, String id2) {
		int result = -1;
		String seed1 = IdGenerator.getSeedFromId(id1);
		String seed2 = IdGenerator.getSeedFromId(id2);
		if (seed1 != null && seed2 != null) {
			result = seed1.compareTo(seed2);
			if (result == 0) {
				long count1 = IdGenerator.getSequenceFromId(id1);
				long count2 = IdGenerator.getSequenceFromId(id2);
				result = (int) (count1 - count2);
			}
		}
		return result;
	}
}
