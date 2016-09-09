/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.client.dispatcher.DispatcherResult;
import com.alipay.zdal.client.dispatcher.EXECUTE_PLAN;
import com.alipay.zdal.parser.result.DefaultSqlParserResult;
import com.alipay.zdal.parser.result.SqlParserResult;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;

public class ControllerUtils {

    public static String toLowerCaseIgnoreNull(String tobeDone) {
        if (tobeDone != null) {
            return tobeDone.toLowerCase();
        }
        return null;
    }

    /**
     * ����ִ�мƻ�
     * 
     * ���б��ִ�мƻ�������ж��������Ķ����ĸ�����ͬ����ô���ձ�����������Ǹ�ֵΪ׼��
     * ������db1~5����ĸ����ֱ�Ϊ0,0,0,0,1:��ô���صı�ִ�мƻ�ΪSINGLE
     * ������ĸ����ֱ�Ϊ0,1,2,3,4,5����ô���ر��ִ�мƻ�ΪMULTIPLE.
     * @param dispatcherResult
     * @param targetDBList
     */
    public static void buildExecutePlan(DispatcherResult dispatcherResult,
                                        List<TargetDB> targetDBList) {
        if (targetDBList == null) {
            throw new IllegalArgumentException("targetDBList is null");
        }
        int size = targetDBList.size();
        switch (size) {
            case 0:
                dispatcherResult.setDatabaseExecutePlan(EXECUTE_PLAN.NONE);
                dispatcherResult.setTableExecutePlan(EXECUTE_PLAN.NONE);
                break;
            case 1:
                TargetDB targetDB = targetDBList.get(0);
                Set<String> set = targetDB.getTableNames();
                dispatcherResult.setTableExecutePlan(buildTableExecutePlan(set, null));
                //�����Ϊnone����ô��ҲΪnone.�����Ϊnone����ô��Ϊsingle
                if (dispatcherResult.getTableExecutePlan() != EXECUTE_PLAN.NONE) {
                    dispatcherResult.setDatabaseExecutePlan(EXECUTE_PLAN.SINGLE);
                } else {
                    dispatcherResult.setDatabaseExecutePlan(EXECUTE_PLAN.NONE);
                }
                break;
            default:
                EXECUTE_PLAN currentExeutePlan = EXECUTE_PLAN.NONE;
                for (TargetDB oneDB : targetDBList) {
                    currentExeutePlan = buildTableExecutePlan(oneDB.getTableNames(),
                        currentExeutePlan);
                }
                dispatcherResult.setTableExecutePlan(currentExeutePlan);
                if (dispatcherResult.getTableExecutePlan() != EXECUTE_PLAN.NONE) {
                    dispatcherResult.setDatabaseExecutePlan(EXECUTE_PLAN.MULTIPLE);
                } else {
                    dispatcherResult.setDatabaseExecutePlan(EXECUTE_PLAN.NONE);
                }
                break;
        }

    }

    private static EXECUTE_PLAN buildTableExecutePlan(Set<String> tableSet,
                                                      EXECUTE_PLAN currentExecutePlan) {
        if (currentExecutePlan == null) {
            currentExecutePlan = EXECUTE_PLAN.NONE;
        }
        EXECUTE_PLAN tempExecutePlan = null;
        if (tableSet == null) {
            throw new IllegalStateException("targetTab is null");
        }
        int tableSize = tableSet.size();
        //������Ϊ����
        switch (tableSize) {
            case 0:
                tempExecutePlan = EXECUTE_PLAN.NONE;
                break;
            case 1:
                tempExecutePlan = EXECUTE_PLAN.SINGLE;
                break;
            default:
                tempExecutePlan = EXECUTE_PLAN.MULTIPLE;
        }
        return tempExecutePlan.value() > currentExecutePlan.value() ? tempExecutePlan
            : currentExecutePlan;
    }

    /**
     * �������������ص�context���������Ŀǰ��Ҫ�ǽ����������
     * 
     * :1.���sql�д����˷��ϱ����滻pattern���ֶΣ����Ҳ��뱻�滻����
     * 2.���sql�а����˿���limit m,n�Ĳ�����
     * 
     * ����������Ϊ�����������Ҳ�����������˲����з���
     * @param args
     * @param dmlc
     * @param max
     * @param skip
     * @param retMeta
     * @param isMySQL
     * @param needRowCopy
     */
    public static void buildReverseOutput(List<Object> args, SqlParserResult dmlc, int max,
                                          int skip, DispatcherResult retMeta, boolean isMySQL) {
        boolean allowReverseOutput = retMeta.allowReverseOutput();
        List<TargetDB> targetdbs = retMeta.getTarget();
        for (TargetDB targetDB : targetdbs) {
            Set<String> tabs = targetDB.getTableNames();
            Map<Integer, Object> modifiedMap = new HashMap<Integer, Object>();
            // ���Ŀ�����ݿ�Ϊһ�����п����ǵ��ⵥ��򵥿���
            if (targetdbs.size() == 1) {
                Set<String> temp_tabs = targetdbs.get(0).getTableNames();
                if (temp_tabs.size() == 1) {
                    if (allowReverseOutput) {
                        // ����ֻ��Ҫ�ı��� //TODO
                        dmlc.getSqlReadyToRun(temp_tabs, args, skip, max, modifiedMap);
                    }
                } else {
                    mutiTableReverseOutput(args, dmlc, max, skip, retMeta, allowReverseOutput,
                        temp_tabs, modifiedMap);
                }
            } else {
                mutiTableReverseOutput(args, dmlc, max, skip, retMeta, allowReverseOutput, tabs,
                    modifiedMap);
            }
            if (retMeta.allowReverseOutput()) {
                targetDB.setChangedParams(modifiedMap);
            }
        }
    }

    /**
     * ��ⵥ�������򵥿�����Ҫ�ı�����ҳ��
     */
    private static void mutiTableReverseOutput(List<Object> args, SqlParserResult dmlc, int max,
                                               int skip, DispatcherResult retMeta,
                                               boolean allowReverseOutput,

                                               Set<String> tabs, Map<Integer, Object> modifiedMap) {
        if (allowReverseOutput) {
            //��ⵥ���������Ҫ�ı�����ҳ��
            dmlc.getSqlReadyToRun(tabs, args, 0, max, modifiedMap);
        } else {
            if (skip != DefaultSqlParserResult.DEFAULT_SKIP_MAX
                && max != DefaultSqlParserResult.DEFAULT_SKIP_MAX) {
                // skip max��Ĭ��ֵ������£�����Ҫ�и��Ƶ�����µķ������
                //��ⵥ�������򵥿�������£���Ȼ����������� ����ȻҪǿ�Ʒ��������֧�ָù���
                dmlc.getSqlReadyToRun(tabs, args, 0, max, modifiedMap);
                retMeta.needAllowReverseOutput(true);
            }
        }
    }

}
