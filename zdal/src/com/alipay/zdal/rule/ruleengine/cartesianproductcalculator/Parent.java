/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.cartesianproductcalculator;

/**
 * ԭ���Ұɣ�ʵ�ڲ�֪�����ĸ���
 * 
 * parent ��λʱ��ļ�����
 *
 */
public interface Parent {
    /**
     * ѯ�ʸ�����û��ֵ
     * 
     * @return
     */
    public boolean parentHasNext();

    /**
     * ֪ͨparent��λ�ķ���
     */
    public void add();
}
