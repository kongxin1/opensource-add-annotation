/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.controller;

import java.util.ArrayList;
import java.util.List;

import com.alipay.zdal.client.dispatcher.DispatcherResult;
import com.alipay.zdal.client.dispatcher.EXECUTE_PLAN;
import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 * һ�����ո���StatementҪ��ô���Ķ���
 * 
 * �������˷ֿ�ֱ�Ľ����Ҳ������SQL�������Ϣ���͹����е�һЩ����
 * 
 * �Ǵ�SQL�����������ȡ��ƥ�����г�ȡ��Ҫ��Ϣ��װ���ɵ�
 * 
 * TargetDBMeta �� TargetDBMetaData �ϲ���ֱ����
 * 1. �޸��˷ֱ������Ϊ���
 * 
 * TODO final���⡣�Ƿ���������������
 * 
 *
 */
public class DispatcherResultImp implements DispatcherResult {
    /**
     * maxֵ�����sql�и�����limit m,n,��rownum<xx ��maxֵ����֮�仯ΪӦ���е�ֵ
     * ��Ҫע����ǣ�max������limitTo�ĺ��壬�����ʵ��󶼻��Ϊ xxx<max����������
     * <p>����oracle: rownum<=n max=n+1</p>
     * <p>����mysql: limit m,n max=m+n</p>
     */
    private final int                  max;
    /**
     * skipֵ�����sql�и�����limit m,n,��rownum>xx ��skipֵ����֮�仯ΪӦ���е�ֵ
     * ��Ҫע����ǣ�skip������limitFrom�ĺ��壬�����ʵ��󶼻��Ϊ xxx>=m����������
     * <p>����oracle: rownum>n skip=n+1</p>
     * <p>����mysql: limit m,n skip=m</p>
     */
    private final int                  skip;

    /**
     * sql �е�order by ��Ϣ
     */
    private final OrderByMessages      orderByMessages;
    /**
     * ��sql�������Ƕ�׵�select�е�columns�����group function��Ϣ��
     * ���ô���group function,��parser���������жϣ�ȷ��ֻ��һ��group function��û�������С���������׳��쳣
     * ����������group function����û�������д��ڣ���᷵�ظ�group function��Ӧ��Type
     * ���û��group function�������������͵�sql(insert update��)���򷵻�normal.
     */
    private final GroupFunctionType    groupFunctionType;

    /**
     * TODO primaryKey, splitDB, splitTab ��ֻ�����и���ʱ����ֵ���ƻ��˲����ԣ����Կ����Ż�
     */
    /**
     * �������ֿ�������ǲ���������
     */
    private ColumnMetaData             uniqueKey;
    /**
     * �ֿ���б���Ϊ�ֿ���������������ģ������Ǹ�list.���������xml��������parameters���ÿһ��
     * ��','�ָ�����Ŀ����Ӧlist�е�һ�ColumnMetaData�е�key��Ӧ��parameters��ÿһ����','�ָ�����Ŀ
     * ��value��Ӧ�Ѿ�ͨ�����㲢�Ұ��˱����Ժ��ֵ�����ֵ����Ϊnull,Ϊnull���ʾ�û�û����sql�и�����Ӧ �Ĳ�����
     */
    private final List<ColumnMetaData> splitDB  = new ArrayList<ColumnMetaData>();
    /**
     * �ֱ������Ϊ�ֱ���������������ģ������Ǹ�ColumnMetaData����.���������xml�������˱�����е�parameters��
     * ����ÿһ����','�ָ�����Ŀ����Ӧlist�е�һ�ColumnMetaData�е�key��Ӧ��parameters��ÿһ����','�ָ�����Ŀ
     * ��value��Ӧ�Ѿ�ͨ�����㲢�Ұ��˱����Ժ��ֵ�����ֵ����Ϊnull,Ϊnull���ʾ�û�û����sql�и�����Ӧ �Ĳ�����
     */
    private final List<ColumnMetaData> splitTab = new ArrayList<ColumnMetaData>();

    /**
     * ���ݿ�ִ�мƻ�
     */
    private EXECUTE_PLAN               databaseExecutePlan;
    /**
     * ���ִ�мƻ�������ж��������Ķ����ĸ�����ͬ����ô���ձ�����������Ǹ�ֵΪ׼��
     * ������db1~5����ĸ����ֱ�Ϊ0,0,0,0,1:��ô���صı�ִ�мƻ�ΪSINGLE
     * ������ĸ����ֱ�Ϊ0,1,2,3,4,5����ô���ر��ִ�мƻ�ΪMULTIPLE.
     */
    private EXECUTE_PLAN               tableExecutePlan;

    public DispatcherResultImp(String virtualTableName, List<TargetDB> targetdbs,
                               boolean allowReverseOutput, int skip, int max,
                               OrderByMessages orderByMessages, GroupFunctionType groupFunctionType) {
        this.skip = skip;
        this.max = max;

        this.orderByMessages = orderByMessages;
        this.groupFunctionType = groupFunctionType;

        this.virtualTableName = virtualTableName;
        this.target = targetdbs;
        this.allowReverseOutput = allowReverseOutput;
    }

    /**
     * �Ƿ����������
     */
    private boolean              allowReverseOutput;
    /**
     * Ŀ���
     */
    private final List<TargetDB> target;
    /**
     * �������
     */
    private final String         virtualTableName;

    public List<TargetDB> getTarget() {
        return target;
    }

    public int getMax() {
        return max;
    }

    public int getSkip() {
        return skip;
    }

    public OrderByMessages getOrderByMessages() {
        return orderByMessages;
    }

    public String getVirtualTableName() {
        return this.virtualTableName;
    }

    public ColumnMetaData getPrimaryKey() {
        return uniqueKey;
    }

    public void setUniqueKey(ColumnMetaData uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public List<ColumnMetaData> getSplitDB() {
        return splitDB;
    }

    public void addSplitDB(ColumnMetaData splitDB) {
        this.splitDB.add(splitDB);
    }

    public void addSplitTab(ColumnMetaData splitTab) {
        this.splitTab.add(splitTab);
    }

    public boolean allowReverseOutput() {
        return this.allowReverseOutput;
    }

    public void needAllowReverseOutput(boolean reverse) {
        this.allowReverseOutput = reverse;
    }

    public GroupFunctionType getGroupFunctionType() {
        return groupFunctionType;
    }

    public List<ColumnMetaData> getSplitTab() {
        return splitTab;
    }

    public EXECUTE_PLAN getDatabaseExecutePlan() {
        return databaseExecutePlan;
    }

    public void setDatabaseExecutePlan(EXECUTE_PLAN databaseExecutePlan) {
        this.databaseExecutePlan = databaseExecutePlan;
    }

    public EXECUTE_PLAN getTableExecutePlan() {
        return tableExecutePlan;
    }

    public void setTableExecutePlan(EXECUTE_PLAN executePlan) {
        this.tableExecutePlan = executePlan;
    }

    /** ��join��������� */
    List<String> virtualJoinTableNames = new ArrayList<String>();

    public List<String> getVirtualJoinTableNames() {
        return virtualJoinTableNames;
    }

    public void setVirtualJoinTableNames(List<String> virtualJoinTableNames) {
        this.virtualJoinTableNames.addAll(virtualJoinTableNames);
    }

}
