/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.common.sqljep.function.ComparativeAND;
import com.alipay.zdal.common.sqljep.function.ComparativeOR;
import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.parser.exceptions.SqlParserException;
import com.alipay.zdal.parser.sql.parser.ParserException;
import com.alipay.zdal.parser.sql.stat.TableStat;
import com.alipay.zdal.parser.sql.stat.TableStat.Column;
import com.alipay.zdal.parser.sql.stat.TableStat.Mode;
import com.alipay.zdal.parser.sql.stat.TableStat.SELECTMODE;
import com.alipay.zdal.parser.sqlobjecttree.ComparativeMapChoicer;
import com.alipay.zdal.parser.visitor.BindVarCondition;
import com.alipay.zdal.parser.visitor.OrderByEle;
import com.alipay.zdal.parser.visitor.ZdalSchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: AbstractSqlParserResult.java, v 0.1 2012-5-21 ����03:11:27 xiaoqing.zhouxq Exp $
 */
public abstract class DefaultSqlParserResult implements SqlParserResult, ComparativeMapChoicer {

    protected ZdalSchemaStatVisitor visitor;

    /**
     * ���û��skip��max�᷵�ش�ֵ
     */
    public final static int         DEFAULT_SKIP_MAX = -1000;

    protected String                tableName        = null;

    public DefaultSqlParserResult(ZdalSchemaStatVisitor visitor) {
        if (visitor == null) {
            throw new SqlParserException("ERROR the visitor is null ");
        }
        this.visitor = visitor;
    }

    public List<OrderByEle> getGroupByEles() {
        Set<Column> columns = visitor.getGroupByColumns();
        List<OrderByEle> results = Collections.emptyList();
        for (Column column : columns) {
            OrderByEle orderByEle = new OrderByEle(column.getTable(), column.getName());
            orderByEle.setAttributes(column.getAttributes());
            results.add(orderByEle);
        }
        return results;
    }

    public GroupFunctionType getGroupFuncType() {
        if (SELECTMODE.COUNT == visitor.getSelectMode()) {
            return GroupFunctionType.COUNT;
        } else if (SELECTMODE.MAX == visitor.getSelectMode()) {
            return GroupFunctionType.MAX;
        } else if (SELECTMODE.MIN == visitor.getSelectMode()) {
            return GroupFunctionType.MIN;
        } else if (SELECTMODE.SUM == visitor.getSelectMode()) {
            return GroupFunctionType.SUM;
        }
        return GroupFunctionType.NORMAL;
    }

    public boolean isDML() {
        return (visitor.getSqlMode() == Mode.Delete) || (visitor.getSqlMode() == Mode.Insert)
               || (visitor.getSqlMode() == Mode.Select) || (visitor.getSqlMode() == Mode.Update);
    }

    public List<OrderByEle> getOrderByEles() {
        List<Column> columns = visitor.getOrderByColumns();
        List<OrderByEle> results = new ArrayList<OrderByEle>();
        for (Column column : columns) {
            OrderByEle orderByEle = new OrderByEle(column.getTable(), column.getName());
            orderByEle.setAttributes(column.getAttributes());
            results.add(orderByEle);
        }
        return results;
    }

    /**
     * ��ȡ����
     * @return
     * @see com.alipay.zdal.parser.result.SqlParserResult#getTableName()
     */
    public String getTableName() {
        if (visitor.getTables() == null || visitor.getTables().isEmpty()) {
            throw new SqlParserException("ERROR ## the tableName is null");
        }
        if (StringUtil.isBlank(tableName)) {
            for (Entry<TableStat.Name, TableStat> entry : visitor.getTables().entrySet()) {
                String temp = entry.getKey().getName();
                if (tableName == null) {
                    if (temp != null) {
                        tableName = temp.toLowerCase();
                    }
                } else {
                    if (temp != null && !tableName.equals(entry.getKey().getName().toLowerCase())) {
                        throw new IllegalArgumentException("sql����еı�����ͬ���뱣֤����sql���ı���"
                                                           + "�Լ����ǵ�schemaName��ͬ��������Ƕsql");
                    }
                }
            }
        }
        return tableName;
    }

    /**
     * ��ȡComparativeMap.
     * map��key ������ value�ǰ󶨱������{@link Comparative}
     * ����Ǹ����ɸ�ֵ�ı������򲻻᷵�ء�
     * ���ɸ�ֵָ���ǣ���Ȼ���Խ������������Ժ�Ľ�����ܽ��м��㡣
     * ��where col = concat(str,str);
     * ����SQL��Ȼ���Խ���������Ϊ��Ӧ�Ĵ�����û����ɣ������ǲ��ܸ�ֵ�ġ����������col
     * �ǲ��ᱻ�ŵ����ص�map�еġ�
     * @param arguments ����ֵ�б�.
     * @param partnationSet ����ֶ��б�.
     * @return
     */
    public Map<String, Comparative> getColumnsMap(List<Object> arguments, Set<String> partnationSet) {
        Map<String, Comparative> copiedMap = new LinkedHashMap<String, Comparative>();
        for (String partnation : partnationSet) {
            Comparative comparative = getComparativeOf(partnation, arguments);
            if (comparative != null) {
                copiedMap.put(partnation, comparative);
            }
        }
        return copiedMap;
    }

    /**
     * ���ݲ���ֶδ�sql���ֶ��л�ȡ��Ӧ����������ֵ.
     * @param partinationKey
     * @param arguments
     * @return
     */
    private Comparative getComparativeOf(String partinationKey, List<Object> arguments) {
        List<BindVarCondition> bindColumns = visitor.getBindVarConditions();

        List<BindVarCondition> conditions = new ArrayList<BindVarCondition>();
        for (BindVarCondition tmp : bindColumns) {
            if (tmp.getColumnName().equalsIgnoreCase(partinationKey)) {
                conditions.add(tmp);
            }
        }
        if (!conditions.isEmpty()) { //�ȴӰ󶨲����б��в���.
            Comparative comparative = null;
            int index = 1;
            for (BindVarCondition bindVarCondition : conditions) {
                String op = bindVarCondition.getOperator();
                int function = Comparative.getComparisonByIdent(op);
                if (function == Comparative.NotSupport || op.trim().equalsIgnoreCase("in")) {//֧�ֲ���ֶ���in��ģʽ.
                    Object arg = arguments.get(bindVarCondition.getIndex());
                    Comparable<?> value = null;
                    if (arg instanceof Comparable<?>) {
                        value = (Comparable<?>) arg;
                    } else {
                        throw new ParserException("ERROR ## can not use this type of partination");
                    }

                    if (comparative == null) {
                        comparative = new Comparative(Comparative.Equivalent, value);
                        if (index == conditions.size()) {
                            return comparative;
                        }
                    } else {
                        Comparative next = new Comparative(Comparative.Equivalent, value);
                        ComparativeOR comparativeOR = new ComparativeOR();
                        comparativeOR.addComparative(comparative);
                        comparativeOR.addComparative(next);
                        comparative = comparativeOR;
                        if (index == conditions.size()) {
                            return comparativeOR;
                        }
                    }
                    index++;
                }
            }

            index = -1;
            for (BindVarCondition condition : conditions) {
                Object arg = arguments.get(condition.getIndex());
                Comparable<?> value = null;
                if (arg instanceof Comparable<?>) {
                    value = (Comparable<?>) arg;
                } else {
                    throw new ParserException("ERROR ## can not use this type of partination");
                }
                int function = Comparative.getComparisonByIdent(condition.getOperator());

                if (comparative == null) {
                    comparative = new Comparative(function, value);
                    index = condition.getIndex();
                } else {
                    Comparative next = new Comparative(function, value);
                    if (index == condition.getIndex()) {//���Ӳ�ѯ�У����ڲ���ֶε�index��ͬ������������ͬ�Ͳ���Ҫand/or ����ƥ����.
                        return comparative;
                    }
                    if (condition.getOp() == 1) {
                        ComparativeAND comparativeAND = new ComparativeAND();
                        comparativeAND.addComparative(comparative);
                        comparativeAND.addComparative(next);
                        return comparativeAND;
                    } else if (condition.getOp() == -1) {
                        ComparativeOR comparativeOR = new ComparativeOR();
                        comparativeOR.addComparative(comparative);
                        comparativeOR.addComparative(next);
                        return comparativeOR;
                    }
                }
            }
            return comparative;
        } else {
            List<BindVarCondition> noBindConditions = visitor.getNoBindVarConditions();

            if (noBindConditions.isEmpty()) {
                return null;
            }
            List<BindVarCondition> noBinditions = new ArrayList<BindVarCondition>();
            for (BindVarCondition tmp : noBindConditions) {
                if (tmp.getColumnName().equalsIgnoreCase(partinationKey)) {
                    int function = Comparative.getComparisonByIdent(tmp.getOperator());
                    if (function == Comparative.NotSupport) {
                        if (tmp.getOperator().trim().equalsIgnoreCase("in")) {
                            noBinditions.add(tmp);
                        } else {
                            continue;
                        }
                    } else {
                        noBinditions.add(tmp);
                    }
                }
            }
            Comparative comparative = null;
            for (BindVarCondition condition : noBinditions) {
                Comparable<?> value = condition.getValue();
                if (value == null) {
                    throw new SqlParserException(
                        "ERROR ## parse from no-bind-column of this partination is error,the partination name = "
                                + partinationKey);
                }
                if (!(value instanceof Comparable<?>)) {
                    throw new ParserException("ERROR ## can not use this type of partination");
                }
                if (condition.getOperator().trim().equalsIgnoreCase("in")) {
                    if (comparative == null) {
                        comparative = new Comparative(Comparative.Equivalent, value);
                    } else {
                        Comparative next = new Comparative(Comparative.Equivalent, value);
                        ComparativeOR comparativeOR = new ComparativeOR();
                        comparativeOR.addComparative(comparative);
                        comparativeOR.addComparative(next);
                        comparative = comparativeOR;
                    }
                } else {
                    int function = Comparative.getComparisonByIdent(condition.getOperator());

                    if (comparative == null) {
                        comparative = new Comparative(function, value);
                    } else {
                        Comparative next = new Comparative(function, value);
                        if (condition.getOp() == 1) {
                            ComparativeAND comparativeAND = new ComparativeAND();
                            comparativeAND.addComparative(comparative);
                            comparativeAND.addComparative(next);
                            return comparativeAND;
                        } else if (condition.getOp() == -1) {
                            ComparativeOR comparativeOR = new ComparativeOR();
                            comparativeOR.addComparative(comparative);
                            comparativeOR.addComparative(next);
                            return comparativeOR;
                        }
                    }
                }
            }
            return comparative;
        }
    }

    /**
     * @param tables
     * @param args
     * @param skip
     *            �����䣬���Ŀ�ʼ
     * @param max
     *            �����䣬����
     * @return
     */
    public void getSqlReadyToRun(Set<String> tables, List<Object> args, Number skip, Number max,
                                 Map<Integer, Object> modifiedMap) {
        if (tables == null) {
            throw new IllegalArgumentException("���滻����Ϊ��");
        }

        //�����skip �� max �����ڣ������ǰ󶨱��������������в������滻
        if (this.isSkipBind() < 0 && this.isRowCountBind() < 0) {
            throw new IllegalArgumentException("The limit skip or rowCount set error!");
        }
        modifyParam(skip, isSkipBind(), modifiedMap);
        modifyParam(max, isRowCountBind(), modifiedMap);
    }

    protected void modifyParam(Number num, int index, Map<Integer, Object> changeParam) {

        Object obj = null;
        if (num instanceof Long) {
            obj = (Long) num;
        } else if (num instanceof Integer) {
            obj = (Integer) num;
        } else {
            throw new IllegalArgumentException("ֻ֧��int long�����");
        }
        changeParam.put(index, obj);
    }

    protected String toColumns(Set<Column> columns) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Column column : columns) {
            result.append(column);
            if (i != (columns.size() - 1)) {
                result.append(",");
            }
        }
        return result.toString();
    }

    public ComparativeMapChoicer getComparativeMapChoicer() {
        return this;
    }

}
