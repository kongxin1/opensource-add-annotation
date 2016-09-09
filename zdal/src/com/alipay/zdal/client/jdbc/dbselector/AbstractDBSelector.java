/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc.dbselector;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import com.alipay.zdal.client.jdbc.DBSelector;
import com.alipay.zdal.client.jdbc.ZdalStatement.DB_OPERATION_TYPE;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.jdbc.sorter.DB2ExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.ExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.MySQLExceptionSorter;
import com.alipay.zdal.common.jdbc.sorter.OracleExceptionSorter;

public abstract class AbstractDBSelector implements DBSelector {
    private DBType            dbType          = DBType.MYSQL;
    protected ExceptionSorter exceptionSorter = new MySQLExceptionSorter();
    private String            id;

    /**
     * added by fanzeng.
     * ��ʾͬһ��dbSeletor������ѡ�񵽵�����Դ��ʶ��
     */
    private String            selectedDSName;

    //����Դ���ƣ����������Ϣ��add by ���� 20130903
    private String            appDsName;

    public AbstractDBSelector(String id) {
        this.id = id;
    }

    public String getSelectedDSName() {
        return selectedDSName;
    }

    public void setSelectedDSName(String setSelectedDSName) {
        this.selectedDSName = setSelectedDSName;
    }

    public <T> T tryExecute(DataSourceTryer<T> tryer, int times, DB_OPERATION_TYPE operationType,
                            Object... args) throws SQLException {
        return this.tryExecute(new LinkedHashMap<DataSource, SQLException>(0), tryer, times,
            operationType, args);
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
        if (dbType.isMysql()) {
            this.exceptionSorter = new MySQLExceptionSorter();
        } else if (dbType.isOracle()) {
            this.exceptionSorter = new OracleExceptionSorter();
        } else if (dbType.isDB2()) {
            this.exceptionSorter = new DB2ExceptionSorter();
        }
    }

    public String getId() {
        return id;
    }

    public void setAppDsName(String appDsName) {
        this.appDsName = appDsName;
    }

    public String getAppDsName() {
        return appDsName;
    }
}
