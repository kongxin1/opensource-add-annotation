/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.groovy;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.alipay.zdal.rule.bean.AdvancedParameter;
import com.alipay.zdal.rule.ruleengine.cartesianproductcalculator.SamplingField;
import com.alipay.zdal.rule.ruleengine.exception.ZdalRuleCalculateException;
import com.alipay.zdal.rule.ruleengine.rule.CartesianProductBasedListResultRule;
import com.alipay.zdal.rule.ruleengine.rule.ResultAndMappingKey;

public class GroovyListRuleEngine extends CartesianProductBasedListResultRule {
    private static final Logger logger               = Logger.getLogger(GroovyListRuleEngine.class);
    private Object              ruleObj;
    private Method              m_routingRuleMap;
    private static final String IMPORT_STATIC_METHOD = "import static com.alipay.zdal.rule.groovy.staticmethod.GroovyStaticMethod.*;";

    protected void initInternal() {
        if (expression == null) {
            throw new IllegalArgumentException("δָ�� expression");
        }
        GroovyClassLoader loader = AccessController
            .doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                public GroovyClassLoader run() {
                    return new GroovyClassLoader(GroovyListRuleEngine.class.getClassLoader());
                }
            });
        String groovyRule = getGroovyRule(expression);
        Class<?> c_groovy = loader.parseClass(groovyRule);

        try {
            // �½���ʵ��
            ruleObj = c_groovy.newInstance();
            // ��ȡ����
            m_routingRuleMap = getMethod(c_groovy, "eval", Map.class);
            if (m_routingRuleMap == null) {
                throw new IllegalArgumentException("���򷽷�û�ҵ�");
            }
            m_routingRuleMap.setAccessible(true);

        } catch (Throwable t) {
            throw new IllegalArgumentException("ʵ�����������ʧ��", t);
        }
    }

    private static final Pattern RETURN_WHOLE_WORD_PATTERN = Pattern.compile("\\breturn\\b",
                                                               Pattern.CASE_INSENSITIVE); // ȫ��ƥ��
    private static final Pattern DOLLER_PATTERN            = Pattern.compile("#.*?#");

    // Integer.valueOf(#userIdStr#.substring(0,1),16).intdiv(8)
    protected String getGroovyRule(String expression) {
        StringBuffer sb = new StringBuffer();
        sb.append(IMPORT_STATIC_METHOD);
        Set<AdvancedParameter> params = new HashSet<AdvancedParameter>();
        Matcher matcher = DOLLER_PATTERN.matcher(expression);
        sb.append("public class RULE ").append("{");
        sb.append("public Object eval(Map map){");
        // StringBuffer sb = new StringBuffer();
        // �滻����װadvancedParameter
        int start = 0;

        Matcher returnMarcher = RETURN_WHOLE_WORD_PATTERN.matcher(expression);
        if (!returnMarcher.find()) {
            sb.append("return ");
        }

        while (matcher.find(start)) {
            String realParam = matcher.group();
            realParam = realParam.substring(1, realParam.length() - 1);
            AdvancedParameter advancedParameter = getAdvancedParamByParamToken(realParam);
            params.add(advancedParameter);
            sb.append(expression.substring(start, matcher.start()));
            sb.append("(map.get(\"");
            // �滻��(map.get("key"));
            sb.append(advancedParameter.key);
            sb.append("\"))");

            start = matcher.end();
        }
        // ������Ҫ�õ��Ĳ���
        setAdvancedParameter(params);
        sb.append(expression.substring(start));
        sb.append(";");
        sb.append("}");
        sb.append("}");
        if (logger.isDebugEnabled()) {
            logger.debug(sb.toString());
        }
        return sb.toString();
    }

    public ResultAndMappingKey evalueateSamplingField(SamplingField samplingField) {
        List<String> columns = samplingField.getColumns();
        List<Object> values = samplingField.getEnumFields();

        int size = columns.size();
        Map<String, Object> argumentMap = new HashMap<String, Object>(size);
        for (int i = 0; i < size; i++) {
            argumentMap.put(columns.get(i), values.get(i));
        }
        // ����Ӧ���Զ����ֶ�
        if (GroovyContextHelper.getContext() != null) {
            for (Map.Entry<String, Object> entry : GroovyContextHelper.getContext().entrySet()) {
                argumentMap.put(entry.getKey(), entry.getValue());
            }
        }
        // ����Ӧ��threadLocal�Զ����ֶ�
        if (GroovyThreadLocalContext.getContext() != null) {
            for (Map.Entry<String, Object> entry : GroovyThreadLocalContext.getContext().entrySet()) {
                argumentMap.put(entry.getKey(), entry.getValue());
            }
        }

        Object[] args = new Object[] { argumentMap };
        try {
            String result = imvokeMethod(args);
            if (result != null) {
                return new ResultAndMappingKey(result);
            } else {
                throw new IllegalArgumentException("��������Ľ������Ϊnull");
            }
        } catch (Exception e) {
            throw new ZdalRuleCalculateException("��������������,���ֵ=" + argumentMap, e);
        }
    }

    /**
     * ����Ŀ�귽��
     * 
     * @param args
     * @return
     */
    public String imvokeMethod(Object[] args) {
        Object value = invoke(ruleObj, m_routingRuleMap, args);
        String retString = null;
        if (value == null) {
            return null;
        } else {
            retString = String.valueOf(value);
            return retString;
        }
    }

    private static Method getMethod(Class<?> c, String name, Class<?>... parameterTypes) {
        try {
            return c.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("ʵ�����������ʧ��", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("û���������" + name, e);
        }
    }

    private static Object invoke(Object obj, Method m, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (Throwable t) {
            // logger.warn("���÷�����" + m + "ʧ��", t);
            // return null;
            throw new IllegalArgumentException("���÷���ʧ��: " + m, t);
        }
    }

    @Override
    public String toString() {
        return "GroovyListRuleEngine [expression=" + expression + ", parameters=" + parameters
               + "]";
    }

}
