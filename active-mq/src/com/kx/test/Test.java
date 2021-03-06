package com.kx.test;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Test {
	public static void main(String argv[]) throws Exception {
		// 下面这个对象并没有设置链接，只是设置了一些属性
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
		// 已经与服务器端建立链接
		Connection conn = factory.createConnection();
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination topic = session.createQueue("TEST");
		MessageProducer producer = session.createProducer(topic);
		Message msg = session.createMessage();
		msg.setIntProperty("age", 1);
		// Message ms = consumer.receive();
		// System.out.println(ms);
		producer.send(msg);
		// MessageProducer producer = session.createProducer(topic);
		// Message msg = session.createMessage();
		// msg.setIntProperty("age", 1);
		// producer.send(topic, msg);
		conn.close();
	}
}
