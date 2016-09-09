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
package org.apache.activemq.console.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.List;

import org.apache.activemq.ActiveMQConnectionMetaData;
import org.apache.activemq.console.CommandContext;
import org.apache.activemq.util.IntrospectionSupport;

public abstract class AbstractCommand implements Command {
	public static final String COMMAND_OPTION_DELIMETER = ",";
	private boolean isPrintHelp;
	private boolean isPrintVersion;
	protected CommandContext context;

	public void setCommandContext(CommandContext context) {
		this.context = context;
	}
	/**
	 * Execute a generic command, which includes parsing the options for the command and running the
	 * specific task.
	 * @param tokens - command arguments
	 * @throws Exception
	 */
	/**
	 * ���ڴ���tokens�еĲ�����Ȼ����������е�runTask����
	 */
	public void execute(List<String> tokens) throws Exception {
		// Parse the options specified by "-"
		parseOptions(tokens);
		// Print the help file of the task
		if (isPrintHelp) {
			// ���������Ϣ
			printHelp();
			// Print the AMQ version
		} else if (isPrintVersion) {
			// ����汾��Ϣ
			context.printVersion(ActiveMQConnectionMetaData.PROVIDER_VERSION);
			// Run the specified task
		} else {
			runTask(tokens);
		}
	}
	/**
	 * @Title: parseOptions
	 * @Description: 
	 *               �����ԡ�-��Ϊǰ׺�Ĳ�������������ԡ�-��Ϊǰ׺���Ǿͽ������Ż�tokens�У���ֱ�ӷ��أ���������ԡ�-��Ϊǰ׺�Ĳ�������ζ����������ǲ����б��е����һ��������
	 *               ���ò���������ָʾ��ĳһ��������Ե����ʵ��ò���ʱ����ֱ�ӷ����ˣ����������Ĳ���������ֱ�Ӵ�tokens��ɾ����
	 * @param tokens
	 * @throws Exception
	 * @return: void
	 */
	protected void parseOptions(List<String> tokens) throws Exception {
		while (!tokens.isEmpty()) {
			String token = tokens.remove(0);
			// ֻ�����ԡ�-����ͷ�Ĳ����Ͳ���ֵ
			if (token.startsWith("-")) {
				// Token is an option
				handleOption(token, tokens);
			} else {
				// Push back to list of tokens
				// ��������ԡ�-����ͷ���ͷŻ�tokens�У�������
				tokens.add(0, token);
				return;
			}
		}
	}
	/**
	 * Handle the general options for each command, which includes -h, -?, --help, -D, --version.
	 * @param token - option token to handle
	 * @param tokens - succeeding command arguments
	 * @throws Exception
	 */
	/**
	 * @Title: handleOption
	 * @Description: �÷������ڴ�������������������ԡ�-��Ϊǰ׺�ģ��Բ����Ĵ����Ϊ���ַ�ʽ��
	 *               1��Ҫ�����������Ϣ����ô����ղ����б�����isPrintHelp������Ϊtrue��Ϊ��һ�����������Ϣ��׼�� 2������汾��Ϣ������ʽ���������������Ϣ
	 *               3��������-DΪǰ׺����ô�ͽ��������Ͳ���ֵʹ��System.setProperty����ϵͳ������
	 *               4����--Ϊǰ׺��������Ϊ�������setter������ʹ�÷������setter������������ֵע�뱾�����У�����������õĲ��Ϸ��������������Ϣ
	 * @param token
	 * @param tokens
	 * @throws Exception
	 * @return: void
	 */
	protected void handleOption(String token, List<String> tokens) throws Exception {
		isPrintHelp = false;
		isPrintVersion = false;
		// If token is a help option
		// ����а���ѡ�����������б�
		if (token.equals("-h") || token.equals("-?") || token.equals("--help")) {
			isPrintHelp = true;
			tokens.clear();
			// If token is a version option
		} else if (token.equals("--version")) {
			// �����Ҫ����汾������������б�
			isPrintVersion = true;
			tokens.clear();
		} else if (token.startsWith("-D")) {
			// �������-D��ͷ������ͽ��������Ͳ���ֵ����ϵͳ�����У���ʹ�÷�����System.setProperty
			// If token is a system property define option
			String key = token.substring(2);
			String value = "";
			int pos = key.indexOf("=");
			if (pos >= 0) {
				value = key.substring(pos + 1);
				key = key.substring(0, pos);
			}
			System.setProperty(key, value);
		} else {
			// ������ʽҪ��--������ ����ֵ������ֵ�����ԡ�-����ͷ
			if (token.startsWith("--")) {
				String prop = token.substring(2);
				// �������Ϸ������������Ϣ
				if (tokens.isEmpty() || tokens.get(0).startsWith("-")) {
					context.print("Property '" + prop + "' is not specified!");
				}
				// �����Ϸ����ҵ���������prop��Ӧ��setter������Ȼ�󽫲���ע�뱾������
				else if (IntrospectionSupport.setProperty(this, prop, tokens.remove(0))) {
					return;
				}
			}
			// Token is unrecognized
			context.printInfo("Unrecognized option: " + token);
			isPrintHelp = true;
		}
	}
	/**
	 * Run the specific task.
	 * @param tokens - command arguments
	 * @throws Exception
	 */
	protected abstract void runTask(List<String> tokens) throws Exception;
	/**
	 * Print the help messages for the specific task
	 */
	protected abstract void printHelp();
	protected void printHelpFromFile() {
		BufferedReader reader = null;
		try {
			InputStream is = getClass().getResourceAsStream(getName() + ".txt");
			reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				context.print(line);
			}
		} catch (Exception e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}
	protected void handleException(Exception exception, String serviceUrl) throws Exception {
		Throwable cause = exception.getCause();
		while (true) {
			Throwable next = cause.getCause();
			if (next == null) {
				break;
			}
			cause = next;
		}
		if (cause instanceof ConnectException) {
			context.printInfo("Broker not available at: " + serviceUrl);
		} else {
			context.printInfo("Failed to execute " + getName() + " task.");
			throw exception;
		}
	}
}
