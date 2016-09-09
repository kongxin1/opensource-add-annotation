/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.alipay.zdal.client.config.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import com.alipay.zdal.common.lang.StringUtil;

/**
 * @author <a href="mailto:xiang.yangx@alipay.com">Yang Xiang</a>
 *
 */
public class PhysicalDataSourceBean implements InitializingBean {

    private static final String ORACLE_DRIVER_CLASS   = "oracle.jdbc.OracleDriver";

    private static final String MYSQL_DRIVER_CLASS    = "com.mysql.jdbc.Driver";

    private static final String DB2_DRIVER_CLASS      = "com.ibm.db2.jcc.DB2Driver";

    /** ��������Դ����. */
    private String              name                  = "";

    /** �����������������Դ���߼�����Դ�б�. */
    private Set<String>         logicDbNameSet;

    /** �������ӵ�jdbcUrl. */
    private String              jdbcUrl               = "";

    /** �������ӵ��û���. */
    private String              userName              = "";

    /** �������ӵ����� */
    private String              password              = "";

    /** ���ӳ��л����С������ */
    private int                 minConn;

    /** ���ӳ��л����������� */

    private int                 maxConn;

    /** �������ӵ�������. */
    private String              driverClass           = "";

    /** ��ȡ���ӵ����ʱʱ�䣬��ָ��ʱ���ڻ�ȡ�������ӣ���һ������ǣ����ӳص�����ȫ����ʹ�ã��ڶ�����������ݿⷱæ�������쳣,��λ������ */
    private int                 blockingTimeoutMillis = 180;

    /** (idleTimeoutMinutes*60*1000)/2(ms)���һ�Σ�����idleTimeoutMinutes*60*1000(ms)û��ʹ�õ����Ӿ��ǿ������ӣ����Զ��޳� ,��λ���� */
    private int                 idleTimeoutMinutes    = 30;

    /** �����preparedStatement��С����Ҫdriver֧�֣�oracle-driver֧�֣�mysql-driver��֧�� */
    private int                 preparedStatementCacheSize;

    /** ִ��execute,executeQuery,excuteUpdate�����ʱ�� */
    private int                 queryTimeout          = 30;

    /** �������ӵĲ����������ݿ����֧��. */
    private Map<String, String> connectionProperties  = new HashMap<String, String>();

    /** ����������Դ��ʼ��ʱ���Ƿ�����С������. */
    private boolean             prefill;
    private String 				checkValidConnectionSQL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getLogicDbNameSet() {
        return logicDbNameSet;
    }

    public void setLogicDbNameSet(Set<String> logicDbNameSet) {
        this.logicDbNameSet = logicDbNameSet;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMinConn() {
        return minConn;
    }

    public void setMinConn(int minConn) {
        this.minConn = minConn;
    }

    public int getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public int getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(int blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    public int getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public int getPreparedStatementCacheSize() {
        return preparedStatementCacheSize;
    }

    public void setPreparedStatementCacheSize(int preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * @return the connectionProperties
     */
    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * @param connectionProperties the connectionProperties to set
     */
    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public boolean isPrefill() {
        return prefill;
    }

    public void setPrefill(boolean prefill) {
        this.prefill = prefill;
    }

    public String getCheckValidConnectionSQL() {
		return checkValidConnectionSQL;
	}

	public void setCheckValidConnectionSQL(String checkValidConnectionSQL) {
		this.checkValidConnectionSQL = checkValidConnectionSQL;
	}

	@Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("ERROR ## the physicalDataSource's name is null");
        }

        if (logicDbNameSet == null || logicDbNameSet.isEmpty()) {
            logicDbNameSet = new HashSet<String>();
            logicDbNameSet.add(name);
        }

        if (StringUtil.isBlank(jdbcUrl)) {
            throw new IllegalArgumentException("ERROR ## the jdbcUrl is null of " + name);
        }
        if (jdbcUrl.contains("oracle")) {
            this.driverClass = ORACLE_DRIVER_CLASS;
        } else if (jdbcUrl.contains("mysql")) {
            this.driverClass = MYSQL_DRIVER_CLASS;
        } else if (jdbcUrl.contains("db2")) {
            this.driverClass = DB2_DRIVER_CLASS;
        } else {
            throw new IllegalArgumentException(
                "ERROR ## the jdbcUrl is invalidate,must contain [oracle,mysql,db2] of " + name);
        }

        if (StringUtil.isBlank(userName)) {
            throw new IllegalArgumentException("ERROR ## the userName is null of " + userName);
        }

        if (StringUtil.isBlank(password)) {
            throw new IllegalArgumentException("ERROR ## the password is null of " + password);
        }

        if (minConn < 0) {
            throw new IllegalArgumentException("ERROR ## the minConn = " + minConn
                                               + " must >=0 of " + minConn);
        }
        if (maxConn < 0) {
            throw new IllegalArgumentException("ERROR ## the maxConn = " + maxConn
                                               + " must >=0 of " + name);
        }
        if (minConn > maxConn) {
            throw new IllegalArgumentException("ERROR ## the maxConn[" + maxConn
                                               + "] must >= minConn[" + minConn + " of " + name);
        }

        if (blockingTimeoutMillis < 0) {
            throw new IllegalArgumentException("ERROR ## the blockingTimeoutMillis = "
                                               + blockingTimeoutMillis + " must >= 0 of " + name);
        }

        if (idleTimeoutMinutes <= 0) {
            throw new IllegalArgumentException("ERROR ## the idleTimeoutMinutes = "
                                               + idleTimeoutMinutes + " must > 0 of " + name);
        }

        if (preparedStatementCacheSize < 0) {
            throw new IllegalArgumentException("ERROR ## the preparedStatementCacheSize = "
                                               + preparedStatementCacheSize + " must >=0 of "
                                               + name);
        }

        if (queryTimeout <= 0) {
            throw new IllegalArgumentException("ERROR ## the queryTimeout  = " + queryTimeout
                                               + " must > 0 of " + name);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blockingTimeoutMillis;
        result = prime * result
                 + ((connectionProperties == null) ? 0 : connectionProperties.hashCode());
        result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
        result = prime * result + idleTimeoutMinutes;
        result = prime * result + ((jdbcUrl == null) ? 0 : jdbcUrl.hashCode());
        result = prime * result + ((logicDbNameSet == null) ? 0 : logicDbNameSet.hashCode());
        result = prime * result + maxConn;
        result = prime * result + minConn;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (prefill ? 1231 : 1237);
        result = prime * result + preparedStatementCacheSize;
        result = prime * result + queryTimeout;
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhysicalDataSourceBean other = (PhysicalDataSourceBean) obj;
        if (blockingTimeoutMillis != other.blockingTimeoutMillis)
            return false;
        if (connectionProperties == null) {
            if (other.connectionProperties != null)
                return false;
        } else if (!connectionProperties.equals(other.connectionProperties))
            return false;
        if (driverClass == null) {
            if (other.driverClass != null)
                return false;
        } else if (!driverClass.equals(other.driverClass))
            return false;
        if (idleTimeoutMinutes != other.idleTimeoutMinutes)
            return false;
        if (jdbcUrl == null) {
            if (other.jdbcUrl != null)
                return false;
        } else if (!jdbcUrl.equals(other.jdbcUrl))
            return false;
        if (logicDbNameSet == null) {
            if (other.logicDbNameSet != null)
                return false;
        } else if (!logicDbNameSet.equals(other.logicDbNameSet))
            return false;
        if (maxConn != other.maxConn)
            return false;
        if (minConn != other.minConn)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (prefill != other.prefill)
            return false;
        if (preparedStatementCacheSize != other.preparedStatementCacheSize)
            return false;
        if (queryTimeout != other.queryTimeout)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PhysicalDataSourceBean [blockingTimeoutMillis=" + blockingTimeoutMillis
               + ", connectionProperties=" + connectionProperties + ", driverClass=" + driverClass
               + ", idleTimeoutMinutes=" + idleTimeoutMinutes + ", jdbcUrl=" + jdbcUrl
               + ", logicDbNameSet=" + logicDbNameSet + ", maxConn=" + maxConn + ", minConn="
               + minConn + ", name=" + name + ", password=" + password + ", prefill=" + prefill
               + ", preparedStatementCacheSize=" + preparedStatementCacheSize + ", queryTimeout="
               + queryTimeout + ", userName=" + userName + "]";
    }

}
