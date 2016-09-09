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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.activemq.console.CommandContext;
import org.apache.activemq.console.formatter.CommandShellOutputFormatter;

/**
 * @ClassName: ShellCommand
 * @Description: ϵͳ����ʱʹ�ã�����ʱ���Ե��ö�Ӧ�����������������������⣬�����������Ϣ���˳�����
 * @author: ����
 * @date: 2016��7��1�� ����11:51:15
 */
public class ShellCommand extends AbstractCommand {
	private boolean interactive;
	private String[] helpFile;

	public ShellCommand() {
		this(false);// �����н���
	}
	public ShellCommand(boolean interactive) {
		this.interactive = interactive;
		ArrayList<String> help = new ArrayList<String>();
		help.addAll(Arrays.asList(new String[] {
				interactive ? "Usage: [task] [task-options] [task data]"
						: "Usage: Main [--extdir <dir>] [task] [task-options] [task data]", "", "Tasks:" }));
		ArrayList<Command> commands = getCommands();
		Collections.sort(commands, new Comparator<Command>() {
			@Override
			public int compare(Command command, Command command1) {
				return command.getName().compareTo(command1.getName());
			}
		});
		for (Command command : commands) {
			help.add(String.format("    %-24s - %s", command.getName(), command.getOneLineDescription()));
		}
		help.addAll(Arrays
				.asList(new String[] {
						"",
						"Task Options (Options specific to each task):",
						"    --extdir <dir>  - Add the jar files in the directory to the classpath.",
						"    --version       - Display the version information.",
						"    -h,-?,--help    - Display this help information. To display task specific help, use "
								+ (interactive ? "" : "Main ") + "[task] -h,-?,--help",
						"",
						"Task Data:",
						"    - Information needed by each specific task.",
						"",
						"JMX system property options:",
						"    -Dactivemq.jmx.url=<jmx service uri> (default is: 'service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi')",
						"    -Dactivemq.jmx.user=<user name>", "    -Dactivemq.jmx.password=<password>", "" }));
		this.helpFile = help.toArray(new String[help.size()]);
	}
	@Override
	public String getName() {
		return "shell";
	}
	@Override
	public String getOneLineDescription() {
		return "Runs the activemq sub shell";
	}
	/**
	 * Main method to run a command shell client.
	 * @param args - command line arguments
	 * @param in - input stream to use
	 * @param out - output stream to use
	 * @return 0 for a successful run, -1 if there are any exception
	 */
	/**
	 * @Title: main
	 * @Description: Main����õı������� args�ǳ�ȥ����չĿ¼����֮������в�������Щ������ͨ���������е���ʱ����ģ�����Ĳ������Կո�Ϊ�ָ����� in�Ǳ�׼����
	 *               out�Ǳ�׼�����Main���еķ������ñ�����ʱ����ͨ�������newInstance�����������Ķ��� �������ķ���ֱֵ�ӷ��ص�ϵͳ��
	 * @param args
	 * @param in
	 * @param out
	 * @return
	 * @return: int
	 */
	public static int main(String[] args, InputStream in, PrintStream out) {
		CommandContext context = new CommandContext();
		// ʹ�ñ�׼�������ϵͳ�����е���Ϣ����ֱ���������Ļ��
		context.setFormatter(new CommandShellOutputFormatter(out));
		// Convert arguments to list for easier management
		List<String> tokens = new ArrayList<String>(Arrays.asList(args));
		// ϵͳ�д�������ShellCommand����һ�������ⴴ���ģ���һ������Main���д����ģ�Main���д����Ķ�������˱�main����
		ShellCommand main = new ShellCommand();
		try {
			// ʹ�ñ�׼�������ϵͳ�����е���Ϣ����ֱ���������Ļ��
			main.setCommandContext(context);
			// args�ǳ�ȥ����չĿ¼����֮������в�������Щ������ͨ���������е���ʱ����ģ�����Ĳ������Կո�Ϊ�ָ���
			main.execute(tokens);
			return 0;
		} catch (Exception e) {
			context.printException(e);
			return 1;
		}
	}
	public static void main(String[] args) {
		main(args, System.in, System.out);
	}
	public boolean isInteractive() {
		return interactive;
	}
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}
	/**
	 * Parses for specific command task.
	 * @param tokens - command arguments
	 * @throws Exception
	 */
	/**
	 * ���tokens�в����ڲ����������������Ϣ����ʱtokens���ֻ��һ��Ԫ�أ���ʹ�ж��Ԫ��Ҳֻʹ�õ�һ��Ԫ�أ�
	 * ���ݸ�Ԫ��ֵ�ҵ���Ӧ��Command���Ȼ�����Command�����execute����������Ҳ�����Command��������������Ϣ��
	 * ÿ��Command������һ�����֣������Ҷ�Ӧ��Command����ʱ����ֱ��ʹ���ַ�����ȾͿ�����
	 */
	protected void runTask(List<String> tokens) throws Exception {
		// Process task token
		if (tokens.size() > 0) {
			// ��ʱtokens��ֻ��һ��Ԫ��
			Command command = null;
			String taskToken = (String) tokens.remove(0);
			// getCommands�������Ի����Ҫ������Command����
			for (Command c : getCommands()) {
				if (taskToken.equals(c.getName())) {
					// �ҵ�����ƥ���Command���󣬶��ڸ�����ʱ��taskToken����start������c����CreateCommand����
					command = c;
					break;
				}
			}
			if (command == null) {
				if (taskToken.equals("help")) {
					printHelp();
				} else {
					printHelp();
				}
			}
			if (command != null) {
				command.setCommandContext(context);
				// ����Ǵ���ϵͳ���������У���ô����ķ������ǵ���StartCommand���󣬲���CreateCommand����
				command.execute(tokens);
			}
		} else {
			printHelp();
		}
	}
	/**
	 * @Title: getCommands
	 * @Description:ͨ��ʹ��ServiceLoader����Ҫ��Command������ؽ��ڴ棬�����뼯������Ϊ����ֵ����
	 * @return
	 * @return: ArrayList<Command>
	 */
	ArrayList<Command> getCommands() {
		/**
		 * org.apache.activemq.console.command.CreateCommand
		 * org.apache.activemq.console.command.StartCommand
		 * org.apache.activemq.console.command.ShutdownCommand
		 * org.apache.activemq.console.command.ListCommand
		 * org.apache.activemq.console.command.AmqBrowseCommand
		 * org.apache.activemq.console.command.QueryCommand
		 * org.apache.activemq.console.command.BstatCommand
		 * org.apache.activemq.console.command.DstatCommand
		 * org.apache.activemq.console.command.EncryptCommand
		 * org.apache.activemq.console.command.DecryptCommand
		 * org.apache.activemq.console.command.StoreExportCommand
		 * org.apache.activemq.console.command.PurgeCommand
		 * org.apache.activemq.console.command.ProducerCommand
		 * org.apache.activemq.console.command.ConsumerCommand
		 */
		ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
		Iterator<Command> iterator = loader.iterator();
		ArrayList<Command> rc = new ArrayList<Command>();
		boolean done = false;
		while (!done) {
			try {
				if (iterator.hasNext()) {
					rc.add(iterator.next());
				} else {
					done = true;
				}
			} catch (ServiceConfigurationError e) {
				// it's ok, some commands may not load if their dependencies
				// are not available.
			}
		}
		return rc;
	}
	/**
	 * Print the help messages for the browse command
	 */
	protected void printHelp() {
		context.printHelp(helpFile);
	}
}
