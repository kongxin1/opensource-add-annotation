/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

/**
 * ��������Ҫ��ÿһ��������ӵ�е�һЩ�������ԣ�����ö��������Ҫ��һЩ��Ϣ
 * 
 *
 */
public class AdvancedParameter {
    /**
     * sql�е�������������Сд��������setter��ʾ�����ó�Сд��
     */
    public String        key;
    /**
     * ��������ö�����õ�
     */
    public Comparable<?> atomicIncreateValue;
    /**
     * ���Ӵ�������ö�����õ�
     */
    public Integer       cumulativeTimes;

    /**
     * ������ǰ�����Ƿ�����Χ��ѯ��>= <= ...
     */
    public boolean       needMergeValueInCloseInterval;

    public Integer getCumulativeTimes() {
        return cumulativeTimes;
    }

    public void setCumulativeTimes(Integer cumulativeTimes) {
        this.cumulativeTimes = cumulativeTimes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key.toLowerCase();
    }

    public Comparable<?> getAtomicIncreateValue() {
        return atomicIncreateValue;
    }

    public void setAtomicIncreateValue(Comparable<?> atomicIncreateValue) {
        this.atomicIncreateValue = atomicIncreateValue;
    }

    public boolean isNeedMergeValueInCloseInterval() {
        return needMergeValueInCloseInterval;
    }

    @Override
    public String toString() {
        return "AdvancedParameter [atomicIncreateValue=" + atomicIncreateValue
               + ", cumulativeTimes=" + cumulativeTimes + ", key=" + key
               + ", needMergeValueInCloseInterval=" + needMergeValueInCloseInterval + "]";
    }

}
