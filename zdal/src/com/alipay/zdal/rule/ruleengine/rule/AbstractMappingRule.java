/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alipay.zdal.rule.bean.AdvancedParameter;
import com.alipay.zdal.rule.groovy.GroovyListRuleEngine;
import com.alipay.zdal.rule.ruleengine.cartesianproductcalculator.SamplingField;

/**
 * ���󷽷������ڶԶ�mapping�ĳ���
 * ���Ƚ���ö�٣�Ȼ��ѿ��������õ������
 * ��Ҫ��ӳ��Ķ����Ժ󣬵���get��������ӳ�䡣
 * Ȼ��ӳ��Ľ������targetRule��������
 * 
 *
 */
public abstract class AbstractMappingRule extends CartesianProductBasedListResultRule {
    //	protected CartesianProductBasedListResultRule targetRule;
    private static final Logger    logger     = Logger.getLogger(AbstractMappingRule.class);
    /**
     * ת���Ժ��Ŀ�����
     */
    protected GroovyListRuleEngine targetRule = new GroovyListRuleEngine();
    /**
     * ת�����Ŀ������
     */
    private String                 targetKey  = null;

    /** 
     * ������ͨ��ӳ������������ؽ����ӳ����������testCase,��Ӧ�ڷֿ�ʱȡ��������߼���
     * @see com.alipay.zdal.rule.ruleengine.rule.CartesianProductBasedListResultRule#evalueateSamplingField(com.alipay.zdal.rule.ruleengine.cartesianproductcalculator.SamplingField)
     */
    @Override
    public ResultAndMappingKey evalueateSamplingField(SamplingField samplingField) {

        List<String> columns = samplingField.getColumns();
        List<Object> enumFields = samplingField.getEnumFields();
        if (columns != null && columns.size() == 1) {
            //ӳ���Ժ������

            Object target = null;
            if (samplingField.getMappingValue() != null
                && samplingField.getMappingTargetKey().equals(targetKey)) {
                //��ȡ��ӳ��ֵ��Ϊ�գ�����targetKey = targetKey.���ʾ�����Ѿ����ֿ�ȡ�����������ÿ��Ա��ֱ���ʹ�á�
                target = samplingField.getMappingValue();
            } else {
                target = get(targetKey, columns.get(0), enumFields.get(0));
            }
            if (target == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("target value is null");
                }
                return null;
            }
            Map<String/*target column*/, Object/*target value*/> argumentMap = new HashMap<String, Object>(
                1);

            argumentMap.put(targetKey, target);
            if (logger.isDebugEnabled()) {
                logger.debug("invoke target rule ,value is " + target);
            }
            //���������ֵ �����в�ѯ
            String resultString = targetRule.imvokeMethod(new Object[] { argumentMap });
            ResultAndMappingKey result = null;
            if (resultString != null) {
                //���ع����������Ӧ��mapping key
                result = new ResultAndMappingKey(resultString);
                result.mappingKey = target;
                result.mappingTargetColumn = targetKey;
            } else {
                //���Ϊ�����׳��쳣�����ӳ��û��ȡ��targetValue��������������顣
                throw new IllegalArgumentException("��������Ľ������Ϊnull");
            }
            return result;
        } else {
            throw new IllegalStateException("��������Ҫ��:columns:" + columns);
        }
    }

    @Override
    protected boolean ruleRequireThrowRuntimeExceptionWhenSetIsEmpty() {
        //��mapping rule�У���Ҫ��Ϊ�մ���ʱ���׳��쳣
        return true;
    }

    /**
     * ����sourceKey��sourceValue��ȡ ����targerRule��Ĳ�����targetValue
     * 
     * @param sourceKey
     * @param sourceValue
     * @return
     */
    protected abstract Object get(String targetKey, String sourceKey, Object sourceValue);

    public CartesianProductBasedListResultRule getTargetRule() {
        return targetRule;
    }

    protected void initInternal() {
        if (targetRule == null) {
            throw new IllegalArgumentException("target rule is null");
        }
        // ����Ŀ�����
        targetRule.initRule();
        // �ӽ������Ŀ��������õ���ǰ����
        Set<AdvancedParameter> advancedParameters = targetRule.getParameters();
        if (advancedParameters.size() != 1) {
            throw new IllegalArgumentException("Ŀ�����Ĳ�������Ϊ1��������ʹ��" + "ӳ�����");
        }
        // ȷ�ϲ���Ψһ�Ժ�ȡ���ò���
        AdvancedParameter advancedParameter = advancedParameters.iterator().next();
        targetKey = advancedParameter.key;
        if (targetKey == null || targetKey.length() == 0) {
            throw new IllegalArgumentException("target key is null .");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("parse mapping rule , target rule is ").append(targetRule).append(
            "target target key is ").append(targetKey);
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }
    }

    @Override
    /**
     * ������ ��ӳ���Ӧ���ߵĹ�����ɶ
     */
    public void setExpression(String expression) {
        targetRule.setExpression(expression);
    }

}
