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

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.Message;

/**
 * callback when the index is updated, allows ordered work to be seen by destinations
 */
public interface IndexListener {
	/**
	 * 消息上下文
	 * @ClassName: MessageContext
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年7月7日 上午8:12:15
	 */
	final class MessageContext {
		public final Message message;
		public final ConnectionContext context;
		public final Runnable onCompletion;
		public boolean duplicate;

		public MessageContext(ConnectionContext context, Message message, Runnable onCompletion) {
			this.context = context;
			this.message = message;
			this.onCompletion = onCompletion;
		}
	}

	/**
	 * called with some global index lock held so that a listener can do order dependent work non
	 * null MessageContext.onCompletion called when work is done
	 */
	public void onAdd(MessageContext messageContext);
}
