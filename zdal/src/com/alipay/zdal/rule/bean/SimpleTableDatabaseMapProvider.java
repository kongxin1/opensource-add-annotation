/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * ֧�ֺ�Database�±�һ���ı�������ʽ,ÿ��Databaseֻ��һ�ű�
 * ����: database01 -> trade_base_01
 *      database02 -> trade_base_02
 * 		database03 -> trade_base_03
 * 
 * @author liang.chenl
 *
 */
public class SimpleTableDatabaseMapProvider extends SimpleTableMapProvider {

    @Override
    protected List<String> getSuffixList(int from, int to, int width, int step, String tableFactor,
                                         String padding) {

        List<String> tableList = new ArrayList<String>(1);
        StringBuilder sb = new StringBuilder();
        sb.append(tableFactor);
        sb.append(padding);

        int multiple = 0;
        try {
            multiple = Integer.valueOf(getParentID());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "ʹ��SimpleTableDatabaseMapProvider��database��indexֵ�����Ǹ�integer����"
                        + "��ǰdatabase��index��:" + getParentID());
        }
        String suffix = getSuffixInit(width, multiple);
        sb.append(suffix);
        tableList.add(sb.toString());
        return tableList;

    }
}
