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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 对象工厂搜寻类，根据需要加载的文件名，找到文件内容，根据文件内容加载对应的对象
 * @ClassName: FactoryFinder
 * @Description: 对象工厂搜寻类
 * @author: 孔新
 * @date: 2016年7月3日 上午12:16:57
 */
public class FactoryFinder {
	/**
	 * The strategy that the FactoryFinder uses to find load and instantiate Objects can be changed
	 * out by calling the
	 * {@link org.apache.activemq.util.FactoryFinder#setObjectFactory(org.apache.activemq.util.FactoryFinder.ObjectFactory)}
	 * method with a custom implementation of ObjectFactory. The default ObjectFactory is typically
	 * changed out when running in a specialized container environment where service discovery needs
	 * to be done via the container system. For example, in an OSGi scenario.
	 */
	/**
	 * @ClassName: ObjectFactory
	 * @Description: 对象工厂接口，根据create方法以及入参path可以创建需要的对象
	 * @author: 孔新
	 * @date: 2016年7月3日 上午12:04:45
	 */
	public interface ObjectFactory {
		/**
		 * @param path the full service path
		 * @return
		 */
		public Object create(String path) throws IllegalAccessException, InstantiationException, IOException,
				ClassNotFoundException;
	}

	/**
	 * The default implementation of Object factory which works well in standalone applications.
	 */
	/**
	 * @ClassName: StandaloneObjectFactory
	 * @Description: 
	 *               ObjectFactory接口的默认实现，里面有一个Map属性，存储了路径与Class对象的对应关系，可以根据输入的path值找到对应的Class对象，并创建出其实例并返回
	 * @author: 孔新
	 * @date: 2016年7月3日 上午12:05:26
	 */
	protected static class StandaloneObjectFactory implements ObjectFactory {
		final ConcurrentMap<String, Class> classMap = new ConcurrentHashMap<String, Class>();

		@Override
		public Object create(final String path) throws InstantiationException, IllegalAccessException,
				ClassNotFoundException, IOException {
			Class clazz = classMap.get(path);
			if (clazz == null) {
				clazz = loadClass(loadProperties(path));
				classMap.put(path, clazz);
			}
			return clazz.newInstance();
		}
		/**
		 * @Title: loadClass
		 * @Description: 使用本线程或FactoryFinder的类加载器加载class对象
		 * @param properties
		 * @return
		 * @throws ClassNotFoundException
		 * @throws IOException
		 * @return: Class
		 */
		static public Class loadClass(Properties properties) throws ClassNotFoundException, IOException {
			String className = properties.getProperty("class");
			if (className == null) {
				throw new IOException("Expected property is missing: class");
			}
			Class clazz = null;
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader != null) {
				try {
					clazz = loader.loadClass(className);
				} catch (ClassNotFoundException e) {
					// ignore
				}
			}
			if (clazz == null) {
				clazz = FactoryFinder.class.getClassLoader().loadClass(className);
			}
			return clazz;
		}
		/**
		 * @Title: loadProperties
		 * @Description: 
		 *               找到uri路径对应的文件，并分析加载进内存，文件解析后形成一个Properties对象，每一个String对应一个Class全限定名。默认启动方式中对应文件中的值是
		 *               ：class=org.apache.activemq.xbean.XBeanBrokerFactory
		 * @param uri
		 * @return
		 * @throws IOException
		 * @return: Properties
		 */
		static public Properties loadProperties(String uri) throws IOException {
			// lets try the thread context class loader first
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader == null) {
				classLoader = StandaloneObjectFactory.class.getClassLoader();
			}
			InputStream in = classLoader.getResourceAsStream(uri);
			if (in == null) {
				in = FactoryFinder.class.getClassLoader().getResourceAsStream(uri);
				if (in == null) {
					throw new IOException("Could not find factory class for resource: " + uri);
				}
			}
			// lets load the file
			BufferedInputStream reader = null;
			try {
				reader = new BufferedInputStream(in);
				Properties properties = new Properties();
				properties.load(reader);
				return properties;
			} finally {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// ================================================================
	// Class methods and properties
	// ================================================================
	// 单例
	private static ObjectFactory objectFactory = new StandaloneObjectFactory();

	public static ObjectFactory getObjectFactory() {
		return objectFactory;
	}
	public static void setObjectFactory(ObjectFactory objectFactory) {
		FactoryFinder.objectFactory = objectFactory;
	}

	// ================================================================
	// Instance methods and properties
	// ================================================================
	private final String path;

	public FactoryFinder(String path) {
		this.path = path;
	}
	/**
	 * Creates a new instance of the given key
	 * @param key is the key to add to the path to find a text file containing the factory name
	 * @return a newly created instance
	 */
	public Object newInstance(String key) throws IllegalAccessException, InstantiationException, IOException,
			ClassNotFoundException {
		return objectFactory.create(path + key);
	}
}
