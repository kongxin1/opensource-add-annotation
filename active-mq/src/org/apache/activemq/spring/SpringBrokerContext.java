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
package org.apache.activemq.spring;

import java.util.Map;

import org.apache.activemq.broker.BrokerContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 该类实现了BrokerContext和ApplicationContextAware，该类的作用是可以在ApplicationContext中查找相应的对象bean，
 * 可以根据name或者Class进行搜寻
 * @ClassName: SpringBrokerContext
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月3日 下午1:47:11
 */
public class SpringBrokerContext implements BrokerContext, ApplicationContextAware {
	ApplicationContext applicationContext;
	String configurationUrl;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	/**
	 * 根据入参在ApplicationContext中查找相应的对象，如果找不到就返回null
	 * @ClassName: SpringBrokerContext
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年7月3日 下午1:47:11
	 */
	public Object getBean(String name) {
		try {
			return applicationContext.getBean(name);
		} catch (BeansException ex) {
			return null;
		}
	}
	/**
	 * 根据入参Class在ApplicationContext中查找相应的对象
	 * @ClassName: SpringBrokerContext
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年7月3日 下午1:47:11
	 */
	@SuppressWarnings("unchecked")
	public Map getBeansOfType(Class type) {
		return applicationContext.getBeansOfType(type);
	}
	public void setConfigurationUrl(String configurationUrl) {
		this.configurationUrl = configurationUrl;
	}
	public String getConfigurationUrl() {
		return configurationUrl;
	}
}
