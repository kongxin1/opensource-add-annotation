/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.dispatcher.SqlDispatcher;
import com.alipay.zdal.common.OperationDBType;

/**
 * 
 * @author ����
 * @version $Id: RetryableTStatement.java, v 0.1 2014-1-6 ����01:49:21 Exp $
 */
public class RetryableTStatement extends ZdalStatement {
    private static final Logger logger                  = Logger
                                                            .getLogger(RetryableTStatement.class);
    public boolean              redirectToWriteDatabase = false;

    public RetryableTStatement(SqlDispatcher writeDispatcher, SqlDispatcher readDispatcher) {
        super(writeDispatcher, readDispatcher);
    }

    //	@Override
    //	/*
    //	 * TEST required :����1�����ԣ��������ԣ������ߵ�43�к�ݹ�������׳��쳣�����
    //	 * 
    //	 * �����ᱣ֤dbIndex->Connection+datasource+dbselector map�е�Datasource�ض���ֵ
    //	 */
    //	protected void createConnection(DBSelector dbselector, String dbIndex,
    //			RetringContext retringContext) throws SQLException {
    //
    //		try {
    //			// �������Ӳ��ŵ�TConnection ��map dbSelectorID->connection+datasource+dbSelector��
    //			super.createConnection(dbselector, dbIndex, retringContext);
    //
    //		} catch (RetrySQLException e) {
    //
    //			retringContext.addSQLException(e.getSqlException());
    //
    //			validRetryable(retringContext, e.getSqlException());
    //			
    //			dbselector = removeCurrentDataSourceFromDbSelector( dbselector, e.getCurrentDataSource(),
    //					retringContext);
    //			
    //			// ���������ӳ������������������,������Ƴ���ѭ��Ƕ��ʱ����Ϊ�ܹ�ȡ��ConnectionAndDataSource���������������������޳�����
    //			getConnectionProxy().removeConnectionAndDatasourceByID(dbIndex);
    //			// ���removeʧ�ܻ�Ϊд�������Ѿ�ֱ���׳��� ������ﶼ�ǳɹ���
    //			createConnection(dbselector, dbIndex, retringContext);
    //		}
    //	}

    //	protected boolean reachMaxRetryableTimes(RetringContext retringContext) {
    //		return retringContext.getAlreadyRetringTimes() > retringTimes;
    //	}

    @Override
    protected Statement createStatementInternal(Connection connection, String dbIndex,
                                                Map<DataSource, SQLException> failedDataSources)
                                                                                                throws SQLException {
        try {
            return super.createStatementInternal(connection, dbIndex, failedDataSources);
        } catch (SQLException e) {
            //retringContext.addSQLException(e);
            //validRetryable(retringContext, e);
            //added by fanzeng, ��Ҫ��һ����֤�˴��Ƿ���Ҫ����
            validRetryable(dbIndex, e, OperationDBType.readFromDb);
            if (failedDataSources != null) {
                ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
                    .getConnectionAndDatasourceByDBSelectorID(dbIndex);
                failedDataSources.put(connectionAndDatasource.parentDataSource, e);
            } else {
                //����������ʱ����Ҫ���ԣ�failedDataSourcesΪnullֵ��ֱ�ӽ��쳣�׳�
                //added by fanzeng.

                throw e;
            }
            connection = tryToConnectToOtherAvailableDataSource(dbIndex, failedDataSources);
            return createStatementInternal(connection, dbIndex, failedDataSources);
        }
    }

    @Override
    protected void queryAndAddResultToCollection(String dbSelectorId,
                                                 List<ResultSet> actualResultSets,
                                                 SqlAndTable targetSql, Statement stmt,
                                                 Map<DataSource, SQLException> failedDataSources)
                                                                                                 throws SQLException {
        try {
            super.queryAndAddResultToCollection(dbSelectorId, actualResultSets, targetSql, stmt,
                failedDataSources);
        } catch (SQLException e) {
            //retringContext.addSQLException(e);
            //validRetryable(retringContext,e);
            validRetryable(dbSelectorId, e, OperationDBType.readFromDb);
            if (failedDataSources != null) {
                ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
                    .getConnectionAndDatasourceByDBSelectorID(dbSelectorId);
                failedDataSources.put(connectionAndDatasource.parentDataSource, e);
            } else {
                //����������ʱ����Ҫ���ԣ�failedDataSourcesΪnullֵ��ֱ�ӽ��쳣�׳�
                //added by fanzeng.
                throw e;
            }
            Connection connection = tryToConnectToOtherAvailableDataSource(dbSelectorId,
                failedDataSources);
            stmt = createStatementInternal(connection, dbSelectorId, failedDataSources);
            queryAndAddResultToCollection(dbSelectorId, actualResultSets, targetSql, stmt,
                failedDataSources);
        }
    }

    /**
     * 1.�ȴ�parent TConnection�����л�ȡconnectionsMap.<br>
     * 2.����DBSelectorId�ҵ���Ӧ��connection��Ϣ��<br>
     * 3.�Ƴ�connectionsMap���������Datasource.<br>
     * 4.�Ƴ�dbSelector�з��������datasource .(����dbSelectorһ�Σ�Ȼ���Ƴ�).<br>
     * 5.����dbSelectorId���µ�dbSelector.���°���Ȩ��ѡ��һ��datasource.<br>
     * 6.��ȡ���ӣ�����connectionsMap.<br>
     * 7.��������<br>
     * 
     * @param dbIndex
     * @param retringContext
     * @param e
     * @return
     * @throws SQLException
     */
    protected Connection tryToConnectToOtherAvailableDataSource(
                                                                String dbIndex,
                                                                Map<DataSource, SQLException> failedDataSources)
                                                                                                                throws SQLException {
        Connection connection;
        //��TConnection��connectionsMap��ȡ����connection

        ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
            .getConnectionAndDatasourceByDBSelectorID(dbIndex);
        DBSelector dbSelector = connectionAndDatasource.dbSelector;
        //DataSource ds = connectionAndDatasource.parentDataSource;
        //��dbSelector���Ƴ���ǰ�������datasource
        //dbSelector = removeCurrentDataSourceFromDbSelector(dbSelector, ds,
        //		retringContext);
        //�Ӹ������Ƴ�
        getConnectionProxy().removeConnectionAndDatasourceByID(dbIndex);
        // ���½�������, exclueDataSource = connectionAndDatasource.parentDataSource
        createConnection(dbSelector, dbIndex, failedDataSources);
        connection = getConnectionProxy().getConnectionAndDatasourceByDBSelectorID(dbIndex).connection;
        return connection;
    }

    /**
     * dbselector��֧������ֱ�����쳣
     * @param dbSelectorId
     * @param currentException
     * @throws SQLException
     */
    protected void validRetryable(String dbSelectorId, SQLException currentException,
                                  OperationDBType type) throws SQLException {
        ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
            .getConnectionAndDatasourceByDBSelectorID(dbSelectorId);

        if (!connectionAndDatasource.dbSelector.isSupportRetry(type)) {
            logger.warn("������������ԣ�dbSelectorId=" + dbSelectorId + ",type=" + type, currentException);
            throw currentException;
        }
    }

    /**
     * �Ӻ�ѡselector���Ƴ���ǰdatasource.
     * 
     * Ϊ�˱�֤���޸�dbSelector����selector����ԭ�е�����״̬��ÿ���Ƴ�ʱ�����ȸ���һ�� ������dbSelector�������д���
     * 
     * ����Ѿ�û�б�ѡ���Ϊһ��д�� ��ֱ���׳��쳣
     * 
     * @param exceptions
     * @return
     * @throws SQLException
     */
    /*public DBSelector removeCurrentDataSourceFromDbSelector(
    		 DBSelector dbSelector,
    		DataSource currentDataSource, RetringContext retringContext)
    		throws SQLException {
    	dbSelector = dbSelector.copyAndRemove(currentDataSource);
    	if (dbSelector == null) {
    		// ��ʾû�ж���Ŀ�ɹ�ѡ��
    		ExceptionUtils.throwSQLException(retringContext.getSqlExceptions(),
    				"getConnection", Collections.emptyList());
    	} else {
    		retringContext.addRetringTimes();
    	}
    	return dbSelector;
    }*/

    //	/**
    //	 * ��֤�Ƿ���Ҫ����
    //	 * 
    //	 * @param retringContext
    //	 * @param currentException
    //	 * @throws SQLException
    //	 */
    //	protected void validRetryable(RetringContext retringContext,
    //			SQLException currentException) throws SQLException {
    //		if(retringContext == null){
    //			ExceptionUtils.throwSQLException(currentException,
    //					"getConnection", Collections.emptyList());
    //		}
    //		if (!retringContext.isExceptionFatal(currentException)) {
    //			ExceptionUtils.throwSQLException(retringContext.getSqlExceptions(),
    //					"getConnection", Collections.emptyList());
    //		}
    //		if (!retringContext.isNeedRetry()) {
    //			// �����д����,��ô����Ҫ���ԣ�ֱ���׳�
    //			ExceptionUtils.throwSQLException(retringContext.getSqlExceptions(),
    //					"getConnection", Collections.emptyList());
    //		}
    //		// ������Դ����������ƣ�ֱ���״��ȥ
    //		if (reachMaxRetryableTimes(retringContext)) {
    //			ExceptionUtils.throwSQLException(retringContext.getSqlExceptions(),
    //					"getConnection", Collections.emptyList());
    //		}
    //	}

}
