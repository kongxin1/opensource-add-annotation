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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

/**
 * @ClassName: Utils
 * @Description: 只有一个方法resourceFromString，可以将uri转化为对应的资源，资源可以是一个磁盘文件，url，或者classpath路径下的文件
 * @author: 孔新
 * @date: 2016年7月3日 上午10:03:22
 */
public class Utils {
	/**
	 * @Title: resourceFromString
	 * @Description: uri是一个路径，根据该路径使用spring找到对应的资源，该资源可以一个磁盘文件，url，或者classpath路径下的文件
	 * @param uri
	 * @return
	 * @throws MalformedURLException
	 * @return: Resource
	 */
	public static Resource resourceFromString(String uri) throws MalformedURLException {
		Resource resource;
		File file = new File(uri);
		if (file.exists()) {
			resource = new FileSystemResource(uri);
		} else if (ResourceUtils.isUrl(uri)) {
			try {
				resource = new UrlResource(ResourceUtils.getURL(uri));
			} catch (FileNotFoundException e) {
				MalformedURLException malformedURLException = new MalformedURLException(uri);
				malformedURLException.initCause(e);
				throw malformedURLException;
			}
		} else {
			resource = new ClassPathResource(uri);
		}
		return resource;
	}
}
