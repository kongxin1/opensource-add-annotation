/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.enumerator;

import java.util.Set;

/**
 * ö�������ṩ�˸���ÿ�������� ������ȡ����ö��ֵ�Ĳ���
 * ��Ҫ�����ڽ��һ��������������������
 * sql ���� :id>100 and id < 200;
 * �����������޷�ֱ�Ӵ�����������н��м���Ȼ��򵥵�ȡ����������ģ�������μ�����ĵ��Ľ��ܡ�
 * 
 * ���Խ���ķ������ǰ�100~200֮�������ֵ������atomicIncreatementValue���趨ֵ����ö�١�
 * ö�ٳ���ֵ������set�󷵻ظ������ߡ�
 * 
 *
 */
public interface Enumerator {
    /**
     * @param condition ����
     * @param cumulativeTimes ֵ�ĸ��������ڲ��������ĺ�����˵�������һ���ۼӵĴ��������޵ģ�����Ҫ�������������
     * @param atomIncrValue ����ֵ������С�䶯�Ķ�����ԭ������ֵ��ex:�������dayofweek�����ĺ�����˵������ֵ��
     * �����仯�Ķ��������С�䶯��ΧΪ1�졣
     * @param needMergeValueInCloseInterval �Ƿ���Ҫ��> < >= <= ���м��㡣
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Object> getEnumeratedValue(Comparable condition, Integer cumulativeTimes,
                                          Comparable<?> atomicIncreatementValue,
                                          boolean needMergeValueInCloseInterval);
    //	 void setNeedMergeValueInCloseInterval(boolean needMergeValueInCloseInterval);
}
