/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util.condition;

import java.util.Map;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.parser.result.SqlParserResult;

/**
 * �߹���������������ʽ
 *
 */
public interface RuleRouteCondition extends RouteCondition {
    /**
     * ������ʵ��
     * @return
     */
    public Map<String, Comparative> getParameters();

    public SqlParserResult getSqlParserResult();
}
