/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * �������->sourceKey��ӳ�䡣
 * 
 * �����һ��Set���������ⳡ�������㡣
 * 
 * ���set����Ҫ���þ��Ǵ��sourceKey��ͬʱ��Ҳ���ӳ���Ľ��������������mapping rule�в�tair�Ժ�����ģ�Ϊ�˼���һ�β�
 * 
 * tair�Ĺ��̣����Ҫ��¼�²�tair�Ժ��ֵ������Щ�����Ұ��ս�����з��ࡣ
 * 
 * ��Ϊӳ�����ֻ��������Ψһ����������в������㡣
 * 
 * ���������ҽ���һ��������¡�set�е�targetValueӦ�þ���sourceKeyͨ��tairӳ���Ժ�Ľ����
 * 
 * �����������,mappingKeysӦ����ԶΪ�ա�
 * 
 * ����д�������ǲ���Ⱦ����sourceKeys�������߼��Ķ�
 * 
 *
 */
public class Field {
    public Field(int capacity) {
        sourceKeys = new HashMap<String, Set<Object>>(capacity);
    }

    public Map<String/* ���� */, Set<Object>/* �õ��ý�������ֵ�� */> sourceKeys;
    /**
    * ����ӳ������д��ӳ��������ֵ����Щֵ��Ӧ������ͬ����������ӦmappingTargetColumn
    */
    public Set<Object>                                        mappingKeys;
    /**
    * ��Ӧ����mappingKeys��targetColumn
    */
    public String                                             mappingTargetColumn;

    public static final Field                                 EMPTY_FIELD = new Field(0);
}
