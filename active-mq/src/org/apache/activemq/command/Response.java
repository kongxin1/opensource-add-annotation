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
package org.apache.activemq.command;

import org.apache.activemq.state.CommandVisitor;

/**
 * @openwire:marshaller code="30"
 * 
 */
/**
 * 客户端或者服务器端的响应对象
 * @ClassName: Response
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月22日 上午8:23:04
 */
public class Response extends BaseCommand {
	public static final byte DATA_STRUCTURE_TYPE = CommandTypes.RESPONSE;
	int correlationId;

	public byte getDataStructureType() {
		return DATA_STRUCTURE_TYPE;
	}
	/**
	 * @openwire:property version=1
	 */
	public int getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(int responseId) {
		this.correlationId = responseId;
	}
	public boolean isResponse() {
		return true;
	}
	public boolean isException() {
		return false;
	}
	public Response visit(CommandVisitor visitor) throws Exception {
		return null;
	}
}
