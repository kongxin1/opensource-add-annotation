import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//activemq的主启动类
public class Main {
	public static final String TASK_DEFAULT_CLASS = "org.apache.activemq.console.command.ShellCommand";
	private static boolean useDefExt = true;// 使用默认的临时目录
	private File activeMQHome;
	private File activeMQBase;
	// 该类加载器是以Main的加载器为父类加载器，并且该类加载路径是输入系统中的classpath路径和扩展路径中的zip和jar文件
	private ClassLoader classLoader;
	private final Set<File> extensions = new LinkedHashSet();// 扩展目录
	private final Set<File> activeMQClassPath = new LinkedHashSet();// mq的class路径

	public static void main(String[] args) {
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));// 获取activemq的临时路径：%ACTIVEMQ_HOME%\data\tmp
		if (!tmpdir.exists()) {
			tmpdir.mkdirs();
		}
		Main app = new Main();
		List tokens = new LinkedList(Arrays.asList(args));// 从配置文件中来看，args里面就只有一个start
		// 下面的这个方法只是看看是否使用扩展目录，如果使用，其路径是什么，扩展目录的参数只能有一个
		app.parseExtensions(tokens);
		// 获取系统的conf目录
		File confDir = app.getActiveMQConfig();
		app.addClassPath(confDir);// conf路径也是classpath的路径之一
		// ShellCommand与扩展路径冲突
		// home和base目录可以不是同一个，但是两个目录的作用是相同的
		if ((useDefExt) && (app.canUseExtdir())) {
			boolean baseIsHome = app.getActiveMQBase().equals(app.getActiveMQHome());
			File baseLibDir = new File(app.getActiveMQBase(), "lib");
			File homeLibDir = new File(app.getActiveMQHome(), "lib");
			if (!baseIsHome) {
				app.addExtensionDirectory(baseLibDir);
			}
			app.addExtensionDirectory(homeLibDir);
			if (!baseIsHome) {
				app.addExtensionDirectory(new File(baseLibDir, "camel"));
				app.addExtensionDirectory(new File(baseLibDir, "optional"));
				app.addExtensionDirectory(new File(baseLibDir, "web"));
				app.addExtensionDirectory(new File(baseLibDir, "extra"));
			}
			app.addExtensionDirectory(new File(homeLibDir, "camel"));
			app.addExtensionDirectory(new File(homeLibDir, "optional"));
			app.addExtensionDirectory(new File(homeLibDir, "web"));
			app.addExtensionDirectory(new File(homeLibDir, "extra"));
		}
		app.addClassPathList(System.getProperty("activemq.classpath"));
		try {
			// tokens中是去除了扩展目录参数后的参数列表
			int ret = app.runTaskClass(tokens);
			System.exit(ret);
		} catch (ClassNotFoundException e) {
			System.out.println(new StringBuilder().append("Could not load class: ").append(e.getMessage()).toString());
			try {
				ClassLoader cl = app.getClassLoader();
				if (cl != null) {
					System.out.println("Class loader setup: ");
					// 输出类加载器的名字以及该类加载器加载资源的目录
					printClassLoaderTree(cl);
				}
			} catch (MalformedURLException localMalformedURLException) {
			}
			System.exit(1);
		} catch (Throwable e) {
			System.out
					.println(new StringBuilder().append("Failed to execute main task. Reason: ").append(e).toString());
			System.exit(1);
		}
	}
	private static int printClassLoaderTree(ClassLoader cl) {
		int depth = 0;
		if (cl.getParent() != null) {
			depth = printClassLoaderTree(cl.getParent()) + 1;
		}
		StringBuffer indent = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			indent.append("  ");
		}
		if ((cl instanceof URLClassLoader)) {
			URLClassLoader ucl = (URLClassLoader) cl;
			System.out.println(new StringBuilder().append(indent).append(cl.getClass().getName()).append(" {")
					.toString());
			// 返回用于加载类和资源的 URL 搜索路径。这包括为构造方法指定的原始 URL 列表，以及由 addURL() 方法后续添加的 URL。
			URL[] urls = ucl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				System.out.println(new StringBuilder().append(indent).append("  ").append(urls[i]).toString());
			}
			System.out.println(new StringBuilder().append(indent).append("}").toString());
		} else {
			System.out.println(new StringBuilder().append(indent).append(cl.getClass().getName()).toString());
		}
		return depth;
	}
	public void parseExtensions(List<String> tokens) {
		if (tokens.isEmpty()) {
			return;
		}
		int count = tokens.size();
		int i = 0;
		while (i < count) {
			String token = (String) tokens.get(i);
			if (token.equals("--extdir")) {
				count--;
				tokens.remove(i);// 从命令参数中去掉“--extdir”
				if ((i >= count) || (((String) tokens.get(i)).startsWith("-"))) {
					// 没有指定扩展目录的值
					System.out.println("Extension directory not specified.");
					System.out.println("Ignoring extension directory option.");
				} else {
					count--;
					File extDir = new File((String) tokens.remove(i));
					if (!canUseExtdir()) {
						// 指定扩展目录的值与系统中的ShellCommand冲突，为什么目前不知道
						System.out
								.println("Extension directory feature not available due to the system classpath being able to load: org.apache.activemq.console.command.ShellCommand");
						System.out.println("Ignoring extension directory option.");
					} else if (!extDir.isDirectory()) {
						System.out.println(new StringBuilder()
								.append("Extension directory specified is not valid directory: ").append(extDir)
								.toString());
						System.out.println("Ignoring extension directory option.");
					} else {
						// 没有文件将改值添加到Set<File> extensions
						addExtensionDirectory(extDir);
					}
				}
			} else if (token.equals("--noDefExt")) {
				// 不使用扩展目录
				count--;
				tokens.remove(i);
				useDefExt = false;
			} else {
				i++;
			}
		}
	}
	// 进入下一个阶段的处理
	// 入参是除去了扩展目录参数之后的所有参数
	// 在进入该方法之前的处理操作有：解析出所有的扩展目录，并把他们存入extensions集合中，以及解析出classpath路径
	public int runTaskClass(List<String> tokens) throws Throwable {
		StringBuilder buffer = new StringBuilder();
		// 输出制作者
		buffer.append(System.getProperty("java.vendor"));
		buffer.append(" ");
		// 输出版本号
		buffer.append(System.getProperty("java.version"));
		buffer.append(" ");
		// 输出java.home
		buffer.append(System.getProperty("java.home"));
		// 输出mq运行时的一些参数信息
		System.out.println(new StringBuilder().append("Java Runtime: ").append(buffer.toString()).toString());
		buffer = new StringBuilder();
		// 计算当前系统内存情况
		buffer.append("current=");
		// 当前系统中正在使用的内存总量，其中包括当前的空闲内存
		buffer.append(Runtime.getRuntime().totalMemory() / 1024L);
		buffer.append("k  free=");
		// 当前系统中的空闲内存量
		buffer.append(Runtime.getRuntime().freeMemory() / 1024L);
		buffer.append("k  max=");
		// 系统可以试图使用的最大内存量
		buffer.append(Runtime.getRuntime().maxMemory() / 1024L);
		buffer.append("k");
		System.out.println(new StringBuilder().append("  Heap sizes: ").append(buffer.toString()).toString());
		// 返回传递给 Java 虚拟机的输入变量，返回值是List<String>
		List jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
		buffer = new StringBuilder();
		for (Iterator i$ = jvmArgs.iterator(); i$.hasNext();) {
			Object arg = i$.next();
			buffer.append(" ").append(arg);
		}
		System.out.println(new StringBuilder().append("    JVM args:").append(buffer.toString()).toString());
		System.out.println(new StringBuilder().append("Extensions classpath:\n  ").append(getExtensionDirForLogging())
				.toString());
		System.out.println(new StringBuilder().append("ACTIVEMQ_HOME: ").append(getActiveMQHome()).toString());
		System.out.println(new StringBuilder().append("ACTIVEMQ_BASE: ").append(getActiveMQBase()).toString());
		System.out.println(new StringBuilder().append("ACTIVEMQ_CONF: ").append(getActiveMQConfig()).toString());
		System.out.println(new StringBuilder().append("ACTIVEMQ_DATA: ").append(getActiveMQDataDir()).toString());
		ClassLoader cl = getClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try {
			// args为系统入参中除去了扩展目录参数之后的所有参数
			String[] args = (String[]) tokens.toArray(new String[tokens.size()]);
			// 使用this.classLoader类加载器加载ShellCommand
			Class task = cl.loadClass("org.apache.activemq.console.command.ShellCommand");
			Method runTask = task.getMethod("main", new Class[] { java.lang.String.class, InputStream.class,
					PrintStream.class });
			return ((Integer) runTask.invoke(task.newInstance(), new Object[] { args, System.in, System.out }))
					.intValue();
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	public void addExtensionDirectory(File directory) {
		this.extensions.add(directory);
	}
	public void addClassPathList(String fileList) {
		if ((fileList != null) && (fileList.length() > 0)) {
			StringTokenizer tokenizer = new StringTokenizer(fileList, ";");
			while (tokenizer.hasMoreTokens())
				addClassPath(new File(tokenizer.nextToken()));
		}
	}
	public void addClassPath(File classpath) {
		this.activeMQClassPath.add(classpath);
	}
	public boolean canUseExtdir() {
		try {
			// 不出意外永远返回的都是false
			Main.class.getClassLoader().loadClass("org.apache.activemq.console.command.ShellCommand");
			return false;
		} catch (ClassNotFoundException e) {
		}
		return true;
	}
	public ClassLoader getClassLoader() throws MalformedURLException {
		if (this.classLoader == null) {
			this.classLoader = Main.class.getClassLoader();
			if ((!this.extensions.isEmpty()) || (!this.activeMQClassPath.isEmpty())) {
				ArrayList urls = new ArrayList();
				for (Iterator iter = this.activeMQClassPath.iterator(); iter.hasNext();) {
					File dir = (File) iter.next();
					urls.add(dir.toURI().toURL());
				}
				for (Iterator iter = this.extensions.iterator(); iter.hasNext();) {
					File dir = (File) iter.next();
					if (dir.isDirectory()) {
						File[] files = dir.listFiles();
						if (files != null) {
							Arrays.sort(files, new Comparator<File>() {
								public int compare(File f1, File f2) {
									return f1.getName().compareTo(f2.getName());
								}
							});
							for (int j = 0; j < files.length; j++) {
								if ((files[j].getName().endsWith(".zip")) || (files[j].getName().endsWith(".jar"))) {
									urls.add(files[j].toURI().toURL());
								}
							}
						}
					}
				}
				URL[] u = new URL[urls.size()];
				urls.toArray(u);
				this.classLoader = new URLClassLoader(u, this.classLoader);
			}
			Thread.currentThread().setContextClassLoader(this.classLoader);
		}
		return this.classLoader;
	}
	public void setActiveMQHome(File activeMQHome) {
		this.activeMQHome = activeMQHome;
	}
	// 获得activemq的home目录
	public File getActiveMQHome() {
		if (this.activeMQHome == null) {
			if (System.getProperty("activemq.home") != null) {
				this.activeMQHome = new File(System.getProperty("activemq.home"));
			}
			if (this.activeMQHome == null) {
				URL url = Main.class.getClassLoader().getResource("org/apache/activemq/console/Main.class");
				if (url != null)
					try {
						// Main.class在jar包中，所以返回的Jar链接
						JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
						url = jarConnection.getJarFileURL();
						// 可以使用这个方法获得上一级目录
						URI baseURI = new URI(url.toString()).resolve("..");
						this.activeMQHome = new File(baseURI).getCanonicalFile();
						System.setProperty("activemq.home", this.activeMQHome.getAbsolutePath());
					} catch (Exception localException) {
					}
			}
			if (this.activeMQHome == null) {
				this.activeMQHome = new File("../.");
				System.setProperty("activemq.home", this.activeMQHome.getAbsolutePath());
			}
		}
		return this.activeMQHome;
	}
	public File getActiveMQBase() {
		if (this.activeMQBase == null) {
			// activemq.base=%ACTIVEMQ_HOME%
			// activemq.base表示的是activemq的根目录
			if (System.getProperty("activemq.base") != null) {
				this.activeMQBase = new File(System.getProperty("activemq.base"));
			}
			if (this.activeMQBase == null) {
				// this.activeMQHome与this.activeMQBase的路径是一致的
				this.activeMQBase = getActiveMQHome();
				System.setProperty("activemq.base", this.activeMQBase.getAbsolutePath());
			}
		}
		return this.activeMQBase;
	}
	public File getActiveMQConfig() {
		File activeMQConfig = null;
		if (System.getProperty("activemq.conf") != null) {
			// activemq.conf=%ACTIVEMQ_HOME%\conf
			activeMQConfig = new File(System.getProperty("activemq.conf"));
		} else {
			activeMQConfig = new File(new StringBuilder().append(getActiveMQBase()).append("/conf").toString());
			System.setProperty("activemq.conf", activeMQConfig.getAbsolutePath());
		}
		return activeMQConfig;
	}
	public File getActiveMQDataDir() {
		File activeMQDataDir = null;
		if (System.getProperty("activemq.data") != null) {
			activeMQDataDir = new File(System.getProperty("activemq.data"));
		} else {
			activeMQDataDir = new File(new StringBuilder().append(getActiveMQBase()).append("/data").toString());
			System.setProperty("activemq.data", activeMQDataDir.getAbsolutePath());
		}
		return activeMQDataDir;
	}
	// 返回当前系统中所有的扩展路径
	public String getExtensionDirForLogging() {
		StringBuilder sb = new StringBuilder("[");
		for (Iterator it = this.extensions.iterator(); it.hasNext();) {
			File file = (File) it.next();
			sb.append(file.getPath());
			if (it.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
