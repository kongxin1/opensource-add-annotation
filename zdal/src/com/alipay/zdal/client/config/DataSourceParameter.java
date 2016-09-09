/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.config;

import java.util.HashMap;
import java.util.Map;

import com.alipay.zdal.client.config.bean.PhysicalDataSourceBean;

/**
 * ����Դ������Ϣ�ĸ�������.
 * 
 * @author ����
 * @version $Id: DataSourceParameter.java, v 0.1 2012-11-17 ����4:06:30 Exp $
 */
public class DataSourceParameter {

	private String jdbcUrl = "";

	private String userName = "";

	private String password = "";

	/** ���ӳ��л����С������ */
	private int minConn;

	/** ���ӳ��л����������� */

	private int maxConn;

	private String driverClass = "";

	private int blockingTimeoutMillis = 180;

	private int idleTimeoutMinutes = 30;

	private int preparedStatementCacheSize;

	private int queryTimeout = 30;

	private boolean prefill;

	private String checkValidConnectionSQL;
	private Map<String, String> connectionProperties = new HashMap<String, String>();

	public Map<String, String> getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(Map<String, String> connectionProperties) {
		this.connectionProperties = connectionProperties;
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

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
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

	public static DataSourceParameter valueOf(PhysicalDataSourceBean bean) {
		DataSourceParameter paramter = new DataSourceParameter();
		paramter.setBlockingTimeoutMillis(bean.getBlockingTimeoutMillis());
		paramter.setDriverClass(bean.getDriverClass());
		paramter.setIdleTimeoutMinutes(bean.getIdleTimeoutMinutes());
		paramter.setJdbcUrl(bean.getJdbcUrl());
		paramter.setMaxConn(bean.getMaxConn());
		paramter.setMinConn(bean.getMinConn());
		paramter.setPassword(bean.getPassword());
		paramter.setPreparedStatementCacheSize(bean
				.getPreparedStatementCacheSize());
		paramter.setQueryTimeout(bean.getQueryTimeout());
		paramter.setUserName(bean.getUserName());
		paramter.setPrefill(bean.isPrefill());
		paramter.setCheckValidConnectionSQL(bean.getCheckValidConnectionSQL());
		paramter.setConnectionProperties(bean.getConnectionProperties());
		return paramter;
	}

	public boolean getPrefill() {
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

}
