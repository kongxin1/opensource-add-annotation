package com.kx.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.kx.domain.ClawerMessage;
import com.kx.mq.MessageBroker;

//启动类
public class Startup {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);

	public static void main(String argv[]) throws Exception {
		MessageBroker broker = null;
		BufferedReader reader = null;
		try {
			ClassPathResource resource = new ClassPathResource("html.txt");
			reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			broker = new MessageBroker();
			broker.createConnection("tcp://localhost:61616", "url", "content");
			String html = null;
			while ((html = reader.readLine()) != null) {
				ClawerMessage msg = new ClawerMessage();
				msg.setOrder(1);
				msg.setUrl(html);
				broker.send("url", msg);
				logger.info("初始url包括：" + html);
			}
			URLHandler uh = new URLHandler();
			uh.start();
			HTMLHandler hh = new HTMLHandler();
			hh.start();
		} finally {
			logger.info("爬虫启动完毕");
			try {
				broker.close();
			} finally {
				reader.close();
			}
		}
	}
}
