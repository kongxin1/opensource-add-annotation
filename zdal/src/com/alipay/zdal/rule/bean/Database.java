/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.OneToManyEntry;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.SharedElement;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.TablePropertiesSetter;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider;
import com.alipay.zdal.rule.ruleengine.entities.inputvalue.CalculationContextInternal;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;
import com.alipay.zdal.rule.ruleengine.rule.Field;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;
import com.alipay.zdal.rule.ruleengine.util.RuleUtils;

/**
 * һ�����ݿ�ĳ���
 * 
 * 
 */
public class Database extends ListSharedElement implements TablePropertiesSetter {
    protected static final Logger        log = Logger.getLogger(Database.class);
    // implements TableContainer,TableListResultRuleContainer
    private String                       dataSourceKey;

    /**
     * �߼�����
     */
    private String                       logicTableName;
    /**
     * ����List������initʱ���ruleChain
     */
    private List<ListAbstractResultRule> tableRuleList;
    /**
     * 1�Զ�ָ��������Entry
     */
    private OneToManyEntry               oneToManyEntry;

    private TableMapProvider             tableMapProvider;

    /** 
     * @see com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement#init()
     * ������Ҫ���ȳ�ʼ������subTable��Ȼ���������Ӧ�Ķ�����
     */
    public void init() {
        initTableRuleChain();
        initLogicTableName();
        initTableMapProvider();
        initDefaultListResultStragety();
        // �����tableRuleProvider ��ô��ʼ���ӱ�tableMapProvier��transmit����������ʱ��������
        if (tableMapProvider != null) {
            Map<String, SharedElement> beConstructedMap = getTableMapByTableMapProvider();
            putAutoConstructedMapIntoCurrentTagMap(beConstructedMap);
        }

        super.init();
    }

    private void initDefaultListResultStragety() {
        if (defaultListResultStragety == null) {
            defaultListResultStragety = oneToManyEntry.getDefaultListResultStragety();
        }
    }

    /**
     * ��������ķ���
     * 1. �����Database����û�й��򣬼ȶ�Ӧ�Ŀ�û�зֱ����,
     * ������Ȼ�й��򣬵��ǵ�ǰ��sql������û��ƥ�䵽��Щ���򣬷����������������
     *    a ���Database����ֻ��һ������ô����Ĭ�Ϲ���Ϊ�ζ�Ӧ��ȡ���ñ����������Ҫ�����ڵ��⣨��,�ࣩ���򣨵����ࣩ�� ���������¡�
     *    b ���Database�ж������ôӦ��ʹ��Ĭ��ѡ����ԣ�DEFAULT_LIST_RESULT_STRAGETY�������������������ΪDEFAULT_LIST_RESULT_STRAGETY.NONE�Ƚϰ�ȫ;
     *     
     * 2. ��������˹���
     * 2.1�����˹���Database��û�д��ݶ�λ����databaseʱ�����õ�����->��㼯�ϵ�map.��ô��ʾǰ�����ݺ͵�ǰ�����޹أ�ֱ��ʹ�õ�ǰ����+sql��ƥ���ֵ���ɡ� 
     * 2.2�������˶�λ����databaseʱ������->���ļ���
     *   2.2.1�����ݵ���㼯���е�key�����˴�sql�л�ȡ��key����ô��ʾdatabase��table�������ǻ��໥����Ӱ��ġ�
     *     ���ʱ������ʹ��database�д���� ����->�������㡣
     *   2.2.2�����ݵ���㼯���е�key��������sql�л�ȡ��key,��ô��ʾdatabase��table֮��������޹�
     *     ��ֱ��ʹ��table�����ݽ��м��㡣
     * 
     * @param targetDB
     *            Ŀ����������������ڱ���װ
     * @param sourceTrace
     *            �������ǰ���ݿ��Դ����׷�١�
     * @param map
     *            ����͹����Ӧ�������ġ�
     */
    public void calculateTable(TargetDB targetDB, Field sourceTrace,
                               Map<RuleChain, CalculationContextInternal> map) {
        CalculationContextInternal calculationContext = map.get(this.listResultRule);
        Set<String> resultSet = null;

        if (calculationContext == null) {
            // ��ʾû��ָ�����򣬰�����ǰ����Ϊnull��û��ƥ�䵽���ݣ���Ĭ��
            if (subSharedElement != null && subSharedElement.size() == 1) {
                // 1��
                resultSet = builSingleTable();
            } else {
                //2)
                resultSet = buildDefaultTable();
            }
        } else if (sourceTrace == null || sourceTrace.sourceKeys.isEmpty()) {
            //2.1��Database��û�д��ݶ�λ����databaseʱ�����õ�����->��㼯�ϵ�map

            ListAbstractResultRule rule = calculationContext.ruleChain
                .getRuleByIndex(calculationContext.index);
            Map<String, Set<Object>> argsFromSQL = getEnumeratedSqlArgsMap(calculationContext, rule);
            resultSet = rule.evalWithoutSourceTrace(argsFromSQL, null, null);
        } else {
            //2.2�������˶�λ����databaseʱ������->���ļ���
            ListAbstractResultRule rule = calculationContext.ruleChain
                .getRuleByIndex(calculationContext.index);
            Map<String, Set<Object>> argsFromSQL = getEnumeratedSqlArgsMap(calculationContext, rule);
            //�й�ϵ������Ӧ�����ȡ�putAll���ô����sourceKeys���Ա�database��Ч����㼯������ȫ��sql��Ϣ�ļ�����
            Map<String/* ���� */, Set<Object>/* �õ��ý�������ֵ�� */> sourceKeys = sourceTrace.sourceKeys;
            for (Entry<String, Set<Object>> entry : sourceKeys.entrySet()) {
                if (argsFromSQL.containsKey(entry.getKey())) {
                    argsFromSQL.put(entry.getKey(), entry.getValue());
                    if (log.isDebugEnabled()) {
                        log.debug("put entry: " + entry + " to args");
                    }
                }
                //				else{
                //					//�ӷֿ���������û�зֱ�����Ҫ�����ݵ�ʱ�򣬲��Ž�ȥ
                //				}
            }
            resultSet = rule.evalWithoutSourceTrace(argsFromSQL, sourceTrace.mappingTargetColumn,
                sourceTrace.mappingKeys);
        }

        buildTableNameSet(targetDB, resultSet);
    }

    private void buildTableNameSet(TargetDB targetDB, Set<String> resultSet) {
        for (String key : resultSet) {
            Table table = (Table) subSharedElement.get(key);
            if (table == null) {
                throw new IllegalArgumentException("cant find table by target index :" + key
                                                   + " current sub tables is " + subSharedElement);
            }
            targetDB.addOneTable(table.getTableName());
        }
    }

    private Map<String, Set<Object>> getEnumeratedSqlArgsMap(
                                                             CalculationContextInternal calculationContext,
                                                             ListAbstractResultRule rule) {
        if (rule == null) {
            throw new IllegalStateException("should not be here");
        }
        //ǿת ��Ҫ�õ�����ķ���
        Map<String, Set<Object>> argsFromSQL = RuleUtils.getSamplingField(
            calculationContext.sqlArgs, rule.getParameters());
        return argsFromSQL;
    }

    private Set<String> buildDefaultTable() {
        Set<String> resultMap;
        resultMap = new HashSet<String>();
        for (String defaultIndex : defaultListResult) {
            resultMap.add(defaultIndex);
        }
        return resultMap;
    }

    private Set<String> builSingleTable() {
        Set<String> resultMap = new HashSet<String>(1);
        for (String key : subSharedElement.keySet()) {
            resultMap.add(key);
        }
        return resultMap;
    }

    /**
     * �����ǰ�ڵ�û��tableMapProvider����ôʹ��1�Զำ����tableMapProvider.
     */
    void initTableMapProvider() {
        if (this.tableMapProvider == null) {
            this.tableMapProvider = oneToManyEntry.getTableMapProvider();
        }
    }

    /**
     * �����ǰ�ڵ�û��logicTableName,��ôʹ��1�Զำ���logictable.
     */
    void initLogicTableName() {
        String logicTable = oneToManyEntry.getLogicTableName();
        if (logicTableName == null || logicTableName.length() == 0) {
            this.logicTableName = logicTable;
        }
    }

    /**
     * ��ʼ��tableRuleChain,�����ǰ�ڵ��ListRule��Ϊ��,RuleChainΪ����ʹ��listRule�½�һ��ruleChain.
     * ���listRuleΪ�գ�RuleChainҲΪ�գ���ôʹ��1�Զำ���ruleChain. ���ruleChain��Ϊ�գ����ʼ��֮
     */
    void initTableRuleChain() {
        RuleChain ruleChain = oneToManyEntry.getTableRuleChain();
        // ���tableRuleList ��Ϊ�գ�����ruleChain == �ա���tableRuleList
        //������ʵ��
        if (this.tableRuleList != null) {
            if (listResultRule != null) {
                throw new IllegalArgumentException("��tableRuleList����ָ����ruleChain");
            } else {
                listResultRule = OneToManyEntry.getRuleChain(tableRuleList);
            }
        }
        //�����ʵ��δָ������ǰdatabase���������ʹ�ô��ݺ�Ĺ���
        if (listResultRule == null) {
            listResultRule = ruleChain;
        }
        //��ǰdatabase�Ѿ��й��������£���ʼ����ǰ������Ϊrulechain�����ظ���ʼ��������ֻ���ʼ��һ�Ρ�
        if (ruleChain != null) {
            listResultRule.init();
        } else {
            log.warn("rule chain size is 0");
        }
    }

    protected Map<String, SharedElement> getTableMapByTableMapProvider() {
        TableMapProvider provider = getTableMapProvider();
        provider.setParentID(this.getId());
        provider.setLogicTable(getLogicTableName());
        Map<String, SharedElement> beConstructedMap = provider.getTablesMap();
        return beConstructedMap;
    }

    /**
     * �����ñ�ݷ������ɵ���Ԫ��map���õ���ǰ�ڵ����Ԫ��map�����С�
     * 
     * �����ǰ�ӽڵ����Ԫ��map����Ϊ����ֱ������
     * 
     * �����ǰ�ӽڵ㲻Ϊ��,���ʾҵ��ͨ��spring�ķ�ʽset��һ�����е�map����
     * 
     * ���map�����ȼ�Ҫ���Զ����ɵ�map�����ȼ�Ҫ�ߡ�
     * 
     * @param beingConstructedMap
     */
    protected void putAutoConstructedMapIntoCurrentTagMap(
                                                          Map<String, SharedElement> beingConstructedMap) {
        if (this.subSharedElement == null) {
            subSharedElement = beingConstructedMap;
        } else {
            if (beingConstructedMap != null) {
                // ���Զ����table��ͬʱ����ͳһ������ô�����ϲ����Զ�����򸲸�ͨ��ͳһ����
                beingConstructedMap.putAll(subSharedElement);
                subSharedElement = beingConstructedMap;
            }
            // else
            // û���Զ����ɹ���ֻ���Զ��������ôʲô���鶼����
        }
    }

    /**
     * 1��databse��Ӧһ��tables
     * 
     * @param tables
     */
    public void setTables(Map<String, SharedElement> tablesMap) {
        super.subSharedElement = tablesMap;
    }

    @SuppressWarnings("unchecked")
    public Map<String, SharedElement> getTables() {
        return (Map<String, SharedElement>) super.subSharedElement;
    }

    private Map<String, SharedElement> getTablesMapByStringList(List<String> tablesString) {

        List<Table> tables = null;
        tables = new ArrayList<Table>(tablesString.size());

        for (String tabName : tablesString) {
            Table tab = new Table();
            tab.setTableName(tabName);
            tables.add(tab);
        }
        Map<String, SharedElement> returnMap = RuleUtils
            .getSharedElemenetMapBySharedElementList(tables);
        return returnMap;
    }

    /**
     * ����ҵ��ֱ��ͨ��string->string�ķ�ʽ��ָ������
     * 
     * @param tablesMapString
     */
    protected void setTablesMapString(Map<String/* ���index */, String/* ���ʵ�ʱ��� */> tablesMapString) {
        Map<String, SharedElement> beingConstructedMap = new HashMap<String, SharedElement>(
            tablesMapString.size());

        for (Entry<String, String> entry : tablesMapString.entrySet()) {
            Table table = new Table();
            table.setTableName(entry.getValue());
            beingConstructedMap.put(entry.getKey(), table);
        }
        putAutoConstructedMapIntoCurrentTagMap(beingConstructedMap);
    }

    @SuppressWarnings("unchecked")
    public void setTablesMapSimple(Object obj) {
        if (obj instanceof Map) {
            setTablesMapString((Map<String/* ���index */, String/* ���ʵ�ʱ��� */>) obj);
        } else if (obj instanceof List) {
            setTablesList((List) obj);
        }
    }

    /**
     * ����ҵ��ʹ��table1,table2,table3�ķ�ʽ��ָ��������key=�����±ꡣ ͬʱҲ����ҵ��ʹ��list.add("table1");
     * list.add("table2");... �ķ�ʽ��ָ����Key=�����±�
     * 
     * @param tablesString
     */
    protected void setTablesList(List<String> tablesString) {
        // û����tablesStringlist��not null��飬��Ϊ����spring
        if (tablesString.size() == 1) {
            String[] tokens = tablesString.get(0).split(",");
            tablesString = new ArrayList<String>();
            tablesString.addAll(Arrays.asList(tokens));
            putAutoConstructedMapIntoCurrentTagMap(getTablesMapByStringList(tablesString));
        } else {
            putAutoConstructedMapIntoCurrentTagMap(getTablesMapByStringList(tablesString));
        }
    }

    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    @Override
    public String toString() {
        return "Database [dataSourceKey=" + dataSourceKey + ", defaultListResult="
               + defaultListResult + ", defaultListResultStragety=" + defaultListResultStragety
               + ", listResultRule=" + listResultRule + ", subSharedElement=" + subSharedElement
               + "]";
    }

    public void setLogicTableName(String logicTable) {
        this.logicTableName = logicTable;
    }

    public void setTableMapProvider(TableMapProvider tableMapProvider) {
        this.tableMapProvider = tableMapProvider;
    }

    public void setTableRule(List<ListAbstractResultRule> tableRule) {
        this.tableRuleList = tableRule;
    }

    public String getLogicTableName() {
        return logicTableName;
    }

    public TableMapProvider getTableMapProvider() {
        return tableMapProvider;
    }

    public RuleChain getRuleChain() {
        return super.listResultRule;
    }

    public void setTableRuleChain(RuleChain ruleChain) {
        super.listResultRule = ruleChain;
    }

    public void put(OneToManyEntry oneToManyEntry) {
        this.oneToManyEntry = oneToManyEntry;
    }

}
