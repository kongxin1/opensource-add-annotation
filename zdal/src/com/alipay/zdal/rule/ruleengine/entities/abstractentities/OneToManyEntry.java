/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.abstractentities;

import java.util.Collections;
import java.util.List;

import com.alipay.zdal.rule.bean.RuleChainImp;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement.DEFAULT_LIST_RESULT_STRAGETY;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.DefaultTableMapProvider;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

/**
 * ����+��ɢһЩ���Ե�ָ�����ӽڵ���
 * 
 * ������һЩһ�Զ�ķַ��������õ���������й���
 * 
 * û���ر�����壬ֻ��һ����һ������ĳ��󣬻ᷢ����logictable��database�����һЩ������Ҫ���ݸ������ӽڵ㡣
 * 
 * �����������ݶ����Էŵ�����߼������С�
 * 
 * 
 * 
 * 
 */
public class OneToManyEntry {

    /**
     * ���ݸ����ɱ����ı�ݷ�����
     */
    private String                         logicTableName;

    /**
     * ���ַַ��ı����
     */
    protected RuleChain                    transmitedtableRuleChain;
    /**
     * �Ǳ���������һ�ֱ�����ʽ
     */
    protected List<ListAbstractResultRule> tableRuleList;
    /**
     * ������������database�ڵ�ʹ��
     */
    private TableMapProvider               tableMapProvider = new DefaultTableMapProvider();
    DEFAULT_LIST_RESULT_STRAGETY           defaultListResultStragety;
    private boolean                        isInited         = false;

    // /**
    // * ����һЩ��Ҫ��ɢ�����ݴӸ����䵽�ӽڵ�
    // * ���������init����֮ǰ������
    // */
    // public void transmit() {
    // for (SharedElement sharedElement : subSharedElement.values()) {
    // if (isCurrentSubSharedElementGlovalEntryTransmitter(sharedElement)) {
    //
    // OneToManyEntry transmitter = upcastSubEntry(sharedElement);
    // //����򴫵�
    // transmitter.setTableRuleChain(transmitedtableRuleChain);
    // // ���ڱ�ݵĹ����
    // transmitter.setLogicTableName(logicTableName);
    // // ��table map������������ȥ
    // transmitter.setTableMapProvider(tableMapProvider);
    // }
    // }
    // }
    //
    // private OneToManyEntry upcastSubEntry(SharedElement sharedElement) {
    // OneToManyEntry transmitter = (OneToManyEntry) sharedElement;
    // return transmitter;
    // }
    //
    // private boolean isCurrentSubSharedElementGlovalEntryTransmitter(
    // SharedElement sharedElement) {
    // return sharedElement instanceof OneToManyEntry;
    // }

    public List<ListAbstractResultRule> getTableRule() {
        if (transmitedtableRuleChain != null) {
            return transmitedtableRuleChain.getListResultRule();
        } else {
            return Collections.emptyList();
        }

    }

    /**
     * ���������Ĭ�ϵ�����£�Ŀ����transmitedtableRuleChain.
     * ����database�㸲���������������Ŀ��ָ��ÿһ��database��tableRule.
     * 
     * �������ᱻtransmit�������á�
     * 
     * @param tableRule
     */
    public void setTableRuleChain(RuleChain ruleChain) {
        this.transmitedtableRuleChain = ruleChain;
    }

    public RuleChain getTableRuleChain() {
        return transmitedtableRuleChain;
    }

    public void setTableRule(List<ListAbstractResultRule> tableRule) {
        tableRuleList = tableRule;
    }

    public void init() {
        if (isInited) {
            return;
        }
        isInited = true;
        if (tableRuleList != null) {
            RuleChainImp ruleChainImp = getRuleChain(tableRuleList);

            if (transmitedtableRuleChain == null) {
                transmitedtableRuleChain = ruleChainImp;
            } else {
                throw new IllegalArgumentException("ruleChain�Ѿ��������ˣ�����Ȼ����ʹ��init�����г�ʼ��");
            }
        }
        if (transmitedtableRuleChain != null) {
            transmitedtableRuleChain.init();
        }

    }

    public static RuleChainImp getRuleChain(List<ListAbstractResultRule> tableRuleList) {
        RuleChainImp ruleChainImp = new RuleChainImp();
        ruleChainImp.setDatabaseRuleChain(false);
        ruleChainImp.setListResultRule(tableRuleList);
        return ruleChainImp;
    }

    public TableMapProvider getTableMapProvider() {
        return tableMapProvider;
    }

    /**
     * ��ΪtableMapProvider��Ҫ���ݸ�ÿһ��database������������ǰdatabase�����Ѿ�����tableMapProvider
     * ��ô�Ͳ���Ҫ�ٽ��ⲿ��tableMapProviderָ��������
     * 
     * @param tableMapProvider
     */
    public void setTableMapProvider(TableMapProvider tableMapProvider) {
        // ��������tableProvider��Ϊ��
        // --���ҵ�ǰ�ڵ��tableMapProviderΪ�ջ�ΪĬ�ϱ����(Ҳ����Ĭ�ϵ���logicTable��Ϊ�����Ļ�)
        // --&& (this.tableMapProvider == null || this.tableMapProvider
        // instanceof DefaultTableMapProvider)
        if (tableMapProvider != null && (!(tableMapProvider instanceof DefaultTableMapProvider))) {
            this.tableMapProvider = tableMapProvider;
        }
    }

    public String getLogicTableName() {
        return logicTableName;
    }

    public void setLogicTableName(String logicTablename) {
        this.logicTableName = logicTablename;
    }

    public DEFAULT_LIST_RESULT_STRAGETY getDefaultListResultStragety() {
        return defaultListResultStragety;
    }

    public void setDefaultListResultStragety(DEFAULT_LIST_RESULT_STRAGETY defaultListResultStragety) {
        this.defaultListResultStragety = defaultListResultStragety;
    }

}
