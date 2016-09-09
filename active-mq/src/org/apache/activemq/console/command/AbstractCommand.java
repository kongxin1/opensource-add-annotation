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
	 * 用于处理tokens中的参数，然后调用子类中的runTask方法
	 */
	public void execute(List<String> tokens) throws Exception {
		// Parse the options specified by "-"
		parseOptions(tokens);
		// Print the help file of the task
		if (isPrintHelp) {
			// 输出帮助信息
			printHelp();
			// Print the AMQ version
		} else if (isPrintVersion) {
			// 输出版本信息
			context.printVersion(ActiveMQConnectionMetaData.PROVIDER_VERSION);
			// Run the specified task
		} else {
			runTask(tokens);
		}
	}
	/**
	 * @Title: parseOptions
	 * @Description: 
	 *               处理以‘-’为前缀的参数，如果不是以‘-’为前缀，那就将参数放回tokens中，并直接返回，如果不是以‘-’为前缀的参数，意味着这个参数是参数列表中的最后一个参数了
	 *               ，该参数可能是指示的某一个命令，所以当访问到该参数时，就直接返回了，对于其他的参数处理后就直接从tokens中删除了
	 * @param tokens
	 * @throws Exception
	 * @return: void
	 */
	protected void parseOptions(List<String> tokens) throws Exception {
		while (!tokens.isEmpty()) {
			String token = tokens.remove(0);
			// 只处理以“-”开头的参数和参数值
			if (token.startsWith("-")) {
				// Token is an option
				handleOption(token, tokens);
			} else {
				// Push back to list of tokens
				// 如果不是以‘-’开头，就放回tokens中，并返回
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
	 * @Description: 该方法用于处理参数，参数名都是以‘-’为前缀的，对参数的处理分为四种方式：
	 *               1、要求输出帮助信息，那么就清空参数列表，并将isPrintHelp属性置为true，为下一步输出帮助信息做准备 2、输出版本信息，处理方式类似于输出帮助信息
	 *               3、参数以-D为前缀，那么就将参数名和参数值使用System.setProperty存入系统参数中
	 *               4、以--为前缀，参数名为本对象的setter方法，使用反射调用setter方法，将参数值注入本对象中，如果参数设置的不合法，就输出帮助信息
	 * @param token
	 * @param tokens
	 * @throws Exception
	 * @return: void
	 */
	protected void handleOption(String token, List<String> tokens) throws Exception {
		isPrintHelp = false;
		isPrintVersion = false;
		// If token is a help option
		// 如果有帮助选项，就清空命令列表
		if (token.equals("-h") || token.equals("-?") || token.equals("--help")) {
			isPrintHelp = true;
			tokens.clear();
			// If token is a version option
		} else if (token.equals("--version")) {
			// 如果需要输出版本，就清空命令列表
			isPrintVersion = true;
			tokens.clear();
		} else if (token.startsWith("-D")) {
			// 如果是以-D开头的命令，就将参数名和参数值存入系统属性中，即使用方法：System.setProperty
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
			// 参数格式要求：--参数名 参数值，参数值不能以‘-’开头
			if (token.startsWith("--")) {
				String prop = token.substring(2);
				// 参数不合法，输出帮助信息
				if (tokens.isEmpty() || tokens.get(0).startsWith("-")) {
					context.print("Property '" + prop + "' is not specified!");
				}
				// 参数合法，找到本对象中prop对应的setter方法，然后将参数注入本对象中
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
