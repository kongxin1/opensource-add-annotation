/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

/**
 * ר�����ڱ�ʶ��Ҫ�ڽ��ֵ����Ϊ�յ�����£���TStatement����getExecutionContextֱ�Ӻ���
 * δ�ҵ�������ؿս������һ����־���쳣��
 * 
 * ������InterruptedException�е����ơ�
 * 
 *
 */
public class EmptySetRuntimeException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6084485716630722062L;

    public EmptySetRuntimeException() {
        super();
    }
}
