/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.datasource.keyweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.RuntimeConfigHolder;

/**
 * trade-failover��������ԴȨ�ع���ĺ�����
 * ��Ҫ���Master/Failover����ԴȨ�صĹ�������Ȩ���������һ��db��š�
 * @author zhaofeng.wang
 * @version $Id: TDataSourceKeyWeightRandom.java,v 0.1 2012-5-2 ����10:29:56 zhaofeng.wang Exp $
 */
public class ZdalDataSourceKeyWeightRandom {

    private static final Logger               logger             = Logger
                                                                     .getLogger(ZdalDataSourceKeyWeightRandom.class);

    /**
     * ÿ�����ڵ�����Դ�ĸ�����
     */
    private final int                         dataSourceNumberInGroup;
    /**
     * ���������ԴȨ�أ�����keyΪ����Դ��ʶ��valueΪ��Ӧ��Ȩ��
     */
    private final Map<String, Integer>        cachedWeightConfig = new HashMap<String, Integer>();
    /**
     * ����ʱ���������Ȩ�ص�����
     */
    private final RuntimeConfigHolder<Weight> weightHolder       = new RuntimeConfigHolder<Weight>();

    /**
     * ���ֲ������ֻ���ؽ��������޸�
     */
    private static class Weight {
        public Weight(int[] weights, String[] weightKeys, int[] weightAreaEnds) {
            this.weightKeys = weightKeys;
            this.weightValues = weights;
            this.weightAreaEnds = weightAreaEnds;
        }

        /**
         * ����Դ�ı�ʶ��Ϊweight��key�������߱�֤�����޸���Ԫ��
         */
        public final String[] weightKeys;
        /**
         * ����Դ��Ȩ�أ��������weightKeys�е�ֵһһ��Ӧ�������߱�֤�����޸���Ԫ��
         */
        public final int[]    weightValues;
        /**
         * ���������Ȩ������Σ���������key��value��Ӧ�������߱�֤�����޸���Ԫ��
         */
        public final int[]    weightAreaEnds;
    }

    /**
     * ��ʼ��Ȩ�ػ��棬�Լ�����Ȩ�������
     * @param weightKeys  ����Դkey
     * @param weights     ������Դkeyһһ��Ӧ��Ȩ��ֵ
     */
    public ZdalDataSourceKeyWeightRandom(String[] weightKeys, int[] weights) {

        for (int i = 0; i < weightKeys.length; i++) {
            this.cachedWeightConfig.put(weightKeys[i], weights[i]);
        }
        int[] weightAreaEnds = genAreaEnds(weights);
        this.dataSourceNumberInGroup = weightKeys.length;
        weightHolder.set(new Weight(weights, weightKeys, weightAreaEnds));
    }

    public Map<String, Integer> getWeightConfig() {
        return this.cachedWeightConfig;
    }

    /**
     * ����������ڸ���Ȩ�ز��������
     */
    private final Random random = new Random();

    /**
     * 
     * ����������Ȩ��    10   9   8
     * ��ôareaEnds����  10  19  27
     * �������0~27֮���һ����������ȥ��areaEnds���Ԫ�رȣ������������С��ĳԪ�أ����ʾӦ��ѡ�����Ԫ��,�����ظ�Ԫ�ص��±�š�
     * 
     * ע�⣺�÷������ܸı������������,����ʵ�ֱ�֤���ܸı�w���κ���������ݣ������̲߳���ȫ
     * @return int 
     */
    public int select() {
        final Weight w = weightHolder.get();
        int[] areaEnds = w.weightAreaEnds;
        int sum = areaEnds[areaEnds.length - 1];
        if (sum == 0) {
            logger.error("��������ԴȨ��ȫ��Ϊ0��areaEnds: " + intArray2String(areaEnds));
            return -1;
        }
        int rand = random.nextInt(sum);
        for (int i = 0; i < areaEnds.length; i++) {
            if (rand < areaEnds[i]) {
                return i;
            }
        }
        logger.error("Choose the dataSource in the areaEnds failed, the rand=" + rand
                     + ", areaEnds:" + intArray2String(areaEnds));
        return -1;
    }

    /**
     * ����Ȩ������
     * 
     * @param weights  ����Դ��Ȩ������
     * @return   Ȩ������
     */
    private static int[] genAreaEnds(int[] weights) {
        if (weights == null) {
            return null;
        }
        int[] areaEnds = new int[weights.length];
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
            areaEnds[i] = sum;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("generate areaEnds" + intArray2String(areaEnds) + " from weights"
                         + intArray2String(weights));
        }
        if (sum == 0) {
            logger.warn("generate areaEnds" + intArray2String(areaEnds) + " from weights"
                        + intArray2String(weights));
        }
        return areaEnds;
    }

    /**
     * Ȩ��������־���ת��
     * @param inta Ȩ������
     * @return  ��ʽ�������־�ַ���
     */
    private static String intArray2String(int[] inta) {
        if (inta == null) {
            return "null";
        } else if (inta.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(inta[0]);
        for (int i = 1; i < inta.length; i++) {
            sb.append(", ").append(inta[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * ��ȡ���е�db�ı�ʶ�ϲ�����ַ���
     *  
     * @return db��ʶ�����ַ���
     */
    public String getAllDbKeys() {
        StringBuilder sb = new StringBuilder();
        final Weight w = weightHolder.get();
        sb.append("[").append(w.weightKeys[0]);
        for (int i = 1; i < w.weightKeys.length; i++) {
            sb.append(",").append(w.weightKeys[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * ����Ȩ�������ȡdb�����
     * @param excludeNums  ������db��ŵļ���
     * @return   db����ţ���һ���������ã���Ҫ��Χ��У��
     */
    public int getRandomDBIndexByWeight(List<Integer> excludeNums) {
        final Weight w = weightHolder.get();
        int weights[] = w.weightValues;
        List<Integer> dbIndexes = new ArrayList<Integer>();
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > 0 && !(excludeNums.contains(i))) {
                dbIndexes.add(i);
            }
        }
        int size = dbIndexes.size();
        if (size <= 0) {
            throw new IllegalArgumentException("û�п��õ�����Դ�ˣ�Ȩ��ȫ��Ϊ0��");
        }
        int rand = random.nextInt(size);
        return dbIndexes.get(rand);
    }

    /**
      * ���ݴ����db���кţ��ж�db�Ƿ����
      * 
      * @param dbNumber  db���к�
      * @return          ��ǰdb�Ƿ����
      */
    public boolean isDataBaseAvailable(int dbNumber) {
        final Weight w = weightHolder.get();
        int weights[] = w.weightValues;
        if (weights[dbNumber] > 0) {
            return true;
        }
        return false;
    }

    /**
     * ���ز����õ����ݿ����м���
     * 
     * @return
     */
    public List<Integer> getNotAvailableDBIndexes() {
        final Weight w = weightHolder.get();
        int weights[] = w.weightValues;
        List<Integer> dbIndexes = new ArrayList<Integer>();
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] <= 0) {
                dbIndexes.add(i);
            }
        }
        return dbIndexes;
    }

    /**
     * ���ؿ��õ����ݿ����м���
     * 
     * @return
     */
    public List<Integer> getAvailableDBIndexes() {
        final Weight w = weightHolder.get();
        int weights[] = w.weightValues;
        List<Integer> dbIndexes = new ArrayList<Integer>();
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > 0) {
                dbIndexes.add(i);
            }
        }
        return dbIndexes;
    }

    /**
     * ����db��Ż�ȡdb��key��ʶ
     * @param number  db���
     * @return   db��ʶ
     */
    public String getDBKeyByNumber(int number) {
        final Weight w = weightHolder.get();
        if (number >= w.weightKeys.length) {
            throw new IllegalArgumentException("The db number is out of scope, number= " + number
                                               + ",the largest is " + w.weightKeys.length);
        }
        return w.weightKeys[number];
    }

    public String[] getDBWeightKeys() {
        final Weight w = weightHolder.get();
        return w.weightKeys;
    }

    public int[] getDBWeightValues() {
        final Weight w = weightHolder.get();
        return w.weightValues;
    }

    /**
     * ����db��Ż�ȡdb��Ȩ��
     * @param number  db���
     * @return   db Ȩ��
     */
    public int getDBWeightByNumber(int number) {
        final Weight w = weightHolder.get();
        if (number >= w.weightKeys.length) {
            throw new IllegalArgumentException("The db number is out of scope, number= " + number
                                               + ",the largest is " + w.weightKeys.length);
        }
        return w.weightValues[number];
    }

    /**
     * ��ȡ���е�����Դ��ʶ
     * 
     * @return ����Դ��ʶ��������
     */
    public String[] getDBKeysArray() {
        String keys[] = weightHolder.get().weightKeys;
        return keys;
    }

    public int getDataSourceNumberInGroup() {
        return dataSourceNumberInGroup;
    }
}
