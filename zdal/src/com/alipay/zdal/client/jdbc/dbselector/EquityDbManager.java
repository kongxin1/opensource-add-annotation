/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.ThreadLocalString;
import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;
import com.alipay.zdal.client.util.ThreadLocalMap;
import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.OperationDBType;
import com.alipay.zdal.common.RuntimeConfigHolder;
import com.alipay.zdal.common.WeightRandom;
import com.alipay.zdal.common.lang.StringUtil;

/**
 * �Ե����ݿ������
 * �����Ƕ��Եȣ��������⣬ÿ�����������ȫ��ͬ���Եȶ�ȡ
 * ������д�Եȣ�����־�⣬ÿ�������ݲ�ͬ��һ������д���ĸ��ⶼ���ԡ��Ե�д��
 * 
 * ֧�ֶ�̬����Ȩ�أ���̬�Ӽ���
 * 
 * 
 * @param <T> JdbcTemplate ���� DataSource
 * 
 * TODO ��DataSource�������ж��ǲ���ͬһ���⣬���ٱ�������������Ϣ��Map<String, Map<String, Object>>
 */
public class EquityDbManager extends AbstractDBSelector {
    private static final Logger logger = Logger.getLogger(EquityDbManager.class);

    /**
     * �����ڻᶯ̬�ı��״̬�����ֲ������ֻ���ؽ��������޸ġ�
     */
    private static class DbRuntime {
        public final Map<String, DataSource>       dataSources;      //���ս��
        public final Map<String, DataSourceHolder> dataSourceHolders; //��װ����datasource����,��Ҫ�����Ƿ�ɶ�������

        public final WeightRandom                  weightRandom;

        public DbRuntime(Map<String, DataSource> dataSources, WeightRandom weightRandom) {
            this.dataSources = Collections.unmodifiableMap(dataSources);

            this.weightRandom = weightRandom;

            this.dataSourceHolders = getDataSourceHolders(dataSources);

        }

        /**
         * �õ���װ����datasource���ϣ�
         * @param dataSources
         * @return
         */
        public Map<String, DataSourceHolder> getDataSourceHolders(
                                                                  Map<String, DataSource> dataSources) {
            Map<String, DataSourceHolder> map = new HashMap<String, DataSourceHolder>(dataSources
                .size());
            for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
                map.put(entry.getKey(), new DataSourceHolder(entry.getValue()));

            }
            return Collections.unmodifiableMap(map);
        }

        public DataSource select() {
            return this.dataSources.get(this.weightRandom.select(null));
        }
    }

    private final RuntimeConfigHolder<DbRuntime> dbHolder = new RuntimeConfigHolder<DbRuntime>();

    /**
     * key:��־���ֵ����������Ȩ��ʱʹ��
     *     ��־������properties�ļ����Զ����key, �ڷֿ�ֱ����������dbSelectId_index
     * value����־���Ӧ��JdbcTemplate
     */
    private Map<String, DataSource>              initDataSources;                                //���ս��
    //    /**
    //     * key: һ��log����Դ��ֵ
    //     * value:����Դ����Properties��Ӧ��Map
    //     *    key������Դ��Ҫ�Ĳ�����driver,username..
    //     *    value:������ֵ
    //     */
    //    private Map<String, DataSourceConfig>        initDataSourceConfigs;
    private String                               dataSourceConfigFile;

    public EquityDbManager(String id) {
        super(id);
    }

    public EquityDbManager(String id, Map<String, DataSource> initDataSources) {
        super(id);
        this.initDataSources = initDataSources;
        try {
            this.init();
        } catch (IOException e) {
            logger.error("Should not happen!!", e); //��Ϊ���ڽ���dataSourceConfigFile�ŻᱨIOException
        }
    }

    public EquityDbManager(String id, Map<String, DataSource> initDataSources,
                           Map<String, Integer> weights) {
        this(id, initDataSources);
        if (weights != null) {
            setWeightRandom(new WeightRandom(weights));
        }
    }

    /**
     * �����������ȼ���1.dataSources 2.dataSourceConfigs 3.dataSourceConfigFile 4.����
     */
    public void init() throws IOException {
        //��ʼ������Դ
        if (this.initDataSources != null) {
            //���ֱ��������syncLogDataSources����ֱ��ʹ�ã���֧�ֶ��ĺͶ�̬�޸�
            WeightRandom weightRandom = new WeightRandom(this.initDataSources.keySet().toArray(
                new String[0]));
            this.dbHolder.set(new DbRuntime(this.initDataSources, weightRandom));
        }
        //        else if (this.initDataSourceConfigs != null) {
        //            //���ֱ��������syncLogDataSourceConfigs����ֱ��ʹ�ã���֧�ֶ��ĺͶ�̬�޸�
        //            //initDataSources(this.initDataSourceConfigs);
        //        }
        else if (this.dataSourceConfigFile != null) {
            //���ֱ��������dataSourceConfigFile��������ļ�����֧�ֶ��ĺͶ�̬�޸�
            Properties p = new Properties();
            if (dataSourceConfigFile.startsWith("/")) {
                dataSourceConfigFile = StringUtil.substringAfter(dataSourceConfigFile, "/");
            }
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                dataSourceConfigFile);
            if (null == inputStream) {
                throw new IllegalArgumentException("dataSource�����ļ�������: " + dataSourceConfigFile);
            }
            p.load(inputStream);
            //initDataSources(parseDataSourceConfig(p));
        } else {

        }

    }

    /**
     * ����Ȩ�أ��������һ��DataSource
     * @return
     */
    public DataSource select() {
        return this.dbHolder.get().select();
    }

    //TODO ���ǽӿ��Ƿ���СΪֻ����DataSource[]
    public Map<String, DataSource> getDataSources() {
        return this.dbHolder.get().dataSources;
    }

    public Map<String, Integer> getWeights() {
        return this.dbHolder.get().weightRandom.getWeightConfig();
    }

    /**
     * ������������ݿ�������ִ��һ���ص�������ʧ���˸���Ȩ��ѡ��һ��������
     * �Ը���Ȩ��ѡ�񵽵�DataSource�����û���������ò���args�����Ե���DataSourceTryer��tryOnDataSource����
     * @param failedDataSources ��֪��ʧ��DS�����쳣
     * @param args ͸����DataSourceTryer��tryOnDataSource������
     * @return null��ʾִ�гɹ��������ʾ���Դ���ִ��ʧ�ܣ�����SQLException�б�
     */
    public <T> T tryExecute(Map<DataSource, SQLException> failedDataSources,
                            DataSourceTryer<T> tryer, int times, DB_OPERATION_TYPE operationType,
                            Object... args) throws SQLException {
        List<SQLException> exceptions = new ArrayList<SQLException>(0);
        List<String> excludeKeys = new ArrayList<String>(0);
        DbRuntime dbrt = this.dbHolder.get();
        WeightRandom wr = dbrt.weightRandom;
        if (failedDataSources != null) {
            exceptions.addAll(failedDataSources.values());
            times = times - failedDataSources.size(); //�۳��Ѿ�ʧ�ܵ������Դ���
            for (SQLException e : failedDataSources.values()) {
                if (!exceptionSorter.isExceptionFatal(e)) {
                    //��һ���쳣����ʵ����������쳣����map�޷�֪��˳��ֻ�ܱ������������ݿⲻ�����쳣�����׳�
                    //�ǲ���Ӧ���ڷ��ַ����ݿ�fatal֮��������׳��������Ƿŵ�failedDataSources���map��?(guangxia)
                    return tryer.onSQLException(exceptions, exceptionSorter, args);
                }
            }
        }
        String name = null;
        //���ָ����ĳ������
        Integer dbIndex = (Integer) ThreadLocalMap.get(ThreadLocalString.DATABASE_INDEX);

        for (int i = 0; i < times; i++) {
            if (i == 0 && dbIndex != null) {
                //�����ָ���˿��
                name = this.getId() + Constants.DBINDEX_DSKEY_CONN_CHAR + dbIndex;
            } else {
                //���ѡ��
                name = wr.select(excludeKeys);
            }
            if (name == null) {
                exceptions.add(new NoMoreDataSourceException("��ִ��sql�Ĺ����У�û�п�������Դ�ˣ�"));
                logger.warn("�ڴ˴�ִ��sql�Ĺ����У�����Դ" + wr.getAllDbKeys() + "���������ˣ�");
                break;
            }
            //��ȡ��db�����֣�Ȼ�󻺴�������ҵ����õ�������
            Map<String, DataSource> map = new HashMap<String, DataSource>();
            //����ǰ׺��by���� 20130903
            map.put(getAppDsName() + "." + name, null);
            ThreadLocalMap.put(ThreadLocalString.GET_ID_AND_DATABASE, map);

            DataSourceHolder selectedDS = dbrt.dataSourceHolders.get(name);
            if (selectedDS == null) {
                //��Ӧ�ó��ֵġ���ʼ���߼�Ӧ�ñ�֤�յ�����Դ(null)���ᱻ����DataSourceHolders
                throw new IllegalStateException("Can't find DataSource for name:" + name
                                                + "from dataSourceHolders!");
            }
            if (failedDataSources != null && failedDataSources.containsKey(selectedDS.getDs())) {
                excludeKeys.add(name);
                if (dbIndex == null) {
                    i--;
                }
                continue;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("selected database name=" + name);
            }
            int size1 = excludeKeys.size();
            //���������Դ�Ѿ�����ʶΪ������
            T t = selectedDS.tryOnSelectedDataSource(operationType, wr, dbrt.dataSourceHolders,
                failedDataSources, tryer, exceptions, excludeKeys, exceptionSorter, name, args);
            boolean isAddedIntoExcludeKeys = excludeKeys.size() - size1 > 0;
            if (isAddedIntoExcludeKeys) {
                continue;
            } else {
                return t;
            }
        }
        //return exceptions; //�����Է���ҵ���log, ����tryExecute�϶���������ȥ��throwSQLException
        return tryer.onSQLException(exceptions, exceptionSorter, args);
    }

    public static interface DataSourceChangeListener {
        public void onDataSourceChanged(Map<String, DataSource> dataSources);
    }

    public void setWeight(Map<String, Integer> weightMap) {
        setWeightRandom(new WeightRandom(weightMap));
    }

    /**
     * ֧�ֶ�̬�޸�Ȩ��
     */
    synchronized boolean setWeightRandom(WeightRandom weightRandom) {
        if (weightRandom == null) {
            return false;
        }
        Map<String, Integer> newWeight = weightRandom.getWeightConfig();
        DbRuntime dbrt = EquityDbManager.this.dbHolder.get();
        for (String newkey : newWeight.keySet()) {
            if (!dbrt.dataSources.containsKey(newkey)) {
                logger.error("��Ȩ�ص�����Դ��������������Դ�в�����:" + newkey);
                return false;
            }
        }
        if (newWeight.size() < dbrt.dataSources.size()) {
            logger.warn("��Ȩ�ص�����Դ���Ƹ���С��ԭ������Դ��");
            return false; //������������������ȫһЩ
        }
        Map<String, DataSource> dataSources = new HashMap<String, DataSource>(dbrt.dataSources
            .size());
        dataSources.putAll(dbrt.dataSources);
        DbRuntime newrt = new DbRuntime(dataSources, weightRandom);
        EquityDbManager.this.dbHolder.set(newrt);
        return true;
    }

    /*
     * �Եȿ�Ĭ�ϲ������ԣ�ֻ�ж���Ž��ж����ԣ�д�ⲻ����д���ԣ�
     */
    public boolean isSupportRetry(OperationDBType type) {
        boolean flag = false;
        String dbSelectorId = getId();
        if (dbSelectorId.endsWith("_r") || (type == OperationDBType.readFromDb)) {
            flag = true;
        }
        return flag;
    }

    /**
     * ���߼���getter/setter
     */
    public void setInitDataSources(Map<String, DataSource> initDataSources) {
        this.initDataSources = initDataSources;
    }

    public void setDataSourceConfigFile(String dataSourceConfigFile) {
        this.dataSourceConfigFile = dataSourceConfigFile;
    }

}