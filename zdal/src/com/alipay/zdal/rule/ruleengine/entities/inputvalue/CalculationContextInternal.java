/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.inputvalue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;

public class CalculationContextInternal {
    public final RuleChain                              ruleChain;
    protected Map<String, Set<Object>>                  result = Collections.emptyMap();
    public final int                                    index;                          //�������ľ�������Ǹ���index��RuleChain��ȡ�õ�
    public final Map<String/*��ǰ����Ҫ�������*/, Comparative> sqlArgs;

    public CalculationContextInternal(RuleChain ruleChain, int index,
                                      Map<String/*��ǰ����Ҫ�������*/, Comparative> sqlArgs) {
        this.ruleChain = ruleChain;
        this.index = index;
        this.sqlArgs = sqlArgs;
    }
}
