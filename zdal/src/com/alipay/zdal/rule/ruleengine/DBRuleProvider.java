/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine;

import java.util.Map;
import java.util.Set;

import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.PartitionElement;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDBMetaData;

public interface DBRuleProvider {
    /**
     * ��ȡĿ�����ݿ�id�ͱ������������ݽṹ��
     * һ��db���飬ÿ��db�������������
     * @param virtualTabName �������
     * @param comp where������ÿһ�����Եķ�Χ���������������{@link Main}�е�ʵ�ַ�ʽ
     * @param position ��where�����е�������comparable������λ�õ�������
     * i.e:where������������������
     * comp[0]=gmt��comp[1]=id��comp[2]=route��
     * ��ô�������Ӧ��Ϊ"gmt,id,route"
     * 
     * @return Ŀ�����ݿ���������List��List�е�ÿһ���Ӧһ���ֿ����ÿһ���ֿ�
     * �������������������������Ϊ��
     * @throws ZdalCheckedExcption
     */
    public TargetDBMetaData getDBAndTabs(String virtualTabName, Map<String, Comparative> colMap)
                                                                                                throws ZdalCheckedExcption;

    public TargetDBMetaData getDBAndTabs(String virtualTableName, String databaseGroupsID,
                                         Set<String> tables) throws ZdalCheckedExcption;

    //	/**
    //	 * ��ȡ�ֿ�����
    //	 * @param virtualTabName �������
    //	 *  @param isPK �����Ҫ���pk��Ϊtrue,����Ϊfalse
    //	 * @return  ����������
    //	 * 			����empty set, if doesn't have split column
    //	 */
    //	public Set<String> getSplitDBColumns(String virtualTabName,boolean isPK);
    /**
     * ��ȡ��ǰ�����ķֿ�ֱ��ֶ�
     * @important �������������getDb_type���������
     * @param virtualTableName
     * @return
     */
    public PartitionElement getPartitionColumns(String virtualTableName);

    public TargetDBMetaData getDBAndTabs(String logicTableName, Map<String, Comparative> colMap,
                                         int databaseRuleIndex, int tableRuleIndex)
                                                                                   throws ZdalCheckedExcption;

    /**
     * ��ȡ��ǰsql ��type.���Ƿ���mysql����oracle���ͻ�������ʲô���͡�
     * Ϊ�����ܿ��ǣ������������������getPartitionColumns�������á���Ϊ��������һ�Ρ�
     * @return
     */
    public DBType getDBType();
}