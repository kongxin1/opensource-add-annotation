/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.jdbc.DBSelector.DataSourceTryer;
import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;

public class PriorityGroupsDataSources {
    private static final Logger   logger              = Logger
                                                          .getLogger(PriorityGroupsDataSources.class);
    private final EquityDbManager equityDbManager;

    /**
     * �������������ڿ���ֻ����һ��ҵ���߳�ȥ���ԣ�
     */
    private final ReentrantLock   lock                = new ReentrantLock();

    /**
     * ����Դ�Ƿ���ã�Ĭ�Ͽ���
     */
    private volatile boolean      isNotAvailable      = false;
    /**
     * �ϴ�����ʱ��
     */
    private volatile long         lastRetryTime       = 0;
    /**
     * �쳣����
     */
    private volatile int          exceptionTimes      = 0;
    /**
     * ��һ�β����쳣��ʱ�䣬��λ����
     */
    private volatile long         firstExceptionTime  = 0;
    /**
     * ���Թ���db��ʱ������Ĭ��ֵ��Ϊ2s,��λ����
     */
    private final int             retryBadDbInterval  = 2000;

    /**
     * ��λʱ��Σ�Ĭ��Ϊ1���ӣ�����ͳ��ʱ�����ĳ��db���쳣�Ĵ�������λ����
     */
    private final int             timeInterval        = 60000;
    /**
     * ��λʱ���������쳣�Ĵ���������������ֵ�㽫����Դ��Ϊ������
     */
    private final int             allowExceptionTimes = 20;

    PriorityGroupsDataSources(EquityDbManager equityDbManager) {
        this.equityDbManager = equityDbManager;
    }

    /**
     * �����ȼ����з��飬����p0��p1�ȣ����ĳ�����ȼ��ڵ�����Դ���������ˣ����׳�NoMoreDataSourceException
     * Ȼ����ݵ�λʱ�����׳����쳣�Ĵ����Ƿ񳬹�ĳ����ֵ�������Ƿ񽫸����ȼ�������Ϊ�����ã���isNotAvailable=true
     * @param <T>
     * @param failedDataSources
     * @param tryer
     * @param times
     * @param operationType
     * @param i
     * @param args
     * @return
     * @throws SQLException
     */
    public <T> T tryExecute(Map<DataSource, SQLException> failedDataSources,
                            DataSourceTryer<T> tryer, int times, DB_OPERATION_TYPE operationType,
                            int i, Object... args) throws SQLException {
        T t = null;
        //���������
        if (isNotAvailable) {
            t = tryOnFailedPriorityGroupDataSource(failedDataSources, tryer, times, operationType,
                i, args);
        } else {
            t = tryOnAvailablePriorityGroupDataSource(failedDataSources, tryer, times,
                operationType, i, args);
        }
        return t;
    }

    /**
     * �ڲ����õ����ȼ��Ͻ��в���
     * ֻ�����߳̽��뵽�����ȼ��������ԣ�������Գɹ��򽫸����ȼ���Ϊ���ã����ʧ�ܾ��׳��쳣����Χ����ס�쳣���᳢����һ�����ȼ���
     * ��������̲߳���������״̬���򲻽������ԣ�ֱ���׳��쳣����Χ�����쳣���漴������һ�����ȼ���
     * 
     * @param <T>
     * @param failedDataSources
     * @param tryer
     * @param times
     * @param operationType
     * @param i
     * @param args
     * @return
     * @throws SQLException 
     * @throws SQLException 
     */
    public <T> T tryOnFailedPriorityGroupDataSource(
                                                    Map<DataSource, SQLException> failedDataSources,
                                                    DataSourceTryer<T> tryer, int times,
                                                    DB_OPERATION_TYPE operationType, int i,
                                                    Object... args) throws SQLException {
        T t = null;
        boolean toTry = System.currentTimeMillis() - lastRetryTime > retryBadDbInterval;
        //ÿ������룬ֻ����һ��ҵ���̼߳���ʹ��������ȼ�������Դ��
        if (toTry && lock.tryLock()) {
            try {
                logger.warn("�߳�" + Thread.currentThread().getName() + "��"
                            + getCurrentDateTime(null) + "�������ȼ�" + i + "�ĵ��߳�����״̬��");
                Long beginTime = System.currentTimeMillis();
                t = equityDbManager
                    .tryExecute(failedDataSources, tryer, times, operationType, args);
                logger.warn("���߳�" + Thread.currentThread().getName() + "ȥ��ȡ�����ȼ�p" + i + "�ɹ�����ʱΪ��"
                            + (System.currentTimeMillis() - beginTime));
                this.isNotAvailable = false;
                this.exceptionTimes = 0;
                logger.warn("����Դ���ȼ�p" + i + "��" + getCurrentDateTime(null) + "�Ѿ��ָ������Ϊ���ã�");
            } catch (NoMoreDataSourceException e) {
                logger.error("���߳��������ȼ� p" + i + "ʧ�ܣ�����ȥѰ������һ�����ȼ�������Դ��", e);
                throw e;
            } finally {
                lastRetryTime = System.currentTimeMillis();
                lock.unlock();
            }
        } else {
            //�����ϳ��Ը����ȼ���������ֱ���׳��쳣��������Χforѭ��ȥ������һ�����ȼ�
            logger.warn("���ȼ�p" + i + "���ڱ������̷߳��ʣ����߲�����2s��ʱ������");
            throw new NoMoreDataSourceException("No more dataSource for p" + i);
        }
        return t;
    }

    /**
     * �ڿ��õ����ȼ��Ͻ��в�����
     * ��������ȼ������쳣�������ͳ�Ƶ�λʱ���ڵ��쳣�������������ָ������ֵ���򽫸����ȼ���Ϊ������
     * ͳ�����쳣�������쳣�׳�����Χ�Ჶ����쳣���漴ѡ����һ�����ȼ�������Դ���в���
     * @param <T>
     * @param failedDataSources
     * @param tryer
     * @param times
     * @param operationType
     * @param i
     * @param args
     * @return
     * @throws SQLException
     */
    public <T> T tryOnAvailablePriorityGroupDataSource(
                                                       Map<DataSource, SQLException> failedDataSources,
                                                       DataSourceTryer<T> tryer, int times,
                                                       DB_OPERATION_TYPE operationType, int i,
                                                       Object... args) throws SQLException {
        T t = null;
        try {
            t = equityDbManager.tryExecute(failedDataSources, tryer, times, operationType, args);
        } catch (NoMoreDataSourceException e) {
            calcFailedDSExceptionTimes(i);
            throw e;
        } catch (IllegalStateException e) {//����ڱ������Ҳ��������׳�NoMoreDataSourceException�쳣�����ڴ���һ��EquityDbManager�м���ִ��.
            throw new NoMoreDataSourceException(e.getMessage());
        }
        return t;
    }

    /**
     * ͳ�����ȼ�pi���쳣����
     * С��ָ��ʱ�������ۼ��쳣����������쳣��������ĳ����ֵ�͸����ȼ�����Ϊ�����ã������������¼���
     * @param i
     */
    public synchronized void calcFailedDSExceptionTimes(int i) {
        //ͳ�Ƹ÷����ڵĵ�λʱ�����쳣�Ĵ���
        if (this.exceptionTimes == 0) {
            this.firstExceptionTime = System.currentTimeMillis();
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.firstExceptionTime <= timeInterval) {
            ++exceptionTimes;
            logger.error("���ȼ�p" + i + "��λʱ���ڵ�" + exceptionTimes + "���쳣����ǰʱ�䣺"
                         + getCurrentDateTime(currentTime) + "���״��쳣ʱ�䣺"
                         + getCurrentDateTime(firstExceptionTime) + "��ʱ����Ϊ��"
                         + (currentTime - firstExceptionTime) + "ms.");
            if (exceptionTimes >= allowExceptionTimes) {
                this.isNotAvailable = true;
                logger.error("���ȼ�p" + i + "��ʱ��" + getCurrentDateTime(null) + "���߳���");
            }
        } else {
            logger.warn("ͳ���쳣����������λʱ����,�ϴε�λʱ�������쳣����Ϊ" + exceptionTimes + "��,���ڿ�ʼ���¼�����");
            this.exceptionTimes = 0;
        }
    }

    /**
     * ��ȡ��ǰ��ʱ��ĸ�ʽ���ַ���
     */
    public String getCurrentDateTime(Long time) {
        java.util.Date now;
        if (time != null) {
            now = new java.util.Date(time);
        } else {
            now = new java.util.Date();
        }
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(now);
    }

    public EquityDbManager getEquityDbManager() {
        return equityDbManager;
    }
}
