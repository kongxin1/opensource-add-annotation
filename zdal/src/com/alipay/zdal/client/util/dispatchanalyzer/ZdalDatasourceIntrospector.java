/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util.dispatchanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.alipay.zdal.client.datasource.keyweight.ZdalDataSourceKeyWeightRandom;
import com.alipay.zdal.client.dispatcher.DatabaseChoicer;
import com.alipay.zdal.client.dispatcher.Result;
import com.alipay.zdal.client.jdbc.AbstractZdalDataSource;
import com.alipay.zdal.client.util.condition.TableShardingRuleImpl;
import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.rule.config.beans.AppRule;
import com.alipay.zdal.rule.config.beans.ShardRule;
import com.alipay.zdal.rule.config.beans.TableRule;
import com.alipay.zdal.rule.groovy.GroovyListRuleEngine;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;
import com.alipay.zdal.rule.ruleengine.util.RuleUtils;

/**
 * ȫ����Ե�֧���࣬�����ȡĳ�����õ�db��Ż��߱��,<br>
 * �༭��ҵ����ˮ���Ȼ������sharding·�ɵ�ʱ����õ���<br>
 * �������ʵ�ֵĹ��ܰ�����
 * 1)ȫ����Գ����£������߼���������ţ�Ĭ��Ϊ0����Ϊֻ��һ�飩�����ȡһ�����õ�db�ţ��Լ��ڸ�db����������<br>
 * 2����ȡ���߼�����ĳ������Դ�ϵķֿ�ֱ�����<br>
 * 
 * ��dual����Ż����ԣ�
 * ����ȫ�������Ҫ��ȡһ�����õ�db��ţ�֮ǰ�ķ�����ÿ����ҵ�����󵽴��Ҫͨ��select from dual 
 * ����db�����Ե�У�飬 ��� dba ����dual��ķ��ʴ���̫�ߣ�����load���ߣ��Ż����󽵵��˶�dual��ķ��ʴ�����db��load��֮���͡�
 * ������������
 * ��֮ǰ��ҵ���̼߳��db �����ԣ��ı�Ϊ ��zdal���첽�̼߳�⣬ÿ��һ����ʱ��Σ�����10ms�������ã�
 * ���һ������db��״̬����ֻ�з��ֺ��ϴεļ�������ڲ��������²�ȥˢ�±��db��״̬���÷����ܺõ�ʵ����ȫ���������Դ���Զ��޳��ͻָ���
 * 
 * @author zhaofeng.wang
 * @version $Id: TDatasourceIntrospector.java, v 0.1 2012-3-26 ����06:26:21 zhaofeng.wang Exp $
 */

public class ZdalDatasourceIntrospector {

    public static final Logger                  logger                 = Logger
                                                                           .getLogger(Constants.CONFIG_LOG_NAME_LOGNAME);
    /**
     * Zdal��װ������Դ 
     */
    private AbstractZdalDataSource              targetDataSource;
    /**
     * key Ϊ ������Դ��ʶ#�߼���������valueΪ��ĺ�׺����
     */
    private Map<String, List<String>>           tablesCache            = new HashMap<String, List<String>>(
                                                                           0);
    /**
     * Ȩ�������
     */
    private final Random                        random                 = new Random();
    /**
     * ����������
     */
    private static final Map<String, RuleChain> ruleChainCache         = new HashMap<String, RuleChain>();
    /**
     * ����������
     */
    private RuleChain                           singleRuleChain;

    /**
     * Ĭ�ϵ���ѵʱ��
     */
    private long                                waitTime               = 1000L;
    /**
     * ͨ��Future��ʽ��ȡ������ӽ���ĳ�ʱʱ�������������ֵ���׳�timeOutException
     */
    private long                                timeOutLength          = 500L;

    /**
     * �������Զ��޳������ݿ�ĸ�����Ĭ��Ϊ-1
     */
    private int                                 closeDBLimitNumber     = -1;

    private CheckDBAvailableStatus              checkDBAvailableStatus = new CheckDBAvailableStatus();

    /**
     * �̳߳ص���Сֵ
     */
    private int                                 corePoolSize           = 1;
    /**
     * �̳߳ص����ֵ
     */
    private int                                 maximumPoolSize        = 10;
    /**
     * �̳߳ض��г���
     */
    private int                                 workQueueSize          = 100;

    /**
     * �Ƿ�ʹ���첽�ύ��ʽ
     */
    private boolean                             isUseFutureMode        = true;

    public void init() {
        //�����첽�̣߳���ѵ���ݿ�Ŀ���״̬
        //�����첽�̣߳���ѵ���ݿ�Ŀ���״̬
        checkDBAvailableStatus.setTargetDataSource(targetDataSource);
        checkDBAvailableStatus.setIsUseFutureMode(isUseFutureMode);
        checkDBAvailableStatus.setWaitTime(waitTime);
        checkDBAvailableStatus.setTimeOutLength(timeOutLength);
        checkDBAvailableStatus.setCloseDBLimitNumber(closeDBLimitNumber);
        checkDBAvailableStatus.setCorePoolSize(corePoolSize);
        checkDBAvailableStatus.setMaximumPoolSize(maximumPoolSize);
        checkDBAvailableStatus.setWorkQueueSize(workQueueSize);
        checkDBAvailableStatus.runCirculateThread();
        logger.warn("The chekDBAvailableStatus parameter:" + checkDBAvailableStatus.toString());

    }

    /**
     * ���ڽ�����Ŀ���Ϻ��յ���Ŀ�ڵ���<br>
     * 
     * ȫ����������Ľӿ�<br>
     * 
     * ����Ȩ�������ѡ��һ������db�����ڸ�db�����ѡ��һ����ҵ��������װ�ֿ�ֱ��ֶ�<br>
     * 
     * @param logicTableName   �߼�����<br>
     * @param groupNum  ȫ����Գ�����Ĭ��Ϊ0
     * @return   String[0]�����ţ�String[1]�����������<br>
     */
    public String[] getAvailableDBAndTableByWeights(String logicTableName, int groupNum,
                                                    boolean isCheckConnection) {
        return getAvailableGroupDBAndTableByWeights(logicTableName, groupNum, isCheckConnection);
    }

    /**
     * ���ڽ�����Ŀ���ڵ���<br>
     * 
     * �Ƚ��з��飬�ٽ�������ȫ����ԵĽӿ�<br>
     *  ��ȡ���ڿ��õ�db���Լ�����ı�<br>
     * @param logicTableName �߼�����
     * @param parameters     ���������
     * @return               ���ڿ��õ�db�ź����ѡ��ı�
     */
    public String[] getAvailableGroupDBAndTable(String logicTableName,
                                                Map<String, String> parameters,
                                                boolean isCheckConnection) {
        int groupNum = getShardingResultByTableName(logicTableName, parameters);
        return getAvailableGroupDBAndTableByWeights(logicTableName, groupNum, isCheckConnection);
    }

    /**
     * �����û���ţ�Ȼ�����Ȩ�������������ѡ��һ������db�����ڸ�db�����ѡ��һ����ҵ��������װ�ֿ�ֱ��ֶ�<br>
     * 
     * @param logicTableName   �߼�����<br>
     * @return   String[0]�����������ѡ��Ŀ�ţ�String[1]�����ڿ������ѡ����������<br>
     */
    private String[] getAvailableGroupDBAndTableByWeights(String logicTableName, int groupNum,
                                                          boolean isCheckConnection) {
        String dbAndTable[] = new String[2];
        String orderInGroup = getAvailableGroupDBByWeights(groupNum, isCheckConnection);
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = getZdalDataSourceKeyWeightRandom(groupNum);
        String dbKey = ZdalDataSourceKeyWeightRandom
            .getDBKeyByNumber(Integer.valueOf(orderInGroup));
        String tableName = getTableNumberByRandom(dbKey, logicTableName);
        dbAndTable[0] = orderInGroup;
        dbAndTable[1] = tableName;
        return dbAndTable;
    }

    /**
     * ͳһ֧����Ŀ�ڵ���
     * 
     * �Ƚ��з��飬�ٽ�������ȫ����ԵĽӿ�.<br>
     * ��һ���ڻ�ȡ���õ�db������Ҫ�߼�����.<br>
     * ���ô˽ӿڣ��뱣֤���б��shardingRule����ͬ������ֻ��һ�������ø�����<br>
     * @param parameters     ������ֵ��<br>
     * @return               ���ڿ��õ�db��
     */
    public String getAvailableGroupDB(Map<String, String> parameters) {
        int groupNum = getShardingResult(parameters);
        return this.getAvailableGroupDBByWeights(groupNum, true);
    }

    /**
     * ͳһ֧����Ŀ����
     * 
     * �Ƚ��з��飬�ٽ�������ȫ����ԵĽӿ�.<br>
     * ��һ���ڻ�ȡ���õ�db������Ҫ�߼�����.<br>
     * ���ô˽ӿڣ��뱣֤���б��shardingRule����ͬ������ֻ��һ�������ø�����<br>
     * @param parameters     ������ֵ��<br>
     * @return               ���ڿ��õ�db��
     */
    public String getAvailableGroupDB(Map<String, String> parameters, boolean isCheckConnection) {
        int groupNum = getShardingResult(parameters);
        return this.getAvailableGroupDBByWeights(groupNum, isCheckConnection);
    }

    /**
     * �����ѡ��һ������db<br>
     * ��� isCheckConnection Ϊ false����Ĭ�� ������ѵ���ƣ���zdal���첽�̼߳��һ��ʱ����м�⣬
     * ���Ϊtrue�����ʾÿ�ζ����һ�£��ķ����ܵ�˵��
     * 
     * @param    groupNum   ���<br>
     * @param    isCheckConnection �Ƿ�������
     * @return   String[0]�����ţ�String[1]�����������<br>
     */
    private String getAvailableGroupDBByWeights(int groupNum, boolean isCheckConnection) {
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = getZdalDataSourceKeyWeightRandom(groupNum);
        int orderInGroup = -1;

        // ����Ȩ���ڸ�groupNum�������ȡһ��
        orderInGroup = ZdalDataSourceKeyWeightRandom
            .getRandomDBIndexByWeight(new ArrayList<Integer>());
        //��ȡ��db��Ӧ�ı�ʶdbKey
        String dbKey = ZdalDataSourceKeyWeightRandom.getDBKeyByNumber(orderInGroup);

        if (logger.isDebugEnabled()) {
            logger.debug("dbNumber=" + orderInGroup + ",dbKey=" + dbKey + ", isCheckConnection = "
                         + isCheckConnection);
        }

        if (orderInGroup < 0
            || orderInGroup >= ZdalDataSourceKeyWeightRandom.getDataSourceNumberInGroup()) {
            throw new IllegalArgumentException("The order number in group_"
                                               + groupNum
                                               + " is "
                                               + orderInGroup
                                               + ", but the biggest number is "
                                               + (ZdalDataSourceKeyWeightRandom
                                                   .getDataSourceNumberInGroup() - 1));
        }

        //���������ȡdb��number
        return Integer.toString(orderInGroup);
    }

    /**
     * ����db��ʶ���߼����������ȡ�ÿ��ϵ�һ���������<br>
     * @param dbKey            ���ݿ��ʶ<br>
     * @param logicTableName   �߼�����<br>
     * @return                 �������<br>
     */
    private String getTableNumberByRandom(String dbKey, String logicTableName) {
        List<String> tableIndexesList = new ArrayList<String>(0);
        tableIndexesList = getAllTableIndex(dbKey, logicTableName);
        if (tableIndexesList == null || tableIndexesList.size() == 0) {
            throw new IllegalArgumentException("������׺���ϲ� ��Ϊ��,dbKey=" + dbKey + ",logicTableName="
                                               + logicTableName);
        }
        int rand = random.nextInt(tableIndexesList.size());
        return tableIndexesList.get(rand);
    }

    /**
     * ���ݿ�ı�ʶdbKey���߼�����logicTableName��ȡ�ÿ��ϸ��߼�������Ӧ�������������<br>
     * ��Ϊһ���ֿ�ֱ�󣬸����ֿ�ķֱ���Զ��ǹ̶��ģ���˿��Ի�����������ȥ����飬<br>
     * ����鲻������ȥ��ȡ�ÿ��ϸ��߼�������Ӧ������������ϡ�<br>
     * 
     * @param dbKey          ��ı�ʶ<br>
     * @param logicTableName �߼�����<br>
     * @return               ���׺����<br>
     */
    private List<String> getAllTableIndex(String dbKey, String logicTableName) {

        String tablesCachekey = dbKey + "#" + logicTableName;

        List<String> tableIndexesList = new ArrayList<String>(0);
        tableIndexesList = tablesCache.get(tablesCachekey);
        //ȥ������飬����鵽�ͷ���
        if (tableIndexesList != null && tableIndexesList.size() != 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("����dbKey=" + dbKey + "��ȡ���߼�����" + logicTableName + "����Ӧ������������ϣ��ֱ��ǣ�"
                             + tableIndexesList.toString());

            }
            return tableIndexesList;
        } else {
            //����鲻������˵����û���ʹ��ÿ��ϵĸ��߼���
            DatabaseChoicer databaseChoicer = getDatabaseChoicer();
            if (databaseChoicer == null) {
                throw new IllegalArgumentException("The dispatcher is null,dbKey=" + dbKey
                                                   + ",logicTableName=" + logicTableName);
            }
            Result result = databaseChoicer.getAllDatabasesAndTables(logicTableName);
            if (result == null) {
                throw new IllegalArgumentException("The result is null,dbKey=" + dbKey
                                                   + ",logicTableName=" + logicTableName);
            }
            List<TargetDB> targetDBs = result.getTarget();
            if (targetDBs == null || targetDBs.size() == 0) {
                throw new IllegalArgumentException("TargetDBs can not be null,dbKey=" + dbKey
                                                   + ",logicTableName=" + logicTableName);
            }
            //���������ֿ⣬��ȡ��Ӧ�ķֱ����
            for (TargetDB targetDB : targetDBs) {
                List<String> list = new ArrayList<String>(0);
                for (String tableName : targetDB.getTableNames()) {
                    list.add(tableName);
                }
                String dbTag = targetDB.getDbIndex();
                if (StringUtil.isBlank(dbTag)) {
                    throw new IllegalArgumentException(
                        "The dbTag can't be null, please check the configure,dbKey=" + dbKey
                                + ",logicTableName=" + logicTableName);
                }
                String key = dbTag + "#" + logicTableName;
                tablesCache.put(key, list);
            }
            return tablesCache.get(tablesCachekey);
        }
    }

    /**
     * ���ݱ����Լ�����Ĳ�������ȡ��Ӧ�ķ����
     * @param tableName      �߼�����
     * @param parameters     ������
     * @return               �����
     */
    public int getShardingResultByTableName(String tableName, Map<String, String> parameters) {
        if (StringUtil.isBlank(tableName)) {
            throw new IllegalArgumentException("The key can't be null!");
        }
        TableShardingRuleImpl tableShardingRuleImpl = new TableShardingRuleImpl();
        tableShardingRuleImpl.put(parameters);
        RuleChain rc = this.getShardingRuleChainByTableName(tableName);
        String result = tableShardingRuleImpl.getShardingResult(rc);
        if (StringUtil.isBlank(result)) {
            throw new IllegalArgumentException("The result can not be null!");
        }
        return Integer.valueOf(result);
    }

    /**
     * ���ݴ���Ĳ�������ȡ��Ӧ�ķ����
     * @param parameters     ����
     * @return               �����
     */
    private int getShardingResult(Map<String, String> parameters) {
        TableShardingRuleImpl tableShardingRuleImpl = new TableShardingRuleImpl();
        tableShardingRuleImpl.put(parameters);
        RuleChain rc = this.getSingleShardingRuleChain();
        String result = tableShardingRuleImpl.getShardingResult(rc);
        if (StringUtil.isBlank(result)) {
            throw new IllegalArgumentException("The result can not be null!");
        }
        return Integer.valueOf(result);
    }

    /**
     * �����߼�������ȡ������
     * @param tableName �߼�����
     * @return shardingRule����������
     */
    private RuleChain getShardingRuleChainByTableName(String tableName) {
        if (StringUtil.isBlank(tableName)) {
            throw new IllegalArgumentException("The tableName can't be null!");
        }
        RuleChain rc = ruleChainCache.get(tableName);
        if (rc == null) {
            Map<String, TableRule> tableRules = getTableRules();
            TableRule tableRule = tableRules.get(tableName);
            rc = getRuleChainByTableRule(tableRule, tableName);
            if (rc == null) {
                throw new IllegalArgumentException("The shardingRules property don't be set!");
            }
            ruleChainCache.put(tableName, rc);
        }
        return rc;

    }

    /**
     * ��ȡ��һ��shardingRule����Ӧ�Ĺ�����,�������еķֿ�ֱ����
     * ���������һ������������shardingRule����������Ϊ��ȡ�����������
     * @return          ��һ������
     */
    private RuleChain getSingleShardingRuleChain() {
        //���singleRuleChainΪ�գ���������б����
        if (singleRuleChain == null) {

            Map<String, TableRule> tableRules = getTableRules();
            for (Map.Entry<String, TableRule> entry : tableRules.entrySet()) {
                String tableName = entry.getKey();
                TableRule tableRule = entry.getValue();
                singleRuleChain = getRuleChainByTableRule(tableRule, tableName);
                //�����һ������������shardingRule
                if (singleRuleChain != null) {
                    break;
                }
            }
            //����������еĹ�����������shardingRule
            if (singleRuleChain == null) {
                throw new IllegalArgumentException("The shardingRules property don't be set!");
            }
        }
        return singleRuleChain;
    }

    /**
     * ���ݷֿ�ֱ���򣬻�ȡshardingRule��Ӧ�Ĺ�����
     * @param tableRule     �ֿ�ֱ����
     * @param tableName     �߼�����
     * @return   shardingRule��Ӧ�Ĺ�����
     */
    private RuleChain getRuleChainByTableRule(TableRule tableRule, String tableName) {
        //tableRule һ���߼�������Ӧ��һ�׷ֿ�ֱ���򣬹ʴ����Բ���Ϊ��
        if (tableRule != null) {
            List<Object> sharidngRuleStringList = tableRule.getShardingRules();
            if (sharidngRuleStringList != null && sharidngRuleStringList.size() != 0) {
                return RuleUtils.getRuleChainByRuleStringList(sharidngRuleStringList,
                    GroovyListRuleEngine.class, false);
            }
        } else {
            throw new IllegalArgumentException("The tableRule object can not be null,tableName="
                                               + tableName);
        }
        return null;
    }

    /**
     * ��ȡ���еķֿ�ֱ����
     * @return  �ֿ�ֱ����
     */
    private Map<String, TableRule> getTableRules() {
        AppRule appRule = getTargetDataSource().getAppRule();
        if (appRule == null) {
            throw new IllegalArgumentException("The appRule can't be null!");
        }
        ShardRule shardRule = appRule.getMasterRule() != null ? appRule.getMasterRule() : appRule
            .getSlaveRule();
        if (shardRule == null) {
            throw new IllegalArgumentException("The sharding rule can't be null!");
        }
        Map<String, TableRule> tableRules = shardRule.getTableRules();
        if (tableRules == null || tableRules.size() == 0) {
            throw new IllegalArgumentException("Please set the tableRules property!");
        }
        return tableRules;

    }

    /**
     * �����߼��������ظñ�������Ӧ�����е������������<br>
     * @param logicTableName  �߼�����<br>
     * @return                �������е����������keyΪ��ı�ʶ��valueΪlist<String>������ÿ��ϵ������ļ���
     */
    public Map<String, List<String>> getAllTableNames(String logicTableName) {
        Map<String, List<String>> map = new HashMap<String, List<String>>(0);
        DatabaseChoicer databaseChoicer = getDatabaseChoicer();
        if (databaseChoicer == null) {
            throw new IllegalArgumentException("The dispatcher is null,logicTableName="
                                               + logicTableName);
        }
        Result result = databaseChoicer.getAllDatabasesAndTables(logicTableName);
        if (result == null) {
            throw new IllegalArgumentException("The result is null,logicTableName="
                                               + logicTableName);
        }
        List<TargetDB> targetDBs = result.getTarget();
        //���������ֿ⣬��ȡ��Ӧ�ķֱ����
        for (TargetDB targetDB : targetDBs) {
            List<String> list = new ArrayList<String>(0);
            for (String tableName : targetDB.getTableNames()) {
                list.add(tableName);
            }
            map.put(targetDB.getDbIndex(), list);
        }
        return map;
    }

    /**
     * ����dbNumber�ж�db�Ƿ����
     * @param dbNumber  db���к�
     * @param groupNum  �����
     * @return          �Ƿ����
     */
    public boolean isDataBaseAvailable(int dbNumber, int groupNum) {
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = getZdalDataSourceKeyWeightRandom(groupNum);
        //��ȡ��db��Ӧ�ı�ʶdbKey
        int dbWeight = ZdalDataSourceKeyWeightRandom.getDBWeightByNumber(dbNumber);
        return dbWeight > 0 ? true : false;
    }

    /**
     * ����Ȩ�ػ�ȡ�����õ����ݿ����м���
     * ���db�����ˣ�ֻ��Ȩ��������֮�󣬲�����Ϊ�߼��ϲ�����
     * �˷����ķ��ؽ��������������Ϊ
     * @return   ���ز����õĿ�ı�ţ���������Ϊ Integer��
     */
    public List<Integer> getNotAvailableDBIndexes(int groupNum) {
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = getZdalDataSourceKeyWeightRandom(groupNum);
        return ZdalDataSourceKeyWeightRandom.getNotAvailableDBIndexes();
    }

    /**
     * ����Ȩ�ػ�ȡ���õ����ݿ����м���
     * ���db�����ˣ�ֻ��Ȩ��������֮�󣬲�����Ϊ�߼��ϲ�����
     * �˷����ķ��ؽ��������������Ϊ
     * @return  ���ؿ��õ�db�ı�ż��ϡ�
     */
    public List<Integer> getAvailableDBIndexes(int groupNum) {
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = getZdalDataSourceKeyWeightRandom(groupNum);
        return ZdalDataSourceKeyWeightRandom.getAvailableDBIndexes();
    }

    /**
     * ������Ż�ȡ���������
     * @param groupNum     ���<br>
     * @return      ZdalDataSourceKeyWeightRandom����
     */
    private ZdalDataSourceKeyWeightRandom getZdalDataSourceKeyWeightRandom(int groupNum) {
        Map<String, ZdalDataSourceKeyWeightRandom> keyWeightMapConfig = getTargetDataSource()
            .getKeyWeightMapConfig();
        if (keyWeightMapConfig == null) {
            throw new IllegalArgumentException(
                "Please check the *-db.xml,property keyWeightMapConfig not set!");
        }
        if (groupNum < 0 || groupNum > keyWeightMapConfig.size() - 1) {
            throw new IllegalArgumentException("The groupNum can't be more than "
                                               + (keyWeightMapConfig.size() - 1) + ",it is"
                                               + groupNum);
        }
        String groupNo = Constants.DBINDEX_DS_GROUP_KEY_PREFIX + groupNum;
        ZdalDataSourceKeyWeightRandom ZdalDataSourceKeyWeightRandom = keyWeightMapConfig
            .get(groupNo);
        if (ZdalDataSourceKeyWeightRandom == null) {
            throw new IllegalArgumentException("Please check the configure,the groupNum is "
                                               + groupNo);
        }
        return ZdalDataSourceKeyWeightRandom;
    }

    /**
     * ��ȡ����Դ������ <br>
     * 
     * @return    DatabaseChoicer
     */
    private DatabaseChoicer getDatabaseChoicer() {
        AbstractZdalDataSource tds = getTargetDataSource();
        return tds.getWriteDispatcher() != null ? tds.getWriteDispatcher() : tds
            .getReadDispatcher();
    }

    /**
     * ��ȡzdal����Դ
     * 
     * @return  zdal��װ�������Դ
     */
    public AbstractZdalDataSource getTargetDataSource() {
        if (targetDataSource == null) {
            throw new IllegalArgumentException("The targetDataSource can't be null!");
        }
        return targetDataSource;
    }

    /**
     * ��������Դ
     * 
     * @param targetDataSource Ŀ������Դ
     */
    public void setTargetDataSource(AbstractZdalDataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    /**
     * Getter method for property <tt>waitTime</tt>.
     * 
     * @return property value of waitTime
     */
    public long getWaitTime() {
        return waitTime;
    }

    /**
     * Setter method for property <tt>waitTime</tt>.
     * 
     * @param waitTime value to be assigned to property waitTime
     */
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * Getter method for property <tt>timeOutLength</tt>.
     * 
     * @return property value of timeOutLength
     */
    public long getTimeOutLength() {
        return timeOutLength;
    }

    /**
     * Setter method for property <tt>timeOutLength</tt>.
     * 
     * @param timeOutLength value to be assigned to property timeOutLength
     */
    public void setTimeOutLength(long timeOutLength) {
        this.timeOutLength = timeOutLength;
    }

    /**
     * Setter method for property <tt>closeDBLimitNumber</tt>.
     * 
     * @param closeDBLimitNumber value to be assigned to property closeDBLimitNumber
     */
    public void setCloseDBLimitNumber(int closeDBLimitNumber) {
        this.closeDBLimitNumber = closeDBLimitNumber;
    }

    /**
     * Getter method for property <tt>closeDBLimitNumber</tt>.
     * 
     * @return property value of closeDBLimitNumber
     */
    public int getCloseDBLimitNumber() {
        return closeDBLimitNumber;
    }

    /**
     * Getter method for property <tt>corePoolSize</tt>.
     * 
     * @return property value of corePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Setter method for property <tt>corePoolSize</tt>.
     * 
     * @param corePoolSize value to be assigned to property corePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * Getter method for property <tt>maximumPoolSize</tt>.
     * 
     * @return property value of maximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Setter method for property <tt>maximumPoolSize</tt>.
     * 
     * @param maximumPoolSize value to be assigned to property maximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * Getter method for property <tt>workQueueSize</tt>.
     * 
     * @return property value of workQueueSize
     */
    public int getWorkQueueSize() {
        return workQueueSize;
    }

    /**
     * Setter method for property <tt>workQueueSize</tt>.
     * 
     * @param workQueueSize value to be assigned to property workQueueSize
     */
    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    /**
     * Getter method for property <tt>isUseFutureMode</tt>.
     * 
     * @return property value of isUseFutureMode
     */
    public boolean isUseFutureMode() {
        return isUseFutureMode;
    }

    /**
     * Setter method for property <tt>isUseFutureMode</tt>.
     * 
     * @param isUseFutureMode value to be assigned to property isUseFutureMode
     */
    public void setIsUseFutureMode(boolean isUseFutureMode) {
        this.isUseFutureMode = isUseFutureMode;
    }
}
