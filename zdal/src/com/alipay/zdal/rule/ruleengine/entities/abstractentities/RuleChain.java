/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.abstractentities;

import java.util.List;
import java.util.Set;

import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

/**
 * 
 * ����������
 * 
 * ��Ҫע���������������е�����������ȫ��ͬ:--->�������еĹ����б��Լ��Ƿ������ݿ����
 * ��ô���Ǿ���Ϊ��������ȫ��ͬ����Ϊ
 * 
 * 1.���������ͬ��sql��һ���ģ�����ܹ��ṩ�������������Ĳ�����һ�µġ�
 * 2.����һ�� ������Ҳһ�£���˲���Ҫ���ж�μ��㡣
 * 
 *
 */
public interface RuleChain {
    boolean isDatabaseRuleChain();

    //	/**
    //	 * ����index�Ͳ������м���
    //	 * 
    //	 * @param index
    //	 * @param args
    //	 * @return ��null��List
    //	 */
    //	Map<String/*column*/,Map<String/*�����ֵ*/,Set<Object>/*�õ��ý�������ֵ��*/>> calculate(int index,Map<String, Comparative> args);
    //	
    /**
     * ����index��ȡ��Ӧ����
     * 
     * @param index ���indexΪ-1�򷵻�null
     * @return
     */
    ListAbstractResultRule getRuleByIndex(int index);

    /**
     * ��ȡ���ռ�������Ĳ����б�
     * @return
     */
    List<Set<String>> getRequiredArgumentSortByLevel();

    /**
     * ��ȡ�������еĹ����б�
     * @return
     */
    List<ListAbstractResultRule> getListResultRule();

    void init();

}
