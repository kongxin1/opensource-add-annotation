package com.kx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLHandler {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);
	// 线程数
	private int thread = 1;

	public void start() {
		for (int i = 0; i < thread; i++) {
			HTMLRunnable t = new HTMLRunnable();
			logger.info("HTML获取线程的第" + (i + 1) + "个线程开始初始化");
			try {
				t.init();
			} catch (Exception e) {
				logger.error("HTML获取线程的第" + (i + 1) + "个线程初始化失败", e);
				continue;
			}
			logger.info("HTML获取线程的第" + (i + 1) + "个线程初始化完毕");
			Thread thread = new Thread(t);
			thread.start();
		}
	}
}
