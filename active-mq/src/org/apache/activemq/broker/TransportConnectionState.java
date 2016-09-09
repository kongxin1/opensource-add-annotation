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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.activemq.command.ConnectionInfo;

/**
 * 
 */
/**
 * �����˿ͻ��˴�������ʱ��connectioninfo�����TransportConnection�����Լ�ConnectionContext����
 * @ClassName: TransportConnectionState
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��23�� ����10:03:55
 */
public class TransportConnectionState extends org.apache.activemq.state.ConnectionState {
	private ConnectionContext context;
	private TransportConnection connection;
	// ��¼�ö���ʹ�õĴ���������һ���ͻ������ӵ�������ʱ������������Ծͻ��1
	private AtomicInteger referenceCounter = new AtomicInteger();
	// ���úͻ�������������
	private final Object connectionMutex = new Object();

	public TransportConnectionState(ConnectionInfo info, TransportConnection transportConnection) {
		super(info);
		connection = transportConnection;
	}
	public ConnectionContext getContext() {
		return context;
	}
	public TransportConnection getConnection() {
		return connection;
	}
	public void setContext(ConnectionContext context) {
		this.context = context;
	}
	public void setConnection(TransportConnection connection) {
		this.connection = connection;
	}
	public int incrementReference() {
		return referenceCounter.incrementAndGet();
	}
	public int decrementReference() {
		return referenceCounter.decrementAndGet();
	}
	public AtomicInteger getReferenceCounter() {
		return referenceCounter;
	}
	public void setReferenceCounter(AtomicInteger referenceCounter) {
		this.referenceCounter = referenceCounter;
	}
	public Object getConnectionMutex() {
		return connectionMutex;
	}
}
