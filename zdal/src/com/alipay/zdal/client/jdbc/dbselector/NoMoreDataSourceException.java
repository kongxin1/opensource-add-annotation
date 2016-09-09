/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.sql.SQLException;

/**
 * ��һ������ݿⶼ�Թ������������ˣ�����û�и��������Դ�ˣ��׳��ô���
 * 
 *
 */
public class NoMoreDataSourceException extends SQLException {

    private static final long serialVersionUID = 1L;

    public NoMoreDataSourceException(String msg) {
        super(msg);
    }

    public NoMoreDataSourceException() {
        super();
    }
}
