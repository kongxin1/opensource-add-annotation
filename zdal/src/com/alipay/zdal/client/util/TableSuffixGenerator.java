/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.util;

import com.alipay.zdal.common.lang.StringUtil;

/**
 * ���ɱ��׺���࣬��Ҫ��failover���õ�
 * @author zhaofeng.wang
 * @version $Id: TableSuffixGenerator.java,v 0.1 2013-2-25 ����02:40:11 zhaofeng.wang Exp $
 */
public class TableSuffixGenerator {

    /**
     * ��ȡ���׺������ݶ�Ϊ 2
     * @param i ��Σ����׺��˳���
     * @return  ���롰0����ı��׺
     * 
     */
    public static String getTableSuffix(int i, int masterDBSize) {
        //Ĭ�϶�����λ�ĺ�׺.
        int lenth = (int) Math.ceil(Math.log10(masterDBSize));
        return StringUtil.alignRight(String.valueOf(i), lenth, '0');
    }

    /**
     * ���groupNum<10 ���=0,11,22,33,44,55,66,77,88,99
     * ���groupNum>=10&&groupNum<110���������£�
     *  10-1...19-10
     *  20-12...29-21
     *  30-23...39-32
     *  40-34...49-43
     *  50-45...59-54
     *  60-56...69-65
     *  70-67...79-76
     *  80-78...89-87
     *  90-89...99-98
     *  100-100...109-109
     * @param groupNum
     * @return
     */
    public static int trade50ConvertGroupNum(int groupNum) {
        if (groupNum < 0) {
            throw new IllegalArgumentException("ERROR ## the groupNum = " + groupNum + " is  < 0 ");
        }
        if (groupNum > 109) {
            throw new IllegalArgumentException("ERROR ## the groupNum = " + groupNum + " is >109 ");
        }
        if (groupNum < 10) {
            return groupNum + groupNum * 10;
        } else {
            return groupNum - (10 - groupNum / 10);
        }
    }

    public static void main(String[] args) {
        //        for (int i = 0; i < 110; i++) {
        //            System.out.println("i = " + i + ",result = " + trade50ConvertGroupNum(i));
        //        }
        //        trade50Convert(110);
    }

}
