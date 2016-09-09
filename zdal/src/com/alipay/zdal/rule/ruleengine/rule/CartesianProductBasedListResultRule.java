/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.ruleengine.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alipay.zdal.common.lang.StringUtil;
import com.alipay.zdal.common.sqljep.function.Comparative;
import com.alipay.zdal.rule.ruleengine.cartesianproductcalculator.CartesianProductCalculator;
import com.alipay.zdal.rule.ruleengine.cartesianproductcalculator.SamplingField;
import com.alipay.zdal.rule.ruleengine.enumerator.Enumerator;
import com.alipay.zdal.rule.ruleengine.enumerator.EnumeratorImp;
import com.alipay.zdal.rule.ruleengine.util.RuleUtils;

/**
 * �������һ�����Ĺ���
 * 
 * 
 */
public abstract class CartesianProductBasedListResultRule extends ListAbstractResultRule {

    private static final Logger log        = Logger
                                               .getLogger(CartesianProductBasedListResultRule.class);
    Enumerator                  enumerator = new EnumeratorImp();

    /**
     * �Ƿ���Ҫ�Խ����ڵ�����ȡ������
     *
     * @see com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule#eval(java.util.Map)
     */
    public Map<String/*�����ֵ����db��index��table��index */, Field> eval(

    Map<String, Comparative> argumentsMap) {
        Map<String, Set<Object>> enumeratedMap = prepareEnumeratedMap(argumentsMap);//������㼯��
        if (log.isDebugEnabled()) {
            log.debug("Sampling filed message : " + enumeratedMap);
        }
        Map<String, Field> map = evalElement(enumeratedMap);
        decideWhetherOrNotToThrowSpecEmptySetRuntimeException(map);//�����Ƿ��׳�runtimeException
        return map;
    }

    /**
     * �����Ƿ��׳�runtimeException
     * 
     * @param map
     */
    private void decideWhetherOrNotToThrowSpecEmptySetRuntimeException(Map<String, Field> map) {
        if ((map == null || map.isEmpty()) && ruleRequireThrowRuntimeExceptionWhenSetIsEmpty()) {
            throw new EmptySetRuntimeException();
        }
    }

    /**
     * TODO:���Ҫ�ᵽ���෽����
     * @param argumentsMap
     * @return
     */
    public Map<String, Set<Object>> prepareEnumeratedMap(Map<String, Comparative> argumentsMap) {
        if (log.isDebugEnabled()) {
            log.debug("eval at CartesianProductRule ,param is " + argumentsMap);
        }

        Map<String/* column */, Set<Object>/* ��� */> enumeratedMap = RuleUtils.getSamplingField(
            argumentsMap, parameters);
        return enumeratedMap;
    }

    /** 
     * @see com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule#evalWithoutSourceTrace(java.util.Map, java.lang.String, java.util.Set)
     */
    public Set<String> evalWithoutSourceTrace(Map<String, Set<Object>> enumeratedMap,
                                              String mappingTargetColumn, Set<Object> mappingKeys) {
        //        Set<String> set = null;

        if (enumeratedMap.size() == 0) {
            return evalZeroArgumentExpression();

        } else if (enumeratedMap.size() == 1) {
            return evalOneArgumentExpression(enumeratedMap, mappingTargetColumn, mappingKeys);

        } else {

            return evalMutiargumentsExpression(enumeratedMap, mappingTargetColumn, mappingKeys);
        }
    }

    private Set<String> evalMutiargumentsExpression(Map<String, Set<Object>> enumeratedMap,
                                                    String mappingTargetColumn,
                                                    Set<Object> mappingKeys) {
        Set<String> set;
        if (mappingTargetColumn != null || mappingKeys != null) {
            throw new IllegalArgumentException("����ö�ٲ�֧��ʹ��ӳ�����");
        }

        // TODO:�õ����ֵ��ͬ�����ֿ��ֱ��ʱ����Ҫreview
        // ����һ��ֵ����Ҫ���еѿ�����
        CartesianProductCalculator cartiesianProductCalculator = new CartesianProductCalculator(
            enumeratedMap);
        /*
         * ȷʵ����ȷ��set�Ĵ�С����һ����˵�ֿ���16������������Ͷ�16����ʱ������һ�ֿ��ܵĿ����ǽ�
         * capacity����Ϊ�����ܳ��ֵĽ����
         */
        set = new HashSet<String>(16);
        for (SamplingField samplingField : cartiesianProductCalculator) {
            evalOnceAndAddToReturnSet(set, samplingField, 16);
        }

        return set;
    }

    /**
     * û�в����������������contextӦ����д��
     * @param enumeratedMap
     * @param mappingTargetColumn
     * @param mappingKeys
     * @return
     */
    private Set<String> evalZeroArgumentExpression() {
        Set<String> set;
        // ����һ��ֵ����Ҫ���еѿ�����
        List<String> columns = new ArrayList<String>(1);

        SamplingField samplingField = new SamplingField(columns, 1);

        // ����ֵ���Ҳ�����뺯����x�ĸ������Ӧ
        set = new HashSet<String>();

        /*
         * ���û��ǰ�˴������Ѿ�ӳ��Ľ������ôʹ��sql�л�õĽ������������
         */
        evalOnceAndAddToReturnSet(set, samplingField, 0);

        if ((set == null || set.isEmpty()) && ruleRequireThrowRuntimeExceptionWhenSetIsEmpty()) {
            throw new EmptySetRuntimeException();
        }
        return set;
    }

    private Set<String> evalOneArgumentExpression(Map<String, Set<Object>> enumeratedMap,
                                                  String mappingTargetColumn,
                                                  Set<Object> mappingKeys) {
        Set<String> set;
        // ����һ��ֵ����Ҫ���еѿ�����
        List<String> columns = new ArrayList<String>(1);
        Set<Object> enumeratedValues = null;
        for (Entry<String, Set<Object>> entry : enumeratedMap.entrySet()) {
            columns.add(entry.getKey());
            enumeratedValues = entry.getValue();
        }

        SamplingField samplingField = new SamplingField(columns, 1);

        // ����ֵ���Ҳ�����뺯����x�ĸ������Ӧ
        set = new HashSet<String>(enumeratedValues.size());

        if (mappingKeys == null) {
            /*
             * ���û��ǰ�˴������Ѿ�ӳ��Ľ������ôʹ��sql�л�õĽ������������
             */
            evalNormal(set, enumeratedValues, samplingField);
        } else {
            //mappingKeys ��Ϊ�գ���ô֤���ֿ�ʱ�Ѿ�������ӳ�������ô���ж�ӳ����ֵ�Ƿ���ȷ��
            //Ȼ��Ҫ����������ʹ��ӳ�������ʹ��sql��������ݣ����������������ȥ��
            evalWithMappingKey(mappingTargetColumn, mappingKeys, set, enumeratedValues,
                samplingField);
        }

        if ((set == null || set.isEmpty()) && ruleRequireThrowRuntimeExceptionWhenSetIsEmpty()) {
            throw new EmptySetRuntimeException();
        }
        return set;
    }

    /**
     * ��������Ҫ����������ֿ�ʱ�Ѿ��鵽ӳ������ݣ�
     * ��ʹ��ӳ������ݡ�
     * 
     * �б�ı�־��mappingKeys��Ϊ�ա�
     * 
     * @param mappingTargetColumn
     * @param mappingKeys
     * @param set
     * @param enumeratedValues
     * @param samplingField
     * 
     * 
     */
    private void evalWithMappingKey(String mappingTargetColumn, Set<Object> mappingKeys,
                                    Set<String> set, Set<Object> enumeratedValues,
                                    SamplingField samplingField) {
        //����targetKey,������������targetKey���Լ���targetKey�ȶԣ���֪�Ƿ�ͷֿ�ʹ����ͬ����targetKey
        samplingField.setMappingTargetKey(mappingTargetColumn);
        if (mappingKeys.size() == enumeratedValues.size()) {
            Iterator<Object> itr = mappingKeys.iterator();
            for (Object value : enumeratedValues) {
                Object oneTargetKey = itr.next();
                samplingField.clear();
                samplingField.setMappingValue(oneTargetKey);
                samplingField.add(0, value);
                evalOnceAndAddToReturnSet(set, samplingField, enumeratedValues.size());
            }
        } else {
            throw new IllegalArgumentException("mappingӳ����targetKeys������Ĳ�����������,mapping :"
                                               + mappingKeys + " " + "enumeratedValues is :"
                                               + enumeratedValues);
        }
    }

    private void evalNormal(Set<String> set, Set<Object> enumeratedValues,
                            SamplingField samplingField) {
        for (Object value : enumeratedValues) {
            samplingField.clear();
            samplingField.add(0, value);
            evalOnceAndAddToReturnSet(set, samplingField, enumeratedValues.size());
        }
    }

    /** 
     * �����ļ�����̣�����->���������������м��㣬��ȡ���ս����
     * 
     * @param enumeratedMap
     * @return ���ص�map����Ϊnull,���п���Ϊ�յ�map�����map��Ϊ�գ����ڲ�����map�ض���Ϊ�ա����ٻ���һ��ֵ
     */
    public Map<String/* �����ֵ */, Field> evalElement(Map<String, Set<Object>> enumeratedMap) {
        Map<String/* �����ֵ */, Field> map;
        if (enumeratedMap.size() == 1) {
            // �и�������һ��ֵ����Ҫ���еѿ�����
            List<String> columns = new ArrayList<String>(1);
            Set<Object> enumeratedValues = null;
            for (Entry<String, Set<Object>> entry : enumeratedMap.entrySet()) {
                columns.add(entry.getKey());
                enumeratedValues = entry.getValue();
            }

            SamplingField samplingField = new SamplingField(columns, 1);
            // ����ֵ���Ҳ�����뺯����x�ĸ������Ӧ
            map = new HashMap<String, Field>(enumeratedValues.size());
            // Ϊ�����и��������ֶ�
            for (Object value : enumeratedValues) {
                samplingField.clear();
                samplingField.add(0, value);
                evalOnceAndAddToReturnMap(map, samplingField, enumeratedValues.size());
            }

            return map;

        }
        /**
         * ��ʹ��GroovyThreadLocal ��ʽע�����ʱ����Ϊsql��û�ж�Ӧ�����������ߵ����
         * �����棬ֱ�Ӹ����û���GroovyThreadLocal��Ĳ���������õĹ�����м�������طֱ��ֿ�Y��
         */
        else if (enumeratedMap.size() == 0) {
            List<String> columns = new ArrayList<String>(1);
            SamplingField samplingField = new SamplingField(columns, 1);
            map = new HashMap<String, Field>(1);
            evalOnceAndAddToReturnMap(map, samplingField, 1);
            return map;

        } else {
            //TODO:Ĭ�Ϲرյ�
            // TODO:�õ����ֵ��ͬ�����ֿ��ֱ��ʱ����Ҫreview
            // ����һ��ֵ����Ҫ���еѿ�����
            CartesianProductCalculator cartiesianProductCalculator = new CartesianProductCalculator(
                enumeratedMap);
            /*
             * ȷʵ����ȷ��set�Ĵ�С����һ����˵�ֿ���16������������Ͷ�16����ʱ������һ�ֿ��ܵĿ����ǽ�
             * capacity����Ϊ�����ܳ��ֵĽ����
             */
            map = new HashMap<String, Field>(16);
            for (SamplingField samplingField : cartiesianProductCalculator) {
                evalOnceAndAddToReturnMap(map, samplingField, 16);
            }

            return map;
        }
    }

    /**
     * ����ӹ�����Ҫ�ڷ���ֵΪnull��Ϊ��collectionsʱ�׳��쳣����̳д����false��Ϊtrue����
     * 
     * @return
     */
    protected boolean ruleRequireThrowRuntimeExceptionWhenSetIsEmpty() {
        return false;
    }

    void evalOnceAndAddToReturnSet(Set<String> set, SamplingField samplingField, int valueSetSize) {
        ResultAndMappingKey resultAndMappingKey = evalueateSamplingField(samplingField);
        String targetIndex = resultAndMappingKey.result;
        //ODOT:�ظ��ж�
        if (targetIndex != null) {
            set.add(targetIndex);
        } else {
            throw new IllegalArgumentException("��������Ľ������Ϊnull");
        }
    }

    /**
     * ��һ�����ݽ��м���
     * ��һ�ι����п��ܷ��ض���⣬����forѭ������
     * ֻ�������ݼ����ȡ��ֵ��ʱ��ŻὫ��Ӧ��ֵ��ȡ���кͶ������ڵ�ֵ����map�С�
    * @param map
    * @param samplingField
    * @param valueSetSize
    * @Test ���������TairBasedMappingRule�ļ��ɲ��Ժ͵�Ԫ�����ﶼ��
    */
    void evalOnceAndAddToReturnMap(Map<String/* �����ֵ */, Field> map, SamplingField samplingField,
                                   int valueSetSize) {
        ResultAndMappingKey returnAndMappingKey = evalueateSamplingField(samplingField);
        if (returnAndMappingKey != null) {
            String dbIndexStr = returnAndMappingKey.result;
            if (StringUtil.isBlank(dbIndexStr)) {
                throw new IllegalArgumentException("����dbRule������Ľ������Ϊnull");
            }
            String[] dbIndexes = dbIndexStr.split(",");

            for (String dbIndex : dbIndexes) {
                List<String> lists = samplingField.getColumns();
                List<Object> values = samplingField.getEnumFields();

                Field colMap = prepareColumnMap(map, samplingField, dbIndex,
                    returnAndMappingKey.mappingTargetColumn, returnAndMappingKey.mappingKey);
                int index = 0;
                for (String column : lists) {
                    Object value = values.get(index);
                    Set<Object> set = prepareEnumeratedSet(valueSetSize, colMap, column);
                    set.add(value);
                    index++;
                }
            }
        }
    }

    private Set<Object> prepareEnumeratedSet(int valueSetSize, Field colMap, String column) {
        //sourcekey ��ʼ���Ժ���ڲ���set��һֱ����
        Set<Object> set = colMap.sourceKeys.get(column);
        if (set == null) {
            set = new HashSet<Object>(valueSetSize);
            colMap.sourceKeys.put(column, set);
        }
        return set;
    }

    private Field prepareColumnMap(Map<String, Field> map, SamplingField samplingField,
                                   String targetIndex, String mappngTargetColumn,
                                   Object mappingValue) {
        Field colMap = map.get(targetIndex);
        if (colMap == null) {
            int size = samplingField.getColumns().size();
            colMap = new Field(size);
            map.put(targetIndex, colMap);
        }

        if (mappngTargetColumn != null && colMap.mappingTargetColumn == null) {
            colMap.mappingTargetColumn = mappngTargetColumn;
        }
        if (mappingValue != null) {
            if (colMap.mappingKeys == null) {
                colMap.mappingKeys = new HashSet<Object>();
            }
            colMap.mappingKeys.add(mappingValue);
        }

        return colMap;
    }

    //	public Map<String, Set<Object>/* ����������key��ֵ��pair */> getSamplingField(
    //			Map<String, SharedValueElement> sharedValueElementMap) {
    //		// TODO:��ϸע��,����ѿ�����
    //		// ö���Ժ��columns�����ǵ����֮��Ķ�Ӧ��ϵ
    //		Map<String, Set<Object>> enumeratedMap = new HashMap<String, Set<Object>>(
    //				sharedValueElementMap.size());
    //		for (Entry<String, SharedValueElement> entry : sharedValueElementMap
    //				.entrySet()) {
    //			SharedValueElement sharedValueElement = entry.getValue();
    //			String key = entry.getKey();
    //			// ��ǰenumerator��ָ����ǰ�����Ƿ���Ҫ���������⡣
    //			// enumerator.setNeedMergeValueInCloseInterval();
    //
    //			try {
    //				Set<Object> samplingField = enumerator.getEnumeratedValue(
    //						sharedValueElement.comp,
    //						sharedValueElement.cumulativeTimes,
    //						sharedValueElement.atomicIncreaseValue,
    //						sharedValueElement.needMergeValueInCloseInterval);
    //				enumeratedMap.put(key, samplingField);
    //			} catch (UnsupportedOperationException e) {
    //				throw new UnsupportedOperationException("��ǰ�зֿ�ֱ���ִ��󣬳��ִ����������:"
    //						+ entry.getKey(), e);
    //			}
    //
    //		}
    //		return enumeratedMap;
    //	}

    /**
     * ����һ������������һ�����
     * 
     * @return ͨ������Ľ�����������������Ϊnull:
     * 			ӳ�����ԭ������ڣ���ӳ����Ŀ�겻���ڣ��᷵��null��
     *          ����ʱ�̣������쳣
     * 
     */
    public abstract ResultAndMappingKey evalueateSamplingField(SamplingField samplingField);

}
