/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * ����ΪFinal�ֻ࣬���ؽ��������޸�
 * 
 * ��һ�׶Σ�ֻ֧������Դ�Ķ�̬����֧�ֹ����dbindex�Ķ�̬
 * @author zhaofeng.wang
 * @version $Id: ZdalRuntime.java,v 0.1 2012-10-26 ����11:21:29 zhaofeng.wang Exp $
 */
public class ZdalRuntime {
    public final Map<String, DBSelector> dbSelectors;

    public ZdalRuntime(Map<String, DBSelector> dbSelectors) {
        this.dbSelectors = dbSelectors;
    }

    /**
     * ��ԭ�е�dbIndexȥ�µ�����������map�в��ң����ҵ������µ� 
     */
    public static ZdalRuntime resetDbSelectors(ZdalRuntime oldrt,
                                               Map<String, DBSelector> newDbSelectors) {
        Map<String, DBSelector> resSelectors = new HashMap<String, DBSelector>();
        for (Map.Entry<String, DBSelector> e : oldrt.dbSelectors.entrySet()) {
            DBSelector newdb = newDbSelectors.get(e.getKey());
            resSelectors.put(e.getKey(), newdb == null ? e.getValue() : newdb);
        }
        return new ZdalRuntime(resSelectors);
    }
}
