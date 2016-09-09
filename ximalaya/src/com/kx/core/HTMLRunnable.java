package com.kx.core;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kx.domain.ClawerMessage;
import com.kx.domain.IntegerAndHtmlMap;
import com.kx.mq.MessageBroker;

//�Ӷ����л�ȡ����ҳ��Ȼ����з�������ȡ����ҳ��url�������䵽����
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
					logger.error("����ҳ����Ϣ�����л�ȡ��Ϣʧ��", e);
				}
				if (msg != null) {
					List<String> list = IntegerAndHtmlMap.map.get(msg.getOrder());
					for (String str : list) {
						// ʹ�÷����ҳ����д���
						Class clazz = null;
						try {
							clazz = Class.forName("com.kx.htmlparser." + str);
						} catch (Exception e) {
							logger.error(str + "�෴���ȡ����ʧ��", e);
							continue;
						}
						Method method = null;
						try {
							method = clazz.getMethod("parse", String.class, ClawerMessage.class);
						} catch (Exception e) {
							logger.error("ͨ�������ȡ����ʧ��", e);
							continue;
						}
						List<ClawerMessage> urls = null;
						try {
							urls = (List<ClawerMessage>) method.invoke(clazz.newInstance(), msg.getUrl(), msg);
						} catch (Exception e) {
							logger.error("ͨ��������÷���ʧ��", e);
							continue;
						}
						for (ClawerMessage cm : urls) {
							cm.setStr(null);
							try {
								broker.send("url", cm);
							} catch (Exception e) {
								logger.error("URL����ʧ�ܣ�url=" + cm.getUrl(), e);
								continue;
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("ϵͳ�����ش����", t);
		}
	}
}
