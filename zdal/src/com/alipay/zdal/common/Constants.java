/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.alipay.zdal.common;

/**
 * 
 * @author ����
 * @version $Id: Constants.java, v 0.1 2013-1-10 ����03:31:24 Exp $
 */
public interface Constants {
    /** config��Ϣ���ʱ����¼��log����. */
    public static final String CONFIG_LOG_NAME_LOGNAME           = "zdal-client-config";

    /** ��ӡzdatasource���ӳ�״̬��log����. */
    public static final String ZDAL_DATASOURCE_POOL_LOGNAME      = "zdal-datasource-pool";

    /**  ���������ļ��� ���ͣ� DS OR��RULE*/
    public static final int    LOCAL_CONFIG_DS                   = 0;

    public static final int    LOCAL_CONFIG_RULE                 = 1;

    /**  ���������ļ������Ƹ�ʽ��appName-dbmode-ds.xml*/
    public static final String LOCAL_CONFIG_FILENAME_SUFFIX      = "{0}-{1}-ds.xml";

    /**  ���������ļ������Ƹ�ʽ��appName-dbmode-rule.xml*/
    public static final String LOCAL_RULE_CONFIG_FILENAME_SUFFIX = "{0}-{1}-rule.xml";

    public static final String DBINDEX_DSKEY_CONN_CHAR           = "_";

    public static final String DBINDEX_DS_GROUP_KEY_PREFIX       = "group_";

}
