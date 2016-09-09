/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.groovy.staticmethod;

import java.util.Calendar;
import java.util.Date;

import com.alipay.zdal.common.util.NestThreadLocalMap;

public class GroovyStaticMethod {
    public static final String  GROOVY_STATIC_METHOD_CALENDAR = "GROOVY_STATIC_METHOD_CALENDAR";
    private final static long[] pow10                         = { 1, 10, 100, 1000, 10000, 100000,
            1000000, 10000000, 100000000, 1000000000, 10000000000L, 100000000000L, 1000000000000L,
            10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L,
            100000000000000000L, 1000000000000000000L        };

    /**
     * 
     * Ĭ�ϵ�dayofweek :
     * ���offset  = 0;��ôΪĬ��dow
     * san = 1
     * sat = 7
     * 
     * @param date
     * @return
     */
    public static int dayofweek(Date date, int offset) {
        Calendar cal = getCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) + offset;
    }

    /**
     * �����Լ���dayofweek.��Ϊ����index��Ĭ�ϴ�0��ʼ�������Ҳ������day of week��0 ��ʼ��
     * Ĭ������� ֱ��offset = -1 ���
     * san = 0;
     * sat = 6;
     * @param date
     * @return
     */
    public static int dayofweek(Date date) {
        return dayofweek(date, -1);
    }

    private static Calendar getCalendar() {
        Calendar cal = (Calendar) NestThreadLocalMap.get(GROOVY_STATIC_METHOD_CALENDAR);
        if (cal == null) {
            cal = Calendar.getInstance();
            NestThreadLocalMap.put(GROOVY_STATIC_METHOD_CALENDAR, cal);
        }
        return cal;
    }

    private static long getModRight(long targetID, int size, int bitNumber) {
        if (bitNumber < size) {
            throw new IllegalArgumentException("�����λ����Ҫ���size��С");
        }
        return (size == 0 ? 0 : targetID / pow10[bitNumber - size]);
    }

    /**
     * ����ʼ��ȡָ�����λ����
     * 
     * @param targetID Ŀ��id��Ҳ���ǵȴ���decode������
     * @param bitNumber Ŀ��id���ݵ�λ��
     * @param st ���Ķ���ʼȡ�������ȡ����ߵ�һλ��ô��������st = 0;ed =1;
     * @param ed ȡ���Ķ��������ȡ����ߵ���λ����ô��������st = 0;ed = 2;
     * @return
     */
    public static long left(long targetID, int bitNumber, int st, int ed) {
        long end = getModRight(targetID, ed, bitNumber);
        return end % pow10[(ed - st)];
    }

    /**
     * ����ʼ��ȡָ�����λ����Ĭ����һ��long�γ��ȵ����ݣ�Ҳ����bitNumber= 19
     * 
     * @param targetID Ŀ��id��Ҳ���ǵȴ���decode������
     * @param st ���Ķ���ʼȡ�������ȡ����ߵ�һλ��ô��������st = 0;ed =1;
     * @param ed ȡ���Ķ��������ȡ����ߵ���λ����ô��������st = 0;ed = 2;
     * @return
     */
    public static long left(long targetID, int st, int ed) {
        long end = getModRight(targetID, ed, 19);
        return end % pow10[(ed - st)];
    }

    /**
     * ���ҿ�ʼ��ȡָ�����λ����
     * 
     * @param targetID Ŀ��id��Ҳ���ǵȴ���decode������
     * @param st ���Ķ���ʼȡ�������ȡ���ұߵ�һλ��ô��������st = 0;ed =1;
     * @param ed ȡ���Ķ��������ȡ���ұߵ���λ����ô��������st = 0;ed = 2;
     * @return
     */
    public static long right(long targetID, int st, int ed) {
        long right = targetID % pow10[ed];
        return right / pow10[(st)];
    }

    public static String right(String right, int rightLength) {
        int length = right.length();
        int start = length - rightLength;
        return right.substring(start < 0 ? 0 : start);
    }

    public static void main(String[] args) {
        //		String l = "8l";
        //System.out.println(right(l, 2));
    }
}
