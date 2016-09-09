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
package org.apache.activemq.xbean;

import java.beans.PropertyEditorManager;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.activemq.broker.BrokerFactoryHandler;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.spring.SpringBrokerContext;
import org.apache.activemq.spring.Utils;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.URISupport;
import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
import org.apache.xbean.spring.context.impl.URIEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 
 */
public class XBeanBrokerFactory implements BrokerFactoryHandler {
	private static final transient Logger LOG = LoggerFactory.getLogger(XBeanBrokerFactory.class);
	static {
		// 注册一个属性编辑器，PropertyEditorManager是jdk提供的，和spring中属性编辑器很类似
		PropertyEditorManager.registerEditor(URI.class, URIEditor.class);
	}
	private boolean validate = true;

	public boolean isValidate() {
		return validate;
	}
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	/**
	 * config在系统启动的时候，config的值是xbean:activemq.xml，activemq.xml文件在classpath路径下
	 * 该类可以根据入参将入参中？号之后的内容进行分析
	 * ，并设置到本对象中，然后找到activemq.xml文件，生成ApplicationContext对象，从ApplicationContext对象获得BrokerService对象
	 * @ClassName: SpringBrokerContext
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年7月3日 下午1:47:11
	 */
	public BrokerService createBroker(URI config) throws Exception {
		// 获得config中去除了schema之后的内容，也就是：号之后的内容
		String uri = config.getSchemeSpecificPart();
		// 将？之后的内容注入本对象中，使用对应的setter方法注入
		if (uri.lastIndexOf('?') != -1) {
			IntrospectionSupport.setProperties(this, URISupport.parseQuery(uri));
			uri = uri.substring(0, uri.lastIndexOf('?'));
		}
		// 根据入参uri找到对应的资源文件，并使用ResourceXmlApplicationContext解析资源文件，得到spring中的ApplicationContext
		// 得到的ApplicationContext是XBeanBrokerFactory$1的实例，这个类是哪来的目前不知道，这个context中只有一个对象就是XBeanBrokerService
		ApplicationContext context = createApplicationContext(uri);
		// for (int i = 0; i < context.getBeanDefinitionCount(); i++) {
		// System.out.println(context.getBeanDefinitionNames()[i]);
		// }
		BrokerService broker = null;
		try {
			// 从context中拿到broker对象，此时的BrokerService对象还未启动
			broker = (BrokerService) context.getBean("broker");
		} catch (BeansException e) {
		}
		if (broker == null) {
			// 如果没有找到BrokerService对象，可能该对象是另外的名字，所以换种方式继续查找
			// lets try find by type
			String[] names = context.getBeanNamesForType(BrokerService.class);
			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				broker = (BrokerService) context.getBean(name);
				if (broker != null) {
					break;
				}
			}
		}
		// 找不到就报异常
		if (broker == null) {
			throw new IllegalArgumentException("The configuration has no BrokerService instance for resource: "
					+ config);
		}
		// 创建broker上下文，便于在ApplicationContext中查找需要的对象，对象是直接从ApplicationContext查找
		SpringBrokerContext springBrokerContext = new SpringBrokerContext();
		springBrokerContext.setApplicationContext(context);
		springBrokerContext.setConfigurationUrl(uri);
		// brokerService中间接持有ApplicationContext对象
		broker.setBrokerContext(springBrokerContext);
		// TODO warning resources from the context may not be closed down!
		return broker;
	}
	/**
	 * 根据入参uri找到对应的资源文件，并使用ResourceXmlApplicationContext解析资源文件，得到spring中的ApplicationContext，然后返回
	 * @Title: createApplicationContext
	 * @Description: TODO
	 * @param uri
	 * @return
	 * @throws MalformedURLException
	 * @return: ApplicationContext
	 */
	protected ApplicationContext createApplicationContext(String uri) throws MalformedURLException {
		// 找到uri对应的资源
		Resource resource = Utils.resourceFromString(uri);
		LOG.debug("Using " + resource + " from " + uri);
		try {
			// 将资源传入下面的对象中，可以解析资源得到ApplicationContext
			return new ResourceXmlApplicationContext(resource) {
				@Override
				protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
					reader.setValidating(isValidate());
				}
			};
		} catch (FatalBeanException errorToLog) {
			LOG.error("Failed to load: " + resource + ", reason: " + errorToLog.getLocalizedMessage(), errorToLog);
			throw errorToLog;
		}
	}
}
