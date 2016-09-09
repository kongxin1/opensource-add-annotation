/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.dispatcher.SqlDispatcher;
import com.alipay.zdal.common.OperationDBType;

public class RetryableTPreparedStatement extends ZdalPreparedStatement {
    private static final Logger logger = Logger.getLogger(RetryableTPreparedStatement.class);

    public RetryableTPreparedStatement(SqlDispatcher writeDispatcher, SqlDispatcher readDispatcher) {
        super(writeDispatcher, readDispatcher);
    }

    /*	
     * �淶û ˵����updateʱ����select��䡣
     * @Override
    	protected int executeUpdateAndCountAffectRows(String dbSelectorId,
    			String targetSql, RetringContext retringContext,
    			Connection connection, int rows) throws SQLException {
    		try{
    		return super.executeUpdateAndCountAffectRows(dbSelectorId, targetSql,
    				retringContext, connection, rows);
    		}catch (SQLException e) {
    			retringContext.addSQLException(e);
    			validRetryable(retringContext, e);
    			connection = tryToConnectToOtherAvaluableDataSource(dbSelectorId,
    					retringContext, e);
    			return executeUpdateAndCountAffectRows(dbSelectorId, targetSql, retringContext, connection, rows);
    		}
    	}*/
    @Override
    protected void executeQueryAndAddIntoCollection(
                                                    String dbSelectorId,
                                                    String targetSql,
                                                    Map<DataSource, SQLException> failedDataSources,
                                                    Connection connection,
                                                    List<ResultSet> actualResultSets)
                                                                                     throws SQLException {
        try {
            super.executeQueryAndAddIntoCollection(dbSelectorId, targetSql, failedDataSources,
                connection, actualResultSets);
        } catch (SQLException e) {
            logger.warn("zdal�����������״̬���ظ�ִ��executeQueryAndAddIntoCollection()����, targetSql="
                        + targetSql, e);
            validRetryable(dbSelectorId, e, OperationDBType.readFromDb);
            if (failedDataSources != null) {
                ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
                    .getConnectionAndDatasourceByDBSelectorID(dbSelectorId);
                failedDataSources.put(connectionAndDatasource.parentDataSource, e);
            } else {
                //����������ʱ����Ҫ���ԣ�failedDataSourcesΪnull ֵ��ֱ�ӽ��쳣�׳�
                //added by fanzeng.
                logger.warn("������failedDataSources=null, zdal��δ���������״̬,targetSql=" + targetSql);
                throw e;
            }
            //Ϊʲô�������ж��Ƿ�Ϊ��fatal���׳�����Ҫ�˺�����Ӱ���Ķ������
            connection = tryToConnectToOtherAvailableDataSource(dbSelectorId, failedDataSources);
            executeQueryAndAddIntoCollection(dbSelectorId, targetSql, failedDataSources,
                connection, actualResultSets);
        }

    }

    /**
     * д������
     */
    @Override
    protected int executeUpdateAndCountAffectRows(String dbSelectorId, String targetSql,
                                                  Map<DataSource, SQLException> failedDataSources,
                                                  Connection connection, int rows)
                                                                                  throws SQLException {
        try {
            rows = super.executeUpdateAndCountAffectRows(dbSelectorId, targetSql,
                failedDataSources, connection, rows);
        } catch (SQLException e) {
            //            logger.error("sqlִ��ʧ��,targetSql=" + targetSql, e);
            throw e;
            /* validRetryable(dbSelectorId, e, operationDBType.writeIntoDb);
             if (failedDataSources != null) {
                 ConnectionAndDatasource connectionAndDatasource = getConnectionProxy()
                     .getConnectionAndDatasourceByDBSelectorID(dbSelectorId);
                 failedDataSources.put(connectionAndDatasource.parentDataSource, e);
             } else {
                 //�������в���Ҫ���ԣ�failedDataSourcesΪnullֵ��ֱ�ӽ��쳣�׳�
                 //added by fanzeng.
                 logger.warn("������failedDataSources=null�� zdal��δ����д����״̬,targetSql=" + targetSql);
                 throw e;
             }
             //Ϊʲô�������ж��Ƿ�Ϊ��fatal���׳�����Ҫ�˺�����Ӱ���Ķ������
             connection = tryToConnectToOtherAvailableDataSource(dbSelectorId, failedDataSources);
             rows = executeUpdateAndCountAffectRows(dbSelectorId, targetSql, failedDataSources,
                 connection, rows);*/
        }
        return rows;
    }

}
