/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.abstractentities;

import java.util.List;

import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

public interface TableListResultRuleContainer {
    /**
     * ��ȫ�ֱ�������ø������������
     * ������óɹ��򷵻�true;
     * �������ʧ���򷵻�false;
     * 
     * @param listResultRule
     * @return
     */
    public boolean setTableListResultRule(List<ListAbstractResultRule> listResultRule);
}
