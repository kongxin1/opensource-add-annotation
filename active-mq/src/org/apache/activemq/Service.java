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
package org.apache.activemq;

/**
 * The core lifecyle interface for ActiveMQ components.
 *  
 * If there was a standard way to do so, it'd be good to register this 
 * interface with Spring so it treats the start/stop methods as those of
 * {@link org.springframework.beans.factory.InitializingBean} 
 * and {@link org.springframework.beans.factory.DisposableBean}
 * 
 * 
 */
/**
 * activemq中各个组件的生命周期接口，内部有两个方法，分别是start和stop方法，用于启动和终止组件运行
 * @ClassName: Service
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月3日 下午2:18:25
 */
public interface Service {
	void start() throws Exception;
	void stop() throws Exception;
}
