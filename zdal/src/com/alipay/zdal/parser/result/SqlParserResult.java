/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.result;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.parser.sqlobjecttree.ComparativeMapChoicer;
import com.alipay.zdal.parser.visitor.OrderByEle;
/**
 * sql-parser������Ľ��.
 * @author xiaoqing.zhouxq
 * @version $Id: SqlParserResult.java, v 0.1 2012-5-21 ����03:29:18 xiaoqing.zhouxq Exp $
 */
public interface SqlParserResult {

    /**
     * ��ȡ��ǰ����,���sql�а������ű�Ĭ��ֻ���ص�һ�ű�.
     * @return
     */
    String getTableName();

    /**
     * ��ȡorder by ����Ϣ
     * @return
     */
    List<OrderByEle> getOrderByEles();

    /**
     * ��ȡgroup by ��Ϣ
     * @return
     */
    List<OrderByEle> getGroupByEles();

    /**
     * insert/update/delete/select.
     * @return
     */
    boolean isDML();

    /**
     * ��ȡsql��SKIPֵ����еĻ���û�е�����»᷵��DEFAULTֵ
     * @param arguments ����ֵ�б�.
     * @return
     */
    int getSkip(List<Object> arguments);

    /**
     * ����skip�󶨱������±�,���û�оͷ���-1.
     * @return
     */
    int isSkipBind();

    /**
     * ��ȡsql��maxֵ����еĻ���û�еĻ��᷵��DEFAULTֵ
     * @param arguments ����ֵ�б�.
     * @return
     */
    int getMax(List<Object> arguments);

    /**
     * ����rowCount�󶨱������±�,���û�оͷ���-1.
     * @return
     */
    int isRowCountBind();

    /**
     * ����ǰsql��������group function.������ҽ���һ��group function,��ôʹ�ø�function
     * ���û��group function�����ж��group function.�򷵻�NORMAL
     * 
     * @return
     */
    GroupFunctionType getGroupFuncType();

    /**
     * ��������Ľӿ�
     * @param tables
     * @param args
     * @param skip
     * @param max
     * @param outputType
     * @param modifiedMap
     * @return
     */
    void getSqlReadyToRun(Set<String> tables, List<Object> args, Number skip,
                                               Number max, 
                                               Map<Integer, Object> modifiedMap);
    


    /**
     * ��ȡ�����ɸѡ��
    * @return
    */
    ComparativeMapChoicer getComparativeMapChoicer();
    
    
}
