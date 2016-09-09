package org.apache.activemq.broker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单例对象，BrokerService的对象会在本对象中进行注册，在BrokerService的start方法中，会注入进来
 * @ClassName: BrokerRegistry
 * @Description: TODO
 * @author: 孔新
 * @date: 2016年7月5日 上午8:09:33
 */
public class BrokerRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(BrokerRegistry.class);
	private static final BrokerRegistry INSTANCE = new BrokerRegistry();
	private final Object mutex = new Object();
	private final Map<String, BrokerService> brokers = new HashMap<String, BrokerService>();

	public static BrokerRegistry getInstance() {
		return INSTANCE;
	}
	/**
	 * @param brokerName
	 * @return the BrokerService
	 */
	/**
	 * 根据入参brokerName在Map对象中查找对应的BrokerService对象，在brokerName满足一定情况的时候，会返回集合中的的第一个元素，如果始终找到不到元素，
	 * 就返回null
	 * @Title: lookup
	 * @Description: TODO
	 * @param brokerName
	 * @return
	 * @return: BrokerService
	 */
	public BrokerService lookup(String brokerName) {
		BrokerService result = null;
		synchronized (mutex) {
			result = brokers.get(brokerName);
			if (result == null && brokerName != null && brokerName.equals(BrokerService.DEFAULT_BROKER_NAME)) {
				result = findFirst();
				if (result != null) {
					LOG.warn("Broker localhost not started so using {} instead", result.getBrokerName());
				}
			}
			if (result == null && (brokerName == null || brokerName.isEmpty() || brokerName.equals("null"))) {
				result = findFirst();
			}
		}
		return result;
	}
	/**
	 * Returns the first registered broker found
	 * @return the first BrokerService
	 */
	/**
	 * 返回Map集合中的第一个BrokerService对象，如果集合中没有对象，那么返回null
	 * @Title: findFirst
	 * @Description: TODO
	 * @return
	 * @return: BrokerService
	 */
	public BrokerService findFirst() {
		synchronized (mutex) {
			Iterator<BrokerService> iter = brokers.values().iterator();
			while (iter.hasNext()) {
				return iter.next();
			}
			return null;
		}
	}
	/**
	 * @param brokerName
	 * @param broker
	 */
	/**
	 * 将BrokerService对象注册到Map对象中
	 * @Title: bind
	 * @Description: TODO
	 * @param brokerName
	 * @param broker
	 * @return: void
	 */
	public void bind(String brokerName, BrokerService broker) {
		synchronized (mutex) {
			brokers.put(brokerName, broker);
			mutex.notifyAll();
		}
	}
	/**
	 * @param brokerName
	 */
	/**
	 * 从Map集合中删除BrokerService对象
	 * @Title: unbind
	 * @Description: TODO
	 * @param brokerName
	 * @return: void
	 */
	public void unbind(String brokerName) {
		synchronized (mutex) {
			brokers.remove(brokerName);
		}
	}
	/**
	 * 获得本对象中使用的对象锁
	 * @Title: getRegistryMutext
	 * @Description: TODO
	 * @return
	 * @return: Object
	 */
	public Object getRegistryMutext() {
		return mutex;
	}
	/**
	 * 获得本对象中存储BrokerService对象的Map集合，但是这个集合是不能修改的，是只读的
	 * @Title: getBrokers
	 * @Description: TODO
	 * @return
	 * @return: Map<String,BrokerService>
	 */
	public Map<String, BrokerService> getBrokers() {
		return Collections.unmodifiableMap(this.brokers);
	}
}
