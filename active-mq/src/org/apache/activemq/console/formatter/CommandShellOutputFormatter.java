package org.apache.activemq.console.formatter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @ClassName: CommandShellOutputFormatter
 * @Description:是一个打印类，用于将MBean和其他对象或者集合对象的信息打印出来，该类中包含了两个流输出属性，这两个输出属性都是使用同一个底层字节流对象
 * @author: 孔新
 * @date: 2016年7月1日 下午10:46:35
 */
public class CommandShellOutputFormatter implements OutputFormatter {
	private OutputStream outputStream;
	private PrintStream out;

	/**
	 * @ClassName: CommandShellOutputFormatter
	 * @Description:是一个打印类，用于将MBean和其他对象或者集合对象的信息打印出来，该类中包含了两个流输出属性，这两个输出属性都是使用同一个底层字节流对象
	 * @author: 孔新
	 * @date: 2016年7月1日 下午10:46:35
	 */
	public CommandShellOutputFormatter(OutputStream out) {
		this.outputStream = out;
		if (out instanceof PrintStream) {
			this.out = (PrintStream) out;
		} else {
			this.out = new PrintStream(out);
		}
	}
	/**
	 * Retrieve the output stream being used by the formatter
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}
	/**
	 * Print an ObjectInstance format of an mbean
	 * @param mbean - mbean to print
	 */
	public void printMBean(ObjectInstance mbean) {
		printMBean(mbean.getObjectName());
	}
	/**
	 * Print an ObjectName format of an mbean
	 * @param mbean - mbean to print
	 */
	public void printMBean(ObjectName mbean) {
		printMBean(mbean.getKeyPropertyList());
	}
	/**
	 * Print an AttributeList format of an mbean
	 * @param mbean - mbean to print
	 */
	public void printMBean(AttributeList mbean) {
		for (Iterator i = mbean.iterator(); i.hasNext();) {
			Attribute attrib = (Attribute) i.next();
			if (attrib.getValue() instanceof ObjectName) {
				printMBean((ObjectName) attrib.getValue());
			} else if (attrib.getValue() instanceof ObjectInstance) {
				printMBean((ObjectInstance) attrib.getValue());
			} else {
				out.println(attrib.getName() + " = " + attrib.getValue().toString());
				out.println();
			}
		}
	}
	/**
	 * Print a Map format of an mbean
	 * @param mbean - mbean to print
	 */
	public void printMBean(Map mbean) {
		for (Iterator i = mbean.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = mbean.get(key).toString();
			out.println(key + " = " + val);
		}
		out.println();
	}
	/**
	 * Print a collection of mbean
	 * @param mbean - collection of mbeans
	 */
	public void printMBean(Collection mbean) {
		for (Iterator i = mbean.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof ObjectInstance) {
				printMBean((ObjectInstance) obj);
			} else if (obj instanceof ObjectName) {
				printMBean((ObjectName) obj);
			} else if (obj instanceof Map) {
				printMBean((Map) obj);
			} else if (obj instanceof AttributeList) {
				printMBean((AttributeList) obj);
			} else if (obj instanceof Collection) {
				printMessage((Collection) obj);
			} else {
				printException(new UnsupportedOperationException("Unknown mbean type: " + obj.getClass().getName()));
			}
		}
	}
	/**
	 * Print a Map format of a JMS message
	 * @param msg
	 */
	public void printMessage(Map msg) {
		for (Iterator i = msg.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = msg.get(key).toString();
			out.println(key + " = " + val);
		}
		out.println();
	}
	/**
	 * Print a Message format of a JMS message
	 * @param msg - JMS message to print
	 */
	public void printMessage(Message msg) {
		// TODO
	}
	/**
	 * Print a collection of JMS messages
	 * @param msg - collection of JMS messages
	 */
	public void printMessage(Collection msg) {
		for (Iterator i = msg.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof Message) {
				printMessage((Message) obj);
			} else if (obj instanceof Map) {
				printMessage((Map) obj);
			} else if (obj instanceof Collection) {
				printMessage((Collection) obj);
			} else {
				printException(new UnsupportedOperationException("Unknown message type: " + obj.getClass().getName()));
			}
		}
	}
	/**
	 * Print help messages
	 * @param helpMsgs - help messages to print
	 */
	public void printHelp(String[] helpMsgs) {
		for (int i = 0; i < helpMsgs.length; i++) {
			out.println(helpMsgs[i]);
		}
		out.println();
	}
	/**
	 * Print an information message
	 * @param info - information message to print
	 */
	public void printInfo(String info) {
		out.println("INFO: " + info);
	}
	/**
	 * Print an exception message
	 * @param e - exception to print
	 */
	public void printException(Exception e) {
		out.println("ERROR: " + e);
		e.printStackTrace(out);
	}
	/**
	 * Print a version information
	 * @param version - version info to print
	 */
	public void printVersion(String version) {
		out.println("");
		out.println("ActiveMQ " + version);
		out.println("For help or more information please see: http://activemq.apache.org");
		out.println("");
	}
	/**
	 * Print a generic key value mapping
	 * @param map to print
	 */
	public void print(Map map) {
		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = map.get(key).toString();
			out.println(key + " = " + val);
		}
		out.println();
	}
	/**
	 * Print a generic array of strings
	 * @param strings - string array to print
	 */
	public void print(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			out.println(strings[i]);
		}
		out.println();
	}
	/**
	 * Print a collection of objects
	 * @param collection - collection to print
	 */
	public void print(Collection collection) {
		for (Iterator i = collection.iterator(); i.hasNext();) {
			out.println(i.next().toString());
		}
		out.println();
	}
	/**
	 * Print a java string
	 * @param string - string to print
	 */
	public void print(String string) {
		out.println(string);
	}
}
