/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.common.exception.runtime;

/**
 * 
 * @author ����
 * @version $Id: CantfindConfigFileByPathException.java, v 0.1 2014-1-6 ����05:18:20 Exp $
 */
public class CantfindConfigFileByPathException extends ZdalRunTimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3338684575935778495L;

    public CantfindConfigFileByPathException(String path) {
        super("�޷�����path:" + path + "�ҵ�ָ����xml�ļ�");
    }
}
