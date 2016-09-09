/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.rule.bean.AdvancedParameter;

/**
 * ������ܳ��� �����ɲ����ͱ��ʽ���
 *
 */
public abstract class AbstractRule {
    private static final Logger      log    = Logger.getLogger(AbstractRule.class);
    /**
     * ��ǰ������Ҫ�õ��Ĳ���
     */
    protected Set<AdvancedParameter> parameters;

    private boolean                  inited = false;

    /**
     * ��ǰ������Ҫ�õ��ı��ʽ
     */
    protected String                 expression;

    /*
     * ͨ���������ṹ�������ܼ�����Ӵ�������set�������У���Ҫע�����
     * ����ж��ֵ������һ������ʽ�����ַ����ǲ�׼ȷ�ģ���ʱ�����ͨ�������ļ�
     * �ֶ��������������ÿһ����������Ĳ����ĵ��Ӵ�����
     * 
     * ���ڻ�û���ã���Ϊ�Ƚϸ���
     * 
     * @param cumulativeTimes
     
    public void setCumulativeTimes(int cumulativeTimes){
    	for(KeyAndAtomIncValue keyAndAtomIncValue :parameters){
    		if(keyAndAtomIncValue.cumulativeTimes == null){
    			keyAndAtomIncValue.cumulativeTimes = cumulativeTimes;
    		}
    	}
    }
    */
    protected abstract void initInternal();

    /**
     * ȷ������ֻ��ʼ��һ��
     */
    public void initRule() {
        if (inited) {
            if (log.isDebugEnabled()) {
                log.debug("rule has inited");
            }
        } else {
            initInternal();
            inited = true;
        }
    }

    public Set<AdvancedParameter> getParameters() {
        return parameters;
    }

    /**
     * springע�����Ĭ�������ֶε�ֵ,�Ὣ����ֵ��ΪСд
     * 
     * @param parameters
     */
    public void setParameters(Set<String> parameters) {
        if (this.parameters == null) {
            this.parameters = new HashSet<AdvancedParameter>();
        }
        for (String str : parameters) {
            AdvancedParameter advancedParameter = getAdvancedParamByParamToken(str);
            this.parameters.add(advancedParameter);
        }
    }

    /**
     * Springע����
     * @param parameters
     */
    public void setAdvancedParameter(Set<AdvancedParameter> parameters) {
        if (this.parameters == null) {
            this.parameters = new HashSet<AdvancedParameter>();
        }
        for (AdvancedParameter keyAndAtomIncValue : parameters) {
            this.parameters.add(keyAndAtomIncValue);
        }

    }

    /**
     * springע��һ��
     * @param parameter
     */
    public void setAdvancedParameter(AdvancedParameter parameter) {
        if (this.parameters == null) {
            this.parameters = new HashSet<AdvancedParameter>();
        }
        this.parameters.add(parameter);
    }

    public String getExpression() {
        return expression;
    }

    /**
     * ����
     * col,1,7�������ֶ�
     * col,1_date,7
     * col = ��Ҫ�ֿ�ֱ������
     * 1����ʾԭ����������
     * 7 ��ʾy�ı仯��Χ
     * �����뿴����ĵ�
     * 
     * @param paramToken
     * @return
     */
    protected AdvancedParameter getAdvancedParamByParamToken(String paramToken) {
        AdvancedParameter param = new AdvancedParameter();
        String[] paramTokens = paramToken.split(",");

        int tokenLength = paramTokens.length;
        switch (tokenLength) {
            case 1:
                param.key = paramTokens[0];
                break;
            case 3:
                param.key = paramTokens[0];
                try {
                    /*
                     * ����tokens��ȡ��������
                     * ��Ҫ�����࣬��һ��������_date...
                     * �ڶ����� ֱ��Ϊ���ֵġ� 
                     */
                    Comparable<?> atomicIncreateValue = getIncreatementValueByString(paramTokens);
                    param.atomicIncreateValue = atomicIncreateValue;
                    param.cumulativeTimes = Integer.valueOf(paramTokens[2]);

                    if (param.atomicIncreateValue != null) {
                        param.needMergeValueInCloseInterval = true;
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("����Ĳ�����ΪInteger����,����Ϊ:" + paramToken, e);
                }
                break;
            default:
                throw new IllegalArgumentException("����Ĳ�������������Ϊ1������3����3����ʱ��Ϊ����ʹ��" + "ö��ʱ������");
        }
        return param;
    }

    /**
     * ���� 1_date������_month������_year ���Լ������ֵļ��������
     * �������ǿ�
     * @param paramTokens
     * @return
     */
    private Comparable<?> getIncreatementValueByString(String[] paramTokens) {
        Comparable<?> atomicIncreateValue = null;
        String atomicIncreateValueField = paramTokens[1];
        String[] fields = StringUtil.split(atomicIncreateValueField, "_");
        int length = fields.length;
        switch (length) {
            //����_��������
            case 2:
                int calendarFieldType = 0;
                String fieldString = StringUtil.trim(fields[1]);

                if (StringUtil.equalsIgnoreCase("date", fieldString)) {
                    calendarFieldType = Calendar.DATE;
                } else if (StringUtil.equalsIgnoreCase("month", fieldString)) {
                    calendarFieldType = Calendar.MONTH;
                } else if (StringUtil.equalsIgnoreCase("YEAR", fieldString)) {
                    calendarFieldType = Calendar.YEAR;
                }
                DateEnumerationParameter dateEP = new DateEnumerationParameter(Integer
                    .valueOf(fields[0]), calendarFieldType);
                atomicIncreateValue = dateEP;
                break;

            default:
                //Ĭ�������ֱ��valueOf,�ߵ�·������ǰһ�������׳�NumberformatExceptionʱ ����쳣��ȥ��
                atomicIncreateValue = Integer.valueOf(paramTokens[1]);
                break;
        }
        return atomicIncreateValue;
    }

    public void setExpression(String expression) {
        if (expression != null)
            this.expression = expression;
    }

    /**
     * col,1,7|col1,1,7....
     * @param parameterArray
     */
    public void setParameter(String parameterArray) {
        if (parameterArray != null && parameterArray.length() != 0) {
            String[] paramArray = parameterArray.split("\\|");
            Set<String> paramSet = new HashSet<String>(Arrays.asList(paramArray));
            this.setParameters(paramSet);
        }
    }

    public void setContext(Map<String, Object> context) {
    }

}
