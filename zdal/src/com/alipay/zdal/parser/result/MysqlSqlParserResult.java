/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.result;

import java.util.List;
import java.util.Set;

import com.alipay.zdal.parser.exceptions.SqlParserException;
import com.alipay.zdal.parser.visitor.BindVarCondition;
import com.alipay.zdal.parser.visitor.ZdalMySqlSchemaStatVisitor;
import com.alipay.zdal.parser.visitor.ZdalSchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: MysqlSqlParserResult.java, v 0.1 2012-5-21 ����03:12:20 xiaoqing.zhouxq Exp $
 */
public class MysqlSqlParserResult extends DefaultSqlParserResult {

    public MysqlSqlParserResult(ZdalSchemaStatVisitor visitor) {
        super(visitor);
    }

    /**
     * �ж�max�Ƿ��ǰ󶨱���.
     * @return
     */
    public int isRowCountBind() {
        ZdalMySqlSchemaStatVisitor mysqlVisitor = (ZdalMySqlSchemaStatVisitor) visitor;
        Set<BindVarCondition> limits = mysqlVisitor.getLimits();
        if (limits == null || limits.isEmpty()) {
            return -1;
        }
        for (BindVarCondition limit : limits) {
            if (ZdalSchemaStatVisitor.ROWCOUNT.equals(limit.getColumnName())) {
                if (limit.getValue() == null) {
                    return limit.getIndex();
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public int getMax(List<Object> arguments) {
        int skip = getSkip(arguments);
        if (skip != DEFAULT_SKIP_MAX && skip < 0) {
            throw new SqlParserException("ERROR ## the skip is less than 0");
        }

        int rowCount = getRowCount(arguments);
        if (rowCount != DEFAULT_SKIP_MAX && rowCount < 0) {
            throw new SqlParserException("ERROR ## the rowcount is less than 0");
        }
        if (skip == DEFAULT_SKIP_MAX) {
            if (rowCount == DEFAULT_SKIP_MAX) {
                //���skip��rowcount�������ھͷ���Ĭ��ֵ.
                return DEFAULT_SKIP_MAX;
            } else {
                //���skip�����ڣ��ͷ���rowcount.
                return rowCount;
            }
        } else {
            if (rowCount == DEFAULT_SKIP_MAX) {
                return skip;
            } else {
                return skip + rowCount;
            }
        }

    }

    /**
     * �ж�skip�Ƿ��ǰ󶨱�����.
     * @return
     */
    public int isSkipBind() {
        ZdalMySqlSchemaStatVisitor mysqlVisitor = (ZdalMySqlSchemaStatVisitor) visitor;
        Set<BindVarCondition> limits = mysqlVisitor.getLimits();
        if (limits == null || limits.isEmpty()) {
            return -1;
        }
        for (BindVarCondition limit : limits) {
            if (ZdalSchemaStatVisitor.OFFSET.equals(limit.getColumnName())) {
                if (limit.getValue() == null) {
                    return limit.getIndex();
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public int getSkip(List<Object> arguments) {
        ZdalMySqlSchemaStatVisitor mysqlVisitor = (ZdalMySqlSchemaStatVisitor) visitor;
        Set<BindVarCondition> limits = mysqlVisitor.getLimits();
        if (limits == null || limits.isEmpty()) {
            return DEFAULT_SKIP_MAX;
        }
        int result = DEFAULT_SKIP_MAX;
        //���һ��sql����а������limit���������ܻ�������.
        for (BindVarCondition limit : limits) {
            if (ZdalSchemaStatVisitor.OFFSET.equals(limit.getColumnName())) {
                //����ǰ󶨲������ʹӲ����б��л�ȡoffset��ֵ.
                if (limit.getValue() == null) {
                    Object obj = arguments.get(limit.getIndex());
                    if (obj instanceof Long) {
                        throw new SqlParserException("ERROR ## row selecter can't handle long data");
                    } else if (obj instanceof Integer) {
                        int tmp = ((Integer) obj).intValue();
                        if (tmp > result) {
                            result = tmp;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## bind offset var has an error , "
                                                     + obj + " is not a int value");
                    }
                } else {
                    //��sql����л�ȡoffset��ֵ.
                    Comparable<?> tmp = limit.getValue();
                    if (tmp instanceof Number) {
                        int skip = ((Integer) tmp).intValue();
                        if (skip > result) {
                            result = skip;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## get offset's value has an error,"
                                                     + tmp + " is not a int value");
                    }
                }
            }
        }
        return result;
    }

    /**
     * ��ȡmysql�е�limit m,n�е�n�󶨲���ֵ.
     * @param args
     * @return
     */
    private int getRowCount(List<Object> args) {
        ZdalMySqlSchemaStatVisitor mysqlVisitor = (ZdalMySqlSchemaStatVisitor) visitor;
        Set<BindVarCondition> limits = mysqlVisitor.getLimits();
        if (limits == null || limits.isEmpty()) {
            return DEFAULT_SKIP_MAX;
        }
        int result = DEFAULT_SKIP_MAX;
        //���һ��sql����а������limit���������ܻ�������.
        for (BindVarCondition limit : limits) {
            if (ZdalSchemaStatVisitor.ROWCOUNT.equals(limit.getColumnName())) {
                //����ǰ󶨲������ʹӲ����б��л�ȡrowcount��ֵ.
                if (limit.getValue() == null) {
                    Object obj = args.get(limit.getIndex());
                    if (obj instanceof Long) {
                        throw new SqlParserException("ERROR ## row selecter can't handle long data");
                    } else if (obj instanceof Integer) {
                        int tmp = ((Integer) obj).intValue();
                        if (tmp > result) {
                            result = tmp;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## bind rowcount var has an error , "
                                                     + obj + " is not a int value");
                    }
                } else {//��sql����л�ȡrowcount��ֵ.
                    Comparable<?> tmp = limit.getValue();
                    if (tmp instanceof Number) {
                        int skip = ((Integer) tmp).intValue();
                        if (skip > result) {
                            result = skip;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## get rowcount's value has an error,"
                                                     + tmp + " is not a int value");
                    }
                }
            }
        }
        return result;
    }

}
