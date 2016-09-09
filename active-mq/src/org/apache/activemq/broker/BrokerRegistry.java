package org.apache.activemq.broker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ��������BrokerService�Ķ�����ڱ������н���ע�ᣬ��BrokerService��start�����У���ע�����
 * @ClassName: BrokerRegistry
 * @Description: TODO
 * @author: ����
 * @date: 2016��7��5�� ����8:09:33
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
	 * �������brokerName��Map�����в��Ҷ�Ӧ��BrokerService������brokerName����һ�������ʱ�򣬻᷵�ؼ����еĵĵ�һ��Ԫ�أ����ʼ���ҵ�����Ԫ�أ�
	 * �ͷ���null
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
	 * ����Map�����еĵ�һ��BrokerService�������������û�ж�����ô����null
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
	 * ��BrokerService����ע�ᵽMap������
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
	 * ��Map������ɾ��BrokerService����
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
	 * ��ñ�������ʹ�õĶ�����
	 * @Title: getRegistryMutext
	 * @Description: TODO
	 * @return
	 * @return: Object
	 */
	public Object getRegistryMutext() {
		return mutex;
	}
	/**
	 * ��ñ������д洢BrokerService�����Map���ϣ�������������ǲ����޸ĵģ���ֻ����
	 * @Title: getBrokers
	 * @Description: TODO
	 * @return
	 * @return: Map<String,BrokerService>
	 */
	public Map<String, BrokerService> getBrokers() {
		return Collections.unmodifiableMap(this.brokers);
	}
}
