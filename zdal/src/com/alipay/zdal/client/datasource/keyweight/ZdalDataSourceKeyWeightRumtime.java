/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.zdal.client.datasource.keyweight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.lang.StringUtil;

/**
 * 
 * @author zhaofeng.wang
 * @version $Id: KeyWeightRumtime.java, v 0.1 2011-9-23 ����04:12:46 zhaofeng.wang Exp $
 */
public class ZdalDataSourceKeyWeightRumtime {

    private static Logger                              logger                    = Logger
                                                                                     .getLogger(ZdalDataSourceKeyWeightRumtime.class);
    private static final int                           DEFAULT_DATASOURCE_WEIGHT = 10;
    private Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapHolder;

    public ZdalDataSourceKeyWeightRumtime(
                                          Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapHolder) {
        this.keyWeightMapHolder = keyWeightMapHolder;
    }

    /**
     * ��������Դ��keyWeight��Ȩ�ر�ʶ
     * 
     * ����p��ʽ����
     * group_0=TradeCore00:10,TradeCore00_0:0
     * group_1=TradeCore01:10,TradeCore01_0:0
     * group_2=TradeCore02:0,TradeCore02_0:10
     * ��������ʱ������keyWeightRuntimeConfigHoder��
     * 
     * @param keyWeightConfig
     * @return
     * @throws Exception 
     */
    public static Map<String, ZdalDataSourceKeyWeightRandom> buildKeyWeightConfig(
                                                                                  Map<String, String> keyWeightConfig,
                                                                                  Map<String, ? extends Object> dataSourceKeyConfig) {
        Map<String, ZdalDataSourceKeyWeightRandom> map = new ConcurrentHashMap<String, ZdalDataSourceKeyWeightRandom>(
            keyWeightConfig.size());
        for (Map.Entry<String, String> entry : keyWeightConfig.entrySet()) {
            String groupKey = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtil.isBlank(groupKey) || StringUtil.isBlank(value)) {
                logger.error("����Դkey=" + groupKey + "����Ȩ��������Ϣ����Ϊ��,value=" + value);
                return null;
            }
            String[] keyWeightStr = value.split(",");
            String[] weightKeys = new String[keyWeightStr.length];
            int[] weights = new int[keyWeightStr.length];
            //case:������Դkey��ʱ�� ��ʾ����group_0=TradeCore00����group_0=TradeCore00:10
            if (keyWeightStr.length == 1) {
                if (StringUtil.isBlank(keyWeightStr[0])) {
                    logger.error("������ԴkeyWeightStr[0]����Ȩ��������ϢΪ��.");
                    return null;
                }
                String[] keyAndWeight = keyWeightStr[0].split(":");
                if (dataSourceKeyConfig.keySet() == null
                    || !dataSourceKeyConfig.keySet().contains(keyAndWeight[0].trim())) {
                    logger.error("����Դkey=" + keyAndWeight[0] + "������Դ�����в�����.");
                    return null;
                }
                weightKeys[0] = keyAndWeight[0].trim();//����Դ����key
                weights[0] = DEFAULT_DATASOURCE_WEIGHT;//Ĭ��Ȩ��10
            } else if (keyWeightStr.length > 1) {
                //case: �������Դ��ʱ��
                // ʾ��:group_0=TradeCore00:10,TradeCore00_0:0
                for (int i = 0; i < keyWeightStr.length; i++) {
                    if (StringUtil.isBlank(keyWeightStr[i])) {
                        logger.error("������ԴkeyWeightStr[" + i + "]����Ȩ��������ϢΪ��.");
                        return null;
                    }
                    String[] keyAndWeight = keyWeightStr[i].split(":");
                    if (keyAndWeight.length != 2) {
                        logger.error("����Դkey��������Ȩ�ش���,keyWeightStr[" + i + "]=" + keyWeightStr[i]
                                     + ".");
                        return null;
                    }
                    String key = keyAndWeight[0];
                    if (dataSourceKeyConfig.keySet() == null
                        || !dataSourceKeyConfig.keySet().contains(key)) {
                        logger.error("����Դkey=" + key + "������ԴdataSourcePool�����в�����.");
                        return null;
                    }
                    String weightStr = keyAndWeight[1];
                    if (StringUtil.isBlank(key) || StringUtil.isBlank(weightStr)) {
                        logger.error("����Դkey=" + key + "����Ȩ������weightStr=" + weightStr + "����Ϊ��.");
                        return null;
                    }
                    weightKeys[i] = key.trim();
                    weights[i] = Integer.parseInt(weightStr.trim());
                }
            } else {
                logger.error("�÷���groupKey=" + groupKey + "������Դ�ĸ������ԣ�length=" + keyWeightStr.length);
                return null;
            }
            //���ݸ����groupKey�Լ���Ӧ��keyAndWeightMap����TDataSourceKeyWeightRandom
            ZdalDataSourceKeyWeightRandom TDataSourceKeyWeightRandom = new ZdalDataSourceKeyWeightRandom(
                weightKeys, weights);
            map.put(groupKey, TDataSourceKeyWeightRandom);
        }
        return map;

    }

    /**
     * Setter method for property <tt>keyWeightMapHolder</tt>.
     * 
     * @param keyWeightMapHolder value to be assigned to property keyWeightMapHolder
     */
    public void setKeyWeightMapHolder(Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapHolder) {
        this.keyWeightMapHolder = keyWeightMapHolder;
    }

    /**
     * Getter method for property <tt>keyWeightMapHolder</tt>.
     * 
     * @return property value of keyWeightMapHolder
     */
    public Map<String, ZdalDataSourceKeyWeightRandom> getKeyWeightMapHolder() {
        return keyWeightMapHolder;
    }

}
