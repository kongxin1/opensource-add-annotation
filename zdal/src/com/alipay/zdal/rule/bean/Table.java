/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import com.alipay.zdal.rule.ruleengine.entities.abstractentities.SharedElement;

/**
 * ��Ҳ���Ƿֿ�ֱ�����е���С��λ
 * 
 *
 */
public class Table extends SharedElement {
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        if (this.tableName == null) {
            this.tableName = tableName;
        } else {
            throw new IllegalArgumentException("you can't modify this element");
        }
    }

    @Override
    public String toString() {
        return "table:" + tableName;
    }

}
