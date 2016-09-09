package com.kx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLHandler {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);
	// �߳���
	private int thread = 1;

	public void start() {
		for (int i = 0; i < thread; i++) {
			HTMLRunnable t = new HTMLRunnable();
			logger.info("HTML��ȡ�̵߳ĵ�" + (i + 1) + "���߳̿�ʼ��ʼ��");
			try {
				t.init();
			} catch (Exception e) {
				logger.error("HTML��ȡ�̵߳ĵ�" + (i + 1) + "���̳߳�ʼ��ʧ��", e);
				continue;
			}
			logger.info("HTML��ȡ�̵߳ĵ�" + (i + 1) + "���̳߳�ʼ�����");
			Thread thread = new Thread(t);
			thread.start();
		}
	}
}
