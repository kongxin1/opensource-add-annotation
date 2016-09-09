/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.Map;
import java.util.Set;

import com.alipay.zdal.common.sqljep.function.Comparative;

public abstract class ListAbstractResultRule extends AbstractRule {
    /**
     * �����ֿ�
     * @param sharedValueElementMap
     * @return ���ص�map����Ϊnull,���п���Ϊ�յ�map�����map��Ϊ�գ����ڲ�����map�ض���Ϊ�ա����ٻ���һ��ֵ
     */
    public abstract Map<String/*column*/, Field> eval(
                                                       Map<String, Comparative> sharedValueElementMap);

    /**
     * �����ֱ������жԼ������ǰֵ�ĺ�����Դ��׷����Ϣ
     * 
     * @param enumeratedMap ����->ö�� ��Ӧ��
     * @param mappingTargetColumn ӳ�������
     * @param mappingKeys ӳ�����ֵ
     * 
     * @return ������ֶΣ�����Ϊ�� ������෽����������setΪ��ʱ���쳣������Զ��׳�
     */
    public abstract Set<String> evalWithoutSourceTrace(Map<String, Set<Object>> enumeratedMap,
                                                       String mappingTargetColumn,
                                                       Set<Object> mappingKeys);

}
