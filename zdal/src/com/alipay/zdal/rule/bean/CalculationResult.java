/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.List;

import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 * ƥ��Ľ��
 * 
 *
 */
public interface CalculationResult {
    /**
     * ���ݵ�ǰ���򣬷���һ��TargetDB���б�
     * @return
     */
    public List<TargetDB> getTargetDBList();

}
