/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.dispatcher;

import java.util.List;

import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 *
 * ִ�н������.
 */
public interface Result {
    /**
     * ��ȡ�������
     * @return
     */
    public String getVirtualTableName();

    /** 
     * ��ȡĿ����Ŀ�����б�
     */
    public List<TargetDB> getTarget();

    /**
     * ��ȡ��ǰsql��select | columns | from
     * ��columns������
     * ���Ϊmax min count�ȣ���ô���ͻ�����Ӧ�仯
     * ͬʱ���group function�����������ֶλ��ã�������᷵��NORMAL
     * @return
     */
    public GroupFunctionType getGroupFunctionType();

    /**
     * ��ȡmaxֵ
     * 
     * @return maxֵ����Ӧoracle����rownum<= ? ����rownum < ? .mysql��ӦLimint m,n�����m+n��Ĭ��ֵ��{@link DMLCommon.DEFAULT_SKIP_MAX}
     */
    public int getMax();

    /**
     * ��ȡskipֵ��
     * 
     * return skipֵ����Ӧoracle����rownum>= ? ����rownum > ? .mysql��ӦLimint m,n�����m��Ĭ��ֵ��{@link DMLCommon.DEFAULT_SKIP_MAX}
     * 
     */
    public int getSkip();
}
