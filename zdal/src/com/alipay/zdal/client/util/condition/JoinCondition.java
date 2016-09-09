/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2009 All Rights Reserved.
 */
package com.alipay.zdal.client.util.condition;

import java.util.ArrayList;
import java.util.List;

/**
 * ����RouteCondition��ʽ�£���join�ļ�֧�֣�����ֻ������˱�join������������ڶ�sql���б����滻��ʱ���õ�
 *
 * @version $Id: JoinCondition.java, v 0.1 2010-1-29 ����02:02:01 jiangping Exp $
 */
public class JoinCondition extends AdvanceCondition {
    List<String> virtualJoinTableNames = new ArrayList<String>();

    /**
     * @return Returns the virtualJoinTableNames.
     */
    public List<String> getVirtualJoinTableNames() {
        return virtualJoinTableNames;
    }

    /**
     * @param virtualJoinTableNames The virtualJoinTableNames to set.
     */
    public void setVirtualJoinTableNames(List<String> virtualJoinTableNames) {
        this.virtualJoinTableNames = virtualJoinTableNames;
    }

    /**
     * ��ӱ�join���������
     * @param virtualTableName
     */
    public void addVirtualJoinTableName(String virtualTableName) {
        if (virtualTableName == null) {
            throw new IllegalArgumentException("�������߼�����");
        }
        this.virtualJoinTableNames.add(virtualTableName.toLowerCase());
    }
}
