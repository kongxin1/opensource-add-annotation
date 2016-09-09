/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.cartesianproductcalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ��ѿ�����
 * 
 * 
 */
public class CartesianProductCalculator implements Parent, Iterable<SamplingField>,
                                       Iterator<SamplingField> {
    /**
     * samplingField���ڴ����Ǳ����õģ���Ϊ������ֻ������ֵ�Ĵ��ݵģ��������ڵ��߳����汻ʹ�á�
     * ���ĸ�class CartesianProductCalculator��������ÿ�μ���ѿ�������ʱ�򶼻��½��ģ�����������
     * �ǲ���Ҫ��ע�̰߳�ȫ��
     * 
     */
    final SamplingField                                    samplingFieldToBeReturned;
    private boolean                                        hasNext      = true;
    final List<CartesianProductCalculatorElement>          list;
    private boolean                                        firstNext    = true;
    private static final CartesianProductCalculatorElement DEFAULT_CPCE = new CartesianProductCalculatorElement(
                                                                            null, Collections
                                                                                .emptySet());
    /**
     * ���ջ��������ѿ����������ұߵ�һ�У�Ҳ���������������
     */
    private CartesianProductCalculatorElement              firstCartesianProductCalculatorElement;

    public CartesianProductCalculator(Map<String, Set<Object>> enumeratedMap) {
        List<String> columnList = new ArrayList<String>(enumeratedMap.size());
        columnList.addAll(enumeratedMap.keySet());

        samplingFieldToBeReturned = new SamplingField(columnList, enumeratedMap.size());

        List<Set<Object>> enumeratedValuesSetOrderByColumnList = new ArrayList<Set<Object>>(
            columnList.size());
        for (String key : columnList) {
            enumeratedValuesSetOrderByColumnList.add(enumeratedMap.get(key));
        }
        list = new ArrayList<CartesianProductCalculatorElement>();
        CartesianProductCalculatorElement parentProductor = null;
        CartesianProductCalculatorElement childrenProductor = null;
        boolean isFirst = true;
        //TODO:���columnListΪ��
        if (!enumeratedValuesSetOrderByColumnList.isEmpty()) {
            for (Set<Object> set : enumeratedValuesSetOrderByColumnList) {
                //parent
                parentProductor = new CartesianProductCalculatorElement(null, set);
                if (isFirst) {
                    firstCartesianProductCalculatorElement = parentProductor;
                    isFirst = false;
                }
                if (childrenProductor != null) {
                    //children
                    childrenProductor.setParent(parentProductor);
                }
                //children become parent
                childrenProductor = parentProductor;
                list.add(childrenProductor);
            }
            childrenProductor.setParent(this);
        } else {
            firstCartesianProductCalculatorElement = DEFAULT_CPCE;
            firstCartesianProductCalculatorElement.setParent(this);
        }

    }

    public boolean hasNext() {
        return firstCartesianProductCalculatorElement.hasNext() && hasNext;
    }

    public SamplingField next() {
        if (firstNext) {
            // ��һ�γ�ʼ����ʱ��Ҫ���еĵѿ�����Ԫ�ض���һ��next�����Գ�ʼ������
            for (CartesianProductCalculatorElement element : list) {
                // TODO:������������ж��޷�next������µ���Ϊ
                if (element.hasNext()) {
                    element.init();
                }
            }
            firstNext = false;
        } else {
            if (firstCartesianProductCalculatorElement.hasNext()) {
                firstCartesianProductCalculatorElement.next();
            }
        }
        samplingFieldToBeReturned.clear();
        int index = 0;
        for (CartesianProductCalculatorElement element : list) {

            samplingFieldToBeReturned.add(index, element.currentObject);
            index++;
        }
        return samplingFieldToBeReturned;

    }

    public boolean parentHasNext() {
        //���ݵ�ͷ��ʱ���ʾû����һ����
        return false;
    }

    public void add() {
        throw new IllegalStateException("should not be here");
    }

    public void remove() {
        throw new IllegalStateException("should not be here");

    }

    public Iterator<SamplingField> iterator() {
        return this;
    }

}
