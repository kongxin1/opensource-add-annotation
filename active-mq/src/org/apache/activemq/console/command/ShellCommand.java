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
 * @Description: 系统启动时使用，启动时可以调用对应的命令，并且如果启动出现问题，会输出帮助信息并退出启动
 * @author: 孔新
 * @date: 2016年7月1日 下午11:51:15
 */
public class ShellCommand extends AbstractCommand {
	private boolean interactive;
	private String[] helpFile;

	public ShellCommand() {
		this(false);// 不进行交互
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
	 * @Description: Main类调用的本方法， args是除去了扩展目录参数之后的所有参数，这些参数是通过在命令行调用时输入的，输入的参数是以空格为分隔符， in是标准输入
	 *               out是标准输出，Main类中的方法调用本方法时，是通过反射的newInstance方法创建出的对象， 本方法的返回值直接返回到系统中
	 * @param args
	 * @param in
	 * @param out
	 * @return
	 * @return: int
	 */
	public static int main(String[] args, InputStream in, PrintStream out) {
		CommandContext context = new CommandContext();
		// 使用标准输出，在系统中所有的信息都是直接输出到屏幕的
		context.setFormatter(new CommandShellOutputFormatter(out));
		// Convert arguments to list for easier management
		List<String> tokens = new ArrayList<String>(Arrays.asList(args));
		// 系统中存在两个ShellCommand对象，一个是在这创建的，另一个是在Main类中创建的，Main类中创建的对象调用了本main方法
		ShellCommand main = new ShellCommand();
		try {
			// 使用标准输出，在系统中所有的信息都是直接输出到屏幕的
			main.setCommandContext(context);
			// args是除去了扩展目录参数之后的所有参数，这些参数是通过在命令行调用时输入的，输入的参数是以空格为分隔符
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
	 * 如果tokens中不存在参数，就输出帮助信息；此时tokens最多只有一个元素，即使有多个元素也只使用第一个元素，
	 * 根据该元素值找到对应的Command命令，然后调用Command对象的execute方法，如果找不到该Command对象就输出帮助信息。
	 * 每个Command对象都有一个名字，所以找对应的Command命令时，就直接使用字符串相等就可以了
	 */
	protected void runTask(List<String> tokens) throws Exception {
		// Process task token
		if (tokens.size() > 0) {
			// 此时tokens中只有一个元素
			Command command = null;
			String taskToken = (String) tokens.remove(0);
			// getCommands方法可以获得需要的所有Command对象
			for (Command c : getCommands()) {
				if (taskToken.equals(c.getName())) {
					// 找到名字匹配的Command对象，对于刚启动时，taskToken就是start，所以c就是CreateCommand对象
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
				// 如果是处于系统启动过程中，那么下面的方法就是调用StartCommand对象，不是CreateCommand对象
				command.execute(tokens);
			}
		} else {
			printHelp();
		}
	}
	/**
	 * @Title: getCommands
	 * @Description:通过使用ServiceLoader将需要的Command对象加载进内存，并放入集合中作为返回值返回
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
