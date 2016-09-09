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
package org.apache.activemq.store;

import java.io.File;

import org.apache.activemq.Service;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
/**
 * 不清楚作用，目前已知的是临时数据存储器需要实现本接口
 * @ClassName: PListStore
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月8日 下午9:47:27
 */
public interface PListStore extends Service {
	File getDirectory();
	void setDirectory(File directory);
	PList getPList(String name) throws Exception;
	boolean removePList(String name) throws Exception;
	long size();
}
