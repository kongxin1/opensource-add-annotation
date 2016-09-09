/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util.condition;

import java.util.HashSet;
import java.util.Set;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.exception.runtime.NotSupportException;
import com.alipay.zdal.rule.ruleengine.DBRuleProvider;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDBMetaData;

/**
 * 
 * @author ����
 * @version $Id: DirectlyRouteCondition.java, v 0.1 2014-1-6 ����05:16:21 Exp $
 */
public class DirectlyRouteCondition implements RouteCondition {
    DBType      dbType;

    Set<String> tables = new HashSet<String>(2);
    String      virtualTableName;
    String      dbRuleID;

    public Set<String> getTables() {
        if (tables.size() != 0) {
            return tables;
        } else {
            //���û��ָ���滻���������Ĭ��ʹ���������
            tables.add(virtualTableName);
            return tables;
        }
    }

    public TargetDBMetaData visit(DBRuleProvider provider) throws ZdalCheckedExcption {
        throw new NotSupportException("not support yet");
    }

    public void setDBType(DBType dbType) {
        this.dbType = dbType;
    }

    public DBType getDBType() {
        return dbType;
    }

    public void setTables(Set<String> tables) {
        this.tables = tables;
    }

    public void addATable(String table) {
        tables.add(table);
    }

    public String getVirtualTableName() {
        return virtualTableName;
    }

    /**
     * �������
     * @param virtualTableName
     */
    public void setVirtualTableName(String virtualTableName) {
        this.virtualTableName = virtualTableName;
    }

    public String getDbRuleID() {
        return dbRuleID;
    }

    /**
     * ��Ӧ���ڹ����ļ���ÿһ��rule����Ҫ��ָ�����Ǹ�id
     * @param dbRuleID
     */
    public void setDbRuleID(String dbRuleID) {
        this.dbRuleID = dbRuleID;
    }

}
