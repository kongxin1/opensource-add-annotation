/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.alipay.zdal.common.SqlType;
import com.alipay.zdal.parser.visitor.ZdalSchemaStatVisitor;

/**
 * ���ڻ����������sql�����.
 * @author xiaoqing.zhouxq
 * @version $Id: ParserCache.java, v 0.1 2012-5-25 ����03:32:41 xiaoqing.zhouxq Exp $
 */
public class ParserCache {

    private static final ParserCache INSTANCE = new ParserCache();

    private ParserCache() {
    }

    public static final ParserCache instance() {

        return INSTANCE;

    }

    private final ReentrantLock          lock = new ReentrantLock();

    private final Map<String, ItemValue> map  = new LinkedHashMap<String, ItemValue>();

    public int size() {
        return map.size();
    }

    protected static class ItemValue {

        /**
         * ���ݵ�CRUD����
         */
        private AtomicReference<SqlType>                           sqlType              = new AtomicReference<SqlType>();

        /**
         * ��ȥvirtualTableName���������sql�ֶ�
         */
        private AtomicReference<List<String>>                      tableNameReplacement = new AtomicReference<List<String>>();

        /**
         * ���������sql
         */
        private AtomicReference<FutureTask<ZdalSchemaStatVisitor>> futureVisitor        = new AtomicReference<FutureTask<ZdalSchemaStatVisitor>>();

        public SqlType getSqlType() {
            return sqlType.get();
        }

        public SqlType setSqlTypeIfAbsent(SqlType sqlTypeinput) {
            //���ԭֵΪnull���ԭ�ӵ�������ֵ��ȥ�����ҷ�����ֵ
            if (sqlType.compareAndSet(null, sqlTypeinput)) {
                return sqlTypeinput;
            } else {
                //��������ֵ�Ѿ���Ϊnull�����ȡ��ֵ
                return sqlType.get();
            }
        }

        public List<String> getTableNameReplacement() {
            return tableNameReplacement.get();
        }

        public List<String> setTableNameReplacementIfAbsent(List<String> tableNameReplacementList) {
            //���ԭֵΪnull���ԭ�ӵ�������ֵ��ȥ�����ҷ�����ֵ
            if (tableNameReplacement.compareAndSet(null, tableNameReplacementList)) {
                return tableNameReplacementList;
            } else {
                //��������ֵ�Ѿ���Ϊnull�����ȡ��ֵ
                return tableNameReplacement.get();
            }

        }

        public FutureTask<ZdalSchemaStatVisitor> getFutureVisitor() {
            return futureVisitor.get();
        }

        public FutureTask<ZdalSchemaStatVisitor> setFutureVisitorIfAbsent(FutureTask<ZdalSchemaStatVisitor> future) {
            //���ԭֵΪnull���ԭ�ӵ�������ֵ��ȥ�����ҷ�����ֵ
            if (futureVisitor.compareAndSet(null, future)) {
                return future;
            } else {
                //��������ֵ�Ѿ���Ϊnull�����ȡ��ֵ
                return futureVisitor.get();
            }
        }

    }

    protected ItemValue get(String sql) {
        return map.get(sql);
    }

    public SqlType getSqlType(String sql) {
        ItemValue itemValue = get(sql);
        if (itemValue != null) {
            return itemValue.getSqlType();
        } else {
            return null;
        }
    }

    public SqlType setSqlTypeIfAbsent(String sql, SqlType sqlType) {
        ItemValue itemValue = get(sql);
        SqlType returnSqlType = null;
        if (itemValue == null) {
            //��ȫû�е����������������£��϶�����Ϊ��û���߳����������ĳ��sql
            lock.lock();
            try {
                // ˫���lock
                itemValue = get(sql);
                if (itemValue == null) {

                    itemValue = new ParserCache.ItemValue();

                    put(sql, itemValue);
                }
            } finally {

                lock.unlock();
            }
            //cas ����ItemValue�е�SqlType����
            returnSqlType = itemValue.setSqlTypeIfAbsent(sqlType);

        } else if (itemValue.getFutureVisitor() == null) {
            //cas ����ItemValue�е�SqlType����
            returnSqlType = itemValue.setSqlTypeIfAbsent(sqlType);

        } else {
            returnSqlType = itemValue.getSqlType();
        }

        return returnSqlType;
    }

    public FutureTask<ZdalSchemaStatVisitor> getFutureTask(String sql) {
        ItemValue itemValue = get(sql);
        if (itemValue != null) {
            return itemValue.getFutureVisitor();
        } else {
            return null;
        }

    }

    public List<String> getTableNameReplacement(String sql) {
        ItemValue itemValue = get(sql);
        if (itemValue != null) {
            return itemValue.getTableNameReplacement();
        } else {
            return null;
        }
    }

    public List<String> setTableNameReplacementIfAbsent(String sql,
                                                        List<String> tablenameReplacement) {
        ItemValue itemValue = get(sql);
        List<String> returnList = null;
        if (itemValue == null) {
            //��ȫû�е����������������£��϶�����Ϊ��û���ֳ����������ĳ��sql
            lock.lock();
            try {
                // ˫���lock
                itemValue = get(sql);
                if (itemValue == null) {

                    itemValue = new ParserCache.ItemValue();

                    put(sql, itemValue);
                }
            } finally {

                lock.unlock();
            }
            //cas ����ItemValue�е�TableNameReplacement����
            returnList = itemValue.setTableNameReplacementIfAbsent(tablenameReplacement);

        } else if (itemValue.getTableNameReplacement() == null) {
            //cas ����ItemValue�е�TableNameReplacement����
            returnList = itemValue.setTableNameReplacementIfAbsent(tablenameReplacement);

        } else {
            returnList = itemValue.getTableNameReplacement();
        }

        return returnList;

    }

    public FutureTask<ZdalSchemaStatVisitor> setFutureTaskIfAbsent(String sql,
                                                                   FutureTask<ZdalSchemaStatVisitor> future) {
        ItemValue itemValue = get(sql);
        FutureTask<ZdalSchemaStatVisitor> returnFutureTask = null;
        if (itemValue == null) {
            //��ȫû�е����������������£��϶�����Ϊ��û���ֳ����������ĳ��sql
            lock.lock();
            try {
                // ˫���lock
                itemValue = get(sql);
                if (itemValue == null) {

                    itemValue = new ParserCache.ItemValue();

                    put(sql, itemValue);
                }
            } finally {

                lock.unlock();
            }
            //cas ����ItemValue�е�Visitor����
            returnFutureTask = itemValue.setFutureVisitorIfAbsent(future);

        } else if (itemValue.getFutureVisitor() == null) {
            //cas ����ItemValue�е�Visitor����
            returnFutureTask = itemValue.setFutureVisitorIfAbsent(future);
        } else {
            returnFutureTask = itemValue.getFutureVisitor();
        }

        return returnFutureTask;

    }

    protected void put(String sql, ItemValue itemValue) {
        map.put(sql, itemValue);
    }
}
