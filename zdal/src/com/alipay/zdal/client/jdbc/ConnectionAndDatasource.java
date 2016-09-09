/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * ��Ϊ������datasourceΪkey,�����Ҫ��parent datasource����Ҳ���뵽��ʱ����Ŀ����������С�
 * 
 * ��3����������ֱ��ͨ��sql��������ķֿ����Ӧ�ģ�ͨ����3������
 * 
 * ����������1�����¸���dbSelectorѡ��datasource.
 *         2������parentDataSource ��dbSelector���Ƴ���ǰ�������parentDataSource
 *         3����ȡ���������ӡ�
 * 
 * 
 *
 */
public class ConnectionAndDatasource {
    /**
     * ���Ŷ����� connectionҲ�᲻�Ϸ����仯��
     */
    Connection connection;
    DataSource parentDataSource;
    /**
     * dbselector�����������������TDatasource�г��е�dbSelector.
     * ��������������ԣ�����е��Ǿ���copy����ȥ����������datasource�����dbSelector
     */
    DBSelector dbSelector;
}
