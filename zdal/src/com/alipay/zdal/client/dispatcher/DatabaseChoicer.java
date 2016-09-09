/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.dispatcher;

import java.util.List;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;

/**
 * ����Դ�Ͷ�Ӧ�б��ѡ����������ͨ��sql��arg��ȡִ��Ŀ��
 * Ҳ����ͨ��rc��ȡ��ͬʱ������ͨ������ӿڻ�����е����ݿ�ͱ�
 * 
 * Result�ṹ���ڲ�ʵ���޹أ�ҵ�񷽿��Խ����޸� ����Ӱ�쵽Zdal�ڲ�ʵ�֡�
 * 
 *
 */
public interface DatabaseChoicer {
    /**
     * ��ȡ��ǰ���ݿ�ͱ�
     * @param sql
     * @param args
     * @return
     * @throws ZdalCheckedExcption
     */
    Result getDBAndTables(String sql, List<Object> args) throws ZdalCheckedExcption;

    /**
     * ������SQL����ThreadLocal�����ָ������RouteCondition�����������Ŀ�ĵصĽӿ�
     * @param rc
     * @return
     */
    Result getDBAndTables(RouteCondition rc);

    /**
     * ��ȡȫ��ȫ����Ϣ
     * @param logicTableName
     * @return
     */
    Result getAllDatabasesAndTables(String logicTableName);
}
