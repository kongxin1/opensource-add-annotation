/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.retvalue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ŀ�����ݿ�����
 * ������дĿ��ds��id
 * �Լ���ds�з���Ҫ��ı����б�
 *
 */
public class TargetDB implements DatabasesAndTables {
    /**
     * �������TDatasource�����е�����
     */
    private String               dbIndex;
    /**
     * д����
     */
    private String[]             writePool;

    /**
     * ������
     */
    private String[]             readPool;
    /**
     * ��������µķ��ϲ�ѯ�����ı����б�
     */
    private Set<String>          tableNames;

    /**
     * ��ȡ���������� ������
     */
    private Map<Integer, Object> changedParams = Collections.emptyMap();

    public String[] getWritePool() {
        return writePool;
    }

    public void setWritePool(String[] writePool) {
        this.writePool = writePool;
    }

    public String[] getReadPool() {
        return readPool;
    }

    public void setReadPool(String[] readPool) {
        this.readPool = readPool;
    }

    /**
     * ���ر����Ľ����
     * 
     * @return ��Set if û�б�
     * 		   ���������	
     */
    public Set<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(Set<String> tableNames) {
        this.tableNames = tableNames;
    }

    public void addOneTable(String table) {
        if (tableNames == null) {
            tableNames = new HashSet<String>();
        }
        tableNames.add(table);
    }

    public String getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(String dbIndex) {
        this.dbIndex = dbIndex;
    }

    @Override
    public String toString() {
        return "TargetDB [dbIndex=" + dbIndex + ", readPool=" + Arrays.toString(readPool)
               + ", tableNames=" + tableNames + ", writePool=" + Arrays.toString(writePool) + "]";
    }

    public void setChangedParams(Map<Integer, Object> changedParams) {
        this.changedParams = changedParams;
    }

    public Map<Integer, Object> getChangedParams() {
        return changedParams;
    }
}
