/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

public class RuleChainImp implements RuleChain {

    public static final int                NO_INDEX            = -1;
    /**
     * �жϵ�ǰRuleChain�Ƿ��ʼ������ֹ����д�������ǡ�
     */
    private volatile boolean               isInited            = false;

    /**
     * ��Ҫ���ڴ��ݿ�����ͱ����ʱʹ�á�
     */
    private boolean                        isDatabaseRuleChain = false;

    private List<Set<String>>              requiredArgumentSortByLevel;

    /**
     * ���Ϊһ��list ����ļ���ʽ������һ�Զ�������
     */
    protected List<ListAbstractResultRule> listResultRule;

    public ListAbstractResultRule getRuleByIndex(int index) {
        if (index != NO_INDEX) {
            return getListRuleByIndexInternal(index);
        } else {
            return null;
        }
    }

    private ListAbstractResultRule getListRuleByIndexInternal(int index) {
        ListAbstractResultRule listRule = listResultRule.get(index);
        return listRule;
    }

    /*	public Set<String> �õ��ý�������ֵ��  calculateTable(int index, Map<String,Set<Object>> args) {
    		if(index != NO_INDEX ){
    			if(args == null|| args.isEmpty()){
    				throw new IllegalArgumentException("argsΪ��");
    			}
    			ListAbstractResultRule listRule = getListRuleByIndexInternal(index);
    			Set<String> result = listRule.evalWithoutSourceTrace(args);
    			return result;
    		}else{
    			return Collections.emptySet();
    		}
    	}*/

    public List<Set<String>> getRequiredArgumentSortByLevel() {
        if (!isInited) {
            throw new IllegalStateException("not inited ");
        }
        return requiredArgumentSortByLevel;
    }

    public void init() {
        if (!isInited) {
            if (listResultRule == null) {
                throw new IllegalArgumentException("û���������");
            }
            requiredArgumentSortByLevel = new ArrayList<Set<String>>(listResultRule.size());
            for (ListAbstractResultRule listRule : listResultRule) {
                listRule.initRule();
                Set<AdvancedParameter> parameterSet = listRule.getParameters();
                Set<String> argStringSet = new HashSet<String>(parameterSet.size());
                for (AdvancedParameter keyAndAtomIncValue : parameterSet) {
                    argStringSet.add(keyAndAtomIncValue.key);
                }
                requiredArgumentSortByLevel.add(argStringSet);
            }

            isInited = true;
        }
    }

    public List<ListAbstractResultRule> getListResultRule() {
        return listResultRule;
    }

    public void setListResultRule(List<ListAbstractResultRule> listResultRule) {
        this.listResultRule = listResultRule;
    }

    public void setDatabaseRuleChain(boolean isDatabaseRuleChain) {
        this.isDatabaseRuleChain = isDatabaseRuleChain;
    }

    public boolean isDatabaseRuleChain() {
        return isDatabaseRuleChain;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RuleChain)) {
            return false;
        }
        RuleChain targetRuleChain = (RuleChain) obj;
        List<ListAbstractResultRule> listResultRules = targetRuleChain.getListResultRule();
        boolean isDatabase = targetRuleChain.isDatabaseRuleChain();
        if ((isDatabase == this.isDatabaseRuleChain) && this.listResultRule == listResultRules) {
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        int result = 19;
        result = 31 * result + listResultRule.hashCode();
        result = 31 * result + (isDatabaseRuleChain ? 0 : 1);
        return result;
    }
}
