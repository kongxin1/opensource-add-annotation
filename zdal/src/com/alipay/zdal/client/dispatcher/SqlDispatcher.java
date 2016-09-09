/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.dispatcher;

import java.util.List;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;

/**
 * ��TStatement�ĽǶȿ���ֻ�贫��sql����������Ϳ��Եõ�������Ϣ��
 *    1. ���sql��Ҫ����Щ�����ִ��
 *    2. ��Щsql������Щ����ĺ�������ҪTStatement���ر�Ĵ�������
 *       a)
 *       b)
 * �����������ӿ�
 * 
 * Ҫ�������ӿ�Ҫ��������������Ҫ���²��裺
 *    1. ����sql�õ�sql����Ľṹ����Ϣ
 *    2. �ӽ�������õ��߼�����, �Ӷ��õ���Ӧ��һ�׹���
 *    3. �ӹ���õ��ֿ�ֱ��ֶ���Ϣ���ӽ�������õ���Щ�ֶ���sql�е�����
 *    4. ���ݷֿ�ֱ��ֶ���sql�е�������=��Χ�����͹�����ƥ��
 * 
 * ����Ҫ��һ�������¼����ӿ�: ����������ƥ��
 *  
 */
public interface SqlDispatcher extends DatabaseChoicer {
    /**
     * ��ȡ��ǰ���ݿ�ͱ�
     * @param sql
     * @param args
     * @return
     * @throws ZdalCheckedExcption
     */
    DispatcherResult getDBAndTables(String sql, List<Object> args) throws ZdalCheckedExcption;

    /**
     * ������SQL����ThreadLocal�����ָ������RouteCondition�����������Ŀ�ĵصĽӿ�
     * @param rc
     * @return
     */
    DispatcherResult getDBAndTables(RouteCondition rc);

    DBType getDBType();

    Result getAllDatabasesAndTables(String logicTableName);
}
