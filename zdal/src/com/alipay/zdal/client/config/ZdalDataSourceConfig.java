/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.config;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.Constants;
import com.alipay.zdal.common.DBType;
import com.alipay.zdal.common.lang.StringUtil;

/**
 * mysql/oracle/db2��������Դ�����ü���.
 * 
 * @author ����
 * @version $Id: ZdalDataSourceConfig.java, v 0.1 2013-1-18 ����03:48:28 Exp $
 */
public abstract class ZdalDataSourceConfig {

    /** ר�Ŵ�ӡ���ͽ����log��Ϣ. */
    protected static final Logger  CONFIG_LOGGER = Logger
                                                     .getLogger(Constants.CONFIG_LOG_NAME_LOGNAME);

    /** app���� */
    protected String               appName;

    /** ����Դ������. */
    protected String               appDsName     = null;

    /** �������ݿ�Ļ���,����,����,���ϻ�����. */
    protected String               dbmode;

    /** ���������ļ���ŵ�·��. */
    protected String               configPath;

    /** ����Դ��������Ϣ. */
    protected ZdalConfig           zdalConfig    = null;

    /** ���ڱ�ʾZdalDataSource�Ƿ��ʼ�����. */
    protected AtomicBoolean        inited        = new AtomicBoolean(false);

    protected DataSourceConfigType dbConfigType  = null;

    /** ���ݿ�����. */
    protected DBType               dbType;

    /**
     * ����������.
     */
    protected void checkParameters() {
        if (StringUtil.isBlank(appName)) {
            throw new IllegalArgumentException("ERROR ## the appName is null");
        }
        CONFIG_LOGGER.warn("WARN ## the appName = " + this.appName);

        if (StringUtil.isBlank(appDsName)) {
            throw new IllegalArgumentException("ERROR ## the appDsName is null");
        }
        CONFIG_LOGGER.warn("WARN ## the appDsName = " + this.appDsName);

        if (StringUtil.isBlank(dbmode)) {
            throw new IllegalArgumentException("ERROR ## the dbmode is null");
        }
        CONFIG_LOGGER.warn("WARN ## the dbmode = " + this.dbmode);

        if (StringUtil.isBlank(configPath)) {
            throw new IllegalArgumentException("ERROR ## the configPath is null");
        }
        CONFIG_LOGGER.warn("WARN ## the configPath = " + this.configPath);
    }

    /**
     * Ӧ��ʹ��ʱ�������ȵ���initZdalDataSource��������ʼ��.
     */
    protected void initZdalDataSource() {
        long startInit = System.currentTimeMillis();
        this.zdalConfig = ZdalConfigurationLoader.getInstance().getZdalConfiguration(appName,
            dbmode, appDsName, configPath);
        this.dbConfigType = zdalConfig.getDataSourceConfigType();
        this.dbType = zdalConfig.getDbType();
        this.initDataSources(zdalConfig);
        this.inited.set(true);
        CONFIG_LOGGER.warn("WARN ## init ZdalDataSource [" + appDsName + "] success,cost "
                           + (System.currentTimeMillis() - startInit) + " ms");
    }

    /**
     * ��ʼ��mysql/oracle/db2������Դ.
     */
    protected abstract void initDataSources(ZdalConfig zdalConfig);

    public ZdalConfig getZdalConfig() {
        return zdalConfig;
    }

    public DataSourceConfigType getDbConfigType() {
        return dbConfigType;
    }

    public DBType getDbType() {
        return dbType;
    }

    // �����get/set��Ӧ�Ĳ�����Ҫ�ڳ�ʼ����ʱ������.
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDsName() {
        return appDsName;
    }

    public void setAppDsName(String appDsName) {
        this.appDsName = appDsName;
    }

    public String getDbmode() {
        return dbmode;
    }

    public void setDbmode(String dbmode) {
        this.dbmode = dbmode.toLowerCase();
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

}
