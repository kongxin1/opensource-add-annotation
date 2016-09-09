/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.client.dispatcher.DispatcherResult;
import com.alipay.zdal.client.dispatcher.EXECUTE_PLAN;
import com.alipay.zdal.client.dispatcher.Matcher;
import com.alipay.zdal.client.dispatcher.Result;
import com.alipay.zdal.client.dispatcher.SqlDispatcher;
import com.alipay.zdal.client.dispatcher.impl.DatabaseAndTablesDispatcherResultImp;
import com.alipay.zdal.client.util.condition.DBSelectorRouteCondition;
import com.alipay.zdal.client.util.condition.DummySqlParcerResult;
import com.alipay.zdal.client.util.condition.JoinCondition;
import com.alipay.zdal.client.util.condition.RuleRouteCondition;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.parser.SQLParser;
import com.alipay.zdal.parser.output.OutputHandlerConsist;
import com.alipay.zdal.parser.result.SqlParserResult;
import com.alipay.zdal.parser.sqlobjecttree.ComparativeMapChoicer;
import com.alipay.zdal.parser.visitor.OrderByEle;
import com.alipay.zdal.rule.LogicTableRule;
import com.alipay.zdal.rule.bean.LogicTable;
import com.alipay.zdal.rule.bean.ZdalRoot;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

/**
 * ��Ҫ����root���õ���Ҫ����Ϣ��Ȼ����matcher����ƥ�䡣
 * 
 * ��󷵻���Ҫ�Ľ��
 * 
 * @author xiaoqing.zhouxq
 * 
 */
public class SpringBasedDispatcherImpl implements SqlDispatcher {
    /**
     * ��Ҫע���sql ����������
     */
    private SQLParser                        parser  = null;

    /**
     * Zdal�ĸ��ڵ�
     */
    ZdalRoot                                 root;

    private final Matcher                    matcher = new SpringBasedRuleMatcherImpl();

    /**
     * ������������Ͷ���
     */
    public final static OutputHandlerConsist consist = new OutputHandlerConsist();

    /** 
     * @see com.alipay.zdal.client.dispatcher.SqlDispatcher#getDBAndTables(java.lang.String, java.util.List)
     */
    public DispatcherResult getDBAndTables(String sql, List<Object> args)
                                                                         throws ZdalCheckedExcption {

        DBType dbType = getDBType();
        SqlParserResult sqlParserResult = parser.parse(sql, dbType);
        // TODO: ������ң�Ҳ��������sql�����һ�б�����Ȼ��ȥ������
        // �鿴�ĸ�����match�������һ����ʾ���������
        // ����ж����ʾҪjoin��join������������ǿ���֧�ֵ�û��֧�ֵ�
        String logicTableName = sqlParserResult.getTableName();
        int index = logicTableName.indexOf(".");
        if (index >= 0) {
            logicTableName = logicTableName.substring(index + 1);
        }
        // ��root�������õ��߼���
        LogicTableRule rule = root.getLogicTableMap(logicTableName);
        if (rule == null) {
            throw new IllegalArgumentException("δ���ҵ���Ӧ����,�߼���:" + logicTableName);
        }

        boolean isAllowReverseOutput = rule.isAllowReverseOutput();
        MatcherResult matcherResult = matcher.match(sqlParserResult.getComparativeMapChoicer(),
            args, rule);
        return getDispatcherResult(matcherResult, sqlParserResult, args, dbType, rule
            .getUniqueColumns(), isAllowReverseOutput, true);

    }

    //TODO:�Ժ��Ǽ�һ��sql��״̬���ͱ�������sql��ִ�����ԣ������Ͳ������еط�����һ����
    //	private boolean validIsSingleDBandSingleTable(List<TargetDB> targetDB){
    //		
    //	}
    /**
     * ����ƥ�������������ո�TStatement�Ľ����ƴװ,��ͬ��matcher���Թ���
     * 
     * @param matcherResult
     * @return
     */
    protected DispatcherResult getDispatcherResult(MatcherResult matcherResult,
                                                   SqlParserResult sqlParserResult,
                                                   List<Object> args, DBType dbType,
                                                   List<String> uniqueColumnSet,
                                                   boolean isAllowReverseOutput, boolean isSqlParser) {
        DispatcherResultImp dispatcherResult = getTargetDatabaseMetaDataBydatabaseGroups(
            matcherResult.getCalculationResult(), sqlParserResult, args, isAllowReverseOutput);

        //��Ȼ�ж�sql����������߼�Ӧ�÷ŵ����������Ϊ����û��Ҫ�����˹����Ժ�ͷ���TargetDBList����ഫ��һ��
        //�������һ�ξͿ�����

        ControllerUtils.buildExecutePlan(dispatcherResult, matcherResult.getCalculationResult());

        validGroupByFunction(sqlParserResult, dispatcherResult);

        //TODO:reverseoutputҲ����ʹ������Ľ��
        if (isSqlParser) {
            //����sql parse���п������������
            ControllerUtils.buildReverseOutput(args, sqlParserResult, dispatcherResult.getMax(),
                dispatcherResult.getSkip(), dispatcherResult, DBType.MYSQL.equals(dbType));
        }

        return dispatcherResult;
    }

    /**
     * �����groupby��������ִ�мƻ���Ϊ���ⵥ��򵥿��ޱ���޿��ޱ�
     * ������ͨ��
     * @param sqlParserResult
     * @param dispatcherResult
     */
    protected void validGroupByFunction(SqlParserResult sqlParserResult,
                                        DispatcherResult dispatcherResult) {
        List<OrderByEle> groupByElement = sqlParserResult.getGroupByEles();
        if (groupByElement.size() != 0) {
            if (dispatcherResult.getDatabaseExecutePlan() == EXECUTE_PLAN.MULTIPLE) {
                throw new IllegalArgumentException("��������£�������ʹ��group by ����");
            }
            if (dispatcherResult.getTableExecutePlan() == EXECUTE_PLAN.MULTIPLE) {
                throw new IllegalArgumentException("��������£�������ʹ��group by����");
            }
        }
    }

    protected DispatcherResultImp getTargetDatabaseMetaDataBydatabaseGroups(
                                                                            List<TargetDB> targetDatabases,
                                                                            SqlParserResult sqlParserResult,
                                                                            List<Object> arguments,

                                                                            boolean isAllowReverseOutput) {
        // targetDatabase.set
        DispatcherResultImp dispatcherResultImp = new DispatcherResultImp(sqlParserResult
            .getTableName(), targetDatabases, isAllowReverseOutput, sqlParserResult
            .getSkip(arguments), sqlParserResult.getMax(arguments), new OrderByMessagesImp(
            sqlParserResult.getOrderByEles()), sqlParserResult.getGroupFuncType());
        return dispatcherResultImp;
    }

    @SuppressWarnings("unchecked")
    public DispatcherResult getDBAndTables(RouteCondition rc) {
        String logicTableName = rc.getVirtualTableName();
        List<String> uniqueColumns = Collections.emptyList();
        SqlParserResult sqlParserResult = null;
        if (rc instanceof RuleRouteCondition) {
            //��Ҫģ��sqlparser�߹���� condition
            sqlParserResult = ((RuleRouteCondition) rc).getSqlParserResult();
            try {
                LogicTableRule rule = root.getLogicTableMap(logicTableName);
                uniqueColumns = rule.getUniqueColumns();
                MatcherResult matcherResult = matcher.match(sqlParserResult
                    .getComparativeMapChoicer(), null, rule);

                DispatcherResult result = getDispatcherResult(matcherResult, sqlParserResult,
                    Collections.emptyList(), null, rule.getUniqueColumns(), false, false);
                //�����JoinCondition ��ôҪ��DispatcherResult�����join����������;
                if (rc instanceof JoinCondition) {
                    result
                        .setVirtualJoinTableNames(((JoinCondition) rc).getVirtualJoinTableNames());
                }

                return result;
            } catch (ZdalCheckedExcption e) {
                throw new RuntimeException(e);
            }
        } else if (rc instanceof DBSelectorRouteCondition) {
            final DBSelectorRouteCondition dBSelectorRouteCondition = (DBSelectorRouteCondition) rc;
            List<TargetDB> targetDBs = new ArrayList<TargetDB>(1);
            TargetDB targetDB = new TargetDB();
            targetDB.setDbIndex(dBSelectorRouteCondition.getDBSelectorID());
            Set<String> targetDBSet = new HashSet<String>();
            targetDBSet.addAll(dBSelectorRouteCondition.getTableList());
            targetDB.setTableNames(targetDBSet);
            targetDBs.add(targetDB);
            ComparativeMapChoicer choicer = new ComparativeMapChoicer() {

                public Map<String, Comparative> getColumnsMap(List<Object> arguments,
                                                              Set<String> partnationSet) {
                    return Collections.emptyMap();
                }
            };
            //����αװ��
            sqlParserResult = new DummySqlParcerResult(choicer, logicTableName);
            MatcherResult matcherResult = matcher.buildMatcherResult(Collections.EMPTY_MAP,
                Collections.EMPTY_MAP, targetDBs);
            return getDispatcherResult(matcherResult, sqlParserResult, Collections.emptyList(),
                null, uniqueColumns, false, false);

        } else {
            throw new IllegalArgumentException("wrong RouteCondition type:"
                                               + rc.getClass().getName());
        }
    }

    public Result getAllDatabasesAndTables(String logicTableName) {
        LogicTable logicTable = root.getLogicTable((logicTableName.toLowerCase()));
        if (logicTable == null) {
            throw new IllegalArgumentException("�߼�����δ�ҵ�");
        }
        return new DatabaseAndTablesDispatcherResultImp(logicTable.getAllTargetDBList(),
            logicTableName);
    }

    /**
     * ���߼���getter/setter
     */
    public SQLParser getParser() {
        return parser;
    }

    public void setParser(SQLParser parser) {
        this.parser = parser;
    }

    public ZdalRoot getRoot() {
        return root;
    }

    public void setRoot(ZdalRoot root) {
        this.root = root;
    }

    public DBType getDBType() {
        return root.getDBType();
    }

}
