/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.entities.retvalue;

import java.util.List;

public class TargetDBMetaData {

    /**
     * �Ƿ����������
     */
    private boolean              allowReverseOutput;
    /**
     * Ŀ���
     */
    private final List<TargetDB> target;

    /**
     * �������
     */
    private final String         virtualTableName;

    public TargetDBMetaData(String virtualTableName, List<TargetDB> targetdbs,
                            boolean allowReverseOutput) {
        this.virtualTableName = virtualTableName;
        this.target = targetdbs;

        this.allowReverseOutput = allowReverseOutput;
    }

    public List<TargetDB> getTarget() {
        return target;
    }

    public String getVirtualTableName() {
        return virtualTableName;
    }

    public boolean allowReverseOutput() {
        return allowReverseOutput;
    }

    public void needAllowReverseOutput(boolean reverse) {
        this.allowReverseOutput = reverse;
    }
}
