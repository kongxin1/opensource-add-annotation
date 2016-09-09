/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.abstractentities;

import java.util.List;

import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

/**
 * ����properties����������
 * 
 * �������һ��һ�Զ�Ľڵ㣬��ȻҲ����logicTable�ڵ㣬��ôʹ�õ�ǰ�ӿڵĽ����
 * 
 * ���ø���ǰ�ӿڵ����ݻᱻ��ɢ���ӽڵ㡣
 * 
 * ����ӽڵ㱾��Ҳ�ж�Ӧ�����ԣ����ӽڵ����Ի�����һ�Զഫ�ݹ��������ԡ�
 * 
 *
 */
public interface TablePropertiesSetter {
    public void setTableMapProvider(TableMapProvider tableMapProvider);

    public void setTableRule(List<ListAbstractResultRule> tableRule);

    public void setLogicTableName(String logicTable);

    public void setTableRuleChain(RuleChain ruleChain);
}
