/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.config;

import java.util.Map;

/**
 * ��̬����failover��״̬�������ڷֲ�ʽ�����µĶ�̬����.
 * @author ����
 * @version $Id: ZdalConfigListener.java, v 0.1 2012-11-17 ����4:29:22 Exp $
 */
public interface ZdalConfigListener {

    /**
     * ͨ��drm�����л���Ϣ.
     * @param keyWeights ���͵�ֵ.
     */
    void resetWeight(Map<String, String> keyWeights);

}
