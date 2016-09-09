/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.rule.groovy.GroovyListRuleEngine;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

/**
 * ��ӹ��򣬽��з������
 * @author zhaofeng.wang
 * @version $Id: TableShardingRuleImpl.java,v 0.1 2012-5-10 ����11:18:44 zhaofeng.wang Exp $
 */
public class TableShardingRuleImpl {

    private Map<String, String> ShardingParameters = new HashMap<String, String>();

    public Map<String, String> getColumnsMap(Set<String> partnationSet) {
        Map<String, String> retMap = new HashMap<String, String>(ShardingParameters.size());
        for (String str : partnationSet) {
            if (str != null) {
                //��Ϊgroovy�Ǵ�Сд���еģ��������ֻ����ƥ���ʱ��תΪСд������map�е�ʱ����Ȼʹ��ԭ���Ĵ�Сд
                String comp = ShardingParameters.get(str.toLowerCase());
                if (comp != null) {
                    retMap.put(str, comp);
                }
            }
        }
        return retMap;
    }

    public void put(Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("The parameters can't be null!");
        }
        //��ÿ��key����Сд����
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String parameter = entry.getValue();
            ShardingParameters.put(key.toLowerCase(), parameter);
        }
    }

    public String getShardingResult(RuleChain rc) {
        if (rc == null) {
            throw new IllegalArgumentException("The RuleChain can't be null!");
        }
        // ���ÿһ��������
        List<Set<String>/*ÿһ��������Ҫ�Ĳ���*/> requiredArgumentSortByLevel = rc
            .getRequiredArgumentSortByLevel();
        int index = 0;
        int ruleIndex = -1;
        Map<String/*��ǰ����Ҫ�������*/, String> argsMap = new HashMap<String, String>();
        for (Set<String> oneLevelArgument : requiredArgumentSortByLevel) {
            argsMap = getColumnsMap(oneLevelArgument);
            if (argsMap.size() == oneLevelArgument.size()) {
                ruleIndex = index;
                break;
            } else {
                index++;
            }
        }
        if (ruleIndex == -1) {
            throw new IllegalArgumentException("����������õ��ֶ��Ƿ���ȷ��");
        }
        ListAbstractResultRule rule = rc.getRuleByIndex(ruleIndex);
        Object[] args = new Object[] { argsMap };
        String result = ((GroovyListRuleEngine) rule).imvokeMethod(args);
        if (StringUtil.isBlank(result)) {
            throw new IllegalArgumentException("The result can not be null,the args "
                                               + args.toString());
        }
        return result;

    }
}
