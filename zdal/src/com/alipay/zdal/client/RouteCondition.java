/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.client;

/**
 * ͨ�ýӿڣ�����ͨ����д{@link RouteHandler} ����ע�ᵽ{@link RouteHandlerRegister}
 * �ϵķ�ʽ�������µ��Զ���conditionHandler.�����Ϳ���֧�ֶ��ֲ�ͬ�������Լ����� {@link RouteHandlerRegister}
 * �е�keyΪRouteConditionʵ�ֵ�classȫ���ơ�
 * 
 * 
 * 
 */
public interface RouteCondition {
    public String getVirtualTableName();

}
