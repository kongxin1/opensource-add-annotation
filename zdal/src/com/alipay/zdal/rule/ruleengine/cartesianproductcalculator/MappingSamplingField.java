/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.cartesianproductcalculator;

import java.util.List;

/**
 * �����ӳ�丽�����ֶΣ�
 * 
 *
 */
public abstract class MappingSamplingField extends SamplingField {

    public MappingSamplingField(List<String> columns, int capacity) {
        super(columns, capacity);
    }

}
