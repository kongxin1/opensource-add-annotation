/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.common;

/**
 * �����ⲿ��Դ.
 * 
 * @author ����
 * @version $Id: Closable.java, v 0.1 2012-11-17 ����4:57:54 Exp $
 */
public interface Closable {
    /**
     * com.alipay.zdal.client.jdbc.ZdalDataSource,
     * com.alipay.zdal.datasource.Zdatasource��Ҫ�����������ⲿ��Դ.
     * @throws Throwable
     */
    void close() throws Throwable;
}
