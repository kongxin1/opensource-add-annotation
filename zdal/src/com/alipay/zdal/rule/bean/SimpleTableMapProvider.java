/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alipay.zdal.rule.ruleengine.entities.abstractentities.SharedElement;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider;
import com.alipay.zdal.rule.ruleengine.util.RuleUtils;

/**
 * �ṩͨ��ƴװ�ķ�ʽ������SimpleTableMap�ķ�ʽ
 * 
 * 
 * 
 * 
 */
public class SimpleTableMapProvider implements TableMapProvider {

    public enum TYPE {
        NORMAL, CUSTOM
    }

    public static final String NORMAL_TAOBAO_TYPE             = "NORMAL";

    public static final String DEFAULT_PADDING                = "_";

    protected static final int DEFAULT_INT                    = -1;

    public static final int    DEFAULT_TABLES_NUM_FOR_EACH_DB = -1;

    private String             type                           = NORMAL_TAOBAO_TYPE;
    /**
     * table[padding]suffix
     * Ĭ�ϵ�padding��_
     */
    private String             padding                        = DEFAULT_PADDING;
    /**
     * width ���
     */
    private int                width                          = DEFAULT_INT;
    /**
     * �ֱ��ʶ���ӡ�����˵�ʼ��ͷ����ʲô�������ָ����Ĭ�����߼�����
     */
    private String             tableFactor;
    /**
     * �߼�����
     */
    private String             logicTable;
    /**
     * ÿ��������
     */
    private int                step                           = 1;

    /**
     * ÿ�����ݿ�ı�ĸ��������ָ����������ÿ�����ڵĸ�����Ϊָ�����
     */
    private int                tablesNumberForEachDatabases   = DEFAULT_TABLES_NUM_FOR_EACH_DB;
    /**
     * database id
     */
    private String             parentID;
    /**
     * ÿ�����ݿ�ı�ĸ����ж��ٸ�
     * >= ?
     */
    private int                from                           = DEFAULT_INT;
    /**
     * <= ?
     */
    private int                to                             = DEFAULT_INT;

    protected boolean doesNotSetTablesNumberForEachDatabases() {
        return tablesNumberForEachDatabases == -1;
    }

    public int getFrom() {
        return from;
    }

    public String getPadding() {
        return padding;
    }

    public String getParentID() {
        return parentID;
    }

    public int getStep() {
        return step;
    }

    static public String getSuffixInit(int w, int i) {
        String suffix = null;
        if (w != DEFAULT_INT) {
            suffix = RuleUtils.placeHolder(w, i);
        } else {
            //�������ʽָ��width����ָ��Ϊ-1���򲻲��㣬ֱ������ֵΪ��׺				
            suffix = String.valueOf(i);
        }
        return suffix;
    }

    protected List<String> getSuffixList(int from, int to, int width, int step, String tableFactor,
                                         String padding) {
        if (from == DEFAULT_INT || to == DEFAULT_INT) {
            throw new IllegalArgumentException("from,to must be spec!");
        }
        int length = to - from + 1;
        List<String> tableList = new ArrayList<String>(length);
        StringBuilder sb = new StringBuilder();
        sb.append(tableFactor);
        sb.append(padding);

        for (int i = from; i <= to; i = i + step) {
            StringBuilder singleTableBuilder = new StringBuilder(sb.toString());

            String suffix = getSuffixInit(width, i);
            singleTableBuilder.append(suffix);
            tableList.add(singleTableBuilder.toString());

        }
        return tableList;
    }

    public String getTableFactor() {
        return tableFactor;
    }

    /** 
     * @see com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider#getTablesMap()
     */
    public Map<String, SharedElement> getTablesMap() {
        TYPE typeEnum = TYPE.valueOf(type);
        //		switch (typeEnum) {
        //		case NORMAL:
        //			// ���������������£���ôӦ���Ǳ�������+"_"+�����λ����������β׺
        //			// ���� tab_001~tab_100
        //			
        //			break;
        //		//custom�ķ�ʽ�£�����Ҫ���Ĭ�ϵ�padding��width,ֱ���˳�case
        //		default:
        //			break;
        //		}
        makeRealTableNameTaobaoLike(typeEnum);
        List<String> tableNames;

        if (tableFactor == null && logicTable != null) {
            tableFactor = logicTable;
        }
        if (tableFactor == null) {
            throw new IllegalArgumentException("û�б�����������");
        }

        // ���û������ÿ�����ݿ��ĸ�������ô��ʾ���б���ͳһ�ı���������(tab_0~tab_3)*16�����ݿ�=64�ű�
        if (doesNotSetTablesNumberForEachDatabases()) {
            tableNames = getSuffixList(from, to, width, step, tableFactor, padding);
        } else {
            // ���������ÿ�����ݿ��ĸ�������ô��ʾ���б��ò�ͬ�ı���������(tab_0~tab63),�ֲ���16�����ݿ���
            int multiple = 0;
            try {
                multiple = Integer.valueOf(parentID);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "ʹ��simpleTableMapProvider����ָ����tablesNumberForEachDatabase������database��indexֵ�����Ǹ�integer����"
                            + "��ǰdatabase��index��:" + parentID);
            }
            int start = tablesNumberForEachDatabases * multiple;
            // ��Ϊβ׺�ķ�Χ�ǵ�<=�����֣�����Ҫ-1.
            int end = start + tablesNumberForEachDatabases - 1;
            //���õ�ǰdatabase����ı���
            tableNames = getSuffixList(start, end, width, step, tableFactor, padding);
        }
        List<Table> tables = null;
        tables = new ArrayList<Table>(tableNames.size());
        for (String tableName : tableNames) {
            Table tab = new Table();
            tab.setTableName(tableName);
            tables.add(tab);
        }
        Map<String, SharedElement> returnMap = RuleUtils
            .getSharedElemenetMapBySharedElementList(tables);
        return returnMap;
    }

    public int getTablesNumberForEachDatabases() {
        return tablesNumberForEachDatabases;
    }

    public int getTo() {
        return to;
    }

    public String getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    /**
     * ���������������£���ôӦ���Ǳ�������+"_"+�����λ����������β׺������ tab_001~tab_100
     */
    protected void makeRealTableNameTaobaoLike(TYPE typeEnum) {
        if (typeEnum == TYPE.CUSTOM) {
            //do nothing
        } else {
            if (padding == null)
                padding = DEFAULT_PADDING;
            if (to != DEFAULT_INT && width == DEFAULT_INT) {
                width = String.valueOf(to).length();
            }
        }

    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setLogicTable(String logicTable) {
        this.logicTable = logicTable;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setTableFactor(String tableFactor) {
        this.tableFactor = tableFactor;
    }

    public void setTablesNumberForEachDatabases(int tablesNumberForEachDatabases) {
        this.tablesNumberForEachDatabases = tablesNumberForEachDatabases;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWidth(int width) {
        if (width > 8) {
            throw new IllegalArgumentException("ռλ�����ܳ���8λ");
        }
        //���׺ռλ������Ϊ0, ��ʱ������
        if (width < 0) {
            throw new IllegalArgumentException("ռλ������Ϊ��ֵ");
        }
        this.width = width;

    }

}
