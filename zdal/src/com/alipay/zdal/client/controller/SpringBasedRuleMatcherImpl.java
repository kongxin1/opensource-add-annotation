/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.client.dispatcher.Matcher;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.parser.sqlobjecttree.ComparativeMapChoicer;
import com.alipay.zdal.rule.LogicTableRule;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.inputvalue.CalculationContextInternal;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

public class SpringBasedRuleMatcherImpl implements Matcher {

    public MatcherResult match(ComparativeMapChoicer comparativeMapChoicer, List<Object> args,
                               LogicTableRule rule) throws ZdalCheckedExcption {
        //�����������ϣ������˹��������й�����
        Set<RuleChain> ruleChainSet = rule.getRuleChainSet();
        //����Ҫ������ݿ�ֿ��ֶκͶ�Ӧ��ֵ������ж����ô�������һ��
        Map<String, Comparative> comparativeMapDatabase = new HashMap<String, Comparative>(2);
        //����Ҫ���talbe�ֱ��ֶκͶ�Ӧ��ֵ������ж����ô�������һ��
        Map<String, Comparative> comparativeTable = new HashMap<String, Comparative>(2);

        Map<RuleChain, CalculationContextInternal/*������Ľ��*/> resultMap = new HashMap<RuleChain, CalculationContextInternal>(
            ruleChainSet.size());

        for (RuleChain ruleChain : ruleChainSet) {

            // ���ÿһ��������
            List<Set<String>/*ÿһ��������Ҫ�Ĳ���*/> requiredArgumentSortByLevel = ruleChain
                .getRequiredArgumentSortByLevel();
            /*
             * ��ΪruleChain����ĸ�����һ���ģ�������getRequiredArgumentSortByLevel list��sizeһ���࣬��˲���Խ��
             */
            int index = 0;

            for (Set<String> oneLevelArgument : requiredArgumentSortByLevel) {
                // ���ÿһ���������е�һ�����𣬼����Ǵӵ͵��ߵ����Ȳ鿴�Ƿ��������Ҫ������������������
                /*��ǰ����Ҫ�������*/
                Map<String, Comparative> sqlArgs = comparativeMapChoicer.getColumnsMap(args,
                    oneLevelArgument);
                if (sqlArgs.size() == oneLevelArgument.size()) {
                    // ��ʾƥ��,��������Ϊkey,valueΪ���
                    resultMap.put(ruleChain, new CalculationContextInternal(ruleChain, index,
                        sqlArgs));
                    if (ruleChain.isDatabaseRuleChain()) {
                        comparativeMapDatabase.putAll(sqlArgs);
                    } else {
                        // isTableRuleChain
                        comparativeTable.putAll(sqlArgs);
                    }
                    break;
                } else {
                    index++;
                }

            }
            /*���һ����������sql�ж�û�б�ƥ�䵽����ô����������Ͳ��������resultMap�У�������һ��һ�Զ�ڵ�
             * �Ĺ�����sql��û�����м����ʱ�򣬻��õ�һ���յ�List index.��ʱ��ͻ�ʹ��Ĭ�Ϲ����null������������㡣
            */
        }
        //not null.ȷ����ÿ�ι�����������
        List<TargetDB> calc = rule.calculate(resultMap);
        return buildMatcherResult(comparativeMapDatabase, comparativeTable, calc);

    }

    public MatcherResult buildMatcherResult(Map<String, Comparative> comparativeMapDatabase,
                                            Map<String, Comparative> comparativeTable,
                                            List<TargetDB> targetDB) {
        MatcherResultImp result = new MatcherResultImp();
        result.setCalculationResult(targetDB);
        result.setDatabaseComparativeMap(comparativeMapDatabase);
        result.setTableComparativeMap(comparativeTable);
        return result;
    }

}
