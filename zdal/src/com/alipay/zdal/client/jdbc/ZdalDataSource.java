/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import javax.sql.DataSource;

import com.alipay.zdal.client.exceptions.ZdalClientException;
import com.alipay.zdal.common.Closable;

/**
 * Zdal ���⹫��������Դ,֧�ֶ�̬��������Դ��������Ϣ���л��ȹ���<br>
 * ע�⣺ 1,ʹ��ǰ�����������appName,appDsName,dbmode,configPath��ֵ�����ҵ���init�������г�ʼ��;
 * 2,��configPath��ȡ������Ϣ: <br>
 * <bean id="testZdalDataSource" class="com.alipay.zdal.client.jdbc.ZdalDataSource" init-method="init" destroy-method="close"> 
 *      <property name="appName" value="appName"/> 
 *      <property name="appDsName" value="appDsName"/> 
 *      <property name="dbmode" value="dev"/> 
 *      <property name="configPath" value="/home/admin/appName-run/jboss/deploy"/> 
 * </bean>
 * 
 * @author ����
 * @version $Id: ZdalDataSource.java, v 0.1 2012-11-17 ����4:08:43 Exp $
 */
public class ZdalDataSource extends AbstractZdalDataSource implements DataSource, Closable {

    public void init() {
        if (super.inited.get() == true) {
            throw new ZdalClientException("ERROR ## init twice");
        }
        try {
            super.initZdalDataSource();
        } catch (Exception e) {
            CONFIG_LOGGER.error("zdal init fail,config:" + this.toString(), e);
            throw new ZdalClientException(e);
        }

    }
}