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

//activemq����������
public class Main {
	public static final String TASK_DEFAULT_CLASS = "org.apache.activemq.console.command.ShellCommand";
	private static boolean useDefExt = true;// ʹ��Ĭ�ϵ���ʱĿ¼
	private File activeMQHome;
	private File activeMQBase;
	// �������������Main�ļ�����Ϊ��������������Ҹ������·��������ϵͳ�е�classpath·������չ·���е�zip��jar�ļ�
	private ClassLoader classLoader;
	private final Set<File> extensions = new LinkedHashSet();// ��չĿ¼
	private final Set<File> activeMQClassPath = new LinkedHashSet();// mq��class·��

	public static void main(String[] args) {
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));// ��ȡactivemq����ʱ·����%ACTIVEMQ_HOME%\data\tmp
		if (!tmpdir.exists()) {
			tmpdir.mkdirs();
		}
		Main app = new Main();
		List tokens = new LinkedList(Arrays.asList(args));// �������ļ���������args�����ֻ��һ��start
		// ������������ֻ�ǿ����Ƿ�ʹ����չĿ¼�����ʹ�ã���·����ʲô����չĿ¼�Ĳ���ֻ����һ��
		app.parseExtensions(tokens);
		// ��ȡϵͳ��confĿ¼
		File confDir = app.getActiveMQConfig();
		app.addClassPath(confDir);// conf·��Ҳ��classpath��·��֮һ
		// ShellCommand����չ·����ͻ
		// home��baseĿ¼���Բ���ͬһ������������Ŀ¼����������ͬ��
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
			// tokens����ȥ������չĿ¼������Ĳ����б�
			int ret = app.runTaskClass(tokens);
			System.exit(ret);
		} catch (ClassNotFoundException e) {
			System.out.println(new StringBuilder().append("Could not load class: ").append(e.getMessage()).toString());
			try {
				ClassLoader cl = app.getClassLoader();
				if (cl != null) {
					System.out.println("Class loader setup: ");
					// �����������������Լ����������������Դ��Ŀ¼
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
			// �������ڼ��������Դ�� URL ����·���������Ϊ���췽��ָ����ԭʼ URL �б��Լ��� addURL() ����������ӵ� URL��
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
				tokens.remove(i);// �����������ȥ����--extdir��
				if ((i >= count) || (((String) tokens.get(i)).startsWith("-"))) {
					// û��ָ����չĿ¼��ֵ
					System.out.println("Extension directory not specified.");
					System.out.println("Ignoring extension directory option.");
				} else {
					count--;
					File extDir = new File((String) tokens.remove(i));
					if (!canUseExtdir()) {
						// ָ����չĿ¼��ֵ��ϵͳ�е�ShellCommand��ͻ��ΪʲôĿǰ��֪��
						System.out
								.println("Extension directory feature not available due to the system classpath being able to load: org.apache.activemq.console.command.ShellCommand");
						System.out.println("Ignoring extension directory option.");
					} else if (!extDir.isDirectory()) {
						System.out.println(new StringBuilder()
								.append("Extension directory specified is not valid directory: ").append(extDir)
								.toString());
						System.out.println("Ignoring extension directory option.");
					} else {
						// û���ļ�����ֵ��ӵ�Set<File> extensions
						addExtensionDirectory(extDir);
					}
				}
			} else if (token.equals("--noDefExt")) {
				// ��ʹ����չĿ¼
				count--;
				tokens.remove(i);
				useDefExt = false;
			} else {
				i++;
			}
		}
	}
	// ������һ���׶εĴ���
	// ����ǳ�ȥ����չĿ¼����֮������в���
	// �ڽ���÷���֮ǰ�Ĵ�������У����������е���չĿ¼���������Ǵ���extensions�����У��Լ�������classpath·��
	public int runTaskClass(List<String> tokens) throws Throwable {
		StringBuilder buffer = new StringBuilder();
		// ���������
		buffer.append(System.getProperty("java.vendor"));
		buffer.append(" ");
		// ����汾��
		buffer.append(System.getProperty("java.version"));
		buffer.append(" ");
		// ���java.home
		buffer.append(System.getProperty("java.home"));
		// ���mq����ʱ��һЩ������Ϣ
		System.out.println(new StringBuilder().append("Java Runtime: ").append(buffer.toString()).toString());
		buffer = new StringBuilder();
		// ���㵱ǰϵͳ�ڴ����
		buffer.append("current=");
		// ��ǰϵͳ������ʹ�õ��ڴ����������а�����ǰ�Ŀ����ڴ�
		buffer.append(Runtime.getRuntime().totalMemory() / 1024L);
		buffer.append("k  free=");
		// ��ǰϵͳ�еĿ����ڴ���
		buffer.append(Runtime.getRuntime().freeMemory() / 1024L);
		buffer.append("k  max=");
		// ϵͳ������ͼʹ�õ�����ڴ���
		buffer.append(Runtime.getRuntime().maxMemory() / 1024L);
		buffer.append("k");
		System.out.println(new StringBuilder().append("  Heap sizes: ").append(buffer.toString()).toString());
		// ���ش��ݸ� Java ��������������������ֵ��List<String>
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
			// argsΪϵͳ����г�ȥ����չĿ¼����֮������в���
			String[] args = (String[]) tokens.toArray(new String[tokens.size()]);
			// ʹ��this.classLoader�����������ShellCommand
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
			// ����������Զ���صĶ���false
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
	// ���activemq��homeĿ¼
	public File getActiveMQHome() {
		if (this.activeMQHome == null) {
			if (System.getProperty("activemq.home") != null) {
				this.activeMQHome = new File(System.getProperty("activemq.home"));
			}
			if (this.activeMQHome == null) {
				URL url = Main.class.getClassLoader().getResource("org/apache/activemq/console/Main.class");
				if (url != null)
					try {
						// Main.class��jar���У����Է��ص�Jar����
						JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
						url = jarConnection.getJarFileURL();
						// ����ʹ��������������һ��Ŀ¼
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
			// activemq.base��ʾ����activemq�ĸ�Ŀ¼
			if (System.getProperty("activemq.base") != null) {
				this.activeMQBase = new File(System.getProperty("activemq.base"));
			}
			if (this.activeMQBase == null) {
				// this.activeMQHome��this.activeMQBase��·����һ�µ�
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
	// ���ص�ǰϵͳ�����е���չ·��
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
