/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.jdbc.DBSelector.DataSourceTryer;
import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;
import com.alipay.zdal.client.util.ExceptionUtils;
import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.WeightRandom;
import com.alipay.zdal.common.exception.sqlexceptionwrapper.ZdalCommunicationException;
import com.alipay.zdal.common.jdbc.sorter.ExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.MySQLExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.OracleExceptionSorter;

/**
 * ������DataSourceHolder��̬�࣬��Ҫ���ڹ�������Դ���޳���ָ�
 * @author zhaofeng.wang
 * @version $Id: DataSourceHolder.java,v 0.1 2011-4-14 ����04:00:55 zhaofeng.wang Exp $
 */
public class DataSourceHolder {

    private static final Logger logger              = Logger
                                                        .getLogger(Constants.CONFIG_LOG_NAME_LOGNAME);

    private final DataSource    ds;

    /**
     * �������������ڿ���ֻ����һ��ҵ���߳�ȥ���ԣ�
     */
    private final ReentrantLock lock                = new ReentrantLock();

    /**
     * ����Դ�Ƿ���ã�Ĭ�Ͽ���
     */
    private volatile boolean    isNotAvailable      = false;
    /**
     * �ϴ�����ʱ��
     */
    private volatile long       lastRetryTime       = 0;
    /**
     * �쳣����
     */
    private volatile int        exceptionTimes      = 0;
    /**
     * ��һ�β����쳣��ʱ�䣬��λ����
     */
    private volatile long       firstExceptionTime  = 0;
    /**
     * ���Թ���db��ʱ������Ĭ��ֵ��Ϊ2s,��λ����
     */
    private final int           retryBadDbInterval  = 2000;

    /**
     * ��λʱ��Σ�Ĭ��Ϊ1���ӣ�����ͳ��ʱ�����ĳ��db���쳣�Ĵ�������λ����
     */
    private final int           timeInterval        = 60000;
    /**
     * ��λʱ���������쳣�Ĵ���������������ֵ�㽫����Դ��Ϊ������
     */
    private final int           allowExceptionTimes = 10;

    /**
     * ���캯��
     * @param ds
     */
    public DataSourceHolder(DataSource ds) {
        this.ds = ds;
    }

    public DataSource getDs() {
        return ds;
    }

    /**
     * ��ѡ�е�����Դ�Ͻ��в��������ݸ�����ԴĿǰ�Ŀ����Խ����ж����ĸ���֧�����ߵ�������Ҫ���������㣺
     * ��1���������ԴĿǰ�����ã�ֻ����ҵ���߳����ԣ�
     * ��2�������ڴ����쳣�Ļ��ڴ���һ��������Ҫ�����Ŀǰ�����ã����̳߳�����ʧ��ʱ���Ͳ���ͳ���쳣�����������Ŀǰ���ã������쳣ʱ��Ҫ
     *      ͳ�Ƶ�λʱ���ڵ��쳣����
     * @param <T>
     * @param operationType  ����������д����
     * @param weightRandom    ��������Դ��Ȩ��
     * @param dataSourceHolders ��װ��������Դ
     * @param failedDataSources  ʧ�ܵ�datasource
     * @param tryer 
     * @param exceptions
     * @param excludeKeys
     * @param exceptionSorter
     * @param name
     * @param args
     * @return
     * @throws SQLException
     */
    public <T> T tryOnSelectedDataSource(DB_OPERATION_TYPE operationType,
                                         WeightRandom weightRandom,
                                         Map<String, DataSourceHolder> dataSourceHolders,
                                         Map<DataSource, SQLException> failedDataSources,
                                         DataSourceTryer<T> tryer, List<SQLException> exceptions,
                                         List<String> excludeKeys, ExceptionSorter exceptionSorter,
                                         String name, Object... args) throws SQLException {
        T t = null;
        if (isNotAvailable) {
            t = tryOnFailedDataSource(failedDataSources, tryer, exceptions, excludeKeys,
                exceptionSorter, name, args);
        } else {
            t = tryOnAvailableDataSource(operationType, weightRandom, dataSourceHolders,
                failedDataSources, tryer, exceptions, excludeKeys, exceptionSorter, name, args);
        }
        return t;

    }

    /**
     * �������Դ�Ѿ������Ϊ�����ã���ֻ����һ���߳̽��г��ԣ����Գɹ�ֱ�ӱ��Ϊ���ã������ų���������Դ��ȥѰ�������Ŀ�������Դ��
     * ����Ѿ���ҵ���߳��ڳ��Ը�����Դ����������ʱ���������ò�����������ô�����߳�ֱ���ų���������Դ��ȥѰ������������Դ
     * @param <T>
     * @param failedDataSources
     * @param tryer
     * @param exceptions
     * @param excludeKeys
     * @param exceptionSorter
     * @param name
     * @param args
     * @return
     * @throws SQLException
     */
    public <T> T tryOnFailedDataSource(Map<DataSource, SQLException> failedDataSources,
                                       DataSourceTryer<T> tryer, List<SQLException> exceptions,
                                       List<String> excludeKeys, ExceptionSorter exceptionSorter,
                                       String name, Object... args) throws SQLException {
        T t = null;
        boolean toTry = System.currentTimeMillis() - lastRetryTime > retryBadDbInterval;
        //ÿ������룬ֻ����һ��ҵ���̼߳���ʹ���������Դ��
        if (toTry && lock.tryLock()) {
            try {
                logger.warn("�߳�" + Thread.currentThread().getName() + "��"
                            + getCurrentDateTime(null) + "��������Դ" + name + "�ĵ��߳�����״̬��");
                //��һ�����������ݿ����ӣ�������ɹ��ͱ�ʾ����Դ�������ã�ֱ�ӷ��أ�
                boolean isSussessful = tryToConnectDataBase(exceptionSorter, name);
                if (!isSussessful) {
                    excludeKeys.add(name);
                    return t;
                }
                Long beginTime = System.currentTimeMillis();
                t = tryer.tryOnDataSource(ds, name, args);
                logger.warn("���߳�" + Thread.currentThread().getName() + "ȥ��ȡ������Դ����" + name
                            + "�ɹ�����ʱΪ��" + (System.currentTimeMillis() - beginTime));
                //����߳�ִ�гɹ�����Ϊ���ã���������Դ���Ϊ����
                isNotAvailable = false;
                exceptionTimes = 0;
                logger.warn("����Դ" + name + "��" + getCurrentDateTime(null) + "�Ѿ��ָ������Ϊ���ã�");
            } catch (SQLException e) {
                exceptions.add(e);
                boolean isFatal = exceptionSorter.isExceptionFatal(e);
                if (!isFatal || failedDataSources == null) {
                    logger.warn("�쳣������: " + e.getErrorCode() + " �������ݿⲻ�����쳣��Ҫ�����ԣ�ֱ���׳�.isFatal= "
                                + isFatal, e);
                    return tryer.onSQLException(exceptions, exceptionSorter, args);
                }
                excludeKeys.add(name);
                logger.warn("���߳�" + Thread.currentThread().getName() + "���Թ�������Դ" + name
                            + "ʱʧ�ܣ�����ȥѰ���������õ�����Դ��");
            } finally {
                lastRetryTime = System.currentTimeMillis();
                lock.unlock();
            }
        } else {
            //�����ϳ��Ը�����Դ��������������Χforѭ��ȥ��Ѱ����������Դ
            logger.warn("�߳�" + Thread.currentThread().getName() + "��" + getCurrentDateTime(null)
                        + "ʱ����㳢�Թ�������Դ" + name + "ʱʧ�ܣ������߳����ڷ��ʻ��߲�����2s��ʱ������");
            excludeKeys.add(name);
        }
        return t;
    }

    /**
     * �ڵ��߳����Ե�ʱ��ȡ�����Ӻ����������ݿ⽨�����ӣ�
     */
    public boolean tryToConnectDataBase(ExceptionSorter exceptionSorter, String name) {
        Long beginTime = System.currentTimeMillis();
        boolean isSussessful = true;
        String sql = null;
        if (exceptionSorter instanceof MySQLExceptionSorter) {
            sql = "select 'x' ";
        } else if (exceptionSorter instanceof OracleExceptionSorter) {
            sql = "select * from dual";
        } else {
            logger.error("���ݿ����ͳ������������ã�");
            isSussessful = false;
            return isSussessful;
        }
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            logger.warn("Get the connection success,time="
                        + getCurrentDateTime(System.currentTimeMillis()));
            stmt = con.createStatement();
            logger.warn("Create the statement success,time="
                        + getCurrentDateTime(System.currentTimeMillis()));
            stmt.executeQuery(sql);
            logger.warn("���߳�" + Thread.currentThread().getName() + "����sql=" + sql + "������У�����Ӹ�����Դ"
                        + name + "�ɹ�����ʱΪ:" + (System.currentTimeMillis() - beginTime));
        } catch (SQLException e) {
            logger.error("���߳�" + Thread.currentThread().getName() + "����sql=" + sql + "������У�����Ӹ�����Դ"
                         + name + "ʧ��,��ʱΪ:" + (System.currentTimeMillis() - beginTime) + "ms", e);
            isSussessful = false;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                logger.warn("close the resource has an error", e);
            }
        }
        return isSussessful;
    }

    /**
     * ���������Դ���ã�ֱ���ڸ�����Դ�Ͻ��в�������������쳣������catch������������Ӧ�Ĵ����������̻�����tryOnFailedDataSource�������ƣ�
     * Ψһ��ͬ�������ʱ����Ҫ�����쳣�������ۼӣ����õķ�ʽ�ǣ������λʱ���ڷ����쳣�Ĵ��������������õ���ֵ������Ŀǰ������Դ����
     * ����һ�����򽫸�����Դ��Ϊ�����ã������д�⣬��ʹֻ��һ����Ҳ����Ϊ������
     * @param <T>
     * @param failedDataSources
     * @param tryer
     * @param exceptions
     * @param excludeKeys
     * @param exceptionSorter
     * @param name
     * @param args
     * @return
     * @throws SQLException
     */
    public <T> T tryOnAvailableDataSource(DB_OPERATION_TYPE operationType,
                                          WeightRandom weightRandom,
                                          Map<String, DataSourceHolder> dataSourceHolders,
                                          Map<DataSource, SQLException> failedDataSources,
                                          DataSourceTryer<T> tryer, List<SQLException> exceptions,
                                          List<String> excludeKeys,
                                          ExceptionSorter exceptionSorter, String name,
                                          Object... args) throws SQLException {
        T t = null;
        try {
            t = tryer.tryOnDataSource(ds, name, args);
        } catch (SQLException e) {
            exceptions.add(e);
            boolean isFatal = exceptionSorter.isExceptionFatal(e);
            if (!isFatal || failedDataSources == null) {
                logger.warn("�쳣������: " + e.getErrorCode() + " �������ݿⲻ�����쳣��Ҫ�����ԣ�ֱ���׳�.isFatal= "
                            + isFatal, e);
                return tryer.onSQLException(exceptions, exceptionSorter, args);
            }
            logger.error("�߳�" + Thread.currentThread().getName()
                         + "�ڷ���dataSourceHolder����Դ�������ϣ�name=" + name, e);
            excludeKeys.add(name);
            //�˴�����������Դ�ų������õĲ����Ǹ��ݵ�λʱ��������ʧ�ܵĴ����������ĳ����ֵ��������Դ������ʣ��һ��ʱ������������Դ���;����Ƿ��߳��� 
            calcFailedDSExceptionTimes(name, operationType, weightRandom, dataSourceHolders);
        }
        return t;

    }

    /**
     * �ⲿ���������������ֱ��Ǹ�������Դ��Ȩ��weightRandom���Լ���װ��������ԴdataSourceHolders��
     * ��������һ������ͳ�Ƶ�ǰ���õ�����Դ����ʱʹ��
     * �ж�һ������Դ���õ�ǰ�������ǣ�Ȩ�ش���0����isNotAvailable��ֵΪfalse��
     */

    /**
     * ���㵱ǰ����Դ���쳣����
     * ��1��ͳ�Ƶ�ǰ���õ�����Դ����������operationType���ж���д�⻹�Ƕ��⣻
     *      ����Ƕ��⣬Ŀǰ��ֻʣ��һ����������Դ�������κδ��������д�⣬��û�д����ƣ�
     * ��2���ڿ��߳�������£������λʱ�䣨����Ϊ1���ӣ����쳣��������ĳ����ֵ������Ϊ20�Σ����򽫸�����Դ���Ϊ�����ã�
     * @param name
     * @param operationType
     * @param weightRandom
     * @param dataSourceHolders
     */
    public synchronized void calcFailedDSExceptionTimes(
                                                        String name,
                                                        DB_OPERATION_TYPE operationType,
                                                        WeightRandom weightRandom,
                                                        Map<String, DataSourceHolder> dataSourceHolders) {
        //���������Դ�Ѿ����Ϊ�����ã���ֱ�ӷ���
        if (isNotAvailable) {
            logger.warn("����Դ" + name + "�Ѿ��������ˣ�������ͳ���쳣�����ˣ�");
            return;
        }
        //ͳ�Ƶ�ǰ���õ�����Դ������
        int availableDSNumber = 0;
        for (Map.Entry<String, DataSourceHolder> entry : dataSourceHolders.entrySet()) {
            int weight = weightRandom.getWeightByKey(entry.getKey());
            //����Դ��Ȩ�ش���0�����Ҹ�����Դ����
            if ((weight > 0) && !(entry.getValue().isNotAvailable)) {
                availableDSNumber++;
            }
            logger.warn("The weight of  key=" + entry.getKey() + ",isNotAvailable="
                        + isNotAvailable);
        }

        logger.warn("Ŀǰ���õ�����Դ�ĸ���Ϊ" + availableDSNumber + ",operationType=" + operationType
                    + ",name=" + name);
        if (availableDSNumber <= 1 && (operationType == DB_OPERATION_TYPE.READ_FROM_DB)) {
            logger.error("������Դ" + name + "�Ѿ������쳣����Ŀǰ���õĶ�����Դ������ʣ��" + availableDSNumber + "�����ʲ��߳���");
            return;
        }

        if (this.exceptionTimes == 0) {
            this.firstExceptionTime = System.currentTimeMillis();
        }
        long currentTime = System.currentTimeMillis();
        //С��ָ��ʱ�������ۼ��쳣����������쳣��������ĳ����ֵ�ͽ�������Դ��Ϊ�����ã������������¼���
        if (currentTime - this.firstExceptionTime <= timeInterval) {
            ++exceptionTimes;
            logger.error("����Դ" + name + "��λʱ���ڵ�" + exceptionTimes + "���쳣����ǰʱ�䣺"
                         + getCurrentDateTime(currentTime) + "���״��쳣ʱ�䣺"
                         + getCurrentDateTime(firstExceptionTime) + "��ʱ����Ϊ��"
                         + (currentTime - firstExceptionTime) + "ms.");
            if (exceptionTimes >= allowExceptionTimes) {
                this.isNotAvailable = true;
                logger.error("����Դ" + name + "��ʱ��" + getCurrentDateTime(null) + "���߳���Ŀǰ"
                             + operationType + "���Ϳ��õ�����Դ����Ϊ��" + (availableDSNumber - 1));
            }
        } else {
            logger.warn("ͳ���쳣����������λʱ����,�ϴε�λʱ�������쳣����Ϊ" + exceptionTimes + "��,���ڿ�ʼ���¼�����");
            this.exceptionTimes = 0;

        }
    }

    /**
     * ���쳣���д�����׳���
     * @param exceptions
     * @param exceptionSorter
     * @throws SQLException
     */
    public void throwZdalCommnicationException(List<SQLException> exceptions,
                                               ExceptionSorter exceptionSorter) throws SQLException {

        int size = exceptions.size();
        if (size <= 0) {
            throw new IllegalArgumentException("should not be here!");
        } else {
            //��������µĴ���
            int lastElementIndex = size - 1;
            //ȡ���һ��exception.�ж��Ƿ������ݿⲻ�����쳣.����ǣ�ȥ�����һ���쳣������ͷ�쳣��װΪZdalCommunicationException�׳�
            SQLException lastSQLException = exceptions.get(lastElementIndex);
            if (exceptionSorter.isExceptionFatal(lastSQLException)) {
                exceptions.remove(lastElementIndex);
                exceptions.add(0, new ZdalCommunicationException("zdal communicationException:",
                    lastSQLException));
            }
        }
        ExceptionUtils.throwSQLException(exceptions, null, null);

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

    //    /**
    //     * �ر�statement
    //     * 
    //     * @param stmt
    //     */
    //    private void closeStatement(Statement stmt) {
    //        if (stmt != null) {
    //            try {
    //                stmt.close();
    //            } catch (SQLException e) {
    //                logger.warn("Could not close JDBC Statement", e);
    //            } catch (Throwable e) {
    //                logger.warn("Unexpected exception on closing JDBC Statement", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * �ر�����
    //     * 
    //     * @param conn
    //     */
    //    private void closeConnection(Connection conn) {
    //        if (conn != null) {
    //            try {
    //                conn.close();
    //            } catch (SQLException e) {
    //                logger.warn("Could not close JDBC Connection", e);
    //            } catch (Throwable e) {
    //                logger.warn("Unexpected exception on closing JDBC Connection", e);
    //            }
    //        }
    //    }

}
