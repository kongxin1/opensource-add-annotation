/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser;

import com.alipay.zdal.common.DBType;
import com.alipay.zdal.parser.result.SqlParserResult;

/**
 * SQL����������
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: SQLParser.java, v 0.1 2012-5-22 ����09:59:15 xiaoqing.zhouxq Exp $
 */
public interface SQLParser {

    /**
     * ����sql���,���ڲ�ͬdbType���ò�ͬ��sql������.
     * @param sql
     * @param dbType
     * @return
     */
    SqlParserResult parse(String sql, DBType dbType);
}
