/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.RouteCondition;
import com.alipay.zdal.client.ThreadLocalString;
import com.alipay.zdal.client.config.DataSourceConfigType;
import com.alipay.zdal.client.controller.RuleController;
import com.alipay.zdal.client.controller.TargetDBMeta;
import com.alipay.zdal.client.dispatcher.DispatcherResult;
import com.alipay.zdal.client.dispatcher.SqlDispatcher;
import com.alipay.zdal.client.jdbc.DBSelector.AbstractDataSourceTryer;
import com.alipay.zdal.client.jdbc.DBSelector.DataSourceTryer;
import com.alipay.zdal.client.jdbc.parameter.ParameterContext;
import com.alipay.zdal.client.jdbc.resultset.CountTResultSet;
import com.alipay.zdal.client.jdbc.resultset.DummyTResultSet;
import com.alipay.zdal.client.jdbc.resultset.EmptySimpleTResultSet;
import com.alipay.zdal.client.jdbc.resultset.MaxTResultSet;
import com.alipay.zdal.client.jdbc.resultset.MinTResultSet;
import com.alipay.zdal.client.jdbc.resultset.OrderByTResultSet;
import com.alipay.zdal.client.jdbc.resultset.SimpleTResultSet;
import com.alipay.zdal.client.jdbc.resultset.SumTResultSet;
import com.alipay.zdal.client.util.ExceptionUtils;
import com.alipay.zdal.client.util.ThreadLocalMap;
import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.SqlType;
import com.alipay.zdal.common.exception.checked.ZdalCheckedExcption;
import com.alipay.zdal.common.exception.sqlexceptionwrapper.ZdalCommunicationException;
import com.alipay.zdal.common.jdbc.sorter.ExceptionSorter;
import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.parser.GroupFunctionType;
import com.alipay.zdal.parser.ParserCache;
import com.alipay.zdal.parser.result.DefaultSqlParserResult;
import com.alipay.zdal.parser.visitor.OrderByEle;
import com.alipay.zdal.rule.config.beans.AppRule;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;
import com.alipay.zdal.rule.ruleengine.exception.RuleRuntimeExceptionWrapper;
import com.alipay.zdal.rule.ruleengine.exception.ZdalRuleCalculateException;
import com.alipay.zdal.rule.ruleengine.rule.EmptySetRuntimeException;

/**
 * 
 * @author ����
 * @version $Id: ZdalStatement.java, v 0.1 2014-1-6 ����01:19:26 Exp $
 */
public class ZdalStatement implements Statement {
    //TODO: ���һ��ѡ��booleanֵ������statlog���м��
    private static final Logger       log                            = Logger
                                                                         .getLogger(ZdalStatement.class);
    private static final Logger       sqlLog                         = Logger
                                                                         .getLogger(Constants.CONFIG_LOG_NAME_LOGNAME);

    /**
     * �����ж��Ƿ���һ��select ... for update��sql
     */
    private static final Pattern      SELECT_FOR_UPDATE_PATTERN      = Pattern
                                                                         .compile(
                                                                             "^select\\s+.*\\s+for\\s+update.*$",
                                                                             Pattern.CASE_INSENSITIVE);

    /**��DB2��ϵͳ���л�ȡsequence���¼���sql.  */
    private static final Pattern      SELECT_FROM_SYSTEMIBM          = Pattern
                                                                         .compile(
                                                                             "^select\\s+.*\\s+from\\s+sysibm.*$",
                                                                             Pattern.CASE_INSENSITIVE);
    private static final Pattern      SELECT_FROM_DUAL_PATTERN       = Pattern
                                                                         .compile(
                                                                             "^select\\s+.*\\s+from\\s+dual.*$",
                                                                             Pattern.CASE_INSENSITIVE);

    /**
     * Ĭ�ϵ�ÿ����ִ��sql�ĳ�ʱʱ��
     */
    public static final long          DEFAULT_TIMEOUT_FOR_EACH_TABLE = 100;

    private static final ParserCache  globalCache                    = ParserCache.instance();

    protected Map<String, DBSelector> dbSelectors;
    protected DBSelector              groupDBSelector                = null;
    protected RuleController          ruleController;
    protected final SqlDispatcher     writeDispatcher;
    protected final SqlDispatcher     readDispatcher;

    //��¼��ǰ�Ĳ�����д��������Ƕ������
    protected DB_OPERATION_TYPE       operation_type;

    public enum DB_OPERATION_TYPE {
        WRITE_INTO_DB, READ_FROM_DB;
    }

    private ZdalConnection         connectionProxy;

    protected List<Statement>      actualStatements     = new ArrayList<Statement>();
    protected ResultSet            results;
    protected boolean              moreResults;
    protected int                  updateCount;
    protected boolean              closed;
    /*
     *  �Ƿ��滻hint�е��߼�������Ĭ���ǲ��滻
     */
    private boolean                isHintReplaceSupport = false;
    /**
     * ���Դ������ⲿָ��
     */
    protected int                  retryingTimes;
    /**
     * fetchsize Ĭ��Ϊ10 
     */
    private int                    fetchSize            = 10;

    private int                    resultSetType        = -1;
    private int                    resultSetConcurrency = -1;
    private int                    resultSetHoldability = -1;

    protected boolean              autoCommit           = true;

    /**
     * �����batch������dbId
     */
    private String                 batchDataBaseId      = null;

    private boolean                readOnly;

    /**
     * ��ԭ��ResultSet�ӿ��·ŵ�Dummy������������֧���Զ��巽��
     */
    protected Set<ResultSet>       openResultSets       = new HashSet<ResultSet>();

    protected List<Object>         batchedArgs;

    private long                   timeoutForEachTable  = DEFAULT_TIMEOUT_FOR_EACH_TABLE;

    protected DataSourceConfigType dbConfigType         = null;

    private int                    autoGeneratedKeys;
    private int[]                  columnIndexes;
    private String[]               columnNames;

    /**����Դ������.  */
    protected String               appDsName            = null;

    protected static void dumpSql(String originalSql, Map<String, SqlAndTable[]> targets) {
        dumpSql(originalSql, targets, null);
    }

    public ZdalStatement(SqlDispatcher writeDispatcher, SqlDispatcher readDispatcher) {
        this.writeDispatcher = writeDispatcher;
        this.readDispatcher = readDispatcher;
    }

    protected static void dumpSql(String originalSql, Map<String, SqlAndTable[]> targets,
                                  Map<Integer, ParameterContext> parameters) {
        if (sqlLog.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("\n[original sql]:").append(originalSql.trim()).append("\n");
            for (Entry<String, SqlAndTable[]> entry : targets.entrySet()) {
                for (SqlAndTable targetSql : entry.getValue()) {
                    buffer.append(" [").append(entry.getKey()).append(".").append(targetSql.table)
                        .append("]:").append(targetSql.sql.trim()).append("\n");
                }
            }

            if (parameters != null && !parameters.isEmpty() && !parameters.values().isEmpty()) {
                buffer.append("[parameters]:").append(parameters.values().toString());
            }

            sqlLog.debug(buffer.toString());
        }
    }

    /**
     * ���SQL�������
     *
     * @param sql SQL���
     * @throws SQLException ��SQL��䲻��SELECT��INSERT��UPDATE��DELETE���ʱ���׳��쳣��
     */
    protected static SqlType getSqlType(String sql) throws SQLException {
        SqlType sqlType = globalCache.getSqlType(sql);
        if (sqlType == null) {
            String noCommentsSql = StringUtil.stripComments(sql, "'\"", "'\"", true, false, true,
                true).trim();

            if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
                if (SELECT_FROM_DUAL_PATTERN.matcher(noCommentsSql).matches()) {
                    sqlType = SqlType.SELECT_FROM_DUAL;
                } else if (SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
                    sqlType = SqlType.SELECT_FOR_UPDATE;
                } else if (SELECT_FROM_SYSTEMIBM.matcher(noCommentsSql).matches()) {
                    sqlType = SqlType.SELECT_FROM_SYSTEMIBM;
                } else {
                    sqlType = SqlType.SELECT;
                }
            } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
                sqlType = SqlType.INSERT;
            } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
                sqlType = SqlType.UPDATE;
            } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
                sqlType = SqlType.DELETE;
            } else {
                throw new SQLException("only select, insert, update, delete sql is supported");
            }
            sqlType = globalCache.setSqlTypeIfAbsent(sql, sqlType);

        }
        return sqlType;
    }

    /**
     * �滻SQL������������Ϊʵ�ʱ����� �� �滻_tableName$ �滻_tableName_ �滻tableName.
     * �滻tableName(
     * �����滻 _tableName, ,tableName, ,tableName_
     * 
     * @param originalSql
     *            SQL���
     * @param virtualName
     *            �������
     * @param actualName
     *            ʵ�ʱ���
     * @return �����滻���SQL��䡣
     */
    protected String replaceTableName(String originalSql, String virtualName, String actualName) {
        if (log.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("virtualName = ").append(virtualName).append(", ");
            buffer.append("actualName = ").append(actualName);
            log.debug(buffer.toString());
        }

        if (virtualName.equalsIgnoreCase(actualName)) {
            return originalSql;
        }
        //add by boya for testcase for schemaname.tablename to ignore replaceTablename.
        if (virtualName.contains(actualName)) {
            return originalSql;
        }

        List<String> sqlPieces = globalCache.getTableNameReplacement(originalSql);
        if (sqlPieces == null) {
            //�滻   tableName$ 
            Pattern pattern1 = Pattern.compile(new StringBuilder("\\s").append(virtualName).append(
                "$").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces1 = new ArrayList<String>();
            Matcher matcher1 = pattern1.matcher(originalSql);
            int start1 = 0;
            while (matcher1.find(start1)) {
                pieces1.add(originalSql.substring(start1, matcher1.start() + 1));
                start1 = matcher1.end();
            }
            pieces1.add(originalSql.substring(start1));
            //�滻   tableName  
            Pattern pattern2 = Pattern.compile(new StringBuilder("\\s").append(virtualName).append(
                "\\s").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces2 = new ArrayList<String>();
            for (String piece : pieces1) {
                Matcher matcher2 = pattern2.matcher(piece);
                int start2 = 0;
                while (matcher2.find(start2)) {
                    pieces2.add(piece.substring(start2 - 1 < 0 ? 0 : start2 - 1,
                        matcher2.start() + 1));
                    start2 = matcher2.end();
                }
                pieces2.add(piece.substring(start2 - 1 < 0 ? 0 : start2 - 1));
            }
            //�滻   tableName. 
            Pattern pattern3 = Pattern.compile(new StringBuilder().append(virtualName)
                .append("\\.").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces3 = new ArrayList<String>();
            for (String piece : pieces2) {
                Matcher matcher3 = pattern3.matcher(piece);
                int start3 = 0;
                while (matcher3.find(start3)) {
                    pieces3.add(piece.substring(start3 - 1 < 0 ? 0 : start3 - 1, matcher3.start()));
                    start3 = matcher3.end();
                }
                pieces3.add(piece.substring(start3 - 1 < 0 ? 0 : start3 - 1));
            }
            //�滻  tablename(
            Pattern pattern4 = Pattern.compile(new StringBuilder("\\s").append(virtualName).append(
                "\\(").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces4 = new ArrayList<String>();
            for (String piece : pieces3) {
                Matcher matcher4 = pattern4.matcher(piece);
                int start4 = 0;
                while (matcher4.find(start4)) {
                    pieces4.add(piece.substring(start4 - 1 < 0 ? 0 : start4 - 1,
                        matcher4.start() + 1));
                    start4 = matcher4.end();
                }
                pieces4.add(piece.substring(start4 - 1 < 0 ? 0 : start4 - 1));
            }

            //�滻_tableName,
            Pattern pattern5 = Pattern.compile(new StringBuilder("\\s").append(virtualName).append(
                "\\,").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces5 = new ArrayList<String>();
            for (String piece : pieces4) {
                Matcher matcher5 = pattern5.matcher(piece);
                int start5 = 0;
                while (matcher5.find(start5)) {
                    pieces5.add(piece.substring(start5 - 1 < 0 ? 0 : start5 - 1,
                        matcher5.start() + 1));
                    start5 = matcher5.end();
                }
                pieces5.add(piece.substring(start5 - 1 < 0 ? 0 : start5 - 1));
            }

            //�滻,tableName
            Pattern pattern6 = Pattern.compile(new StringBuilder("\\,").append(virtualName).append(
                "\\s").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces6 = new ArrayList<String>();
            for (String piece : pieces5) {
                Matcher matcher6 = pattern6.matcher(piece);
                int start6 = 0;
                while (matcher6.find(start6)) {
                    pieces6.add(piece.substring(start6 - 1 < 0 ? 0 : start6 - 1,
                        matcher6.start() + 1));
                    start6 = matcher6.end();
                }
                pieces6.add(piece.substring(start6 - 1 < 0 ? 0 : start6 - 1));
            }
            //�滻 ,tableName,
            Pattern pattern7 = Pattern.compile(new StringBuilder("\\,").append(virtualName).append(
                "\\,").toString(), Pattern.CASE_INSENSITIVE);
            List<String> pieces7 = new ArrayList<String>();
            for (String piece : pieces6) {
                Matcher matcher7 = pattern7.matcher(piece);
                int start7 = 0;
                while (matcher7.find(start7)) {
                    pieces7.add(piece.substring(start7 - 1 < 0 ? 0 : start7 - 1,
                        matcher7.start() + 1));
                    start7 = matcher7.end();
                }
                pieces7.add(piece.substring(start7 - 1 < 0 ? 0 : start7 - 1));
            }

            sqlPieces = globalCache.setTableNameReplacementIfAbsent(originalSql, pieces7);

        }

        // ��������SQL
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String piece : sqlPieces) {
            if (!first) {
                buffer.append(actualName);
            } else {
                first = false;
            }
            buffer.append(piece);
        }
        String sql_replace = buffer.toString();

        /*
         * added by fanzeng
         * ֧����Ĭ�ϲ��滻HINT��ı����������Ҫ�滻��������������ļ���ָ��
         * <property name="isHintReplaceSupport" value="true"/>
         * */
        if (log.isDebugEnabled()) {
            log.debug("�Ƿ�֧���滻hint���߼�������isHintSupport = " + this.isHintReplaceSupport);
        }
        //�滻  hint����ʽ���ٽ������� 
        if (isHintReplaceSupport) {
            Pattern pattern8 = Pattern.compile(new StringBuilder("/\\s?\\*\\s?.*").append(
                virtualName).append(".*\\s?\\*\\s?/").toString(), Pattern.CASE_INSENSITIVE);
            String sql_pieces[] = new String[2];
            String hint = "";
            Matcher matcher8 = pattern8.matcher(sql_replace);

            int start8 = 0;
            if (matcher8.find(start8)) {
                sql_pieces[0] = sql_replace.substring(start8 - 1 < 0 ? 0 : start8 - 1, matcher8
                    .start());
                sql_pieces[1] = sql_replace.substring(matcher8.end());
                hint = sql_replace.substring(matcher8.start(), matcher8.end()).toUpperCase();

                hint = hint.replace(virtualName.toUpperCase(), actualName.toUpperCase());

                sql_replace = sql_pieces[0] + hint + sql_pieces[1];
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("�滻�������sqlΪ��" + sql_replace);
        }

        return sql_replace;
    }

    protected SqlExecutionContext getExecutionContext(String originalSql, List<Object> parameters)
                                                                                                  throws SQLException {
        SqlExecutionContext executionContext = null;
        try {
            executionContext = getExecutionContext1(originalSql, parameters); //�¹��� 
        } catch (RuleRuntimeExceptionWrapper e) {
            //��ΪRUleRuntimeExceptionҲ�Ǹ�RuntimeException,�������ں���runtimeExceptionǰ��
            SQLException sqlException = e.getSqlException();
            if (sqlException instanceof ZdalCommunicationException) {
                //���ظ��Ľ��а�װ������쳣��zdal��ѯ�߷ֿ�ʱ���ֿ����Դ�����������ʱ���Լ����׳��ġ�ҵ����Ҫ����쳣
                throw e;
            } else {
                //���ڷ�zdal��Ϊ����������mapping rule �Ͳ����ݿ��ѯ�ĳ�����Ҫ��sqlException���а�װ���׳�
                throw new ZdalCommunicationException("rule sql exceptoin.", sqlException);
            }

        } catch (ZdalRuleCalculateException e) {
            log.error("��������������sql=" + originalSql, e);
            throw e;
        } catch (RuntimeException e) {
            String context = ExceptionUtils.getErrorContext(originalSql, parameters,
                "An error occerred on  routing or getExecutionContext,sql is :");
            //log.error(context, e);
            throw new RuntimeException(context, e);
        }
        return executionContext;
    }

    /**
     * @param dbSelectorID
     * @param retringContext
     * @throws SQLException
     */
    public void createConnectionByID(String dbSelectorID) throws SQLException {
        DBSelector dbSelector = this.dbSelectors.get(dbSelectorID);
        //			retringContext.setDbSelector(dbSelector);
        createConnection(dbSelector, dbSelectorID);
    }

    /**
     * ��ȡ�µ�Connection������Ӧ��Datasource
     * 
     * datasource��Ҫ��������������Ե�ʱ���ų��Ѿ��ҵ�������Դ
     * 
     * ���ṩ����
     * @param ds
     * @return
     * @throws SQLException
     */
    private ConnectionAndDatasource getNewConnectionAndDataSource(DataSource ds,
                                                                  DBSelector dbSelector)
                                                                                        throws SQLException {
        ConnectionAndDatasource connectionAndDatasource = new ConnectionAndDatasource();
        connectionAndDatasource.parentDataSource = ds;
        connectionAndDatasource.dbSelector = dbSelector;
        long begin = System.currentTimeMillis();
        Connection conn = ds.getConnection();
        conn.setAutoCommit(autoCommit);
        long elapsed = System.currentTimeMillis() - begin;
        if (log.isDebugEnabled()) {
            log.debug("get the connection, elapsed time=" + elapsed + ",thread="
                      + Thread.currentThread().getName());
        }

        connectionAndDatasource.connection = conn;
        return connectionAndDatasource;
    }

    protected SqlDispatcher selectSqlDispatcher(boolean autoCommit, SqlType sqlType)
                                                                                    throws SQLException {
        SqlDispatcher sqlDispatcher;
        if (sqlType != SqlType.SELECT) {
            if (this.writeDispatcher == null) {
                throw new SQLException("�ֿⲻ֧��д�롣�������û�SQL");
            }
            sqlDispatcher = this.writeDispatcher;
        } else {
            if (autoCommit) {
                String rc = (String) ThreadLocalMap.get(ThreadLocalString.SELECT_DATABASE);
                if (rc != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("rc=" + rc);
                    }
                    sqlDispatcher = this.writeDispatcher;
                } else {
                    sqlDispatcher = this.readDispatcher != null ? this.readDispatcher
                        : this.writeDispatcher;
                }
            } else {
                sqlDispatcher = this.writeDispatcher;
                if (sqlDispatcher == null) {
                    throw new SQLException("�ֿⲻ֧��д�롣�벻Ҫʹ������");
                }
            }
        }
        if (sqlDispatcher == null) {
            throw new SQLException("û�зֿ�����sqlDispatcherΪnull���������û�SQL");
        }

        if (sqlDispatcher == this.writeDispatcher) {
            this.setOperation_type(DB_OPERATION_TYPE.WRITE_INTO_DB);
        } else if (sqlDispatcher == this.readDispatcher) {
            this.setOperation_type(DB_OPERATION_TYPE.READ_FROM_DB);
        } else {
            throw new SQLException("�������ͷ����쳣������������Χ��");
        }
        return sqlDispatcher;
    }

    /**
     * 1. ֻ֧�ֵ��������, ������ͬʱĿ���Ϊ���ʱ����
     * 2. ֻ֧�ֵ��������, �������ҵ�ǰ�������Ѿ������ķֿ�ͱ��ν�����Ŀ��ⲻ��ͬһ����ʱ����
     */
    protected SqlExecutionContext getExecutionContext1(String originalSql, List<Object> parameters)
                                                                                                   throws SQLException {
        SqlExecutionContext context = new SqlExecutionContext();

        SqlType sqlType = getSqlType(originalSql);

        RouteCondition rc = (RouteCondition) ThreadLocalMap.get(ThreadLocalString.ROUTE_CONDITION);

        ThreadLocalMap.put(ThreadLocalString.ROUTE_CONDITION, null);

        DispatcherResult metaData = null;
        List<TargetDB> targets = null;

        SqlDispatcher sqlDispatcher = selectSqlDispatcher(autoCommit, sqlType);

        /*
         * �鿴sqlDispatcher�Ƿ�ΪwriteDispatcher.
         * 
         * writeDispatcher��Ҫ����insert ,update ,select for update,������select��
         * 4�������������Ҫ�����ԡ�
         * 
         * ��Ϊ����������֣���һ�����ڴ��ж���״̬��һ��������Ӧ����readDispatcher��
         * �������dispatcher����Ϊ����read��writeDispatcher��������������
         * ��selectSqlDispatcher������֤��
         * 
         * ����һ����writeDispatcherΪnull ��ʱ�� ������Ϊ�������
         * dispatcherֻ����Ϊ read��write������Ҳ���Ա�֤��ȷ�Ľ����
         */
        boolean ruleReturnNullValue = false;
        /**
         * ������autoCommit����ֵ�������ڸ�������ԴkeyȨ���������ѡ������dbʱ���ж��ǲ�����������
         * ����������У��ͽ��˴μ����ֵ����������Ȼ��������е�����sqlִ������㷨ʱ��ֱ�ӽ���ֵ����
         * �Դﵽ��һ�������н�ֹ����������п���ѡ��ͬ��db
         */
        //        ThreadLocalMap.put(ThreadLocalString.GET_AUTOCOMMIT_PROPERTY, autoCommit);
        try {
            metaData = getExecutionMetaData(originalSql, parameters, rc, sqlDispatcher);
            targets = metaData.getTarget();
        } catch (EmptySetRuntimeException e) {
            ruleReturnNullValue = true;
        }

        if (targets == null || targets.isEmpty()) {
            if (!ruleReturnNullValue) {
                throw new SQLException("�Ҳ���Ŀ��⣬��������,the originalSql = " + originalSql
                                       + " ,the parameters = " + parameters);
            } else {
                //�����mapping rule���ڼ������������null��ֱ�ӷ���emptyResultSet
                context.setRuleReturnNullValue(ruleReturnNullValue);
            }
        } else {
            buildExecutionContext(originalSql, context, sqlType, metaData, targets);
        }

        return context;
    }

    @SuppressWarnings("unchecked")
    protected final ResultSet getEmptyResultSet() {
        return new EmptySimpleTResultSet(this, Collections.EMPTY_LIST);
    }

    private void buildExecutionContext(String originalSql, SqlExecutionContext context,
                                       SqlType sqlType, DispatcherResult metaData,
                                       List<TargetDB> targets) throws SQLException {
        // ֻ֧�ֵ��������
        if (!autoCommit && targets.size() != 1 && sqlType != SqlType.SELECT) {
            throw new SQLException("ֻ֧�ֵ��������target.size=" + targets.size());
        }

        // ��������
        setTransaction(targets, originalSql);

        for (TargetDB target : targets) {

            String dbIndex = target.getDbIndex();

            Set<String> actualTables = target.getTableNames();

            if (dbIndex == null || dbIndex.length() < 1) {
                throw new SQLException("invalid dbIndex:" + dbIndex);
            }

            if (actualTables == null || actualTables.isEmpty()) {
                throw new SQLException("�Ҳ���Ŀ���");
            }

            // ��������
            //			���ﴦ���ȡ���������ӵ�����
            DBSelector dbselector = dbSelectors.get(dbIndex);
            if (dbselector == null) {
                throw new IllegalStateException("û��ΪdbIndex[" + dbIndex + "]��������Դ");
            }
            createConnection(dbselector, dbIndex); //������������Ժ�ɹ�����������ʱû���ų��������Թ���ds

            if (sqlType == SqlType.INSERT) {
                if (actualTables.size() != 1) {
                    if (actualTables.isEmpty()) {
                        throw new SQLException(
                            "Zdal need at least one table, but There is none selected ");
                    }

                    throw new SQLException("mapping many actual tables");
                }
            }

            if (!autoCommit && !dbIndex.equals(getConnectionProxy().getTxTarget())
                && sqlType != SqlType.SELECT) {
                throw new SQLException("zdalֻ֧�ֵ��������dbIndex=" + dbIndex + ",txTarget="
                                       + getConnectionProxy().getTxTarget() + ",originalSql="
                                       + originalSql);
            }

            SqlAndTable[] targetSqls = new SqlAndTable[actualTables.size()];
            if (!metaData.allowReverseOutput()) {
                int i = 0;
                for (String tab : actualTables) {
                    SqlAndTable sqlAndTable = new SqlAndTable();
                    targetSqls[i] = sqlAndTable;
                    sqlAndTable.sql = replaceTableName(originalSql, metaData.getVirtualTableName(),
                        tab);
                    //���metaData(Ҳ����DispatcherResult)������join��������ô���滻��;
                    sqlAndTable.sql = replaceJoinTableName(metaData.getVirtualTableName(), metaData
                        .getVirtualJoinTableNames(), tab, sqlAndTable.sql);
                    sqlAndTable.table = tab;
                    i++;
                }
            } else {
                int i = 0;
                for (String tab : actualTables) {
                    SqlAndTable targetSql = new SqlAndTable();
                    targetSql.sql = replaceTableName(originalSql, metaData.getVirtualTableName(),
                        tab);
                    targetSql.table = tab;
                    targetSqls[i] = targetSql;
                    i++;
                }
                // ��Ϊ����SQL�󶨲�����һ��������ֻҪȡ��һ����
                context.setChangedParameters(target.getChangedParams());
            }
            context.getTargetSqls().put(dbIndex, targetSqls);

        }

        context.setOrderByColumns(metaData.getOrderByMessages().getOrderbyList());
        context.setSkip(metaData.getSkip() == DefaultSqlParserResult.DEFAULT_SKIP_MAX ? 0
            : metaData.getSkip());
        context.setMax(metaData.getMax() == DefaultSqlParserResult.DEFAULT_SKIP_MAX ? -1 : metaData
            .getMax());
        context.setGroupFunctionType(metaData.getGroupFunctionType());
        context.setVirtualTableName(metaData.getVirtualTableName());
        //������Ҫע���
        // boolean needRetry = SqlType.SELECT.equals(sqlType) && this.autoCommit;
        boolean needRetry = this.autoCommit;
        //boolean isMySQL = sqlDispatcher.getDBType() == DBType.MYSQL?true:false;
        //RetringContext retringContext = new RetringContext(isMySQL);
        //retringContext.setNeedRetry(needRetry);
        //context.setRetringContext(retringContext);
        Map<DataSource, SQLException> failedDataSources = needRetry ? new LinkedHashMap<DataSource, SQLException>(
            0)
            : null;
        context.setFailedDataSources(failedDataSources);
    }

    /**
     * @param tab ʵ�ʱ���
     * @param vtab �������
     * @return
     */
    private String getSuffix(String tab, String vtab) {
        String result = tab.substring(vtab.length());
        return result;
    }

    /**
     * ��ȡsqlִ����Ϣ
     * @param originalSql
     * @param parameters
     * @param rc
     * @param metaData
     * @param sqlDispatcher
     * @return
     * @throws SQLException
     */
    protected DispatcherResult getExecutionMetaData(String originalSql, List<Object> parameters,
                                                    RouteCondition rc, SqlDispatcher sqlDispatcher)
                                                                                                   throws SQLException {
        DispatcherResult metaData;
        if (rc != null) {
            //���߽���SQL����ThreadLocal�����ָ������RouteCondition�����������Ŀ�ĵ�
            metaData = sqlDispatcher.getDBAndTables(rc);
        } else {
            // ͨ������SQL���ֿ�ֱ�
            try {
                metaData = sqlDispatcher.getDBAndTables(originalSql, parameters);
            } catch (ZdalCheckedExcption e) {
                throw new SQLException(e.getMessage());
            }
        }
        return metaData;
    }

    /**
     * ��������
     * @param targets
     */
    private void setTransaction(List<TargetDB> targets, String originalSql) {
        if (!autoCommit && getConnectionProxy().getTxStart()) {
            getConnectionProxy().setTxStart(false);
            //getConnectionProxy().setTxTarget(targets.get(0).getWritePool()[0]);
            getConnectionProxy().setTxTarget(targets.get(0).getDbIndex());
            if (log.isDebugEnabled()) {
                log.debug("�����������ݿ��ʶ:Set the txStart property to false, and the dbIndex="
                          + targets.get(0).getDbIndex() + ",originalSql=" + originalSql);
            }
        }
    }

    private final DataSourceTryer<ConnectionAndDatasource> createConnectionTryer = new AbstractDataSourceTryer<ConnectionAndDatasource>() {
                                                                                     public ConnectionAndDatasource tryOnDataSource(
                                                                                                                                    DataSource ds,
                                                                                                                                    String name,
                                                                                                                                    Object... args)
                                                                                                                                                   throws SQLException {
                                                                                         DBSelector dbSelector = (DBSelector) args[0];
                                                                                         dbSelector
                                                                                             .setSelectedDSName(name);
                                                                                         return getNewConnectionAndDataSource(
                                                                                             ds,
                                                                                             dbSelector);
                                                                                     }

                                                                                     @Override
                                                                                     public ConnectionAndDatasource onSQLException(
                                                                                                                                   java.util.List<SQLException> exceptions,
                                                                                                                                   ExceptionSorter exceptionSorter,
                                                                                                                                   Object... args)
                                                                                                                                                  throws SQLException {
                                                                                         int size = exceptions
                                                                                             .size();
                                                                                         if (size <= 0) {
                                                                                             throw new IllegalArgumentException(
                                                                                                 "should not be here");
                                                                                         } else {
                                                                                             //��������µĴ���
                                                                                             int lastElementIndex = size - 1;
                                                                                             //ȡ���һ��exception.�ж��Ƿ������ݿⲻ�����쳣.����ǣ�ȥ�����һ���쳣������ͷ�쳣��װΪZdalCommunicationException�׳�
                                                                                             SQLException lastSQLException = exceptions
                                                                                                 .get(lastElementIndex);
                                                                                             if (exceptionSorter
                                                                                                 .isExceptionFatal(lastSQLException)) {
                                                                                                 exceptions
                                                                                                     .remove(lastElementIndex);
                                                                                                 exceptions
                                                                                                     .add(
                                                                                                         0,
                                                                                                         new ZdalCommunicationException(
                                                                                                             "zdal communicationException ",
                                                                                                             lastSQLException));
                                                                                             }
                                                                                         }
                                                                                         return super
                                                                                             .onSQLException(
                                                                                                 exceptions,
                                                                                                 exceptionSorter,
                                                                                                 args);
                                                                                     };
                                                                                 };

    /**
     * ����������� ����������autoCommitȻ�����͡�
     * 
     * ������������ӣ����Datasource����ѡ��һ��Datasource��������
     * 
     * Ȼ�����ӷ���parentConnection�Ŀ���������map��(getConnectionProxy.getAuctualConnections)
     * @param dbIndex
     * @return 
     * @throws SQLException
     */
    protected void createConnection(DBSelector dbSelector, String dbIndex) throws SQLException {
        this.createConnection(dbSelector, dbIndex, new LinkedHashMap<DataSource, SQLException>(0));
    }

    protected void createConnection(DBSelector dbSelector, String dbIndex,
                                    Map<DataSource, SQLException> failedDataSources)
                                                                                    throws SQLException {
        //		Map<String, ConnectionAndDatasource> connections = getConnectionProxy().getActualConnections();
        ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
            .getConnectionAndDatasourceByDBSelectorID(dbIndex);

        //DataSource datasource = null;
        //datasource = dbSelector.select();
        if (connectionAndDatasource == null) {
            if (dbIndex == null) {
                throw new SQLException(new StringBuilder("data source ").append(dbIndex).append(
                    " does not exist").toString());
            }
            //û��connection
            //try {
            //connectionAndDatasource = getNewConnectionAndDataSource(datasource,dbSelector);
            connectionAndDatasource = dbSelector.tryExecute(failedDataSources,
                createConnectionTryer, retryingTimes, operation_type, dbSelector);
            getConnectionProxy().put(dbIndex, connectionAndDatasource);
            //} catch (NullPointerException e) {
            //	throw new SQLException(new StringBuilder("data source ").append(dbIndex).append(" does not exist")
            //			.toString());
            //}catch (SQLException e) {
            //	throw new RetrySQLException(e, datasource);
            //}
        } else {
            //������connection
            //datasource = connectionAndDatasource.parentDataSource;
            try {
                connectionAndDatasource.connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                //throw new RetrySQLException(e, datasource);
                failedDataSources.put(connectionAndDatasource.parentDataSource, e);
                getConnectionProxy().removeConnectionAndDatasourceByID(dbIndex);
                createConnection(dbSelector, dbIndex, failedDataSources);
            }

        }
    }

    /**
     * �õ�ǰ���ӽ���statementd
     * 
     * @param connection ��ǰ�����õ�connection,�������Դ�map��ȡ����ΪЧ���ϵĿ������Ի��ǲ�������
     * @param connections 
     * @param dbIndex
     * @param retringContext
     * @return
     * @throws SQLException
     */
    protected Statement createStatementInternal(Connection connection, String dbIndex,
                                                Map<DataSource, SQLException> failedDataSources)
                                                                                                throws SQLException {
        Statement stmt;
        if (this.resultSetType != -1 && this.resultSetConcurrency != -1
            && this.resultSetHoldability != -1) {
            stmt = connection.createStatement(this.resultSetType, this.resultSetConcurrency,
                this.resultSetHoldability);
        } else if (this.resultSetType != -1 && this.resultSetConcurrency != -1) {
            stmt = connection.createStatement(this.resultSetType, this.resultSetConcurrency);
        } else {
            stmt = connection.createStatement();
        }

        return stmt;
    }

    public Connection getConnection() throws SQLException {
        return connectionProxy;
    }

    private boolean executeInternal(String sql, int autoGeneratedKeys, int[] columnIndexes,
                                    String[] columnNames) throws SQLException {
        SqlType sqlType = getSqlType(sql);
        if (sqlType == SqlType.SELECT || sqlType == SqlType.SELECT_FROM_DUAL
            || sqlType == SqlType.SELECT_FOR_UPDATE) {
            if (this.dbConfigType == DataSourceConfigType.GROUP) {
                executeQuery0(sql, sqlType);
            } else {
                executeQuery(sql);
            }
            return true;
        } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE
                   || sqlType == SqlType.DELETE) {
            if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
                executeUpdate(sql);
            } else if (autoGeneratedKeys != -1) {
                executeUpdate(sql, autoGeneratedKeys);
            } else if (columnIndexes != null) {
                executeUpdate(sql, columnIndexes);
            } else if (columnNames != null) {
                executeUpdate(sql, columnNames);
            } else {
                executeUpdate(sql);
            }

            return false;
        } else {
            throw new SQLException("only select, insert, update, delete sql is supported");
        }
    }

    public boolean execute(String sql) throws SQLException {
        return executeInternal(sql, -1, null, null);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return executeInternal(sql, autoGeneratedKeys, null, null);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return executeInternal(sql, -1, columnIndexes, null);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return executeInternal(sql, -1, null, columnNames);
    }

    private ResultSet executeQuery0(String sql, SqlType sqlType) throws SQLException {
        checkClosed();
        this.setOperation_type(DB_OPERATION_TYPE.READ_FROM_DB);
        //��ȡ����
        DBSelector dbselector = getGroupDBSelector(sqlType);
        if (dbselector == null) {
            throw new IllegalStateException("load balance����Դ�������ʹ���");
        }

        //����ִ�н��
        return dbselector.tryExecute(new LinkedHashMap<DataSource, SQLException>(0),
            this.executeQueryTryer, retryingTimes, operation_type, sql, sqlType);
    }

    protected DataSourceTryer<ResultSet> executeQueryTryer = new AbstractDataSourceTryer<ResultSet>() {
                                                               public ResultSet tryOnDataSource(
                                                                                                DataSource ds,
                                                                                                String name,
                                                                                                Object... args)
                                                                                                               throws SQLException {
                                                                   String sql = (String) args[0];
                                                                   SqlType sqlType = (SqlType) args[1];
                                                                   //��ȡ����
                                                                   Connection conn = ZdalStatement.this
                                                                       .getGroupConnection(ds,
                                                                           sqlType, name);
                                                                   return ZdalStatement.this
                                                                       .executeQueryOnConnection(
                                                                           conn, sql);
                                                               }

                                                           };

    private ResultSet executeQueryOnConnection(Connection conn, String sql) throws SQLException {
        Statement stmt = createStatementInternal(conn, null, null);
        actualStatements.add(stmt);
        List<ResultSet> actualResultSets = new ArrayList<ResultSet>();

        actualResultSets.add(stmt.executeQuery(sql));

        DummyTResultSet currentResultSet = new SimpleTResultSet(this, actualResultSets);

        openResultSets.add(currentResultSet);

        this.results = currentResultSet;
        this.moreResults = false;
        this.updateCount = -1;

        return this.results;

    }

    /**
     * �����������Ӳ��գ�ֱ�ӷ��ظ����ӣ����򴴽�������
     * @param ds
     * @return
     * @throws SQLException
     */
    public Connection getGroupConnection(DataSource ds, SqlType sqlType, String name)
                                                                                     throws SQLException {
        Connection conn = null;
        //�����������ж��selectʱ�������޷��ͷţ����Թ���ͬһ��select������.
        if (this.getConnectionProxy().get(name) != null) {
            conn = this.getConnectionProxy().get(name).connection;
        } else {
            ConnectionAndDatasource connectionAndDatasource = new ConnectionAndDatasource();
            connectionAndDatasource.parentDataSource = ds;
            connectionAndDatasource.dbSelector = null;
            conn = ds.getConnection();
            connectionAndDatasource.connection = conn;
            this.getConnectionProxy().put(name, connectionAndDatasource);
        }
        conn.setAutoCommit(autoCommit);
        return conn;
    }

    /**
     * ��dbSelector�Ǳ�loadbalance�����õ���
     * @return
     */
    public DBSelector getGroupDBSelector(SqlType sqlType) {
        DBSelector rGroupDBSelector = null, wGroupDBSelector = null;
        for (Map.Entry<String, DBSelector> dbSelector : dbSelectors.entrySet()) {
            if (dbSelector.getKey().endsWith(AppRule.DBINDEX_SUFFIX_READ)
                && dbSelector.getValue() != null) {
                rGroupDBSelector = dbSelector.getValue();
            } else if (dbSelector.getKey().endsWith(AppRule.DBINDEX_SUFFIX_WRITE)
                       && dbSelector.getValue() != null) {
                wGroupDBSelector = dbSelector.getValue();
            } else {
                throw new IllegalArgumentException("The dbSelector set error!");
            }
        }
        //�����������ֱ�ӵ�д��
        if (sqlType != SqlType.SELECT) {
            return wGroupDBSelector;
        } else if (!autoCommit) {
            return wGroupDBSelector;
        } else {
            return rGroupDBSelector;
        }
    }

    /**
     * ��dbSelector�Ǳ�loadbalance�����õ���
     * @return
     */
    public String getGroupDBSelectorID(SqlType sqlType) {
        String rGroupDBSelectorID = null, wGroupDBSelectorID = null;
        for (Map.Entry<String, DBSelector> dbSelector : dbSelectors.entrySet()) {
            if (dbSelector.getKey().endsWith(AppRule.DBINDEX_SUFFIX_READ)
                && dbSelector.getValue() != null) {
                rGroupDBSelectorID = dbSelector.getKey();
            } else if (dbSelector.getKey().endsWith(AppRule.DBINDEX_SUFFIX_WRITE)
                       && dbSelector.getValue() != null) {
                wGroupDBSelectorID = dbSelector.getKey();
            } else {
                throw new IllegalArgumentException("The dbSelector set error!");
            }
        }
        if (sqlType != SqlType.SELECT) {
            return wGroupDBSelectorID;
        } else {
            return rGroupDBSelectorID;
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {

        if (this.dbConfigType == DataSourceConfigType.GROUP) {
            SqlType sqlType = getSqlType(sql);
            return this.executeQuery0(sql, sqlType);
        }

        checkClosed();

        SqlExecutionContext context = getExecutionContext(sql, null);
        /*
         * modified by shenxun:
         * ������Ҫ�Ǵ���mappingRule���ؿյ�����£�Ӧ�÷��ؿս����
         */
        if (context.mappingRuleReturnNullValue()) {
            ResultSet emptyResultSet = getEmptyResultSet();
            this.results = emptyResultSet;
            return emptyResultSet;
        }

        //        int tablesSize = 0;
        dumpSql(sql, context.getTargetSqls());

        List<SQLException> exceptions = null;
        List<ResultSet> actualResultSets = new ArrayList<ResultSet>();
        // int databaseSize = context.getTargetSqls().size();
        for (Entry<String/*dbSelectorID*/, SqlAndTable[]> entry : context.getTargetSqls()
            .entrySet()) {
            for (SqlAndTable targetSql : entry.getValue()) {
                //                tablesSize++;
                try {
                    //RetringContext retringContext = context.getRetringContext();
                    String dbSelectorId = entry.getKey();
                    Statement stmt = createStatementByDataSourceSelectorID(dbSelectorId, context
                        .getFailedDataSources());
                    //��������
                    actualStatements.add(stmt);

                    queryAndAddResultToCollection(dbSelectorId, actualResultSets, targetSql, stmt,
                        context.getFailedDataSources());

                } catch (SQLException e) {

                    //�쳣����
                    if (exceptions == null) {
                        exceptions = new ArrayList<SQLException>();
                    }
                    exceptions.add(e);
                    ExceptionUtils.throwSQLException(exceptions, sql, Collections.emptyList()); //ֱ���׳�
                }
            }
        }

        ExceptionUtils.throwSQLException(exceptions, sql, Collections.emptyList());

        DummyTResultSet resultSet = mergeResultSets(context, actualResultSets);
        openResultSets.add(resultSet);

        this.results = resultSet;
        this.moreResults = false;
        this.updateCount = -1;

        return this.results;
    }

    protected void queryAndAddResultToCollection(String dbSelectorId,
                                                 List<ResultSet> actualResultSets,
                                                 SqlAndTable targetSql, Statement stmt,
                                                 Map<DataSource, SQLException> failedDataSources)
                                                                                                 throws SQLException {
        //added by fanzeng.
        //����dbSelectorId��ȡ��Ӧ������Դ�ı�ʶ���Լ�����Դ��Ȼ��ŵ�threadlocal��
        try {
            actualResultSets.add(stmt.executeQuery(targetSql.sql));
        } finally {
            Map<String, DataSource> map = getActualIdAndDataSource(dbSelectorId);
            ThreadLocalMap.put(ThreadLocalString.GET_ID_AND_DATABASE, map);
        }
    }

    protected Connection getActualConnection(String key) {
        ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
            .getConnectionAndDatasourceByDBSelectorID(key);
        Connection conn = connectionAndDatasource.connection;
        return conn;
    }

    // ��ȡ������ ����Դ�ı�ʶ�Լ�����Դ
    protected Map<String, DataSource> getActualIdAndDataSource(String key) {
        ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
            .getConnectionAndDatasourceByDBSelectorID(key);
        Map<String, DataSource> map = new HashMap<String, DataSource>();
        if (connectionAndDatasource != null) {
            DBSelector dbSelector = connectionAndDatasource.dbSelector;
            DataSource ds = connectionAndDatasource.parentDataSource;
            if (dbSelector == null || ds == null) {
                throw new IllegalArgumentException("����Դ����Ϊ�գ����飡");
            }
            //��������Դ����ǰ׺��by���� 20130903
            map.put(appDsName + "." + dbSelector.getSelectedDSName(), ds);
        }
        return map;
    }

    protected DummyTResultSet mergeResultSets(SqlExecutionContext context,
                                              List<ResultSet> actualResultSets) throws SQLException {
        if (context.getOrderByColumns() != null && !context.getOrderByColumns().isEmpty()
            && context.getGroupFunctionType() != GroupFunctionType.NORMAL) {
            throw new SQLException("'group function' and 'order by' can't be together!");
        }
        if (context.getGroupFunctionType() == GroupFunctionType.AVG) {
            throw new SQLException("The group function 'AVG' is not supported now!");
        } else if (context.getGroupFunctionType() == GroupFunctionType.COUNT) {
            return new CountTResultSet(this, actualResultSets);
        } else if (context.getGroupFunctionType() == GroupFunctionType.MAX) {
            return new MaxTResultSet(this, actualResultSets);
        } else if (context.getGroupFunctionType() == GroupFunctionType.MIN) {
            return new MinTResultSet(this, actualResultSets);
        } else if (context.getGroupFunctionType() == GroupFunctionType.SUM) {
            return new SumTResultSet(this, actualResultSets);
        } else if (context.getOrderByColumns() != null && !context.getOrderByColumns().isEmpty()) {
            OrderByColumn[] orderByColumns = new OrderByColumn[context.getOrderByColumns().size()];
            int i = 0;
            for (OrderByEle element : context.getOrderByColumns()) {
                orderByColumns[i] = new OrderByColumn();
                orderByColumns[i].setColumnName(element.getName());
                orderByColumns[i++].setAsc(element.isASC());
            }
            OrderByTResultSet orderByTResultSet = new OrderByTResultSet(this, actualResultSets);
            orderByTResultSet.setOrderByColumns(orderByColumns);
            orderByTResultSet.setLimitFrom(context.getSkip());
            orderByTResultSet.setLimitTo(context.getMax());
            return orderByTResultSet;
        } else {
            SimpleTResultSet simpleTResultSet = new SimpleTResultSet(this, actualResultSets);
            simpleTResultSet.setLimitFrom(context.getSkip());
            simpleTResultSet.setLimitTo(context.getMax());
            return simpleTResultSet;
        }
    }

    protected Statement createStatementByDataSourceSelectorID(
                                                              String id,
                                                              Map<DataSource, SQLException> failedDataSources)
                                                                                                              throws SQLException {

        Connection connection = getActualConnection(id);
        Statement stmt = createStatementInternal(connection, id, failedDataSources);
        return stmt;
    }

    private int executeUpdateInternal0(String sql, int autoGeneratedKeys, int[] columnIndexes,
                                       String[] columnNames) throws SQLException {
        checkClosed();
        //��ȡ����Դ
        this.setOperation_type(DB_OPERATION_TYPE.WRITE_INTO_DB);
        //��ȡ����
        DBSelector dbselector = getGroupDBSelector(SqlType.DEFAULT_SQL_TYPE);
        if (dbselector == null) {
            throw new IllegalStateException("load balance����Դ�������ʹ���");
        }
        this.autoGeneratedKeys = autoGeneratedKeys;
        this.columnIndexes = columnIndexes;
        this.columnNames = columnNames;
        boolean needRetry = this.autoCommit;
        Map<DataSource, SQLException> failedDataSources = needRetry ? new LinkedHashMap<DataSource, SQLException>(
            0)
            : null;
        //����ִ�н��
        return dbselector.tryExecute(failedDataSources, this.executeUpdateTryer, retryingTimes,
            operation_type, sql, SqlType.DEFAULT_SQL_TYPE);
    }

    private DataSourceTryer<Integer> executeUpdateTryer = new AbstractDataSourceTryer<Integer>() {
                                                            public Integer tryOnDataSource(
                                                                                           DataSource ds,
                                                                                           String name,
                                                                                           Object... args)
                                                                                                          throws SQLException {
                                                                SqlType sqlType = (SqlType) args[1];
                                                                //��ȡ����
                                                                Connection conn = ZdalStatement.this
                                                                    .getGroupConnection(ds,
                                                                        sqlType, name);
                                                                return ZdalStatement.this
                                                                    .executeUpdateOnConnection(
                                                                        conn, args);
                                                            }
                                                        };

    private int executeUpdateOnConnection(Connection conn, Object... args) throws SQLException {
        Statement stmt = createStatementInternal(conn, null, null);
        String sql = (String) args[0];
        int affectedRows = 0;
        if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
            affectedRows += stmt.executeUpdate(sql);
        } else if (autoGeneratedKeys != -1) {
            affectedRows += stmt.executeUpdate(sql, autoGeneratedKeys);
        } else if (columnIndexes != null) {
            affectedRows += stmt.executeUpdate(sql, columnIndexes);
        } else if (columnNames != null) {
            affectedRows += stmt.executeUpdate(sql, columnNames);
        } else {
            affectedRows += stmt.executeUpdate(sql);
        }
        return affectedRows;
    }

    private int executeUpdateInternal(String sql, int autoGeneratedKeys, int[] columnIndexes,
                                      String[] columnNames) throws SQLException {
        checkClosed();

        SqlExecutionContext context = getExecutionContext(sql, null);

        if (context.mappingRuleReturnNullValue()) {
            return 0;
        }

        dumpSql(sql, context.getTargetSqls());

        int affectedRows = 0;
        List<SQLException> exceptions = null;

        for (Entry<String, SqlAndTable[]> entry : context.getTargetSqls().entrySet()) {
            for (SqlAndTable targetSql : entry.getValue()) {
                //                tablesSize++;
                try {
                    String dbSelectorId = entry.getKey();
                    Statement stmt = createStatementByDataSourceSelectorID(dbSelectorId, context
                        .getFailedDataSources());
                    actualStatements.add(stmt);
                    //added by fanzeng.
                    //����dbSelectorId��ȡ��Ӧ������Դ�ı�ʶ���Լ�����Դ��Ȼ��ŵ�threadlocal��
                    Map<String, DataSource> map = getActualIdAndDataSource(dbSelectorId);
                    ThreadLocalMap.put(ThreadLocalString.GET_ID_AND_DATABASE, map);
                    if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
                        affectedRows += stmt.executeUpdate(targetSql.sql);
                    } else if (autoGeneratedKeys != -1) {
                        affectedRows += stmt.executeUpdate(targetSql.sql, autoGeneratedKeys);
                    } else if (columnIndexes != null) {
                        affectedRows += stmt.executeUpdate(targetSql.sql, columnIndexes);
                    } else if (columnNames != null) {
                        affectedRows += stmt.executeUpdate(targetSql.sql, columnNames);
                    } else {
                        affectedRows += stmt.executeUpdate(targetSql.sql);
                    }

                } catch (SQLException e) {
                    if (exceptions == null) {
                        exceptions = new ArrayList<SQLException>();
                    }
                    exceptions.add(e);
                }
            }
        }

        this.results = null;
        this.moreResults = false;
        this.updateCount = affectedRows;

        ExceptionUtils.throwSQLException(exceptions, sql, Collections.emptyList());

        return affectedRows;
    }

    public int executeUpdate(String sql) throws SQLException {
        if (this.dbConfigType == DataSourceConfigType.GROUP) {
            return executeUpdateInternal0(sql, -1, null, null);
        }
        return executeUpdateInternal(sql, -1, null, null);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (this.dbConfigType == DataSourceConfigType.GROUP) {
            return executeUpdateInternal0(sql, autoGeneratedKeys, null, null);
        }
        return executeUpdateInternal(sql, autoGeneratedKeys, null, null);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        if (this.dbConfigType == DataSourceConfigType.GROUP) {
            return executeUpdateInternal0(sql, -1, columnIndexes, null);
        }
        return executeUpdateInternal(sql, -1, columnIndexes, null);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        if (this.dbConfigType == DataSourceConfigType.GROUP) {
            return executeUpdateInternal0(sql, -1, null, columnNames);
        }
        return executeUpdateInternal(sql, -1, null, columnNames);
    }

    public void addBatch(String sql) throws SQLException {
        checkClosed();

        if (batchedArgs == null) {
            batchedArgs = new ArrayList<Object>();
        }

        if (sql != null) {
            batchedArgs.add(sql);
        }
    }

    /**
     * @param targetSqls: key:��������ԴID; value:��������Դ��ִ�е����������SQL
     * @throws ZdalCheckedExcption
     */
    protected void sortBatch0(String originalSql, Map<String, List<String>> targetSqls)
                                                                                       throws SQLException {
        SqlType sqlType = getSqlType(originalSql);
        String dbselectorID = getGroupDBSelectorID(sqlType);
        if (!targetSqls.containsKey(dbselectorID)) {
            targetSqls.put(dbselectorID, new ArrayList<String>());
        }
        List<String> sqls = targetSqls.get(dbselectorID);
        sqls.add(originalSql);
    }

    /**
     * @param targetSqls: key:��������ԴID; value:��������Դ��ִ�е����������SQL
     * @throws ZdalCheckedExcption
     */
    protected void sortBatch(String originalSql, Map<String, List<String>> targetSqls)
                                                                                      throws SQLException {
        //TODO:batch�����ʹ����ӳ�����ӳ�����û�з��ؽ��ʱ�����д���
        try {
            List<TargetDB> targets;
            String virtualTableName;
            List<String> virtualJoinTableNames;
            if (ruleController != null) {
                TargetDBMeta metaData = ruleController.getDBAndTables(originalSql, null);
                targets = metaData.getTarget();
                virtualTableName = metaData.getVirtualTableName();
                virtualJoinTableNames = metaData.getVirtualJoinTableNames();
            } else {
                SqlType sqlType = getSqlType(originalSql);
                SqlDispatcher sqlDispatcher = selectSqlDispatcher(autoCommit, sqlType);
                DispatcherResult dispatcherResult = getExecutionMetaData(originalSql, Collections
                    .emptyList(), null, sqlDispatcher);
                targets = dispatcherResult.getTarget();
                virtualTableName = dispatcherResult.getVirtualTableName();
                virtualJoinTableNames = dispatcherResult.getVirtualJoinTableNames();
            }
            for (TargetDB target : targets) {
                //���������¾ɹ������
                String targetName = ruleController != null ? target.getWritePool()[0] : target
                    .getDbIndex();
                if (!targetSqls.containsKey(targetName)) {
                    targetSqls.put(targetName, new ArrayList<String>());
                }

                List<String> sqls = targetSqls.get(targetName);

                Set<String> actualTables = target.getTableNames();
                for (String tab : actualTables) {
                    String targetSql = replaceTableName(originalSql, virtualTableName, tab);
                    //���metaData(Ҳ����DispatcherResult)������join��������ô���滻��;
                    targetSql = replaceJoinTableName(virtualTableName, virtualJoinTableNames, tab,
                        targetSql);
                    sqls.add(targetSql);
                }
            }
        } catch (ZdalCheckedExcption e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * @param virtualTableName
     * @param virtualJoinTableNames
     * @param realTableName
     * @param targetSql
     * @return
     */
    private String replaceJoinTableName(String virtualTableName,
                                        List<String> virtualJoinTableNames, String realTableName,
                                        String targetSql) {
        if (virtualJoinTableNames.size() > 0) {
            String suffix = getSuffix(realTableName, virtualTableName);
            for (String vtab : virtualJoinTableNames) {
                //��ʵ����������,ָ��
                String repTab = vtab + suffix;
                String[] tabs = vtab.split(",");
                if (tabs.length == 2) {
                    vtab = tabs[0];
                    repTab = tabs[1];
                }
                targetSql = replaceTableName(targetSql, vtab, repTab);
            }
        }
        return targetSql;
    }

    public int[] executeBatch() throws SQLException {
        checkClosed();

        if (batchedArgs == null || batchedArgs.isEmpty()) {
            return new int[0];
        }

        List<SQLException> exceptions = null;

        try {
            Map<String/*����ԴID*/, List<String>/*����Դ��ִ�е�SQL*/> targetSqls = new HashMap<String, List<String>>();

            for (Object arg : batchedArgs) {
                if (this.dbConfigType == DataSourceConfigType.GROUP) {
                    sortBatch0((String) arg, targetSqls);
                } else {
                    sortBatch((String) arg, targetSqls);
                }

            }

            //Map<String, ConnectionAndDatasource> connections = getConnectionProxy().getActualConnections();

            for (Entry<String, List<String>> entry : targetSqls.entrySet()) {
                //���ûȡ������Դ
                String dbSelectorID = entry.getKey();
                //У���Ƿ�����batch����
                checkBatchDataBaseID(dbSelectorID);
                //retryContextΪnull��ʱ���ֱ���׳��쳣��
                createConnectionByID(dbSelectorID);
                try {

                    Statement stmt = createStatementByDataSourceSelectorID(dbSelectorID, null);

                    actualStatements.add(stmt);

                    for (String targetSql : entry.getValue()) {
                        stmt.addBatch(targetSql);
                    }

                    // TODO: ���Է���ֵ
                    stmt.executeBatch();

                    stmt.clearBatch();
                } catch (SQLException e) {
                    if (exceptions == null) {
                        exceptions = new ArrayList<SQLException>();
                    }
                    exceptions.add(e);
                }
            }
        } finally {
            batchedArgs.clear();
        }

        ExceptionUtils.throwSQLException(exceptions, null, Collections.emptyList());

        // TODO: ���Է���ֵ
        return new int[0];
    }

    public void clearBatch() throws SQLException {
        checkClosed();

        if (batchedArgs != null) {
            batchedArgs.clear();
        }
    }

    public ResultSet getResultSet() throws SQLException {
        return results;
    }

    /**
     * ��֧�ֶ�������ѯ�����Ƿ���false
     */
    public boolean getMoreResults() throws SQLException {
        return moreResults;
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("getMoreResults");
    }

    public int getUpdateCount() throws SQLException {
        return updateCount;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException("getGeneratedKeys");
    }

    public void cancel() throws SQLException {
        throw new UnsupportedOperationException("cancel");
    }

    protected void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("No operations allowed after statement closed.");
        }
    }

    public void closeInternal(boolean removeThis) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("invoke close");
        }

        if (closed) {
            return;
        }

        List<SQLException> exceptions = null;

        try {
            for (ResultSet resultSet : openResultSets) {
                try {
                    //bug fix by shenxun :�ڲ�������remove,��TStatment��ͳһclear������
                    ((DummyTResultSet) resultSet).closeInternal(false);
                } catch (SQLException e) {
                    if (exceptions == null) {
                        exceptions = new ArrayList<SQLException>();
                    }
                    exceptions.add(e);
                }
            }

            for (Statement stmt : actualStatements) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    if (exceptions == null) {
                        exceptions = new ArrayList<SQLException>();
                    }
                    exceptions.add(e);
                }
            }
        } finally {
            closed = true;
            openResultSets.clear();
            actualStatements.clear();

            results = null;
            if (removeThis) {
                if (!getConnectionProxy().getOpenStatements().remove(this)) {
                    log.warn("open statement does not exist");
                }
            }
        }

        ExceptionUtils.throwSQLException(exceptions, null, Collections.emptyList());
    }

    public void close() throws SQLException {
        closeInternal(true);
    }

    /**
     * ��batch�������ÿ�ζ�Ҫ����Ƿ�ͬһ������Դ��ʶ,ֻ�����߼�����ж�
     * added by fanzeng����֧��batch�ĵ�������
     * @param dbSelectorID  �߼�����Դ��ʶ
     * @throws SQLException
     */
    public void checkBatchDataBaseID(String dbSelectorID) throws SQLException {
        if (StringUtil.isBlank(dbSelectorID)) {
            throw new SQLException("The dbSelectorID can't be null!");
        }
        //����������У���һ�ξ�����batchDataBaseId��ֵ,Ȼ��ֱ�ӷ���
        if (!isAutoCommit() && getBatchDataBaseId() == null) {
            setBatchDataBaseId(dbSelectorID);
            return;
        }
        //����������У����ҵ�ǰ��dbId�ͻ����dbId��ͬ�����׳��쳣��         
        if (!isAutoCommit() && !dbSelectorID.equals(getBatchDataBaseId())) {
            throw new SQLException("batch����ֻ֧�ֵ��������,��ǰdbSelectorID=" + dbSelectorID + ",�����dbId="
                                   + getBatchDataBaseId());
        }
    }

    /**
     * ����Ϊ��֧�ֵķ���
     */
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("getFetchDirection");
    }

    public void setFetchDirection(int fetchDirection) throws SQLException {
        throw new UnsupportedOperationException("setFetchDirection");
    }

    public int getFetchSize() throws SQLException {
        return this.fetchSize;
        //throw new UnsupportedOperationException("getFetchSize");
    }

    public void setFetchSize(int fetchSize) throws SQLException {
        this.fetchSize = fetchSize;
        //throw new UnsupportedOperationException("setFetchSize");
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException("getMaxFieldSize");
    }

    public void setMaxFieldSize(int maxFieldSize) throws SQLException {
        throw new UnsupportedOperationException("setMaxFieldSize");
    }

    public int getMaxRows() throws SQLException {
        throw new UnsupportedOperationException("getMaxRows");
    }

    public void setMaxRows(int maxRows) throws SQLException {
        throw new UnsupportedOperationException("setMaxRows");
    }

    public int getQueryTimeout() throws SQLException {
        throw new UnsupportedOperationException("getQueryTimeout");
    }

    public void setQueryTimeout(int queryTimeout) throws SQLException {
        throw new UnsupportedOperationException("setQueryTimeout");
    }

    public void setCursorName(String cursorName) throws SQLException {
        throw new UnsupportedOperationException("setCursorName");
    }

    public void setEscapeProcessing(boolean escapeProcessing) throws SQLException {
        throw new UnsupportedOperationException("setEscapeProcessing");
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {
    }

    /**
     * ����Ϊ���߼���getter/setter
     */
    public int getResultSetType() throws SQLException {
        return resultSetType;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    public int getResultSetConcurrency() throws SQLException {
        return resultSetConcurrency;
    }

    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public int getResultSetHoldability() throws SQLException {
        return resultSetHoldability;
    }

    public void setResultSetHoldability(int resultSetHoldability) {
        this.resultSetHoldability = resultSetHoldability;
    }

    public Map<String, DBSelector> getDataSourcePool() {
        return dbSelectors;
    }

    public void setDataSourcePool(Map<String, DBSelector> dbSelectors) {
        this.dbSelectors = dbSelectors;
    }

    public ZdalConnection getConnectionProxy() {
        return connectionProxy;
    }

    public void setConnectionProxy(ZdalConnection connectionProxy) {
        this.connectionProxy = connectionProxy;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Set<ResultSet> getTResultSets() {
        return openResultSets;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public long getTimeoutForEachTable() {
        return timeoutForEachTable;
    }

    public void setTimeoutForEachTable(long timeoutForEachTable) {
        this.timeoutForEachTable = timeoutForEachTable;
    }

    public int getRetryingTimes() {
        return retryingTimes;
    }

    public void setRetryingTimes(int retryingTimes) {
        this.retryingTimes = retryingTimes;
    }

    public void setOperation_type(DB_OPERATION_TYPE operation_type) {
        this.operation_type = operation_type;
    }

    public DB_OPERATION_TYPE getOperation_type() {
        return operation_type;
    }

    public String getBatchDataBaseId() {
        return batchDataBaseId;
    }

    public void setBatchDataBaseId(String batchDataBaseId) {
        this.batchDataBaseId = batchDataBaseId;
    }

    public boolean isHintReplaceSupport() {
        return isHintReplaceSupport;
    }

    public void setHintReplaceSupport(boolean isHintReplaceSupport) {
        this.isHintReplaceSupport = isHintReplaceSupport;
    }

    public DataSourceConfigType getDbConfigType() {
        return dbConfigType;
    }

    public void setDbConfigType(DataSourceConfigType dbConfigType) {
        this.dbConfigType = dbConfigType;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public String getAppDsName() {
        return appDsName;
    }

    public void setAppDsName(String appDsName) {
        this.appDsName = appDsName;
    }

}
