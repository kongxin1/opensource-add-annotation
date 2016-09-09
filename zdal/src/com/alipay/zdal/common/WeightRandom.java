/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * 
 * @author ����
 * @version $Id: WeightRandom.java, v 0.1 2014-1-6 ����05:17:44 Exp $
 */
public class WeightRandom {

    private static final Logger               logger                 = Logger
                                                                         .getLogger(WeightRandom.class);

    public static final int                   DEFAULT_WEIGHT_NEW_ADD = 0;
    public static final int                   DEFAULT_WEIGHT_INIT    = 10;

    private Map<String, Integer>              cachedWeightConfig;
    private final RuntimeConfigHolder<Weight> weightHolder           = new RuntimeConfigHolder<Weight>();

    /**
     * ���ֲ������ֻ���ؽ��������޸�
     */
    private static class Weight {
        public Weight(int[] weights, String[] weightKeys, int[] weightAreaEnds) {
            this.weightKeys = weightKeys;
            this.weightValues = weights;
            this.weightAreaEnds = weightAreaEnds;
        }

        public final String[] weightKeys;    //�����߱�֤�����޸���Ԫ��
        public final int[]    weightValues;  //�����߱�֤�����޸���Ԫ��
        public final int[]    weightAreaEnds; //�����߱�֤�����޸���Ԫ��
    }

    public WeightRandom(Map<String, Integer> weightConfigs) {
        this.init(weightConfigs);
    }

    public WeightRandom(String[] keys) {
        Map<String, Integer> weightConfigs = new HashMap<String, Integer>(keys.length);
        for (String key : keys) {
            weightConfigs.put(key, DEFAULT_WEIGHT_INIT);
        }
        this.init(weightConfigs);
    }

    private void init(Map<String, Integer> weightConfig) {
        this.cachedWeightConfig = weightConfig;
        String[] weightKeys = weightConfig.keySet().toArray(new String[0]);
        int[] weights = new int[weightConfig.size()];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = weightConfig.get(weightKeys[i]);
        }
        int[] weightAreaEnds = genAreaEnds(weights);
        weightHolder.set(new Weight(weights, weightKeys, weightAreaEnds));
    }

    /**
     * ֧�ֶ�̬�޸�
     */
    public void setWeightConfig(Map<String, Integer> weightConfig) {
        this.init(weightConfig);
    }

    public Map<String, Integer> getWeightConfig() {
        return this.cachedWeightConfig;
    }

    /**
     * ����������Ȩ��    10   9   8
     * ��ôareaEnds����  10  19  27
     * �������0~27֮���һ����
     * 
     * �ֱ�ȥ����areaEnds���Ԫ�رȡ�
     * 
     * ���������С��һ��Ԫ���ˣ����ʾӦ��ѡ�����Ԫ��
     * 
     * ע�⣺�÷������ܸı������������
     */
    private final Random random = new Random();

    private String select(int[] areaEnds, String[] keys) {
        int sum = areaEnds[areaEnds.length - 1];
        if (sum == 0) {
            logger.error("areaEnds: " + intArray2String(areaEnds));
            return null;
        }
        //ѡ��Ĺ�
        //findbugs��Ϊ���ﲻ�Ǻܺ�(ÿ�ζ��½�һ��Random)(guangxia)
        int rand = random.nextInt(sum);
        for (int i = 0; i < areaEnds.length; i++) {
            if (rand < areaEnds[i]) {
                return keys[i];
            }
        }
        return null;
    }

    /**
     * ͨ������Դ�ı�ʶ��ȡ��Ӧ��Ȩ�أ�
     * @param key
     * @return
     */
    public int getWeightByKey(String key) {
        int weight = 0;
        boolean flag = false;
        final Weight w = weightHolder.get();
        for (int k = 0; k < w.weightKeys.length; k++) {
            if (w.weightKeys[k].equals(key)) {
                flag = true;
                weight = w.weightValues[k];
            }
        }
        if (flag == false) {
            logger.error("����Դ�ı�ʶ�����ڣ��Ƿ���key=" + key);
        }
        return weight;
    }

    /**
     * @param excludeKeys ��Ҫ�ų���key�б� 
     * @return
     */
    public String select(List<String> excludeKeys) {
        final Weight w = weightHolder.get(); //����ʵ�ֱ�֤���ܸı�w���κ���������ݣ������̲߳���ȫ
        if (excludeKeys == null || excludeKeys.isEmpty()) {
            return select(w.weightAreaEnds, w.weightKeys);
        }
        int[] tempWeights = w.weightValues.clone();
        for (int k = 0; k < w.weightKeys.length; k++) {
            if (excludeKeys.contains(w.weightKeys[k])) {
                tempWeights[k] = 0;
            }
        }
        int[] tempAreaEnd = genAreaEnds(tempWeights);
        return select(tempAreaEnd, w.weightKeys);
    }

    public static interface Tryer<T extends Throwable> {
        /**
         * @return null��ʾ�ɹ������򷵻�һ���쳣
         */
        public T tryOne(String name);
    }

    /**
     * @return null��ʾ�ɹ������򷵻�һ���쳣�б�
     */
    public <T extends Throwable> List<T> retry(int times, Tryer<T> tryer) {
        List<T> exceptions = new ArrayList<T>(0);
        List<String> excludeKeys = new ArrayList<String>(0);
        for (int i = 0; i < times; i++) {
            String name = this.select(excludeKeys);
            T e = tryer.tryOne(name);
            if (e != null) {
                exceptions.add(e);
                excludeKeys.add(name);
            } else {
                return null;
            }
        }
        return exceptions;
    }

    public <T extends Throwable> List<T> retry(Tryer<T> tryer) {
        return retry(3, tryer);
    }

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
            logger.debug("generate " + intArray2String(areaEnds) + " from "
                         + intArray2String(weights));
        }
        if (sum == 0) {
            logger.warn("generate " + intArray2String(areaEnds) + " from "
                        + intArray2String(weights));
        }
        return areaEnds;
    }

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
}
