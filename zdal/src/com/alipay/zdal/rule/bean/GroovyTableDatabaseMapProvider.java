/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.util.TableSuffixTypeEnum;
import com.alipay.zdal.rule.config.beans.Preffix;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.SharedElement;

/**
 * �����û����õ�һ��groove�ű�����һ��List<string> ֱ��ƴ��һ��map����
 * 
 * 
 */
public class GroovyTableDatabaseMapProvider extends SimpleTableMapProvider {

    private static final Logger        log                 = Logger
                                                               .getLogger(GroovyTableDatabaseMapProvider.class);

    private List<String>               tableNames;
    /**
     * ���ݿ�ĸ���
     */
    private int                        dbNumber;

    private String                     expression;                                                              // groovy script

    private Binding                    binding             = new Binding();

    private GroovyShell                shell               = new GroovyShell(binding);
    /**
     * ����ǰ� Ϊ��ʱ��Ĭ��ʹ���߼����� logicTable
     */
    private Preffix                    tbPreffix;
    /**
     * �߼�����
     */
    private String                     logicTable;

    private static Map<String, Object> tbSuffixRuleResult  = new HashMap<String, Object>();

    /**
     * �ֱ��׺��������
     */
    private String                     tbType;

    private DBAndNumberRelation        dbAndNumberRelation = null;

    private static class DBAndNumberRelation {
        private final int[] dbNumber;
        private final int[] tbNumber;
        private final int[] dbNumberEnds;
        private final int[] tbNumberEnds;

        public DBAndNumberRelation(int[] dbNumber, int[] tbNumber) {
            this.dbNumber = dbNumber;
            this.tbNumber = tbNumber;
            this.dbNumberEnds = genDBNumberEnds(dbNumber);
            this.tbNumberEnds = genTbNumberEnds(dbNumber, tbNumber);
        }

        /**
         * ��ȡ�������
         * 
         * @param dbNumber
         * @return
         */
        private int[] genDBNumberEnds(int[] dbNumber) {
            if (dbNumber == null) {
                return null;
            }
            int[] dbNumberEnds = new int[dbNumber.length];
            int sum = 0;
            for (int i = 0; i < dbNumber.length; i++) {
                sum += dbNumber[i];
                dbNumberEnds[i] = sum;
            }
            return dbNumberEnds;
        }

        /**
         * ��ȡ�������
         * 
         * @param dbNumber
         * @param tbNumber
         * @return
         */
        private int[] genTbNumberEnds(int[] dbNumber, int[] tbNumber) {
            if (dbNumber == null) {
                return null;
            }
            int[] tbNumberEnds = new int[dbNumber.length];
            int sum = 0;
            for (int i = 0; i < dbNumber.length; i++) {
                sum += dbNumber[i] * tbNumber[i];
                tbNumberEnds[i] = sum;
            }
            return tbNumberEnds;
        }
    }

    /**
     * ����Ӧ����д��groovy�ű����õ�һ����������table��list
     * 
     * @param expression
     * @return tables list
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected List<String> getTableNames(String tableFactory, String expression)
                                                                                throws IllegalArgumentException {
        if (expression == null || expression.equals("")) {
            throw new IllegalArgumentException("groovy script is null or empty!");
        }
        long time1 = System.currentTimeMillis();
        // Object value = shell.evaluate(expression);
        Object value = tbSuffixRuleResult.get(expression);
        if (value == null) {
            value = shell.evaluate(expression);
            tbSuffixRuleResult.put(expression, value);
        }
        long time2 = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("tbsuffix shell use time:" + (time2 - time1) + ",expression=" + expression);
        }

        /*
         * ���ݽű����ɵĺ�������ǰ����������յ�ʵ�ʱ����б�
         */

        if (value instanceof List) {
            List<String> tableNameList = (List<String>) value;
            List<String> finalList = new ArrayList<String>(tableNameList.size());
            // ÿ�����������еı�
            if (TableSuffixTypeEnum.groovyTableList.getValue().equals(this.tbType)) {
                String temp = this.getPadding();
                for (String tname : tableNameList) {
                    finalList.add(new StringBuilder(tableFactory).append(temp).append(tname)
                        .toString());
                }
            } else if (TableSuffixTypeEnum.groovyThroughAllDBTableList.getValue().equals(
                this.tbType)) {
                // �������ڸ÷ֿ�ı�ĺ�׺
                int multiple = Integer.valueOf(super.getParentID());
                int tableNumberForEachDB = tableNameList.size() / dbNumber;
                int begin = multiple * tableNumberForEachDB;
                int end = begin + tableNumberForEachDB;
                String temp = this.getPadding();
                for (int i = begin; i < end; i++) {
                    String tname = tableNameList.get(i);
                    finalList.add(new StringBuilder(tableFactory).append(temp).append(tname)
                        .toString());
                }
            } else if (TableSuffixTypeEnum.groovyAdjustTableList.getValue().equals(this.tbType)) {
                // check һ��
                if (this.dbAndNumberRelation.tbNumberEnds[this.dbAndNumberRelation.tbNumber.length - 1] != tableNameList
                    .size()
                    || this.dbAndNumberRelation.dbNumberEnds[this.dbAndNumberRelation.dbNumber.length - 1] != this.dbNumber) {
                    throw new IllegalArgumentException(
                        "The size is different, error. tableNameList.size=" + tableNameList.size()
                                + ",dbNumber=" + this.dbNumber);
                }

                int dbId = Integer.valueOf(super.getParentID());
                int number = 0;
                for (int i = 0; i < this.dbAndNumberRelation.dbNumberEnds.length; i++) {
                    if (dbId < (this.dbAndNumberRelation.dbNumberEnds)[i]) {
                        number = i;
                        break;
                    }
                }
                int begin = 0;
                if (number > 0) {
                    for (int j = 0; j < number; j++) {
                        begin = begin + this.dbAndNumberRelation.dbNumber[j]
                                * this.dbAndNumberRelation.tbNumber[j];
                    }
                    begin = begin + (dbId - this.dbAndNumberRelation.dbNumberEnds[number - 1])
                            * this.dbAndNumberRelation.tbNumber[number];
                } else {
                    begin = dbId * this.dbAndNumberRelation.tbNumber[0];
                }
                int end = begin + this.dbAndNumberRelation.tbNumber[number];
                String temp = this.getPadding();
                for (int i = begin; i < end; i++) {
                    String tname = tableNameList.get(i);
                    finalList.add(new StringBuilder(tableFactory).append(temp).append(tname)
                        .toString());
                }

            }
            if (log.isDebugEnabled()) {
                log.debug("dbId=" + super.getParentID() + ",tableNameList=" + finalList);
            }
            /*
             * //���ָ��ÿ���ֱ��ֱ�ĸ���Ϊ-1��˵��ÿ���ⶼ�ǳ�ʼ�����еı� if
             * (super.doesNotSetTablesNumberForEachDatabases()) { String temp =
             * this.getPadding(); for (String tname : tableNameList) {
             * finalList.add(new
             * StringBuilder(tableFactory).append(temp).append(tname)
             * .toString()); } } else { //�������ڸ÷ֿ�ı�ĺ�׺ int multiple =
             * Integer.valueOf(super.getParentID()); int tableNumberForEachDB =
             * tableNameList.size() / dbNumber; int begin = multiple *
             * tableNumberForEachDB; int end = begin + tableNumberForEachDB;
             * String temp = this.getPadding(); for (int i = begin; i < end;
             * i++) { String tname = tableNameList.get(i); finalList.add(new
             * StringBuilder(tableFactory).append(temp).append(tname)
             * .toString()); } }
             */
            return finalList;
        } else {
            throw new IllegalArgumentException("ִ�нű��õ��˴��������!����");
        }
    }

    /**
     * ���������е�groovy�ű�������һ��table map
     * 
     * @return Map<String, SharedElement>
     */
    public Map<String, SharedElement> getTablesMap() {

        if (getTbPreffix() != null) {
            setTableFactor(getTbPreffix().getTbPreffix());
        } else if (getTableFactor() == null && getLogicTable() != null) {
            setTableFactor(logicTable);
        }
        if (getTableFactor() == null) {
            throw new IllegalArgumentException("û�б�����������");
        }

        try {
            this.tableNames = getTableNames(getTableFactor(), getExpression());
        } catch (Exception e) {
            log.error("���ܵõ�tablesNames ,ֱ�ӷ���null. ", e);
            return null;
        }
        List<Table> tables = null;
        tables = new ArrayList<Table>(tableNames.size());
        for (String tableName : tableNames) {
            Table tab = new Table();
            tab.setTableName(tableName);
            if (log.isDebugEnabled()) {
                log.debug("Groovy table databases provide : get table names from groovy script "
                          + tableName);
            }
            tables.add(tab);
        }
        Map<String, SharedElement> returnMap = getSharedElemenetMapBySharedElementList(tables);

        return returnMap;
    }

    public Map<String, SharedElement> getSharedElemenetMapBySharedElementList(
                                                                              List<? extends SharedElement> sharedElementList) {
        Map<String, SharedElement> returnMap = new HashMap<String, SharedElement>();

        if (sharedElementList != null) {
            for (SharedElement sharedElement : sharedElementList) {
                Table t = (Table) sharedElement;
                String key = t.getTableName().substring(
                    getTableFactor().length() + this.getPadding().length());
                // String key =
                // t.getTableName().substring(getTableFactor().length());
                returnMap.put(key, sharedElement);
            }
        }
        return returnMap;
    }

    @Override
    protected List<String> getSuffixList(int from, int to, int width, int step, String tableFactor,
                                         String padding) {
        return null;

    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        // �������������ķ�ʽ���������⴦��
        if (TableSuffixTypeEnum.groovyAdjustTableList.getValue().equals(this.tbType)) {
            String temp[] = expression.split(":");
            // �ж��Ƿ����[20_10,200_1]��ʽ
            if (!temp[0].startsWith("[") || !temp[0].endsWith("]")) {
                throw new IllegalArgumentException("The tbsuffix of groovyAjustTableList error!");
            }
            // ȥ��[]
            String part = temp[0].substring(1, temp[0].length() - 1);
            String[] part2 = part.split(",");
            int[] dbNumber = new int[part2.length];
            int[] tbNumber = new int[part2.length];
            for (int i = 0; i < part2.length; i++) {
                String temp2[] = part2[i].split("_");
                if (temp2.length != 2) {
                    throw new IllegalArgumentException("The db_table number relation error! it is "
                                                       + part);
                }
                dbNumber[i] = Integer.parseInt(temp2[0].trim());
                tbNumber[i] = Integer.parseInt(temp2[1].trim());
            }
            this.setDbAndNumberRelation(new DBAndNumberRelation(dbNumber, tbNumber));
            expression = temp[1].trim();
        }
        this.expression = expression;
    }

    public String getLogicTable() {
        return logicTable;
    }

    public void setLogicTable(String logicTable) {
        this.logicTable = logicTable;
    }

    public Preffix getTbPreffix() {
        return tbPreffix;
    }

    public void setTbPreffix(Preffix tbPreffix) {
        this.tbPreffix = tbPreffix;
    }

    /**
     * Setter method for property <tt>dbNumber</tt>.
     * 
     * @param dbNumber
     *            value to be assigned to property dbNumber
     */
    public void setDbNumber(int dbNumber) {
        this.dbNumber = dbNumber;
    }

    /**
     * Getter method for property <tt>dbNumber</tt>.
     * 
     * @return property value of dbNumber
     */
    public int getDbNumber() {
        return dbNumber;
    }

    public String getTbType() {
        return tbType;
    }

    public void setTbType(String tbType) {
        this.tbType = tbType;
    }

    /**
     * Setter method for property <tt>dbAndNumberRelation</tt>.
     * 
     * @param dbAndNumberRelation
     *            value to be assigned to property dbAndNumberRelation
     */
    public void setDbAndNumberRelation(DBAndNumberRelation dbAndNumberRelation) {
        this.dbAndNumberRelation = dbAndNumberRelation;
    }

    /**
     * Getter method for property <tt>dbAndNumberRelation</tt>.
     * 
     * @return property value of dbAndNumberRelation
     */
    public DBAndNumberRelation getDbAndNumberRelation() {
        return dbAndNumberRelation;
    }

}
