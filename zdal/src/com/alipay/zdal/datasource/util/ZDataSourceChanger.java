/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.datasource.util;

import java.util.Map;

import org.apache.log4j.Logger;

import com.alipay.zdal.datasource.LocalTxDataSourceDO;
import com.alipay.zdal.datasource.ZDataSource;
import com.alipay.zdal.datasource.ZDataSourceFactory;
import com.alipay.zdal.datasource.resource.adapter.jdbc.local.LocalTxDataSource;

/**
 * 
 * 
 * @author liangjie.li
 * @version $Id: ZDataSourceChanger.java, v 0.1 2012-8-20 ����8:08:03 liangjie.li Exp $
 */
public class ZDataSourceChanger {
    private static final Logger logger = Logger.getLogger(ZDataSourceChanger.class);

    public static boolean configChange(Map<String, String> properties, ZDataSource zds) {
        boolean urlChange = ZDataSourceUtil.isChanged(properties, Parameter.JDBC_URL); //url�Ƿ�ı�
        boolean driverChange = ZDataSourceUtil.isChanged(properties, Parameter.DRIVER_CLASS);//driverclass �Ƿ�ı�

        //�ȸ���һ��,�������͵���ֵ����
        LocalTxDataSourceDO newDO = new LocalTxDataSourceDO();
        newDO.setDsName(zds.getDsName());
        ZDataSourceUtil.copyDS2DO(zds.getLocalTxDataSource(), newDO);
        ZDataSourceUtil.replaceValueFromMap(newDO, properties);

        if (driverChange && !urlChange) {//jboss��Ϊurl����driver
            logger.error("driverClass�������Ͳ���ʱ��connectionUrlҲ�����ͣ��������ͺ���");
            return false;
        }
        //connectionproperties�Ƿ�ı�
        boolean propChange = ZDataSourceUtil.solveConnectionProperties(newDO
            .getConnectionProperties(), zds.getLocalTxDataSource().getConnectionProperties());

        boolean dsFileChange = ZDataSourceUtil.isNeedReCreate(newDO, zds.getLocalTxDataSource(),
            propChange);

        if (dsFileChange) {
            return reCreatePool(newDO, zds);
        }
        return true;
    }

    /**
     * �ؽ�����Դ���ӳأ��Ƚ���һ���µ����ӳأ�Ȼ��;ɵĽ�������������ɵ�
     * 
     * @param newDO
     * @return
     */
    static boolean reCreatePool(LocalTxDataSourceDO newDO, ZDataSource zds) {
        try {
            long t1 = System.currentTimeMillis();
            LocalTxDataSource newDs = ZDataSourceFactory.createLocalTxDataSource(newDO, zds);
            LocalTxDataSource oldDs = zds.getLocalTxDataSource();

            zds.setLocalTxDataSource(newDs);

            oldDs.destroy();
            oldDs = null;

            logger.warn("���ӳ��Ѿ��ؽ�, cost " + (System.currentTimeMillis() - t1) + " ms");
            return true;
        } catch (Exception e) {
            logger.error("���ӳ��ؽ�ʧ��", e);
            return false;
        }

    }
}
