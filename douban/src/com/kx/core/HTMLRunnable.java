package com.kx.core;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kx.domain.ClawerMessage;
import com.kx.domain.IntegerAndHtmlMap;
import com.kx.mq.MessageBroker;

//从队列中获取到网页，然后进行分析，获取到网页中url，并传输到队列
public class HTMLRunnable implements Runnable {
	private MessageBroker broker = null;
	private static Logger logger = LoggerFactory.getLogger(Startup.class);

	public void init() throws Exception {
		broker = new MessageBroker();
		broker.createConnection("tcp://localhost:61616", "url", "content");
	}
	public void run() {
		try {
			while (true) {
				ClawerMessage msg = null;
				try {
					msg = broker.get("html");
				} catch (Exception e) {
					logger.error("从网页的消息队列中获取消息失败", e);
				}
				if (msg != null) {
					List<String> list = IntegerAndHtmlMap.map.get(msg.getOrder());
					for (String str : list) {
						// 使用反射对页面进行处理
						Class clazz = null;
						try {
							clazz = Class.forName("com.kx.htmlparser." + str);
						} catch (Exception e) {
							logger.error(str + "类反射获取对象失败", e);
							continue;
						}
						Method method = null;
						try {
							method = clazz.getMethod("parse", String.class, ClawerMessage.class);
						} catch (Exception e) {
							logger.error("通过反射获取对象失败", e);
							continue;
						}
						List<ClawerMessage> urls = null;
						try {
							urls = (List<ClawerMessage>) method.invoke(clazz.newInstance(), msg.getUrl(), msg);
						} catch (Exception e) {
							logger.error("通过反射调用方法失败", e);
							continue;
						}
						for (ClawerMessage cm : urls) {
							cm.setStr(null);
							try {
								broker.send("url", cm);
							} catch (Exception e) {
								logger.error("URL发送失败，url=" + cm.getUrl(), e);
								continue;
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("系统出现重大故障", t);
		}
	}
}
