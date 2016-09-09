/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.Calendar;

/**
 * ���ڴ����������ֺ��������ֶ�Ӧ��Calendar�������
 * �̳�Comparable����Ϊ��ʼԤ���Ľӿ���Comparable...
 *
 */
@SuppressWarnings("unchecked")
public class DateEnumerationParameter implements Comparable {
    /**
     * Ĭ��ʹ��Date��Ϊ�������͵Ļ���������λ
     * @param atomicIncreateNumber
     */
    public DateEnumerationParameter(int atomicIncreateNumber) {
        this.atomicIncreatementNumber = atomicIncreateNumber;
        this.calendarFieldType = Calendar.DATE;
    }

    public DateEnumerationParameter(int atomicIncreateNumber, int calendarFieldType) {
        this.atomicIncreatementNumber = atomicIncreateNumber;
        this.calendarFieldType = calendarFieldType;
    }

    public final int atomicIncreatementNumber;
    public final int calendarFieldType;

    public int compareTo(Object o) {
        throw new IllegalArgumentException("should not be here !");
    }

}
