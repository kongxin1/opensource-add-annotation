/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.rule.LogicTableRule;

public class ZdalRoot {
    private static final Logger               log    = Logger
                                                         .getLogger(Constants.CONFIG_LOG_NAME_LOGNAME);
    private DBType                            dbType = DBType.MYSQL;
    private Map<String/* key */, LogicTable> logicTableMap;
    private String                            defaultDBSelectorID;

    public Map<String, LogicTable> getLogicTableMap() {
        return Collections.unmodifiableMap(logicTableMap);
    }

    public LogicTableRule getLogicTableMap(String logicTableName) {
        LogicTableRule logicTableRule = getLogicTable(logicTableName);
        if (logicTableRule == null) {
            // �߼������������ڹ�����У����Դ�Ĭ�ϱ����Ѱ�ң�������Ҳ��������쳣�ˡ�
            if (defaultDBSelectorID != null && defaultDBSelectorID.length() != 0) {
                // �����Ĭ�Ϲ�����ô��ΪĬ�Ϲ����г��е�ֻ������Դ����Ҫ������������¡һ���Ժ������������֤�̰߳�ȫ
                DefaultLogicTableRule defaultLogicTableRule = new DefaultLogicTableRule(
                    defaultDBSelectorID, logicTableName);
                logicTableRule = defaultLogicTableRule;
            } else {
                throw new IllegalArgumentException("δ���ҵ���Ӧ����,�߼���:" + logicTableName);
            }
        }
        return logicTableRule;
    }

    public LogicTable getLogicTable(String logicTableName) {
        if (logicTableName == null) {
            throw new IllegalArgumentException("logic table name is null");
        }
        LogicTable logicTable = logicTableMap.get(logicTableName);
        return logicTable;
    }

    /**
     * logicMap��key���붼��ʾ������ΪСд
     * 
     * @param logicTableMap
     */
    public void setLogicTableMap(Map<String, LogicTable> logicTableMap) {
        this.logicTableMap = new HashMap<String, LogicTable>(logicTableMap.size());
        for (Entry<String, LogicTable> entry : logicTableMap.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                key = key.toLowerCase();
            }
            this.logicTableMap.put(key, entry.getValue());
        }
    }

    /**
     * ��Ҫע�����init�����Ǻ��ڲ����е����init�����޹صģ���Ȼ�����ڷ���һ����ʼ������
     * 
     */
    public void init(String appDsName) {
        for (Entry<String, LogicTable> logicTableEntry : logicTableMap.entrySet()) {
            log.warn("WARN ## logic Table is starting :" + appDsName + "."
                     + logicTableEntry.getKey());
            LogicTable logicTable = logicTableEntry.getValue();
            String logicTableName = logicTable.getLogicTableName();
            if (logicTableName == null || logicTableName.length() == 0) {
                // ���û��ָ��logicTableName,��ô��map��key��ΪlogicTable��key
                logicTable.setLogicTableName(logicTableEntry.getKey());
            }
            logicTable.setDBType(dbType);
            logicTable.init();
            log.warn("WARN ## logic Table inited :" + logicTable.toString());
        }
    }

    public DBType getDBType() {
        return dbType;
    }

    public void setDBType(DBType dbType) {
        this.dbType = dbType;
    }

    public String getDefaultDBSelectorID() {
        return defaultDBSelectorID;
    }

    public void setDefaultDBSelectorID(String defaultDBSelectorID) {
        this.defaultDBSelectorID = defaultDBSelectorID;
    }

}
