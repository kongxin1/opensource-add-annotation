/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.parser.visitor.OrderByEle;

/**
 * sqlִ�е�������.
 * @author ����
 * @version $Id: SqlExecutionContext.java, v 0.1 2014-1-6 ����04:54:59 Exp $
 */
public class SqlExecutionContext {
    private Map<String/*����ԴID*/, SqlAndTable[]/*����Դ��Ҫִ�е�SQL*/> targetSqls                 = new HashMap<String, SqlAndTable[]>();
    private Map<Integer/*�󶨲�����ţ���0��ʼ*/, Object/*�󶨲���*/>        changedParameters          = Collections
                                                                                                .emptyMap();
    private List<OrderByEle>                                     orderByColumns;
    private int                                                  skip;
    private int                                                  max;
    private GroupFunctionType                                    groupFunctionType;
    /**
     * �����Ƿ�ʹ�ÿս��������Ҫ��Ϊ��mappring rule���sql����Ȼ�п��Ա�mappingruleʹ�õķֿ�ֱ�����
     * �����ݿ���û�����ݵ�ʱ��ҵ��Ҫ�󷵻ؿյĽ������
     */
    private boolean                                              mappingRuleReturnNullValue = false;

    //private RetringContext retringContext;
    //private boolean needRetry;
    Map<DataSource, SQLException>                                failedDataSources;

    private String                                               virtualTableName;

    private List<SqlExecuteEvent>                                events;

    public Map<String, SqlAndTable[]> getTargetSqls() {
        return targetSqls;
    }

    public void setTargetSqls(Map<String, SqlAndTable[]> targetSqls) {
        this.targetSqls = targetSqls;
    }

    public Map<Integer, Object> getChangedParameters() {
        return changedParameters;
    }

    public void setChangedParameters(Map<Integer, Object> changedParameters) {
        this.changedParameters = changedParameters;
    }

    public List<OrderByEle> getOrderByColumns() {
        return orderByColumns;
    }

    public void setOrderByColumns(List<OrderByEle> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<SqlExecuteEvent> getEvents() {
        return events;
    }

    public void setEvents(List<SqlExecuteEvent> events) {
        this.events = events;
    }

    public void setGroupFunctionType(GroupFunctionType groupFunctionType) {
        this.groupFunctionType = groupFunctionType;
    }

    public GroupFunctionType getGroupFunctionType() {
        return groupFunctionType;
    }

    public String getVirtualTableName() {
        return virtualTableName;
    }

    public void setVirtualTableName(String virtualTableName) {
        this.virtualTableName = virtualTableName;
    }

    /*
    public RetringContext getRetringContext() {
    	return retringContext;
    }

    public void setRetringContext(RetringContext retringContext) {
    	this.retringContext = retringContext;
    }
    */

    public Map<DataSource, SQLException> getFailedDataSources() {
        return failedDataSources;
    }

    public void setFailedDataSources(Map<DataSource, SQLException> failedDataSources) {
        this.failedDataSources = failedDataSources;
    }

    public boolean mappingRuleReturnNullValue() {
        return mappingRuleReturnNullValue;
    }

    public void setRuleReturnNullValue(boolean needEmptyResultSet) {
        this.mappingRuleReturnNullValue = needEmptyResultSet;
    }

}
