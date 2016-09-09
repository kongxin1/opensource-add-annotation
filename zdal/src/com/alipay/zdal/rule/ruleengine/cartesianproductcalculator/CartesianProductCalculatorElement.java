/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.cartesianproductcalculator;

import java.util.Iterator;
import java.util.Set;

public class CartesianProductCalculatorElement implements Parent {
    Parent            parent;
    final Set<Object> elementOfCartesianProductCalculation;
    Iterator<Object>  iteratorOfCartesianProductCalculation;
    Object            currentObject;

    public CartesianProductCalculatorElement(Parent listener,
                                             Set<Object> elementOfCartesianProductCalculation) {
        this.parent = listener;
        this.elementOfCartesianProductCalculation = elementOfCartesianProductCalculation;
    }

    public Object getCurrentObject() {
        return currentObject;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Parent getParent() {
        return parent;
    }

    public Set<Object> getElementOfCartesianProductCalculation() {
        return elementOfCartesianProductCalculation;
    }

    public boolean hasNext() {
        if (elementOfCartesianProductCalculation == null) {
            return parent.parentHasNext();
        }
        //�����һ�ν���element��hasNext���������ʼ��һ��
        if (iteratorOfCartesianProductCalculation == null) {
            iteratorOfCartesianProductCalculation = elementOfCartesianProductCalculation.iterator();
        }

        if (iteratorOfCartesianProductCalculation.hasNext()) {
            //��ǰ�ڵ㻹��nextֵ�Ļ���ֱ�ӻش���ֵ
            return true;
        } else {
            //û��nextֵ�Ļ���ѯ��parent�Ƿ���nextֵ��
            return parent.parentHasNext();
        }
    }

    /**
     * ��ʼ�����߼���next�߼�������ͬ��������Ҫ֪ͨparent��λ
     */
    public void init() {
        if (elementOfCartesianProductCalculation == null) {
            currentObject = null;
            return;
        }
        //�����ǰ��list����nextֵ����ʹ�õ�ǰlist��nextֵ
        if (iteratorOfCartesianProductCalculation.hasNext()) {
            currentObject = iteratorOfCartesianProductCalculation.next();
        } else {
            //����֪ͨparentҪ��λ�ˣ�Ȼ���Լ��������á�
            iteratorOfCartesianProductCalculation = elementOfCartesianProductCalculation.iterator();
            if (iteratorOfCartesianProductCalculation.hasNext()) {
                currentObject = iteratorOfCartesianProductCalculation.next();
            } else {
                currentObject = null;
            }
        }
    }

    public Object next() {
        if (iteratorOfCartesianProductCalculation == null) {
            parent.add();
            return currentObject;
        }
        //�����ǰ��list����nextֵ����ʹ�õ�ǰlist��nextֵ
        if (iteratorOfCartesianProductCalculation.hasNext()) {
            currentObject = iteratorOfCartesianProductCalculation.next();
        } else {
            //����֪ͨparentҪ��λ�ˣ�Ȼ���Լ��������á�
            iteratorOfCartesianProductCalculation = elementOfCartesianProductCalculation.iterator();
            parent.add();
            if (iteratorOfCartesianProductCalculation.hasNext()) {
                currentObject = iteratorOfCartesianProductCalculation.next();
            } else {
                currentObject = null;
            }
        }
        return currentObject;
    }

    public void add() {
        next();
    }

    public void remove() {
        throw new IllegalStateException("shold not be here");
    }

    public boolean parentHasNext() {
        return hasNext();
    }

    @Override
    public String toString() {
        return "current Obj:" + currentObject + "elementOfCartesianProductCalculation:"
               + elementOfCartesianProductCalculation;
    }

}
