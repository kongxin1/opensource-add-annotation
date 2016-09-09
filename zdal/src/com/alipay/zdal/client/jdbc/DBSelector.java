/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;
import com.alipay.zdal.client.util.ExceptionUtils;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.OperationDBType;
import com.alipay.zdal.common.jdbc.sorter.ExceptionSorter;

/**
 * �Ե����ݿ�ѡ������
 * ��������ȫ��ͬ��һ�����ѡ��һ����
 * ���ڶ�HA/RAC���,���������ȡһ�����Ĳ���
 * 
 */
public interface DBSelector {

    /**
     * @return ���ظ�Selector�ı�ʶ
     */
    String getId();

    /**
     * added by fanzeng.
     * ��һ��Եȿ���ѡ��Ŀ������.
     * ����������һ��Եȿ⣬��IdΪ dbs_w, ���� dps_w_0, dps_w_1...���������ds��
     * ����֪����Id��ֻ��֪��������һ��dbSelector��ִ�еģ�
     * ����ҵ����Ҫ֪��sql�������ĸ�db��ִ�е�
     */
    void setSelectedDSName(String name);

    /**
     * added by fanzeng.
     *��ִ��sql֮ǰ����ȡ֮ǰ���õ�selectedDSName
     *Ȼ�����threadLocal����ҵ����ȡ 
     */
    String getSelectedDSName();

    /**
     * �Ե����ݿ�ѡ������
     * ��������ȫ��ͬ��һ�����ѡ��һ����
     * ���ڶ�HA/RAC���,���������ȡһ�����Ĳ���
     */

    DataSource select();

    /**
     * ����Ȩ��
     * @param weightMap 
     *    keyΪȨ�ؼ�����ʹ���ߺ;���ʵ�־���
     *    valueΪȨ��ֵ
     */
    void setWeight(Map<String, Integer> weightMap);

    /**
     * �������ݿ����ͣ�Ŀǰֻ����ѡ��exceptionSorter 
     */
    void setDbType(DBType dbType);

    /**
     * ��ѡ�񵽵�DataSource�ʹ����args������ִ��
     *    tryer.tryOnDataSource(DataSource ds, Object... args), ÿ��ѡ��DataSource���ų��ϴ�����ʧ�ܵ�
     * ֱ���ﵽָ�������Դ��������ڼ��׳������ݿⲻ�����쳣
     * 
     * �׳��쳣�������������쳣�б��������args������
     *    tryer.onSQLException(List<SQLException> exceptions, Object... args)
     * 
     * @param tryer
     * @param times
     * @param args
     * @throws SQLException
     */
    <T> T tryExecute(DataSourceTryer<T> tryer, int times, DB_OPERATION_TYPE operationType,
                     Object... args) throws SQLException;

    /**
     * @param failedDataSources: �ڵ��ø÷���ǰ���Ѿ���֪�Թ�ʧ�ܵ�DataSource�Ͷ�Ӧ��SQLException
     * �������������ԭ������Ϊ���ݿ��������ΪgetConnection/createStatement/execute����������������һ�����try catch��
     * failedDataSources == null ��ʾ����Ҫ���ԣ������κ��쳣ֱ���׳�������д���ϵĲ���
     */
    <T> T tryExecute(Map<DataSource, SQLException> failedDataSources, DataSourceTryer<T> tryer,
                     int times, DB_OPERATION_TYPE operationType, Object... args)
                                                                                throws SQLException;

    /**
     * �Ƿ�֧������
     * @return
     */
    boolean isSupportRetry(OperationDBType type);

    //	/**
    //	 * ����ǰ����Դ���鿽��һ�ݣ�Ȼ���½�һ���µ����ѡ������
    //	 * 
    //	 * �������ΪDBSelector��ʾ�ڲ����п��Խ���ѡ�������Դ��
    //	 * 
    //	 * ���������null ���ʾ�ڲ�û�пɽ���ѡ�������Դ
    //	 * 
    //	 * @param removedDataSource
    //	 * @return
    //	 */
    //	DBSelector copyAndRemove(DataSource removedDataSource);

    /**
     * ��DBSelector���������Դ������ִ�в����Ļص��ӿ�
     */
    public static interface DataSourceTryer<T> {
        T tryOnDataSource(DataSource ds, String name, Object... args) throws SQLException;

        /**
         * tryExecute�����Ե���tryOnDataSource���������ݿⲻ�����쳣�����������Դ���ʱ������ø÷���
         * @param exceptions ��������ʧ���׳����쳣��
         *    ���һ���쳣���������ݿⲻ���õ��쳣��Ҳ��������ͨ��SQL�쳣
         *    ���һ��֮ǰ���쳣�����ݿⲻ���õ��쳣
         * @param exceptionSorter ��ǰ�õ����ж�Exception���͵ķ�����
         * @param args ��tryOnDataSourceʱ��args��ͬ�������û�����tryExecuteʱ�����arg
         * @return �û���ʵ���ߣ������Ƿ񷵻�ʲôֵ
         * @throws SQLException
         */
        T onSQLException(List<SQLException> exceptions, ExceptionSorter exceptionSorter,
                         Object... args) throws SQLException;
    }

    /**
     * DataSourceTryer.onSQLException ֱ���׳��쳣
     */
    public static abstract class AbstractDataSourceTryer<T> implements DataSourceTryer<T> {
        public T onSQLException(List<SQLException> exceptions, ExceptionSorter exceptionSorter,
                                Object... args) throws SQLException {
            ExceptionUtils.throwSQLException(exceptions, null, null);
            return null;
        }
    }

    /*public class RetringContext{
    	public Map<DataSource,SQLException> failed;
    	public boolean needRetry;
    }*/
}
