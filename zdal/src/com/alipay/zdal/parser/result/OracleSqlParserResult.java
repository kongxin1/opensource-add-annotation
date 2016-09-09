/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.result;

import java.util.List;
import java.util.Set;

import com.alipay.zdal.parser.exceptions.SqlParserException;
import com.alipay.zdal.parser.sql.ast.expr.SQLBinaryOperator;
import com.alipay.zdal.parser.visitor.BindVarCondition;
import com.alipay.zdal.parser.visitor.ZdalOracleSchemaStatVisitor;
import com.alipay.zdal.parser.visitor.ZdalSchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: OracleSqlParserResult.java, v 0.1 2012-5-21 ����03:12:36 xiaoqing.zhouxq Exp $
 */
public class OracleSqlParserResult extends DefaultSqlParserResult {

    public OracleSqlParserResult(ZdalSchemaStatVisitor visitor) {
        super(visitor);
    }

    /**
     * �ж�max�Ƿ��ǰ󶨱���.
     * @return
     */
    public int isRowCountBind() {
        ZdalOracleSchemaStatVisitor mysqlVisitor = (ZdalOracleSchemaStatVisitor) visitor;
        Set<BindVarCondition> rowNums = mysqlVisitor.getRownums();
        if (rowNums == null || rowNums.isEmpty()) {
            return -1;
        }
        for (BindVarCondition rowNum : rowNums) {
            if (ZdalSchemaStatVisitor.ROWCOUNT.equalsIgnoreCase(rowNum.getColumnName())) {
                if (rowNum.getValue() == null) {
                    return rowNum.getIndex();
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public int getMax(List<Object> arguments) {
        ZdalOracleSchemaStatVisitor oracleVisitor = (ZdalOracleSchemaStatVisitor) visitor;
        Set<BindVarCondition> rowNums = oracleVisitor.getRownums();
        if (rowNums == null || rowNums.isEmpty()) {
            return DEFAULT_SKIP_MAX;
        }
        int result = DEFAULT_SKIP_MAX;
        //���һ��sql����а�������rownum���������ܻ�������.
        for (BindVarCondition rowNum : rowNums) {
            if (ZdalSchemaStatVisitor.ROWCOUNT.equalsIgnoreCase(rowNum.getColumnName())) {
                //����ǰ󶨲������ʹӲ����б��л�ȡrowcount��ֵ.
                if (rowNum.getValue() == null) {
                    Object obj = arguments.get(rowNum.getIndex());
                    if (obj instanceof Long) {
                        throw new SqlParserException("ERROR ## row selecter can't handle long data");
                    } else if (obj instanceof Integer) {
                        int tmp = ((Integer) obj).intValue();
                        if (rowNum.getOperator().equals(SQLBinaryOperator.LessThan.name)) {//�����С�ڣ�����Ҫ��һ
                            tmp = tmp - 1;
                        }
                        if (tmp > result) {
                            result = tmp;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## bind rowcount var has an error , "
                                                     + obj + " is not a int value");
                    }
                } else {//��sql����л�ȡrowcount��ֵ.
                    Comparable<?> tmp = rowNum.getValue();
                    if (tmp instanceof Number) {
                        int rowcount = ((Integer) tmp).intValue();
                        if (rowNum.getOperator().equals(SQLBinaryOperator.LessThan.name)) {//�����С�ڣ�����Ҫ��һ
                            rowcount = rowcount - 1;
                        }
                        if (rowcount > result) {
                            result = rowcount;
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

    public int isSkipBind() {
        ZdalOracleSchemaStatVisitor oracleVisitor = (ZdalOracleSchemaStatVisitor) visitor;
        Set<BindVarCondition> rowNums = oracleVisitor.getRownums();
        if (rowNums == null || rowNums.isEmpty()) {
            return -1;
        }
        for (BindVarCondition rowNum : rowNums) {
            if (ZdalSchemaStatVisitor.OFFSET.equalsIgnoreCase(rowNum.getColumnName())) {
                if (rowNum.getValue() == null) {
                    return rowNum.getIndex();
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public int getSkip(List<Object> arguments) {
        ZdalOracleSchemaStatVisitor oracleVisitor = (ZdalOracleSchemaStatVisitor) visitor;
        Set<BindVarCondition> rowNums = oracleVisitor.getRownums();
        if (rowNums == null || rowNums.isEmpty()) {
            return DEFAULT_SKIP_MAX;
        }
        int result = DEFAULT_SKIP_MAX;
        //���һ��sql����а�������offset���������ܻ�������.
        for (BindVarCondition rowNum : rowNums) {
            if (ZdalSchemaStatVisitor.OFFSET.equals(rowNum.getColumnName())) {
                //����ǰ󶨲������ʹӲ����б��л�ȡoffset��ֵ.
                if (rowNum.getValue() == null) {
                    Object obj = arguments.get(rowNum.getIndex());
                    if (obj instanceof Long) {
                        throw new SqlParserException("ERROR ## row selecter can't handle long data");
                    } else if (obj instanceof Integer) {
                        int tmp = ((Integer) obj).intValue();
                        if (rowNum.getOperator().equals(SQLBinaryOperator.GreaterThanOrEqual.name)) {//����Ǵ��ڵ��ڣ�����Ҫ��һ
                            tmp = tmp - 1;
                        }
                        if (tmp > result) {
                            result = tmp;
                        }
                    } else {
                        throw new SqlParserException("ERROR ## bind offset var has an error , "
                                                     + obj + " is not a int value");
                    }
                } else {//��sql����л�ȡrowcount��ֵ.
                    Comparable<?> tmp = rowNum.getValue();
                    if (tmp instanceof Number) {
                        int offset = ((Integer) tmp).intValue();
                        if (rowNum.getOperator().equals(SQLBinaryOperator.GreaterThanOrEqual.name)) {//����Ǵ��ڵ��ڣ�����Ҫ��һ
                            offset = offset - 1;
                        }
                        if (offset > result) {
                            result = offset;
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
}
