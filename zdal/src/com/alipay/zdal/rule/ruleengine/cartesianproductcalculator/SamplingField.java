/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.cartesianproductcalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * �����ѿ������Ժ��һ��ֵ����Ϊ�ж��������ÿ��������������з�Χ������£�
 * Ҫ�����������ֻ�н��еѿ�������ö�ٳ����п��ܵ�ֵ���������㡣
 * �������ö��ֵ�е�һ����
 * columns�ǹ�����С�����samplingField���Ƽ��Σ����Ṳ��ͬһ��������
 * ��enumFields���ʾ����������˳��ͨ���ѿ���������ʽö�ٳ���һ��ֵ��
 * 
 *
 */
public class SamplingField {
    /**
     * ��ʾ����������˳��ͨ���ѿ���������ʽö�ٳ���һ��ֵ
     */
    final List<Object>         enumFields;

    /**
     * һ������
     */
    private final List<String> columns;

    private String             mappingTargetKey;

    private Object             mappingValue;
    final int                  capacity;

    public SamplingField(List<String> columns, int capacity) {
        this.enumFields = new ArrayList<Object>(capacity);
        this.capacity = capacity;
        this.columns = Collections.unmodifiableList(columns);
    }

    public void add(int index, Object value) {

        enumFields.add(index, value);
    }

    //	public final Object clone() throws CloneNotSupportedException {
    //		SamplingField samplingFiled = new SamplingField(columns,capacity);
    //		return samplingFiled;
    //	}

    public List<String> getColumns() {
        return columns;
    }

    public List<Object> getEnumFields() {
        return enumFields;
    }

    public void clear() {
        if (enumFields != null) {
            enumFields.clear();
        }
    }

    public String getMappingTargetKey() {
        return mappingTargetKey;
    }

    public void setMappingTargetKey(String mappingTargetKey) {
        this.mappingTargetKey = mappingTargetKey;
    }

    /*public void setEnumFields(List<Object> enumFields) {
    	this.enumFields = enumFields;
    }*/
    @Override
    public String toString() {
        return "columns:" + columns + "enumedFileds:" + enumFields;
    }

    public Object getMappingValue() {
        return mappingValue;
    }

    public void setMappingValue(Object mappingValue) {
        this.mappingValue = mappingValue;
    }

}
