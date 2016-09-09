/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.datasource;

import org.apache.log4j.Logger;

import com.alipay.zdal.datasource.resource.adapter.jdbc.local.LocalTxDataSource;
import com.alipay.zdal.datasource.resource.connectionmanager.CachedConnectionManager;
import com.alipay.zdal.datasource.tm.TxManager;

/**
 *  ����Դ���������࣬�ṩ����Դ�Ĵ���������
 * 
 * @author liangjie.li
 * 
 */
public final class ZDataSourceFactory {

    private static final TxManager               defaultTransactionManager      = TxManager
                                                                                    .getInstance();
    private static final Logger                  logger                         = Logger
                                                                                    .getLogger(ZDataSourceFactory.class);
    private static final CachedConnectionManager defaultCachedConnectionManager = new CachedConnectionManager();

    /**
     *  �����µ�����Դ
     * 
     * @param dataSourceDO
     * @return com.alipay.zdatasource.resource.adapter.jdbc.local.LocalTxDataSource
     * @throws Exception
     */
    public static LocalTxDataSource createLocalTxDataSource(LocalTxDataSourceDO dataSourceDO,
                                                            ZDataSource zdatasource)
                                                                                    throws Exception {
        return ZDataSourceFactory.createLocalTxDataSource(dataSourceDO, null, null, zdatasource);
    }

    /**
     *  �����µ�����Դ
     * 
     * @param dataSourceDO
     * @param transactionManager
     * @param cachedConnectionManager
     * @param configRoot
     * @return com.alipay.zdatasource.resource.adapter.jdbc.local.LocalTxDataSource
     * @throws Exception
     */
    public static LocalTxDataSource createLocalTxDataSource(
                                                            LocalTxDataSourceDO dataSourceDO,
                                                            TxManager transactionManager,
                                                            CachedConnectionManager cachedConnectionManager,
                                                            ZDataSource zdatasource)
                                                                                    throws Exception {
        if (null == dataSourceDO) {
            throw new Exception("dataSource config is Empty!");
        }
        LocalTxDataSource localTxDataSource = new LocalTxDataSource();
        // �������ӻ�������������������ʹ�ø����ģ����ûָ����Ĭ�ϸ�һ��
        if (null != cachedConnectionManager) {
            localTxDataSource.setCachedConnectionManager(cachedConnectionManager);
        } else {
            localTxDataSource.setCachedConnectionManager(defaultCachedConnectionManager);
        }
        // ������������������������ʹ�ø����ģ����û����Ĭ�ϸ��Ը�
        if (null != transactionManager) {
            localTxDataSource.setTransactionManager(transactionManager);
        } else {
            localTxDataSource.setTransactionManager(defaultTransactionManager);
        }

        // �������Դ
        fillLocalTxDataSource(dataSourceDO, localTxDataSource);

        // ��ʼ������Դ
        localTxDataSource.init(zdatasource);

        return localTxDataSource;
    }

    /**
     * 
     * 
     * @param dataSourceDO
     * @param localTxDataSource
     * @param configRoot
     * @throws Exception
     */
    public static void fillLocalTxDataSource(LocalTxDataSourceDO dataSourceDO,
                                             LocalTxDataSource localTxDataSource) throws Exception {
        if (null == dataSourceDO) {
            throw new RuntimeException("dataSource config is Empty!");
        }

        localTxDataSource.setName(dataSourceDO.getDsName());
        localTxDataSource.setBackgroundValidation(dataSourceDO.isBackgroundValidation());
        localTxDataSource.setBackGroundValidationMinutes(dataSourceDO
            .getBackgroundValidationMinutes());
        localTxDataSource.setBlockingTimeoutMillis(dataSourceDO.getBlockingTimeoutMillis());
        localTxDataSource.setCheckValidConnectionSQL(dataSourceDO.getCheckValidConnectionSQL());
        localTxDataSource.setConnectionProperties(dataSourceDO.getConnectionProperties());
        localTxDataSource.setConnectionURL(dataSourceDO.getConnectionURL());
        localTxDataSource.setDriverClass(dataSourceDO.getDriverClass());
        localTxDataSource.setExceptionSorterClassName(dataSourceDO.getExceptionSorterClassName());
        localTxDataSource.setIdleTimeoutMinutes(dataSourceDO.getIdleTimeoutMinutes());
        localTxDataSource.setMaxSize(dataSourceDO.getMaxPoolSize());
        localTxDataSource.setMinSize(dataSourceDO.getMinPoolSize());
        localTxDataSource.setNewConnectionSQL(dataSourceDO.getNewConnectionSQL());
        localTxDataSource.setNoTxSeparatePools(dataSourceDO.isNoTxSeparatePools());
        localTxDataSource.setPassword(dataSourceDO.getPassWord());
        localTxDataSource.setEncPassword(dataSourceDO.getEncPassword());
        localTxDataSource.setPrefill(dataSourceDO.isPrefill());
        localTxDataSource.setPreparedStatementCacheSize(dataSourceDO
            .getPreparedStatementCacheSize());
        localTxDataSource.setQueryTimeout(dataSourceDO.getQueryTimeout());
        localTxDataSource.setSharePreparedStatements(dataSourceDO.isSharePreparedStatements());
        localTxDataSource.setTrackStatements(dataSourceDO.getTrackStatements());
        localTxDataSource.setTransactionIsolation(dataSourceDO.getTransactionIsolation());
        localTxDataSource.setTxQueryTimeout(dataSourceDO.isTxQueryTimeout());
        localTxDataSource.setUseFastFail(dataSourceDO.isUseFastFail());
        localTxDataSource.setUserName(dataSourceDO.getUserName());
        localTxDataSource.setValidateOnMatch(dataSourceDO.isValidateOnMatch());
        localTxDataSource.setValidConnectionCheckerClassName(dataSourceDO
            .getValidConnectionCheckerClassName());
        localTxDataSource.setCriteria(dataSourceDO.getCriteria());
    }

    /**
     * ���ٵ�ǰ������Դ
     * 
     * @param localTxDataSource
     * @throws Exception
     */
    public static void destroy(LocalTxDataSource localTxDataSource) throws Exception {
        if (null != localTxDataSource) {
            localTxDataSource.destroy();
            logger.warn("�Ѿ���������Դ �� " + localTxDataSource.getBeanName());
        }
    }

    private ZDataSourceFactory() {
    }
}
