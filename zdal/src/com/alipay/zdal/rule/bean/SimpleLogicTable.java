/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.alipay.zdal.rule.groovy.GroovyListRuleEngine;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.DefaultTableMapProvider;
import com.alipay.zdal.rule.ruleengine.util.RuleUtils;

/**
 * �򵥱��������ʵ��������Ĺ���ļ��ϣ��ὫһЩ����������ƽ��
 * 
 * ������Ժ�������ʱ����ʹ�õ�����ʱ���ݣ�Ȼ��������С�
 * 
 *
 */
public class SimpleLogicTable extends LogicTable {
    private static final Logger    log                       = Logger
                                                                 .getLogger(SimpleLogicTable.class);

    String                         databases;

    /**
     * �ֿ��
     */
    String                         shardingKey;

    List<Object>                   tableRuleStringList;

    List<Object>                   databaseRuleStringList;

    boolean                        isSimpleTableMapPropertiesChanged;

    /**
     * Simple logic table���ڲ�����,���ڼ�¼��Ҫ���ݸ�����ı�������Ϣ��
     * 
     *TODO: ��֪��Ϊʲô֧������ͬ־���������ȥ���ˡ���
     */
    private SimpleTableMapProvider simpleTableMapProvider    = new SimpleTableMapProvider();

    SimpleListDatabaseMapProvider  simpleDatabaseMapProvider = new SimpleListDatabaseMapProvider();

    /**
     * �Ƿ�ʹ���Զ����ɹ���һ����˵�ò���
     * ֻ��Ϊ��ǿ�Ƹ�ҵ��һ����ʹ���Զ������ѡ��Ա���ʱ֮�衣
     */
    boolean                        useAutoGeneratingRule     = true;

    /**
     * ���ֿ���ͷֱ����ָ��������£�
     * �Զ��������й���ֻҪ�ֿ���ͷֱ����һ��ָ���ˡ���ô����Ҫ�Զ����ɹ���
     * 
     * �Զ����ɹ���Ḳ�������Ѿ����ڵĹ���
     *
     * 
     * @return �Ƿ����ʹ���Զ���������
     */
    protected boolean canUseAutoGenerationRule() {
        if (!useAutoGeneratingRule) {
            return false;
        }
        if (shardingKey == null) {
            return false;
        }
        return true;
    }

    protected void valid(int databasesSize, int tableSizeForEachDatabase) {
        if (databasesSize == 0 || tableSizeForEachDatabase == 0) {
            //�ֿ���ͷֱ��Ϊ0�����������ڷֿ��ֱ�û��ָ�������ʱ���ǲ���Ҫƴ�ӵ�
            return;
        }
        int dividend = 0;
        int divisor = 0;
        if (databasesSize > tableSizeForEachDatabase) {
            dividend = databasesSize;
            divisor = tableSizeForEachDatabase;
        } else {
            dividend = tableSizeForEachDatabase;
            divisor = databasesSize;
        }
        if (dividend % divisor != 0) {
            throw new IllegalArgumentException("�ֱ���������Ƿֿ�����ı���," + "�ֿ���:" + databasesSize + "�ֱ���:"
                                               + tableSizeForEachDatabase);
        }
    }

    static class TableAGRuleHandler extends DatabaseAndTableAGRuleHandler implements
                                                                         AutoGenerationRuleHandler {

        @Override
        public String getTableRule(String tableShardingKey, int databaseSize,
                                   int tableSizeForEachDatabase) {
            StringBuilder sb = new StringBuilder();

            sb.append("#").append(tableShardingKey).append("#");
            if (tableSizeForEachDatabase != 0) {
                sb.append(" % ").append(tableSizeForEachDatabase);
            }
            return sb.toString();
        }

        public String getDatabaseRule(String databaseShardingKey, int tablesSize,
                                      int tableSizeForEachDatabase) {
            return null;
        }
    }

    static class DatabaseAGRuleHandler extends DatabaseAndTableAGRuleHandler implements
                                                                            AutoGenerationRuleHandler {
        @Override
        public String getTableRule(String tableShardingKey, int databaseSize,
                                   int tableSizeForEachDatabase) {
            return null;
        }

        @Override
        public String getDatabaseRule(String databaseShardingKey, int databaseSize,
                                      int tableSizeForEachDatabase) {
            StringBuilder sb = new StringBuilder();
            sb.append("#").append(databaseShardingKey).append("#");
            if (databaseSize != 0) {
                sb.append(" % ").append(databaseSize);
            }
            sb.append("");
            return sb.toString();
        }
    }

    static class NoneAGRuleHandler implements AutoGenerationRuleHandler {

        public String getDatabaseRule(String databaseShardingKey, int tablesSize,
                                      int tableSizeForEachDatabase) {
            return null;
        }

        public String getTableRule(String tableShardingKey, int tablesSize,
                                   int tableSizeForEachDatabase) {
            return null;
        }

    }

    static class DatabaseAndTableAGRuleHandler implements AutoGenerationRuleHandler {

        public String getDatabaseRule(String databaseShardingKey, int databaseSize,
                                      int tableSizeForEachDatabase) {
            int tablesSize = databaseSize * tableSizeForEachDatabase;
            StringBuilder sb = new StringBuilder();
            sb.append("(#").append(databaseShardingKey).append("#");
            if (tablesSize != 0) {
                sb.append(" % ").append(tablesSize);
            }
            sb.append(")");
            if (tableSizeForEachDatabase != 0) {
                sb.append(".intdiv(").append(tableSizeForEachDatabase).append(")");
            }
            return sb.toString();
        }

        public String getTableRule(String tableShardingKey, int databaseSize,
                                   int tableSizeForEachDatabase) {
            int tablesSize = databaseSize * tableSizeForEachDatabase;

            StringBuilder sb = new StringBuilder();

            sb.append("(#").append(tableShardingKey).append("#");
            if (tablesSize != 0) {
                sb.append(" % ").append(tablesSize);
            }
            sb.append(")");
            if (tableSizeForEachDatabase != 0) {
                sb.append(" % ").append(tableSizeForEachDatabase);
            }
            return sb.toString();
        }
    }

    static interface AutoGenerationRuleHandler {
        /**
         * ��ȡ�ֱ����
         * @param tableShardingKey �ֱ��
         * @param tablesSize ����ܸ���
         * @param tableSizeForEachDatabase ÿ����ı�ĸ���
         * @return
         */
        String getTableRule(String tableShardingKey, int tablesSize, int tableSizeForEachDatabase);

        /**��ȡ�ֿ����
         * 
         * @param tableShardingKey �ֱ��
         * @param tablesSize ����ܸ���
         * @param tableSizeForEachDatabase ÿ����ı�ĸ���
         * 
         * @return
         */
        String getDatabaseRule(String databaseShardingKey, int tablesSize,
                               int tableSizeForEachDatabase);
    }

    AutoGenerationRuleHandler decideAutoGenerationRuleHandler(int databaseSize,
                                                              int tableSizeForEachDatabase) {
        if (databaseSize <= 0 || tableSizeForEachDatabase <= 0) {
            throw new IllegalArgumentException("������Ҫһ����,һ�ű�");
        }
        if (databaseSize == 1) {
            if (tableSizeForEachDatabase == 1) {
                //���ⵥ����
                return new NoneAGRuleHandler();
            } else {
                //��������
                return new TableAGRuleHandler();
            }
        } else {
            if (tableSizeForEachDatabase == 1) {
                //��ⵥ��
                return new DatabaseAGRuleHandler();
            } else {
                //�����
                return new DatabaseAndTableAGRuleHandler();
            }
        }
    }

    /**
     * �Զ����ɹ���
     * 
     * ��ָ����һ��databaseKey��tableKey��ʱ�򴥷���
     * 
     * �Զ�����һ������
     * 
     * Ȼ�����û��ͨ���ⲿ��ָ��String����Ļ����ͻ�ʹ�õ�ǰ����������ⲿ����
     * 
     * ������ⲿָ���Ĺ�����ôʹ���ⲿ����
     * 
     * ���ȼ���͵�һ�ֹ�����������
     */
    protected void processAutoGenerationRule() {
        if (!canUseAutoGenerationRule()) {
            return;
        }
        int databaseSize = simpleDatabaseMapProvider.getDatasourceKeys().size();
        int tablesNumberForEachDatabases = getTablesNumberForEachDatabases();
        valid(databaseSize, tablesNumberForEachDatabases);
        //���п���ܱ����
        AutoGenerationRuleHandler agrHandler = decideAutoGenerationRuleHandler(databaseSize,
            tablesNumberForEachDatabases);
        String dbRule = agrHandler.getDatabaseRule(shardingKey, databaseSize,
            tablesNumberForEachDatabases);
        //ֻ�е�databaseRuleStringListΪnull������²���Ĭ�Ϲ����滻֮��
        if (dbRule != null && this.databaseRuleStringList == null) {
            this.databaseRuleStringList = new ArrayList<Object>(1);
            if (log.isDebugEnabled()) {
                log.debug("auto generation rule for database: " + dbRule);
            }
            databaseRuleStringList.add(dbRule);
        }
        String tableRule = agrHandler.getTableRule(shardingKey, databaseSize,
            tablesNumberForEachDatabases);
        if (tableRule != null) {
            this.tableRuleStringList = new ArrayList<Object>(1);
            if (log.isDebugEnabled()) {
                log.debug("auto generation rule for database: " + tableRule);
            }
            tableRuleStringList.add(tableRule);
        }
    }

    protected int getTablesNumberForEachDatabases() {
        int tablesNumberForEachDatabases = simpleTableMapProvider.getTablesNumberForEachDatabases();
        if (tablesNumberForEachDatabases == SimpleTableMapProvider.DEFAULT_TABLES_NUM_FOR_EACH_DB) {
            //���ÿ�����ڱ��������Ĭ��ֵ����ôӦ����to-from+1��ô���ű�
            tablesNumberForEachDatabases = simpleTableMapProvider.getTo()
                                           - simpleTableMapProvider.getFrom() + 1;
        }
        return tablesNumberForEachDatabases;
    }

    @Override
    public void init() {

        boolean isDatabase = true;
        //��ע�����ﲻҪ����ߵ���ʼ��˳��
        if (superClassDatabaseProviderIsNull()) {

            setSimpleDatabaseMapToSuperLogicTable();
        }

        if (superClassTableMapProviderIsNull()) {
            if (isSimpleTableMapPropertiesChanged)
                setTableMapProvider(this.simpleTableMapProvider);
        }

        processAutoGenerationRule();

        if (superClassDatabaseRuleIsNull()) {
            RuleChain rc = RuleUtils.getRuleChainByRuleStringList(databaseRuleStringList,
                GroovyListRuleEngine.class, isDatabase);

            super.listResultRule = rc;
        }

        if (transmitterTableRuleIsNull()) {
            RuleChain rc = RuleUtils.getRuleChainByRuleStringList(tableRuleStringList,
                GroovyListRuleEngine.class, !isDatabase);
            setTableRuleChain(rc);
        }
        super.init();
    }

    private boolean transmitterTableRuleIsNull() {
        return getTableRule() == null || getTableRule().isEmpty();
    }

    private boolean superClassDatabaseRuleIsNull() {
        return listResultRule == null;
    }

    private boolean superClassTableMapProviderIsNull() {
        return getTableMapProvider() == null
               || getTableMapProvider() instanceof DefaultTableMapProvider;
    }

    protected void setSimpleDatabaseMapToSuperLogicTable() {
        if (databases == null) {
            return;
        }
        String[] databasesTokens = databases.split(",");
        simpleDatabaseMapProvider.setDatasourceKeys(Arrays.asList(databasesTokens));
        setDatabaseMapProvider(simpleDatabaseMapProvider);
    }

    private boolean superClassDatabaseProviderIsNull() {
        return getDatabaseMapProvider() == null;
    }

    /**
     * ��{@linkplain setLogicTableName}����һ��
     * @param logicTable
     */
    public void setLogicTable(String logicTable) {
        setLogicTableName(logicTable);
    }

    public void setPadding(String padding) {
        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setPadding(padding);
    }

    public void setParentID(String parentID) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setParentID(parentID);
    }

    public void setStep(int step) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setStep(step);
    }

    /**
     * ��{@linkplain setLogicTableName}����һ��
     * @param tableFactor
     */
    public void setTableFactor(String tableFactor) {
        setLogicTableName(tableFactor);
    }

    public void setTablesNumberForEachDatabases(int tablesNumberForEachDatabases) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setTablesNumberForEachDatabases(tablesNumberForEachDatabases);
    }

    public void setFrom(int from) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setFrom(from);
    }

    public void setTo(int to) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setTo(to);
    }

    public void setType(String type) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setType(type);
    }

    public void setWidth(int width) {

        isSimpleTableMapPropertiesChanged = true;
        simpleTableMapProvider.setWidth(width);
    }

    public String getDatabases() {
        return databases;
    }

    public void setDatabases(String databases) {
        this.databases = databases;
    }

    public List<Object> getTableRuleStringList() {
        return tableRuleStringList;
    }

    public void setTableRuleStringList(List<Object> tableRuleStringList) {
        this.tableRuleStringList = tableRuleStringList;
    }

    public List<Object> getDatabaseRuleStringList() {
        return databaseRuleStringList;
    }

    public void setDatabaseRuleStringList(List<Object> databaseRuleStringList) {
        this.databaseRuleStringList = databaseRuleStringList;
    }

    public boolean isUseAutoGeneratingRule() {
        return useAutoGeneratingRule;
    }

    public void setUseAutoGeneratingRule(boolean useAutoGeneratingRule) {
        this.useAutoGeneratingRule = useAutoGeneratingRule;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    public void setSimpleTableMapProvider(SimpleTableMapProvider simpleTableMapProvider) {
        this.simpleTableMapProvider = simpleTableMapProvider;
    }

    @Override
    public String toString() {
        return "SimpleLogicTable [databaseRuleStringList=" + databaseRuleStringList
               + ", databases=" + databases + ", isSimpleTableMapPropertiesChanged="
               + isSimpleTableMapPropertiesChanged + ", shardingKey=" + shardingKey
               + ", simpleDatabaseMapProvider=" + simpleDatabaseMapProvider
               + ", simpleTableMapProvider=" + simpleTableMapProvider + ", tableRuleStringList="
               + tableRuleStringList + ", useAutoGeneratingRule=" + useAutoGeneratingRule
               + ", defaultListResult=" + defaultListResult + ", defaultListResultStragety="
               + defaultListResultStragety + ", listResultRule=" + listResultRule
               + ", subSharedElement=" + subSharedElement + "]";
    }

}
