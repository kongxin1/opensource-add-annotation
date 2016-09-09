package com.kx.mq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.kx.domain.ClawerMessage;

public class MessageBroker {
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private String pro;
	private String con;
	private String brokerURL;
	private ActiveMQConnectionFactory factory;

	public void createConnection(String brokerURL, String pro, String con) throws JMSException {
		this.pro = pro;
		this.con = con;
		this.brokerURL = brokerURL;
		factory = new ActiveMQConnectionFactory(brokerURL);
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination proDest = session.createQueue(pro);
			Destination conDest = session.createQueue(con);
			producer = session.createProducer(proDest);
			consumer = session.createConsumer(conDest);
		} catch (JMSException jmse) {
			if (connection != null) {
				this.close();
			}
			throw jmse;
		}
	}
	// 发送消息
	public void send(String str, ClawerMessage obj) throws JMSException {
		while (true) {
			try {
				Message msg = session.createMessage();
				msg.setIntProperty("order", obj.getOrder());
				msg.setStringProperty("url", obj.getUrl());
				msg.setStringProperty("content", obj.getStr());
				producer.send(msg);
				break;
			} catch (Exception e) {
				close();
				getProducer();
			}
		}
	}
	// 接受消息
	public ClawerMessage get(String str) throws JMSException {
		while (true) {
			try {
				Message msg = consumer.receive();
				ClawerMessage cm = new ClawerMessage();
				cm.setOrder(msg.getIntProperty("order"));
				cm.setStr(msg.getStringProperty("content"));
				cm.setUrl(msg.getStringProperty("url"));
				return cm;
			} catch (Exception e) {
				close();
				getConsumer();
			}
		}
	}
	public void close() throws JMSException {
		if (connection != null) {
			connection.close();
		}
	}
	public MessageConsumer getConsumer() throws JMSException {
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination conDest = session.createQueue(con);
			consumer = session.createConsumer(conDest);
			return consumer;
		} catch (JMSException jmse) {
			if (connection != null) {
				this.close();
			}
			throw jmse;
		}
	}
	public MessageProducer getProducer() throws JMSException {
		try {
			connection = factory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination proDest = session.createQueue(pro);
			producer = session.createProducer(proDest);
			return producer;
		} catch (JMSException jmse) {
			if (connection != null) {
				this.close();
			}
			throw jmse;
		}
	}
}
