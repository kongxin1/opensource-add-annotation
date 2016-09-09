/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.config.beans;

public class MappingRuleBean {
    //	private String originalColumn;
    //	private String targetRule;
    //	private String dbIndex; //Ĭ���ñ�TDatasource
    //	private String routeTable; //·�ɱ�ӳ���key-value��
    //	private String routeColumn;//·�ɱ���Ϊkey������
    //	private String routeTargetColumn; //·�ɱ���Ϊvalue������
    //	private String routeReturnType; //#��������(int,long,String)

    private String parameter;
    private String expression;
    private String mappingRuleBeanId;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMappingRuleBeanId() {
        return mappingRuleBeanId;
    }

    public void setMappingRuleBeanId(String mappingRuleBeanId) {
        this.mappingRuleBeanId = mappingRuleBeanId;
    }
}
