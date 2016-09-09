/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author ����
 * @version $Id: DBSelectorIDRouteCondition.java, v 0.1 2014-1-6 ����05:16:07 Exp $
 */
public class DBSelectorIDRouteCondition implements DBSelectorRouteCondition {
    final String       dbSelectorID;
    final List<String> tableList = new ArrayList<String>();
    final String       logicTableName;
    volatile boolean   used;

    /**
     * �߼�������Ŀ�������ȫһ�µļ򻯷���
     * 
     * @param logicTableName
     * @param dbSelectorID
     */
    public DBSelectorIDRouteCondition(String logicTableName, String dbSelectorID) {
        this(logicTableName, dbSelectorID, logicTableName);
    }

    /**
     * ����һ��ֱ��ͨ���߼����������ݿ�ִ��id��ʵ�ʱ�����ִ��SQL��RouteCondition
     * 
     * @param logicTableName
     * @param dbSelectorID
     * @param tables
     */
    public DBSelectorIDRouteCondition(String logicTableName, String dbSelectorID, String... tables) {
        this.dbSelectorID = dbSelectorID;
        this.logicTableName = logicTableName;
        List<String> list = Arrays.asList(tables);
        tableList.addAll(list);
    }

    public String getDBSelectorID() {
        //ò��û�еط�set used = true;
        if (!used) {
            return dbSelectorID;
        } else {
            throw new IllegalArgumentException("һ��route����ֻ�ܱ�ʹ��һ�Σ������½���" + "route����");
        }
    }

    public void addTable(String table) {
        tableList.add(table);
    }

    public void addAllTable(Collection<String> tables) {
        tableList.addAll(tables);
    }

    public void addAllTable(String[] tables) {
        tableList.addAll(Arrays.asList(tables));
    }

    public List<String> getTableList() {
        return tableList;
    }

    public String getVirtualTableName() {
        return logicTableName;
    }

}
