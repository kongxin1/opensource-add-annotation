/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

/**
 * ��Ϊ�����Ժ���õ����������ӳ���������Զ��õ�һ��mappingKey
 * 
 *
 */
public class ResultAndMappingKey {
    public ResultAndMappingKey(String result) {
        this.result = result;
    }

    final String result;
    /**
     * һ�����ֻ��֧����һ��mapping keyӳ���������֧�ֶ����
     * �������ֻ����mappingrule��ʱ�������
     */
    Object       mappingKey;

    String       mappingTargetColumn;
}
