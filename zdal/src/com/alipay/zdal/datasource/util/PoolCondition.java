/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.datasource.util;

/**
 * 
 * @author sicong.shou
 * @version $Id: PoolCondition.java, v 0.1 2012-11-19 ����08:19:23 sicong.shou Exp $
 */
public class PoolCondition {
    /** ����Դ���� */
    String dsName                   = "";
    /**��С������  */
    int    minSize                  = 0;
    /** ��������� */
    int    maxSize                  = 0;
    /** ���õ������� */
    long   availableConnectionCount = 0;
    /**  ��ǰ����Դ�����������*/
    int    connectionCount          = 0;
    /**  ��ǰ��ʹ���е�������*/
    long   inUseConnectionCount     = 0;
    /**  ��ʹ�ù�������������*/
    long   maxConnectionsInUseCount = 0;
    /** ������������ */
    int    connectionCreatedCount   = 0;
    /** ���ٵ������� */
    int    connectionDestroyedCount = 0;

    public PoolCondition(String dsName, int min, int max, long avl, int con, long inUse,
                         long maxInUse, int createCnt, int destroyCnt) {
        this.dsName = dsName;
        minSize = min;
        maxSize = max;
        availableConnectionCount = avl;
        connectionCount = con;
        inUseConnectionCount = inUse;
        maxConnectionsInUseCount = maxInUse;
        connectionCreatedCount = createCnt;
        connectionDestroyedCount = destroyCnt;
    }

    @Override
    public String toString() {
        //        return dsName + "\t��С������:" + minSize + "\t���������:" + maxSize + "\t���õ�������:"
        //               + availableConnectionCount + "\t��ǰ����Դ�����������:" + connectionCount + "\t��ǰ��ʹ���е�������:"
        //               + inUseConnectionCount + "\t��ʹ�ù�������������:" + maxConnectionsInUseCount + "\t�ܹ�������������:"
        //               + connectionCreatedCount + "\t�ܹ����ٵ�������:" + connectionDestroyedCount;
        return dsName + "[min:" + minSize + "-max:" + maxSize + "-canUse:"
               + availableConnectionCount + "-managed:" + connectionCount + "-using:"
               + inUseConnectionCount + "-maxUsed:" + maxConnectionsInUseCount + "-createCount:"
               + connectionCreatedCount + "-destroyCount:" + connectionDestroyedCount + "]";
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public long getAvailableConnectionCount() {
        return availableConnectionCount;
    }

    public void setAvailableConnectionCount(long availableConnectionCount) {
        this.availableConnectionCount = availableConnectionCount;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public long getInUseConnectionCount() {
        return inUseConnectionCount;
    }

    public void setInUseConnectionCount(long inUseConnectionCount) {
        this.inUseConnectionCount = inUseConnectionCount;
    }

    public long getMaxConnectionsInUseCount() {
        return maxConnectionsInUseCount;
    }

    public void setMaxConnectionsInUseCount(long maxConnectionsInUseCount) {
        this.maxConnectionsInUseCount = maxConnectionsInUseCount;
    }

    public int getConnectionCreatedCount() {
        return connectionCreatedCount;
    }

    public void setConnectionCreatedCount(int connectionCreatedCount) {
        this.connectionCreatedCount = connectionCreatedCount;
    }

    public int getConnectionDestroyedCount() {
        return connectionDestroyedCount;
    }

    public void setConnectionDestroyedCount(int connectionDestroyedCount) {
        this.connectionDestroyedCount = connectionDestroyedCount;
    }
}
