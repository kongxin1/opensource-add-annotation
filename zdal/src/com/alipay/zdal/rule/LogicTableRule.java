/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.inputvalue.CalculationContextInternal;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

public interface LogicTableRule {
    Set<RuleChain> getRuleChainSet();

    //	void setNeedRowCopy(boolean needRowCopy);

    boolean isAllowReverseOutput();

    //	void setAllowReverseOutput(boolean allowReverseOutput);

    /**
     * ��ͬ�Ľڵ������Լ��Ľ���������ݽ������1�Զ�ӳ��
     * @param map
     * @return
     */
    public List<TargetDB> calculate(Map<RuleChain, CalculationContextInternal> map);

    public List<String> getUniqueColumns();

    //	public Map<String, ? extends SharedElement> getSubSharedElements();
    //	DBType getDBType();
}
