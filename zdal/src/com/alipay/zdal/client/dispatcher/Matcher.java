/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.dispatcher;

import java.util.List;
import java.util.Map;

import com.alipay.zdal.client.controller.MatcherResult;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.parser.sqlobjecttree.ComparativeMapChoicer;
import com.alipay.zdal.rule.LogicTableRule;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 * ƥ������õĽ�ڣ��Ὣsql������Ľ���������Լ��������ƥ��
 *
 */
public interface Matcher {
    /**
     * ����SqlParserResult pr + List<Object> args����Ҫ�����һ����С�Ķ���/�ӿ�
     * ����ҵ��ͨ��ThreadLocal��ʽ�ƹ�������ֱ��ָ��
     */
    MatcherResult match(ComparativeMapChoicer comparativeMapChoicer, List<Object> args,
                        LogicTableRule rule) throws ZdalCheckedExcption;

    MatcherResult buildMatcherResult(Map<String, Comparative> comparativeMapDatabase,
                                     Map<String, Comparative> comparativeTable, List<TargetDB> calc);
}
