/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;
import com.alipay.zdal.common.OperationDBType;
import com.alipay.zdal.common.WeightRandom;
import com.alipay.zdal.common.jdbc.sorter.ExceptionSorter;

/**
 * �����ȼ�ѡ���selector
 * 
 * ÿ��ѡ��ֻ�����ȼ���ߵ�һ��DB��ѡ�����������ã��ż�������һ�����ȼ���DB����ѡ��
 * 
 * ���ȼ���ͬ��DB�������ѡ��
 * 
 * ԭʼ����TCҪ����ÿ��dbgroup�����ȶ����⣬�����ⲻ����ʱ���Զ������� 
 * ��չ����һ���౸��������������⡣�����ⶼ������ʱ���Ŷ�����
 * 
 * Ϊ�˷��㴦��ͽӿ�һ�£�������Ҫ�� 
 * 1. Ŀǰֻ֧�ֶ������ȼ��� 
 * 2. һ��Ȩ�����͵���Ϣ�У������� 
 * 3. һ������Դֻ����һ�����ȼ����У�
 * 
 * 
 */
public class PriorityDbGroupSelector extends AbstractDBSelector {
    private static final Logger         logger = Logger.getLogger(PriorityDbGroupSelector.class);

    /**
     * �����ȼ�˳�������ݿ��顣Ԫ��0���ȼ���ߡ�ÿ��EquityDbManagerԪ�ش��������ͬ���ȼ���һ�����ݿ�
     */
    //private EquityDbManager[] priorityGroups;

    private PriorityGroupsDataSources[] priorityGroupsDataSourceHolder;

    public PriorityDbGroupSelector(String id, EquityDbManager[] priorityGroups) {
        super(id);
        // this.priorityGroups = priorityGroups;
        if (priorityGroupsDataSourceHolder == null) {
            priorityGroupsDataSourceHolder = new PriorityGroupsDataSources[priorityGroups.length];
        }
        for (int i = 0; i < priorityGroups.length; i++) {
            this.priorityGroupsDataSourceHolder[i] = new PriorityGroupsDataSources(
                priorityGroups[i]);
        }

        if (priorityGroupsDataSourceHolder == null || priorityGroupsDataSourceHolder.length == 0) {
            throw new IllegalArgumentException("PriorityGroupsDataSourceHolder is null or empty!");
        }
    }

    public DataSource select() {
        for (int i = 0; i < priorityGroupsDataSourceHolder.length; i++) {
            DataSource ds = getEquityDbManager(i).select();
            if (ds != null) {
                return ds;
            }
        }
        return null;
    }

    /**
     * ȡÿ�������weightKey���ܵ�weightKey�Ľ�������������
     */
    public void setWeight(Map<String, Integer> weightMap) {
        for (int i = 0; i < priorityGroupsDataSourceHolder.length; i++) {
            Map<String, Integer> oldWeights = getEquityDbManager(i).getWeights();
            Map<String, Integer> newWeights = new HashMap<String, Integer>(oldWeights.size());
            for (Map.Entry<String, Integer> e : weightMap.entrySet()) {
                if (oldWeights.containsKey(e.getKey())) {
                    newWeights.put(e.getKey(), e.getValue());
                }
            }
            getEquityDbManager(i).setWeightRandom(new WeightRandom(newWeights));
        }
    }

    private static class DataSourceTryerWrapper<T> implements DataSourceTryer<T> {
        private final List<SQLException> historyExceptions;
        private final DataSourceTryer<T> tryer;

        public DataSourceTryerWrapper(DataSourceTryer<T> tryer, List<SQLException> historyExceptions) {
            this.tryer = tryer;
            this.historyExceptions = historyExceptions;
        }

        public T onSQLException(List<SQLException> exceptions, ExceptionSorter exceptionSorter,
                                Object... args) throws SQLException {
            Exception last = exceptions.get(exceptions.size() - 1);
            if (last instanceof NoMoreDataSourceException) {
                if (exceptions.size() > 1) {
                    exceptions.remove(exceptions.size() - 1);
                }
                historyExceptions.addAll(exceptions);
                throw (NoMoreDataSourceException) last;
            } else {
                return tryer.onSQLException(exceptions, exceptionSorter, args);
            }
        }

        public T tryOnDataSource(DataSource ds, String name, Object... args) throws SQLException {
            return tryer.tryOnDataSource(ds, name, args);
        }
    };

    /**
     * ����EquityDbManager��tryExecuteʵ�֣����û���tryer��һ����װ����wrapperTryer.onSQLException��
     * ��⵽���һ��e��NoMoreDataSourceExceptionʱ������ԭtryer��onSQLException, ת�������������ȼ���
     */
    public <T> T tryExecute(Map<DataSource, SQLException> failedDataSources,
                            DataSourceTryer<T> tryer, int times, DB_OPERATION_TYPE operationType,
                            Object... args) throws SQLException {
        final List<SQLException> historyExceptions = new ArrayList<SQLException>(0);
        DataSourceTryer<T> wrapperTryer = new DataSourceTryerWrapper<T>(tryer, historyExceptions);

        for (int i = 0; i < priorityGroupsDataSourceHolder.length; i++) {
            try {
                return priorityGroupsDataSourceHolder[i].tryExecute(failedDataSources,
                    wrapperTryer, times, operationType, i, args);
            } catch (NoMoreDataSourceException e) {
                logger.warn("NoMoreDataSource for retry for priority group " + i);
            }
        }
        //���е����ȼ��鶼�����ã����׳��쳣
        return tryer.onSQLException(historyExceptions, exceptionSorter, args);
    }

    //Ĭ�϶�д�ⶼ���Խ�������
    public boolean isSupportRetry(OperationDBType type) {
        return true;
    }

    public EquityDbManager[] getPriorityGroups() {
        EquityDbManager[] priorityGroups = new EquityDbManager[priorityGroupsDataSourceHolder.length];
        for (int i = 0; i < priorityGroupsDataSourceHolder.length; i++) {
            priorityGroups[i] = getEquityDbManager(i);
        }
        return priorityGroups;
    }

    private EquityDbManager getEquityDbManager(int i) {
        if (priorityGroupsDataSourceHolder[i] == null
            || priorityGroupsDataSourceHolder[i].getEquityDbManager() == null) {
            throw new IllegalArgumentException(
                "The priorityGroupsDataSourceHolder or equityDbManager can't be null!");
        }
        return priorityGroupsDataSourceHolder[i].getEquityDbManager();
    }
}
