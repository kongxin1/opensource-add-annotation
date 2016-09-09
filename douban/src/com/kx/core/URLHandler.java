package com.kx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLHandler {
	private static Logger logger = LoggerFactory.getLogger(URLHandler.class);
	// �߳���
	private int thread = 50;

	public void start() {
		for (int i = 0; i < thread; i++) {
			URLRunnable t = new URLRunnable();
			logger.info("URL��ȡ�̵߳ĵ�" + (i + 1) + "���߳̿�ʼ��ʼ��");
			try {
				t.init();
			} catch (Exception e) {
				logger.error("HTML��ȡ�̵߳ĵ�" + (i + 1) + "���̳߳�ʼ��ʧ��", e);
				continue;
			}
			logger.info("URL��ȡ�̵߳ĵ�" + (i + 1) + "���̳߳�ʼ�����");
			Thread thread = new Thread(t);
			thread.start();
		}
	}
}
