/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.controller;

import java.util.List;
import java.util.Map;

import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 * ƥ��Ľ�����󣬹�����Controller���з��ض����ƴװ
 * 
 * 
 * ��Щ�Ǵ���Ĵ�ƥ���п��Ի�õ����� ��Ҫ��Ӧ������Щ����Щ���Ƿ���������ֿ�ֱ����
 * 
 *
 */
public interface MatcherResult {
    /**
     * ��������Ľ������
     * @return
     */
    List<TargetDB> getCalculationResult();

    /**
     * ƥ��Ŀ������ʲô,�������Nullֵ
     * @return
     */
    Map<String, Comparative> getDatabaseComparativeMap();

    /**
     * ƥ��ı������ʲô,�������nullֵ
     * @return
     */
    Map<String, Comparative> getTableComparativeMap();
}
