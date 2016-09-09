/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.common;

/**
 * @author added by fanzeng
 * �������ö�����͵�Ŀ���ǣ��ڽ��ж�д���Ե�ʱ�򣬸���read������write�������������ж�������Ƕ������Խ���
 * �����ԣ��������д��ֻ��PriorityDbGroupSelector��ʱ��ſ��Խ���д���ԣ�������дp0�����дp0ʧ�ܺ�����дp1��
 */
public enum OperationDBType {
    readFromDb(0), writeIntoDb(1);
    private int i;

    private OperationDBType(int i) {
        this.i = i;
    }

    public int value() {
        return this.i;
    }

    public static OperationDBType valueOf(int i) {
        for (OperationDBType t : values()) {
            if (t.value() == i) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid operationDBType:" + i);
    }
}
